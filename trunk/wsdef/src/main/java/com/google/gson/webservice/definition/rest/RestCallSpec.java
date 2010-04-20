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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.webservice.definition.CallPath;
import com.google.gson.webservice.definition.HeaderMapSpec;
import com.google.gson.webservice.definition.HttpMethod;
import com.google.gson.webservice.definition.TypedKey;

/**
 * Specification for a REST service
 *
 * @author inder
 */
public final class RestCallSpec<R> {
  public static class Builder<R> {
    private final CallPath callPath;
    private final Set<HttpMethod> supportedHttpMethods = new LinkedHashSet<HttpMethod>();
    private final HeaderMapSpec.Builder reqParamsSpecBuilder = new HeaderMapSpec.Builder();
    private final HeaderMapSpec.Builder resParamsSpecBuilder = new HeaderMapSpec.Builder();
    private final Class<R> resourceClass;
    
    public Builder(CallPath callPath, Class<R> resourceClass) {
      this.callPath = callPath;
      supportedHttpMethods.addAll(HttpMethod.ALL_METHODS);
      this.resourceClass = resourceClass;
    }

    public Builder<R> disableHttpMethod(HttpMethod httpMethod) {
      supportedHttpMethods.remove(httpMethod);
      return this;
    }
    
    public <T> Builder<R> addRequestParam(TypedKey<T> param) {
      reqParamsSpecBuilder.put(param.getName(), param.getClassOfT());
      return this;
    }

    public <T> Builder<R> addResponseParam(TypedKey<T> param) {
      resParamsSpecBuilder.put(param.getName(), param.getClassOfT());
      return this;
    }

    public RestCallSpec<R> build() {
      if (supportedHttpMethods.isEmpty()) {
        supportedHttpMethods.addAll(Arrays.asList(HttpMethod.values()));
      }
      RestRequestSpec<R> requestSpec = 
        new RestRequestSpec<R>(reqParamsSpecBuilder.build(), resourceClass);
      RestResponseSpec<R> responseSpec =
        new RestResponseSpec<R>(resParamsSpecBuilder.build(), resourceClass);
      return new RestCallSpec<R>(supportedHttpMethods, callPath, 
          requestSpec, responseSpec);
    }
  }

  private final Set<HttpMethod> supportedHttpMethods;
  private final CallPath path;
  private final RestRequestSpec<R> requestSpec;
  private final RestResponseSpec<R> responseSpec;

  private RestCallSpec(Set<HttpMethod> supportedHttpMethods, CallPath path,
      RestRequestSpec<R> requestSpec, RestResponseSpec<R> responseSpec) {
    Preconditions.checkArgument(!supportedHttpMethods.isEmpty());
    Preconditions.checkNotNull(path);
    this.supportedHttpMethods = supportedHttpMethods;
    this.path = path;
    this.requestSpec = requestSpec;
    this.responseSpec = responseSpec;
  }

  public CallPath getPath() {
    return path;
  }
  
  public Set<HttpMethod> getSupportedHttpMethods() {
    return supportedHttpMethods;
  }

  public RestResponseSpec<R> getResponseSpec() {
    return responseSpec;
  }
  
  public RestRequestSpec<R> getRequestSpec() {
    return requestSpec;
  }
}
