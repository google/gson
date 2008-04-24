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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A class representing an object type in Json. An object consists of 
 * name-value pairs where names are strings, and values are any other 
 * type of {@link JsonElement} 
 * 
 * @author Inderjeet Singh
 */
public final class JsonObject extends JsonElement {
  private Map<String, JsonElement> members = Maps.newHashMap();

  public void add(String property, JsonElement value) {
    members.put(property, value);
  }
  
  public boolean has(String property) {
    return members.containsKey(property);
  }
  
  public JsonElement get(String property) {
    return members.get(property);
  }
  
  public JsonPrimitive getAsPrimitive(String property) {
    return (JsonPrimitive) members.get(property);
  }
  
  public JsonArray getAsArray(String property) {
    return (JsonArray) members.get(property);
  }
}
