/*
 * Copyright (C) 2008 Google Inc.
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
package com.google.gson.internal;

import java.lang.reflect.Type;

/**
 * Contains static utility methods pertaining to primitive types and their
 * corresponding wrapper types.
 *
 * @author Kevin Bourrillion
 */
public final class Primitives {
  private Primitives() {}

  private static final Class<?>[] classes = {int.class,     float.class, byte.class, 
		  									 double.class,  long.class,  char.class, 
		  									 boolean.class, short.class, void.class,
		  									 Integer.class, Float.class, Byte.class,
		  									 Double.class,  Long.class,  Character.class,
		  									 Boolean.class, Short.class, Void.class};

  //Finds the index of a type, begin is 0 for primitive lookup, 9 for wrapper lookup
  //Returns -1 if the type is not a wrapper/primitve class
  private static int indexOf(Type type, int begin) {
	$Gson$Preconditions.checkNotNull(type);
	Class<?>[] array = classes;
	
	for(int k = begin, end = begin + 9; k < end; ++k) {
	  if(array[k] == type) {
		  return k;
	  }
	}
	return -1;
  }
  
  /**
   * Returns true if this type is a primitive.
   */
  public static boolean isPrimitive(Type type) {
    return indexOf(type, 0) != -1;
  }

  /**
   * Returns {@code true} if {@code type} is one of the nine
   * primitive-wrapper types, such as {@link Integer}.
   *
   * @see Class#isPrimitive
   */
  public static boolean isWrapperType(Type type) {
	return indexOf(type, 9) != -1;
  }

  /**
   * Returns the corresponding wrapper type of {@code type} if it is a primitive
   * type; otherwise returns {@code type} itself. Idempotent.
   * <pre>
   *     wrap(int.class) == Integer.class
   *     wrap(Integer.class) == Integer.class
   *     wrap(String.class) == String.class
   * </pre>
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> wrap(Class<T> type) {
	int index = indexOf(type, 0);
	
    // cast is safe: long.class and Long.class are both of type Class<Long>
	return index == -1 ? type : (Class<T>) classes[index + 9];
  }

  /**
   * Returns the corresponding primitive type of {@code type} if it is a
   * wrapper type; otherwise returns {@code type} itself. Idempotent.
   * <pre>
   *     unwrap(Integer.class) == int.class
   *     unwrap(int.class) == int.class
   *     unwrap(String.class) == String.class
   * </pre>
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> unwrap(Class<T> type) {
	int index = indexOf(type, 9);
	
	// cast is safe: long.class and Long.class are both of type Class<Long>
	return index == -1 ? type : (Class<T>) classes[index - 9];
  }
}