package com.google.gson.functional;

import java.io.IOException;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.ExplicitlyNullableJsonElement;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

public class JsonElementNullTest extends TestCase {
  private static class AnnotationOnInvalidType {
    @ExplicitlyNullableJsonElement
    String f;
  }

  @Test
  public void testAnnotationOnInvalidType() {
    try {
      new Gson().fromJson("{}", AnnotationOnInvalidType.class);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("Non-JsonElement field 'java.lang.String " + AnnotationOnInvalidType.class.getName()
          + ".f' annotated with @ExplicitlyNullableJsonElement", expected.getMessage());
    }
  }

  private static class Annotated {
    @ExplicitlyNullableJsonElement
    JsonElement f;

    @ExplicitlyNullableJsonElement
    JsonObject f2;

    // Should also work for JsonNull
    @ExplicitlyNullableJsonElement
    JsonNull f3;
  }

  @Test
  public void testNullValues() {
    Annotated deserialized = new Gson().fromJson("{\"f\": null, \"f2\": null, \"f3\": null}", Annotated.class);
    assertNull(deserialized.f);
    assertNull(deserialized.f2);
    assertNull(deserialized.f3);
  }

  /**
   * Verify that fields annotated with {@link ExplicitlyNullableJsonElement} are
   * assigned the correct value if their JSON value is non-{@code null}
   */
  @Test
  public void testNonNullValues() {
    Annotated deserialized = new Gson().fromJson("{\"f\": 1, \"f2\": {}}", Annotated.class);
    assertEquals(new JsonPrimitive(1), deserialized.f);
    assertEquals(0, deserialized.f2.size());
  }

  private static class GenericUnboundAnnotated<T> {
    @ExplicitlyNullableJsonElement
    protected T f;
  }

  private static class JsonElementGeneric extends GenericUnboundAnnotated<JsonElement> {
  }

  private static class GenericBoundAnnotated<T extends JsonElement> {
    @ExplicitlyNullableJsonElement
    T f;
  }

  /**
   * Verify that declared field type instead of effective type is checked, otherwise
   * this would allow potentially unsafe constructs
   */
  @Test
  public void testDeclaredFieldType() {
    /*
     * Here effective field type is JsonElement, however declared field type
     * is unbound, so this should fail
     */
    try {
      new Gson().fromJson("{}", JsonElementGeneric.class);
      fail();
    } catch (IllegalArgumentException expected) {
      assertEquals("Non-JsonElement field 'protected java.lang.Object " + GenericUnboundAnnotated.class.getName()
          + ".f' annotated with @ExplicitlyNullableJsonElement", expected.getMessage());
    }

    // Generic type is bound to JsonElement or subclass, so this should work
    GenericBoundAnnotated<?> deserialized = new Gson().fromJson("{\"f\": null}", GenericBoundAnnotated.class);
    assertNull(deserialized.f);
  }


  private static class FieldWithCustomAdapter {
    @ExplicitlyNullableJsonElement
    @JsonAdapter(value = CustomAdapter.class, nullSafe = false)
    JsonElement f;
  }

  private static class CustomAdapter extends TypeAdapter<JsonElement> {
    public static final JsonElement DESERIALIZED = new JsonPrimitive(2);

    @Override
    public JsonElement read(JsonReader in) throws IOException {
      in.skipValue();
      // Simply always return the same value
      return DESERIALIZED;
    }

    @Override
    public void write(JsonWriter out, JsonElement value) throws IOException {
      // Not needed for this test
      fail();
    }
  }

  /**
   * Verifies that non-{@code null} values are handled by custom adapters
   * specified using the {@link JsonAdapter} annotation
   *
   * <p>Note: Checking for custom adapters registered on {@code GsonBuilder}
   * is not necessary because built-in {@link JsonElement} adapter cannot
   * be overwritten.
   */
  @Test
  public void testCustomAdapter() {
    FieldWithCustomAdapter deserialized = new Gson().fromJson("{\"f\": \"test\"}", FieldWithCustomAdapter.class);
    assertSame(CustomAdapter.DESERIALIZED, deserialized.f);
  }

  /**
   * Verifies that even if {@link JsonAdapter} annotation specifies that
   * adapter handles {@code null}, {@link ExplicitlyNullableJsonElement}
   * should have higher precedence
   */
  @Test
  public void testCustomAdapterNull() {
    FieldWithCustomAdapter deserialized = new Gson().fromJson("{\"f\": null}", FieldWithCustomAdapter.class);
    // If custom adapter was used, this would be CustomAdapter.DESERIALIZED
    assertNull(deserialized.f);
  }
}
