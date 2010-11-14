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

import com.google.greaze.definition.CallPath;
import com.google.greaze.definition.rest.ValueBasedId;
import com.google.greaze.rest.client.ResourceDepotClient;
import com.google.greaze.rest.client.RestClientStub;
import com.google.greaze.rest.query.client.ResourceQueryClient;
import com.google.greaze.webservice.client.ServerConfig;
import com.google.greaze.webservice.client.WebServiceClient;
import com.google.gson.Gson;
import com.google.gson.example.model.Cart;
import com.google.gson.example.model.LineItem;
import com.google.gson.example.model.Order;
import com.google.gson.example.model.QueryOrdersByItemName;

import java.util.ArrayList;
import java.util.List;

/**
 * A sample client for the rest resource for {@link Order}
 *
 * @author Inderjeet Singh
 */
public class OrderClient {
  public static final CallPath CALL_PATH = new CallPath("/rest/order");
  private final ResourceDepotClient<ValueBasedId<Order>, Order> restClient;
  private final ResourceQueryClient<
      ValueBasedId<Order>, Order, QueryOrdersByItemName> queryClient;
  public OrderClient() {
    ServerConfig serverConfig = new ServerConfig("http://localhost");
    Gson gson = new Gson();

    restClient = new ResourceDepotClient<ValueBasedId<Order>, Order>(
        new RestClientStub(serverConfig), CALL_PATH, Order.class, new Gson());
    ServerConfig wsServerConfig = new ServerConfig("http://localhost");
    queryClient = new ResourceQueryClient<ValueBasedId<Order>, Order, QueryOrdersByItemName>(
        new WebServiceClient(wsServerConfig), CALL_PATH, gson); 
  }

  public Order placeOrder(Cart cart) {
    Order order = new Order(cart, cart.getId().getValueAsString());
    return restClient.post(order);
  }

  private List<Order> query(String itemName) {
    return queryClient.query(new QueryOrdersByItemName(itemName));
  }

  public static void main(String[] args) {
    OrderClient client = new OrderClient();
    List<LineItem> lineItems = new ArrayList<LineItem>();
    String itemName = "item1";
    lineItems.add(new LineItem(itemName, 2, 1000000L, "USD"));
    Cart cart = new Cart(lineItems, "first last", "4111-1111-1111-1111");
    Order order = client.placeOrder(cart);
    System.out.println("Placed order: " + order);
    List<Order> queriedOrder = client.query(itemName);
    System.out.println("Queried order by item name ( " + itemName + "): " + order);
  }
}
