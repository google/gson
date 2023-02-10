/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public final class ObjectTypeAdapterTest {
  private final Gson gson = new GsonBuilder().create();
  private final TypeAdapter<Object> adapter = gson.getAdapter(Object.class);

  @Test
  public void testDeserialize() throws Exception {
    Map<?, ?> map = (Map<?, ?>) adapter.fromJson("{\"a\":5,\"b\":[1,2,null],\"c\":{\"x\":\"y\"}}");
    assertThat(map.get("a")).isEqualTo(5.0);
    assertThat(map.get("b")).isEqualTo(Arrays.asList(1.0, 2.0, null));
    assertThat(map.get("c")).isEqualTo(Collections.singletonMap("x", "y"));
    assertThat(map).hasSize(3);
  }

  @Test
  public void testSerialize() {
    Object object = new RuntimeType();
    assertThat(adapter.toJson(object).replace("\"", "'")).isEqualTo("{'a':5,'b':[1,2,null]}");
  }

  @Test
  public void testSerializeNullValue() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("a", null);
    assertThat(adapter.toJson(map).replace('"', '\'')).isEqualTo("{'a':null}");
  }

  @Test
  public void testDeserializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("a", null);
    assertThat(adapter.fromJson("{\"a\":null}")).isEqualTo(map);
  }

  @Test
  public void testSerializeObject() {
    assertThat(adapter.toJson(new Object())).isEqualTo("{}");
  }

  private static String repeat(String s, int times) {
    StringBuilder stringBuilder = new StringBuilder(s.length() * times);
    for (int i = 0; i < times; i++) {
      stringBuilder.append(s);
    }
    return stringBuilder.toString();
  }

  /** Deeply nested JSON arrays should not cause {@link StackOverflowError} */
  @SuppressWarnings("unchecked")
  @Test
  public void testDeserializeDeeplyNestedArrays() throws IOException {
    int times = 10000;
    // [[[ ... ]]]
    String json = repeat("[", times) + repeat("]", times);

    int actualTimes = 0;
    List<List<?>> current = (List<List<?>>) adapter.fromJson(json);
    while (true) {
      actualTimes++;
      if (current.isEmpty()) {
        break;
      }
      assertThat(current).hasSize(1);
      current = (List<List<?>>) current.get(0);
    }
    assertThat(actualTimes).isEqualTo(times);
  }

  /** Deeply nested JSON objects should not cause {@link StackOverflowError} */
  @SuppressWarnings("unchecked")
  @Test
  public void testDeserializeDeeplyNestedObjects() throws IOException {
    int times = 10000;
    // {"a":{"a": ... {"a":null} ... }}
    String json = repeat("{\"a\":", times) + "null" + repeat("}", times);

    int actualTimes = 0;
    Map<String, Map<?, ?>> current = (Map<String, Map<?, ?>>) adapter.fromJson(json);
    while (current != null) {
      assertThat(current).hasSize(1);
      actualTimes++;
      current = (Map<String, Map<?, ?>>) current.get("a");
    }
    assertThat(actualTimes).isEqualTo(times);
  }

  @SuppressWarnings("unused")
  private class RuntimeType {
    Object a = 5;
    Object b = Arrays.asList(1, 2, null);
  }
}
