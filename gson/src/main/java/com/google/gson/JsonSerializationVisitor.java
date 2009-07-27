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
 * A visitor that adds JSON elements corresponding to each field of an object
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class JsonSerializationVisitor implements ObjectNavigator.Visitor {

  private final ObjectNavigatorFactory factory;
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;
  private final boolean serializeNulls;

  private final JsonSerializationContext context;

  private JsonElement root;

  JsonSerializationVisitor(ObjectNavigatorFactory factory, boolean serializeNulls,
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers,
      JsonSerializationContext context) {
    this.factory = factory;
    this.serializeNulls = serializeNulls;
    this.serializers = serializers;
    this.context = context;
  }
  
  public Object getTarget() {
    return null;
  }

  public void startVisitingObject(Object node) {
    assignToRoot(new JsonObject());
  }

  public void visitArray(Object array, Type arrayType) {
    assignToRoot(new JsonArray());
    int length = Array.getLength(array);
    TypeInfoArray fieldTypeInfo = TypeInfoFactory.getTypeInfoForArray(arrayType);
    Type componentType = fieldTypeInfo.getSecondLevelType();
    for (int i = 0; i < length; ++i) {
      Object child = Array.get(array, i);
      addAsArrayElement(componentType, child);
    }
  }

  public void visitArrayField(Field f, Type typeOfF, Object obj) {
    if (isFieldNull(f, obj)) {
      if (serializeNulls) {
        addChildAsElement(f, JsonNull.createJsonNull());
      }
    } else {
      Object array = getFieldValue(f, obj);
      addAsChildOfObject(f, typeOfF, array);
    }
  }

  public void visitObjectField(Field f, Type typeOfF, Object obj) {
    if (isFieldNull(f, obj)) {
      if (serializeNulls) {
        addChildAsElement(f, JsonNull.createJsonNull());
      }
    } else {
      Object fieldValue = getFieldValue(f, obj);
      // This takes care of situations where the field was declared as an Object, but the
      // actual value contains something more specific. See Issue 54.
      if (fieldValue != null && typeOfF == Object.class) {
        typeOfF = fieldValue.getClass();
      }
      addAsChildOfObject(f, typeOfF, fieldValue);
    }
  }

  public void visitPrimitive(Object obj) {
    if (obj != null) {
      JsonElement json = new JsonPrimitive(obj);
      assignToRoot(json);
    }
  }

  private void addAsChildOfObject(Field f, Type fieldType, Object fieldValue) {
    JsonElement childElement = getJsonElementForChild(fieldType, fieldValue);
    addChildAsElement(f, childElement);
  }

  private void addChildAsElement(Field f, JsonElement childElement) {
    FieldNamingStrategy namingPolicy = factory.getFieldNamingPolicy();
    root.getAsJsonObject().add(namingPolicy.translateName(f), childElement);
  }

  private void addAsArrayElement(Type elementType, Object elementValue) {
    if (elementValue == null) {
      root.getAsJsonArray().add(JsonNull.createJsonNull());
    } else {
      JsonElement childElement = getJsonElementForChild(elementType, elementValue);
      root.getAsJsonArray().add(childElement);
    }
  }

  private JsonElement getJsonElementForChild(Type fieldType, Object fieldValue) {
    ObjectNavigator on = factory.create(fieldValue, fieldType);
    JsonSerializationVisitor childVisitor =
        new JsonSerializationVisitor(factory, serializeNulls, serializers, context);
    on.accept(childVisitor);
    return childVisitor.getJsonElement();
  }

  @SuppressWarnings("unchecked")
  public boolean visitUsingCustomHandler(Object obj, Type objType) {
    JsonSerializer serializer = serializers.getHandlerFor(objType);
    if (serializer != null) {
      if (obj == null) {
        assignToRoot(JsonNull.createJsonNull());
      } else {
        assignToRoot(serializer.serialize(obj, objType, context));
      }
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public boolean visitFieldUsingCustomHandler(Field f, Type actualTypeOfField, Object parent) {
    try {
      Preconditions.checkState(root.isJsonObject());
      Object obj = f.get(parent);
      if (obj == null) {
        if (serializeNulls) {
          addChildAsElement(f, JsonNull.createJsonNull());
        }
        return true;
      }
      JsonSerializer serializer = serializers.getHandlerFor(actualTypeOfField);
      if (serializer != null) {
        JsonElement child = serializer.serialize(obj, actualTypeOfField, context);
        addChildAsElement(f, child);
        return true;
      }
      return false;
    } catch (IllegalAccessException e) {
      throw new RuntimeException();
    }
  }

  private void assignToRoot(JsonElement newRoot) {
    Preconditions.checkNotNull(newRoot);
    root = newRoot;
  }

  private boolean isFieldNull(Field f, Object obj) {
    return getFieldValue(f, obj) == null;
  }

  private Object getFieldValue(Field f, Object obj) {
    try {
      return f.get(obj);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonElement getJsonElement() {
    return root;
  }
}
