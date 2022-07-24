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
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import junit.framework.TestCase;

public final class RuntimeTypeAdapterFactoryTest extends TestCase {

  public void testRuntimeTypeAdapter() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class)
        .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(rta)
        .create();

    CreditCard original = new CreditCard("Jesse", 234);
    assertEquals("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}",
        //do not give the explicit typeOfSrc, because if this would be in a list
        //or an attribute, there would also be no hint. See #712
        gson.toJson(original));
    BillingInstrument deserialized = gson.fromJson(
        "{type:'CreditCard',cvv:234,ownerName:'Jesse'}", BillingInstrument.class);
    assertEquals("Jesse", deserialized.ownerName);
    assertTrue(deserialized instanceof CreditCard);
  }

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

  public void testNullBaseType() {
    try {
      RuntimeTypeAdapterFactory.of(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testNullTypeFieldName() {
    try {
      RuntimeTypeAdapterFactory.of(BillingInstrument.class, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testNullSubtype() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    try {
      rta.registerSubtype(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testNullLabel() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta = RuntimeTypeAdapterFactory.of(
        BillingInstrument.class);
    try {
      rta.registerSubtype(CreditCard.class, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

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

  public void testDeserializeReaderSettings() throws IOException {
    // Directly use TypeAdapter to avoid default lenientness of Gson
    TypeAdapter<DummyBaseClass> adapter = RuntimeTypeAdapterFactory.of(DummyBaseClass.class, "type", true)
        .registerSubtype(DoubleContainer.class, "d")
        .create(new Gson(), TypeToken.get(DummyBaseClass.class));

    String json = "{\"type\":\"d\",\"d\":\"NaN\"}";
    try {
      adapter.read(new JsonReader(new StringReader(json)));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("JSON forbids NaN and infinities: NaN", e.getMessage());
    }

    JsonReader lenientReader = new JsonReader(new StringReader(json));
    lenientReader.setLenient(true);
    DoubleContainer deserialized = (DoubleContainer) adapter.read(lenientReader);
    assertEquals((Double) Double.NaN, deserialized.d);
  }

  public void testSerializeWriterSettings() throws IOException {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    // Directly use TypeAdapter to avoid default lenientness of Gson
    TypeAdapter<DummyBaseClass> adapter = RuntimeTypeAdapterFactory.of(DummyBaseClass.class, "type")
        .registerSubtype(DoubleContainer.class, "d")
        .create(gson, TypeToken.get(DummyBaseClass.class));

    String json = adapter.toJson(new DoubleContainer(1.0));
    assertEquals("{\"type\":\"d\",\"d\":1.0,\"d2\":null}", json);

    try {
      adapter.toJson(new DoubleContainer(Double.NaN));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("JSON forbids NaN and infinities: NaN", e.getMessage());
    }

    StringWriter writer = new StringWriter();
    JsonWriter customWriter = new JsonWriter(writer);
    customWriter.setLenient(true);
    customWriter.setSerializeNulls(false);

    adapter.write(customWriter, new DoubleContainer(Double.NaN));
    assertEquals("{\"type\":\"d\",\"d\":NaN}", writer.toString());
  }

  static class DummyBaseClass {
  }

  static class DoubleContainer extends DummyBaseClass {
    Double d;
    Double d2;

    DoubleContainer(Double d) {
      this.d = d;
    }
  }
}
