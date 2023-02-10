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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import org.junit.Test;

public final class RuntimeTypeAdapterFactoryTest {

  @Test
  public void testRuntimeTypeAdapter() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class)
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(rta)
        .create();

    CreditCard original = new CreditCard("Jesse", 234);
    assertEquals("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}",
        gson.toJson(original, BillingInstrument.class));
    BillingInstrument deserialized = gson.fromJson(
        "{type:'CreditCard',cvv:234,ownerName:'Jesse'}", BillingInstrument.class);
    assertEquals("Jesse", deserialized.ownerName);
    assertTrue(deserialized instanceof CreditCard);
  }

  @Test
  public void testRuntimeTypeAdapterRecognizeSubtypes() {
    // We don't have an explicit factory for CreditCard.class, but we do have one for
    // BillingInstrument.class that has recognizeSubtypes(). So it should recognize CreditCard, and
    // when we call gson.toJson(original) below, without an explicit type, it should be invoked.
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class)
        .recognizeSubtypes()
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(rta)
        .create();

    CreditCard original = new CreditCard("Jesse", 234);
    assertEquals("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}",
        gson.toJson(original));
    BillingInstrument deserialized = gson.fromJson(
        "{type:'CreditCard',cvv:234,ownerName:'Jesse'}", BillingInstrument.class);
    assertEquals("Jesse", deserialized.ownerName);
    assertTrue(deserialized instanceof CreditCard);
  }

  @Test
  public void testRuntimeTypeIsBaseType() {
    TypeAdapterFactory rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class)
        .registerSubtype(BillingInstrument.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(rta)
        .create();

    BillingInstrument original = new BillingInstrument("Jesse");
    assertEquals("{\"type\":\"BillingInstrument\",\"ownerName\":\"Jesse\"}",
        gson.toJson(original, BillingInstrument.class));
    BillingInstrument deserialized = gson.fromJson(
        "{type:'BillingInstrument',ownerName:'Jesse'}", BillingInstrument.class);
    assertEquals("Jesse", deserialized.ownerName);
  }

  @Test
  public void testNullBaseType() {
    try {
      RuntimeTypeAdapterFactory.of(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testNullTypeFieldName() {
    try {
      RuntimeTypeAdapterFactory.of(BillingInstrument.class, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testNullSubtype() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    try {
      rta.registerSubtype(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testNullLabel() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    try {
      rta.registerSubtype(CreditCard.class, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testDuplicateSubtype() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    rta.registerSubtype(CreditCard.class, "CC");
    try {
      rta.registerSubtype(CreditCard.class, "Visa");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testDuplicateLabel() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    rta.registerSubtype(CreditCard.class, "CC");
    try {
      rta.registerSubtype(BankTransfer.class, "CC");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testDeserializeMissingTypeField() {
    TypeAdapterFactory billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class)
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(billingAdapter)
        .create();
    try {
      gson.fromJson("{ownerName:'Jesse'}", BillingInstrument.class);
      fail();
    } catch (JsonParseException expected) {
    }
  }

  @Test
  public void testDeserializeMissingSubtype() {
    TypeAdapterFactory billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class)
        .registerSubtype(BankTransfer.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(billingAdapter)
        .create();
    try {
      gson.fromJson("{type:'CreditCard',ownerName:'Jesse'}", BillingInstrument.class);
      fail();
    } catch (JsonParseException expected) {
    }
  }

  @Test
  public void testSerializeMissingSubtype() {
    TypeAdapterFactory billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class)
        .registerSubtype(BankTransfer.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(billingAdapter)
        .create();
    try {
      gson.toJson(new CreditCard("Jesse", 456), BillingInstrument.class);
      fail();
    } catch (JsonParseException expected) {
    }
  }

  @Test
  public void testSerializeCollidingTypeFieldName() {
    TypeAdapterFactory billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class, "cvv")
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(billingAdapter)
        .create();
    try {
      gson.toJson(new CreditCard("Jesse", 456), BillingInstrument.class);
      fail();
    } catch (JsonParseException expected) {
    }
  }

  @Test
  public void testSerializeWrappedNullValue() {
    TypeAdapterFactory billingAdapter = RuntimeTypeAdapterFactory.of(BillingInstrument.class)
        .registerSubtype(CreditCard.class)
        .registerSubtype(BankTransfer.class);    
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(billingAdapter)
        .create();    
    String serialized = gson.toJson(new BillingInstrumentWrapper(null), BillingInstrumentWrapper.class);
    BillingInstrumentWrapper deserialized = gson.fromJson(serialized, BillingInstrumentWrapper.class);
    assertNull(deserialized.instrument);
  }

  static class BillingInstrumentWrapper {
    BillingInstrument instrument;
    BillingInstrumentWrapper(BillingInstrument instrument) {
      this.instrument = instrument;
    }
  }

  static class BillingInstrument {
    private final String ownerName;
    BillingInstrument(String ownerName) {
      this.ownerName = ownerName;
    }
  }

  static class CreditCard extends BillingInstrument {
    int cvv;
    CreditCard(String ownerName, int cvv) {
      super(ownerName);
      this.cvv = cvv;
    }
  }

  static class BankTransfer extends BillingInstrument {
    int bankAccount;
    BankTransfer(String ownerName, int bankAccount) {
      super(ownerName);
      this.bankAccount = bankAccount;
    }
  }
}
