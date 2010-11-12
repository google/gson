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
package com.google.gson.rest.client;

import com.google.greaze.definition.WebServiceSystemException;
import com.google.greaze.definition.rest.ID;
import com.google.greaze.definition.rest.RestCallSpec;
import com.google.greaze.definition.rest.RestRequest;
import com.google.greaze.definition.rest.RestResource;
import com.google.greaze.definition.rest.RestResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.webservice.client.ServerConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A stub to access the rest service
 * 
 * @author inder
 */
public class RestClientStub {
  private final ServerConfig config;
  private final Logger logger;
  private final Level logLevel;

  public RestClientStub(ServerConfig serverConfig) {
    this(serverConfig, null);
  }

  public RestClientStub(ServerConfig serverConfig, Level logLevel) {
    this.config = serverConfig;
    this.logger = logLevel == null ? null : Logger.getLogger(RestClientStub.class.getName());
    this.logLevel = logLevel;
  }
  
  private <I extends ID> URL getWebServiceUrl(
      RestCallSpec callSpec, ID id) {
    double version = callSpec.getVersion();
    StringBuilder url = new StringBuilder(config.getServiceBaseUrl());
    if (version != -1D) {
      url.append('/').append(version);
    }
    url.append(callSpec.getPath().get());
    if (id != null) {
      url.append('/').append(id.getValue());
    }
    try {
      return new URL(url.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <I extends ID, R extends RestResource<I, R>> RestResponse<I, R> getResponse(
      RestCallSpec callSpec, RestRequest<I, R> request) {
    Gson gson = new GsonBuilder().setVersion(callSpec.getVersion()).create();
    return getResponse(callSpec, request, gson);
  }

  public <I extends ID, R extends RestResource<I, R>> RestResponse<I, R> getResponse(
      RestCallSpec callSpec, RestRequest<I, R> request, Gson gson) {
    HttpURLConnection conn = null;
    try {
      URL webServiceUrl = getWebServiceUrl(callSpec, request.getId());
      conn = (HttpURLConnection) webServiceUrl.openConnection();
      return getResponse(callSpec, request, gson, conn);
    } catch (IOException e) {
      throw new WebServiceSystemException(e);
    } finally {
      closeIgnoringErrors(conn);
    }
  }

  /**
   * Use this method if you want to mange the HTTP Connection yourself. This is useful when you
   * want to use HTTP pipelining.
   */
  public <I extends ID, R extends RestResource<I, R>> RestResponse<I, R> getResponse(
      RestCallSpec callSpec, RestRequest<I, R> request, Gson gson,
      HttpURLConnection conn) {
    try {
      if (logger != null) {
        URL webServiceUrl = getWebServiceUrl(callSpec, request.getId());
        logger.log(logLevel, "Opening connection to " + webServiceUrl);
      }
      RestRequestSender requestSender = new RestRequestSender(gson, logLevel);
      requestSender.send(conn, request);
      RestResponseReceiver<I, R> responseReceiver =
        new RestResponseReceiver<I, R>(gson, callSpec.getResponseSpec(), logLevel);
      return responseReceiver.receive(conn);
    } catch (IllegalArgumentException e) {
      throw new WebServiceSystemException(e);
    }
  }
  
  private static void closeIgnoringErrors(HttpURLConnection conn) {
    if (conn != null) {
      conn.disconnect();
    }
  }

  @Override
  public String toString() {
    return String.format("config:%s", config);
  }
}