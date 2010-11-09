/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.rest.definition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.webservice.definition.internal.utils.Preconditions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * An id for a rest resource
 *
 * @author inder
 *
 * @param <R> type variable for the rest resource
 */
public final class ValueBasedId<R> implements ID {
  private final long value;
  private final Type typeOfId;

  private ValueBasedId(long value, Type typeOfId) {
    this.value = value;
    this.typeOfId = typeOfId;
  }

  @Override
  public long getValue() {
    return value;
  }

  public static long getValue(ValueBasedId<?> id) {
    return id == null ? INVALID_ID : id.getValue();
  }

  public String getValueAsString() {
    return String.valueOf(value);
  }

  public Type getTypeOfId() {
    return typeOfId;
  }

  @Override
  public int hashCode() {
    return (int) value;
  }

  public static boolean isValid(ValueBasedId<?> id) {
    return id != null && id.value != INVALID_ID;
  }

  /**
   * A more efficient comparison method for ids that take into account of ids being nullable.
   * Since the method is parameterized and both ids are of the same type, this method compares
   * only id values, not their types. Note that this shortcut doesn't work if you pass raw ids
   * to this method
   */
  public static <T> boolean equals(/* @Nullable */ ValueBasedId<T> id1,
      /* @Nullable */ ValueBasedId<T> id2) {
    if ((id1 == null && id2 != null) || (id1 != null && id2 == null)) {
      return false;
    }
    if (id1 == null && id2 == null) {
      return true;
    }
    return id1.value == id2.value;
  }

  @Override  
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    @SuppressWarnings("unchecked")
    ValueBasedId<R> other = (ValueBasedId<R>)obj;
    if (typeOfId == null) {
      if (other.typeOfId != null) return false;
    } else if (!equivalentTypes(typeOfId, other.typeOfId)) return false;
    if (value != other.value) return false;
    return true;
  }

  /**
   * Returns true for equivalentTypes(Class<?>, Class)
   * Visible for testing only 
   */
  @SuppressWarnings("rawtypes")
  static boolean equivalentTypes(Type type1, Type type2) {
    if (type1 instanceof ParameterizedType && type2 instanceof Class) {
      return areEquivalentTypes((ParameterizedType)type1, (Class)type2);
    } else if (type2 instanceof ParameterizedType && type1 instanceof Class) {
      return areEquivalentTypes((ParameterizedType)type2, (Class)type1);
    }
    return type1.equals(type2);
  }

  /**
   * Visible for testing only
   */
  @SuppressWarnings("rawtypes")
  static boolean areEquivalentTypes(ParameterizedType type, Class clazz) {
    Class rawClass = (Class) type.getRawType();
    if (!clazz.equals(rawClass)) {
      return false;
    }
    for (Type typeVariable : type.getActualTypeArguments()) {
      if (typeVariable instanceof WildcardType) {
        continue;
      }
      // This is a real parameterized type, not just ?
      return false;
    }
    return true;
  }

  public static <RS> ValueBasedId<RS> get(long value, Type typeOfId) {
    return new ValueBasedId<RS>(value, typeOfId);
  }

  @Override
  public String toString() {
    String typeAsString = getSimpleTypeName(typeOfId);
    return String.format("{value:%s,type:%s}", value, typeAsString);
  }

  @SuppressWarnings("rawtypes")
  private static String getSimpleTypeName(Type type) {
    if (type == null) {
      return "null";
    }
    if (type instanceof Class) {
      return ((Class)type).getSimpleName();
    } else if (type instanceof ParameterizedType) {
      ParameterizedType pType = (ParameterizedType) type;
      StringBuilder sb = new StringBuilder(getSimpleTypeName(pType.getRawType()));
      sb.append('<');
      boolean first = true;
      for (Type argumentType : pType.getActualTypeArguments()) {
        if (first) {
          first = false;
        } else {
          sb.append(',');
        }
        sb.append(getSimpleTypeName(argumentType));
      }
      sb.append('>');
      return sb.toString();
    } else if (type instanceof WildcardType) {
      return "?";
    }
    return type.toString();
  }

  /**
   * Type adapter for converting an Id to its serialized form
   *
   * @author inder
   *
   */
  public static final class GsonTypeAdapter implements JsonSerializer<ValueBasedId<?>>,
      JsonDeserializer<ValueBasedId<?>> {

    @Override
    public JsonElement serialize(ValueBasedId<?> src, Type typeOfSrc,
        JsonSerializationContext context) {
      return new JsonPrimitive(src.getValue());
    }

    @Override
    public ValueBasedId<?> deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      if (!(typeOfT instanceof ParameterizedType)) {
        throw new JsonParseException("Id of unknown type: " + typeOfT);
      }
      ParameterizedType parameterizedType = (ParameterizedType) typeOfT;
      // Since Id takes only one TypeVariable, the actual type corresponding to the first
      // TypeVariable is the Type we are looking for
      Type typeOfId = parameterizedType.getActualTypeArguments()[0];
      return ValueBasedId.get(json.getAsLong(), typeOfId);
    }
  }
}
