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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Default JSON deserializers for common Java types
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultJsonDeserializers {

  @SuppressWarnings("unchecked")
  static Map<Type, JsonDeserializer<?>> getDefaultDeserializers() {
    Map<Type, JsonDeserializer<?>> map = new LinkedHashMap<Type, JsonDeserializer<?>>();
    map.put(Enum.class, new EnumDeserializer());
    map.put(Map.class, new MapDeserializer());
    map.put(URL.class, new UrlDeserializer());
    map.put(URI.class, new UriDeserializer());
    map.put(Locale.class, new LocaleDeserializer());
    return map;
  }

  @SuppressWarnings("unchecked")
  private static class EnumDeserializer<T extends Enum> implements JsonDeserializer<T> {
    @SuppressWarnings("cast")
    public T deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context) 
        throws JsonParseException {
      return (T) Enum.valueOf((Class<T>)classOfT, json.getAsString());
    }
  }
  
  private static class UrlDeserializer implements JsonDeserializer<URL> {
    public URL deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
        throws JsonParseException {
      try {
        return new URL(json.getAsString());
      } catch (MalformedURLException e) {
        throw new JsonParseException(e);
      }
    }    
  }
  
  private static class UriDeserializer implements JsonDeserializer<URI> {
    public URI deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
        throws JsonParseException {
      try {
	  	return new URI(json.getAsString());
      } catch (URISyntaxException e) {
	    throw new JsonParseException(e);
      }
    }
  }
  
  private static class LocaleDeserializer implements JsonDeserializer<Locale> {
    public Locale deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
        throws JsonParseException {
      String locale = json.getAsString();
      StringTokenizer tokenizer = new StringTokenizer(locale, "_");
      String language = null;
      String country = null;
      String variant = null;
      if (tokenizer.hasMoreElements()) {
        language = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        country = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        variant = tokenizer.nextToken();
      }
      if (country == null && variant == null) {
        return new Locale(language);
      } else if (variant == null) {
        return new Locale(language, country);
      } else {
        return new Locale(language, country, variant);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private static class MapDeserializer implements JsonDeserializer<Map> {

    public Map deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
        throws JsonParseException {
      // Using linked hash map to preserve order in which elements are entered
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      Type childType = new MapTypeInfo(typeOfT).getValueType();     
      for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
        Object value = context.deserialize(entry.getValue(), childType);
        map.put(entry.getKey(), value);
      }
      return map;
    }
  }
}
