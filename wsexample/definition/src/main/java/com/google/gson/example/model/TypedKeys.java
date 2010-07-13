// Copyright 2010 Google Inc. All Rights Reserved.

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
