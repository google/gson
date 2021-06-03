package com.google.gson.internal.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.google.gson.JsonIOException;

public class ReflectionHelper {
  private ReflectionHelper() { }

  /**
   * Tries making the field accessible, wrapping any thrown exception in a
   * {@link JsonIOException} with descriptive message.
   *
   * @param field Field to make accessible
   * @throws JsonIOException If making the field accessible fails
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

    return stringBuilder.append(")").toString();
  }

  /**
   * Tries making the constructor accessible, wrapping any thrown exception in a
   * {@link JsonIOException} with descriptive message.
   *
   * @param constructor Constructor to make accessible
   * @throws JsonIOException If making the constructor accessible fails
   */
  public static void makeAccessible(Constructor<?> constructor) throws JsonIOException {
    try {
      constructor.setAccessible(true);
    } catch (Exception exception) {
      throw new JsonIOException("Failed making constructor '" + constructorToString(constructor)
          + "' accessible; either change its visibility or write a custom InstanceCreator for its "
          + "declaring type", exception);
    }
  }
}
