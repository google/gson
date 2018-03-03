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

import java.math.BigInteger;
import java.util.*;
import junit.framework.TestCase;

public final class ObjectTypeAdapterTest extends TestCase {
  private final Gson gson = new GsonBuilder().create();
  private final TypeAdapter<Object> adapter = gson.getAdapter(Object.class);

  public void testDeserialize() throws Exception {
    Map<?, ?> map = (Map<?, ?>) adapter.fromJson("{\"a\":5,\"b\":[1,2,null],\"c\":{\"x\":\"y\"}}");
    assertEquals(5, map.get("a"));
    assertEquals(Arrays.asList(1, 2, null), map.get("b"));
    assertEquals(Collections.singletonMap("x", "y"), map.get("c"));
    assertEquals(3, map.size());
  }

  public void testSerialize() throws Exception {
    Object object = new RuntimeType();
    assertEquals("{'a':5,'b':[1,2,null]}", adapter.toJson(object).replace("\"", "'"));
  }
  
  public void testSerializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", null);
    assertEquals("{'a':null}", adapter.toJson(map).replace('"', '\''));
  }

  public void testDeserializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", null);
    assertEquals(map, adapter.fromJson("{\"a\":null}"));
  }

  public void testSerializeObject() throws Exception {
    assertEquals("{}", adapter.toJson(new Object()));
  }

  public void testDeserializeNumbers() {
    String json = "[2147483647,9223372036854775807,9223372036854775808,-9223372036854775809,1E6,1.0]";
    Object[] numbers = gson.fromJson(json, Object[].class);
    assertEquals(Integer.MAX_VALUE, numbers[0]);                       //int
    assertEquals(Long.MAX_VALUE, numbers[1]);                          //long
    assertEquals(new BigInteger("9223372036854775808"), numbers[2]);  //BigInteger
    assertEquals(new BigInteger("-9223372036854775809"), numbers[3]); //BigInteger
    assertEquals(1.0E6, numbers[4]);                                    //double
    assertEquals(1.0, numbers[5]);                                      //double
  }

  @SuppressWarnings("unused")
  private class RuntimeType {
    Object a = 5;
    Object b = Arrays.asList(1, 2, null);
  }
}
