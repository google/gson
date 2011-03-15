/*
 * Copyright (C) 2011 Google Inc.
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default serialization and deserialization of a map type. This implementation really only works
 * well with simple primitive types as the map key. If the key is not a simple primitive then the
 * object is {@code toString}ed and that value is used as its key.
 *
 * @author Joel Leitch
 */
@SuppressWarnings("unchecked") 
final class MapTypeAdapter implements JsonSerializer<Map<?, ?>>,
    JsonDeserializer<Map<?, ?>>, InstanceCreator<Map<?, ?>> {

  public JsonElement serialize(Map src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject map = new JsonObject();
    Type childGenericType = null;
    if (typeOfSrc instanceof ParameterizedType) {
      Class<?> rawTypeOfSrc = Types.getRawType(typeOfSrc);
      childGenericType = Types.getMapKeyAndValueTypes(typeOfSrc, rawTypeOfSrc)[1];
    }

    for (Map.Entry entry : (Set<Map.Entry>) src.entrySet()) {
      Object value = entry.getValue();

      JsonElement valueElement;
      if (value == null) {
        valueElement = JsonNull.createJsonNull();
      } else {
        Type childType = (childGenericType == null)
            ? value.getClass() : childGenericType;
        valueElement = context.serialize(value, childType);
      }
      map.add(String.valueOf(entry.getKey()), valueElement);
    }
    return map;
  }

  public Map deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    // Use ObjectConstructor to create instance instead of hard-coding a specific type.
    // This handles cases where users are using their own subclass of Map.
    Map<Object, Object> map = constructMapType(typeOfT, context);
    Type[] keyAndValueTypes = Types.getMapKeyAndValueTypes(typeOfT, Types.getRawType(typeOfT));
    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
      Object key = context.deserialize(new JsonPrimitive(entry.getKey()), keyAndValueTypes[0]);
      Object value = context.deserialize(entry.getValue(), keyAndValueTypes[1]);
      map.put(key, value);
    }
    return map;
  }

  private Map constructMapType(Type mapType, JsonDeserializationContext context) {
    JsonDeserializationContextDefault contextImpl = (JsonDeserializationContextDefault) context;
    ObjectConstructor objectConstructor = contextImpl.getObjectConstructor();
    return (Map) objectConstructor.construct(mapType);
  }

  public Map<Object, Object> createInstance(Type type) {
    return new LinkedHashMap();
  }

  @Override
  public String toString() {
    return MapTypeAdapter.class.getSimpleName();
  }
}