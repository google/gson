package com.google.gson.internal.reflect;

import com.google.gson.JsonIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class ReflectionHelper {
  private ReflectionHelper() { }

  /**
   * Tries making the field accessible, wrapping any thrown exception in a
   * {@link JsonIOException} with descriptive message.
   *
   * @param field field to make accessible
   * @throws JsonIOException if making the field accessible fails
   */
  public static void makeAccessible(Field field) throws JsonIOException {
    try {
      field.setAccessible(true);
    } catch (Exception exception) {
      throw new JsonIOException("Failed making field '" + field.getDeclaringClass().getName() + "#"
          + field.getName() + "' accessible; either change its visibility or write a custom "
          + "TypeAdapter for its declaring type", exception);
    }
  }

  /**
   * Creates a string representation for a constructor.
   * E.g.: {@code java.lang.String#String(char[], int, int)}
   */
  private static String constructorToString(Constructor<?> constructor) {
    StringBuilder stringBuilder = new StringBuilder(constructor.getDeclaringClass().getName())
      .append('#')
      .append(constructor.getDeclaringClass().getSimpleName())
      .append('(');
    Class<?>[] parameters = constructor.getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        stringBuilder.append(", ");
      }
      stringBuilder.append(parameters[i].getSimpleName());
    }

    return stringBuilder.append(')').toString();
  }

  /**
   * Tries making the constructor accessible, returning an exception message
   * if this fails.
   *
   * @param constructor constructor to make accessible
   * @return exception message; {@code null} if successful, non-{@code null} if
   *    unsuccessful
   */
  public static String tryMakeAccessible(Constructor<?> constructor) {
    try {
      constructor.setAccessible(true);
      return null;
    } catch (Exception exception) {
      return "Failed making constructor '" + constructorToString(constructor) + "' accessible; "
          + "either change its visibility or write a custom InstanceCreator or TypeAdapter for its declaring type: "
          // Include the message since it might contain more detailed information
          + exception.getMessage();
    }
  }
}
