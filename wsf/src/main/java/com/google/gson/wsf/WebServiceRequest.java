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

/**
 * The data associated with a Web service request. This includes HTTP request header parameters 
 * (form and URL parameters), and {@link RequestBody}. 
 * 
 * @author inder
 */
public final class WebServiceRequest {
  private final HttpMethod method;
  private final HeaderMap headers;
  private final RequestBody body;
  private final RequestSpec spec;
  
  public WebServiceRequest(HttpMethod method, HeaderMap requestHeaders, RequestBody requestBody) {
    this.method = method;
    this.body = requestBody;
    this.headers = requestHeaders;
    this.spec = new RequestSpec(requestHeaders.getSpec(), requestBody.getSpec());
  }

  public HttpMethod getMethod() {
    return method;
  }

  public RequestSpec getSpec() {
    return spec;
  }

  public HttpMethod getHttpMethod() {
    return method;
  }

  public RequestBody getBody() {
    return body;
  }

  public HeaderMap getHeaders() {
    return headers;
  }

  public String getContentType() {
    return ContentBodySpec.JSON_CONTENT_TYPE;
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getHeader(String headerName) {
    return (T) headers.get(headerName);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    sb.append(method).append(",");
    sb.append(",headers:").append(headers);
    sb.append(",body:").append(body);
    sb.append("}");
    return sb.toString();
  }
}
