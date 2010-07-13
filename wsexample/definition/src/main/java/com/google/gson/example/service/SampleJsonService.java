// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.gson.example.service;
import com.google.gson.example.model.TypedKeys;
import com.google.gson.webservice.definition.CallPath;
import com.google.gson.webservice.definition.HttpMethod;
import com.google.gson.webservice.definition.WebServiceCallSpec;

/**
 * An example of a web-service definition
 *
 * @author inder
 */
public class SampleJsonService {

  public static final WebServiceCallSpec PLACE_ORDER = new WebServiceCallSpec.Builder(
      new CallPath("/placeOrder"))
      .supportsHttpMethod(HttpMethod.POST)
      .addRequestParam(TypedKeys.Request.AUTH_TOKEN)
      .addRequestBodyParam(TypedKeys.RequestBody.CART)
      .addResponseBodyParam(TypedKeys.ResponseBody.ORDER)
      .build();
}
