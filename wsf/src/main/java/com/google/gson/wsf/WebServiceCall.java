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
 * The data associated with a Web service call. This includes http request header parameters (form 
 * and URL parameters), {@link RequestBody}, response header parameters, and {@link ResponseBody}. 
 * 
 * @author inder
 */
public final class WebServiceCall {
  
  private final WebServiceCallSpec callSpec;
  private final WebServiceRequest request;
  private final WebServiceResponse response;
  
  public WebServiceCall(WebServiceCallSpec callSpec, WebServiceRequest request, 
      WebServiceResponse response) {
    this.callSpec = callSpec;
    this.request = request;
    this.response = response;
  }

  public WebServiceCallSpec getSpec() {
    return callSpec;
  }
  
  public WebServiceRequest getRequest() {
    return request;
  }

  public WebServiceResponse getResponse() {
    return response;
  }
}
