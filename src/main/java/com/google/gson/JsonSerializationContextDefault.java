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
 * An implementation of serialization context for Gson
 * 
 * @author Inderjeet Singh
 */
final class JsonSerializationContextDefault implements JsonSerializationContext {
  
  private final ObjectNavigatorFactory factory;
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;
  private final boolean serializeNulls;

  JsonSerializationContextDefault(ObjectNavigatorFactory factory, boolean serializeNulls, 
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers) {
    this.factory = factory;
    this.serializeNulls = serializeNulls;
    this.serializers = serializers;
  }
  
  public JsonElement serialize(Object src) {
    return serialize(src, src.getClass());
  }
  
  public JsonElement serialize(Object src, Type typeOfSrc) {
    ObjectNavigator on = factory.create(src, typeOfSrc);
    JsonSerializationVisitor visitor = 
      new JsonSerializationVisitor(factory, serializeNulls, serializers, this);
    on.accept(visitor);
    return visitor.getJsonElement();
  }
}
