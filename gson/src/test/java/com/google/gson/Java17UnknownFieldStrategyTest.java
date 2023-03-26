package com.google.gson;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class Java17UnknownFieldStrategyTest {
  @Test
  public void testIgnore() {
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(UnknownFieldStrategy.IGNORE).create();

    CustomRecord deserialized = gson.fromJson("{\"a\": 1, \"b\": 2}", CustomRecord.class);
    assertThat(deserialized.a).isEqualTo(1);
  }

  @Test
  public void testThrowException() {
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(UnknownFieldStrategy.THROW_EXCEPTION).create();

    CustomRecord deserialized = gson.fromJson("{\"a\": 1}", CustomRecord.class);
    assertThat(deserialized.a).isEqualTo(1);

    try {
      gson.fromJson("{\"a\": 1, \"b\": 2}", CustomRecord.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Unknown field 'b' for " + CustomRecord.class + " at path $.b");
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
      gson.fromJson("{\"a\": 1, \"b\": 2}", CustomRecord.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Failed handling unknown field 'b' for " + CustomRecord.class + " at path $.b");
      assertThat(expected).hasCauseThat().hasMessageThat().isEqualTo("my-exception");
    }
  }

  @Test
  public void testCustom() {
    Map<String, Object> unknownFieldsMap = new LinkedHashMap<>();
    Gson gson = new GsonBuilder().setUnknownFieldStrategy(new UnknownFieldStrategy() {
      @Override
      public void handleUnknownField(TypeToken<?> declaringType, Object instance, String fieldName,
          JsonReader jsonReader, Gson gson) throws IOException {
        assertThat(declaringType).isEqualTo(TypeToken.get(CustomRecord.class));
        // Due to how Record instances are constructed currently cannot provide access to instance
        assertThat(instance).isNull();
        assertThat(jsonReader).isNotNull();
        assertThat(gson).isNotNull();

        Object value = gson.fromJson(jsonReader, Object.class);
        unknownFieldsMap.put(fieldName, value);
      }
    }).create();

    CustomRecord deserialized = gson.fromJson("{\"a\": 1, \"b\": 2}", CustomRecord.class);
    assertThat(deserialized.a).isEqualTo(1);
    assertThat(unknownFieldsMap).containsExactly("b", 2.0);
  }

  record CustomRecord(int a) { }
}
