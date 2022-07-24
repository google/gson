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
import com.google.gson.annotations.JsonAdapter;
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

  @JsonAdapter(CustomClass.Adapter.class)
  static class CustomClass extends DummyBaseClass {
    Boolean hasTypeField = null;
    int f;

    CustomClass(int f) {
      this.f = f;
    }

    static class Adapter extends TypeAdapter<CustomClass> {
      @Override public void write(JsonWriter out, CustomClass value) throws IOException {
        out.beginObject();
        out.name("f");
        out.value(value.f);
        out.endObject();
      }

      @Override public CustomClass read(JsonReader in) throws IOException {
        Boolean hasTypeField = null;
        Integer fieldValue = null;

        in.beginObject();
        while (in.hasNext()) {
          String name = in.nextName();
          if (name.equals("t")) {
            assertNull(hasTypeField);
            hasTypeField = true;
            assertEquals(in.nextString(), "custom-name");
          } else if (name.equals("f")) {
            assertNull(fieldValue);
            fieldValue = in.nextInt();
          } else {
            fail("Unexpected name: " + name);
          }
        }
        in.endObject();

        assertNotNull(fieldValue);

        CustomClass result = new CustomClass(fieldValue);
        // Compare with Boolean.TRUE because value might be null
        result.hasTypeField = Boolean.TRUE.equals(hasTypeField);
        return result;
      }
    }
  }

  public void testCustomTypeFieldName() {
    TypeAdapterFactory factory = RuntimeTypeAdapterFactory.of(DummyBaseClass.class, "t")
        .registerSubtype(CustomClass.class, "custom-name");
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(factory)
        .create();

    assertEquals("{\"t\":\"custom-name\",\"f\":1}", gson.toJson(new CustomClass(1)));

    CustomClass deserialized = (CustomClass) gson.fromJson("{\"t\":\"custom-name\",\"f\":1}", DummyBaseClass.class);
    // Type field should have been removed
    assertFalse(deserialized.hasTypeField);
    assertEquals(1, deserialized.f);
  }

  public void testMaintainType() {
    TypeAdapterFactory factory = RuntimeTypeAdapterFactory.of(DummyBaseClass.class, "t", true)
        .registerSubtype(CustomClass.class, "custom-name");
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(factory)
        .create();

    assertEquals("{\"f\":1}", gson.toJson(new CustomClass(1)));

    CustomClass deserialized = (CustomClass) gson.fromJson("{\"t\":\"custom-name\",\"f\":1}", DummyBaseClass.class);
    // Type field should not have been removed, and type adapter should have seen it
    assertTrue(deserialized.hasTypeField);
    assertEquals(1, deserialized.f);
  }

  public void testDeserializeReaderSettings() throws IOException {
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
            .of(DummyBaseClass.class, "type").registerSubtype(DoubleContainer.class, "d"))
        .create();
    // Use TypeAdapter to avoid default lenientness of Gson
    TypeAdapter<DummyBaseClass> adapter = gson.getAdapter(DummyBaseClass.class);

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
    Gson gson = new GsonBuilder()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
            .of(DummyBaseClass.class, "type").registerSubtype(DoubleContainer.class, "d"))
        .create();
    // Use TypeAdapter to avoid default lenientness of Gson
    TypeAdapter<DummyBaseClass> adapter = gson.getAdapter(DummyBaseClass.class);

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

  /**
   * Tests serialization behavior when custom adapter temporarily modifies {@link JsonWriter}.
   */
  public void testSerializeAdapterOverwriting() throws IOException {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DoubleContainer.class, new TypeAdapter<DoubleContainer>() {
          @Override public void write(JsonWriter out, DoubleContainer value) throws IOException {
            boolean oldLenient = out.isLenient();
            boolean oldSerializeNulls = out.getSerializeNulls();
            try {
              out.setLenient(true);
              out.setSerializeNulls(true);

              out.beginObject();
              out.name("c1");
              out.value(Double.NaN);
              out.name("c2");
              out.nullValue();
              out.endObject();
            } finally {
              out.setLenient(oldLenient);
              out.setSerializeNulls(oldSerializeNulls);
            }
          }

          @Override public DoubleContainer read(JsonReader in) throws IOException {
            throw new AssertionError("not used by this test");
          }
        })
        .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
            .of(DummyBaseClass.class, "type").registerSubtype(DoubleContainer.class, "d"))
        .create();
    // Use TypeAdapter to avoid default lenientness of Gson
    TypeAdapter<DoubleContainer> adapter = gson.getAdapter(DoubleContainer.class);

    String expectedJson = "{\"type\":\"d\",\"c1\":NaN,\"c2\":null}";

    // First create a permissive writer
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    jsonWriter.setSerializeNulls(true);
    jsonWriter.setLenient(true);

    adapter.write(jsonWriter, new DoubleContainer(0.0));
    assertEquals(expectedJson, writer.toString());

    // Should still have original settings values
    assertEquals(true, jsonWriter.getSerializeNulls());
    assertEquals(true, jsonWriter.isLenient());

    // Then try non-permissive writer; should have same result because custom
    // adapter temporarily changed writer settings
    writer = new StringWriter();
    jsonWriter = new JsonWriter(writer);
    jsonWriter.setSerializeNulls(false);
    jsonWriter.setLenient(false);

    adapter.write(jsonWriter, new DoubleContainer(0.0));
    assertEquals(expectedJson, writer.toString());

    // Should still have original settings values
    assertEquals(false, jsonWriter.getSerializeNulls());
    assertEquals(false, jsonWriter.isLenient());
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
