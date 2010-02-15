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

import com.google.gson.webservice.definition.HeaderMapSpec;

/**
 * Specification for a {@link RestResponse}.
 * 
 * @author inder
 */
public final class RestResponseSpec<R> {
  private final HeaderMapSpec headersSpec;
  private final Class<R> resourceClass;

  public RestResponseSpec(HeaderMapSpec headersSpec, Class<R> resourceClass) {
    this.headersSpec = headersSpec;
    this.resourceClass = resourceClass;
  }

  public Class<R> getResourceClass() {
    return resourceClass;
  }

  public HeaderMapSpec getHeadersSpec() {
    return headersSpec;
  }
  
  @Override
  public String toString() {
    return String.format("{headersSpec:%s,resourceClass:%s}", headersSpec, resourceClass);
  }
}
