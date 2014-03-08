/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;

/**
 * Given a type T, looks for the magic static field named GSON_TYPE_ADAPTER of type
 * TypeAdapter&lt;T&gt; and uses it as the default type adapter.
 *
 * @since 2.3
 */
public final class FieldTypeAdapterFactory implements TypeAdapterFactory {
  private static final String FIELD_ADAPTER_NAME = "GSON_TYPE_ADAPTER";

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> targetType) {
    Class<? super T> clazz = targetType.getRawType();
    try {
      Field typeAdapterField = clazz.getDeclaredField(FIELD_ADAPTER_NAME);
      typeAdapterField.setAccessible(true);
      if (!Modifier.isStatic(typeAdapterField.getModifiers())) return null;
      Object fieldAdapterValue = typeAdapterField.get(null);
      if (fieldAdapterValue != null && fieldAdapterValue instanceof TypeAdapter) {
        // We know that the GSON_TYPE_ADAPTER field is of type TypeAdapter.
        // However, we need to assert that its type variable TypeAdapter<TypeVariable> matches
        // the target type
        Type fieldTypeVariable = $Gson$Types.getFirstTypeArgument(typeAdapterField.getGenericType());
        if (targetType.getType().equals(fieldTypeVariable)) {
          return (TypeAdapter) fieldAdapterValue;
        }
      }
    } catch (Exception e) { // ignore1
    }
    return null;
  }

}
