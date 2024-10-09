/*
 * Copyright (C) 2023 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.it;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import com.example.UnusedClass;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/** Integration test verifying behavior of shrunken and obfuscated JARs. */
@SuppressWarnings("MemberName") // class name must end with 'IT' for Maven Failsafe Plugin
@RunWith(Parameterized.class)
public class ShrinkingIT {
  // These JAR files are prepared by the Maven build
  public static final Path PROGUARD_RESULT_PATH = Paths.get("target/proguard-output.jar");
  public static final Path R8_RESULT_PATH = Paths.get("target/r8-output.jar");

  @Parameters(name = "{index}: {0}")
  public static List<Path> jarsToTest() {
    return Arrays.asList(PROGUARD_RESULT_PATH, R8_RESULT_PATH);
  }

  @Parameter public Path jarToTest;

  @Before
  public void verifyJarExists() {
    if (!Files.isRegularFile(jarToTest)) {
      fail("JAR file " + jarToTest + " does not exist; run this test with `mvn clean verify`");
    }
  }

  @FunctionalInterface
  interface TestAction {
    void run(Class<?> c) throws Exception;
  }

  private void runTest(String className, TestAction testAction) throws Exception {
    // Use bootstrap class loader; load all custom classes from JAR and not
    // from dependencies of this test
    ClassLoader classLoader = null;

    // Load the shrunken and obfuscated JARs with a separate class loader, then load
    // the main test class from it and let the test action invoke its test methods
    try (URLClassLoader loader =
        new URLClassLoader(new URL[] {jarToTest.toUri().toURL()}, classLoader)) {
      Class<?> c = loader.loadClass(className);
      testAction.run(c);
    }
  }

  @Test
  public void test() throws Exception {
    StringBuilder output = new StringBuilder();

    runTest(
        "com.example.Main",
        c -> {
          Method m = c.getMethod("runTests", BiConsumer.class);
          m.invoke(
              null,
              (BiConsumer<String, String>)
                  (name, content) -> output.append(name + "\n" + content + "\n===\n"));
        });

    assertThat(output.toString())
        .isEqualTo(
            String.join(
                "\n",
                "Write: TypeToken anonymous",
                "[",
                "  {",
                "    \"custom\": 1",
                "  }",
                "]",
                "===",
                "Read: TypeToken anonymous",
                "[ClassWithAdapter[3]]",
                "===",
                "Write: TypeToken manual",
                "[",
                "  {",
                "    \"custom\": 1",
                "  }",
                "]",
                "===",
                "Read: TypeToken manual",
                "[ClassWithAdapter[3]]",
                "===",
                "Write: Named fields",
                "{",
                "  \"myField\": 2,",
                "  \"notAccessedField\": -1",
                "}",
                "===",
                "Read: Named fields",
                "3",
                "===",
                "Write: SerializedName",
                "{",
                "  \"myField\": 2,",
                "  \"notAccessed\": -1",
                "}",
                "===",
                "Read: SerializedName",
                "3",
                "===",
                "Write: No args constructor",
                "{",
                "  \"myField\": -3",
                "}",
                "===",
                "Read: No args constructor; initial constructor value",
                "-3",
                "===",
                "Read: No args constructor; custom value",
                "3",
                "===",
                "Write: Constructor with args",
                "{",
                "  \"myField\": 2",
                "}",
                "===",
                "Read: Constructor with args",
                "3",
                "===",
                "Read: Unreferenced no args constructor; initial constructor value",
                "-3",
                "===",
                "Read: Unreferenced no args constructor; custom value",
                "3",
                "===",
                "Read: Unreferenced constructor with args",
                "3",
                "===",
                "Read: No JDK Unsafe; initial constructor value",
                "-3",
                "===",
                "Read: No JDK Unsafe; custom value",
                "3",
                "===",
                "Write: Enum",
                "\"FIRST\"",
                "===",
                "Read: Enum",
                "SECOND",
                "===",
                "Write: Enum SerializedName",
                "\"one\"",
                "===",
                "Read: Enum SerializedName",
                "SECOND",
                "===",
                "Write: @Expose",
                "{\"i\":0}",
                "===",
                "Write: Version annotations",
                "{\"i1\":0,\"i4\":0}",
                "===",
                "Write: JsonAdapter on fields",
                "{",
                "  \"f\": \"adapter-null\",",
                "  \"f1\": \"adapter-1\",",
                "  \"f2\": \"factory-2\",",
                "  \"f3\": \"serializer-3\",",
                // For f4 only a JsonDeserializer is registered, so serialization falls back to
                // reflection
                "  \"f4\": {",
                "    \"s\": \"4\"",
                "  }",
                "}",
                "===",
                "Read: JsonAdapter on fields",
                // For f3 only a JsonSerializer is registered, so for deserialization value is read
                // as is using reflection
                "ClassWithJsonAdapterAnnotation[f1=adapter-1, f2=factory-2, f3=3,"
                    + " f4=deserializer-4]",
                "===",
                "Read: Generic TypeToken",
                "{t=read-1}",
                "===",
                "Read: Using Generic",
                "{g={t=read-1}}",
                "===",
                "Read: Using Generic TypeToken",
                "{g={t=read-1}}",
                "===",
                ""));
  }

  @Test
  public void testNoSerializedName_NoArgsConstructor() throws Exception {
    runTest(
        "com.example.NoSerializedNameMain",
        c -> {
          Method m = c.getMethod("runTestNoArgsConstructor");

          if (jarToTest.equals(PROGUARD_RESULT_PATH)) {
            Object result = m.invoke(null);
            assertThat(result).isEqualTo("value");
          } else {
            // R8 performs more aggressive optimizations
            Exception e = assertThrows(InvocationTargetException.class, () -> m.invoke(null));
            assertThat(e)
                .hasCauseThat()
                .hasMessageThat()
                .isEqualTo(
                    "Abstract classes can't be instantiated! Adjust the R8 configuration or"
                        + " register an InstanceCreator or a TypeAdapter for this type. Class name:"
                        + " com.example.NoSerializedNameMain$TestClassNoArgsConstructor\n"
                        + "See https://github.com/google/gson/blob/main/Troubleshooting.md#r8-abstract-class");
          }
        });
  }

  @Test
  public void testNoSerializedName_NoArgsConstructorNoJdkUnsafe() throws Exception {
    runTest(
        "com.example.NoSerializedNameMain",
        c -> {
          Method m = c.getMethod("runTestNoJdkUnsafe");

          if (jarToTest.equals(PROGUARD_RESULT_PATH)) {
            Object result = m.invoke(null);
            assertThat(result).isEqualTo("value");
          } else {
            // R8 performs more aggressive optimizations
            Exception e = assertThrows(InvocationTargetException.class, () -> m.invoke(null));
            assertThat(e)
                .hasCauseThat()
                .hasMessageThat()
                .isEqualTo(
                    "Unable to create instance of class"
                        + " com.example.NoSerializedNameMain$TestClassNotAbstract; usage of JDK"
                        + " Unsafe is disabled. Registering an InstanceCreator or a TypeAdapter for"
                        + " this type, adding a no-args constructor, or enabling usage of JDK"
                        + " Unsafe may fix this problem. Or adjust your R8 configuration to keep"
                        + " the no-args constructor of the class.");
          }
        });
  }

  @Test
  public void testNoSerializedName_HasArgsConstructor() throws Exception {
    runTest(
        "com.example.NoSerializedNameMain",
        c -> {
          Method m = c.getMethod("runTestHasArgsConstructor");

          if (jarToTest.equals(PROGUARD_RESULT_PATH)) {
            Object result = m.invoke(null);
            assertThat(result).isEqualTo("value");
          } else {
            // R8 performs more aggressive optimizations
            Exception e = assertThrows(InvocationTargetException.class, () -> m.invoke(null));
            assertThat(e)
                .hasCauseThat()
                .hasMessageThat()
                .isEqualTo(
                    "Abstract classes can't be instantiated! Adjust the R8 configuration or"
                        + " register an InstanceCreator or a TypeAdapter for this type. Class name:"
                        + " com.example.NoSerializedNameMain$TestClassHasArgsConstructor\n"
                        + "See https://github.com/google/gson/blob/main/Troubleshooting.md#r8-abstract-class");
          }
        });
  }

  @Test
  public void testUnusedClassRemoved() throws Exception {
    // For some reason this test only works for R8 but not for ProGuard; ProGuard keeps the unused
    // class
    assumeTrue(jarToTest.equals(R8_RESULT_PATH));

    String className = UnusedClass.class.getName();
    ClassNotFoundException e =
        assertThrows(
            ClassNotFoundException.class,
            () -> {
              runTest(
                  className,
                  c -> {
                    fail("Class should have been removed during shrinking: " + c);
                  });
            });
    assertThat(e).hasMessageThat().contains(className);
  }
}
