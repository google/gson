// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.gson.wsexample.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.example.model.Cart;
import com.google.gson.example.model.Order;
import com.google.gson.example.model.TypedKeys;
import com.google.gson.example.service.SampleJsonService;
import com.google.gson.webservice.definition.HeaderMap;
import com.google.gson.webservice.definition.RequestBody;
import com.google.gson.webservice.definition.RequestSpec;
import com.google.gson.webservice.definition.ResponseBody;
import com.google.gson.webservice.definition.ResponseSpec;
import com.google.gson.webservice.definition.WebServiceCallSpec;
import com.google.gson.webservice.definition.WebServiceRequest;
import com.google.gson.webservice.definition.WebServiceResponse;
import com.google.gson.webservice.typeadapters.RequestBodyGsonConverter;
import com.google.gson.webservice.typeadapters.ResponseBodyGsonConverter;
import com.google.gson.wsf.server.RequestReceiver;
import com.google.gson.wsf.server.ResponseSender;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An example servlet that receives JSON web-service requests
 *
 * @author inder
 */
public class MainServlet extends HttpServlet {
  @Override
  public void service(HttpServletRequest req, HttpServletResponse res) {
    // construct specs
    WebServiceCallSpec spec = SampleJsonService.PLACE_ORDER;
    RequestSpec requestSpec = spec.getRequestSpec();
    ResponseSpec responseSpec = spec.getResponseSpec();
    // construct gson instance
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(RequestBody.class,
            new RequestBodyGsonConverter(requestSpec.getBodySpec()))
        .registerTypeAdapter(ResponseBody.class, 
            new ResponseBodyGsonConverter(responseSpec.getBodySpec()))
        .create();
    RequestReceiver requestReceiver = new RequestReceiver(gson, requestSpec);
    WebServiceRequest webServiceRequest = requestReceiver.receive(req);
    RequestBody requestBody = webServiceRequest.getBody();
    Cart cart = requestBody.get(TypedKeys.RequestBody.CART);
    String authToken = webServiceRequest.getHeader(TypedKeys.Request.AUTH_TOKEN);
    Order order = placeOrder(cart, authToken);

    HeaderMap responseHeaders = new HeaderMap.Builder(responseSpec.getHeadersSpec()).build();
    ResponseBody responseBody = new ResponseBody.Builder(responseSpec.getBodySpec())
        .put(TypedKeys.ResponseBody.ORDER, order)
        .build();
    WebServiceResponse response = new WebServiceResponse(responseHeaders, responseBody);
    ResponseSender responseSender = new ResponseSender(gson);
    responseSender.send(res, response);
  }
  
  private Order placeOrder(Cart cart, String authToken) {
    return new Order(cart, "Order123");
  }
}
