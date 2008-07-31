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

/**
 * A convenience object for retrieving the map type information.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class MapTypeInfo {
  private final ParameterizedType mapType;

  public MapTypeInfo(Type mapType) {
    if (!(mapType instanceof ParameterizedType)) {
      throw new IllegalArgumentException(
          "Map objects need to be parameterized unless you use a custom serializer. "
              + "Use the com.google.gson.reflect.TypeToken to extract the ParameterizedType.");
    }
    TypeInfo<Object> rawType = new TypeInfo<Object>(mapType);
    Preconditions.checkArgument(Map.class.isAssignableFrom(rawType.getTopLevelClass()));
    this.mapType = (ParameterizedType) mapType;
  }

  public Type getKeyType() {
    return mapType.getActualTypeArguments()[0];
  }

  public Type getValueType() {
    return mapType.getActualTypeArguments()[1];
  }
}
