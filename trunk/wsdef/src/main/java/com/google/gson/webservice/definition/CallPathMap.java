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

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A generic Map of calls with relative path where the call is available as the key. 
 * 
 * @author inder
 *
 * @param <T> The target of the call path.
 */
public final class CallPathMap<T> {
  
  public static class Builder<T> {
    private final Map<CallPath, T> contents = Maps.newHashMap();
    private final T nullValue;
    
    public Builder(T nullValue) {
      this.nullValue = nullValue;
    }
    public <R extends T> Builder<T> put(CallPath path, R content) {
      contents.put(path, content);
      return this;
    }
    
    public CallPathMap<T> create() {
      return new CallPathMap<T>(contents, nullValue);
    }
  }
  
  private final Map<CallPath, T> contents;
  private final T nullValue;
  
  private CallPathMap(Map<CallPath, T> contents, T nullValue) {
    this.contents = contents;
    this.nullValue = nullValue;
  }
  
  public T get(CallPath path) {
    T content = contents.get(path);
    return content == null ? nullValue : content;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<CallPath, T> entry : contents.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }
      CallPath path = entry.getKey();
      sb.append(path.get()).append(":");
      sb.append(entry.getValue().toString());
    }
    sb.append("}");
    return sb.toString();
  }
}