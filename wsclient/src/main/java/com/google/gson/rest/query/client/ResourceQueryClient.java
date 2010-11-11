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
package com.google.gson.rest.query.client;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.rest.definition.ID;
import com.google.gson.rest.definition.RestResource;
import com.google.gson.rest.query.ResourceQuery;
import com.google.gson.rest.query.TypedKeysQuery;
import com.google.gson.webservice.client.WebServiceClient;
import com.google.gson.webservice.definition.CallPath;
import com.google.gson.webservice.definition.HeaderMap;
import com.google.gson.webservice.definition.HttpMethod;
import com.google.gson.webservice.definition.procedural.RequestBody;
import com.google.gson.webservice.definition.procedural.ResponseBody;
import com.google.gson.webservice.definition.procedural.WebServiceCallSpec;
import com.google.gson.webservice.definition.procedural.WebServiceRequest;
import com.google.gson.webservice.definition.procedural.WebServiceResponse;

/**
 * A client to invoke {@link ResourceQuery}s associated with a REST resource
 * 
 * @author Inderjeet Singh
 *
 * @param <I> ID type of the REST resource
 * @param <R> type of the REST resource
 * @param <Q> Query parameters
 */
public class ResourceQueryClient<I extends ID, R extends RestResource<I, R>, Q>
    implements ResourceQuery<I, R, Q> {

  private final WebServiceClient stub;
  private final WebServiceCallSpec callSpec;
  private final Gson gson;

  /**
   * @param stub stub containing server info to access the rest client
   * @param callPath relative path to the resource
   */
  public ResourceQueryClient(WebServiceClient stub, CallPath callPath, Gson gson) {
    this(stub, generateCallSpec(callPath), gson);
  }

  protected ResourceQueryClient(WebServiceClient stub, WebServiceCallSpec callSpec, Gson gson) {
    this.stub = stub;
    this.callSpec = callSpec;
    this.gson = gson;
  }

  private static <T> WebServiceCallSpec generateCallSpec(CallPath callPath) {
    return new WebServiceCallSpec.Builder(callPath)
        .supportsHttpMethod(HttpMethod.GET)
        .addResponseBodyParam(TypedKeysQuery.RESOURCE_LIST)
        .build();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<R> query(Q query) {
    HeaderMap requestHeaders =
      new HeaderMap.Builder(callSpec.getRequestSpec().getHeadersSpec()).build();
    RequestBody requestBody =
      new RequestBody.Builder(callSpec.getRequestSpec().getBodySpec())
      .build();
    WebServiceRequest request = new WebServiceRequest(HttpMethod.GET, requestHeaders, requestBody);
    WebServiceResponse response = stub.getResponse(callSpec, request, gson);
    ResponseBody body = response.getBody();
    List list = body.get(TypedKeysQuery.RESOURCE_LIST);
    return list;
  }
}
