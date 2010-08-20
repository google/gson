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

  public boolean shouldSkipField(FieldAttributes f) {
    return !isValidVersion(f.getAnnotation(Since.class), f.getAnnotation(Until.class));
  }

  public boolean shouldSkipClass(Class<?> clazz) {
    return !isValidVersion(clazz.getAnnotation(Since.class), clazz.getAnnotation(Until.class));
  }

  private boolean isValidVersion(Since since, Until until) {
    return (isValidSince(since) && isValidUntil(until));
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
