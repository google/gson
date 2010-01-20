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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.webservice.definition.HeaderMap;
import com.google.gson.webservice.definition.HeaderMapSpec;
import com.google.gson.webservice.definition.ResponseBody;
import com.google.gson.webservice.definition.ResponseBodySpec;
import com.google.gson.webservice.definition.ResponseSpec;
import com.google.gson.webservice.definition.WebServiceResponse;

/**
 * Receives a response coming on an {@link HttpURLConnection}.
 * 
 * @author inder
 */
public final class ResponseReceiver {
  private final Gson gson;
  private final ResponseSpec spec;
  private final Logger logger;
  private final Level logLevel;

  public ResponseReceiver(Gson gson, ResponseSpec spec) {
    this(gson, spec, null);
  }
  public ResponseReceiver(Gson gson, ResponseSpec spec, Level logLevel) {
    this.gson = gson;
    this.spec = spec;
    this.logger = logLevel == null ? null : Logger.getLogger(ResponseReceiver.class.getName());
    this.logLevel = logLevel;
  }
  
  public WebServiceResponse receive(HttpURLConnection conn) {
    try {
      HeaderMapSpec paramSpec = spec.getHeadersSpec();
      ResponseBodySpec bodySpec = spec.getBodySpec();
      // read response
      HeaderMap responseParams = readResponseHeaders(conn, paramSpec);
      ResponseBody responseBody = readResponseBody(conn, bodySpec);
      return new WebServiceResponse(responseParams, responseBody);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private HeaderMap readResponseHeaders(HttpURLConnection conn, HeaderMapSpec paramsSpec) {    
    HeaderMap.Builder paramsBuilder = new HeaderMap.Builder(paramsSpec);    
    for (Map.Entry<String, Type> entry : paramsSpec.entrySet()) {
      String paramName = entry.getKey();
      String json = conn.getHeaderField(paramName);
      if (json != null) {
        if (logger != null) {
          logger.log(logLevel, String.format("Response Header: %s:%s\n", paramName, json));
        }
        Type typeOfT = paramsSpec.getTypeFor(paramName);
        Object value = gson.fromJson(json, typeOfT);
        paramsBuilder.put(paramName, value, typeOfT);
      }
    }
    return paramsBuilder.build();
  }

  private ResponseBody readResponseBody(HttpURLConnection conn, ResponseBodySpec bodySpec) 
      throws IOException {
    if (bodySpec.size() == 0) {
      return new ResponseBody.Builder(bodySpec).build();
    }
    String connContentType = conn.getContentType();
    Preconditions.checkArgument(connContentType.contains(bodySpec.getContentType()));
    Reader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    ResponseBody body = gson.fromJson(reader, ResponseBody.class);
    return body;
  }
}
