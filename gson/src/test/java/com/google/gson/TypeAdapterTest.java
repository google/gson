package com.google.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
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
}
