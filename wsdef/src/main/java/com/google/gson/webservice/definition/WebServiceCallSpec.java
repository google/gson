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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Preconditions;


/**
 * Specification for a Json web service call. The call includes the relative path where the call 
 * is available, the specification of requests, and responses. 
 * 
 * @author inder
 */
public final class WebServiceCallSpec {
  
  public static final WebServiceCallSpec NULL_SPEC = new Builder(new CallPath("")).create();
  
  public static class Builder {
	private final CallPath callPath;
	private final Set<HttpMethod> supportedHttpMethods = new LinkedHashSet<HttpMethod>();
    private final HeaderMapSpec.Builder reqParamsSpecBuilder = new HeaderMapSpec.Builder();
    private final RequestBodySpec.Builder reqBodySpecBuilder = new RequestBodySpec.Builder();
    private final HeaderMapSpec.Builder resParamsSpecBuilder = new HeaderMapSpec.Builder();
    private final ResponseBodySpec.Builder resBodySpecBuilder = new ResponseBodySpec.Builder();
    
    public Builder(CallPath callPath) {
      this.callPath = callPath;      
    }
    
    /**
     * If this method is not invoked, then it is assumed that the WebServiceCall supports all
     * methods specified in {@link HttpMethod#values()}.
     * 
     * @param httpMethods list of methods that this call supports.
     * @return self to follow the Builder pattern.
     */
    public Builder supportsHttpMethod(HttpMethod... httpMethods) {
      supportedHttpMethods.addAll(Arrays.asList(httpMethods));
      return this;
    }
    
    public Builder addRequestParam(String paramName, Type type) {
      reqParamsSpecBuilder.put(paramName, type);
      return this;
    }
    public Builder addRequestBodyParam(String paramName, Type type) {
      reqBodySpecBuilder.add(paramName, type);
      return this;
    }
    public Builder addResponseParam(String paramName, Type type) {
      resParamsSpecBuilder.put(paramName, type);
      return this;
    }
    public Builder addResponseBodyParam(String paramName, Type type) {
      resBodySpecBuilder.add(paramName, type);
      return this;
    }
    public WebServiceCallSpec create() {      
      if (supportedHttpMethods.isEmpty()) {
        supportedHttpMethods.addAll(Arrays.asList(HttpMethod.values()));
      }
      RequestSpec requestSpec = 
        new RequestSpec(reqParamsSpecBuilder.create(), reqBodySpecBuilder.create());
      ResponseSpec responseSpec = 
        new ResponseSpec(resParamsSpecBuilder.create(), resBodySpecBuilder.create());
      WebServiceCallSpec callSpec = new WebServiceCallSpec(supportedHttpMethods, callPath, 
          requestSpec, responseSpec);
      return callSpec;
    }
  }
  
  private final Set<HttpMethod> supportedHttpMethods;
  private final CallPath path;
  private final ResponseSpec responseSpec;
  private final RequestSpec requestSpec;
  
  private WebServiceCallSpec(Set<HttpMethod> supportedHttpMethods, CallPath path, 
      RequestSpec requestSpec, ResponseSpec responseSpec) {
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

  public ResponseSpec getResponseSpec() {
    return responseSpec;
  }
  
  public RequestSpec getRequestSpec() {
    return requestSpec;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{path:");
    sb.append(path).append(",supportedHttpMethods:");
    boolean first = true;
    for (HttpMethod method : supportedHttpMethods) {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }
      sb.append(method);
    }
    sb.append(path).append(",requestSpec:");
    sb.append(requestSpec).append(",responseSpec:");
    sb.append(responseSpec).append("}");
    return sb.toString();
  }
}
