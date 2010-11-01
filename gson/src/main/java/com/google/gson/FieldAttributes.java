/*
 * Copyright (C) 2009 Google Inc.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A data object that stores attributes of a field.
 *
 * <p>This class is immutable; therefore, it can be safely shared across threads.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 *
 * @since 1.4
 */
public final class FieldAttributes {
  private static final String MAX_CACHE_PROPERTY_NAME =
      "com.google.gson.annotation_cache_size_hint";

  private static final Cache<Pair<Class<?>, String>, Collection<Annotation>> ANNOTATION_CACHE =
      new LruCache<Pair<Class<?>,String>, Collection<Annotation>>(getMaxCacheSize());

  private final Class<?> declaringClazz;
  private final Field field;
  private final Class<?> declaredType;
  private final boolean isSynthetic;
  private final int modifiers;
  private final String name;

  // Fields used for lazy initialization
  private Type genericType;
  private Collection<Annotation> annotations;

  /**
   * Constructs a Field Attributes object from the {@code f}.
   *
   * @param f the field to pull attributes from
   */
  FieldAttributes(final Class<?> declaringClazz, final Field f) {
    Preconditions.checkNotNull(declaringClazz);
    this.declaringClazz = declaringClazz;
    name = f.getName();
    declaredType = f.getType();
    isSynthetic = f.isSynthetic();
    modifiers = f.getModifiers();
    field = f;
  }

  private static int getMaxCacheSize() {
    final int defaultMaxCacheSize = 2000;
    try {
      String propertyValue = System.getProperty(
          MAX_CACHE_PROPERTY_NAME, String.valueOf(defaultMaxCacheSize));
      return Integer.parseInt(propertyValue);
    } catch (NumberFormatException e) {
      return defaultMaxCacheSize;
    }
  }

  /**
   * @return the declaring class that contains this field
   */
  public Class<?> getDeclaringClass() {
    return declaringClazz;
  }

  /**
   * @return the name of the field
   */
  public String getName() {
    return name;
  }

  /**
   * <p>For example, assume the following class definition:
   * <pre class="code">
   * public class Foo {
   *   private String bar;
   *   private List&lt;String&gt; red;
   * }
   *
   * Type listParmeterizedType = new TypeToken<List<String>>() {}.getType();
   * </pre>
   *
   * <p>This method would return {@code String.class} for the {@code bar} field and
   * {@code listParameterizedType} for the {@code red} field.
   *
   * @return the specific type declared for this field
   */
  public Type getDeclaredType() {
    if (genericType == null) {
      genericType = field.getGenericType();
    }
    return genericType;
  }

  /**
   * Returns the {@code Class<?>} object that was declared for this field.
   *
   * <p>For example, assume the following class definition:
   * <pre class="code">
   * public class Foo {
   *   private String bar;
   *   private List&lt;String&gt; red;
   * }
   * </pre>
   *
   * <p>This method would return {@code String.class} for the {@code bar} field and
   * {@code List.class} for the {@code red} field.
   *
   * @return the specific class object that was declared for the field
   */
  public Class<?> getDeclaredClass() {
    return declaredType;
  }

  /**
   * Return the {@code T} annotation object from this field if it exist; otherwise returns
   * {@code null}.
   *
   * @param annotation the class of the annotation that will be retrieved
   * @return the annotation instance if it is bound to the field; otherwise {@code null}
   */
  public <T extends Annotation> T getAnnotation(Class<T> annotation) {
    return getAnnotationFromArray(getAnnotations(), annotation);
  }

  /**
   * Return the annotations that are present on this field.
   *
   * @return an array of all the annotations set on the field
   * @since 1.4
   */
  public Collection<Annotation> getAnnotations() {
    if (annotations == null) {
      Pair<Class<?>, String> key = new Pair<Class<?>, String>(declaringClazz, name);
      annotations = ANNOTATION_CACHE.getElement(key);
      if (annotations == null) {
        annotations = Collections.unmodifiableCollection(
            Arrays.asList(field.getAnnotations()));
        ANNOTATION_CACHE.addElement(key, annotations);
      }
    }
    return annotations;
  }

  /**
   * Returns {@code true} if the field is defined with the {@code modifier}.
   *
   * <p>This method is meant to be called as:
   * <pre class="code">
   * boolean hasPublicModifier = fieldAttribute.hasModifier(java.lang.reflect.Modifier.PUBLIC);
   * </pre>
   *
   * @see java.lang.reflect.Modifier
   */
  public boolean hasModifier(int modifier) {
    return (modifiers & modifier) != 0;
  }

  /**
   * This is exposed internally only for the removing synthetic fields from the JSON output.
   *
   * @return true if the field is synthetic; otherwise false
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  void set(Object instance, Object value) throws IllegalAccessException {
    field.set(instance, value);
  }

  /**
   * This is exposed internally only for the removing synthetic fields from the JSON output.
   *
   * @return true if the field is synthetic; otherwise false
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  Object get(Object instance) throws IllegalAccessException {
    return field.get(instance);
  }

  /**
   * This is exposed internally only for the removing synthetic fields from the JSON output.
   *
   * @return true if the field is synthetic; otherwise false
   */
  boolean isSynthetic() {
    return isSynthetic;
  }

  /**
   * @deprecated remove this when {@link FieldNamingStrategy} is deleted.
   */
  @Deprecated
  Field getFieldObject() {
    return field;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Annotation> T getAnnotationFromArray(
      Collection<Annotation> annotations, Class<T> annotation) {
    for (Annotation a : annotations) {
      if (a.annotationType() == annotation) {
        return (T) a;
      }
    }
    return null;
  }
}
