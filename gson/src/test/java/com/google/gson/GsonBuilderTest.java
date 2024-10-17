/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import org.junit.Test;

/**
 * Unit tests for {@link GsonBuilder}.
 *
 * @author Inderjeet Singh
 */
public class GsonBuilderTest {
  private static final TypeAdapter<Object> NULL_TYPE_ADAPTER =
      new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Object value) {
          throw new AssertionError();
        }

        @Override
        public Object read(JsonReader in) {
          throw new AssertionError();
        }
      };

  @Test
  public void testCreatingMoreThanOnce() {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    assertThat(gson).isNotNull();
    assertThat(builder.create()).isNotNull();

    builder.setFieldNamingStrategy(f -> "test");

    Gson otherGson = builder.create();
    assertThat(otherGson).isNotNull();
    // Should be different instances because builder has been modified in the meantime
    assertThat(gson).isNotSameInstanceAs(otherGson);
  }

  /**
   * Gson instances should not be affected by subsequent modification of GsonBuilder which created
   * them.
   */
  @Test
  public void testModificationAfterCreate() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();

    // Modifications of `gsonBuilder` should not affect `gson` object
    gsonBuilder.registerTypeAdapter(
        CustomClass1.class,
        new TypeAdapter<CustomClass1>() {
          @Override
          public CustomClass1 read(JsonReader in) {
            throw new UnsupportedOperationException();
          }

          @Override
          public void write(JsonWriter out, CustomClass1 value) throws IOException {
            out.value("custom-adapter");
          }
        });

    gsonBuilder.registerTypeHierarchyAdapter(
        CustomClass2.class,
        (JsonSerializer<CustomClass2>)
            (src, typeOfSrc, context) -> new JsonPrimitive("custom-hierarchy-adapter"));

    gsonBuilder.registerTypeAdapter(
        CustomClass3.class,
        (InstanceCreator<CustomClass3>) type -> new CustomClass3("custom-instance"));

    assertDefaultGson(gson);
    // New GsonBuilder created from `gson` should not have been affected by changes
    // to `gsonBuilder` either
    assertDefaultGson(gson.newBuilder().create());

    // New Gson instance from modified GsonBuilder should be affected by changes
    assertCustomGson(gsonBuilder.create());
  }

  private static void assertDefaultGson(Gson gson) {
    // Should use default reflective adapter
    String json1 = gson.toJson(new CustomClass1());
    assertThat(json1).isEqualTo("{}");

    // Should use default reflective adapter
    String json2 = gson.toJson(new CustomClass2());
    assertThat(json2).isEqualTo("{}");

    // Should use default instance creator
    CustomClass3 customClass3 = gson.fromJson("{}", CustomClass3.class);
    assertThat(customClass3.s).isEqualTo(CustomClass3.NO_ARG_CONSTRUCTOR_VALUE);
  }

  private static void assertCustomGson(Gson gson) {
    String json1 = gson.toJson(new CustomClass1());
    assertThat(json1).isEqualTo("\"custom-adapter\"");

    String json2 = gson.toJson(new CustomClass2());
    assertThat(json2).isEqualTo("\"custom-hierarchy-adapter\"");

    CustomClass3 customClass3 = gson.fromJson("{}", CustomClass3.class);
    assertThat(customClass3.s).isEqualTo("custom-instance");
  }

  static class CustomClass1 {}

  static class CustomClass2 {}

  static class CustomClass3 {
    static final String NO_ARG_CONSTRUCTOR_VALUE = "default instance";

    final String s;

    public CustomClass3(String s) {
      this.s = s;
    }

    public CustomClass3() {
      this(NO_ARG_CONSTRUCTOR_VALUE);
    }
  }

  @Test
  public void testExcludeFieldsWithModifiers() {
    Gson gson =
        new GsonBuilder().excludeFieldsWithModifiers(Modifier.VOLATILE, Modifier.PRIVATE).create();
    assertThat(gson.toJson(new HasModifiers())).isEqualTo("{\"d\":\"d\"}");
  }

  @SuppressWarnings("unused")
  static class HasModifiers {
    private String a = "a";
    volatile String b = "b";
    private volatile String c = "c";
    String d = "d";
  }

  @Test
  public void testTransientFieldExclusion() {
    Gson gson = new GsonBuilder().excludeFieldsWithModifiers().create();
    assertThat(gson.toJson(new HasTransients())).isEqualTo("{\"a\":\"a\"}");
  }

  static class HasTransients {
    transient String a = "a";
  }

  @Test
  public void testRegisterTypeAdapterForCoreType() {
    Type[] types = {
      byte.class, int.class, double.class, Short.class, Long.class, String.class,
    };
    for (Type type : types) {
      new GsonBuilder().registerTypeAdapter(type, NULL_TYPE_ADAPTER);
    }
  }

  @Test
  public void testDisableJdkUnsafe() {
    Gson gson = new GsonBuilder().disableJdkUnsafe().create();
    var e =
        assertThrows(
            JsonIOException.class, () -> gson.fromJson("{}", ClassWithoutNoArgsConstructor.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Unable to create instance of class"
                + " com.google.gson.GsonBuilderTest$ClassWithoutNoArgsConstructor; usage of JDK"
                + " Unsafe is disabled. Registering an InstanceCreator or a TypeAdapter for this"
                + " type, adding a no-args constructor, or enabling usage of JDK Unsafe may fix"
                + " this problem.");
  }

  private static class ClassWithoutNoArgsConstructor {
    @SuppressWarnings("unused")
    public ClassWithoutNoArgsConstructor(String s) {}
  }

  @Test
  public void testSetVersionInvalid() {
    GsonBuilder builder = new GsonBuilder();
    var e = assertThrows(IllegalArgumentException.class, () -> builder.setVersion(Double.NaN));
    assertThat(e).hasMessageThat().isEqualTo("Invalid version: NaN");

    e = assertThrows(IllegalArgumentException.class, () -> builder.setVersion(-0.1));
    assertThat(e).hasMessageThat().isEqualTo("Invalid version: -0.1");
  }

  @Test
  public void testDefaultStrictness() throws IOException {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    assertThat(gson.newJsonReader(new StringReader("{}")).getStrictness())
        .isEqualTo(Strictness.LEGACY_STRICT);
    assertThat(gson.newJsonWriter(new StringWriter()).getStrictness())
        .isEqualTo(Strictness.LEGACY_STRICT);
  }

  @SuppressWarnings({"deprecation", "InlineMeInliner"}) // for GsonBuilder.setLenient
  @Test
  public void testSetLenient() throws IOException {
    GsonBuilder builder = new GsonBuilder();
    builder.setLenient();
    Gson gson = builder.create();
    assertThat(gson.newJsonReader(new StringReader("{}")).getStrictness())
        .isEqualTo(Strictness.LENIENT);
    assertThat(gson.newJsonWriter(new StringWriter()).getStrictness())
        .isEqualTo(Strictness.LENIENT);
  }

  @Test
  public void testSetStrictness() throws IOException {
    Strictness strictness = Strictness.STRICT;
    GsonBuilder builder = new GsonBuilder();
    builder.setStrictness(strictness);
    Gson gson = builder.create();
    assertThat(gson.newJsonReader(new StringReader("{}")).getStrictness()).isEqualTo(strictness);
    assertThat(gson.newJsonWriter(new StringWriter()).getStrictness()).isEqualTo(strictness);
  }

  @Test
  public void testRegisterTypeAdapterForObjectAndJsonElements() {
    String errorMessage = "Cannot override built-in adapter for ";
    Type[] types = {
      Object.class, JsonElement.class, JsonArray.class,
    };
    GsonBuilder gsonBuilder = new GsonBuilder();
    for (Type type : types) {
      IllegalArgumentException e =
          assertThrows(
              IllegalArgumentException.class,
              () -> gsonBuilder.registerTypeAdapter(type, NULL_TYPE_ADAPTER));
      assertThat(e).hasMessageThat().isEqualTo(errorMessage + type);
    }
  }

  @Test
  public void testRegisterTypeHierarchyAdapterJsonElements() {
    String errorMessage = "Cannot override built-in adapter for ";
    Class<?>[] types = {
      JsonElement.class, JsonArray.class,
    };
    GsonBuilder gsonBuilder = new GsonBuilder();
    for (Class<?> type : types) {
      IllegalArgumentException e =
          assertThrows(
              IllegalArgumentException.class,
              () -> gsonBuilder.registerTypeHierarchyAdapter(type, NULL_TYPE_ADAPTER));

      assertThat(e).hasMessageThat().isEqualTo(errorMessage + type);
    }
    // But registering type hierarchy adapter for Object should be allowed
    gsonBuilder.registerTypeHierarchyAdapter(Object.class, NULL_TYPE_ADAPTER);
  }

  @Test
  public void testSetDateFormatWithInvalidPattern() {
    GsonBuilder builder = new GsonBuilder();
    String invalidPattern = "This is an invalid Pattern";
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> builder.setDateFormat(invalidPattern));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo("The date pattern '" + invalidPattern + "' is not valid");
  }

  @Test
  public void testSetDateFormatWithValidPattern() {
    GsonBuilder builder = new GsonBuilder();
    String validPattern = "yyyy-MM-dd";
    // Should not throw an exception
    builder.setDateFormat(validPattern);
  }

  @Test
  public void testSetDateFormatNullPattern() {
    GsonBuilder builder = new GsonBuilder();
    @SuppressWarnings("JavaUtilDate")
    Date date = new Date(0);
    String originalFormatted = builder.create().toJson(date);

    String customFormatted = builder.setDateFormat("yyyy-MM-dd").create().toJson(date);
    assertThat(customFormatted).isNotEqualTo(originalFormatted);

    // `null` should reset the format to the default
    String resetFormatted = builder.setDateFormat(null).create().toJson(date);
    assertThat(resetFormatted).isEqualTo(originalFormatted);
  }

  /**
   * Tests behavior for an empty date pattern; this behavior is not publicly documented at the
   * moment.
   */
  @Test
  public void testSetDateFormatEmptyPattern() {
    GsonBuilder builder = new GsonBuilder();
    @SuppressWarnings("JavaUtilDate")
    Date date = new Date(0);
    String originalFormatted = builder.create().toJson(date);

    String emptyFormatted = builder.setDateFormat("    ").create().toJson(date);
    // Empty pattern was ignored
    assertThat(emptyFormatted).isEqualTo(originalFormatted);
  }

  @SuppressWarnings("deprecation") // for GsonBuilder.setDateFormat(int)
  @Test
  public void testSetDateFormatValidStyle() {
    GsonBuilder builder = new GsonBuilder();
    int[] validStyles = {DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT};

    for (int style : validStyles) {
      // Should not throw an exception
      builder.setDateFormat(style);
      builder.setDateFormat(style, style);
    }
  }

  @SuppressWarnings("deprecation") // for GsonBuilder.setDateFormat(int)
  @Test
  public void testSetDateFormatInvalidStyle() {
    GsonBuilder builder = new GsonBuilder();

    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> builder.setDateFormat(-1));
    assertThat(e).hasMessageThat().isEqualTo("Invalid style: -1");

    e = assertThrows(IllegalArgumentException.class, () -> builder.setDateFormat(4));
    assertThat(e).hasMessageThat().isEqualTo("Invalid style: 4");

    e =
        assertThrows(
            IllegalArgumentException.class, () -> builder.setDateFormat(-1, DateFormat.FULL));
    assertThat(e).hasMessageThat().isEqualTo("Invalid style: -1");

    e =
        assertThrows(
            IllegalArgumentException.class, () -> builder.setDateFormat(DateFormat.FULL, -1));
    assertThat(e).hasMessageThat().isEqualTo("Invalid style: -1");
  }
}
