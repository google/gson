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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An enum of Http methods to provide strongly-typed versions instead of strings. 
 * 
 * @author inder
 */
public enum HttpMethod { 
  GET, 
  POST, 
  PUT, 
  DELETE;
  
  public static HttpMethod getMethod(String method) {
    return valueOf(method.trim().toUpperCase());
  }
  
  public static final List<HttpMethod> ALL_METHODS =
    Collections.unmodifiableList(Arrays.asList(values()));

  /**
   * This header is used to indicate the real method that is channeled through the current
   * request. For example, you can use it to send PUT requests under a POST.
   */
  public static final String SIMULATED_METHOD_HEADER = "SimulatedMethod";
}
