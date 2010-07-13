// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.gson.example.model;

import java.util.List;

/**
 * A cart that can be posted to the server
 * 
 * @author inder
 */
public class Cart {
  private final List<LineItem> lineItems;
  private final String buyerName;
  private final String creditCard;

  public Cart(List<LineItem> lineItems, String buyerName, String creditCard) {
    this.lineItems = lineItems;
    this.buyerName = buyerName;
    this.creditCard = creditCard;
  }

  public List<LineItem> getLineItems() {
    return lineItems;
  }

  public String getBuyerName() {
    return buyerName;
  }

  public String getCreditCard() {
    return creditCard;
  }
}
