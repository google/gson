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
package com.google.gson.webservice.definition;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class ParamMap {

  public static class Builder<T extends ParamMapSpec> {    
    protected final Map<String, Object> contents = new LinkedHashMap<String, Object>();
    protected final T spec;

    public Builder(T spec) {
      this.spec = spec;
    }

    /**
     * If value is a generic type, use {@link #put(String, Object, Type)} instead.
     */
    public Builder<T> put(String paramName, Object content) {
      return put(paramName, content, content.getClass());
    }

    public Builder<T> put(String paramName, Object content, Type typeOfContent) {
      Preconditions.checkArgument(spec.checkIfCompatible(paramName, typeOfContent));
      contents.put(paramName, content);
      return this;
    }

    public <K> Builder<T> put(TypedKey<K> paramKey, K param) {
      contents.put(paramKey.getName(), param);
      return this;
    }
  }
  
  protected final Map<String, Object> contents;
  protected final ParamMapSpec spec;

  protected ParamMap(ParamMapSpec spec, Map<String, Object> contents) {
    this.spec = spec;
    this.contents = contents;
  }

  public ParamMapSpec getSpec() {
    return spec;
  }

  public Object get(String paramName) {
    return contents.get(paramName);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(TypedKey<T> key) {
    return (T) get(key.getName(), key.getClassOfT());
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key, Type typeOfValue) {
    Preconditions.checkArgument(spec.checkIfCompatible(key, typeOfValue),
        "Incompatible key %s for type %s", key, typeOfValue);
    return (T) contents.get(key);
  }
  
  public Type getSpec(String headerName) {
    return spec.getTypeFor(headerName);
  }

  public Set<Map.Entry<String, Object>> entrySet() {
    return contents.entrySet();
  }

  public int size() {
    return contents.size();
  }

  @Override
  public String toString() {
    return Util.toStringMap(contents);
  }
}
