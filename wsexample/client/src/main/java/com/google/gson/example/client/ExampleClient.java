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
package com.google.gson.example.client;

import com.google.greaze.definition.HeaderMap;
import com.google.greaze.definition.HttpMethod;
import com.google.greaze.definition.webservice.RequestBody;
import com.google.greaze.definition.webservice.WebServiceCallSpec;
import com.google.greaze.definition.webservice.WebServiceRequest;
import com.google.greaze.definition.webservice.WebServiceResponse;
import com.google.greaze.webservice.client.ServerConfig;
import com.google.greaze.webservice.client.WebServiceClient;
import com.google.gson.example.model.Cart;
import com.google.gson.example.model.LineItem;
import com.google.gson.example.model.Order;
import com.google.gson.example.model.TypedKeys;
import com.google.gson.example.service.SampleJsonService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ExampleClient {

  private final WebServiceClient wsClient;
  public ExampleClient() {
    ServerConfig serverConfig = new ServerConfig("http://localhost");
	wsClient = new WebServiceClient(serverConfig, Level.INFO); 
  }

  public Order placeOrder(Cart cart, String authToken) {
    WebServiceCallSpec spec = SampleJsonService.PLACE_ORDER;
	HeaderMap requestHeaders = new HeaderMap.Builder(spec.getRequestSpec().getHeadersSpec())
	    .put(TypedKeys.Request.AUTH_TOKEN, authToken)
	    .build();
	RequestBody requestBody = new RequestBody.Builder(spec.getRequestSpec().getBodySpec())
	    .put(TypedKeys.RequestBody.CART, cart)
	    .build();
	WebServiceRequest request = new WebServiceRequest(HttpMethod.POST, requestHeaders, requestBody);
	WebServiceResponse response = wsClient.getResponse(spec, request);
	return response.getBody().get(TypedKeys.ResponseBody.ORDER);
  }

  public static void main(String[] args) {
    ExampleClient client = new ExampleClient();
    List<LineItem> lineItems = new ArrayList<LineItem>();
    lineItems.add(new LineItem("item1", 2, 1000000L, "USD"));
	Cart cart = new Cart(lineItems, "first last", "4111-1111-1111-1111");
	String authToken = "authToken";
	client.placeOrder(cart, authToken );
  }
}
