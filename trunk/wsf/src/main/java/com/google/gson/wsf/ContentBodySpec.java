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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Base class for the specification of a {@link ContentBody}.
 * 
 * @author inder
 */
class ContentBodySpec implements ParamMapSpec {

  public static final String JSON_CONTENT_TYPE = "application/json";
  private static final String JSON_CHARACTER_ENCODING = "utf-8";
  
  private final Map<String, Type> paramsSpec;

  protected ContentBodySpec(Map<String, Type> paramsSpec) {
    this.paramsSpec = Collections.unmodifiableMap(paramsSpec);
  }
  
  public Type getTypeFor(String paramName) {
    return paramsSpec.get(paramName);
  }
  
  public boolean checkIfCompatible(String paramName, Type type) {
    return type.equals(getTypeFor(paramName));
  }
  
  public boolean checkIfCompatible(String paramName, Object object) {
    return checkIfCompatible(paramName, object.getClass());
  }
  
  public Set<Map.Entry<String, Type>> entrySet() {
    return paramsSpec.entrySet();
  }
  
  public int size() {
    return paramsSpec.size();
  }
  
  public String getContentType() {
    return JSON_CONTENT_TYPE;
  }
  
  public String getCharacterEncoding() {
    return JSON_CHARACTER_ENCODING;
  }
  
  @Override
  public String toString() {
    return Util.toStringMapKeys(paramsSpec);
  }
}
