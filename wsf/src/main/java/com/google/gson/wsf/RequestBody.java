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

import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Definition of the request body of a {@link WebServiceCall}. The request body is what is sent out
 * in the output stream of the request (for example, with 
 * {@link java.net.HttpURLConnection#getOutputStream()}) , and is read by the 
 * {@link javax.servlet.http.HttpServletRequest#getInputStream()}.
 * 
 * @author inder
 */
public final class RequestBody extends ContentBody {

  public static class Builder {    
    private final Map<String, Object> contents = Maps.newLinkedHashMap();
    private final RequestBodySpec spec;
    
    @Inject
    public Builder(RequestBodySpec spec) {
      this.spec = spec;
    }
    
    public Builder add(String paramName, Object content) {
      return add(paramName, content, content.getClass());
    }
    
    public Builder add(String paramName, Object content, Type typeOfContent) {
      spec.isCompatible(paramName, typeOfContent);
      contents.put(paramName, content);
      return this;
    }
    
    public RequestBody create() {
      RequestBody requestBody = new RequestBody(spec, contents);
      return requestBody;
    }    
  }

  public RequestBody(RequestBodySpec spec, Map<String, Object> contents) {
    super(spec, contents);
  }
  
  @Override
  public RequestBodySpec getSpec() {
    return (RequestBodySpec) spec;
  }  
}
