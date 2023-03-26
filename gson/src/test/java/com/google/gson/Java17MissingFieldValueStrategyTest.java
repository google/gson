package com.google.gson;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class Java17MissingFieldValueStrategyTest {
  @Test
  public void testDoNothing() {
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(MissingFieldValueStrategy.DO_NOTHING).create();

    CustomRecord deserialized = gson.fromJson("{\"a\": \"custom-a\"}", CustomRecord.class);
    assertThat(deserialized.a).isEqualTo("custom-a");
    assertThat(deserialized.b).isEqualTo(null);
  }

  @Test
  public void testThrowException() {
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(MissingFieldValueStrategy.THROW_EXCEPTION).create();

    CustomRecord deserialized = gson.fromJson("{\"a\": \"custom-a\", \"b\": \"custom-b\"}", CustomRecord.class);
    assertThat(deserialized.a).isEqualTo("custom-a");
    assertThat(deserialized.b).isEqualTo("custom-b");

    try {
      gson.fromJson("{\"a\": \"custom-a\"}", CustomRecord.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Failed handling missing field '" + CustomRecord.class.getName() + "#b' at path $");
      assertThat(expected).hasCauseThat().hasMessageThat().isEqualTo("Missing value for field '" + CustomRecord.class.getName() + "#b'");
    }

    try {
      gson.fromJson("{\"b\": \"custom-b\"}", CustomRecord.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Failed handling missing field '" + CustomRecord.class.getName() + "#a' at path $");
      assertThat(expected).hasCauseThat().hasMessageThat().isEqualTo("Missing value for field '" + CustomRecord.class.getName() + "#a'");
    }
  }


  /**
   * Should only report missing field once, even if {@code @SerializedName} specifies multiple names.
   */
  @Test
  public void testSerializedName() throws Exception {
    List<Field> missingFields = new ArrayList<>();
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field,
          TypeToken<?> resolvedFieldType) {
        missingFields.add(field);
        return 1;
      }
    }).create();

    WithSerializedName deserialized = gson.fromJson("{}", WithSerializedName.class);
    assertThat(deserialized.a).isEqualTo(1);
    Field field = WithSerializedName.class.getDeclaredField("a");
    assertThat(missingFields).containsExactly(field);
  }

  /**
   * Should handle serialization and deserialization exclusions correctly.
   */
  @Test
  public void testExcluded() throws Exception {
    List<Field> missingFields = new ArrayList<>();
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field,
          TypeToken<?> resolvedFieldType) {
        missingFields.add(field);
        return null;
      }
    }).excludeFieldsWithoutExposeAnnotation().create();

    gson.fromJson("{}", WithExclusions.class);
    Field field1 = WithExclusions.class.getDeclaredField("both");
    Field field2 = WithExclusions.class.getDeclaredField("deserialize");
    assertThat(missingFields).containsExactly(field1, field2);
  }

  @Test
  public void testResolvedFieldType() {
    List<TypeToken<?>> fieldTypes = new ArrayList<>();
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field,
          TypeToken<?> resolvedFieldType) {
        fieldTypes.add(resolvedFieldType);
        return null;
      }
    }).create();

    gson.fromJson("{}", new TypeToken<WithTypeVariable<String>>() {});
    assertThat(fieldTypes).containsExactly(TypeToken.get(String.class));
  }

  @Test
  public void testCustom() {
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field, TypeToken<?> resolvedFieldType) {
        assertThat(declaringType).isEqualTo(TypeToken.get(CustomRecord.class));
        // Due to how Record instances are constructed currently cannot provide access to instance
        assertThat(instance).isNull();
        assertThat(field.getDeclaringClass()).isEqualTo(CustomRecord.class);

        if (field.getName().equals("a")) {
          // Preserve existing value
          return null;
        }
        return "field-" + field.getName();
      }
    }).create();

    CustomRecord deserialized = gson.fromJson("{}", CustomRecord.class);
    assertThat(deserialized.a).isEqualTo(null);
    assertThat(deserialized.b).isEqualTo("field-b");


    gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field, TypeToken<?> resolvedFieldType) {
        // Preserve existing value
        return null;
      }
    }).create();
    RecordWithInt deserialized2 = gson.fromJson("{}", RecordWithInt.class);
    // Uses default value for primitive
    assertThat(deserialized2.a).isEqualTo(0);
  }

  @Test
  public void testBadNewFieldValue() {
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field, TypeToken<?> resolvedFieldType) {
        return 1;
      }

      @Override
      public String toString() {
        return "my-strategy";
      }
    }).create();

    try {
      gson.fromJson("{\"a\": \"custom-a\"}", CustomRecord.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      // Exception currently does not point out usage of MissingFieldValueStrategy
      assertThat(expected).hasMessageThat().isEqualTo("Failed to invoke constructor 'com.google.gson.Java17MissingFieldValueStrategyTest$CustomRecord(String, String)'"
          + " with args [custom-a, 1]");
      assertThat(expected).hasCauseThat().isNotNull();
    }
  }

  record CustomRecord(String a, String b) {}

  record RecordWithInt(int a) {}

  record WithSerializedName(
    @SerializedName(value = "b", alternate = {"c", "d", "e"})
    int a
  ) {}

  record WithExclusions(
    @Expose(deserialize = true, serialize = true)
    int both,
    @Expose(deserialize = true, serialize = false)
    int deserialize,
    @Expose(deserialize = false, serialize = true)
    int serialize,
    @Expose(deserialize = false, serialize = false)
    int none
  ) {}

  record WithTypeVariable<T>(
    T a
  ) {}
}
