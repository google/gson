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

import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

/**
 * An implementation of {@link ReflectionAccessor} based on {@link Unsafe}.
 * <p>
 * NOTE: This implementation is designed for Java 9. Although it should work with earlier Java releases, it is better to
 * use {@link PreJava9ReflectionAccessor} for them.
 */
final class UnsafeReflectionAccessor extends ReflectionAccessor {

  private final Unsafe theUnsafe = getUnsafeInstance();
  private final Field overrideField = getOverrideField();

  /**
   * {@inheritDoc}
   */
  @Override
  public void makeAccessible(AccessibleObject ao) {
    if (theUnsafe != null && overrideField != null) {
      long overrideOffset = theUnsafe.objectFieldOffset(overrideField);
      theUnsafe.putBoolean(ao, overrideOffset, true);
    }
  }

  private static Unsafe getUnsafeInstance() {
    try {
      Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      return (Unsafe) unsafeField.get(null);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Field getOverrideField() {
    try {
      return AccessibleObject.class.getDeclaredField("override");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      return null;
    }
  }
}
