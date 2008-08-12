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
import java.util.logging.Logger;

/**
 * Abstract data value container for the {@link ObjectNavigator.Visitor}
 * implementations.  This class exposes the {@link #getTarget()} method
 * which returns the class that was visited by this object.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
abstract class JsonDeserializationVisitor<T> implements ObjectNavigator.Visitor {

  protected static Logger logger = Logger.getLogger(JsonDeserializationVisitor.class.getName());

  protected final ObjectNavigatorFactory factory;
  protected final ObjectConstructor objectConstructor;
  protected final TypeAdapter typeAdapter;
  protected final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers;
  protected T target;
  protected final JsonElement json;
  private final JsonDeserializationContext context;

  public JsonDeserializationVisitor(JsonElement json, ObjectNavigatorFactory factory,
      ObjectConstructor objectConstructor, TypeAdapter typeAdapter,
      ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers,
      JsonDeserializationContext context) {
    Preconditions.checkNotNull(json);
    this.factory = factory;
    this.objectConstructor = objectConstructor;
    this.typeAdapter = typeAdapter;
    this.deserializers = deserializers;
    this.json = json;
    this.context = context;
  }

  T getTarget() {
    return target;
  }

  @SuppressWarnings("unchecked")
  public final void visitEnum(Object obj, Type objType) {
    JsonDeserializer<T> deserializer = (JsonDeserializer<T>) deserializers.getHandlerFor(objType);
    if (deserializer == null) {
      deserializer =  (JsonDeserializer<T>) deserializers.getHandlerFor(Enum.class);
    }
    if (deserializer == null) {
      throw new RuntimeException("Register a JsonDeserializer for Enum or "
          + obj.getClass().getName());
    }
    target = deserializer.deserialize(json, objType, context);
  }

  @SuppressWarnings("unchecked")
  public final boolean visitUsingCustomHandler(Object obj, Type objType) {
    JsonDeserializer<T> deserializer = (JsonDeserializer<T>) deserializers.getHandlerFor(objType);
    if (deserializer != null) {
      target = deserializer.deserialize(json, objType, context);
      return true;
    }
    return false;
  }

  final Object visitChildAsObject(Type childType, JsonElement jsonChild) {
    JsonDeserializationVisitor<?> childVisitor =
      new JsonObjectDeserializationVisitor<Object>(jsonChild, childType,
          factory, objectConstructor, typeAdapter, deserializers, context);
    return visitChild(childType, childVisitor);
  }

  final Object visitChildAsArray(Type childType, JsonArray jsonChild) {
    JsonDeserializationVisitor<?> childVisitor =
      new JsonArrayDeserializationVisitor<Object>(jsonChild.getAsJsonArray(), childType,
          factory, objectConstructor, typeAdapter, deserializers, context);
    return visitChild(childType, childVisitor);
  }

  final Object visitChildAsPrimitive(Type childType, JsonPrimitive jsonChild) {
    Preconditions.checkNotNull(jsonChild);
    Class<?> childClass;
    if (childType instanceof Class) {
      childClass = (Class<?>) childType;
    } else {
      TypeInfo childTypeInfo = new TypeInfo(childType);
      Type genericClass = childTypeInfo.getGenericClass();
      childClass = new TypeInfo(genericClass).getTopLevelClass();
    }
    return typeAdapter.adaptType(jsonChild.getAsObject(), childClass);
  }

  final Object visitChild(Type childType, JsonElement jsonChild) {
    if (jsonChild == null) {
      return null;
    } else if (jsonChild instanceof JsonArray) {
      return visitChildAsArray(childType, jsonChild.getAsJsonArray());
    } else if (jsonChild instanceof JsonObject) {
      return visitChildAsObject(childType, jsonChild);
    } else if (jsonChild instanceof JsonPrimitive) {
      return visitChildAsPrimitive(childType, jsonChild.getAsJsonPrimitive());
    } else {
      throw new IllegalStateException();
    }
  }

  private Object visitChild(Type type, JsonDeserializationVisitor<?> childVisitor) {
    Object child = childVisitor.getTarget();
    ObjectNavigator on = factory.create(child, type);
    on.accept(childVisitor);
    // the underlying object may have changed during the construction phase
    // This happens primarily because of custom deserializers
    return childVisitor.getTarget();
  }
}
