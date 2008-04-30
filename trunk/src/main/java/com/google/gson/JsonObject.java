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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A class representing an object type in Json. An object consists of 
 * name-value pairs where names are strings, and values are any other 
 * type of {@link JsonElement} 
 * 
 * @author Inderjeet Singh
 */
public final class JsonObject extends JsonElement {
  // We are using a linked hash map because it is important to preserve
  // the order in which elements are inserted. This is needed to ensure
  // that the fields of an object are inserted in the order they were 
  // defined in the class. 
  private Map<String, JsonElement> members = new LinkedHashMap<String, JsonElement>();

  public void add(String property, JsonElement value) {
    members.put(property, value);
  }
  
  public void addProperty(String property, String value) {
    members.put(property, new JsonPrimitive(value));
  }
  
  public void addProperty(String property, Number value) {
    members.put(property, new JsonPrimitive(value));
  }

  public Set<Entry<String, JsonElement>> getEntries() {
    return members.entrySet();
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

  @Override
  protected void toString(StringBuilder sb) {
    sb.append('{');
    boolean first = true;
    for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(',');
      }
      sb.append('\"');
      sb.append(entry.getKey());
      sb.append("\":");
      entry.getValue().toString(sb);
    }
    sb.append('}');
  }
}
