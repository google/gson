package com.google.gson.internal.reflect;

import java.lang.reflect.AccessibleObject;

import com.google.gson.JsonIOException;

public class ReflectionHelper {
  private ReflectionHelper() { }

  /**
   * Tries making the object accessible, wrapping any thrown exception in a
   * {@link JsonIOException} with descriptive message.
   *
   * @param object Object to make accessible
   * @throws JsonIOException If making the object accessible fails
   */
  public static void makeAccessible(AccessibleObject object) throws JsonIOException {
    try {
      object.setAccessible(true);
    } catch (Exception exception) {
      throw new JsonIOException("Failed making '" + object + "' accessible; either change its visibility "
          + "or write a custom TypeAdapter for its declaring type", exception);
    }
  }
}
