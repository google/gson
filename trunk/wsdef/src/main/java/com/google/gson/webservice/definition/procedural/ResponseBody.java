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
package com.google.gson.webservice.definition.procedural;

import com.google.gson.webservice.definition.ContentBody;
import com.google.gson.webservice.definition.ParamMap;
import com.google.gson.webservice.definition.TypedKey;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * body of the response. This is written out as JSON to be sent out to the client. 
 * This class omits the default constructor for use by Gson. Instead the user must use
 * {@link com.google.gson.webservice.definition.procedural.ResponseBodyGsonConverter}
 *
 * @author inder
 */
public final class ResponseBody extends ContentBody {

  public static class Builder extends ParamMap.Builder<ResponseBodySpec> {    

    public Builder(ResponseBodySpec spec) {
      super(spec);
    }

    @Override
    public Builder put(String paramName, Object content) {
      return (Builder) super.put(paramName, content);
    }

    @Override
    public Builder put(String paramName, Object content, Type typeOfContent) {
      return (Builder) super.put(paramName, content, typeOfContent);
    }

    @Override
    public <T> Builder put(TypedKey<T> paramKey, T param) {
      return (Builder) super.put(paramKey, param);
    }

    public ResponseBody build() {
      return new ResponseBody(spec, contents);
    }    
  }

  private ResponseBody(ResponseBodySpec spec, Map<String, Object> contents) {
    super(spec, contents);
  }
  
  @Override
  public ResponseBodySpec getSpec() {
    return (ResponseBodySpec) spec;
  }
}
