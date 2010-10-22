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
  private Object obj;
  final Type type;
  private final boolean preserveType;

  ObjectTypePair(Object obj, Type type, boolean preserveType) {
    this.obj = obj;
    this.type = type;
    this.preserveType = preserveType;
  }

  Object getObject() {
    return obj;
  }

  void setObject(Object obj) {
    this.obj = obj;
  }

  Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("preserveType: %b, type: %s, obj: %s", preserveType, type, obj);
  }

  <HANDLER> Pair<HANDLER, ObjectTypePair> getMatchingHandler(
      ParameterizedTypeHandlerMap<HANDLER> handlers) {
    HANDLER handler = null;
    if (!preserveType && obj != null) {
      // First try looking up the handler for the actual type
      ObjectTypePair moreSpecificType = toMoreSpecificType();    
      handler = handlers.getHandlerFor(moreSpecificType.type);
      if (handler != null) {
        return new Pair<HANDLER, ObjectTypePair>(handler, moreSpecificType);
      }
    }
    // Try the specified type
    handler = handlers.getHandlerFor(type);
    return handler == null ? null : new Pair<HANDLER, ObjectTypePair>(handler, this);
  }

  ObjectTypePair toMoreSpecificType() {    
    if (preserveType || obj == null) {
      return this;
    }
    Type actualType = getActualTypeIfMoreSpecific(type, obj.getClass());
    if (actualType == type) {
      return this;
    }
    return new ObjectTypePair(obj, actualType, preserveType);
  }

  // This takes care of situations where the field was declared as an Object, but the
  // actual value contains something more specific. See Issue 54.
  // TODO (inder): This solution will not work if the field is of a generic type, but 
  // the actual object is of a raw type (which is a sub-class of the generic type).
  static Type getActualTypeIfMoreSpecific(Type type, Class<?> actualClass) {
    if (type instanceof Class<?>) {
      Class<?> typeAsClass = (Class<?>) type;
      if (typeAsClass.isAssignableFrom(actualClass)) {
        type = actualClass;
      }
      if (type == Object.class) {
        type = actualClass;
      } 
    }
    return type;
  }

  @Override
  public int hashCode() {
    // Not using type.hashCode() since I am not sure if the subclasses of type reimplement
    // hashCode() to be equal for equal types
    return ((obj == null) ? 31 : obj.hashCode());
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
    return preserveType == other.preserveType;
  }

  public boolean isPreserveType() {
    return preserveType;
  }
}
