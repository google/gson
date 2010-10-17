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
package com.google.gson.wsf.server.rest;

import com.google.gson.webservice.definition.HttpMethod;
import com.google.gson.webservice.definition.rest.Id;
import com.google.gson.webservice.definition.rest.RestCallSpec;
import com.google.gson.webservice.definition.rest.RestRequest;
import com.google.gson.webservice.definition.rest.RestResource;
import com.google.gson.webservice.definition.rest.RestResponse;

public abstract class RestResponseBuilder<R extends RestResource<R>> {
  protected final Repository<R> resources;

  public RestResponseBuilder(Repository<R> resources) {
    this.resources = resources;
  }

  public void buildResponse(RestCallSpec callSpec, RestRequest<R> request,
      RestResponse.Builder<R> responseBuilder) {
    HttpMethod method = request.getMethod();
    R responseBody = null;
    switch (method) {
      case GET:
        responseBody = get(request.getId());
        break;
      case POST:
        responseBody = post(request.getBody());
        break;
      case DELETE:
        delete(request.getId());
        break;
      case PUT:
      default:
        throw new IllegalStateException("Unexpected method: " + method);
    }
    responseBuilder.setBody(responseBody);
  }

  public R get(Id<R> resourceId) {
    return resources.get(resourceId);
  }

  public R post(R resource) {
    return resources.put(resource);
  }

  public void delete(Id<R> resourceId) {
    resources.delete(resourceId);
  }

  public R put(R resource) {
    return resources.put(resource);
  }
}
