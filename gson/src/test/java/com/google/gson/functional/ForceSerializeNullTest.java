package com.google.gson.functional;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.ForceSerializeNull;
import com.google.gson.annotations.ForceSerializeNull.CheckTime;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

/**
 * Tests {@link ForceSerializeNull} annotation
 */
public class ForceSerializeNullTest extends TestCase {
  private final Gson gson = new Gson();

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

  /** Ignores value and always writes {@code null}. */
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

  private static class BeforeAdapterTestClass {
    @ForceSerializeNull
    Integer f1;

    @ForceSerializeNull
    @JsonAdapter(value = ConstantSerializingAdapter.class, nullSafe = false)
    Boolean f2;

    @ForceSerializeNull
    @JsonAdapter(value = NullSerializingAdapter.class, nullSafe = false)
    String f3;
  }

  /**
   * Test checkTime == BEFORE_ADAPTER for null field values:
   * Type adapter should not be used if field value is null
   */
  public void testBeforeAdapterNull() {
    BeforeAdapterTestClass toSerialize = new BeforeAdapterTestClass();
    toSerialize.f1 = null;
    toSerialize.f2 = null;
    toSerialize.f3 = null;

    String json = gson.toJson(toSerialize);
    assertEquals("{\"f1\":null,\"f2\":null,\"f3\":null}", json);
  }

  /**
   * Test checkTime == BEFORE_ADAPTER for non-null field values:
   * Type adapter should be used in case field values are non-null
   */
  public void testBeforeAdapterNonNull() {
    BeforeAdapterTestClass toSerialize = new BeforeAdapterTestClass();
    toSerialize.f1 = 123;
    toSerialize.f2 = true;
    toSerialize.f3 = "test";

    String json = gson.toJson(toSerialize);
    // f3 is missing because checkTime == BEFORE_ADAPTER does
    // not consider value written by adapter, so null written
    // by adapter is omitted due to getSerializeNulls() = false
    assertEquals("{\"f1\":123,\"f2\":\"test\"}", json);
  }

  private static class AfterAdapterTestClass {
    @ForceSerializeNull(checkTime = CheckTime.AFTER_ADAPTER)
    Map<Integer, String> f1;

    @ForceSerializeNull(checkTime = CheckTime.AFTER_ADAPTER)
    @JsonAdapter(value = ConstantSerializingAdapter.class, nullSafe = false)
    Boolean f2;

    @ForceSerializeNull(checkTime = CheckTime.AFTER_ADAPTER)
    @JsonAdapter(value = NullSerializingAdapter.class, nullSafe = false)
    String f3;
  }

  /**
   * Test checkTime == AFTER_ADAPTER for null field values
   */
  public void testAfterAdapterNull() {
    AfterAdapterTestClass toSerialize = new AfterAdapterTestClass();
    toSerialize.f1 = null;
    toSerialize.f2 = null;
    toSerialize.f3 = null;

    String json = gson.toJson(toSerialize);
    assertEquals("{\"f1\":null,\"f2\":\"test\",\"f3\":null}", json);
  }

  /**
   * Test checkTime == AFTER_ADAPTER for non-null field values
   */
  public void testAfterAdapterNonNull() {
    AfterAdapterTestClass toSerialize = new AfterAdapterTestClass();
    Map<Integer, String> map = new HashMap<Integer, String>();
    map.put(1, "a");
    // nested nulls should not be affected by annotation, should therefore
    // be omitted due to getSerializeNulls() = false
    map.put(2, null);
    toSerialize.f1 = map;
    toSerialize.f2 = true;
    toSerialize.f3 = "test";

    String json = gson.toJson(toSerialize);
    assertEquals("{\"f1\":{\"1\":\"a\"},\"f2\":\"test\",\"f3\":null}", json);
  }

  /** Adapter which always throws exception. */
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


  private static class AfterAdapterThrowingTestClass {
    @ForceSerializeNull(checkTime = CheckTime.AFTER_ADAPTER)
    @JsonAdapter(ThrowingAdapter.class)
    String f1 = "test";
  }

  /**
   * Test checkTime == AFTER_ADAPTER for case when the adapter writes no
   * value because it throws exception:
   * Should reset forced null serialization in that case
   */
  public void testAfterAdapterThrowingAdapter() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.setSerializeNulls(false);

    AfterAdapterThrowingTestClass toSerialize = new AfterAdapterThrowingTestClass();
    try {
      gson.toJson(toSerialize, AfterAdapterThrowingTestClass.class, jsonWriter);
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
