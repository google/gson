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

import com.google.common.collect.Maps;

/**
 * Specification of a {@link RequestBody}.
 * 
 * @author inder
 */
public final class RequestBodySpec extends ContentBodySpec {
  
  public static class Builder {
    private final Map<String, Type> paramsSpec = Maps.newLinkedHashMap();
    public Builder add(String paramName, Type type) {
      paramsSpec.put(paramName, type);
      return this;
    }
    
    public RequestBodySpec create() {
      RequestBodySpec spec = new RequestBodySpec(paramsSpec);
      return spec;
    }    
  }
  
  public RequestBodySpec(Map<String, Type> paramsSpec) {
    super(paramsSpec);
  }
}
