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

import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * This strategy will exclude any files and/or class that are passed the
 * {@link #version} value.
 *
 * @author Joel Leitch
 */
final class VersionExclusionStrategy implements ExclusionStrategy {
  private final double version;

  public VersionExclusionStrategy(double version) {
    Preconditions.checkArgument(version >= 0.0D);
    this.version = version;
  }

  public boolean shouldSkipField(Field f) {
    return !isValidVersion(f.getAnnotations());
  }

  public boolean shouldSkipClass(Class<?> clazz) {
    return !isValidVersion(clazz.getAnnotations());
  }

  private boolean isValidVersion(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (!isValidSince(annotation) || !isValidUntil(annotation)) {
        return false;
      }
    }
    return true;
  }
  
  private boolean isValidSince(Annotation annotation) {
    if (annotation instanceof Since) {
      double annotationVersion = ((Since) annotation).value();
      if (annotationVersion > version) {
        return false;
      }
    }
    return true;
  }
  
  private boolean isValidUntil(Annotation annotation) {
    if (annotation instanceof Until) {
      double annotationVersion = ((Until) annotation).value();
      if (annotationVersion <= version) {
        return false;
      }
    }
    return true;
  }
}
