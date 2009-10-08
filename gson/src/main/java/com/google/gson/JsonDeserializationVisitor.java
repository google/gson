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

/**
 * Abstract data value container for the {@link ObjectNavigator.Visitor}
 * implementations.  This class exposes the {@link #getTarget()} method
 * which returns the class that was visited by this object.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
abstract class JsonDeserializationVisitor<T> implements ObjectNavigator.Visitor {

  protected final ObjectNavigatorFactory factory;
  protected final ObjectConstructor objectConstructor;
  protected final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers;
  protected T target;
  protected final JsonElement json;
  protected final Type targetType;
  protected final JsonDeserializationContext context;

  public JsonDeserializationVisitor(JsonElement json, Type targetType,
      ObjectNavigatorFactory factory, ObjectConstructor objectConstructor,
      ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers,
      JsonDeserializationContext context) {
    Preconditions.checkNotNull(json);
    this.targetType = targetType;
    this.factory = factory;
    this.objectConstructor = objectConstructor;
    this.deserializers = deserializers;
    this.json = json;
    this.context = context;
  }

  public T getTarget() {
    if (target == null) {
      target = constructTarget();
    }
    return target;
  }

  protected abstract T constructTarget();

  public void start(ObjectTypePair node) {
  }

  public void end(ObjectTypePair node) {
  }

  @SuppressWarnings("unchecked")
  public final boolean visitUsingCustomHandler(ObjectTypePair objTypePair) {
    Type objType = objTypePair.getType();
    JsonDeserializer deserializer = deserializers.getHandlerFor(objType);
    if (deserializer != null) {
      if (json != null && !json.isJsonNull()) {
        target = (T) deserializer.deserialize(json, objType, context);
      }
      return true;
    }
    return false;
  }

  final Object visitChildAsObject(Type childType, JsonElement jsonChild) {
    JsonDeserializationVisitor<?> childVisitor =
        new JsonObjectDeserializationVisitor<Object>(jsonChild, childType,
            factory, objectConstructor, deserializers, context);
    return visitChild(childType, childVisitor);
  }

  final Object visitChildAsArray(Type childType, JsonArray jsonChild) {
    JsonDeserializationVisitor<?> childVisitor =
        new JsonArrayDeserializationVisitor<Object>(jsonChild.getAsJsonArray(), childType,
            factory, objectConstructor, deserializers, context);
    return visitChild(childType, childVisitor);
  }

  private Object visitChild(Type type, JsonDeserializationVisitor<?> childVisitor) {
    ObjectNavigator on = factory.create(new ObjectTypePair(null, type, true));
    on.accept(childVisitor);
    // the underlying object may have changed during the construction phase
    // This happens primarily because of custom deserializers
    return childVisitor.getTarget();
  }

  public ObjectTypePair getActualTypeIfMoreSpecific(ObjectTypePair objTypePair) {
    return objTypePair;
  }
}
