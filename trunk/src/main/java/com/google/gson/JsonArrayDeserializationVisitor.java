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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * A visitor that populates fields of an object with data from its equivalent
 * JSON representation
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class JsonArrayDeserializationVisitor<T> extends JsonDeserializationVisitor<T> {
  private final Class<?> componentType;

  @SuppressWarnings("unchecked")
  JsonArrayDeserializationVisitor(JsonArray jsonArray, Type arrayType,
      ObjectNavigatorFactory factory, ObjectConstructor objectConstructor,
      TypeAdapter typeAdapter, ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers,
      JsonDeserializationContext context) {
    super(jsonArray, factory, objectConstructor, typeAdapter, deserializers, context);

    TypeInfo<T> arrayTypeInfo = new TypeInfo<T>(arrayType);
    this.componentType = arrayTypeInfo.getSecondLevelClass();

    if (arrayTypeInfo.isPrimitiveOrStringAndNotAnArray()) {
      if (jsonArray.size() != 1) {
        throw new IllegalArgumentException(
            "Primitives should be an array of length 1, but was: " + jsonArray);
      }
      target = (T) objectConstructor.construct(arrayTypeInfo.getWrappedClazz());
    } else if (arrayTypeInfo.isArray()) {
      target = (T) objectConstructor.constructArray(arrayTypeInfo.getSecondLevelClass(),
          jsonArray.size());
    } else { // is a collection
      target = (T) objectConstructor.construct(arrayTypeInfo.getSecondLevelClass());
    }
  }

  public void visitArray(Object array, Type arrayType) {
    JsonArray jsonArray = json.getAsJsonArray();
    TypeInfo<?> arrayTypeInfo = new TypeInfo<Object>(arrayType);
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonElement jsonChild = jsonArray.get(i);
      Object child;

      if (jsonChild == null) {
        child = null;
      } else if (jsonChild instanceof JsonObject) {
        child = visitChildAsObject(arrayTypeInfo.getComponentType(), jsonChild);
      } else if (jsonChild instanceof JsonArray) {
        child = visitChildAsArray(arrayTypeInfo.getSecondLevelClass(), jsonChild.getAsJsonArray());
      } else if (jsonChild instanceof JsonPrimitive) {
        child = visitChildAsPrimitive(arrayTypeInfo.getComponentType(),
            jsonChild.getAsJsonPrimitive());
      } else {
        throw new IllegalStateException();
      }
      Array.set(array, i, child);
    }
  }

  @SuppressWarnings("unchecked")
  public void visitCollection(Collection collection, Type collectionType) {
    TypeInfo<Object> childTypeInfo = new TypeInfo<Object>(collectionType);
    Type childType = childTypeInfo.getGenericClass();
    for (JsonElement jsonChild : json.getAsJsonArray()) {
      if (childType == Object.class) {
        throw new JsonParseException(collection +
            " must not be a raw collection. Try making it genericized instead.");
      }
      Object child = visitChild(childType, jsonChild);
      collection.add(child);
    }
  }

  @SuppressWarnings("unchecked")
  public void visitPrimitiveValue(Object obj) {
    target = (T) typeAdapter.adaptType(json.getAsJsonArray().get(0).getAsObject(), componentType);
  }

  // We should not implement any other method from Visitor interface since
  // all other methods should be invoked on JsonObjectDeserializationVisitor
  // instead.

  public void endVisitingObject(Object node) {
    throw new UnsupportedOperationException();
  }

  public void startVisitingObject(Object node) {
    throw new UnsupportedOperationException();
  }

  public void visitArrayField(Field f, Object obj) {
    throw new UnsupportedOperationException();
  }

  public void visitCollectionField(Field f, Object obj) {
    throw new UnsupportedOperationException();
  }

  public void visitObjectField(Field f, Object obj) {
    throw new UnsupportedOperationException();
  }

  public void visitPrimitiveField(Field f, Object obj) {
    throw new UnsupportedOperationException();
  }
}
