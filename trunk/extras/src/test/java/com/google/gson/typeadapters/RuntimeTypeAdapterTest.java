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

package com.google.gson.typeadapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;

public final class RuntimeTypeAdapterTest extends TestCase {

  public void testRuntimeTypeAdapter() {
    RuntimeTypeAdapter<BillingInstrument> rta = RuntimeTypeAdapter.create(BillingInstrument.class)
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, rta)
        .create();

    CreditCard original = new CreditCard("Jesse", 234);
    assertEquals("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}",
        gson.toJson(original, BillingInstrument.class));
    BillingInstrument deserialized = gson.fromJson(
        "{type:'CreditCard',cvv:234,ownerName:'Jesse'}", BillingInstrument.class);
    assertEquals("Jesse", deserialized.ownerName);
    assertTrue(deserialized instanceof CreditCard);
  }

  public void testRuntimeTypeIsBaseType() {
    RuntimeTypeAdapter<BillingInstrument> rta = RuntimeTypeAdapter.create(BillingInstrument.class)
        .registerSubtype(BillingInstrument.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, rta)
        .create();

    BillingInstrument original = new BillingInstrument("Jesse");
    assertEquals("{\"type\":\"BillingInstrument\",\"ownerName\":\"Jesse\"}",
        gson.toJson(original, BillingInstrument.class));
    BillingInstrument deserialized = gson.fromJson(
        "{type:'CreditCard',ownerName:'Jesse'}", BillingInstrument.class);
    assertEquals("Jesse", deserialized.ownerName);
  }

  static class CreditCard extends BillingInstrument {
    int cvv;

    CreditCard(String ownerName, int cvv) {
      super(ownerName);
      this.cvv = cvv;
    }
  }

  static class BillingInstrument {
    private final String ownerName;

    BillingInstrument(String ownerName) {
      this.ownerName = ownerName;
    }
  }
}
