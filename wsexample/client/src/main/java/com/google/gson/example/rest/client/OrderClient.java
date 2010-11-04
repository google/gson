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
 */package com.google.gson.example.rest.client;

import com.google.gson.example.model.Cart;
import com.google.gson.example.model.LineItem;
import com.google.gson.example.model.Order;
import com.google.gson.rest.client.RestClient;
import com.google.gson.rest.client.RestClientStub;
import com.google.gson.rest.client.RestServerConfig;
import com.google.gson.rest.definition.Id;
import com.google.gson.webservice.definition.CallPath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A sample client for the rest resource for {@link Order}
 *
 * @author Inderjeet Singh
 */
public class OrderClient {
  public static final CallPath CALL_PATH = new CallPath("/rest/order");
  private final RestClient<Id<Order>, Order> restClient;
  public OrderClient() {
    RestServerConfig serverConfig = new RestServerConfig("http://localhost");
    RestClientStub stub = new RestClientStub(serverConfig, Level.INFO);
    restClient = new RestClient<Id<Order>, Order>(stub, CALL_PATH, Order.class);
  }

  public Order placeOrder(Cart cart) {
    Order order = new Order(cart, cart.getId().getValueAsString());
    return restClient.post(order);
  }

  public static void main(String[] args) {
    OrderClient client = new OrderClient();
    List<LineItem> lineItems = new ArrayList<LineItem>();
    lineItems.add(new LineItem("item1", 2, 1000000L, "USD"));
    Cart cart = new Cart(lineItems, "first last", "4111-1111-1111-1111");
    client.placeOrder(cart);
  }
}
