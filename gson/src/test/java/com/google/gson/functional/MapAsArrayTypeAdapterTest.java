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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;

public class MapAsArrayTypeAdapterTest {

  @Test
  public void testSerializeComplexMapWithTypeAdapter() {
    Type type = new TypeToken<Map<Point, String>>() {}.getType();
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    Map<Point, String> original = new LinkedHashMap<>();
    original.put(new Point(5, 5), "a");
    original.put(new Point(8, 8), "b");
    String json = gson.toJson(original, type);
    assertThat(json).isEqualTo("[[{\"x\":5,\"y\":5},\"a\"],[{\"x\":8,\"y\":8},\"b\"]]");
    assertThat(gson.<Map<Point, String>>fromJson(json, type)).isEqualTo(original);

    // test that registering a type adapter for one map doesn't interfere with others
    Map<String, Boolean> otherMap = new LinkedHashMap<>();
    otherMap.put("t", true);
    otherMap.put("f", false);
    assertThat(gson.toJson(otherMap, Map.class)).isEqualTo("{\"t\":true,\"f\":false}");
    assertThat(gson.toJson(otherMap, new TypeToken<Map<String, Boolean>>() {}.getType()))
        .isEqualTo("{\"t\":true,\"f\":false}");
    assertThat(
            gson.<Object>fromJson(
                "{\"t\":true,\"f\":false}", new TypeToken<Map<String, Boolean>>() {}.getType()))
        .isEqualTo(otherMap);
  }

  @Test
  @Ignore("we no longer hash keys at serialization time")
  public void testTwoTypesCollapseToOneSerialize() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    Map<Number, String> original = new LinkedHashMap<>();
    original.put(1.0D, "a");
    original.put(1.0F, "b");
    Type type = new TypeToken<Map<Number, String>>() {}.getType();
    var e = assertThrows(JsonSyntaxException.class, () -> gson.toJson(original, type));
    assertThat(e).hasMessageThat().isEqualTo("TODO");
  }

  @Test
  public void testTwoTypesCollapseToOneDeserialize() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    String s = "[[\"1.00\",\"a\"],[\"1.0\",\"b\"]]";
    Type type = new TypeToken<Map<Double, String>>() {}.getType();
    var e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson(s, type));
    assertThat(e).hasMessageThat().isEqualTo("duplicate key: 1.0");
  }

  @Test
  public void testDuplicateKeyWithNullFirstValueArrayForm() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    String s = "[[\"a\",null],[\"a\",\"x\"]]";
    Type type = new TypeToken<Map<String, String>>() {}.getType();
    var e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson(s, type));
    assertThat(e).hasMessageThat().isEqualTo("duplicate key: a");
  }

  @Test
  public void testMultipleEnableComplexKeyRegistrationHasNoEffect() {
    Type type = new TypeToken<Map<Point, String>>() {}.getType();
    Gson gson =
        new GsonBuilder()
            .enableComplexMapKeySerialization()
            .enableComplexMapKeySerialization()
            .create();

    Map<Point, String> original = new LinkedHashMap<>();
    original.put(new Point(6, 5), "abc");
    original.put(new Point(1, 8), "def");
    String json = gson.toJson(original, type);
    assertThat(json).isEqualTo("[[{\"x\":6,\"y\":5},\"abc\"],[{\"x\":1,\"y\":8},\"def\"]]");
    assertThat(gson.<Map<Point, String>>fromJson(json, type)).isEqualTo(original);
  }

  @Test
  public void testMapWithTypeVariableSerialization() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    PointWithProperty<Point> map = new PointWithProperty<>();
    map.map.put(new Point(2, 3), new Point(4, 5));
    Type type = new TypeToken<PointWithProperty<Point>>() {}.getType();
    String json = gson.toJson(map, type);
    assertThat(json).isEqualTo("{\"map\":[[{\"x\":2,\"y\":3},{\"x\":4,\"y\":5}]]}");
  }

  @Test
  public void testMapWithTypeVariableDeserialization() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    String json = "{map:[[{x:2,y:3},{x:4,y:5}]]}";
    Type type = new TypeToken<PointWithProperty<Point>>() {}.getType();
    PointWithProperty<Point> map = gson.fromJson(json, type);
    Point key = map.map.keySet().iterator().next();
    Point value = map.map.values().iterator().next();
    assertThat(key).isEqualTo(new Point(2, 3));
    assertThat(value).isEqualTo(new Point(4, 5));
  }

  @Test
  public void testDeserializationWithNullKey() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson("[[null,123]]", typeOfMap);
    assertThat(map).hasSize(1);
    assertThat(map.get(null)).isEqualTo(123);
  }

  /** Tests deserializing as raw {@code Map.class} with {@code null} key. */
  @Test
  public void testDeserializeRawMapNullKey() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    String json = "[[\"a\", 1.0], [null, 2.0], [\"b\", 3.0]]";

    Map<Object, Double> expectedMap = new LinkedHashMap<>();
    expectedMap.put("a", 1.0);
    expectedMap.put(null, 2.0);
    expectedMap.put("b", 3.0);

    assertThat(gson.fromJson(json, Map.class)).isEqualTo(expectedMap);
  }

  /** Tests deserializing as raw {@code Map.class} with non-{@code Comparable} key. */
  @Test
  public void testDeserializeRawMapNonComparableKey() {
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    String json = "[[\"a\", 1.0], [{}, 2.0], [\"b\", 3.0]]";

    Map<Object, Double> expectedMap = new LinkedHashMap<>();
    expectedMap.put("a", 1.0);
    expectedMap.put(Collections.emptyMap(), 2.0);
    expectedMap.put("b", 3.0);

    @SuppressWarnings("unchecked")
    Map<Object, Double> map = gson.fromJson(json, Map.class);
    assertThat(map).isEqualTo(expectedMap);

    Iterator<Map.Entry<Object, Double>> mapIterator = map.entrySet().iterator();
    mapIterator.next(); // skip first entry
    Map.Entry<Object, Double> entry = mapIterator.next();
    Object key = entry.getKey();
    assertThat(key).isEqualTo(Collections.emptyMap());
    assertThat(key).isNotInstanceOf(Comparable.class);
    assertThat(entry.getValue()).isEqualTo(2.0);
  }

  /**
   * Deserialization of array should work, even if {@link
   * GsonBuilder#enableComplexMapKeySerialization()} is not used.
   */
  @Test
  public void testDeserializationWithoutExplicitlyEnabled() {
    Gson gson = new GsonBuilder().create();
    Type type = new TypeToken<Map<Integer, Integer>>() {}.getType();
    String json = "[[1, 11], [2, 22]]";
    Map<Integer, Integer> map = gson.fromJson(json, type);
    assertThat(map).containsExactly(1, 11, 2, 22).inOrder();

    // But for "map as array" all entries have to be encoded as `[key, value]` pairs
    var e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[[1, 11], 2, 22]", type));
    assertThat(e).hasCauseThat().isInstanceOf(IllegalStateException.class);
    assertThat(e)
        .hasCauseThat()
        .hasMessageThat()
        .startsWith("Expected BEGIN_ARRAY but was NUMBER at line 1 column 12 path $[1]");
  }

  static class Point {
    int x;
    int y;

    Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @SuppressWarnings("unused")
    Point() {}

    @Override
    public boolean equals(Object o) {
      return o instanceof Point && ((Point) o).x == x && ((Point) o).y == y;
    }

    @Override
    public int hashCode() {
      return x * 37 + y;
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  static class PointWithProperty<T> {
    Map<Point, T> map = new HashMap<>();
  }
}
