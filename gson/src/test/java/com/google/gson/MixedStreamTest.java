/*
 * Copyright (C) 2010 Google Inc.
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

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public final class MixedStreamTest {

  private static final Car BLUE_MUSTANG = new Car("mustang", 0x0000FF);
  private static final Car BLACK_BMW = new Car("bmw", 0x000000);
  private static final Car RED_MIATA = new Car("miata", 0xFF0000);
  private static final String CARS_JSON =
      "[\n"
          + "  {\n"
          + "    \"name\": \"mustang\",\n"
          + "    \"color\": 255\n"
          + "  },\n"
          + "  {\n"
          + "    \"name\": \"bmw\",\n"
          + "    \"color\": 0\n"
          + "  },\n"
          + "  {\n"
          + "    \"name\": \"miata\",\n"
          + "    \"color\": 16711680\n"
          + "  }\n"
          + "]";

  @Test
  public void testWriteMixedStreamed() throws IOException {
    Gson gson = new Gson();
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");
    gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
    gson.toJson(BLACK_BMW, Car.class, jsonWriter);
    gson.toJson(RED_MIATA, Car.class, jsonWriter);
    jsonWriter.endArray();

    assertThat(stringWriter.toString()).isEqualTo(CARS_JSON);
  }

  @Test
  public void testReadMixedStreamed() throws IOException {
    Gson gson = new Gson();
    StringReader stringReader = new StringReader(CARS_JSON);
    JsonReader jsonReader = new JsonReader(stringReader);

    jsonReader.beginArray();
    assertThat(gson.<Car>fromJson(jsonReader, Car.class)).isEqualTo(BLUE_MUSTANG);
    assertThat(gson.<Car>fromJson(jsonReader, Car.class)).isEqualTo(BLACK_BMW);
    assertThat(gson.<Car>fromJson(jsonReader, Car.class)).isEqualTo(RED_MIATA);
    jsonReader.endArray();
  }

  @SuppressWarnings("deprecation") // for JsonReader.setLenient
  @Test
  public void testReadDoesNotMutateState() throws IOException {
    Gson gson = new Gson();
    JsonReader jsonReader = new JsonReader(new StringReader(CARS_JSON));
    jsonReader.beginArray();

    jsonReader.setLenient(false);
    Car deserialized = gson.fromJson(jsonReader, Car.class);
    assertThat(deserialized).isNotNull();
    assertThat(jsonReader.isLenient()).isFalse();

    jsonReader.setLenient(true);
    deserialized = gson.fromJson(jsonReader, Car.class);
    assertThat(deserialized).isNotNull();
    assertThat(jsonReader.isLenient()).isTrue();
  }

  @SuppressWarnings("deprecation") // for JsonWriter.setLenient
  @Test
  public void testWriteDoesNotMutateState() throws IOException {
    Gson gson = new Gson();
    JsonWriter jsonWriter = new JsonWriter(new StringWriter());
    jsonWriter.beginArray();

    jsonWriter.setHtmlSafe(true);
    jsonWriter.setLenient(true);
    gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
    assertThat(jsonWriter.isHtmlSafe()).isTrue();
    assertThat(jsonWriter.isLenient()).isTrue();

    jsonWriter.setHtmlSafe(false);
    jsonWriter.setLenient(false);
    gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
    assertThat(jsonWriter.isHtmlSafe()).isFalse();
    assertThat(jsonWriter.isLenient()).isFalse();
  }

  @Test
  public void testReadInvalidState() throws IOException {
    Gson gson = new Gson();
    JsonReader jsonReader = new JsonReader(new StringReader(CARS_JSON));
    jsonReader.beginArray();
    jsonReader.beginObject();
    assertThrows(JsonParseException.class, () -> gson.fromJson(jsonReader, String.class));
  }

  @Test
  public void testReadClosed() throws IOException {
    Gson gson = new Gson();
    JsonReader jsonReader = new JsonReader(new StringReader(CARS_JSON));
    jsonReader.close();
    var e =
        assertThrows(
            JsonParseException.class,
            () -> gson.fromJson(jsonReader, new TypeToken<List<Car>>() {}.getType()));
    assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("JsonReader is closed");
  }

  @Test
  public void testWriteInvalidState() throws IOException {
    Gson gson = new Gson();
    JsonWriter jsonWriter = new JsonWriter(new StringWriter());
    jsonWriter.beginObject();
    var e =
        assertThrows(
            IllegalStateException.class, () -> gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter));
    assertThat(e).hasMessageThat().isEqualTo("Nesting problem.");
  }

  @Test
  public void testWriteClosed() throws IOException {
    Gson gson = new Gson();
    JsonWriter jsonWriter = new JsonWriter(new StringWriter());
    jsonWriter.beginArray();
    jsonWriter.endArray();
    jsonWriter.close();
    var e =
        assertThrows(
            IllegalStateException.class, () -> gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter));
    assertThat(e).hasMessageThat().isEqualTo("JsonWriter is closed.");
  }

  @Test
  public void testWriteNulls() {
    Gson gson = new Gson();
    assertThrows(
        NullPointerException.class,
        () -> gson.toJson(new JsonPrimitive("hello"), (JsonWriter) null));

    StringWriter stringWriter = new StringWriter();
    gson.toJson(null, new JsonWriter(stringWriter));
    assertThat(stringWriter.toString()).isEqualTo("null");
  }

  @Test
  public void testReadNulls() {
    Gson gson = new Gson();
    assertThrows(NullPointerException.class, () -> gson.fromJson((JsonReader) null, Integer.class));
    assertThrows(
        NullPointerException.class,
        () -> gson.fromJson(new JsonReader(new StringReader("true")), (Type) null));
  }

  @Test
  public void testWriteHtmlSafeWithEscaping() {
    List<String> contents = Arrays.asList("<", ">", "&", "=", "'");
    Type type = new TypeToken<List<String>>() {}.getType();

    StringWriter writer = new StringWriter();
    new Gson().toJson(contents, type, new JsonWriter(writer));
    assertThat(writer.toString())
        .isEqualTo("[\"\\u003c\",\"\\u003e\",\"\\u0026\",\"\\u003d\",\"\\u0027\"]");
  }

  @Test
  public void testWriteHtmlSafeWithoutEscaping() {
    List<String> contents = Arrays.asList("<", ">", "&", "=", "'");
    Type type = new TypeToken<List<String>>() {}.getType();

    StringWriter writer = new StringWriter();
    new GsonBuilder().disableHtmlEscaping().create().toJson(contents, type, new JsonWriter(writer));
    assertThat(writer.toString()).isEqualTo("[\"<\",\">\",\"&\",\"=\",\"'\"]");
  }

  @Test
  public void testWriteLenient() {
    List<Double> doubles =
        Arrays.asList(
            Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -0.0d, 0.5d, 0.0d);
    Type type = new TypeToken<List<Double>>() {}.getType();

    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    new GsonBuilder()
        .serializeSpecialFloatingPointValues()
        .create()
        .toJson(doubles, type, jsonWriter);
    assertThat(writer.toString()).isEqualTo("[NaN,-Infinity,Infinity,-0.0,0.5,0.0]");

    var e =
        assertThrows(
            IllegalArgumentException.class,
            () -> new Gson().toJson(doubles, type, new JsonWriter(new StringWriter())));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "NaN is not a valid double value as per JSON specification. To override this behavior,"
                + " use GsonBuilder.serializeSpecialFloatingPointValues() method.");
  }

  static final class Car {
    String name;
    int color;

    Car(String name, int color) {
      this.name = name;
      this.color = color;
    }

    // used by Gson
    Car() {}

    @Override
    public int hashCode() {
      return name.hashCode() ^ color;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Car && ((Car) o).name.equals(name) && ((Car) o).color == color;
    }
  }
}
