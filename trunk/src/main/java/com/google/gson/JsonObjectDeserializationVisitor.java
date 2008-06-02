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
 * A visitor that populates fields of an object with data from its equivalent
 * JSON representation
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class JsonObjectDeserializationVisitor<T> extends JsonDeserializationVisitor<T> {

  @SuppressWarnings("unchecked")
  JsonObjectDeserializationVisitor(JsonElement json, Type type,
      ObjectNavigatorFactory factory, ObjectConstructor objectConstructor,
      TypeAdapter typeAdapter, ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers,
      JsonDeserializationContext context) {
    super(json, factory, objectConstructor, typeAdapter, deserializers, context);
    this.target = (T) objectConstructor.construct(type);
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

  public void visitPrimitiveValue(Object obj) {
    // should not be called since this case should invoke JsonPrimitiveDeserializationVisitor
    throw new IllegalStateException();
  }

  public void visitObjectField(Field f, Object obj) {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      String fName = f.getName();
      if (jsonObject.has(fName)) {
        JsonElement jsonChild = jsonObject.get(fName);
        Type fieldType = f.getGenericType();
        Object child = visitChildAsObject(fieldType, jsonChild);
        f.set(obj, child);
      } else {
        f.set(obj, null);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public void visitCollectionField(Field f, Object obj) {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      String fName = f.getName();
      if (jsonObject.has(fName)) {
        Collection collection = (Collection) objectConstructor.construct(f.getType());
        f.set(obj, collection);
        Type childType = new TypeInfo(f.getGenericType()).getGenericClass();
        JsonArray jsonArray = jsonObject.get(fName).getAsJsonArray();
        for (JsonElement jsonChild : jsonArray) {
          Object child = visitChild(childType, jsonChild);
          if (childType == Object.class) {
            throw new JsonParseException(fName +
                " can not be a raw collection. Try making it a genericized collection instead");
          }
          collection.add(child);
        }
      } else {
        f.set(obj, null);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitArrayField(Field f, Object obj) {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      String fName = f.getName();
      if (jsonObject.has(fName)) {
        JsonArray jsonChild = (JsonArray) jsonObject.get(fName);
        Object array = visitChildAsArray(f.getType(), jsonChild);
        f.set(obj, array);
      } else {
        f.set(obj, null);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitPrimitiveField(Field f, Object obj) {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      String fName = f.getName();
      if (jsonObject.has(fName)) {
        JsonPrimitive value = jsonObject.getAsJsonPrimitive(fName);
        if (value == null) {
          throw new JsonParseException("Primitive field " + fName + " must not be set to null");
        }
        f.set(obj, typeAdapter.adaptType(value.getAsObject(), f.getType()));
      } else {
        // For Strings, we need to set the field to null
        // For other primitive types, any value created during default construction is fine
        if (f.getType() == String.class) {
          f.set(obj, null);
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
