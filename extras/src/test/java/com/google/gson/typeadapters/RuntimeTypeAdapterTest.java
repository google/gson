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
    RuntimeTypeAdapter<BillingInstrument> rta = RuntimeTypeAdapter.create(BillingInstrument.class);
    rta.registerSubtype(CreditCard.class);

    CreditCard cc = new CreditCard("Jesse", 234);
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(BillingInstrument.class, rta)
        .create();
    String ccJson = gson.toJson(cc, BillingInstrument.class);
    assertEquals("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}", ccJson);

    BillingInstrument creditCard = gson.fromJson(
        "{type:'CreditCard',cvv:234,ownerName:'Jesse'}", BillingInstrument.class);
    assertTrue(creditCard instanceof CreditCard);
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
