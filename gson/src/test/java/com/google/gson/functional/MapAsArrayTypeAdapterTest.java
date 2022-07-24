/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import junit.framework.TestCase;

public class MapAsArrayTypeAdapterTest extends TestCase {

  public void testSerializeComplexMapWithTypeAdapter() {
    Type type = new TypeToken<Map<Point, String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();

    Map<Point, String> original = new LinkedHashMap<>();
    original.put(new Point(5, 5), "a");
    original.put(new Point(8, 8), "b");
    String json = gson.toJson(original, type);
    assertEquals("[[{\"x\":5,\"y\":5},\"a\"],[{\"x\":8,\"y\":8},\"b\"]]", json);
    assertEquals(original, gson.<Map<Point, String>>fromJson(json, type));

    // test that registering a type adapter for one map doesn't interfere with others
    Map<String, Boolean> otherMap = new LinkedHashMap<>();
    otherMap.put("t", true);
    otherMap.put("f", false);
    assertEquals("{\"t\":true,\"f\":false}",
        gson.toJson(otherMap, Map.class));
    assertEquals("{\"t\":true,\"f\":false}",
        gson.toJson(otherMap, new TypeToken<Map<String, Boolean>>() {}.getType()));
    assertEquals(otherMap, gson.<Object>fromJson("{\"t\":true,\"f\":false}",
        new TypeToken<Map<String, Boolean>>() {}.getType()));
  }

  public void disabled_testTwoTypesCollapseToOneSerialize() {
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();

    Map<Number, String> original = new LinkedHashMap<>();
    original.put(1.0D, "a");
    original.put(1.0F, "b");
    try {
      gson.toJson(original, new TypeToken<Map<Number, String>>() {}.getType());
      fail(); // we no longer hash keys at serialization time
    } catch (JsonSyntaxException expected) {
    }
  }

  public void testTwoTypesCollapseToOneDeserialize() {
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();

    String s = "[[\"1.00\",\"a\"],[\"1.0\",\"b\"]]";
    try {
      gson.fromJson(s, new TypeToken<Map<Double, String>>() {}.getType());
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  public void testMultipleEnableComplexKeyRegistrationHasNoEffect() throws Exception {
    Type type = new TypeToken<Map<Point, String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .enableComplexMapKeySerialization()
        .create();

    Map<Point, String> original = new LinkedHashMap<>();
    original.put(new Point(6, 5), "abc");
    original.put(new Point(1, 8), "def");
    String json = gson.toJson(original, type);
    assertEquals("[[{\"x\":6,\"y\":5},\"abc\"],[{\"x\":1,\"y\":8},\"def\"]]", json);
    assertEquals(original, gson.<Map<Point, String>>fromJson(json, type));
  }

  public void testMapWithTypeVariableSerialization() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    PointWithProperty<Point> map = new PointWithProperty<>();
    map.map.put(new Point(2, 3), new Point(4, 5));
    Type type = new TypeToken<PointWithProperty<Point>>(){}.getType();
    String json = gson.toJson(map, type);
    assertEquals("{\"map\":[[{\"x\":2,\"y\":3},{\"x\":4,\"y\":5}]]}", json);
  }

  public void testMapWithTypeVariableDeserialization() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    String json = "{map:[[{x:2,y:3},{x:4,y:5}]]}";
    Type type = new TypeToken<PointWithProperty<Point>>(){}.getType();
    PointWithProperty<Point> map = gson.fromJson(json, type);
    Point key = map.map.keySet().iterator().next();
    Point value = map.map.values().iterator().next();
    assertEquals(new Point(2, 3), key);
    assertEquals(new Point(4, 5), value);
  }

  static class Point {
    int x;
    int y;
    Point(int x, int y) {
      this.x = x;
      this.y = y;
    }
    Point() {}
    @Override public boolean equals(Object o) {
      return o instanceof Point && ((Point) o).x == x && ((Point) o).y == y;
    }
    @Override public int hashCode() {
      return x * 37 + y;
    }
    @Override public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  static class PointWithProperty<T> {
    Map<Point, T> map = new LinkedHashMap<>();
  }

  /**
   * Complex map key serialization should use same {@link JsonWriter} settings as
   * originally provided writer.
   */
  public void testCustomJsonWriter() throws IOException {
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .serializeSpecialFloatingPointValues()
        .create();

    // Use TypeAdapter to avoid default lenientness of Gson
    TypeAdapter<Map<DoubleContainer, Integer>> adapter = gson.getAdapter(new TypeToken<Map<DoubleContainer, Integer>>() {});

    {
      StringWriter writer = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setSerializeNulls(true);

      adapter.write(jsonWriter, Collections.singletonMap(new DoubleContainer(null), 1));
      assertEquals("[[{\"d\":null},1]]", writer.toString());
    }


    Map<DoubleContainer, Integer> map = new LinkedHashMap<>();
    map.put(new DoubleContainer(null), 1);
    map.put(new DoubleContainer(Double.NaN), 2);

    {
      JsonWriter jsonWriter = new JsonWriter(new StringWriter());
      jsonWriter.setLenient(false);

      try {
        adapter.write(jsonWriter, map);
        fail();
      } catch (IllegalArgumentException e) {
        assertEquals("JSON forbids NaN and infinities: NaN", e.getMessage());
      }
    }

    {
      StringWriter writer = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setLenient(true);
      jsonWriter.setSerializeNulls(false);

      adapter.write(jsonWriter, map);
      assertEquals("[[{},1],[{\"d\":NaN},2]]", writer.toString());
    }
  }

  /**
   * Tests serialization behavior when custom adapter temporarily modifies {@link JsonWriter}.
   */
  public void testSerializeAdapterOverwriting() throws IOException {
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .registerTypeAdapter(DoubleContainer.class, new TypeAdapter<DoubleContainer>() {
          @Override public void write(JsonWriter out, DoubleContainer value) throws IOException {
            boolean oldLenient = out.isLenient();
            boolean oldSerializeNulls = out.getSerializeNulls();
            try {
              out.setLenient(true);
              out.setSerializeNulls(true);

              out.beginObject();
              out.name("c1");
              out.value(Double.NaN);
              out.name("c2");
              out.nullValue();
              out.endObject();
            } finally {
              out.setLenient(oldLenient);
              out.setSerializeNulls(oldSerializeNulls);
            }
          }

          @Override public DoubleContainer read(JsonReader in) throws IOException {
            throw new AssertionError("not used by this test");
          }
        })
        .create();

    Map<DoubleContainer, Integer> map = new LinkedHashMap<>();
    map.put(new DoubleContainer(null), 1);

    // Use TypeAdapter to avoid default lenientness of Gson
    TypeAdapter<Map<DoubleContainer, Integer>> adapter = gson.getAdapter(new TypeToken<Map<DoubleContainer, Integer>>() {});

    String expectedJson = "[[{\"c1\":NaN,\"c2\":null},1]]";

    // First create a permissive writer
    {
      StringWriter writer = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setSerializeNulls(true);
      jsonWriter.setLenient(true);

      adapter.write(jsonWriter, map);
      assertEquals(expectedJson, writer.toString());

      // Should still have original settings values
      assertEquals(true, jsonWriter.getSerializeNulls());
      assertEquals(true, jsonWriter.isLenient());
    }

    // Then try non-permissive writer; should have same result because custom
    // adapter temporarily changed writer settings
    {
      StringWriter writer = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setSerializeNulls(false);
      jsonWriter.setLenient(false);

      adapter.write(jsonWriter, map);
      assertEquals(expectedJson, writer.toString());

      // Should still have original settings values
      assertEquals(false, jsonWriter.getSerializeNulls());
      assertEquals(false, jsonWriter.isLenient());
    }
  }

  static class DoubleContainer {
    Double d = Double.NaN;

    DoubleContainer(Double d) {
      this.d = d;
    }
  }
}
