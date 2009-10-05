/*
 * Copyright (C) 2009 Google Inc.
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
 * A holder class for an object and its type
 *
 * @author Inderjeet Singh
 */
final class ObjectTypePair {
  private static final int PRIME = 31;

  private final Object obj;
  private final Type type;

  public ObjectTypePair(Object obj, Type type) {
    this.obj = obj;
    this.type = type;
  }

  public Object getObject() {
    return obj;
  }

  public Type getType() {
    return type;
  }

  @Override
  public int hashCode() {
    // Not using type.hashCode() since I am not sure if the subclasses of type reimplement
    // hashCode() to be equal for equal types
    return ((obj == null) ? PRIME : obj.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ObjectTypePair other = (ObjectTypePair) obj;
    if (this.obj == null) {
      if (other.obj != null) {
        return false;
      }
    } else if (this.obj != other.obj) { // Checking for reference equality
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
  }
}
