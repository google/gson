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

/**
 * This is a configurable {@link ExclusionStrategy} that can be used for
 * unit testing.
 *
 * @author Joel Leitch
 */
final class MockExclusionStrategy implements ExclusionStrategy {
  private final boolean skipClass;
  private final boolean skipField;

  public MockExclusionStrategy(boolean skipClass, boolean skipField) {
    this.skipClass = skipClass;
    this.skipField = skipField;
  }

  @Override
  public boolean shouldSkipField(FieldAttributes f) {
    return skipField;
  }

  @Override
  public boolean shouldSkipClass(Class<?> clazz) {
    return skipClass;
  }
}
