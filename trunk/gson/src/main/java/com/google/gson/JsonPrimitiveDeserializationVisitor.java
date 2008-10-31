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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * A visitor that populates a primitive value from its JSON representation
 *
 * @author Inderjeet Singh
 */
final class JsonPrimitiveDeserializationVisitor<T> extends JsonDeserializationVisitor<T> {

  JsonPrimitiveDeserializationVisitor(JsonPrimitive json, Type type,
      ObjectNavigatorFactory factory, ObjectConstructor objectConstructor,
      TypeAdapter typeAdapter, ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers,
      JsonDeserializationContext context) {
    super(json, type, factory, objectConstructor, typeAdapter, deserializers, context);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected T constructTarget() {
    return (T) objectConstructor.construct(targetType);
  }

  public void startVisitingObject(Object node) {
    // do nothing
  }

  public void endVisitingObject(Object node) {
    // do nothing
  }

  public void visitCollection(@SuppressWarnings("unchecked")Collection collection,
      Type componentType) {
    // should not be called since this case should invoke JsonArrayDeserializationVisitor
    throw new IllegalStateException();
  }

  public void visitArray(Object array, Type componentType) {
    // should not be called since this case should invoke JsonArrayDeserializationVisitor
    throw new IllegalStateException();
  }

  @SuppressWarnings("unchecked")
  public void visitPrimitiveValue(Object obj) {
    JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
    if (jsonPrimitive.isBoolean()) {
      target = (T) jsonPrimitive.getAsBooleanWrapper();
    } else if (jsonPrimitive.isNumber()) {
      target = (T) jsonPrimitive.getAsNumber();
    } else if (jsonPrimitive.isString()) {
      target = (T) jsonPrimitive.getAsString();
    } else {
      throw new IllegalStateException();
    }
  }

  public void visitObjectField(Field f, Type typeOfF, Object obj) {
    // should not be called since this case should invoke JsonArrayDeserializationVisitor
    throw new IllegalStateException();
  }

  public void visitCollectionField(Field f, Type typeOfF, Object obj) {
    // should not be called since this case should invoke JsonArrayDeserializationVisitor
    throw new IllegalStateException();
  }

  public void visitArrayField(Field f, Type typeOfF, Object obj) {
    // should not be called since this case should invoke JsonArrayDeserializationVisitor
    throw new IllegalStateException();
  }

  public void visitPrimitiveField(Field f, Type fType, Object obj) {
    // should not be called since this case should invoke JsonArrayDeserializationVisitor
    throw new IllegalStateException();
  }

  public boolean visitFieldUsingCustomHandler(Field f, Type actualTypeOfField, Object parent) {
    // should not be called since this case should invoke JsonObjectDeserializationVisitor
    throw new IllegalStateException();
  }
}
