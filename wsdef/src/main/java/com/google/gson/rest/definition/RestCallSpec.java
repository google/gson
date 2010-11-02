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
package com.google.gson.rest.definition;

import com.google.gson.webservice.definition.CallPath;
import com.google.gson.webservice.definition.HeaderMapSpec;
import com.google.gson.webservice.definition.HttpMethod;
import com.google.gson.webservice.definition.TypedKey;
import com.google.gson.webservice.definition.internal.utils.Preconditions;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Specification for a REST service
 *
 * @author inder
 */
public final class RestCallSpec {
  public static class Builder {
    private final CallPath callPath;
    private final Set<HttpMethod> supportedHttpMethods = new LinkedHashSet<HttpMethod>();
    private final HeaderMapSpec.Builder reqParamsSpecBuilder = new HeaderMapSpec.Builder();
    private final HeaderMapSpec.Builder resParamsSpecBuilder = new HeaderMapSpec.Builder();
    private final Type resourceType;
    private double version;
    
    public Builder(CallPath callPath, Type resourceType) {
      this.callPath = callPath;
      supportedHttpMethods.addAll(HttpMethod.ALL_METHODS);
      this.resourceType = resourceType;
      this.version = -1D; 
    }

    public Builder disableHttpMethod(HttpMethod httpMethod) {
      supportedHttpMethods.remove(httpMethod);
      return this;
    }

    public Builder setVersion(double version) {
      this.version = version;
      return this;
    }

    public <T> Builder addRequestParam(TypedKey<T> param) {
      reqParamsSpecBuilder.put(param.getName(), param.getClassOfT());
      return this;
    }

    public <T> Builder addResponseParam(TypedKey<T> param) {
      resParamsSpecBuilder.put(param.getName(), param.getClassOfT());
      return this;
    }

    public RestCallSpec build() {
      if (supportedHttpMethods.isEmpty()) {
        supportedHttpMethods.addAll(Arrays.asList(HttpMethod.values()));
      }
      RestRequestSpec requestSpec = 
        new RestRequestSpec(reqParamsSpecBuilder.build(), resourceType);
      RestResponseSpec responseSpec =
        new RestResponseSpec(resParamsSpecBuilder.build(), resourceType);
      return new RestCallSpec(supportedHttpMethods, callPath, 
          requestSpec, responseSpec, resourceType, version);
    }
  }

  private final Set<HttpMethod> supportedHttpMethods;
  private final CallPath path;
  private final RestRequestSpec requestSpec;
  private final RestResponseSpec responseSpec;
  private final Type resourceType;
  private final double version;

  private RestCallSpec(Set<HttpMethod> supportedHttpMethods, CallPath path,
      RestRequestSpec requestSpec, RestResponseSpec responseSpec,
      Type resourceType, double version) {
    Preconditions.checkArgument(!supportedHttpMethods.isEmpty());
    Preconditions.checkNotNull(path);
    this.supportedHttpMethods = supportedHttpMethods;
    this.path = path;
    this.requestSpec = requestSpec;
    this.responseSpec = responseSpec;
    this.resourceType = resourceType;
    this.version = version;
  }

  public CallPath getPath() {
    return path;
  }
  
  public Set<HttpMethod> getSupportedHttpMethods() {
    return supportedHttpMethods;
  }

  public RestResponseSpec getResponseSpec() {
    return responseSpec;
  }
  
  public RestRequestSpec getRequestSpec() {
    return requestSpec;
  }

  public Type getResourceType() {
    return resourceType;
  }

  public double getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return String.format(
      "path: %s, version: %.2f, resourceType: %s, requestSpec: %s, responseSpec: %s",
      path, version, resourceType, requestSpec, responseSpec);
  }
}
