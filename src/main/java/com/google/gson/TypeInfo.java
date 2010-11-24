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
import java.util.Collection;

/**
 * Class that provides information relevant to different parts of a type.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
class TypeInfo {
  protected final Type actualType;
  protected final Class<?> rawClass;

  TypeInfo(Type actualType) {
    this.actualType = actualType;
    rawClass = TypeUtils.toRawClass(actualType);
  }

  public final Type getActualType() {
    return actualType;
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
  public final Class<?> getWrappedClass() {
    return Primitives.wrap(rawClass);
  }

  /**
   * @return the raw class associated with this type
   */
  public final Class<?> getRawClass() {
    return rawClass;
  }

  public final boolean isCollectionOrArray() {
    return Collection.class.isAssignableFrom(rawClass) || isArray();
  }

  public final boolean isArray() {
    return TypeUtils.isArray(rawClass);
  }

  public final boolean isEnum() {
    return rawClass.isEnum();
  }

  public final boolean isPrimitive() {
    return Primitives.isWrapperType(Primitives.wrap(rawClass));
  }
}