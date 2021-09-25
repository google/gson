package com.google.gson;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

public class TypeAdapterTest extends TestCase {
  static class PropertyNameAdapter extends TypeAdapter<Number> {
    @Override public Number read(JsonReader in) throws IOException {
      assertEquals(JsonToken.STRING, in.peek());
      assertEquals("123", in.nextString());
      return 456;
    }
    @Override public void write(JsonWriter out, Number value) throws IOException {
      fail("Not needed by this test");
    }
  }

  public void testCreatePropertyName() {
    PropertyNameAdapter adapter = new PropertyNameAdapter();
    assertEquals("123", adapter.createPropertyName(123));
    assertEquals("null", adapter.createPropertyName(null));
  }

  public void testReadFromPropertyName() {
    PropertyNameAdapter adapter = new PropertyNameAdapter();
    // Uses custom `read` method
    assertEquals(456, adapter.readFromPropertyName("123"));
  }

  public void testReadFromPropertyName_NotConsuming() {
    class NotConsumingAdapter extends TypeAdapter<Number> {
      @Override public Number read(JsonReader in) throws IOException {
        // Does not consume value
        return null;
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        fail("Not needed by this test");
      }
    }

    NotConsumingAdapter adapter = new NotConsumingAdapter();
    try {
      adapter.readFromPropertyName("123");
      fail();
    } catch (IllegalStateException e) {
      assertEquals("Adapter did not consume name", e.getMessage());
    }
  }

  public void testReadFromPropertyName_Skipping() {
    class SkippingAdapter extends TypeAdapter<Number> {
      @Override public Number read(JsonReader in) throws IOException {
        // Skips value; allowed
        in.skipValue();
        return 456;
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        fail("Not needed by this test");
      }
    }

    SkippingAdapter adapter = new SkippingAdapter();
    // Uses custom `read` method
    assertEquals(456, adapter.readFromPropertyName("123"));
  }

  public void testReadFromPropertyName_ExceptionJsonPath() {
    class BadReadingAdapter extends TypeAdapter<Number> {
      @Override public Number read(JsonReader in) throws IOException {
        in.beginObject(); // should throw
        fail("Exception should have been thrown");
        return null;
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        fail("Not needed by this test");
      }
    }

    BadReadingAdapter adapter = new BadReadingAdapter();
    try {
      adapter.readFromPropertyName("123");
      fail();
    } catch (JsonParseException e) {
      assertEquals("Failed reading from name", e.getMessage());
      // Should contain artificial JSON path
      assertEquals("Expected BEGIN_OBJECT but was STRING at path #fake-property-name-path", e.getCause().getMessage());
    }
  }

  public void testReadFromPropertyName_ClosingReader() {
    class ClosingAdapter extends TypeAdapter<Number> {
      @Override public Number read(JsonReader in) throws IOException {
        in.close(); // should throw
        fail("Exception should have been thrown");
        return null;
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        fail("Not needed by this test");
      }
    }

    ClosingAdapter adapter = new ClosingAdapter();
    try {
      adapter.readFromPropertyName("123");
      fail();
    } catch (JsonParseException e) {
      assertEquals("Failed reading from name", e.getMessage());
      assertEquals("Closing property name reader is not supported", e.getCause().getMessage());
    }
  }

  public void testReadFromPropertyName_ChangingLenient() {
    class LenientRevertingAdapter extends TypeAdapter<Number> {
      @Override public Number read(JsonReader in) throws IOException {
        boolean wasLenient = in.isLenient();
        assertFalse(wasLenient);
        in.setLenient(true);
        try {
          return in.nextDouble();
        } finally {
          // Good: Reverts lenient change
          in.setLenient(wasLenient);
        }
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        fail("Not needed by this test");
      }
    }

    LenientRevertingAdapter adapter = new LenientRevertingAdapter();
    assertEquals(Double.NaN, adapter.readFromPropertyName("NaN"));
  }

  public void testReadFromPropertyName_ChangingLenientNotReverting() {
    class LenientNotRevertingAdapter extends TypeAdapter<Number> {
      @Override public Number read(JsonReader in) throws IOException {
        boolean wasLenient = in.isLenient();
        assertFalse(wasLenient);
        // Bad: Lenient change is not reverted
        in.setLenient(true);
        return in.nextDouble();
      }
      @Override public void write(JsonWriter out, Number value) throws IOException {
        fail("Not needed by this test");
      }
    }

    LenientNotRevertingAdapter adapter = new LenientNotRevertingAdapter();
    try {
      adapter.readFromPropertyName("123");
      fail();
    } catch (IllegalStateException e) {
      assertEquals("Lenient setting was changed but not reverted", e.getMessage());
    }
  }
}
