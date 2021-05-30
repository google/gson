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

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;

/**
 * Given a type T, looks for the annotation {@link JsonAdapter} and uses an instance of the
 * specified class as the default type adapter.
 *
 * @since 2.3
 */
public final class JsonAdapterAnnotationTypeAdapterFactory implements TypeAdapterFactory {
  private static class DummyTypeAdapterFactory implements TypeAdapterFactory {
    @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      throw new AssertionError("Factory should not be used");
    }
  }
  /**
   * Factory used for {@link TreeTypeAdapter}s created for a {@code @JsonAdapter}
   * on a class.
   */
  private static final TypeAdapterFactory DUMMY_FACTORY_CLASS = new DummyTypeAdapterFactory();
  /**
   * Factory used for {@link TreeTypeAdapter}s created for a {@code @JsonAdapter}
   * on a field.
   */
  private static final TypeAdapterFactory DUMMY_FACTORY_FIELD = new DummyTypeAdapterFactory();

  /**
   * Cache, mapping from a {@link TypeAdapterFactory} class to a corresponding
   * instance which was created by {@code this}. Only contains type adapter
   * factories specified for annotated fields. Allows reusing instances if an
   * annotation on another field specifies the same adapter factory.
   *
   * <p>Does not impose type restrictions on key type to not require casting.
   */
  private final ThreadLocal<Map<Class<?>, Object>> fieldAdapterFactoryCache = new ThreadLocal<Map<Class<?>, Object>>();

  private final ConstructorConstructor constructorConstructor;

  public JsonAdapterAnnotationTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> targetType) {
    Class<? super T> rawType = targetType.getRawType();
    JsonAdapter annotation = rawType.getAnnotation(JsonAdapter.class);
    if (annotation == null) {
      return null;
    }
    return (TypeAdapter<T>) getTypeAdapter(constructorConstructor, gson, targetType, annotation, false);
  }

  /**
   * Returns whether the non-{@code null} adapter factory was created by
   * this instance for an annotated field.
   */
  public boolean isFieldAdapterFactory(TypeAdapterFactory adapterFactory) {
    if (adapterFactory == DUMMY_FACTORY_FIELD) {
      return true;
    } else if (adapterFactory == DUMMY_FACTORY_CLASS) {
      return false;
    }

    Map<Class<?>, Object> cacheMap = fieldAdapterFactoryCache.get();
    if (cacheMap == null) {
      // ThreadLocal was initialized by get(), have to remove entry again
      fieldAdapterFactoryCache.remove();
      return false;
    } else {
      return cacheMap.get(adapterFactory.getClass()) == adapterFactory;
    }
  }

  public void clearCache() {
    fieldAdapterFactoryCache.remove();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" }) // Casts guarded by conditionals.
  TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor, Gson gson,
      TypeToken<?> type, JsonAdapter annotation, boolean forField) {
    Class<?> adapterClass = annotation.value();
    TypeToken<?> adapterTypeToken = TypeToken.get(adapterClass);
    Object instance = null;
    if (forField && TypeAdapterFactory.class.isAssignableFrom(adapterClass)) {
      Map<Class<?>, Object> cacheMap = fieldAdapterFactoryCache.get();
      if (cacheMap == null) {
        cacheMap = new HashMap<Class<?>, Object>();
        fieldAdapterFactoryCache.set(cacheMap);
      } else {
        instance = cacheMap.get(adapterClass);
      }

      if (instance == null) {
        instance = constructorConstructor.get(adapterTypeToken).construct();
        cacheMap.put(adapterClass, instance);
      }
    } else {
      instance = constructorConstructor.get(adapterTypeToken).construct();
    }

    TypeAdapter<?> typeAdapter;
    if (instance instanceof TypeAdapter) {
      typeAdapter = (TypeAdapter<?>) instance;
    } else if (instance instanceof TypeAdapterFactory) {
      typeAdapter = ((TypeAdapterFactory) instance).create(gson, type);
    } else if (instance instanceof JsonSerializer || instance instanceof JsonDeserializer) {
      JsonSerializer<?> serializer = instance instanceof JsonSerializer
          ? (JsonSerializer) instance
          : null;
      JsonDeserializer<?> deserializer = instance instanceof JsonDeserializer
          ? (JsonDeserializer) instance
          : null;

      TypeAdapterFactory dummyFactory = forField ? DUMMY_FACTORY_FIELD : DUMMY_FACTORY_CLASS;
      typeAdapter = new TreeTypeAdapter(serializer, deserializer, gson, type, dummyFactory);
    } else {
      throw new IllegalArgumentException("Invalid attempt to bind an instance of "
          + instance.getClass().getName() + " as a @JsonAdapter for " + type.toString()
          + ". @JsonAdapter value must be a TypeAdapter, TypeAdapterFactory,"
          + " JsonSerializer or JsonDeserializer.");
    }

    if (typeAdapter != null && annotation.nullSafe()) {
      typeAdapter = typeAdapter.nullSafe();
    }

    return typeAdapter;
  }
}
