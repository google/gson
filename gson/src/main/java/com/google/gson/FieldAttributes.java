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
import java.util.Objects;

/**
 * A data object that stores attributes of a field.
 *
 * <p>This class is immutable; therefore, it can be safely shared across threads.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.4
 */
public final class FieldAttributes {
  private final Field field;

  /**
   * Constructs a Field Attributes object from the {@code f}.
   *
   * @param f the field to pull attributes from
   */
  public FieldAttributes(Field f) {
    this.field = Objects.requireNonNull(f);
  }

  /**
   * Gets the declaring Class that contains this field
   *
   * @return the declaring class that contains this field
   */
  public Class<?> getDeclaringClass() {
    return field.getDeclaringClass();
  }

  /**
   * Gets the name of the field
   *
   * @return the name of the field
   */
  public String getName() {
    return field.getName();
  }

  /**
   * Returns the declared generic type of the field.
   *
   * <p>For example, assume the following class definition:
   *
   * <pre class="code">
   * public class Foo {
   *   private String bar;
   *   private List&lt;String&gt; red;
   * }
   *
   * Type listParameterizedType = new TypeToken&lt;List&lt;String&gt;&gt;() {}.getType();
   * </pre>
   *
   * <p>This method would return {@code String.class} for the {@code bar} field and {@code
   * listParameterizedType} for the {@code red} field.
   *
   * @return the specific type declared for this field
   */
  public Type getDeclaredType() {
    return field.getGenericType();
  }

  /**
   * Returns the {@code Class} object that was declared for this field.
   *
   * <p>For example, assume the following class definition:
   *
   * <pre class="code">
   * public class Foo {
   *   private String bar;
   *   private List&lt;String&gt; red;
   * }
   * </pre>
   *
   * <p>This method would return {@code String.class} for the {@code bar} field and {@code
   * List.class} for the {@code red} field.
   *
   * @return the specific class object that was declared for the field
   */
  public Class<?> getDeclaredClass() {
    return field.getType();
  }

  /**
   * Returns the {@code T} annotation object from this field if it exists; otherwise returns {@code
   * null}.
   *
   * @param annotation the class of the annotation that will be retrieved
   * @return the annotation instance if it is bound to the field; otherwise {@code null}
   */
  public <T extends Annotation> T getAnnotation(Class<T> annotation) {
    return field.getAnnotation(annotation);
  }

  /**
   * Returns the annotations that are present on this field.
   *
   * @return an array of all the annotations set on the field
   * @since 1.4
   */
  public Collection<Annotation> getAnnotations() {
    return Arrays.asList(field.getAnnotations());
  }

  /**
   * Returns {@code true} if the field is defined with the {@code modifier}.
   *
   * <p>This method is meant to be called as:
   *
   * <pre class="code">
   * boolean hasPublicModifier = fieldAttribute.hasModifier(java.lang.reflect.Modifier.PUBLIC);
   * </pre>
   *
   * @see java.lang.reflect.Modifier
   */
  public boolean hasModifier(int modifier) {
    return (field.getModifiers() & modifier) != 0;
  }

  @Override
  public String toString() {
    return field.toString();
  }
}
