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

import java.lang.reflect.Type;

/**
 * Class that represents a constructor or method parameter.  The class in which
 * the parameter represents will always by of type object; thus, primitive
 * classes will be converted to its wrapper class.
 *
 * @author Joel Leitch
 */
final class TypeInfo {
  /* Foo<Bar<Red>> object = ...;
   * Type type = new TypeToken<Foo<Bar<Red>>>() {}.getType();
   * if this instance was invoked as new TypeInfo(type);
   * isArray will be false
   * topLevel will be Foo.class
   * componentType will be Foo.class
   * secondLevel (no idea) but meaningful in case type had an array
   * genericOfTopLevel will be ParameterizedType of Bar<Red>
   */
  private final boolean isArray;
  private final Class<?> topLevel;
  private final Class<?> componentType;
  private final Class<?> secondLevel;
  private final Type genericOfTopLevel;

  private TypeInfo(Class<?> topLevel, boolean isArray, Type genericClass) {
    this.isArray = isArray;
    this.topLevel = topLevel;
    Class<?> rootComponentType = topLevel;
    while (rootComponentType.isArray()) {
      rootComponentType = rootComponentType.getComponentType();
    }
    componentType = rootComponentType;
    this.secondLevel = (topLevel.isArray() ? topLevel.getComponentType() : topLevel);
    this.genericOfTopLevel = genericClass;
  }

  public TypeInfo(Type type) {
    this(TypeUtils.toRawClass(type), TypeUtils.isArray(type), TypeUtils.toGenericClass(type));
  }

  public Class<?> getWrappedClazz() {
    return Primitives.wrap(secondLevel);
  }

  /**
   * @return the raw class associated with this type
   */
  public Class<?> getTopLevelClass() {
    return topLevel;
  }

  public boolean isArray() {
    return isArray;
  }

  boolean isPrimitive() {
    return Primitives.isWrapperType(Primitives.wrap(getSecondLevelClass()));
  }

  boolean isString() {
    return getSecondLevelClass() == String.class;
  }

  public boolean isPrimitiveOrStringAndNotAnArray() {
    return (isPrimitive() || isString()) && !isArray();
  }

  public boolean isEnum() {
    return topLevel.isEnum();
  }

  public Type getGenericClass() {
    return genericOfTopLevel;
  }

  /**
   * @return the class type of this parameter if this is a non-array Java object
   * If the object is (single-dimensional or multi-dimensional) array, it is the class of the
   * elements of the array. For example, this method returns Foo.class for Foo[].
   * It will return Foo[].class for Foo[][]
   */
  public Class<?> getSecondLevelClass() {
    return secondLevel;
  }

  /**
   * @return the root component type of the specified object.
   * If the object is a non-array class, then the component type is the class of the object itself.
   * If the object is a single-dimensional array then the component type is the class of an
   * element of the array.
   * If the object is a multi-dimensional array then the component type is the class of the
   * inner-most array element. For example, the This method will return Foo.class for Foo[][][].
   */
  public Class<?> getComponentType() {
    return componentType;
  }
}
