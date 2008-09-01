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
package com.google.gson.wsf;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Body of a request or response. The body contains a map of name-value pairs.
 * There is a {@link ContentBodySpec} associated with the body as well and only the name-value 
 * pairs consistent with the specification are permitted.
 * 
 * @author inder
 */
class ContentBody {
  
  protected final ContentBodySpec spec;
  protected final Map<String, Object> contents;
  
  ContentBody(ContentBodySpec spec) {
    this.spec = spec;
    this.contents = Maps.newLinkedHashMap();
  }
  
  ContentBody(ContentBodySpec spec, Map<String, Object> contents) {
    this.spec = spec;
    this.contents = contents;
  }
  
  public ContentBodySpec getSpec() {
    return spec;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> classOfValue) {
    Preconditions.checkArgument(spec.isCompatible(key, classOfValue));
    return (T) contents.get(key);
  }
  
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Type typeOfValue) {
    Preconditions.checkArgument(spec.isCompatible(key, typeOfValue));
    return (T) contents.get(key);
  }
  
  public Set<Map.Entry<String, Object>> entrySet() {
    return contents.entrySet();
  }
  
  public int size() {
    return contents.size();
  }
  
  public String getContentType() {
    return spec.getContentType();
  }
  
  public String getCharacterEncoding() {
    return spec.getCharacterEncoding();
  }
  
  @Override
  public String toString() {
    return Util.toStringMap(contents);
  }
}
