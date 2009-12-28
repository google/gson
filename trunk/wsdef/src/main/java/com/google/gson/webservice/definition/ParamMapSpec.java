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
import java.util.Map;
import java.util.Set;

/**
 * Specification of a parameter map. Both {@link ContentBody} and {@link HeaderMap} are 
 * parameter maps.
 * 
 * @author inder
 */
interface ParamMapSpec {

  Type getTypeFor(String paramName);

  boolean checkIfCompatible(String paramName, Type type);

  boolean checkIfCompatible(String paramName, Object object);
  
  public Set<Map.Entry<String, Type>> entrySet();

  public int size();

}