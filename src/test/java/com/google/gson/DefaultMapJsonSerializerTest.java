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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 * Unit test for the default JSON map serialization object located in the
 * {@link DefaultTypeAdapters} class.
 *
 * @author Joel Leitch
 */
public class DefaultMapJsonSerializerTest extends TestCase {
  private Gson gson = new Gson();

  public void testEmptyMapNoTypeSerialization() {
    Map<String, String> emptyMap = new HashMap<String, String>();
    JsonElement element = gson.toJsonTree(emptyMap, emptyMap.getClass());
    assertTrue(element instanceof JsonObject);
    JsonObject emptyMapJsonObject = (JsonObject) element;
    assertTrue(emptyMapJsonObject.entrySet().isEmpty());
  }

  public void testEmptyMapSerialization() {
    Type mapType = new TypeToken<Map<String, String>>() { }.getType();
    Map<String, String> emptyMap = new HashMap<String, String>();
    JsonElement element = gson.toJsonTree(emptyMap, mapType);

    assertTrue(element instanceof JsonObject);
    JsonObject emptyMapJsonObject = (JsonObject) element;
    assertTrue(emptyMapJsonObject.entrySet().isEmpty());
  }

  public void testNonEmptyMapSerialization() {
    Type mapType = new TypeToken<Map<String, String>>() { }.getType();
    Map<String, String> myMap = new HashMap<String, String>();
    String key = "key1";
    myMap.put(key, "value1");
    Gson gson = new Gson();
    JsonElement element = gson.toJsonTree(myMap, mapType);

    assertTrue(element.isJsonObject());
    JsonObject mapJsonObject = element.getAsJsonObject();
    assertTrue(mapJsonObject.has(key));
  }
}
