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
  private final MemoryRefStack ancestors;
  private JsonElement root;

  JsonSerializationVisitor(ObjectNavigatorFactory factory, boolean serializeNulls,
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers, JsonSerializationContext context,
      MemoryRefStack ancestors) {
    this.factory = factory;
    this.serializeNulls = serializeNulls;
    this.serializers = serializers;
    this.context = context;
    this.ancestors = ancestors;
  }

  public Object getTarget() {
    return null;
  }

  public void start(ObjectTypePair node) {
    if (node == null) {
      return;
    }
    if (ancestors.contains(node)) {
      throw new CircularReferenceException(node);
    }
    ancestors.push(node);
  }

  public void end(ObjectTypePair node) {
    if (node != null) {
      ancestors.pop();
    }
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
      Type childType = componentType;
      // we should not get more specific component type yet since it is possible
      // that a custom
      // serializer is registered for the componentType
      addAsArrayElement(new ObjectTypePair(child, childType, false));
    }
  }

  public void visitArrayField(Field f, Type typeOfF, Object obj) {
    try {
      if (isFieldNull(f, obj)) {
        if (serializeNulls) {
          addChildAsElement(f, JsonNull.createJsonNull());
        }
      } else {
        Object array = getFieldValue(f, obj);
        addAsChildOfObject(f, new ObjectTypePair(array, typeOfF, false));
      }
    } catch (CircularReferenceException e) {
      throw e.createDetailedException(f);
    }
  }

  public void visitObjectField(Field f, Type typeOfF, Object obj) {
    try {
      if (isFieldNull(f, obj)) {
        if (serializeNulls) {
          addChildAsElement(f, JsonNull.createJsonNull());
        }
      } else {
        Object fieldValue = getFieldValue(f, obj);
        // we should not get more specific component type yet since it is
        // possible that a custom
        // serializer is registered for the componentType
        addAsChildOfObject(f, new ObjectTypePair(fieldValue, typeOfF, false));
      }
    } catch (CircularReferenceException e) {
      throw e.createDetailedException(f);
    }
  }

  public void visitPrimitive(Object obj) {
    JsonElement json = obj == null ? JsonNull.createJsonNull() : new JsonPrimitive(obj);
    assignToRoot(json);
  }

  private void addAsChildOfObject(Field f, ObjectTypePair fieldValuePair) {
    JsonElement childElement = getJsonElementForChild(fieldValuePair);
    addChildAsElement(f, childElement);
  }

  private void addChildAsElement(Field f, JsonElement childElement) {
    FieldNamingStrategy namingPolicy = factory.getFieldNamingPolicy();
    root.getAsJsonObject().add(namingPolicy.translateName(f), childElement);
  }

  private void addAsArrayElement(ObjectTypePair elementTypePair) {
    if (elementTypePair.getObject() == null) {
      root.getAsJsonArray().add(JsonNull.createJsonNull());
    } else {
      JsonElement childElement = getJsonElementForChild(elementTypePair);
      root.getAsJsonArray().add(childElement);
    }
  }

  private JsonElement getJsonElementForChild(ObjectTypePair fieldValueTypePair) {
    ObjectNavigator on = factory.create(fieldValueTypePair);
    JsonSerializationVisitor childVisitor =
        new JsonSerializationVisitor(factory, serializeNulls, serializers, context, ancestors);
    on.accept(childVisitor);
    return childVisitor.getJsonElement();
  }

  public boolean visitUsingCustomHandler(ObjectTypePair objTypePair) {
    try {
      Object obj = objTypePair.getObject();
      if (obj == null) {
        if (serializeNulls) {
          assignToRoot(JsonNull.createJsonNull());
        }
        return true;
      }
      JsonElement element = findAndInvokeCustomSerializer(objTypePair);
      if (element != null) {
        assignToRoot(element);
        return true;
      } else {
        return false;
      }
    } catch (CircularReferenceException e) {
      throw e.createDetailedException(null);
    }
  }

  /**
   * objTypePair.getObject() must not be null
   */
  @SuppressWarnings("unchecked")
  private JsonElement findAndInvokeCustomSerializer(ObjectTypePair objTypePair) {
    Pair<JsonSerializer<?>,ObjectTypePair> pair = objTypePair.getMatchingHandler(serializers);
    if (pair == null) {
      return null;
    }
    JsonSerializer serializer = pair.getFirst();
    objTypePair = pair.getSecond();
    start(objTypePair);
    try {
      JsonElement element =
          serializer.serialize(objTypePair.getObject(), objTypePair.getType(), context);
      return element == null ? JsonNull.createJsonNull() : element;
    } finally {
      end(objTypePair);
    }
  }

  public boolean visitFieldUsingCustomHandler(Field f, Type declaredTypeOfField, Object parent) {
    try {
      Preconditions.checkState(root.isJsonObject());
      Object obj = f.get(parent);
      if (obj == null) {
        if (serializeNulls) {
          addChildAsElement(f, JsonNull.createJsonNull());
        }
        return true;
      }
      ObjectTypePair objTypePair = new ObjectTypePair(obj, declaredTypeOfField, false);
      JsonElement child = findAndInvokeCustomSerializer(objTypePair);
      if (child != null) {
        addChildAsElement(f, child);
        return true;
      } else {
        return false;
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException();
    } catch (CircularReferenceException e) {
      throw e.createDetailedException(f);
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
