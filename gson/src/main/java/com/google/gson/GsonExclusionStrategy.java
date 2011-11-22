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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import java.lang.reflect.Modifier;

/**
 * A configurable exclusion strategy. This strategy supports version attributes
 * {@link Since} and {@link Until}, modifiers, synthetic fields, anonymous and
 * local classes, inner classes, and fields with the {@link Expose} annotation.
 *
 * @author Joel Leitch
 * @author Jesse Wilson
 */
final class GsonExclusionStrategy implements ExclusionStrategy {
  static final double IGNORE_VERSIONS = -1D;
  private final double version;
  private final int modifiers;
  private final boolean excludeSyntheticFields;
  private final boolean excludeAnonymousAndLocal;
  private final boolean serializeInnerClasses;
  private final boolean requireExposeOnSerialize;
  private final boolean requireExposeOnDeserialize;

  GsonExclusionStrategy(double version, int modifiers, boolean excludeSyntheticFields,
      boolean excludeAnonymousAndLocal, boolean serializeInnerClasses,
      boolean requireExposeOnSerialize, boolean requireExposeOnDeserialize) {
    this.version = version;
    this.modifiers = modifiers;
    this.excludeSyntheticFields = excludeSyntheticFields;
    this.excludeAnonymousAndLocal = excludeAnonymousAndLocal;
    this.serializeInnerClasses = serializeInnerClasses;
    this.requireExposeOnSerialize = requireExposeOnSerialize;
    this.requireExposeOnDeserialize = requireExposeOnDeserialize;
  }

  public boolean shouldSkipField(FieldAttributes f) {
    if (f.hasModifier(modifiers)) {
      return true;
    }
    if (version != GsonExclusionStrategy.IGNORE_VERSIONS
        && !isValidVersion(f.getAnnotation(Since.class), f.getAnnotation(Until.class))) {
      return true;
    }
    if (excludeSyntheticFields && f.isSynthetic()) {
      return true;
    }
    if (requireExposeOnSerialize || requireExposeOnDeserialize) {
      Expose annotation = f.getAnnotation(Expose.class);
      if (annotation == null
          || requireExposeOnSerialize && !annotation.serialize()
          || requireExposeOnDeserialize && !annotation.deserialize()) {
        return true;
      }
    }
    if (!serializeInnerClasses && isInnerClass(f.getDeclaredClass())) {
      return true;
    }
    if (excludeAnonymousAndLocal && isAnonymousOrLocal(f.getDeclaredClass())) {
      return true;
    }
    return false;
  }

  public boolean shouldSkipClass(Class<?> clazz) {
    if (version != GsonExclusionStrategy.IGNORE_VERSIONS
        && !isValidVersion(clazz.getAnnotation(Since.class), clazz.getAnnotation(Until.class))) {
      return true;
    }
    if (!serializeInnerClasses && isInnerClass(clazz)) {
      return true;
    }
    if (excludeAnonymousAndLocal && isAnonymousOrLocal(clazz)) {
      return true;
    }
    return false;
  }

  private boolean isAnonymousOrLocal(Class<?> clazz) {
    return !Enum.class.isAssignableFrom(clazz)
        && (clazz.isAnonymousClass() || clazz.isLocalClass());
  }

  private boolean isInnerClass(Class<?> clazz) {
    return clazz.isMemberClass() && !isStatic(clazz);
  }

  private boolean isStatic(Class<?> clazz) {
    return (clazz.getModifiers() & Modifier.STATIC) != 0;
  }

  private boolean isValidVersion(Since since, Until until) {
    return isValidSince(since) && isValidUntil(until);
  }

  private boolean isValidSince(Since annotation) {
    if (annotation != null) {
      double annotationVersion = annotation.value();
      if (annotationVersion > version) {
        return false;
      }
    }
    return true;
  }

  private boolean isValidUntil(Until annotation) {
    if (annotation != null) {
      double annotationVersion = annotation.value();
      if (annotationVersion <= version) {
        return false;
      }
    }
    return true;
  }
}
