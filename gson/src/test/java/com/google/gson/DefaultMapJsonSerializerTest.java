/*
 * Copyright (C) 2008 Google Inc.
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

import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for the default JSON map serialization object located in the
 * {@link DefaultTypeAdapters} class.
 *
 * @author Joel Leitch
 */
public class DefaultMapJsonSerializerTest extends TestCase {

  @SuppressWarnings("rawtypes")
  private JsonSerializer<Map> mapSerializer;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mapSerializer = new DefaultTypeAdapters.MapTypeAdapter();
  }

  public void testEmptyMapNoTypeSerialization() {
    Map<String, String> emptyMap = new HashMap<String, String>();
    JsonElement element = mapSerializer.serialize(emptyMap, emptyMap.getClass(), null);
    assertTrue(element instanceof JsonObject);
    JsonObject emptyMapJsonObject = (JsonObject) element;
    assertTrue(emptyMapJsonObject.entrySet().isEmpty());
  }

  public void testEmptyMapSerialization() {
    Type mapType = new TypeToken<Map<String, String>>() { }.getType();
    Map<String, String> emptyMap = new HashMap<String, String>();
    JsonElement element = mapSerializer.serialize(emptyMap, mapType, null);

    assertTrue(element instanceof JsonObject);
    JsonObject emptyMapJsonObject = (JsonObject) element;
    assertTrue(emptyMapJsonObject.entrySet().isEmpty());
  }
}
