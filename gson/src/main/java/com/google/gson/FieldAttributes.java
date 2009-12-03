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
  private final Field field;
  private final Class<?> declaredType;
  private final boolean isSynthetic;
  private final int modifiers;

  // Fields used for lazy initialization
  private String name;
  private Type genericType;
  private Annotation[] annotations;

  /**
   * Constructs a Field Attributes object from the {@code f}.
   *
   * @param f the field to pull attributes from
   */
  FieldAttributes(final Field f) {
    Preconditions.checkNotNull(f);
    field = f;
    declaredType = f.getType();
    isSynthetic = f.isSynthetic();
    modifiers = field.getModifiers();
  }

  /**
   * @return the name of the field
   */
  public String getName() {
    if (name == null) {
      name = field.getName();
    }
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
    if (annotations == null) {
      annotations = field.getAnnotations();
    }
    return getAnnotationFromArray(annotations, annotation);
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
   */
  boolean isSynthetic() {
    return isSynthetic;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Annotation> T getAnnotationFromArray(
      Annotation[] annotations, Class<T> annotation) {
    for (Annotation a : annotations) {
      if (a.annotationType() == annotation) {
        return (T) a;
      }
    }
    return null;
  }
}
