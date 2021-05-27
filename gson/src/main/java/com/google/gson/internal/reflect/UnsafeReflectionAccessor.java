/*
 * Copyright (C) 2017 The Gson authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.internal.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.gson.JsonIOException;

/**
 * An implementation of {@link ReflectionAccessor} based on {@link Unsafe}.
 * <p>
 * NOTE: This implementation is designed for Java 9. Although it should work with earlier Java releases, it is better to
 * use {@link PreJava9ReflectionAccessor} for them.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class UnsafeReflectionAccessor extends ReflectionAccessor {

  private static Class unsafeClass;
  private final Object theUnsafe = getUnsafeInstance();
  private final Field overrideField = getOverrideField();

  /** {@inheritDoc} */
  @Override
  public void makeAccessible(AccessibleObject ao) {
    boolean success = makeAccessibleWithUnsafe(ao);
    if (!success) {
      try {
        // unsafe couldn't be found, so try using accessible anyway
        ao.setAccessible(true);
      } catch (SecurityException e) {
        throw new JsonIOException("Gson couldn't modify fields for " + ao
          + "\nand sun.misc.Unsafe not found.\nEither write a custom type adapter,"
          + " or make fields accessible, or include sun.misc.Unsafe.", e);
      }
    }
  }

  // Visible for testing only
  boolean makeAccessibleWithUnsafe(AccessibleObject ao) {
    if (theUnsafe != null && overrideField != null) {
      try {
        Method method = unsafeClass.getMethod("objectFieldOffset", Field.class);
        long overrideOffset = (Long) method.invoke(theUnsafe, overrideField);  // long overrideOffset = theUnsafe.objectFieldOffset(overrideField);
        Method putBooleanMethod = unsafeClass.getMethod("putBoolean",  Object.class, long.class, boolean.class);
        putBooleanMethod.invoke(theUnsafe, ao, overrideOffset, true); // theUnsafe.putBoolean(ao, overrideOffset, true);
        return true;
      } catch (Exception ignored) { // do nothing
      }
    }
    return false;
  }

  private static Object getUnsafeInstance() {
    try {
      unsafeClass = Class.forName("sun.misc.Unsafe");
      Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      return unsafeField.get(null);
    } catch (Exception e) {
      return null;
    }
  }

  private static Field getOverrideField() {
    try {
      return AccessibleObject.class.getDeclaredField("override");
    } catch (Exception e) {
      return null;
    }
  }
}
