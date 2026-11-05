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
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
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
    roundTrip(Optional.empty(), new TypeToken<Optional<String>>() {}, "{}");
  }

  @Test
  public void testEmptyOptionalSerializeNulls() {
    Gson gsonWithNulls = new GsonBuilder().serializeNulls().create();
    var typeToken = new TypeToken<Optional<String>>() {};
    assertThat(gsonWithNulls.toJson(Optional.empty(), typeToken.getType()))
        .isEqualTo("{\"value\":null}");
    assertThat(gsonWithNulls.fromJson("{\"value\":null}", typeToken)).isEmpty();
  }

  @Test
  public void testNullOptional() {
    var typeToken = new TypeToken<Optional<String>>() {};
    assertThat(gson.toJson(null, typeToken.getType())).isEqualTo("null");
    assertThat(gson.fromJson("null", typeToken)).isNull();
  }

  @Test
  public void testOptionalWithUnknownFields() {
    var typeToken = new TypeToken<Optional<String>>() {};
    String json = "{\"tiddly\":\"pom\",\"value\":\"hello\",\"wibble\":\"wobble\"}";
    assertThat(gson.fromJson(json, typeToken)).hasValue("hello");
  }

  @Test
  public void testRawOptional() {
    roundTrip(Optional.of("hello"), TypeToken.get(Optional.class), "{\"value\":\"hello\"}");
    roundTrip(Optional.empty(), TypeToken.get(Optional.class), "{}");
  }

  @Test
  public void testNestedOptional() {
    var typeToken = new TypeToken<Optional<Optional<String>>>() {};
    roundTrip(Optional.of(Optional.of("hello")), typeToken, "{\"value\":{\"value\":\"hello\"}}");
    roundTrip(Optional.of(Optional.empty()), typeToken, "{\"value\":{}}");
    roundTrip(Optional.empty(), typeToken, "{}");
  }

  @Test
  public void testOptionalPolymorphicValue() {
    var typeToken = new TypeToken<Optional<SuperClass>>() {};
    assertThat(gson.toJson(Optional.of(new SubClass("abc", "def")), typeToken.getType()))
        .isEqualTo("{\"value\":{\"sub\":\"def\",\"sup\":\"abc\"}}");
  }

  @Test
  public void testOptionalFieldSerialization_serializeNulls() {
    Gson gsonWithNulls = new GsonBuilder().serializeNulls().create();
    TestClass testClass = new TestClass(Optional.of("hello"), Optional.empty());
    String json = gsonWithNulls.toJson(testClass);
    assertThat(json).isEqualTo("{\"present\":{\"value\":\"hello\"},\"empty\":{\"value\":null}}");
    TestClass deserialized = gsonWithNulls.fromJson(json, TestClass.class);
    assertThat(deserialized.present).hasValue("hello");
    assertThat(deserialized.empty).isEmpty();
  }

  @Test
  public void testOptionalFieldSerialization_skipNulls() {
    TestClass testClass = new TestClass(Optional.of("hello"), Optional.empty());
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
    Gson lenientGson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    // Empty OptionalDouble internally has value NaN; reflective adapter would have written that,
    // verify that it can be deserialized again
    assertThat(lenientGson.fromJson("{\"isPresent\":false,\"value\":NaN}", OptionalDouble.class))
        .isEqualTo(OptionalDouble.empty());
  }

  @Test
  public void testCustomAdapterPrecedence() {
    TypeAdapter<OptionalInt> customAdapter =
        new TypeAdapter<OptionalInt>() {
          @Override
          public OptionalInt read(JsonReader in) throws IOException {
            int value = in.nextInt();
            return value == -1 ? OptionalInt.empty() : OptionalInt.of(value);
          }

          @Override
          public void write(JsonWriter out, OptionalInt value) throws IOException {
            out.value(value.orElse(-1));
          }
        }.nullSafe();
    Gson customGson =
        new GsonBuilder().registerTypeAdapter(OptionalInt.class, customAdapter).create();
    assertThat(customGson.toJson(OptionalInt.of(42))).isEqualTo("42");
    assertThat(customGson.toJson(OptionalInt.empty())).isEqualTo("-1");
    assertThat(customGson.fromJson("42", OptionalInt.class)).isEqualTo(OptionalInt.of(42));
    assertThat(customGson.fromJson("-1", OptionalInt.class)).isEqualTo(OptionalInt.empty());
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
    assertThat(gson.getAdapter(typeToken).toString()).contains("JdkOptionalTypeAdapters");
    assertThat(gson.toJson(value, typeToken.getType())).isEqualTo(expectedJson);
    assertThat(gson.fromJson(expectedJson, typeToken)).isEqualTo(value);
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
    // Safe for test serialization
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

  private static class SuperClass {
    // Read reflectively by Gson
    @SuppressWarnings("unused")
    final String sup;

    SuperClass(String sup) {
      this.sup = sup;
    }
  }

  private static class SubClass extends SuperClass {
    // Read reflectively by Gson
    @SuppressWarnings("unused")
    final String sub;

    SubClass(String sup, String sub) {
      super(sup);
      this.sub = sub;
    }
  }

  private static class TestClass {
    Optional<String> present;
    Optional<String> empty;

    TestClass() {}

    TestClass(Optional<String> present, Optional<String> empty) {
      this.present = present;
      this.empty = empty;
    }
  }
}
