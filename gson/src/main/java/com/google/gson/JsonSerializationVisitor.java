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
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers,
      JsonSerializationContext context, MemoryRefStack ancestors) {
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
      if (child != null) {
        childType = getActualTypeIfMoreSpecific(childType, child.getClass());
      }
      addAsArrayElement(new ObjectTypePair(child, childType));
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
        addAsChildOfObject(f, new ObjectTypePair(array, typeOfF));
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
        if (fieldValue != null) {
          typeOfF = getActualTypeIfMoreSpecific(typeOfF, fieldValue.getClass());
        }
        addAsChildOfObject(f, new ObjectTypePair(fieldValue, typeOfF));
      }
    } catch (CircularReferenceException e) {
      throw e.createDetailedException(f);
    }
  }

  // This takes care of situations where the field was declared as an Object, but the
  // actual value contains something more specific. See Issue 54.
  // TODO (inder): This solution will not work if the field is of a generic type, but 
  // the actual object is of a raw type (which is a sub-class of the generic type).
  private Type getActualTypeIfMoreSpecific(Type type, Class<?> actualClass) {
    if (type instanceof Class<?>) {
      Class<?> typeAsClass = (Class<?>) type;
      if (typeAsClass.isAssignableFrom(actualClass)) {
        type = actualClass;
      }
      if (type == Object.class) {
        type = actualClass;
      } 
    }
    return type;
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

  @SuppressWarnings("unchecked")
  public boolean visitUsingCustomHandler(ObjectTypePair objTypePair) {
    try {
      Object obj = objTypePair.getObject();
      Type objType = objTypePair.getType();
      JsonSerializer serializer = serializers.getHandlerFor(objType);
      if (serializer == null && obj != null) {
        serializer = serializers.getHandlerFor(obj.getClass());
      }

      if (serializer != null) {
        if (obj == null) {
          assignToRoot(JsonNull.createJsonNull());
        } else {
          assignToRoot(invokeCustomHandler(objTypePair, serializer));
        }
        return true;
      }
      return false;
    } catch (CircularReferenceException e) {
      throw e.createDetailedException(null);
    }
  }

  @SuppressWarnings("unchecked")
  private JsonElement invokeCustomHandler(ObjectTypePair objTypePair, JsonSerializer serializer) {
    start(objTypePair);
    try {
      return serializer.serialize(objTypePair.getObject(), objTypePair.getType(), context);
    } finally {
      end(objTypePair);
    }
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
        ObjectTypePair objTypePair = new ObjectTypePair(obj, actualTypeOfField);
        JsonElement child = invokeCustomHandler(objTypePair, serializer);
        addChildAsElement(f, child);
        return true;
      }
      return false;
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
