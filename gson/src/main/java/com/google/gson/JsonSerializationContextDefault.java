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
 * An implementation of serialization context for Gson.
 *
 * @author Inderjeet Singh
 */
final class JsonSerializationContextDefault implements JsonSerializationContext {

  private final ObjectNavigator objectNavigator;
  private final FieldNamingStrategy2 fieldNamingPolicy;
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;
  private final boolean serializeNulls;
  private final MemoryRefStack ancestors;

  JsonSerializationContextDefault(ObjectNavigator objectNavigator,
      FieldNamingStrategy2 fieldNamingPolicy, boolean serializeNulls,
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers) {
    this.objectNavigator = objectNavigator;
    this.fieldNamingPolicy = fieldNamingPolicy;
    this.serializeNulls = serializeNulls;
    this.serializers = serializers;
    this.ancestors = new MemoryRefStack();
  }

  public JsonElement serialize(Object src) {
    if (src == null) {
      return JsonNull.createJsonNull();
    }
    return serialize(src, src.getClass(), false);
  }

  public JsonElement serialize(Object src, Type typeOfSrc) {
    return serialize(src, typeOfSrc, true);
  }

  JsonElement serialize(Object src, Type typeOfSrc, boolean preserveType) {
    if (src == null) {
      return JsonNull.createJsonNull();
    }
    JsonSerializationVisitor visitor = new JsonSerializationVisitor(
        objectNavigator, fieldNamingPolicy, serializeNulls, serializers, this, ancestors);
    ObjectTypePair objTypePair = new ObjectTypePair(src, typeOfSrc, preserveType);
    objectNavigator.accept(objTypePair, visitor);
    return visitor.getJsonElement();
  }
}