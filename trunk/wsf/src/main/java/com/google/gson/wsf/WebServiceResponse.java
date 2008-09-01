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
 * The data associated with a Web service response. This includes http response header parameters, 
 * and {@link ResponseBody}. 
 * 
 * @author inder
 */
public final class WebServiceResponse {
  
  private final HeaderMap headers;
  private final ResponseBody body;
  private final ResponseSpec spec;
  
  public WebServiceResponse(ResponseSpec spec) {
    this.spec = spec;
    headers = new HeaderMap(spec.getHeadersSpec());
    body = new ResponseBody(spec.getBodySpec());
  }
  
  public WebServiceResponse(HeaderMap responseHeaders, ResponseBody responseBody) {
    this.spec = new ResponseSpec(responseHeaders.getSpec(), responseBody.getSpec());
    this.headers = responseHeaders;
    this.body = responseBody;
  }

  public ResponseSpec getSpec() {
    return spec;
  }

  public HeaderMap getHeaders() {
    return headers;
  }
  
  public ResponseBody getBody() {
    return body;
  }

  @SuppressWarnings("unchecked")
  public <T> T getHeader(String headerName) {
    return (T) headers.get(headerName);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{headers:");
    sb.append(headers);
    sb.append(",body:").append(body);
    sb.append("}");
    return sb.toString();
  }
}
