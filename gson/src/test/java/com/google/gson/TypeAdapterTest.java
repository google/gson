package com.google.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.Test;

public class TypeAdapterTest {
  @Test
  public void testNullSafe() throws IOException {
    TypeAdapter<String> adapter = new TypeAdapter<String>() {
      @Override public void write(JsonWriter out, String value) {
        throw new AssertionError("unexpected call");
      }

      @Override public String read(JsonReader in) {
        throw new AssertionError("unexpected call");
      }
    }.nullSafe();

    assertEquals("null", adapter.toJson(null));
    assertNull(adapter.fromJson("null"));
  }

  private static final TypeAdapter<String> adapter = new TypeAdapter<String>() {
    @Override public void write(JsonWriter out, String value) throws IOException {
      out.value(value);
    }

    @Override public String read(JsonReader in) throws IOException {
      return in.nextString();
    }
  };

  // Note: This test just verifies the current behavior; it is a bit questionable
  // whether that behavior is actually desired
  @Test
  public void testFromJson_Reader_TrailingData() throws IOException {
    assertEquals("a", adapter.fromJson(new StringReader("\"a\"1")));
  }

  // Note: This test just verifies the current behavior; it is a bit questionable
  // whether that behavior is actually desired
  @Test
  public void testFromJson_String_TrailingData() throws IOException {
    assertEquals("a", adapter.fromJson("\"a\"1"));
  }

  private static final TypeAdapter<Double> customDoubleAdapter = new TypeAdapter<Double>() {
    @Override public void write(JsonWriter out, Double value) throws IOException {
      out.beginObject();
      out.name("d");
      out.value(value);
      out.endObject();
    }

    @Override public Double read(JsonReader in) throws IOException {
      // Note: Does not match `write` method above because tests don't require that
      return in.nextDouble();
    }
  };

  @Test
  public void testToJsonTree() {
    {
      JsonObject expectedJson = new JsonObject();
      expectedJson.addProperty("d", 1.0);

      assertEquals(expectedJson, customDoubleAdapter.toJsonTree(1.0));
    }

    {
      JsonObject expectedJson = new JsonObject();
      expectedJson.add("d", JsonNull.INSTANCE);

      assertEquals(expectedJson, customDoubleAdapter.toJsonTree(null));
    }

    try {
      customDoubleAdapter.toJsonTree(Double.NaN);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("JSON forbids NaN and infinities: NaN", e.getMessage());
    }
  }

  @Test
  public void testToJsonTreeWithSettingsFrom() {
    JsonWriter customWriter = new JsonWriter(new StringWriter());

    {
      JsonObject expectedJson = new JsonObject();
      expectedJson.add("d", JsonNull.INSTANCE);

      assertEquals(expectedJson, customDoubleAdapter.toJsonTreeWithSettingsFrom(null, customWriter));
    }

    try {
      customDoubleAdapter.toJsonTreeWithSettingsFrom(Double.NaN, customWriter);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("JSON forbids NaN and infinities: NaN", e.getMessage());
    }

    customWriter.setLenient(true);
    customWriter.setSerializeNulls(false);

    // Should be empty JSON object
    assertEquals(new JsonObject(), customDoubleAdapter.toJsonTreeWithSettingsFrom(null, customWriter));

    {
      JsonObject expectedJson = new JsonObject();
      expectedJson.addProperty("d", Double.NaN);
      assertEquals(expectedJson, customDoubleAdapter.toJsonTreeWithSettingsFrom(Double.NaN, customWriter));
    }
  }

  @Test
  public void testFromJsonTree() {
    assertEquals((Double) 1.0, customDoubleAdapter.fromJsonTree(new JsonPrimitive(1.0)));

    try {
      customDoubleAdapter.fromJsonTree(new JsonPrimitive(Double.NaN));
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("JSON forbids NaN and infinities: NaN", e.getMessage());
    }
  }

  @Test
  public void testFromJsonTreeWithSettingsFrom() {
    JsonReader customReader = new JsonReader(new StringReader(""));

    try {
      customDoubleAdapter.fromJsonTreeWithSettingsFrom(new JsonPrimitive(Double.NaN), customReader);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("JSON forbids NaN and infinities: NaN", e.getMessage());
    }

    customReader.setLenient(true);

    Double deserialized = customDoubleAdapter.fromJsonTreeWithSettingsFrom(new JsonPrimitive(Double.NaN), customReader);
    assertEquals((Double) Double.NaN, deserialized);
  }
}
