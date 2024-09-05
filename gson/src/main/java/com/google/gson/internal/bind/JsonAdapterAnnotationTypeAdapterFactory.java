/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Given a type T, looks for the annotation {@link JsonAdapter} and uses an instance of the
 * specified class as the default type adapter.
 *
 * @since 2.3
 */
public final class JsonAdapterAnnotationTypeAdapterFactory implements TypeAdapterFactory {
  private static class DummyTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      throw new AssertionError("Factory should not be used");
    }
  }

  /** Factory used for {@link TreeTypeAdapter}s created for {@code @JsonAdapter} on a class. */
  private static final TypeAdapterFactory TREE_TYPE_CLASS_DUMMY_FACTORY =
      new DummyTypeAdapterFactory();

  /** Factory used for {@link TreeTypeAdapter}s created for {@code @JsonAdapter} on a field. */
  private static final TypeAdapterFactory TREE_TYPE_FIELD_DUMMY_FACTORY =
      new DummyTypeAdapterFactory();

  private final ConstructorConstructor constructorConstructor;

  /**
   * For a class, if it is annotated with {@code @JsonAdapter} and refers to a {@link
   * TypeAdapterFactory}, stores the factory instance in case it has been requested already. Has to
   * be a {@link ConcurrentMap} because {@link Gson} guarantees to be thread-safe.
   */
  // Note: In case these strong reference to TypeAdapterFactory instances are considered
  // a memory leak in the future, could consider switching to WeakReference<TypeAdapterFactory>
  private final ConcurrentMap<Class<?>, TypeAdapterFactory> adapterFactoryMap;

  public JsonAdapterAnnotationTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
    this.adapterFactoryMap = new ConcurrentHashMap<>();
  }

  // Separate helper method to make sure callers retrieve annotation in a consistent way
  private static JsonAdapter getAnnotation(Class<?> rawType) {
    return rawType.getAnnotation(JsonAdapter.class);
  }

  // this is not safe; requires that user has specified correct adapter class for @JsonAdapter
  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> targetType) {
    Class<? super T> rawType = targetType.getRawType();
    JsonAdapter annotation = getAnnotation(rawType);
    if (annotation == null) {
      return null;
    }
    return (TypeAdapter<T>)
        getTypeAdapter(constructorConstructor, gson, targetType, annotation, true);
  }

  // Separate helper method to make sure callers create adapter in a consistent way
  private static Object createAdapter(
      ConstructorConstructor constructorConstructor, Class<?> adapterClass) {
    // TODO: The exception messages created by ConstructorConstructor are currently written in the
    // context of deserialization and for example suggest usage of TypeAdapter, which would not work
    // for @JsonAdapter usage
    return constructorConstructor.get(TypeToken.get(adapterClass)).construct();
  }

  private TypeAdapterFactory putFactoryAndGetCurrent(Class<?> rawType, TypeAdapterFactory factory) {
    // Uses putIfAbsent in case multiple threads concurrently create factory
    TypeAdapterFactory existingFactory = adapterFactoryMap.putIfAbsent(rawType, factory);
    return existingFactory != null ? existingFactory : factory;
  }

  TypeAdapter<?> getTypeAdapter(
      ConstructorConstructor constructorConstructor,
      Gson gson,
      TypeToken<?> type,
      JsonAdapter annotation,
      boolean isClassAnnotation) {
    Object instance = createAdapter(constructorConstructor, annotation.value());

    TypeAdapter<?> typeAdapter;
    boolean nullSafe = annotation.nullSafe();
    if (instance instanceof TypeAdapter) {
      typeAdapter = (TypeAdapter<?>) instance;
    } else if (instance instanceof TypeAdapterFactory) {
      TypeAdapterFactory factory = (TypeAdapterFactory) instance;

      if (isClassAnnotation) {
        factory = putFactoryAndGetCurrent(type.getRawType(), factory);
      }

      typeAdapter = factory.create(gson, type);
    } else if (instance instanceof JsonSerializer || instance instanceof JsonDeserializer) {
      JsonSerializer<?> serializer =
          instance instanceof JsonSerializer ? (JsonSerializer<?>) instance : null;
      JsonDeserializer<?> deserializer =
          instance instanceof JsonDeserializer ? (JsonDeserializer<?>) instance : null;

      // Uses dummy factory instances because TreeTypeAdapter needs a 'skipPast' factory for
      // `Gson.getDelegateAdapter` call and has to differentiate there whether TreeTypeAdapter was
      // created for @JsonAdapter on class or field
      TypeAdapterFactory skipPast;
      if (isClassAnnotation) {
        skipPast = TREE_TYPE_CLASS_DUMMY_FACTORY;
      } else {
        skipPast = TREE_TYPE_FIELD_DUMMY_FACTORY;
      }
      @SuppressWarnings({"unchecked", "rawtypes"})
      TypeAdapter<?> tempAdapter =
          new TreeTypeAdapter(serializer, deserializer, gson, type, skipPast, nullSafe);
      typeAdapter = tempAdapter;

      // TreeTypeAdapter handles nullSafe; don't additionally call `nullSafe()`
      nullSafe = false;
    } else {
      throw new IllegalArgumentException(
          "Invalid attempt to bind an instance of "
              + instance.getClass().getName()
              + " as a @JsonAdapter for "
              + type.toString()
              + ". @JsonAdapter value must be a TypeAdapter, TypeAdapterFactory,"
              + " JsonSerializer or JsonDeserializer.");
    }

    if (typeAdapter != null && nullSafe) {
      typeAdapter = typeAdapter.nullSafe();
    }

    return typeAdapter;
  }

  /**
   * Returns whether {@code factory} is a type adapter factory created for {@code @JsonAdapter}
   * placed on {@code type}.
   */
  public boolean isClassJsonAdapterFactory(TypeToken<?> type, TypeAdapterFactory factory) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(factory);

    if (factory == TREE_TYPE_CLASS_DUMMY_FACTORY) {
      return true;
    }

    // Using raw type to match behavior of `create(Gson, TypeToken<T>)` above
    Class<?> rawType = type.getRawType();

    TypeAdapterFactory existingFactory = adapterFactoryMap.get(rawType);
    if (existingFactory != null) {
      // Checks for reference equality, like it is done by `Gson.getDelegateAdapter`
      return existingFactory == factory;
    }

    // If no factory has been created for the type yet check manually for a @JsonAdapter annotation
    // which specifies a TypeAdapterFactory
    // Otherwise behavior would not be consistent, depending on whether or not adapter had been
    // requested before call to `isClassJsonAdapterFactory` was made
    JsonAdapter annotation = getAnnotation(rawType);
    if (annotation == null) {
      return false;
    }

    Class<?> adapterClass = annotation.value();
    if (!TypeAdapterFactory.class.isAssignableFrom(adapterClass)) {
      return false;
    }

    Object adapter = createAdapter(constructorConstructor, adapterClass);
    TypeAdapterFactory newFactory = (TypeAdapterFactory) adapter;

    return putFactoryAndGetCurrent(rawType, newFactory) == factory;
  }
}
