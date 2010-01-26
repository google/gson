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
package com.google.gson.webservice.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.webservice.definition.HeaderMap;
import com.google.gson.webservice.definition.HeaderMapSpec;
import com.google.gson.webservice.definition.RequestBody;
import com.google.gson.webservice.definition.WebServiceRequest;
import com.google.gson.webservice.definition.WebServiceSystemException;

/**
 * Class to send Web service requests on a {@link HttpURLConnection}.
 * 
 * @author inder
 */
public final class RequestSender {
  private final Gson gson;
  private final Logger logger;
  private final Level logLevel;

  public RequestSender(Gson gson) {
    this(gson, null);
  }

  public RequestSender(Gson gson, Level logLevel) {
    this.gson = gson;
    logger = logLevel == null ? null : Logger.getLogger(RequestSender.class.getName());
    this.logLevel = logLevel;
  }
  
  public void send(HttpURLConnection conn, WebServiceRequest request) {    
    try {
      conn.setRequestMethod(request.getHttpMethod().toString());
      setHeader(conn, "Content-Type", request.getContentType(), true);
      
      // Assume conservatively that the response will need to be read.
      // This is done here instead of in the response receiver because this property must be set
      // before sending any data on the connection.
      conn.setDoInput(true);
      
      RequestBody requestBody = request.getBody();
      String requestBodyContents = "";
      // Android Java VM ignore Content-Length if setDoOutput is not set
      conn.setDoOutput(true);    
      if (requestBody.getSpec().size() > 0) {
        requestBodyContents = gson.toJson(requestBody);
      }
      String contentLength = String.valueOf(requestBodyContents.length());
      setHeader(conn, "Content-Length", contentLength, true);
      addRequestParams(conn, request.getHeaders());
      if (requestBodyContents != null) {
        Streams.copy(requestBodyContents, conn.getOutputStream(), false);
      }
      
      // Initiate the sending of the request.
      conn.connect();
    } catch (IOException e) {
      throw new WebServiceSystemException(e);
    }
  }

  private void addRequestParams(HttpURLConnection conn, HeaderMap requestParams) {
    HeaderMapSpec spec = requestParams.getSpec();
    for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
      String paramName = entry.getKey();
      Type type = spec.getTypeFor(paramName);
      Object value = entry.getValue();
      String json = gson.toJson(value, type);
      setHeader(conn, paramName, json, false);
    }
  }

  private void setHeader(HttpURLConnection conn, String name, String value, boolean overwrite) {
    if (logger != null) {
      logger.log(logLevel, String.format("Request param: %s:%s", name, value));
    }
    if (overwrite) {
      conn.setRequestProperty(name, value);
    } else {
      conn.addRequestProperty(name, value);
    }
  }  
}
