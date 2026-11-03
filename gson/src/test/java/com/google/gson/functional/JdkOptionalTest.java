/*
 * Copyright (C) 2026 Google Inc.
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.junit.Test;

public final class JdkOptionalTest {

  private final Gson gson = new Gson();

  @Test
  public void testPresentOptional() {
    roundTrip(Optional.of("hello"), new TypeToken<Optional<String>>() {}, "{\"value\":\"hello\"}");
  }

  @Test
  public void testEmptyOptional() {
    roundTrip(Optional.<String>empty(), new TypeToken<Optional<String>>() {}, "{}");
  }

  @Test
  public void testEmptyOptionalSerializeNulls() {
    Gson gsonWithNulls = new GsonBuilder().serializeNulls().create();
    Type type = new TypeToken<Optional<String>>() {}.getType();
    assertThat(gsonWithNulls.toJson(Optional.empty(), type)).isEqualTo("{\"value\":null}");
    assertThat(gsonWithNulls.<Optional<String>>fromJson("{\"value\":null}", type)).isEmpty();
  }

  @Test
  public void testNullOptional() {
    Type type = new TypeToken<Optional<String>>() {}.getType();
    assertThat(gson.toJson(null, type)).isEqualTo("null");
    assertThat(gson.<Optional<String>>fromJson("null", type)).isNull();
  }

  @Test
  public void testOptionalWithUnknownFields() {
    Type type = new TypeToken<Optional<String>>() {}.getType();
    String json = "{\"tiddly\":\"pom\",\"value\":\"hello\",\"wibble\":\"wobble\"}";
    assertThat(gson.<Optional<String>>fromJson(json, type)).hasValue("hello");
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void testRawOptional() {
    roundTrip(Optional.of("hello"), TypeToken.get(Optional.class), "{\"value\":\"hello\"}");
    roundTrip(Optional.empty(), TypeToken.get(Optional.class), "{}");
  }

  @Test
  public void testNestedOptional() {
    TypeToken<Optional<Optional<String>>> typeToken =
        new TypeToken<Optional<Optional<String>>>() {};
    roundTrip(Optional.of(Optional.of("hello")), typeToken, "{\"value\":{\"value\":\"hello\"}}");
    roundTrip(Optional.of(Optional.<String>empty()), typeToken, "{\"value\":{}}");
    roundTrip(Optional.<Optional<String>>empty(), typeToken, "{}");
  }

  @Test
  public void testOptionalPolymorphicValue() {
    Type type = new TypeToken<Optional<Object>>() {}.getType();
    assertThat(gson.toJson(Optional.<Object>of(new Holder("abc")), type))
        .isEqualTo("{\"value\":{\"s\":\"abc\"}}");
  }

  @Test
  public void testOptionalFieldSerialization_serializeNulls() {
    Gson gsonWithNulls = new GsonBuilder().serializeNulls().create();
    TestClass testClass = new TestClass(Optional.of("hello"), Optional.<String>empty());
    String json = gsonWithNulls.toJson(testClass);
    assertThat(json).isEqualTo("{\"present\":{\"value\":\"hello\"},\"empty\":{\"value\":null}}");
    TestClass deserialized = gsonWithNulls.fromJson(json, TestClass.class);
    assertThat(deserialized.present).hasValue("hello");
    assertThat(deserialized.empty).isEmpty();
  }

  @Test
  public void testOptionalFieldSerialization_skipNulls() {
    TestClass testClass = new TestClass(Optional.of("hello"), Optional.<String>empty());
    String json = gson.toJson(testClass);
    assertThat(json).isEqualTo("{\"present\":{\"value\":\"hello\"},\"empty\":{}}");
    TestClass deserialized = gson.fromJson(json, TestClass.class);
    assertThat(deserialized.present).hasValue("hello");
    assertThat(deserialized.empty).isEmpty();
  }

  @Test
  public void testOptionalInt() {
    roundTrip(
        OptionalInt.of(123),
        TypeToken.get(OptionalInt.class),
        "{\"isPresent\":true,\"value\":123}");
    roundTrip(
        OptionalInt.empty(), TypeToken.get(OptionalInt.class), "{\"isPresent\":false,\"value\":0}");
    assertThat(gson.toJson(null, OptionalInt.class)).isEqualTo("null");
    assertThat(gson.fromJson("null", OptionalInt.class)).isNull();
    assertThat(
            gson.fromJson(
                "{\"extra\":1,\"isPresent\":true,\"value\":123,\"more\":2}", OptionalInt.class))
        .isEqualTo(OptionalInt.of(123));
  }

  @Test
  public void testOptionalLong() {
    roundTrip(
        OptionalLong.of(1234567890123L),
        TypeToken.get(OptionalLong.class),
        "{\"isPresent\":true,\"value\":1234567890123}");
    roundTrip(
        OptionalLong.empty(),
        TypeToken.get(OptionalLong.class),
        "{\"isPresent\":false,\"value\":0}");
    assertThat(gson.toJson(null, OptionalLong.class)).isEqualTo("null");
    assertThat(gson.fromJson("null", OptionalLong.class)).isNull();
    assertThat(
            gson.fromJson(
                "{\"extra\":1,\"isPresent\":true,\"value\":1234567890123,\"more\":2}",
                OptionalLong.class))
        .isEqualTo(OptionalLong.of(1234567890123L));
  }

  @Test
  public void testOptionalDouble() {
    roundTrip(
        OptionalDouble.of(12.5),
        TypeToken.get(OptionalDouble.class),
        "{\"isPresent\":true,\"value\":12.5}");
    roundTrip(
        OptionalDouble.empty(),
        TypeToken.get(OptionalDouble.class),
        "{\"isPresent\":false,\"value\":0.0}");
    assertThat(gson.toJson(null, OptionalDouble.class)).isEqualTo("null");
    assertThat(gson.fromJson("null", OptionalDouble.class)).isNull();
    assertThat(
            gson.fromJson(
                "{\"extra\":1,\"isPresent\":true,\"value\":12.5,\"more\":2}", OptionalDouble.class))
        .isEqualTo(OptionalDouble.of(12.5));
  }

  private static final boolean OPTIONAL_FIELDS_ARE_ACCESSIBLE;

  static {
    boolean accessible = false;
    try {
      Optional.class.getDeclaredField("value").setAccessible(true);
      accessible = true;
    } catch (InaccessibleObjectException e) {
      // OK: we can't reflect on java.util.Optional fields
    } catch (NoSuchFieldException e) {
      throw new AssertionError(e);
    }
    OPTIONAL_FIELDS_ARE_ACCESSIBLE = accessible;
  }

  private <T> void roundTrip(T value, TypeToken<T> typeToken, String expectedJson) {
    assertThat(gson.getAdapter(typeToken).getClass().getName()).doesNotContain("Reflective");
    assertThat(gson.toJson(value, typeToken.getType())).isEqualTo(expectedJson);
    assertThat(gson.<T>fromJson(expectedJson, typeToken.getType())).isEqualTo(value);
    if (OPTIONAL_FIELDS_ARE_ACCESSIBLE) {
      checkReflectiveTypeAdapterFactory(value, typeToken, expectedJson);
    }
  }

  // Assuming we have reflective access to the fields of java.util.Optional* classes, check that
  // ReflectiveTypeAdapterFactory would produce the same JSON. This ensures that we are preserving
  // a compatible JSON format for those classes even though we no longer use reflection.
  private void checkReflectiveTypeAdapterFactory(
      Object value, TypeToken<?> typeToken, String expectedJson) {
    if (value.equals(OptionalDouble.empty())) {
      // OptionalDouble.EMPTY has value = Double.NaN in the JDK, which causes
      // ReflectiveTypeAdapterFactory to throw IllegalArgumentException by default.
      return;
    }
    List<?> factories;
    try {
      Field factoriesField = gson.getClass().getDeclaredField("factories");
      factoriesField.setAccessible(true);
      factories = (List<?>) factoriesField.get(gson);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
    ReflectiveTypeAdapterFactory adapterFactory =
        factories.stream()
            .filter(f -> f instanceof ReflectiveTypeAdapterFactory)
            .map(f -> (ReflectiveTypeAdapterFactory) f)
            .findFirst()
            .get();
    @SuppressWarnings("unchecked")
    TypeAdapter<Object> adapter = (TypeAdapter<Object>) adapterFactory.create(gson, typeToken);
    StringWriter stringWriter = new StringWriter();
    try {
      adapter.write(gson.newJsonWriter(stringWriter), value);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    assertThat(stringWriter.toString()).isEqualTo(expectedJson);
  }

  private static class Holder {
    @SuppressWarnings("unused")
    final String s;

    Holder(String s) {
      this.s = s;
    }
  }

  private static class TestClass {
    Optional<String> present;
    Optional<String> empty = Optional.empty();

    TestClass() {}

    TestClass(Optional<String> present, Optional<String> empty) {
      this.present = present;
      this.empty = empty;
    }
  }
}
