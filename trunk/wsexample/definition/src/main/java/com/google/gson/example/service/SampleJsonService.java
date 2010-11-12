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
package com.google.gson.example.service;
import com.google.greaze.definition.CallPath;
import com.google.greaze.definition.HttpMethod;
import com.google.greaze.definition.webservice.WebServiceCallSpec;
import com.google.gson.example.model.TypedKeys;

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
