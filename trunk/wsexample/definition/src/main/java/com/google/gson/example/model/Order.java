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

import com.google.gson.rest.definition.Id;
import com.google.gson.rest.definition.RestResource;

/**
 * An order
 *
 * @author inder
 */
public class Order implements RestResource<Id<Order>, Order> {
  public final Cart postedCart;
  public final String orderNumber;
  private Id<Order> id;
 
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

  @Override
  public Id<Order> getId() {
    return id;
  }

  @Override
  public void setId(Id<Order> id) {
    this.id = id;
  }

  @Override
  public boolean hasId() {
    return Id.isValid(id);
  }
}
