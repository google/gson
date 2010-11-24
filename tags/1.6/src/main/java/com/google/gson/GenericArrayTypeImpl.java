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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * An simple pojo-like immutable instance of the {@link GenericArrayType}.  This object provides
 * us the ability to create reflective types on demand.  This object is required for support
 * object similar to the one defined below:
 * <pre>
 * class Foo<T> {
 *   private final List<T>[] arrayOfListT;
 *
 *   Foo(List<T>[] arrayList) {
 *     this.arrayOfListT = arrayList;
 *   }
 * }
 * </pre>
 *
 * <p>During parsing or serialization, we know the real variable type parameter {@code T},
 * so we can build a new {@code GenericTypeArray} with the "real" type parameters and
 * pass that object along instead.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class GenericArrayTypeImpl implements GenericArrayType {

  private final Type genericComponentType;

  public GenericArrayTypeImpl(Type genericComponentType) {
    this.genericComponentType = genericComponentType;
  }

  public Type getGenericComponentType() {
    return genericComponentType;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof  GenericArrayType)) {
      return false;
    }
    GenericArrayType that = (GenericArrayType) o;
    Type thatComponentType = that.getGenericComponentType();
    return genericComponentType == null ?
        thatComponentType == null : genericComponentType.equals(thatComponentType);
  }

  @Override
  public int hashCode() {
    return (genericComponentType == null) ? 0 : genericComponentType.hashCode();
  }
}
