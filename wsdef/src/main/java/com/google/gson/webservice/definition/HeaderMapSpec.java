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
package com.google.gson.webservice.definition;


import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Specification of a header map for {@link HeaderMap}. 
 *
 * @author inder
 */
public final class HeaderMapSpec implements ParamMapSpec {
  
  public static class Builder {
    private final Map<String, Type> map = new LinkedHashMap<String, Type>();
  
    public void put(String headerName, Type headerType) {
      map.put(headerName, headerType);
    }
    
    public HeaderMapSpec build() {
      return new HeaderMapSpec(map);
    }
  }
  private final Map<String, Type> map;
  
  private HeaderMapSpec(Map<String, Type> map) {
    this.map = map;
  }  
  
  @Override
  public Type getTypeFor(String headerName) {
    return map.get(headerName);
  }
  
  @Override
  public Set<Map.Entry<String, Type>> entrySet() {
    return map.entrySet();
  }
  
  @Override
  public boolean checkIfCompatible(String headerName, Type targetType) {
    Type typeOfHeader = getTypeFor(headerName);
    if (typeOfHeader == null) {
      return false;
    }
    Class<?> rawClassOfHeader = TypeUtils.toRawClass(typeOfHeader);
    Class<?> rawClassOfTargetType = TypeUtils.toRawClass(targetType);
    return rawClassOfHeader.isAssignableFrom(rawClassOfTargetType);
  }

  @Override
  public boolean checkIfCompatible(String headerName, Object headerValue) {
    return checkIfCompatible(headerName, headerValue.getClass());
  }
  
  @Override
  public String toString() {
    return Util.toStringMapKeys(map);
  }

  @Override
  public int size() {
    return map.size();
  }
}