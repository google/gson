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
