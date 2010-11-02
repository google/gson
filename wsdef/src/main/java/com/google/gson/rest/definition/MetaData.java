/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.rest.definition;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * Metadata associated with a repository for a rest resource. Metadata is of two types: persistent
 * and transient. All metadata is persistent by default, and must be a name-value pair of strings.
 * Transient metadata can be an arbitrary key-value pair of objects and is available through
 * {@link #getFromTransient(Object)}, {@link #putInTransient(Object, Object)},
 * and {@link #removeFromTransient(Object)} methods.
 *
 * @author inder
 *
 * @param <R> The resource
 */
public final class MetaData<R extends RestResource<R>> {

  private final Map<String, String> map;
  private final transient Map<Object, Object> mapTransient;

  public static <RS extends RestResource<RS>> MetaData<RS> create() {
    return new MetaData<RS>();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static MetaData<?> createTypeUnsafe(Map<String, String> values) {
    return new MetaData(values);
  }

  public MetaData() {
    this(new HashMap<String, String>());
  }

  private MetaData(Map<String, String> values) {
    this.map = values == null ? new HashMap<String, String>() : values;
    this.mapTransient = new HashMap<Object, Object>();
  }

  public String getString(String key) {
    return map.get(key);
  }

  public void putString(String key, String value) {
    map.put(key, value);
  }

  public boolean getBoolean(String key) {
    String value = map.get(key);
    return value == null ? false : Boolean.parseBoolean(value);
  }

  public void putBoolean(String key, boolean value) {
    map.put(key, String.valueOf(value));
  }

  public void remove(String key) {
    map.remove(key);
  }

  public Object getFromTransient(Object key) {
    return mapTransient.get(key);
  }

  public void putInTransient(Object key, Object value) {
    mapTransient.put(key, value);
  }

  public void removeFromTransient(Object key) {
    mapTransient.remove(key);
  }

  @Override
  public String toString() {
    return new StringBuilder().append(map).append(',').append(mapTransient).toString();
  }

  /**
   * Gson Type adapter for {@link MetaData}. The serialized representation on wire is just a
   * Map<String, String>
   */
  public static final class GsonTypeAdapter implements JsonSerializer<MetaData<?>>,
    JsonDeserializer<MetaData<?>>{

    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    @Override
    public MetaData<?> deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      Map<String, String> map = context.deserialize(json, MAP_TYPE);
      return MetaData.createTypeUnsafe(map);
    }

    @Override
    public JsonElement serialize(MetaData<?> src, Type typeOfSrc,
        JsonSerializationContext context) {
      return context.serialize(src.map, MAP_TYPE);
    }
  }
}
