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
package com.google.gson.wsf.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.wsf.WebServiceCallSpec;
import com.google.gson.wsf.WebServiceRequest;
import com.google.gson.wsf.WebServiceResponse;

/**
 * Main class used by clients to access a Gson Web service.
 * 
 * @author inder
 */
public final class WebServiceClient {
  private final WebServiceConfig config;
  private final WebServiceCallSpec callSpec;
  private final Gson gson;

  public WebServiceClient(Gson gson, WebServiceConfig serverConfig, WebServiceCallSpec callSpec) {
    this.gson = gson;
    this.config = serverConfig;
    this.callSpec = callSpec;
  }
  
  private URL getWebServiceUrl() {
    String url = config.getServiceBaseUrl() + callSpec.getPath().get();
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
  
  public WebServiceResponse getResponse(WebServiceRequest request) {
    try {
      HttpURLConnection conn = (HttpURLConnection) getWebServiceUrl().openConnection();
      RequestSender requestSender = new RequestSender(gson);
      requestSender.send(conn, request);
      ResponseReceiver responseReceiver = new ResponseReceiver(gson, callSpec.getResponseSpec());
      return responseReceiver.receive(conn);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
