// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.gson.example.model;

/**
 * An order
 *
 * @author inder
 */
public class Order {
  public final Cart postedCart;
  public final String orderNumber;

  public Order(Cart postedCart, String orderNumber) {
    this.postedCart = postedCart;
    this.orderNumber = orderNumber;
  }

  public Cart getPostedCart() {
    return postedCart;
  }

  public String getOrderNumber() {
    return orderNumber;
  }

}
