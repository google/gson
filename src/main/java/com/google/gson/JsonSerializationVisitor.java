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
import java.util.Map;

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

  public void endVisitingObject(Object node) {
    // nothing to be done here
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

  @SuppressWarnings("unchecked")
  public void visitCollection(Collection collection, Type collectionType) {
    assignToRoot(new JsonArray());
    for (Object child : collection) {
      Type childType = TypeUtils.getActualTypeForFirstTypeVariable(collectionType);
      if (childType == Object.class && child != null) {
        // Try our luck some other way
        childType = child.getClass();
      }
      addAsArrayElement(childType, child);
    }
  }

  public void visitArrayField(Field f, Type typeOfF, Object obj) {
    if (isFieldNull(f, obj)) {
      if (serializeNulls) {
        addChildAsElement(f, new JsonNull());
      }
    } else {
      Object array = getFieldValue(f, obj);
      addAsChildOfObject(f, typeOfF, array);
    }
  }

  public void visitCollectionField(Field f, Type typeOfF, Object obj) {
    if (isFieldNull(f, obj)) {
      if (serializeNulls) {
        addChildAsElement(f, new JsonNull());
      }
    } else {
      if (typeOfF == null) {
        throw new RuntimeException("Can not handle non-generic collections");
      }
      Object collection = getFieldValue(f, obj);
      addAsChildOfObject(f, typeOfF, collection);
    }
  }

  @SuppressWarnings("unchecked")
  public void visitEnum(Object obj, Type objType) {
    JsonSerializer serializer = serializers.getHandlerFor(objType);
    if (serializer == null) {
      serializer = serializers.getHandlerFor(Enum.class);
    }
    if (serializer == null) {
      throw new RuntimeException("Register a JsonSerializer for Enum or "
          + obj.getClass().getName());
    }
    assignToRoot(serializer.serialize(obj, objType, context));
  }

  public void visitObjectField(Field f, Type typeOfF, Object obj) {
    if (isFieldNull(f, obj)) {
      if (serializeNulls) {
        addChildAsElement(f, new JsonNull());
      }
    } else {
      Object fieldValue = getFieldValue(f, obj);
      // See if the fieldValue has better type information than the specified typeOfF
      // This takes care of situations where the field was declared as an Object, but the
      // actual value contains something more specific. See Issue 54.
      if (fieldValue != null && typeOfF instanceof Class) {
        Class<?> classOfF = (Class<?>) typeOfF;
        Class<?> actualClassOfF = fieldValue.getClass();
        if (classOfF.isAssignableFrom(actualClassOfF)) {
          typeOfF = actualClassOfF;
        }
      }
      addAsChildOfObject(f, typeOfF, fieldValue);
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
      addNullAsArrayElement();
    } else {
      JsonElement childElement = getJsonElementForChild(elementType, elementValue);
      root.getAsJsonArray().add(childElement);
    }
  }

  private void addNullAsArrayElement() {
    root.getAsJsonArray().add(null);
  }

  private JsonElement getJsonElementForChild(Type fieldType, Object fieldValue) {
    ObjectNavigator on = factory.create(fieldValue, fieldType);
    JsonSerializationVisitor childVisitor =
        new JsonSerializationVisitor(factory, serializeNulls, serializers, context);
    on.accept(childVisitor);
    return childVisitor.getJsonElement();
  }

  public void visitPrimitiveField(Field f, Type typeOfF, Object obj) {    
    if (isFieldNull(f, obj)) {
      if (serializeNulls) {
        addChildAsElement(f, new JsonNull());
      }      
    } else {
      TypeInfo typeInfo = new TypeInfo(typeOfF);
      if (typeInfo.isPrimitiveOrStringAndNotAnArray()) {
        Object fieldValue = getFieldValue(f, obj);
        addAsChildOfObject(f, typeOfF, fieldValue);
      } else {
        throw new IllegalArgumentException("Not a primitive type");
      }
    }
  }

  public void visitPrimitiveValue(Object obj) {
    assignToRoot(new JsonPrimitive(obj));
  }

  @SuppressWarnings("unchecked")
  public boolean visitUsingCustomHandler(Object obj, Type objType) {
    JsonSerializer serializer = serializers.getHandlerFor(objType);
    if (serializer == null && obj instanceof Map) {
      serializer = serializers.getHandlerFor(Map.class);
    }
    if (serializer != null) {
      assignToRoot(serializer.serialize(obj, objType, context));
      return true;
    }
    return false;
  }

  private void assignToRoot(JsonElement newRoot) {
    Preconditions.checkArgument(root == null);
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
