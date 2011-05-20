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
 * Context for deserialization that is passed to a custom deserializer during invocation of its
 * {@link JsonDeserializer#deserialize(JsonElement, Type, JsonDeserializationContext)}
 * method.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonDeserializationContext {
  private final ObjectNavigator objectNavigator;
  private final FieldNamingStrategy2 fieldNamingPolicy;
  private final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers;
  private final MappedObjectConstructor objectConstructor;

  JsonDeserializationContext(ObjectNavigator objectNavigator,
      FieldNamingStrategy2 fieldNamingPolicy,
      ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers,
      MappedObjectConstructor objectConstructor) {
    this.objectNavigator = objectNavigator;
    this.fieldNamingPolicy = fieldNamingPolicy;
    this.deserializers = deserializers;
    this.objectConstructor = objectConstructor;
  }


  public <T> T construct(Type type) {
    return objectConstructor.construct(type);
  }

  public Object constructArray(Type type, int length) {
    return objectConstructor.constructArray(type, length);
  }


  private <T> T fromJsonArray(Type arrayType, JsonArray jsonArray,
      JsonDeserializationContext context) throws JsonParseException {
    JsonArrayDeserializationVisitor<T> visitor = new JsonArrayDeserializationVisitor<T>(
        jsonArray, arrayType, objectNavigator, fieldNamingPolicy,
        objectConstructor, deserializers, context);
    objectNavigator.accept(new ObjectTypePair(null, arrayType, true), visitor);
    return visitor.getTarget();
  }

  private <T> T fromJsonObject(Type typeOfT, JsonObject jsonObject,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObjectDeserializationVisitor<T> visitor = new JsonObjectDeserializationVisitor<T>(
        jsonObject, typeOfT, objectNavigator, fieldNamingPolicy,
        objectConstructor, deserializers, context);
    objectNavigator.accept(new ObjectTypePair(null, typeOfT, true), visitor);
    return visitor.getTarget();
  }

  @SuppressWarnings("unchecked")
  private <T> T fromJsonPrimitive(Type typeOfT, JsonPrimitive json,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObjectDeserializationVisitor<T> visitor = new JsonObjectDeserializationVisitor<T>(
        json, typeOfT, objectNavigator, fieldNamingPolicy, objectConstructor, deserializers, context);
    objectNavigator.accept(new ObjectTypePair(json.getAsObject(), typeOfT, true), visitor);
    Object target = visitor.getTarget();
    return (T) target;
  }

  /**
   * Invokes default deserialization on the specified object. It should never be invoked on
   * the element received as a parameter of the
   * {@link JsonDeserializer#deserialize(JsonElement, Type, JsonDeserializationContext)} method. Doing
   * so will result in an infinite loop since Gson will in-turn call the custom deserializer again.

   * @param json the parse tree.
   * @param typeOfT type of the expected return value.
   * @param <T> The type of the deserialized object.
   * @return An object of type typeOfT.
   * @throws JsonParseException if the parse tree does not contain expected data.
   */
  public <T> T deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
    if (json == null || json.isJsonNull()) {
      return null;
    } else if (json.isJsonArray()) {
      return fromJsonArray(typeOfT, json.getAsJsonArray(), this);
    } else if (json.isJsonObject()) {
      return fromJsonObject(typeOfT, json.getAsJsonObject(), this);
    } else if (json.isJsonPrimitive()) {
      return fromJsonPrimitive(typeOfT, json.getAsJsonPrimitive(), this);
    } else {
      throw new JsonParseException("Failed parsing JSON source: " + json + " to Json");
    }
  }
}