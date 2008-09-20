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

import com.google.inject.Inject;

/**
 * Map of request or response header objects. There is a {@link HeaderMapSpec} associated with the
 * map as well and only those headers are allowed that are consistent with the specification.
 * 
 * @author inder
 */
public final class HeaderMap extends ParamMap {

  public static class Builder extends ParamMap.Builder<HeaderMapSpec> {
    @Inject
    public Builder(HeaderMapSpec spec) {
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
    
    public HeaderMap create() {
      return new HeaderMap(spec, contents);
    }
  }
  
  private HeaderMap(HeaderMapSpec spec, Map<String, Object> contents) {
    super(spec, contents);
  }  

  @Override
  public HeaderMapSpec getSpec() {
    return (HeaderMapSpec) super.getSpec();
  }
}
