/*
 * Copyright (C) 2009 Google Inc.
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

import junit.framework.TestCase;

/**
 * Unit test for the {@link LongSerializationPolicy} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class LongSerializationPolicyTest extends TestCase {

  public void testDefaultLongSerialization() throws Exception {
    JsonElement element = LongSerializationPolicy.DEFAULT.serialize(1556L);
    assertTrue(element.isJsonPrimitive());
    
    JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
    assertFalse(jsonPrimitive.isString());
    assertTrue(jsonPrimitive.isNumber());
    assertEquals(1556L, element.getAsLong());
  }
  
  public void testDefaultLongSerializationIntegration() {
    Gson gson = new GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.DEFAULT)
        .create();
    assertEquals("[1]", gson.toJson(new long[] { 1L }, long[].class));
    assertEquals("[1]", gson.toJson(new Long[] { 1L }, Long[].class));
  }

  public void testStringLongSerialization() throws Exception {
    JsonElement element = LongSerializationPolicy.STRING.serialize(1556L);
    assertTrue(element.isJsonPrimitive());

    JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
    assertFalse(jsonPrimitive.isNumber());
    assertTrue(jsonPrimitive.isString());
    assertEquals("1556", element.getAsString());
  }

  public void testStringLongSerializationIntegration() {
    Gson gson = new GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create();
    assertEquals("[\"1\"]", gson.toJson(new long[] { 1L }, long[].class));
    assertEquals("[\"1\"]", gson.toJson(new Long[] { 1L }, Long[].class));
  }
}
