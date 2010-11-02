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

import java.lang.reflect.Type;

import com.google.gson.webservice.definition.HeaderMap;
import com.google.gson.webservice.definition.TypedKey;

/**
 * The data associated with a REST Web service response. This includes http response header
 * parameters, and the response body. 
 * 
 * @author inder
 */
public final class RestResponse<R extends RestResource<R>> {
  
  private final HeaderMap headers;
  private final R body;
  private final RestResponseSpec spec;
  
  public static class Builder<RS extends RestResource<RS>> {
    private final HeaderMap.Builder headers;
    private RS body;
    private final RestResponseSpec spec;
    
    public Builder(RestResponseSpec spec) {
      this.spec = spec;
      headers = new HeaderMap.Builder(spec.getHeadersSpec());
    }
    
    public <T> Builder<RS> putHeader(TypedKey<T> paramName, T content) {
      headers.put(paramName.getName(), content, paramName.getClassOfT());
      return this;
    }
    
    public Builder<RS> setBody(RS body) {
      this.body = body;
      return this;
    }

    public RestResponse<RS> build() {
      return new RestResponse<RS>(spec, headers.build(), body);
    }
  }
  
  private RestResponse(RestResponseSpec spec, HeaderMap headers, R body) {
    this.spec = spec;
    this.headers = headers;
    this.body = body;
  }
  
  public RestResponse(HeaderMap responseHeaders, R responseBody, Type responseBodyType) {
    this.spec = new RestResponseSpec(responseHeaders.getSpec(), responseBodyType);
    this.headers = responseHeaders;
    this.body = responseBody;
  }

  public RestResponseSpec getSpec() {
    return spec;
  }

  public HeaderMap getHeaders() {
    return headers;
  }
  
  public R getBody() {
    return body;
  }

  @SuppressWarnings("unchecked")
  public <T> T getHeader(String headerName) {
    return (T) headers.get(headerName);
  }
  
  @Override
  public String toString() {
    return String.format("{headers:%s, body:%s}", headers, body);
  }
}
