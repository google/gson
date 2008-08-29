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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * An immutable implementation of the {@link ParameterizedType} interface.  This object allows
 * us to build a reflective {@link Type} objects on demand.  This object is used to support
 * serialization and deserialization of classes with an {@code ParameterizedType} field where
 * as least one of the actual type parameters is a {@code TypeVariable}.
 *
 * <p>Here's an example class:
 * <pre>
 * class Foo<T> {
 *   private List<T> someList;
 *
 *   Foo(List<T> list) {
 *     this.someList = list;
 *   }
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class ParameterizedTypeImpl implements ParameterizedType {

  private final Type rawType;
  private final Type[] actualTypeArguments;
  private final Type owner;

  public ParameterizedTypeImpl(Type rawType, Type[] actualTypeArguments, Type owner) {
    this.rawType = rawType;
    this.actualTypeArguments = actualTypeArguments;
    this.owner = owner;
  }

  public Type getRawType() {
    return rawType;
  }

  public Type[] getActualTypeArguments() {
    return actualTypeArguments;
  }

  public Type getOwnerType() {
    return owner;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof  ParameterizedType)) {
      return false;
    } else {
      // Check that information is equivalent
      ParameterizedType that = (ParameterizedType) o;
      if (this  == that)
      return true;

      Type thatOwner = that.getOwnerType();
      Type thatRawType = that.getRawType();

      return (owner == null ? thatOwner == null : owner.equals(thatOwner))
          && (rawType == null ? thatRawType == null : rawType.equals(thatRawType))
          && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
    }
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(actualTypeArguments)
        ^ (owner == null ? 0 : owner.hashCode())
        ^ (rawType == null ? 0 : rawType.hashCode());
  }
}
