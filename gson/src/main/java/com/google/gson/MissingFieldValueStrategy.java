package com.google.gson;

import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Field;

/**
 * A strategy defining how to handle missing field values during reflection-based deserialization.
 *
 * @see GsonBuilder#setMissingFieldValueStrategy(MissingFieldValueStrategy)
 * @since $next-version$
 */
public interface MissingFieldValueStrategy {
  /**
   * This strategy does nothing when a missing field is detected, it preserves the initial field
   * value, if any.
   *
   * <p>This is the default missing field value strategy.
   */
  MissingFieldValueStrategy DO_NOTHING = new MissingFieldValueStrategy() {
    @Override
    public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field, TypeToken<?> resolvedFieldType) {
      // Preserve initial field value
      return null;
    }

    @Override
    public String toString() {
      return "MissingFieldValueStrategy.DO_NOTHING";
    }
  };

  /**
   * This strategy throws an exception when a missing field is detected.
   */
  MissingFieldValueStrategy THROW_EXCEPTION = new MissingFieldValueStrategy() {
    @Override
    public Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field, TypeToken<?> resolvedFieldType) {
      // TODO: Proper exception
      throw new RuntimeException("Missing value for field '" + ReflectionHelper.fieldToString(field) + "'");
    }

    @Override
    public String toString() {
      return "MissingFieldValueStrategy.THROW_EXCEPTION";
    }
  };

  /**
   * Called when a missing field value is detected. Implementations can either throw an exception or
   * return a default value.
   *
   * <p>Returning {@code null} will keep the initial field value, if any. For example when returning
   * {@code null} for the field {@code String f = "default"}, the field will still have the value
   * {@code "default"} afterwards (assuming the constructor of the class was called, see also
   * {@link GsonBuilder#disableJdkUnsafe()}). The type of the returned value has to match the
   * type of the field, no narrowing or widening numeric conversion is performed.
   *
   * <p>The {@code instance} represents an instance of the declaring type with the so far already
   * deserialized fields. It is intended to be used for looking up existing field values to derive
   * the missing field value from them. Manipulating {@code instance} in any way is not recommended.<br>
   * For Record classes (Java 16 feature) the {@code instance} is {@code null}.
   *
   * <p>{@code resolvedFieldType} is the type of the field with type variables being resolved, if
   * possible. For example if {@code class MyClass<T>} has a field {@code T myField} and
   * {@code MyClass<String>} is deserialized, then {@code resolvedFieldType} will be {@code String}.
   *
   * @param declaringType type declaring the field
   * @param instance instance of the declaring type, {@code null} for Record classes
   * @param field field whose value is missing
   * @param resolvedFieldType resolved type of the field
   * @return the field value, or {@code null}
   */
  // TODO: Should this really expose `instance`? Only use case would be to derive value from other fields
  //   but besides that user should not directly manipulate `instance` but return new value instead
  Object handleMissingField(TypeToken<?> declaringType, Object instance, Field field, TypeToken<?> resolvedFieldType);
}
