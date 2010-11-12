/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.rest.server;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.greaze.definition.rest.ID;
import com.google.greaze.definition.rest.RestCallSpec;
import com.google.greaze.definition.rest.RestResource;

/**
 * A map of {@link RestCallSpec}, {@link RestResponseBuilder} to help figure out which
 * {@link RestResponseBuilder} to use for a {@link RestCallSpec}.
 *
 * @author Inderjeet Singh
 */
public final class ResponseBuilderMap {
  public static final class Builder {
    private final Map<Type, RestResponseBuilder<?, ?>> map =
      new HashMap<Type, RestResponseBuilder<?, ?>>();
    
    public <I extends ID, R extends RestResource<I, R>> Builder set(
        Type resourceType, RestResponseBuilder<I, R> responseBuilder) {
      map.put(resourceType, responseBuilder);
      return this;
    }

    public ResponseBuilderMap build() {
      return new ResponseBuilderMap(map);
    }
  }

  private final Map<Type, RestResponseBuilder<?, ?>> map;

  public ResponseBuilderMap(Map<Type, RestResponseBuilder<?, ?>> map) {
    this.map = map;
  }
  
  public RestResponseBuilder<?, ?> get(Type resourceType) {
    return (RestResponseBuilder<?, ?>)map.get(resourceType);
  }
}
