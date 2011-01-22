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
 * This is a configurable {@link ExclusionStrategy2} that can be used for
 * unit testing.
 *
 * @author Joel Leitch
 */
public class MockExclusionStrategy2 implements ExclusionStrategy2 {
  private final MockExclusionStrategy strategy;
  private final Mode mode;

  public MockExclusionStrategy2(boolean skipClass, boolean skipField, Mode mode) {
    this.strategy = new MockExclusionStrategy(skipClass, skipField);
    this.mode = mode;
  }
  
  public boolean shouldSkipField(FieldAttributes f, Mode mode) {
    if (this.mode == null || this.mode == mode) {
      return strategy.shouldSkipField(f);
    } else {
      return false;
    }
  }

  public boolean shouldSkipClass(Class<?> clazz, Mode mode) {
    if (this.mode == null || this.mode == mode) {
      return strategy.shouldSkipClass(clazz);
    } else {
      return false;
    }
  }
}
