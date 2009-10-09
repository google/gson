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

package com.google.gson;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Utility class containing some methods for obtaining information on types.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class TypeUtils {

  /**
   * Returns the actual type matching up with the first type variable.
   * So, for a {@code typeInfo} instance defined as:
   * <pre>
   *   class Foo<A, B> {
   *   }
   *   Type fooType = new TypeToken<Foo<Integer, String>>() {}.getType();
   * </pre>
   * <code>TypeUtils.getActualTypeForFirstTypeVariable(fooType)</code> will return Integer.class.
   */
  static Type getActualTypeForFirstTypeVariable(Type type) {
    if (type instanceof Class<?>) {
      return Object.class;
    } else if (type instanceof ParameterizedType) {
      return ((ParameterizedType)type).getActualTypeArguments()[0];
    } else if (type instanceof GenericArrayType) {
      return getActualTypeForFirstTypeVariable(((GenericArrayType)type).getGenericComponentType());
    } else {
      throw new IllegalArgumentException("Type \'" + type + "\' is not a Class, "
          + "ParameterizedType, or GenericArrayType. Can't extract class.");
    }
  }

  static boolean isArray(Type type) {
    if (type instanceof Class<?>) {
      return ((Class<?>)type).isArray();
    } else if (type instanceof GenericArrayType) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This method returns the actual raw class associated with the specified type.
   */
  static Class<?> toRawClass(Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType actualType = (ParameterizedType)type;
      return toRawClass(actualType.getRawType());
    } else if (type instanceof GenericArrayType) {
      GenericArrayType actualType = (GenericArrayType) type;
      Class<?> rawClass = toRawClass(actualType.getGenericComponentType());
      return wrapWithArray(rawClass);
    } else if (type instanceof WildcardType) {
      WildcardType castedType = (WildcardType) type;
      return toRawClass(castedType.getUpperBounds()[0]);
    } else {
      throw new IllegalArgumentException("Type \'" + type + "\' is not a Class, "
          + "ParameterizedType, or GenericArrayType. Can't extract class.");
    }
  }

  static Class<?> wrapWithArray(Class<?> rawClass) {
    return Array.newInstance(rawClass, 0).getClass();
  }

  private TypeUtils() {
    // Class with just some static utility methods, should not be instantiated
  }
}
