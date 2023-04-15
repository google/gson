/*
 * Copyright (C) 2021 Google Inc.
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

package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Permission;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class ReflectionAccessTest {
  @SuppressWarnings("unused")
  private static class ClassWithPrivateMembers {
    private String s;

    private ClassWithPrivateMembers() {
    }
  }

  private static Class<?> loadClassWithDifferentClassLoader(Class<?> c) throws Exception {
    URL url = c.getProtectionDomain().getCodeSource().getLocation();
    URLClassLoader classLoader = new URLClassLoader(new URL[] { url }, null);
    return classLoader.loadClass(c.getName());
  }

  @SuppressWarnings("removal") // java.lang.SecurityManager deprecation in Java 17
  @Test
  public void testRestrictiveSecurityManager() throws Exception {
    // Must use separate class loader, otherwise permission is not checked, see Class.getDeclaredFields()
    Class<?> clazz = loadClassWithDifferentClassLoader(ClassWithPrivateMembers.class);

    final Permission accessDeclaredMembers = new RuntimePermission("accessDeclaredMembers");
    final Permission suppressAccessChecks = new ReflectPermission("suppressAccessChecks");
    SecurityManager original = System.getSecurityManager();
    SecurityManager restrictiveManager = new SecurityManager() {
      @Override
      public void checkPermission(Permission perm) {
        if (accessDeclaredMembers.equals(perm)) {
          throw new SecurityException("Gson: no-member-access");
        }
        if (suppressAccessChecks.equals(perm)) {
          throw new SecurityException("Gson: no-suppress-access-check");
        }
      }
    };
    System.setSecurityManager(restrictiveManager);

    try {
      Gson gson = new Gson();
      try {
        // Getting reflection based adapter should fail
        gson.getAdapter(clazz);
        fail();
      } catch (SecurityException e) {
        assertThat(e).hasMessageThat().isEqualTo("Gson: no-member-access");
      }

      final AtomicBoolean wasReadCalled = new AtomicBoolean(false);
      gson = new GsonBuilder()
        .registerTypeAdapter(clazz, new TypeAdapter<Object>() {
          @Override
          public void write(JsonWriter out, Object value) throws IOException {
            out.value("custom-write");
          }

          @Override
          public Object read(JsonReader in) throws IOException {
            in.skipValue();
            wasReadCalled.set(true);
            return null;
          }}
        )
        .create();

      assertThat(gson.toJson(null, clazz)).isEqualTo("\"custom-write\"");
      assertThat(gson.fromJson("{}", clazz)).isNull();
      assertThat(wasReadCalled.get()).isTrue();
    } finally {
      System.setSecurityManager(original);
    }
  }

  private static JsonIOException assertInaccessibleException(String json, Class<?> toDeserialize) {
    Gson gson = new Gson();
    try {
      gson.fromJson(json, toDeserialize);
      throw new AssertionError("Missing exception; test has to be run with `--illegal-access=deny`");
    } catch (JsonSyntaxException e) {
      throw new AssertionError("Unexpected exception; test has to be run with `--illegal-access=deny`", e);
    } catch (JsonIOException expected) {
      assertThat(expected).hasMessageThat().endsWith("\nSee https://github.com/google/gson/blob/master/Troubleshooting.md#reflection-inaccessible");
      // Return exception for further assertions
      return expected;
    }
  }

  /**
   * Test serializing an instance of a non-accessible internal class, but where
   * Gson supports serializing one of its superinterfaces.
   *
   * <p>Here {@link Collections#emptyList()} is used which returns an instance
   * of the internal class {@code java.util.Collections.EmptyList}. Gson should
   * serialize the object as {@code List} despite the internal class not being
   * accessible.
   *
   * <p>See https://github.com/google/gson/issues/1875
   */
  @Test
  public void testSerializeInternalImplementationObject() {
    Gson gson = new Gson();
    String json = gson.toJson(Collections.emptyList());
    assertThat(json).isEqualTo("[]");

    // But deserialization should fail
    Class<?> internalClass = Collections.emptyList().getClass();
    JsonIOException exception = assertInaccessibleException("[]", internalClass);
    // Don't check exact class name because it is a JDK implementation detail
    assertThat(exception).hasMessageThat().startsWith("Failed making constructor '");
    assertThat(exception).hasMessageThat().contains("' accessible; either increase its visibility or"
        + " write a custom InstanceCreator or TypeAdapter for its declaring type: ");
  }

  @Test
  public void testInaccessibleField() {
    JsonIOException exception = assertInaccessibleException("{}", Throwable.class);
    // Don't check exact field name because it is a JDK implementation detail
    assertThat(exception).hasMessageThat().startsWith("Failed making field 'java.lang.Throwable#");
    assertThat(exception).hasMessageThat().contains("' accessible; either increase its visibility or"
        + " write a custom TypeAdapter for its declaring type.");
  }
}
