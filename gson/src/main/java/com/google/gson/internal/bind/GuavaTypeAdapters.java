/*
 * Copyright (C) 2025 Google Inc.
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

import static java.util.Arrays.asList;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Defines {@link TypeAdapterFactory} instances for Guava's immutable collections and maps.
 * Specifically, this defines adapters for {@code ImmutableList}, {@code ImmutableSet}, {@code
 * ImmutableSortedSet}, {@code ImmutableMap}, {@code ImmutableSortedMap}, and {@code
 * ImmutableBiMap}. Other immutable types may be added in the future.
 *
 * <p>The reason for having these adapters is that Gson's default way to build a collection or map
 * is to instantiate an empty one and then add elements as they come in. That obviously can't work
 * for immutable collections. Instead, we use their builders.
 *
 * <p>For {@code ImmutableSortedSet} and {@code ImmutableSortedMap}, the comparator is not included
 * in the serialized JSON.
 *
 * <p>This class has no compile-time dependency on Guava. It uses reflection for everything.
 * Although an optional dependency on Guava would probably work with the open-source build, Google's
 * internal build system doesn't have a notion of optional dependencies.
 */
public final class GuavaTypeAdapters {
  public static List<TypeAdapterFactory> factories() {
    return asList(
        new GuavaCollectionTypeAdapterFactory(),
        new GuavaMapTypeAdapterFactory(),
        new GuavaMultimapTypeAdapterFactory());
  }

  /** Shared parent class with reflective information for Guava collection and map types. */
  private abstract static class GuavaType {
    final Class<?> collectionClass;
    final Class<?> builderClass;
    final Method builderMethod;
    final Method buildMethod;

    GuavaType(String className, String builderMethodName) {
      try {
        this.collectionClass = Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      this.builderMethod = getMethod(collectionClass, builderMethodName);
      this.builderClass = builderMethod.getReturnType();
      this.buildMethod = getMethod(builderClass, "build");
    }

    Object createBuilder() {
      return invoke(builderMethod, null);
    }

    Object build(Object builder) {
      return invoke(buildMethod, builder);
    }
  }

  private static final class GuavaCollectionType extends GuavaType {
    private final Method addMethod;

    GuavaCollectionType(String className, String builderMethodName) {
      super(className, builderMethodName);
      this.addMethod = getMethod(builderClass, "add", Object.class);
    }

    void add(Object builder, Object element) {
      invoke(addMethod, builder, element);
    }
  }

  private static class GuavaMapType extends GuavaType {
    private final Method putMethod;

    GuavaMapType(String className, String builderMethodName) {
      super(className, builderMethodName);
      this.putMethod = getMethod(builderClass, "put", Object.class, Object.class);
    }

    void put(Object builder, Object key, Object value) {
      invoke(putMethod, builder, key, value);
    }
  }

  private static final class GuavaMultimapType extends GuavaMapType {
    private final Method asMapMethod;
    private final Method putAllMethod;

    GuavaMultimapType(String className, String builderMethodName) {
      super(className, builderMethodName);
      this.asMapMethod = getMethod(collectionClass, "asMap");
      this.putAllMethod = getMethod(builderClass, "putAll", Object.class, Iterable.class);
    }

    Map<?, ?> asMap(Object multimap) {
      return (Map<?, ?>) invoke(asMapMethod, multimap);
    }

    void putAll(Object builder, Object key, Iterable<?> values) {
      invoke(putAllMethod, builder, key, values);
    }
  }

  /**
   * A {@link TypeAdapterFactory} that creates {@link TypeAdapter}s for Guava immutable collections.
   */
  private static final class GuavaCollectionTypeAdapterFactory implements TypeAdapterFactory {
    private static final List<GuavaCollectionType> GUAVA_COLLECTION_CLASSES;

    static {
      List<GuavaCollectionType> guavaCollectionClasses = new ArrayList<>();
      // ImmutableSet must follow ImmutableSortedSet since it is a superclass. Otherwise
      // ImmutableSet would match both ImmutableSortedSet and ImmutableSet.
      String[][] guavaCollectionClassNames = {
        {"com.google.common.collect.ImmutableList", "builder"},
        {"com.google.common.collect.ImmutableSortedSet", "naturalOrder"},
        {"com.google.common.collect.ImmutableSet", "builder"},
        {"com.google.common.collect.ImmutableMultiset", "builder"},
      };
      for (String[] pair : guavaCollectionClassNames) {
        guavaCollectionClasses.add(new GuavaCollectionType(pair[0], pair[1]));
      }
      GUAVA_COLLECTION_CLASSES = Collections.unmodifiableList(guavaCollectionClasses);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      Type type = typeToken.getType();
      Class<?> rawClass = typeToken.getRawType();
      for (GuavaCollectionType guavaType : GUAVA_COLLECTION_CLASSES) {
        if (guavaType.collectionClass.isAssignableFrom(rawClass)) {
          Type elementType = $Gson$Types.getCollectionElementType(type, rawClass);
          TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));
          @SuppressWarnings("unchecked")
          TypeAdapter<Object> delegate =
              (TypeAdapter<Object>) gson.getDelegateAdapter(this, typeToken);
          @SuppressWarnings("unchecked")
          TypeAdapter<T> adapter =
              (TypeAdapter<T>)
                  new GuavaCollectionTypeAdapter(guavaType, delegate, elementTypeAdapter);
          return adapter;
        }
      }
      return null;
    }
  }

  /**
   * A {@link TypeAdapter} for an individual immutable collection with a given element type. It
   * serializes using the standard logic for collections, and deserializes by creating the
   * appropriate builder and adding elements one by one.
   */
  private static class GuavaCollectionTypeAdapter extends TypeAdapter<Object> {
    private final GuavaCollectionType guavaType;
    private final TypeAdapter<Object> delegate;
    private final TypeAdapter<?> elementTypeAdapter;

    GuavaCollectionTypeAdapter(
        GuavaCollectionType guavaType,
        TypeAdapter<Object> delegate,
        TypeAdapter<?> elementTypeAdapter) {
      this.guavaType = guavaType;
      this.delegate = delegate;
      this.elementTypeAdapter = elementTypeAdapter;
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      delegate.write(out, value);
    }

    @Override
    public Object read(JsonReader in) throws IOException {
      // This is basically the same as the code in CollectionTypeAdapterFactory.Adapter.read, except
      // using a builder rather than a mutable collection.
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      Object builder = guavaType.createBuilder();
      in.beginArray();
      while (in.hasNext()) {
        Object element = elementTypeAdapter.read(in);
        guavaType.add(builder, element);
      }
      in.endArray();
      return guavaType.build(builder);
    }
  }

  /** A {@link TypeAdapterFactory} that creates {@link TypeAdapter}s for Guava immutable maps. */
  private static final class GuavaMapTypeAdapterFactory implements TypeAdapterFactory {
    private static final List<GuavaMapType> GUAVA_MAP_CLASSES;

    static {
      List<GuavaMapType> guavaMapClasses = new ArrayList<>();
      // ImmutableMap must follow ImmutableSortedMap since it is a superclass. Otherwise
      // ImmutableMap would match both ImmutableSortedMap and ImmutableMap.
      String[][] guavaMapClassNames = {
        {"com.google.common.collect.ImmutableBiMap", "builder"},
        {"com.google.common.collect.ImmutableSortedMap", "naturalOrder"},
        {"com.google.common.collect.ImmutableMap", "builder"},
      };
      for (String[] pair : guavaMapClassNames) {
        guavaMapClasses.add(new GuavaMapType(pair[0], pair[1]));
      }
      GUAVA_MAP_CLASSES = Collections.unmodifiableList(guavaMapClasses);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      Type type = typeToken.getType();
      Class<?> rawClass = typeToken.getRawType();
      for (GuavaMapType guavaType : GUAVA_MAP_CLASSES) {
        if (guavaType.collectionClass.isAssignableFrom(rawClass)) {
          Type[] keyAndValueTypes = $Gson$Types.getMapKeyAndValueTypes(type, rawClass);
          Type keyType = keyAndValueTypes[0];
          Type valueType = keyAndValueTypes[1];
          TypeToken<?> mapKToV = TypeToken.getParameterized(Map.class, keyType, valueType);
          @SuppressWarnings("unchecked")
          TypeAdapter<Object> delegate =
              (TypeAdapter<Object>) gson.getDelegateAdapter(this, mapKToV);
          TypeAdapter<?> keyTypeAdapter = gson.getAdapter(TypeToken.get(keyType));
          TypeAdapter<?> valueTypeAdapter = gson.getAdapter(TypeToken.get(valueType));
          @SuppressWarnings("unchecked")
          TypeAdapter<T> adapter =
              (TypeAdapter<T>)
                  new GuavaMapTypeAdapter(guavaType, delegate, keyTypeAdapter, valueTypeAdapter);
          return adapter;
        }
      }
      return null;
    }
  }

  /**
   * A {@link TypeAdapter} for an individual immutable map with given key and value types. It
   * serializes using the standard logic for maps, and deserializes by creating the appropriate
   * builder and adding entries one by one.
   */
  private static class GuavaMapTypeAdapter extends TypeAdapter<Object> {
    private final GuavaMapType guavaType;
    private final TypeAdapter<Object> delegate;
    private final TypeAdapter<?> keyTypeAdapter;
    private final TypeAdapter<?> valueTypeAdapter;

    GuavaMapTypeAdapter(
        GuavaMapType guavaType,
        TypeAdapter<Object> delegate,
        TypeAdapter<?> keyTypeAdapter,
        TypeAdapter<?> valueTypeAdapter) {
      this.guavaType = guavaType;
      this.delegate = delegate;
      this.keyTypeAdapter = keyTypeAdapter;
      this.valueTypeAdapter = valueTypeAdapter;
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      delegate.write(out, value);
    }

    @Override
    public Object read(JsonReader in) throws IOException {
      switch (in.peek()) {
        case NULL:
          in.nextNull();
          return null;
        case BEGIN_ARRAY:
          // This is Gson's "complex map key serialization", where it serializes a map as an array
          // of [key, value] arrays. We don't try to be efficient for this unusual case: we just
          // deserialize it into a map and then copy it into an ImmutableMap.
          Map<?, ?> map = (Map<?, ?>) delegate.read(in);
          Method copyOfMethod = getMethod(guavaType.collectionClass, "copyOf", Map.class);
          return invoke(copyOfMethod, null, map);
        default:
          // Regular Gson map serialization, using a JSON map.
          // This is basically the same as the code in MapTypeAdapterFactory.Adapter.read, except
          // using a builder rather than a mutable map.
          Object builder = guavaType.createBuilder();
          in.beginObject();
          while (in.hasNext()) {
            JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
            Object key = keyTypeAdapter.read(in);
            Object value = valueTypeAdapter.read(in);
            guavaType.put(builder, key, value);
          }
          in.endObject();
          return guavaType.build(builder);
      }
    }
  }

  /**
   * A {@link TypeAdapterFactory} that creates {@link TypeAdapter}s for Guava immutable multimaps.
   */
  private static final class GuavaMultimapTypeAdapterFactory implements TypeAdapterFactory {
    // ImmutableMultimap must follow the others since it is a superclass. Otherwise
    // ImmutableListMultimap would match both ImmutableListMultimap and ImmutableMultimap.
    private static final List<GuavaMultimapType> GUAVA_MULTIMAP_CLASSES;

    static {
      List<GuavaMultimapType> guavaMultimapClasses = new ArrayList<>();
      String[] guavaMultimapClassNames = {
        "com.google.common.collect.ImmutableListMultimap",
        "com.google.common.collect.ImmutableSetMultimap",
        "com.google.common.collect.ImmutableMultimap",
      };
      for (String className : guavaMultimapClassNames) {
        guavaMultimapClasses.add(new GuavaMultimapType(className, "builder"));
      }
      GUAVA_MULTIMAP_CLASSES = Collections.unmodifiableList(guavaMultimapClasses);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      Type type = typeToken.getType();
      Class<?> rawClass = typeToken.getRawType();
      for (GuavaMultimapType guavaType : GUAVA_MULTIMAP_CLASSES) {
        if (guavaType.collectionClass.isAssignableFrom(rawClass)) {
          Type keyType;
          Type valueType;
          if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] keyAndValueTypes = parameterizedType.getActualTypeArguments();
            keyType = keyAndValueTypes[0];
            valueType = keyAndValueTypes[1];
          } else {
            keyType = Object.class;
            valueType = Object.class;
          }
          TypeToken<?> mapKToCollectionOfV =
              TypeToken.getParameterized(
                  Map.class,
                  keyType,
                  TypeToken.getParameterized(Collection.class, valueType).getType());
          @SuppressWarnings("unchecked")
          TypeAdapter<Object> mapKToCollectionOfVDelegate =
              (TypeAdapter<Object>) gson.getAdapter(mapKToCollectionOfV);
          @SuppressWarnings("unchecked")
          TypeAdapter<T> adapter =
              (TypeAdapter<T>) new GuavaMultimapTypeAdapter(guavaType, mapKToCollectionOfVDelegate);
          return adapter;
        }
      }
      return null;
    }
  }

  /**
   * A {@link TypeAdapter} for an individual immutable map with given key and value types. The JSON
   * serialization comes from the {@code asMap()} view, so it is a {@code Map<K, Collection<V>>}. To
   * deserialize, we read that same {@code Map<K, Collection<V>>} and then use {@code putAll} to
   * populate a multimap builder.
   */
  private static class GuavaMultimapTypeAdapter extends TypeAdapter<Object> {
    private final GuavaMultimapType guavaType;
    private final TypeAdapter<Object> mapKToCollectionOfVDelegate;

    GuavaMultimapTypeAdapter(
        GuavaMultimapType guavaType, TypeAdapter<Object> mapKToCollectionOfVDelegate) {
      this.guavaType = guavaType;
      this.mapKToCollectionOfVDelegate = mapKToCollectionOfVDelegate;
    }

    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      Object map = guavaType.asMap(value);
      mapKToCollectionOfVDelegate.write(out, map);
    }

    @Override
    public Object read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      Object builder = guavaType.createBuilder();
      Map<?, ?> map = (Map<?, ?>) mapKToCollectionOfVDelegate.read(in);
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        guavaType.putAll(builder, entry.getKey(), (Iterable<?>) entry.getValue());
      }
      return guavaType.build(builder);
    }
  }

  @CanIgnoreReturnValue
  private static Object invoke(Method method, Object target, Object... args) {
    try {
      return method.invoke(target, args);
    } catch (ReflectiveOperationException e) {
      throw new JsonIOException("Failed to invoke " + method.getName(), e);
    }
  }

  private static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
    try {
      return clazz.getMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private GuavaTypeAdapters() {}
}
