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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import com.google.gson.JsonIOException;

/**
 * An implementation of {@link ReflectionAccessor} based on {@link Unsafe}.
 * <p>
 * NOTE: This implementation is designed for Java 9. Although it should work with earlier Java releases, it is better to
 * use {@link PreJava9ReflectionAccessor} for them.
 */
@SuppressWarnings("rawtypes")
final class UnsafeReflectionAccessor extends ReflectionAccessor{

  private static Class unsafeClass;
  private static final Object theUnsafe = getUnsafeInstance();

  private static final MethodHandle objectFieldOffsetHandle = getObjectFieldOffsetHandle();
  private static final MethodHandle putBooleanHandle = getPutBooleanHandle();
  
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
    if (theUnsafe != null && objectFieldOffsetHandle != null && putBooleanHandle != null) {
      try {
    	long overrideOffset = (long) objectFieldOffsetHandle.invokeExact(); // long overrideOffset = theUnsafe.objectFieldOffset(overrideField);
    	putBooleanHandle.invokeExact((Object) ao, overrideOffset, true); // theUnsafe.putBoolean(ao, overrideOffset, true);
        return true;
      } catch (Throwable ignored) { // do nothing
      }
    }
    return false;
  }

  private static MethodHandle getObjectFieldOffsetHandle() {
	  try {
		return MethodHandles.publicLookup()
							.findVirtual(unsafeClass, "objectFieldOffset", MethodType.methodType(long.class, Field.class))
							.bindTo(theUnsafe)
							.bindTo(AccessibleObject.class.getDeclaredField("override"));
	} catch (NoSuchMethodException e) {
		return null;
	} catch (IllegalAccessException e) {
		return null;
	} catch (NoSuchFieldException e) {
		return null;
	} catch (SecurityException e) {
		return null;
	}
  }
  
  private static MethodHandle getPutBooleanHandle() {
	  try {
		return MethodHandles.publicLookup()
							.findVirtual(unsafeClass, "putBoolean", MethodType.methodType(void.class, Object.class, long.class, boolean.class))
							.bindTo(theUnsafe);
	} catch (NoSuchMethodException e) {
		return null;
	} catch (IllegalAccessException e) {
		return null;
	}
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
}
