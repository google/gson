package com.google.gson.functional;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializeNull;
import com.google.gson.reflect.TypeToken;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

/**
 * Tests {@link SerializeNull} annotation
 */
public class SerializeNullAnnotationTest extends TestCase {
  /** Gson with {@link Gson#serializeNulls()} = false */
  private final Gson nullOmittingGson;
  /** Gson with {@link Gson#serializeNulls()} = true */
  private final Gson nullSerializingGson;

  public SerializeNullAnnotationTest() {
    nullOmittingGson = new Gson();
    assertFalse(nullOmittingGson.serializeNulls());
    nullSerializingGson = new GsonBuilder().serializeNulls().create();
  }

  /** Ignores value and always writes {@value #CONSTANT}. */
  private static class ConstantSerializingAdapter extends TypeAdapter<Object> {
    public static final String CONSTANT = "test";

    @Override
    public Object read(JsonReader in) throws IOException {
      throw new UnsupportedOperationException(); // Not needed for these tests
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      out.value(CONSTANT);
    }
  }

  /** Ignores value and always writes null. */
  private static class NullSerializingAdapter extends TypeAdapter<Object> {
    @Override
    public Object read(JsonReader in) throws IOException {
      throw new UnsupportedOperationException(); // Not needed for these tests
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      out.nullValue();
    }
  }

  /** Ignores value and always uses {@link JsonWriter#forceNullValue()}. */
  private static class ForcedNullSerializingAdapter extends TypeAdapter<Object> {
    @Override
    public Object read(JsonReader in) throws IOException {
      throw new UnsupportedOperationException(); // Not needed for these tests
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      out.forceNullValue();
    }
  }

  private static class SerializeNullTestClass {
    @SerializeNull
    Map<Integer, String> f1;

    @SerializeNull
    @JsonAdapter(value = ConstantSerializingAdapter.class, nullSafe = false)
    Boolean f2;

    @SerializeNull
    @JsonAdapter(value = NullSerializingAdapter.class, nullSafe = false)
    String f3;

    @SerializeNull
    @JsonAdapter(value = ForcedNullSerializingAdapter.class, nullSafe = false)
    Integer f4;
  }

  /**
   * Test the default value of {@link SerializeNull#value()}
   */
  public void testDefaultValue() throws NoSuchFieldException {
    SerializeNull annotation = SerializeNullTestClass.class.getDeclaredField("f1").getAnnotation(SerializeNull.class);
    assertTrue(annotation.value());
  }

  /**
   * Test {@code @SerializeNull(true)} for null field values
   */
  private static void testSerializeNull_NullFieldValues(Gson gson) {
    SerializeNullTestClass toSerialize = new SerializeNullTestClass();
    toSerialize.f1 = null;
    toSerialize.f2 = null;
    toSerialize.f3 = null;
    toSerialize.f4 = null;

    String json = gson.toJson(toSerialize);
    // Adapter for f2 always writes non-null
    assertEquals("{\"f1\":null,\"f2\":\"test\",\"f3\":null,\"f4\":null}", json);
  }

  /**
   * Test {@code @SerializeNull(true)} for non-null field values
   */
  private static void testSerializeNull_NonNullFieldValues(Gson gson) {
    SerializeNullTestClass toSerialize = new SerializeNullTestClass();
    Map<Integer, String> map = new HashMap<Integer, String>();
    map.put(1, "a");
    // nested nulls should not be affected by annotation, should therefore
    // be omitted due to getSerializeNulls() = false
    map.put(2, null);
    toSerialize.f1 = map;
    toSerialize.f2 = true;
    toSerialize.f3 = "test";
    toSerialize.f4 = 1;

    String json = gson.toJson(toSerialize);
    String expectedMapJson = gson.serializeNulls() ? "{\"1\":\"a\",\"2\":null}" : "{\"1\":\"a\"}";
    // Adapters for f3 and f4 write null regardless of value
    assertEquals("{\"f1\":" + expectedMapJson + ",\"f2\":\"test\",\"f3\":null,\"f4\":null}", json);
  }

  /**
   * Test {@code @SerializeNull(true)} for null field values with Gson
   * not serializing null
   */
  public void testSerializeNull_NullFieldValues_GsonDontSerializeNull() {
    testSerializeNull_NullFieldValues(nullOmittingGson);
  }

  /**
   * Test {@code @SerializeNull(true)} for non-null field values with Gson
   * not serializing null
   */
  public void testSerializeNull_NonNullFieldValues_GsonDontSerializeNull() {
    testSerializeNull_NonNullFieldValues(nullOmittingGson);
  }

  /**
   * Test {@code @SerializeNull(true)} for null field values with Gson
   * serializing null
   */
  public void testSerializeNull_NullFieldValues_GsonSerializeNull() {
    testSerializeNull_NullFieldValues(nullSerializingGson);
  }

  /**
   * Test {@code @SerializeNull(true)} for non-null field values with Gson
   * serializing null
   */
  public void testSerializeNull_NonNullFieldValues_GsonSerializeNull() {
    testSerializeNull_NonNullFieldValues(nullSerializingGson);
  }

  private static class DontSerializeNullTestClass {
    @SerializeNull(false)
    Map<Integer, String> f1;

    @SerializeNull(false)
    @JsonAdapter(value = ConstantSerializingAdapter.class, nullSafe = false)
    Boolean f2;

    @SerializeNull(false)
    @JsonAdapter(value = NullSerializingAdapter.class, nullSafe = false)
    String f3;

    @SerializeNull(false)
    @JsonAdapter(value = ForcedNullSerializingAdapter.class, nullSafe = false)
    Integer f4;
  }

  /**
   * Test {@code @SerializeNull(false)} for null field values
   */
  private static void testDontSerializeNull_NullFieldValues(Gson gson) {
    DontSerializeNullTestClass toSerialize = new DontSerializeNullTestClass();
    toSerialize.f1 = null;
    toSerialize.f2 = null;
    toSerialize.f3 = null;
    toSerialize.f4 = null;

    String json = gson.toJson(toSerialize);
    // Adapter for f2 always writes non-null, should therefore be in JSON
    // Adapter for f4 forces null, should therefore be in JSON
    assertEquals("{\"f2\":\"test\",\"f4\":null}", json);
  }

  /**
   * Test {@code @SerializeNull(false)} for non-null field values
   */
  private static void testDontSerializeNull_NonNullFieldValues(Gson gson) {
    DontSerializeNullTestClass toSerialize = new DontSerializeNullTestClass();
    Map<Integer, String> map = new HashMap<Integer, String>();
    map.put(1, "a");
    // nested nulls should not be affected by annotation, should therefore
    // be omitted due to getSerializeNulls() = false
    map.put(2, null);
    toSerialize.f1 = map;
    toSerialize.f2 = false;
    toSerialize.f3 = "abc";
    toSerialize.f4 = 2;

    String json = gson.toJson(toSerialize);
    String expectedMapJson = gson.serializeNulls() ? "{\"1\":\"a\",\"2\":null}" : "{\"1\":\"a\"}";
    // Adapter for f3 always writes null, should therefore not be in JSON
    // Adapter for f4 forces null, should therefore be in JSON
    assertEquals("{\"f1\":" + expectedMapJson + ",\"f2\":\"test\",\"f4\":null}", json);
  }

  /**
   * Test {@code @SerializeNull(false)} for null field values with Gson
   * not serializing null
   */
  public void testDontSerializeNull_NullFieldValues_GsonDontSerializeNull() {
    testDontSerializeNull_NullFieldValues(nullOmittingGson);
  }

  /**
   * Test {@code @SerializeNull(false)} for non-null field values with Gson
   * not serializing null
   */
  public void testDontSerializeNull_NonNullFieldValues_GsonDontSerializeNull() {
    testDontSerializeNull_NonNullFieldValues(nullOmittingGson);
  }

  /**
   * Test {@code @SerializeNull(false)} for null field values with Gson
   * serializing null
   */
  public void testDontSerializeNull_NullFieldValues_GsonSerializeNull() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    testDontSerializeNull_NullFieldValues(gson);
  }

  /**
   * Test {@code @SerializeNull(false)} for non-null field values with Gson
   * serializing null
   */
  public void testDontSerializeNull_NonNullFieldValues_GsonSerializeNull() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    testDontSerializeNull_NonNullFieldValues(gson);
  }

  /**
   * Creates adapter which calls {@link JsonWriter#setSerializeNulls(boolean)}
   * before writing value.
   */
  private static class SerializeNullsChangingAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
      return new TypeAdapter<T>() {
        @Override
        public T read(JsonReader in) throws IOException {
          throw new UnsupportedOperationException(); // Not needed for these tests
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
          boolean wasSerializingNulls = out.getSerializeNulls();
          out.setSerializeNulls(true);
          // Verify that @SerializeNull does not affect this method
          assertTrue(out.getSerializeNulls());
          try {
            gson.getAdapter(type).write(out, value);
          } finally {
            out.setSerializeNulls(wasSerializingNulls);
          }
        }
      };
    }
  }

  @SuppressWarnings("unused") // Fields are accessed by Gson
  private static class SerializeNullsChangingTestClass {
    String f1;

    @SerializeNull(false)
    @JsonAdapter(value = SerializeNullsChangingAdapterFactory.class, nullSafe = false)
    String f2;

    Map<Integer, String> f3;

    @SerializeNull(false)
    @JsonAdapter(value = SerializeNullsChangingAdapterFactory.class, nullSafe = false)
    Map<Integer, String> f4;
  }

  public void testSerializeNullChangingAdapter() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setSerializeNulls(false);

    SerializeNullsChangingTestClass toSerialize = new SerializeNullsChangingTestClass();
    Map<Integer, String> map = new HashMap<Integer, String>();
    map.put(1, "a");
    // nested nulls should not be affected by annotation, should therefore
    // be omitted due to getSerializeNulls() = false
    map.put(2, null);
    toSerialize.f1 = null;
    toSerialize.f2 = null;
    toSerialize.f3 = map;
    toSerialize.f4 = map;

    nullOmittingGson.toJson(toSerialize, SerializeNullsChangingTestClass.class, jsonWriter);
    // setSerializeNulls(true) call by field adapter for @SerializeNull(false)
    // field should not affect its direct value (-> f2 omitted), but should affect
    // nested values (property `"2":null` of f4 not omitted)
    assertEquals("{\"f3\":{\"1\":\"a\"},\"f4\":{\"1\":\"a\",\"2\":null}}", stringWriter.toString());
  }

  /** Ignores value and always throws exception. */
  private static class ThrowingAdapter extends TypeAdapter<Object> {
    @Override
    public Object read(JsonReader in) throws IOException {
      throw new UnsupportedOperationException(); // Not needed for these tests
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      throw new NumberFormatException("expected");
    }
  }

  private static class ThrowingTestClass {
    @SerializeNull
    @JsonAdapter(ThrowingAdapter.class)
    String f1 = "test";
  }

  /**
   * Test case when the adapter writes no value because it throws exception:
   * Should reset serialize null overwrite in that case
   */
  public void testThrowingAdapter() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setSerializeNulls(false);

    ThrowingTestClass toSerialize = new ThrowingTestClass();
    try {
      nullOmittingGson.toJson(toSerialize, ThrowingTestClass.class, jsonWriter);
      fail();
    } catch (NumberFormatException expected) {
    }
    jsonWriter.nullValue(); // regular null, not forced
    jsonWriter.endObject();
    jsonWriter.close();
    // Regular null was written so should be omitted due to getSerializeNulls() = false
    assertEquals("{}", stringWriter.toString());
  }
}
