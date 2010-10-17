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

import java.lang.reflect.Type;

import com.google.gson.webservice.definition.ContentBodySpec;
import com.google.gson.webservice.definition.HeaderMap;
import com.google.gson.webservice.definition.HttpMethod;
import com.google.gson.webservice.definition.RequestBody;
import com.google.gson.webservice.definition.TypedKey;

/**
 * The data associated with a Web service request. This includes HTTP request header parameters 
 * (form and URL parameters), and {@link RequestBody}. 
 * 
 * @author inder
 */
public final class RestRequest<R extends RestResource<R>> {
  private final HttpMethod method;
  private final HeaderMap headers;
  private final R body;
  private final RestRequestSpec spec;
  
  public RestRequest(HttpMethod method, HeaderMap requestHeaders,
      R requestBody, Type resourceType) {
    this.method = method;
    this.body = requestBody;
    this.headers = requestHeaders;
    this.spec = new RestRequestSpec(requestHeaders.getSpec(), resourceType);
  }

  public Id<R> getId() {
    return body.getId();
  }

  public HttpMethod getMethod() {
    return method;
  }

  public RestRequestSpec getSpec() {
    return spec;
  }

  public HttpMethod getHttpMethod() {
    return method;
  }

  public R getBody() {
    return body;
  }

  public HeaderMap getHeaders() {
    return headers;
  }

  public String getContentType() {
    return ContentBodySpec.JSON_CONTENT_TYPE;
  }

  public <T> T getHeader(TypedKey<T> key) {
    return headers.get(key);
  }

  @SuppressWarnings("unchecked")
  public <T> T getHeader(String headerName) {
    return (T) headers.get(headerName);
  }
  
  @Override
  public String toString() {
    return String.format("{method:%s,headers:%s,body:%s}", method, headers, body);
  }
}
