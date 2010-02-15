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
  
  public static class Builder {
    private final HeaderMap.Builder headers;
    private final ResponseBody.Builder body;
    private final ResponseSpec spec;
    
    public Builder(ResponseSpec spec) {
      this.spec = spec;
      headers = new HeaderMap.Builder(spec.getHeadersSpec());
      body = new ResponseBody.Builder(spec.getBodySpec());      
    }
    
    public Builder putHeader(String paramName, Object content) {
      headers.put(paramName, content);
      return this;
    }
    
    public Builder putHeader(String paramName, Object content, Type typeOfContent) {
      headers.put(paramName, content, typeOfContent);
      return this;
    }
    
    public <T> Builder putBody(TypedKey<T> paramName, T content) {
      return putBody(paramName.getName(), content);
    }

    public Builder putBody(String paramName, Object content) {
      body.put(paramName, content);
      return this;
    }

    public Builder put(String paramName, Object content, Type typeOfContent) {
      body.put(paramName, content, typeOfContent);
      return this;
    }
    
    public WebServiceResponse build() {
      return new WebServiceResponse(spec, headers.build(), body.build());
    }
  }
  
  private WebServiceResponse(ResponseSpec spec, HeaderMap headers, ResponseBody body) {
    this.spec = spec;
    this.headers = headers;
    this.body = body;
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
