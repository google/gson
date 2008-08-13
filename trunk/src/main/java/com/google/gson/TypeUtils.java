package com.google.gson;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;

/**
 * Utility class containing some methods for obtaining information on types.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class TypeUtils {

  static Type getActualTypeOfField(Field f, Class<?> classOfF, Type typeOfF) {
    Type type = f.getGenericType();
    if (type instanceof Class || type instanceof ParameterizedType
        || type instanceof GenericArrayType) {
      return type;
    } else if (type instanceof TypeVariable) {
      TypeVariable<?> fieldTypeVariable = (TypeVariable<?>) type;
      TypeVariable<?>[] classTypeVariables = classOfF.getTypeParameters();
      ParameterizedType objParameterizedType = (ParameterizedType) typeOfF;
      int indexOfActualTypeArgument = getIndex(classTypeVariables, fieldTypeVariable);
      Type[] actualTypeArguments = objParameterizedType.getActualTypeArguments();
      return actualTypeArguments[indexOfActualTypeArgument];
    } else {
      throw new IllegalArgumentException("Type \'" + type + "\' is not a Class, "
          + "ParameterizedType, GenericArrayType or TypeVariable. Can't extract type.");
    }
  }

  static Type toGenericClass(Type type) {
    if (type instanceof Class) {
      return Object.class;
    } else if (type instanceof ParameterizedType) {
      return ((ParameterizedType)type).getActualTypeArguments()[0];
    } else if (type instanceof GenericArrayType) {
      return toGenericClass(((GenericArrayType)type).getGenericComponentType());
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

  static boolean isCollectionOrArray(TypeInfo typeInfo) {
    return Collection.class.isAssignableFrom(typeInfo.getTopLevelClass()) || typeInfo.isArray();
  }

  private static int getIndex(TypeVariable<?>[] types, TypeVariable<?> type) {
    for (int i = 0; i < types.length; ++i) {
      if (type.equals(types[i])) {
        return i;
      }
    }
    throw new IllegalStateException("How can the type variable not be present in the class declaration!");
  }
}
