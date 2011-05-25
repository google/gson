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
 * Context for serialization that is passed to a custom serializer during invocation of its
 * {@link JsonSerializer#serialize(Object, Type, JsonSerializationContext)} method.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonSerializationContext {

  private final ObjectNavigator objectNavigator;
  private final FieldNamingStrategy2 fieldNamingPolicy;
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;
  private final boolean serializeNulls;
  private final MemoryRefStack ancestors;

  JsonSerializationContext(ObjectNavigator objectNavigator,
      FieldNamingStrategy2 fieldNamingPolicy, boolean serializeNulls,
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers) {
    this.objectNavigator = objectNavigator;
    this.fieldNamingPolicy = fieldNamingPolicy;
    this.serializeNulls = serializeNulls;
    this.serializers = serializers;
    this.ancestors = new MemoryRefStack();
  }

  /**
   * Invokes default serialization on the specified object.
   *
   * @param src the object that needs to be serialized.
   * @return a tree of {@link JsonElement}s corresponding to the serialized form of {@code src}.
   */
  public JsonElement serialize(Object src) {
    if (src == null) {
      return JsonNull.INSTANCE;
    }
    return serialize(src, src.getClass(), false);
  }

  /**
   * Invokes default serialization on the specified object passing the specific type information.
   * It should never be invoked on the element received as a parameter of the
   * {@link JsonSerializer#serialize(Object, Type, JsonSerializationContext)} method. Doing
   * so will result in an infinite loop since Gson will in-turn call the custom serializer again.
   *
   * @param src the object that needs to be serialized.
   * @param typeOfSrc the actual genericized type of src object.
   * @return a tree of {@link JsonElement}s corresponding to the serialized form of {@code src}.
   */
  public JsonElement serialize(Object src, Type typeOfSrc) {
    return serialize(src, typeOfSrc, true);
  }

  JsonElement serialize(Object src, Type typeOfSrc, boolean preserveType) {
    if (src == null) {
      return JsonNull.INSTANCE;
    }
    JsonSerializationVisitor visitor = new JsonSerializationVisitor(
        objectNavigator, fieldNamingPolicy, serializeNulls, serializers, this, ancestors);
    ObjectTypePair objTypePair = new ObjectTypePair(src, typeOfSrc, preserveType);
    objectNavigator.accept(objTypePair, visitor);
    return visitor.getJsonElement();
  }
}