package com.google.gson;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class containing some methods for obtaining information on types.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class TypeUtils {

  /**
   * Returns the actual type matching up with the first type variable.
   * So, for
   * <pre>
   *   class Foo<A, B> {
   *   }
   * new Foo<Integer, String>{}
   * </pre>, it will return Integer.class.
   */
  static Type getActualTypeForFirstTypeVariable(Type type) {
    if (type instanceof Class) {
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
    if (type instanceof Class) {
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
    if (type instanceof Class) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType actualType = (ParameterizedType)type;
      return toRawClass(actualType.getRawType());
    } else if (type instanceof GenericArrayType) {
      GenericArrayType actualType = (GenericArrayType) type;
      Class<?> rawClass = toRawClass(actualType.getGenericComponentType());
      return wrapWithArray(rawClass);
    } else {
      throw new IllegalArgumentException("Type \'" + type + "\' is not a Class, "
          + "ParameterizedType, or GenericArrayType. Can't extract class.");
    }
  }

  private static Class<?> wrapWithArray(Class<?> rawClass) {
    return Array.newInstance(rawClass, 0).getClass();
  }
}
