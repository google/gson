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
package com.google.gson.example.model;

import com.google.gson.webservice.definition.TypedKey;

/**
 * Definition of various constants to be used for parameter names of request and response headers,
 * and request and response body.
 *
 * @author inder
 */
public final class TypedKeys {
  public static final class Request {
    public static final TypedKey<String> AUTH_TOKEN =
      new TypedKey<String>("authToken", String.class);
  }

  public static final class RequestBody {
    public static final TypedKey<Cart> CART = new TypedKey<Cart>("cart", Cart.class);
  }

  public static final class ResponseBody {
    public static final TypedKey<Order> ORDER = new TypedKey<Order>("order", Order.class);
  }
}
