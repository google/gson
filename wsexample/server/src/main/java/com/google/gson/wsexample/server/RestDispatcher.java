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
package com.google.gson.wsexample.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.greaze.definition.CallPath;
import com.google.greaze.definition.rest.IDFactory;
import com.google.greaze.definition.rest.MetaData;
import com.google.greaze.definition.rest.ResourceMap;
import com.google.greaze.definition.rest.RestCallSpec;
import com.google.greaze.definition.rest.RestRequest;
import com.google.greaze.definition.rest.RestResponse;
import com.google.greaze.definition.rest.ValueBasedId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.example.model.Cart;
import com.google.gson.example.model.Order;
import com.google.gson.example.service.ServicePaths;
import com.google.gson.rest.server.Repository;
import com.google.gson.rest.server.RepositoryInMemory;
import com.google.gson.rest.server.ResponseBuilderMap;
import com.google.gson.rest.server.RestRequestReceiver;
import com.google.gson.rest.server.RestResponseBuilder;
import com.google.gson.rest.server.RestResponseSender;

/**
 * A dispatcher for all the REST requests
 *
 * @author Inderjeet Singh
 */
public final class RestDispatcher {
  private static final double CURRENT_VERSION = 1D;
  private final ResourceMap resourceMap;
  private final ResponseBuilderMap responseBuilders;
  private final RestCallSpec cartSpec;
  private final RestCallSpec orderSpec;
  private final Gson gson;

  public RestDispatcher() {
    this.cartSpec = new RestCallSpec.Builder(ServicePaths.CART.getCallPath(), Cart.class)
      .setVersion(CURRENT_VERSION)
      .build();
    this.orderSpec = new RestCallSpec.Builder(ServicePaths.CART.getCallPath(), Cart.class)
      .setVersion(CURRENT_VERSION)
      .build(); 
    this.resourceMap = new ResourceMap.Builder()
      .set(ServicePaths.CART.getCallPath(), cartSpec)
      .set(ServicePaths.ORDER.getCallPath(), orderSpec)
      .build();
    gson = new GsonBuilder()
      .setVersion(CURRENT_VERSION)
      .registerTypeAdapter(ValueBasedId.class, new ValueBasedId.GsonTypeAdapter())
      .registerTypeAdapter(MetaData.class, new MetaData.GsonTypeAdapter())
      .create();
    Repository<ValueBasedId<Cart>, Cart> carts =
      new RepositoryInMemory<ValueBasedId<Cart>, Cart>(ValueBasedId.class, Cart.class);
    Repository<ValueBasedId<Order>, Order> orders =
      new RepositoryInMemory<ValueBasedId<Order>, Order>(ValueBasedId.class, Order.class);
    responseBuilders = new ResponseBuilderMap.Builder()
        .set(cartSpec.getResourceType(), new RestResponseBuilder<ValueBasedId<Cart>, Cart>(carts))
        .set(orderSpec.getResourceType(), new RestResponseBuilder<ValueBasedId<Order>, Order>(orders))
        .build();
  }

  public IDFactory<ValueBasedId<?>> getIDFactory(RestCallSpec callSpec) {
    return new IDFactory<ValueBasedId<?>>(ValueBasedId.class, callSpec.getResourceType());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public RestRequest getRestRequest(Gson gson, RestCallSpec callSpec, CallPath callPath,
      HttpServletRequest request, IDFactory<ValueBasedId<?>> idFactory) {
    RestRequestReceiver requestReceiver = new RestRequestReceiver(gson, callSpec.getRequestSpec());
    return requestReceiver.receive(request, idFactory.createId(callPath.getResourceId()));
  }

  public void service(HttpServletRequest req, HttpServletResponse res, CallPath callPath) {
    RestCallSpec callSpec = resourceMap.get(callPath).createCopy(callPath);
    @SuppressWarnings("rawtypes")
    RestRequestReceiver requestReceiver = new RestRequestReceiver(gson, callSpec.getRequestSpec());
    IDFactory<ValueBasedId<?>> idFactory = getIDFactory(callSpec);
    RestRequest<?, ?> restRequest = getRestRequest(gson, callSpec, callPath, req, idFactory);
    RestResponse.Builder response = new RestResponse.Builder(callSpec.getResponseSpec());
    RestResponseBuilder responseBuilder = responseBuilders.get(callSpec.getResourceType());
    responseBuilder.buildResponse(callSpec, restRequest, response);
    RestResponse restResponse = response.build();
    RestResponseSender responseSender = new RestResponseSender(gson);
    responseSender.send(res, restResponse);
  }
}
