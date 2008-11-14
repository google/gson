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

  JsonObjectDeserializationVisitor(JsonElement json, Type type,
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

  public void visitArray(Object array, Type componentType) {
    // should not be called since this case should invoke JsonArrayDeserializationVisitor
    throw new IllegalStateException();
  }

  public void visitPrimitiveValue(Object obj) {
    // should not be called since this case should invoke JsonPrimitiveDeserializationVisitor
    throw new IllegalStateException();
  }

  public void visitObjectField(Field f, Type typeOfF, Object obj) {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      String fName = getFieldName(f);
      JsonElement jsonChild = jsonObject.get(fName);
      if (jsonChild != null) {
        Object child = visitChildAsObject(typeOfF, jsonChild);
        f.set(obj, child);
      } else {
        f.set(obj, null);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitArrayField(Field f, Type typeOfF, Object obj) {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      String fName = getFieldName(f);
      JsonArray jsonChild = (JsonArray) jsonObject.get(fName);
      if (jsonChild != null) {
        Object array = visitChildAsArray(typeOfF, jsonChild);
        f.set(obj, array);
      } else {
        f.set(obj, null);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitPrimitiveField(Field f, Type typeOfF, Object obj) {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      String fName = getFieldName(f);
      JsonPrimitive value = jsonObject.getAsJsonPrimitive(fName);
      if (value != null) {
        f.set(obj, typeAdapter.adaptType(value.getAsObject(), TypeUtils.toRawClass(typeOfF)));
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

  private String getFieldName(Field f) {
    FieldNamingStrategy namingPolicy = factory.getFieldNamingPolicy();
    return namingPolicy.translateName(f);
  }

  public boolean visitFieldUsingCustomHandler(Field f, Type actualTypeOfField, Object parent) {
    try {
      @SuppressWarnings("unchecked")
      JsonDeserializer deserializer = deserializers.getHandlerFor(actualTypeOfField);
      if (deserializer != null) {
        String fName = getFieldName(f);
        JsonElement child = json.getAsJsonObject().get(fName);
        if (child == null) {
          child = JsonNull.INSTANCE;
        }
        Object value = deserializer.deserialize(child, actualTypeOfField, context);
        f.set(parent, value);
        return true;
      }
      return false;
    } catch (IllegalAccessException e) {
      throw new RuntimeException();
    }
  }
}
