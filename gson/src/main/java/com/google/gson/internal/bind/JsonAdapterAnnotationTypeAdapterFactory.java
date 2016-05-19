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

  @SuppressWarnings("rawtypes")
  private final ThreadLocal<Map<Class, TypeAdapter>> activeJsonAdapterClasses = new ThreadLocal<Map<Class, TypeAdapter>>() {
    @Override protected Map<Class, TypeAdapter> initialValue() {
      // No need for a thread-safe map since we are using it in a single thread
      return new HashMap<Class, TypeAdapter>();
    }
  };
  @SuppressWarnings("rawtypes")
  private final ThreadLocal<Map<Class, TypeAdapterFactory>> activeJsonAdapterFactories = new ThreadLocal<Map<Class, TypeAdapterFactory>>() {
    @Override protected Map<Class, TypeAdapterFactory> initialValue() {
      // No need for a thread-safe map since we are using it in a single thread
      return new HashMap<Class, TypeAdapterFactory>();
    }
  };

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
    return (TypeAdapter<T>) getTypeAdapter(constructorConstructor, gson, targetType, annotation);
  }

  public <T> TypeAdapter<T> getDelegateAdapter(Gson gson, TypeAdapterFactory skipPast, TypeToken<T> targetType) {
    TypeAdapterFactory factory = getDelegateAdapterFactory(targetType);
    if (factory == skipPast) factory = null;
    return factory == null ? null: factory.create(gson, targetType);
  }

  public <T> TypeAdapterFactory getDelegateAdapterFactory(TypeToken<T> targetType) {
    Class<?> annotatedClass = targetType.getRawType();
    JsonAdapter annotation = annotatedClass.getAnnotation(JsonAdapter.class);
    if (annotation == null) {
      return null;
    }
    return getTypeAdapterFactory(annotation, constructorConstructor);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" }) // Casts guarded by conditionals.
  TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor, Gson gson,
      TypeToken<?> type, JsonAdapter annotation) {
    Class<?> value = annotation.value();
    boolean isTypeAdapter = TypeAdapter.class.isAssignableFrom(value);
    boolean isJsonSerializer = JsonSerializer.class.isAssignableFrom(value);
    boolean isJsonDeserializer = JsonDeserializer.class.isAssignableFrom(value);

    TypeAdapter<?> typeAdapter;
    if (isTypeAdapter || isJsonSerializer || isJsonDeserializer) {
      Map<Class, TypeAdapter> adapters = activeJsonAdapterClasses.get();
      typeAdapter = adapters.get(value);
      if (typeAdapter == null) {
        if (isTypeAdapter) {
          Class<TypeAdapter<?>> typeAdapterClass = (Class<TypeAdapter<?>>) value;
          typeAdapter = constructorConstructor.get(TypeToken.get(typeAdapterClass)).construct();
        } else if (isJsonSerializer || isJsonDeserializer) {
          JsonSerializer serializer = null;
          if (isJsonSerializer) {
            Class<JsonSerializer<?>> serializerClass = (Class<JsonSerializer<?>>) value;
            serializer = constructorConstructor.get(TypeToken.get(serializerClass)).construct();
          }
          JsonDeserializer deserializer = null;
          if (isJsonDeserializer) {
            Class<JsonDeserializer<?>> deserializerClass = (Class<JsonDeserializer<?>>) value;
            deserializer = constructorConstructor.get(TypeToken.get(deserializerClass)).construct();
          }
          typeAdapter = new TreeTypeAdapter(serializer, deserializer, gson, type, null);
        }
        adapters.put(value, typeAdapter);
      }
    } else if (TypeAdapterFactory.class.isAssignableFrom(value)) {
      TypeAdapterFactory factory = getTypeAdapterFactory(annotation, constructorConstructor);
      typeAdapter = factory == null ? null : factory.create(gson, type);
    } else {
      throw new IllegalArgumentException(
          "@JsonAdapter value must be TypeAdapter, TypeAdapterFactory, JsonSerializer or JsonDeserializer reference.");
    }
    if (typeAdapter != null) {
      typeAdapter = typeAdapter.nullSafe();
    }
    return typeAdapter;
  }


  @SuppressWarnings({ "unchecked", "rawtypes" }) // Casts guarded by conditionals.
  TypeAdapterFactory getTypeAdapterFactory(JsonAdapter annotation, ConstructorConstructor constructorConstructor) {
    Class<?> value = annotation.value();
    if (!TypeAdapterFactory.class.isAssignableFrom(value)) return null;
    Map<Class, TypeAdapterFactory> adapterFactories = activeJsonAdapterFactories.get();
    TypeAdapterFactory factory = adapterFactories.get(value);
    if (factory == null) {
      Class<TypeAdapterFactory> typeAdapterFactoryClass = (Class<TypeAdapterFactory>) value;
      factory = constructorConstructor.get(TypeToken.get(typeAdapterFactoryClass))
          .construct();
      adapterFactories.put(value, factory);
    }
    return factory;
  }
}
