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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

public final class ObjectTypeAdapterTest extends TestCase {
  private final Gson gson = new GsonBuilder().create();
  private final TypeAdapter<Object> adapter = gson.getAdapter(Object.class);

  public void testDeserialize() throws Exception {
    Map<?, ?> map = (Map<?, ?>) adapter.fromJson("{\"a\":5,\"b\":[1,2,null],\"c\":{\"x\":\"y\"}}");
    assertEquals(5.0, map.get("a"));
    assertEquals(Arrays.asList(1.0, 2.0, null), map.get("b"));
    assertEquals(Collections.singletonMap("x", "y"), map.get("c"));
    assertEquals(3, map.size());
  }

  public void testSerialize() throws Exception {
    Object object = new RuntimeType();
    assertEquals("{'a':5,'b':[1,2,null]}", adapter.toJson(object).replace("\"", "'"));
  }

  public void testSerializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("a", null);
    assertEquals("{'a':null}", adapter.toJson(map).replace('"', '\''));
  }

  public void testDeserializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("a", null);
    assertEquals(map, adapter.fromJson("{\"a\":null}"));
  }

  public void testSerializeObject() throws Exception {
    assertEquals("{}", adapter.toJson(new Object()));
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
      assertEquals(1, current.size());
      current = (List<List<?>>) current.get(0);
    }
    assertEquals(times, actualTimes);
  }

  /** Deeply nested JSON objects should not cause {@link StackOverflowError} */
  @SuppressWarnings("unchecked")
  public void testDeserializeDeeplyNestedObjects() throws IOException {
    int times = 10000;
    // {"a":{"a": ... {"a":null} ... }}
    String json = repeat("{\"a\":", times) + "null" + repeat("}", times);

    int actualTimes = 0;
    Map<String, Map<?, ?>> current = (Map<String, Map<?, ?>>) adapter.fromJson(json);
    while (current != null) {
      assertEquals(1, current.size());
      actualTimes++;
      current = (Map<String, Map<?, ?>>) current.get("a");
    }
    assertEquals(times, actualTimes);
  }

  @SuppressWarnings("unused")
  private class RuntimeType {
    Object a = 5;
    Object b = Arrays.asList(1, 2, null);
  }
}
