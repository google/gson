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
package com.google.gson.webservice.definition.rest;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.webservice.definition.CallPath;

/**
 * A {@link Map} of {@link CallPath} to {@link RestCallSpec}
 *
 * @author inder
 */
public final class ResourceMap {

  public static final class Builder {
    private final Map<CallPath, RestCallSpec> resources =
      new HashMap<CallPath, RestCallSpec>();
    
    public Builder set(CallPath callPath, RestCallSpec spec) {
      Preconditions.checkArgument(resources.get(callPath) == null);
      resources.put(callPath, spec);
      return this;
    }

    public ResourceMap build() {
      return new ResourceMap(resources);
    }
  }

  private final Map<CallPath, RestCallSpec> resources;

  public ResourceMap(Map<CallPath, RestCallSpec> resources) {
    this.resources = resources;
  }

  public RestCallSpec get(CallPath callPath) {
    return (RestCallSpec)resources.get(callPath);
  }
}
