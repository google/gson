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
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.wsf.HeaderMap;
import com.google.gson.wsf.HeaderMapSpec;
import com.google.gson.wsf.RequestBody;
import com.google.gson.wsf.WebServiceRequest;

/**
 * Class to send Web service requests on a {@link HttpURLConnection}.
 * 
 * @author inder
 */
public final class RequestSender {
  private final Gson gson;

  public RequestSender(Gson gson) {
    this.gson = gson;
  }
  
  public void send(HttpURLConnection conn, WebServiceRequest request) {    
    try {
      conn.setRequestMethod(request.getHttpMethod().toString());
      conn.setRequestProperty("Content-Type", request.getContentType());
      addRequestParams(conn, request.getHeaders());
      RequestBody requestBody = request.getBody();
      if (requestBody.getSpec().size() > 0) {
        conn.setDoOutput(true);    
        addRequestBody(conn, requestBody);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addRequestParams(HttpURLConnection conn, HeaderMap requestParams) {
    HeaderMapSpec spec = requestParams.getSpec();
    for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
      String paramName = entry.getKey();
      Type type = spec.getTypeFor(paramName);
      Object value = entry.getValue();
      String json = gson.toJson(value, type);
      conn.addRequestProperty(paramName, json);
    }
  }
  
  private void addRequestBody(HttpURLConnection conn, RequestBody body) throws IOException {
    Writer writer = new PrintWriter(conn.getOutputStream());
    gson.toJson(body, writer);
    writer.close();
  }
}
