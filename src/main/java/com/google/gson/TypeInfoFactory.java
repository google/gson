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

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * A static factory class used to construct the "TypeInfo" objects.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class TypeInfoFactory {

  private TypeInfoFactory() {
    // Not instantiable since it provides factory methods only.
  }

  public static TypeInfoArray getTypeInfoForArray(Type type) {
    Preconditions.checkArgument(TypeUtils.isArray(type));
    return new TypeInfoArray(type);
  }

  /**
   * Evaluates the "actual" type for the field.  If the field is a "TypeVariable" or has a
   * "TypeVariable" in a parameterized type then it evaluates the real type.
   *
   * @param f the actual field object to retrieve the type from
   * @param typeDefiningF the type that contains the field {@code f}
   * @return the type information for the field
   */
  public static TypeInfo getTypeInfoForField(Field f, Type typeDefiningF) {
    Class<?> classDefiningF = TypeUtils.toRawClass(typeDefiningF);
    Type type = f.getGenericType();
    Type actualType = getActualType(type, typeDefiningF, classDefiningF);
    return new TypeInfo(actualType);
  }

  private static Type getActualType(
      Type typeToEvaluate, Type parentType, Class<?> rawParentClass) {
    if (typeToEvaluate instanceof Class<?>) {
      return typeToEvaluate;
    } else if (typeToEvaluate instanceof ParameterizedType) {
      ParameterizedType castedType = (ParameterizedType) typeToEvaluate;
      Type owner = castedType.getOwnerType();
      Type[] actualTypeParameters =
          extractRealTypes(castedType.getActualTypeArguments(), parentType, rawParentClass);
      Type rawType = castedType.getRawType();
      return new ParameterizedTypeImpl(rawType, actualTypeParameters, owner);
    } else if (typeToEvaluate instanceof GenericArrayType) {
      GenericArrayType castedType = (GenericArrayType) typeToEvaluate;
      Type componentType = castedType.getGenericComponentType();
      Type actualType = getActualType(componentType, parentType, rawParentClass);
      if (componentType.equals(actualType)) {
        return castedType;
      }
      return actualType instanceof Class<?> ?
          TypeUtils.wrapWithArray(TypeUtils.toRawClass(actualType))
          : new GenericArrayTypeImpl(actualType);
    } else if (typeToEvaluate instanceof TypeVariable<?>) {
      if (parentType instanceof ParameterizedType) {
        // The class definition has the actual types used for the type variables.
        // Find the matching actual type for the Type Variable used for the field.
        // For example, class Foo<A> { A a; }
        // new Foo<Integer>(); defines the actual type of A to be Integer.
        // So, to find the type of the field a, we will have to look at the class'
        // actual type arguments.
        TypeVariable<?> fieldTypeVariable = (TypeVariable<?>) typeToEvaluate;
        TypeVariable<?>[] classTypeVariables = rawParentClass.getTypeParameters();
        ParameterizedType objParameterizedType = (ParameterizedType) parentType;
        int indexOfActualTypeArgument = getIndex(classTypeVariables, fieldTypeVariable);
        Type[] actualTypeArguments = objParameterizedType.getActualTypeArguments();
        return actualTypeArguments[indexOfActualTypeArgument];
      } else if (typeToEvaluate instanceof TypeVariable<?>) {
        Type theSearchedType = null;

        do {
          theSearchedType = extractTypeForHierarchy(parentType, (TypeVariable<?>) typeToEvaluate);
        } while ((theSearchedType != null) && (theSearchedType instanceof TypeVariable<?>));

        if (theSearchedType != null) {
          return theSearchedType;
        }
      }

      throw new UnsupportedOperationException("Expecting parameterized type, got " + parentType
          + ".\n Are you missing the use of TypeToken idiom?\n See "
          + "http://sites.google.com/site/gson/gson-user-guide#TOC-Serializing-and-Deserializing-Gener");
    } else if (typeToEvaluate instanceof WildcardType) {
      WildcardType castedType = (WildcardType) typeToEvaluate;
      return getActualType(castedType.getUpperBounds()[0], parentType, rawParentClass);
    } else {
      throw new IllegalArgumentException("Type \'" + typeToEvaluate + "\' is not a Class, "
          + "ParameterizedType, GenericArrayType or TypeVariable. Can't extract type.");
    }
  }

  private static Type extractTypeForHierarchy(Type parentType, TypeVariable<?> typeToEvaluate) {
    Class<?> rawParentType = null;
    if (parentType instanceof Class<?>) {
      rawParentType = (Class<?>) parentType;
    } else if (parentType instanceof ParameterizedType) {
      ParameterizedType parentTypeAsPT = (ParameterizedType) parentType;
      rawParentType = (Class<?>) parentTypeAsPT.getRawType();
    } else {
      return null;
    }

    Type superClass = rawParentType.getGenericSuperclass();
    if (superClass instanceof ParameterizedType
        && ((ParameterizedType) superClass).getRawType() == typeToEvaluate.getGenericDeclaration()) {
      // Evaluate type on this type
      TypeVariable<?>[] classTypeVariables =
          ((Class<?>) ((ParameterizedType) superClass).getRawType()).getTypeParameters();
      int indexOfActualTypeArgument = getIndex(classTypeVariables, typeToEvaluate);

      Type[] actualTypeArguments = null;
      if (parentType instanceof Class<?>) {
        actualTypeArguments = ((ParameterizedType) superClass).getActualTypeArguments();
      } else if (parentType instanceof ParameterizedType) {
        actualTypeArguments = ((ParameterizedType) parentType).getActualTypeArguments();
      } else {
        return null;
      }

      return actualTypeArguments[indexOfActualTypeArgument];
    }

    Type searchedType = null;
    if (superClass != null) {
      searchedType = extractTypeForHierarchy(superClass, typeToEvaluate);
    }
    return searchedType;
  }

  private static Type[] extractRealTypes(
      Type[] actualTypeArguments, Type parentType, Class<?> rawParentClass) {
    Preconditions.checkNotNull(actualTypeArguments);

    Type[] retTypes = new Type[actualTypeArguments.length];
    for (int i = 0; i < actualTypeArguments.length; ++i) {
      retTypes[i] = getActualType(actualTypeArguments[i], parentType, rawParentClass);
    }
    return retTypes;
  }

  private static int getIndex(TypeVariable<?>[] types, TypeVariable<?> type) {
    for (int i = 0; i < types.length; ++i) {
      if (type.equals(types[i])) {
        return i;
      }
    }
    throw new IllegalStateException(
        "How can the type variable not be present in the class declaration!");
  }
}
