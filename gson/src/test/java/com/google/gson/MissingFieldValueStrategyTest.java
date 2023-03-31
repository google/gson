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

public class MissingFieldValueStrategyTest {
  @Test
  public void testDoNothing() {
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(MissingFieldValueStrategy.DO_NOTHING).create();

    CustomClass deserialized = gson.fromJson("{\"a\": \"custom-a\"}", CustomClass.class);
    assertThat(deserialized.a).isEqualTo("custom-a");
    assertThat(deserialized.b).isEqualTo("default-b");
  }

  @Test
  public void testThrowException() {
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(MissingFieldValueStrategy.THROW_EXCEPTION).create();

    CustomClass deserialized = gson.fromJson("{\"a\": \"custom-a\", \"b\": \"custom-b\"}", CustomClass.class);
    assertThat(deserialized.a).isEqualTo("custom-a");
    assertThat(deserialized.b).isEqualTo("custom-b");

    try {
      gson.fromJson("{\"a\": \"custom-a\"}", CustomClass.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Failed handling missing field '" + CustomClass.class.getName() + "#b' at path $");
      assertThat(expected).hasCauseThat().hasMessageThat().isEqualTo("Missing value for field '" + CustomClass.class.getName() + "#b'");
    }

    try {
      gson.fromJson("{\"b\": \"custom-b\"}", CustomClass.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Failed handling missing field '" + CustomClass.class.getName() + "#a' at path $");
      assertThat(expected).hasCauseThat().hasMessageThat().isEqualTo("Missing value for field '" + CustomClass.class.getName() + "#a'");
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
    List<TypeToken<?>> declaringTypes = new ArrayList<>();
    List<TypeToken<?>> fieldTypes = new ArrayList<>();
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field,
          TypeToken<?> resolvedFieldType) {
        declaringTypes.add(declaringType);
        fieldTypes.add(resolvedFieldType);
        return null;
      }
    }).create();

    TypeToken<WithTypeVariable<String>> typeToken = new TypeToken<WithTypeVariable<String>>() {};
    gson.fromJson("{}", typeToken);
    assertThat(declaringTypes).containsExactly(typeToken);
    assertThat(fieldTypes).containsExactly(TypeToken.get(String.class));
  }

  @Test
  public void testCustom() {
    Gson gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field, TypeToken<?> resolvedFieldType) {
        assertThat(declaringType).isEqualTo(TypeToken.get(CustomClass.class));
        assertThat(instance).isInstanceOf(CustomClass.class);
        assertThat(field.getDeclaringClass()).isEqualTo(CustomClass.class);

        try {
          Object existingValue = field.get(instance);
          return "field-" + field.getName() + "-" + existingValue;
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }).create();

    CustomClass deserialized = gson.fromJson("{}", CustomClass.class);
    assertThat(deserialized.a).isEqualTo("field-a-default-a");
    assertThat(deserialized.b).isEqualTo("field-b-default-b");


    gson = new GsonBuilder().setMissingFieldValueStrategy(new MissingFieldValueStrategy() {
      @Override
      public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field, TypeToken<?> resolvedFieldType) {
        if (field.getName().equals("a")) {
          // Preserve existing value
          return null;
        }
        return "field-" + field.getName();
      }
    }).create();

    deserialized = gson.fromJson("{}", CustomClass.class);
    assertThat(deserialized.a).isEqualTo("default-a");
    assertThat(deserialized.b).isEqualTo("field-b");
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
      gson.fromJson("{\"a\": \"custom-a\"}", CustomClass.class);
      fail();
    }
    // TODO: Adjust this once a more specific exception is thrown
    catch (RuntimeException expected) {
      assertThat(expected).hasMessageThat().isEqualTo("Failed storing java.lang.Integer provided by my-strategy"
          + " into field '" + CustomClass.class.getName() + "#b' at path $");
      assertThat(expected).hasCauseThat().isNotNull();
    }
  }

  private static class CustomClass {
    String a = "default-a";
    String b = "default-b";
  }

  static class WithSerializedName {
    @SerializedName(value = "b", alternate = {"c", "d", "e"})
    int a;
  }

  static class WithExclusions {
    @Expose(deserialize = true, serialize = true)
    int both;
    @Expose(deserialize = true, serialize = false)
    int deserialize;
    @Expose(deserialize = false, serialize = true)
    int serialize;
    @Expose(deserialize = false, serialize = false)
    int none;
  }

  private static class WithTypeVariable<T> {
    @SuppressWarnings("unused")
    T a;
  }
}
