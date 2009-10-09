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

  private final ObjectNavigatorFactory factory;
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;
  private final boolean serializeNulls;
  private final MemoryRefStack ancestors;

  JsonSerializationContextDefault(ObjectNavigatorFactory factory, boolean serializeNulls,
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers) {
    this.factory = factory;
    this.serializeNulls = serializeNulls;
    this.serializers = serializers;
    this.ancestors = new MemoryRefStack();
  }

  public JsonElement serialize(Object src) {
    if (src == null) {
      return JsonNull.createJsonNull();
    }
    return serialize(src, src.getClass(), true);
  }

  public JsonElement serialize(Object src, Type typeOfSrc) {
    return serialize(src, typeOfSrc, true);
  }

  public JsonElement serialize(Object src, Type typeOfSrc, boolean preserveType) {
    ObjectNavigator on = factory.create(new ObjectTypePair(src, typeOfSrc, preserveType));
    JsonSerializationVisitor visitor =
        new JsonSerializationVisitor(factory, serializeNulls, serializers, this, ancestors);
    on.accept(visitor);
    return visitor.getJsonElement();
  }
}
