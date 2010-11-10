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

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.rest.definition.ID;
import com.google.gson.rest.definition.ResourceDepot;
import com.google.gson.rest.definition.RestCallSpec;
import com.google.gson.rest.definition.RestRequest;
import com.google.gson.rest.definition.RestResource;
import com.google.gson.rest.definition.RestResponse;
import com.google.gson.webservice.definition.CallPath;
import com.google.gson.webservice.definition.HeaderMap;
import com.google.gson.webservice.definition.HttpMethod;

/**
 * A client class to access a rest resource
 *
 * @author Inderjeet Singh
 */
public class ResourceDepotClient<I extends ID, R extends RestResource<I, R>>
    implements ResourceDepot<I, R> {
  private final RestClientStub stub;
  private final RestCallSpec callSpec;
  private final Type resourceType;
  private final Gson gson;

  /**
   * @param stub stub containing server info to access the rest client
   * @param callPath relative path to the resource
   * @param resourceType Class for the resource. Such as Cart.class
   */
  public ResourceDepotClient(RestClientStub stub, CallPath callPath, Type resourceType, Gson gson) {
    this(stub, resourceType, generateRestCallSpec(callPath, resourceType), gson);
  }

  protected ResourceDepotClient(RestClientStub stub, Type resourceType, RestCallSpec callSpec, Gson gson) {
    this.stub = stub;
    this.callSpec = callSpec;
    this.resourceType = resourceType;
    this.gson = gson;
  }

  private static <T> RestCallSpec generateRestCallSpec(CallPath callPath, Type resourceType) {
    return new RestCallSpec.Builder(callPath, resourceType).build();
  }

  public R get(I resourceId) {
    HeaderMap requestHeaders =
      new HeaderMap.Builder(callSpec.getRequestSpec().getHeadersSpec()).build();
    RestRequest<I, R> request =
      new RestRequest<I, R>(HttpMethod.GET, requestHeaders, resourceId, null, resourceType);
    RestResponse<I, R> response = stub.getResponse(callSpec, request, gson);
    return response.getBody();
  }

  public R post(R resource) {
    HeaderMap requestHeaders =
      new HeaderMap.Builder(callSpec.getRequestSpec().getHeadersSpec()).build();
    RestRequest<I, R> request =
      new RestRequest<I, R>(HttpMethod.POST, requestHeaders, resource.getId(), resource, resourceType);
    RestResponse<I, R> response = stub.getResponse(callSpec, request, gson);
    return response.getBody();
  }

  public R put(R resource) {
    HeaderMap requestHeaders =
      new HeaderMap.Builder(callSpec.getRequestSpec().getHeadersSpec()).build();
    RestRequest<I, R> request =
      new RestRequest<I, R>(HttpMethod.PUT, requestHeaders, resource.getId(), resource, resourceType);
    RestResponse<I, R> response = stub.getResponse(callSpec, request, gson);
    return response.getBody();
  }

  public void delete(I resourceId) {
    HeaderMap requestHeaders =
      new HeaderMap.Builder(callSpec.getRequestSpec().getHeadersSpec()).build();
    RestRequest<I, R> request =
      new RestRequest<I, R>(HttpMethod.DELETE, requestHeaders, resourceId, null, resourceType);
    stub.getResponse(callSpec, request, gson);
  }
}
