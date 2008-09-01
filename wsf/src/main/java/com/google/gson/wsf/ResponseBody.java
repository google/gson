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

import com.google.common.base.Preconditions;

/**
 * body of the response. This is written out as JSON to be sent out to the client. 
 * 
 * @author inder
 */
public final class ResponseBody extends ContentBody {

  public ResponseBody(ResponseBodySpec spec) {
    super(spec);
  }
  
  @Override
  public ResponseBodySpec getSpec() {
    return (ResponseBodySpec) spec;
  }
  
  /**
   * If value is a generic type, use {@link #put(String, Object, Type)} instead.
   * 
   * @param key
   * @param value
   */
  public void put(String key, Object value) {
    put(key, value, value.getClass());
  }
    
  public void put(String key, Object value, Type typeOfValue) {
    Type expectedType = spec.getTypeFor(key);
    Preconditions.checkArgument(Util.isAssignableFrom(typeOfValue, expectedType));
    contents.put(key, value);
  }  
}
