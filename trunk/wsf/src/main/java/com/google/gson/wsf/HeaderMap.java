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
 * Map of request or response header objects. There is a {@link HeaderMapSpec} associated with the
 * map as well and only those headers are allowed that are consistent with the specification.
 * 
 * @author inder
 */
public final class HeaderMap {

  private final Map<String, Object> contents;
  private final HeaderMapSpec spec;
  
  public HeaderMap(HeaderMapSpec spec) {
    this.spec = spec;
    contents = Maps.newHashMap();
  }  
  
  /**
   * If paramValue is a generic type, use {@link #put(String, Object, Type)} instead.
   * 
   * @param headerName
   * @param headerValue
   */
  public void put(String headerName, Object headerValue) {
    Preconditions.checkArgument(spec.isCompatible(headerName, headerValue));
    contents.put(headerName, headerValue);
  }
  
  public void put(String headerName, Object headerValue, Type typeOfHeaderValue) {
    Preconditions.checkArgument(spec.isCompatible(headerName, typeOfHeaderValue));
    contents.put(headerName, headerValue);
  }
  
  public HeaderMapSpec getSpec() {
    return spec;
  }

  public Object get(String headerName) {
    return contents.get(headerName);
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
