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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import org.junit.Test;

public final class RuntimeTypeAdapterFactoryTest {

  @Test
  public void testRuntimeTypeAdapter() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class).registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(rta).create();

    CreditCard original = new CreditCard("Jesse", 234);
    assertThat(gson.toJson(original, BillingInstrument.class))
        .isEqualTo("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}");
    BillingInstrument deserialized =
        gson.fromJson("{type:'CreditCard',cvv:234,ownerName:'Jesse'}", BillingInstrument.class);
    assertThat(deserialized.ownerName).isEqualTo("Jesse");
    assertThat(deserialized).isInstanceOf(CreditCard.class);
  }

  @Test
  public void testRuntimeTypeAdapterRecognizeSubtypes() {
    // We don't have an explicit factory for CreditCard.class, but we do have one for
    // BillingInstrument.class that has recognizeSubtypes(). So it should recognize CreditCard, and
    // when we call gson.toJson(original) below, without an explicit type, it should be invoked.
    RuntimeTypeAdapterFactory<BillingInstrument> rta =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class)
            .recognizeSubtypes()
            .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(rta).create();

    CreditCard original = new CreditCard("Jesse", 234);
    assertThat(gson.toJson(original))
        .isEqualTo("{\"type\":\"CreditCard\",\"cvv\":234,\"ownerName\":\"Jesse\"}");
    BillingInstrument deserialized =
        gson.fromJson("{type:'CreditCard',cvv:234,ownerName:'Jesse'}", BillingInstrument.class);
    assertThat(deserialized.ownerName).isEqualTo("Jesse");
    assertThat(deserialized).isInstanceOf(CreditCard.class);
  }

  @Test
  public void testRuntimeTypeIsBaseType() {
    TypeAdapterFactory rta =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class)
            .registerSubtype(BillingInstrument.class);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(rta).create();

    BillingInstrument original = new BillingInstrument("Jesse");
    assertThat(gson.toJson(original, BillingInstrument.class))
        .isEqualTo("{\"type\":\"BillingInstrument\",\"ownerName\":\"Jesse\"}");
    BillingInstrument deserialized =
        gson.fromJson("{type:'BillingInstrument',ownerName:'Jesse'}", BillingInstrument.class);
    assertThat(deserialized.ownerName).isEqualTo("Jesse");
  }

  @Test
  public void testNullBaseType() {
    assertThrows(NullPointerException.class, () -> RuntimeTypeAdapterFactory.of(null));
  }

  @Test
  public void testNullTypeFieldName() {
    assertThrows(
        NullPointerException.class,
        () -> RuntimeTypeAdapterFactory.of(BillingInstrument.class, null));
  }

  @Test
  public void testNullSubtype() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class);
    assertThrows(NullPointerException.class, () -> rta.registerSubtype(null));
  }

  @Test
  public void testNullLabel() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class);
    assertThrows(NullPointerException.class, () -> rta.registerSubtype(CreditCard.class, null));
  }

  @Test
  public void testDuplicateSubtype() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class);
    rta.registerSubtype(CreditCard.class, "CC");
    var e =
        assertThrows(
            IllegalArgumentException.class, () -> rta.registerSubtype(CreditCard.class, "Visa"));
    assertThat(e).hasMessageThat().isEqualTo("types and labels must be unique");
  }

  @Test
  public void testDuplicateLabel() {
    RuntimeTypeAdapterFactory<BillingInstrument> rta =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class);
    rta.registerSubtype(CreditCard.class, "CC");
    var e =
        assertThrows(
            IllegalArgumentException.class, () -> rta.registerSubtype(BankTransfer.class, "CC"));
    assertThat(e).hasMessageThat().isEqualTo("types and labels must be unique");
  }

  @Test
  public void testDeserializeMissingTypeField() {
    TypeAdapterFactory billingAdapter =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class).registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(billingAdapter).create();
    var e =
        assertThrows(
            JsonParseException.class,
            () -> gson.fromJson("{ownerName:'Jesse'}", BillingInstrument.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "cannot deserialize "
                + BillingInstrument.class
                + " because it does not define a field named type");
  }

  @Test
  public void testDeserializeMissingSubtype() {
    TypeAdapterFactory billingAdapter =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class).registerSubtype(BankTransfer.class);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(billingAdapter).create();
    var e =
        assertThrows(
            JsonParseException.class,
            () -> gson.fromJson("{type:'CreditCard',ownerName:'Jesse'}", BillingInstrument.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "cannot deserialize "
                + BillingInstrument.class
                + " subtype named CreditCard; did you forget to register a subtype?");
  }

  @Test
  public void testSerializeMissingSubtype() {
    TypeAdapterFactory billingAdapter =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class).registerSubtype(BankTransfer.class);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(billingAdapter).create();
    var e =
        assertThrows(
            JsonParseException.class,
            () -> gson.toJson(new CreditCard("Jesse", 456), BillingInstrument.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "cannot serialize "
                + CreditCard.class.getName()
                + "; did you forget to register a subtype?");
  }

  @Test
  public void testSerializeCollidingTypeFieldName() {
    TypeAdapterFactory billingAdapter =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class, "cvv")
            .registerSubtype(CreditCard.class);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(billingAdapter).create();
    var e =
        assertThrows(
            JsonParseException.class,
            () -> gson.toJson(new CreditCard("Jesse", 456), BillingInstrument.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "cannot serialize "
                + CreditCard.class.getName()
                + " because it already defines a field named cvv");
  }

  @Test
  public void testSerializeWrappedNullValue() {
    TypeAdapterFactory billingAdapter =
        RuntimeTypeAdapterFactory.of(BillingInstrument.class)
            .registerSubtype(CreditCard.class)
            .registerSubtype(BankTransfer.class);
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(billingAdapter).create();
    String serialized =
        gson.toJson(new BillingInstrumentWrapper(null), BillingInstrumentWrapper.class);
    BillingInstrumentWrapper deserialized =
        gson.fromJson(serialized, BillingInstrumentWrapper.class);
    assertThat(deserialized.instrument).isNull();
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
