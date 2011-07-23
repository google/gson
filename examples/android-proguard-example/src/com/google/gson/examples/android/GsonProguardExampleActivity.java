/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.examples.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.examples.android.model.Cart;
import com.google.gson.examples.android.model.LineItem;

/**
 * Activity class illustrating how to use proguard with Gson
 *
 * @author Inderjeet Singh
 */
public class GsonProguardExampleActivity extends Activity {
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.main);
    TextView tv = (TextView) findViewById(R.id.tv);
    Gson gson = new Gson();
    Cart cart = buildCart();
    StringBuilder sb = new StringBuilder();
    sb.append("Gson.toJson() example: \n");
    sb.append("  Cart Object: ").append(cart).append("\n");
    sb.append("  Cart JSON: ").append(gson.toJson(cart)).append("\n");
    sb.append("\n\nGson.fromJson() example: \n");
    String json = "{buyer:'Happy Camper',creditCard:'4111-1111-1111-1111',"
      + "lineItems:[{name:'nails',priceInMicros:100000,quantity:100,currencyCode:'USD'}]}";
    sb.append("Cart JSON: ").append(json).append("\n");
    sb.append("Cart Object: ").append(gson.fromJson(json, Cart.class)).append("\n");
    tv.setText(sb.toString());
    tv.invalidate();
  }

  private Cart buildCart() {
    List<LineItem> lineItems = new ArrayList<LineItem>();
    lineItems.add(new LineItem("hammer", 1, 12000000, "USD"));
    return new Cart(lineItems, "Happy Buyer", "4111-1111-1111-1111");
  }
}
