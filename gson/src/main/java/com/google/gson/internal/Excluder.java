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

package com.google.gson.internal;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class selects which fields and types to omit. It is configurable, supporting version
 * attributes {@link Since} and {@link Until}, modifiers, synthetic fields, anonymous and local
 * classes, inner classes, and fields with the {@link Expose} annotation.
 *
 * <p>This class is a type adapter factory; types that are excluded will be adapted to null. It may
 * delegate to another type adapter if only one direction is excluded.
 *
 * @author Joel Leitch
 * @author Jesse Wilson
 */
public final class Excluder implements TypeAdapterFactory, Cloneable {
  private static final double IGNORE_VERSIONS = -1.0d;
  public static final Excluder DEFAULT = new Excluder();

  private double version = IGNORE_VERSIONS;
  private int modifiers = Modifier.TRANSIENT | Modifier.STATIC;
  private boolean serializeInnerClasses = true;
  private boolean requireExpose;
  private List<ExclusionStrategy> serializationStrategies = Collections.emptyList();
  private List<ExclusionStrategy> deserializationStrategies = Collections.emptyList();

  @Override
  protected Excluder clone() {
    try {
      return (Excluder) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  public Excluder withVersion(double ignoreVersionsAfter) {
    Excluder result = clone();
    result.version = ignoreVersionsAfter;
    return result;
  }

  public Excluder withModifiers(int... modifiers) {
    Excluder result = clone();
    result.modifiers = 0;
    for (int modifier : modifiers) {
      result.modifiers |= modifier;
    }
    return result;
  }

  public Excluder disableInnerClassSerialization() {
    Excluder result = clone();
    result.serializeInnerClasses = false;
    return result;
  }

  public Excluder excludeFieldsWithoutExposeAnnotation() {
    Excluder result = clone();
    result.requireExpose = true;
    return result;
  }

  public Excluder withExclusionStrategy(
      ExclusionStrategy exclusionStrategy, boolean serialization, boolean deserialization) {
    Excluder result = clone();
    if (serialization) {
      result.serializationStrategies = new ArrayList<>(serializationStrategies);
      result.serializationStrategies.add(exclusionStrategy);
    }
    if (deserialization) {
      result.deserializationStrategies = new ArrayList<>(deserializationStrategies);
      result.deserializationStrategies.add(exclusionStrategy);
    }
    return result;
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<?> rawType = type.getRawType();

    boolean skipSerialize = excludeClass(rawType, true);
    boolean skipDeserialize = excludeClass(rawType, false);

    if (!skipSerialize && !skipDeserialize) {
      return null;
    }

    return new TypeAdapter<T>() {
      /**
       * The delegate is lazily created because it may not be needed, and creating it may fail.
       * Field has to be {@code volatile} because {@link Gson} guarantees to be thread-safe.
       */
      private volatile TypeAdapter<T> delegate;

      @Override
      public T read(JsonReader in) throws IOException {
        if (skipDeserialize) {
          in.skipValue();
          return null;
        }
        return delegate().read(in);
      }

      @Override
      public void write(JsonWriter out, T value) throws IOException {
        if (skipSerialize) {
          out.nullValue();
          return;
        }
        delegate().write(out, value);
      }

      private TypeAdapter<T> delegate() {
        // A race might lead to `delegate` being assigned by multiple threads but the last
        // assignment will stick
        TypeAdapter<T> d = delegate;
        return d != null ? d : (delegate = gson.getDelegateAdapter(Excluder.this, type));
      }
    };
  }

  public boolean excludeField(Field field, boolean serialize) {
    if ((modifiers & field.getModifiers()) != 0) {
      return true;
    }

    if (version != Excluder.IGNORE_VERSIONS
        && !isValidVersion(field.getAnnotation(Since.class), field.getAnnotation(Until.class))) {
      return true;
    }

    if (field.isSynthetic()) {
      return true;
    }

    if (requireExpose) {
      Expose annotation = field.getAnnotation(Expose.class);
      if (annotation == null || (serialize ? !annotation.serialize() : !annotation.deserialize())) {
        return true;
      }
    }

    if (excludeClass(field.getType(), serialize)) {
      return true;
    }

    List<ExclusionStrategy> list = serialize ? serializationStrategies : deserializationStrategies;
    if (!list.isEmpty()) {
      FieldAttributes fieldAttributes = new FieldAttributes(field);
      for (ExclusionStrategy exclusionStrategy : list) {
        if (exclusionStrategy.shouldSkipField(fieldAttributes)) {
          return true;
        }
      }
    }

    return false;
  }

  // public for unit tests; can otherwise be private
  public boolean excludeClass(Class<?> clazz, boolean serialize) {
    if (version != Excluder.IGNORE_VERSIONS
        && !isValidVersion(clazz.getAnnotation(Since.class), clazz.getAnnotation(Until.class))) {
      return true;
    }

    if (!serializeInnerClasses && isInnerClass(clazz)) {
      return true;
    }

    /*
     * Exclude anonymous and local classes because they can have synthetic fields capturing enclosing
     * values which makes serialization and deserialization unreliable.
     * Don't exclude anonymous enum subclasses because enum types have a built-in adapter.
     *
     * Exclude only for deserialization; for serialization allow because custom adapter might be
     * used; if no custom adapter exists reflection-based adapter otherwise excludes value.
     *
     * Cannot allow deserialization reliably here because some custom adapters like Collection adapter
     * fall back to creating instances using Unsafe, which would likely lead to runtime exceptions
     * for anonymous and local classes if they capture values.
     */
    if (!serialize
        && !Enum.class.isAssignableFrom(clazz)
        && ReflectionHelper.isAnonymousOrNonStaticLocal(clazz)) {
      return true;
    }

    List<ExclusionStrategy> list = serialize ? serializationStrategies : deserializationStrategies;
    for (ExclusionStrategy exclusionStrategy : list) {
      if (exclusionStrategy.shouldSkipClass(clazz)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isInnerClass(Class<?> clazz) {
    return clazz.isMemberClass() && !ReflectionHelper.isStatic(clazz);
  }

  private boolean isValidVersion(Since since, Until until) {
    return isValidSince(since) && isValidUntil(until);
  }

  private boolean isValidSince(Since annotation) {
    if (annotation != null) {
      double annotationVersion = annotation.value();
      return version >= annotationVersion;
    }
    return true;
  }

  private boolean isValidUntil(Until annotation) {
    if (annotation != null) {
      double annotationVersion = annotation.value();
      return version < annotationVersion;
    }
    return true;
  }
}
