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

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

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
        gson.toJson(original, BillingInstrument.class));
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
  
  static abstract class Outer {
    Inner wrapped;
    Outer(Inner wrapped) {
      this.wrapped = wrapped;
    }
  }
  
  static class OuterA extends Outer {
    OuterA(Inner wrapped) {
      super(wrapped);
    }
  }
  
  static class OuterB extends Outer {
    String other;
    OuterB(Inner wrapped, String other) {
      super(wrapped);
    }
  }
  
  static class Inner {
    String prop;
    Inner(String prop) {
      this.prop = prop;
    }
  }
  
  static class TypeAdapterForInner extends TypeAdapter<Inner> {

    @Override
    public void write(JsonWriter out, Inner value) throws IOException {
      boolean oldSerializeNulls = out.getSerializeNulls();
      try {
        out.setSerializeNulls(true);
        out.beginObject().name("prop").value(value.prop).endObject();
      } finally {
        out.setSerializeNulls(oldSerializeNulls);
      }
    }

    @Override
    public Inner read(JsonReader in) throws IOException {
      in.beginObject();
      in.nextName();
      String value = null;
      if (in.peek() == JsonToken.STRING) {
        value = in.nextString();
      } else {
        in.nextNull();
      }
      in.endObject();
      return new Inner(value);
    }
    
  }

  public void testSerializingToPreserveNullsInEmbeddedObjects() {
    TypeAdapterFactory metaWrapperAdapter = RuntimeTypeAdapterFactory.of(Outer.class, "type")
        .registerSubtype(OuterA.class, "a")
        .registerSubtype(OuterB.class, "b");

    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(metaWrapperAdapter)
        .registerTypeAdapter(Inner.class, new TypeAdapterForInner().nullSafe())
        .create();

    // verify, null serialization works for unwrapped, inner value
    Inner inner = new Inner(null);    
    assertJsonEquivalent(
        "{ prop: null }",
        gson.toJson(inner));

    // verify, null serialization works when wrapped in runtime-type-driven wrapper
    // note: setting "other" to null, to ensure default non-null-serializing behavior is preserved
    OuterB outer = new OuterB(inner, null); 
    assertJsonEquivalent(
        "{ type: 'b', wrapped: { prop: null } }",
        gson.toJson(outer, Outer.class));
  }
  
  private static void assertJsonEquivalent(String expected, String actual) {
    JsonElement expectedElem = new JsonParser().parse(expected);
    JsonElement actualElem = new JsonParser().parse(actual);
    assertEquals(expectedElem, actualElem);
  }
  
}
