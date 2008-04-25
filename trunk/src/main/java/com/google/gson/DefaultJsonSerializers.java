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

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
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
    Map<Type, JsonSerializer<?>> map = Maps.newHashMap();
    map.put(Enum.class, new EnumSerializer());
    map.put(URL.class, new UrlSerializer());
    map.put(URI.class, new UriSerializer());
    return map;
  }

  @SuppressWarnings("unchecked")
  private static class EnumSerializer<T extends Enum> implements JsonSerializer<T> {

    public JsonElement toJson(T src) {
      return new JsonPrimitive(src.name());
    }
  }
  
  private static class UrlSerializer implements JsonSerializer<URL> {
	  
    public JsonElement toJson(URL src) {
      return new JsonPrimitive(src.toExternalForm());
    }    
  }
  
  private static class UriSerializer implements JsonSerializer<URI> {

    public JsonElement toJson(URI src) {
      return new JsonPrimitive(src.toASCIIString());
    }    
  }
}
