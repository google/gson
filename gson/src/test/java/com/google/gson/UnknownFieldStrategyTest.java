package com.google.gson;


import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class UnknownFieldStrategyTest {
  @Test
  public void testIgnore() {
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(UnknownFieldStrategy.IGNORE).create();

    CustomClass deserialized = gson.fromJson("{\"a\": 1, \"b\": 2}", CustomClass.class);
    assertThat(deserialized.a).isEqualTo(1);
    assertThat(deserialized.unknownFields).isEmpty();
  }

  @Test
  public void testThrowException() {
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(UnknownFieldStrategy.THROW_EXCEPTION).create();

    CustomClass deserialized = gson.fromJson("{\"a\": 1}", CustomClass.class);
    assertThat(deserialized.a).isEqualTo(1);
    assertThat(deserialized.unknownFields).isEmpty();

    try {
      gson.fromJson("{\"a\": 1, \"b\": 2}", CustomClass.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Unknown field 'b' for " + CustomClass.class + " at path $.b");
    }
  }

  @Test
  public void testCustomThrowing() {
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(new UnknownFieldStrategy() {
      @Override
      public void handleUnknownField(TypeToken<?> declaringType, Object instance, String fieldName,
          JsonReader jsonReader, Gson gson) throws IOException {
        throw new RuntimeException("my-exception");
      }
    }).create();

    try {
      gson.fromJson("{\"a\": 1, \"b\": 2}", CustomClass.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Failed handling unknown field 'b' for " + CustomClass.class + " at path $.b");
      assertThat(expected).hasCauseThat().hasMessageThat().isEqualTo("my-exception");
    }
  }

  @Test
  public void testCustomThrowingAfterRead() {
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(new UnknownFieldStrategy() {
      @Override
      public void handleUnknownField(TypeToken<?> declaringType, Object instance, String fieldName,
          JsonReader jsonReader, Gson gson) throws IOException {
        // Consume the value before throwing exception
        jsonReader.skipValue();
        throw new RuntimeException("my-exception");
      }
    }).create();

    try {
      gson.fromJson("{\"a\": 1, \"b\": 2}", CustomClass.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Failed handling unknown field 'b' for " + CustomClass.class + " at path $.b");
      assertThat(expected).hasCauseThat().hasMessageThat().isEqualTo("my-exception");
    }
  }

  /**
   * Provides a simple example for how to store unknown values in an extra field on the class.
   *
   * <p><b>Important</b>: Do not use this code in production; it is not properly handling the
   * case where no such field exists, or when the deserialized class is a Record.
   */
  @Test
  public void testCustom() {
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(new UnknownFieldStrategy() {
      @Override
      public void handleUnknownField(TypeToken<?> declaringType, Object instance, String fieldName,
          JsonReader jsonReader, Gson gson) throws IOException {
        assertThat(declaringType).isEqualTo(TypeToken.get(CustomClass.class));
        assertThat(instance).isInstanceOf(CustomClass.class);
        assertThat(jsonReader).isNotNull();
        assertThat(gson).isNotNull();

        try {
          Field unknownFieldsField = declaringType.getRawType().getDeclaredField("unknownFields");

          @SuppressWarnings("unchecked")
          Map<String, Object> unknownFieldsMap = (Map<String, Object>) unknownFieldsField.get(instance);
          if (unknownFieldsMap.containsKey(fieldName)) {
            throw new IllegalArgumentException("Already contains value for " + fieldName);
          }

          Object value = gson.fromJson(jsonReader, Object.class);
          unknownFieldsMap.put(fieldName, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }).create();

    CustomClass deserialized = gson.fromJson("{\"a\": 1, \"b\": 2}", CustomClass.class);
    assertThat(deserialized.a).isEqualTo(1);
    assertThat(deserialized.unknownFields).containsExactly("b", 2.0);
  }

  @Test
  public void testExcludedConsideredUnknown() {
    List<String> unknownFields = new ArrayList<>();
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(new UnknownFieldStrategy() {
      @Override
      public void handleUnknownField(TypeToken<?> declaringType, Object instance, String fieldName,
          JsonReader jsonReader, Gson gson) throws IOException {
        jsonReader.skipValue();
        unknownFields.add(fieldName);
      }
    }).excludeFieldsWithoutExposeAnnotation().create();

    WithExcluded obj = new WithExcluded();
    obj.a = 1;
    String json = gson.toJson(obj);
    // Serialization should include field
    assertThat(json).isEqualTo("{\"a\":1}");

    WithExcluded deserialized = gson.fromJson(json, WithExcluded.class);
    assertThat(deserialized.a).isEqualTo(0);
    // Excluded field should be considered unknown
    assertThat(unknownFields).containsExactly("a");
  }

  private static class CustomClass {
    int a;

    transient Map<String, Object> unknownFields = new LinkedHashMap<>();
  }

  private static class WithExcluded {
    @Expose(deserialize = false, serialize = true)
    int a;
  }
}
