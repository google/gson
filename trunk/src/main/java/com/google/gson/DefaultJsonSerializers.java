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

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Default JSON serializers for common Java types
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultJsonSerializers {

  @SuppressWarnings("unchecked")
  static Map<Type, JsonSerializer<?>> getDefaultSerializers() {
    Map<Type, JsonSerializer<?>> map = new LinkedHashMap<Type, JsonSerializer<?>>();
    map.put(Enum.class, new EnumSerializer());
    map.put(Map.class, new MapSerializer());
    map.put(URL.class, new UrlSerializer());
    map.put(URI.class, new UriSerializer());
    map.put(Locale.class, new LocaleSerializer());
    map.put(Date.class, DefaultDateTypeAdapter.DEFAULT_TYPE_ADAPTER);
    return map;
  }

  @SuppressWarnings("unchecked")
  private static class EnumSerializer<T extends Enum> implements JsonSerializer<T> {
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.name());
    }
  }

  private static class UrlSerializer implements JsonSerializer<URL> {
    public JsonElement serialize(URL src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toExternalForm());
    }
  }

  private static class UriSerializer implements JsonSerializer<URI> {
    public JsonElement serialize(URI src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toASCIIString());
    }
  }

  private static class LocaleSerializer implements JsonSerializer<Locale> {
    public JsonElement serialize(Locale src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }
  }

  @SuppressWarnings("unchecked")
  private static class MapSerializer implements JsonSerializer<Map> {
    public JsonElement serialize(Map src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject map = new JsonObject();
      Type childType = new TypeInfoMap(typeOfSrc).getValueType();
      for (Iterator iterator = src.entrySet().iterator(); iterator.hasNext(); ) {
        Map.Entry entry = (Map.Entry) iterator.next();
        JsonElement valueElement = context.serialize(entry.getValue(), childType);
        map.add(entry.getKey().toString(), valueElement);
      }
      return map;
    }
  }
}
