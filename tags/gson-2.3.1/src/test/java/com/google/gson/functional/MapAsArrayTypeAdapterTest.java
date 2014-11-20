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
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import junit.framework.TestCase;

public class MapAsArrayTypeAdapterTest extends TestCase {

  public void testSerializeComplexMapWithTypeAdapter() {
    Type type = new TypeToken<Map<Point, String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .create();

    Map<Point, String> original = new LinkedHashMap<Point, String>();
    original.put(new Point(5, 5), "a");
    original.put(new Point(8, 8), "b");
    String json = gson.toJson(original, type);
    assertEquals("[[{\"x\":5,\"y\":5},\"a\"],[{\"x\":8,\"y\":8},\"b\"]]", json);
    assertEquals(original, gson.<Map<Point, String>>fromJson(json, type));

    // test that registering a type adapter for one map doesn't interfere with others
    Map<String, Boolean> otherMap = new LinkedHashMap<String, Boolean>();
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

    Map<Number, String> original = new LinkedHashMap<Number, String>();
    original.put(new Double(1.0), "a");
    original.put(new Float(1.0), "b");
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

    Map<Point, String> original = new LinkedHashMap<Point, String>();
    original.put(new Point(6, 5), "abc");
    original.put(new Point(1, 8), "def");
    String json = gson.toJson(original, type);
    assertEquals("[[{\"x\":6,\"y\":5},\"abc\"],[{\"x\":1,\"y\":8},\"def\"]]", json);
    assertEquals(original, gson.<Map<Point, String>>fromJson(json, type));
  }

  public void testMapWithTypeVariableSerialization() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    PointWithProperty<Point> map = new PointWithProperty<Point>();
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
    Map<Point, T> map = new HashMap<Point, T>();
  }
}
