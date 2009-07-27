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

/**
 * A visitor that populates fields of an object with data from its equivalent
 * JSON representation
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class JsonArrayDeserializationVisitor<T> extends JsonDeserializationVisitor<T> {

  JsonArrayDeserializationVisitor(JsonArray jsonArray, Type arrayType,
      ObjectNavigatorFactory factory, ObjectConstructor objectConstructor,
      ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers,
      JsonDeserializationContext context) {
    super(jsonArray, arrayType, factory, objectConstructor, deserializers, context);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected T constructTarget() {

    TypeInfo typeInfo = new TypeInfo(targetType);

    if (!json.isJsonArray()) {
      throw new JsonParseException("Expecting array found: " + json); 
    }
    JsonArray jsonArray = json.getAsJsonArray();
    if (typeInfo.isArray()) {
      TypeInfoArray arrayTypeInfo = TypeInfoFactory.getTypeInfoForArray(targetType);
      // We know that we are getting back an array of the required type, so
      // this typecasting is safe.
      return (T) objectConstructor.constructArray(arrayTypeInfo.getSecondLevelType(),
          jsonArray.size());
    } else { // is a collection
      return (T) objectConstructor.construct(typeInfo.getRawClass());
    }
  }

  public void visitArray(Object array, Type arrayType) {
    if (!json.isJsonArray()) {
      throw new JsonParseException("Expecting array found: " + json); 
    }
    JsonArray jsonArray = json.getAsJsonArray();
    TypeInfoArray arrayTypeInfo = TypeInfoFactory.getTypeInfoForArray(arrayType);
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonElement jsonChild = jsonArray.get(i);
      Object child;

      if (jsonChild == null || jsonChild.isJsonNull()) {
        child = null;
      } else if (jsonChild instanceof JsonObject) {
        child = visitChildAsObject(arrayTypeInfo.getComponentRawType(), jsonChild);
      } else if (jsonChild instanceof JsonArray) {
        child = visitChildAsArray(arrayTypeInfo.getSecondLevelType(), jsonChild.getAsJsonArray());
      } else if (jsonChild instanceof JsonPrimitive) {
        child = visitChildAsObject(arrayTypeInfo.getComponentRawType(),
            jsonChild.getAsJsonPrimitive());
      } else {
        throw new IllegalStateException();
      }
      Array.set(array, i, child);
    }
  }

  // We should not implement any other method from Visitor interface since
  // all other methods should be invoked on JsonObjectDeserializationVisitor
  // instead.

  public void startVisitingObject(Object node) {
    throw new JsonParseException("Expecting array but found object: " + node);
  }

  public void visitArrayField(Field f, Type typeOfF, Object obj) {
    throw new JsonParseException("Expecting array but found array field " + f.getName() + ": "
        + obj);
  }

  public void visitObjectField(Field f, Type typeOfF, Object obj) {
    throw new JsonParseException("Expecting array but found object field " + f.getName() + ": " 
        + obj);
  }

  public boolean visitFieldUsingCustomHandler(Field f, Type actualTypeOfField, Object parent) {
    throw new JsonParseException("Expecting array but found field " + f.getName() + ": " 
        + parent);
  }

  public void visitPrimitive(Object primitive) {
    throw new JsonParseException(
        "Type information is unavailable, and the target is not a primitive: " + json);
  }
}
