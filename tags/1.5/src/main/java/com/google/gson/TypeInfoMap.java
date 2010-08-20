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
import java.util.Map;
import java.util.Properties;

/**
 * A convenience object for retrieving the map type information.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class TypeInfoMap {
  private final Type keyType;
  private final Type valueType;
  
  public TypeInfoMap(Type mapType) {
    if (mapType instanceof Class<?> && Properties.class.isAssignableFrom((Class<?>) mapType)) {
      keyType = String.class;
      valueType = String.class;
    } else if (mapType instanceof ParameterizedType) {
      TypeInfo rawType = new TypeInfo(mapType);
      Preconditions.checkArgument(Map.class.isAssignableFrom(rawType.getRawClass()));
      ParameterizedType paramType = (ParameterizedType) mapType;
      keyType = paramType.getActualTypeArguments()[0];
      valueType = paramType.getActualTypeArguments()[1];      
    } else {
      throw new IllegalArgumentException(
          "Map objects need to be parameterized unless you use a custom serializer. "
              + "Use the com.google.gson.reflect.TypeToken to extract the ParameterizedType.");
    }
  }

  public Type getKeyType() {
    return keyType;
  }

  public Type getValueType() {
    return valueType;
  }
}
