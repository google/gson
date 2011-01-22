/*
 * Copyright (C) 2011 Google Inc.
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
 * Adapts the old {@link ExclusionStrategy} into the newer {@link ExclusionStrategy2} type.
 *
 * @author Joel Leitch
 */
class ExclusionStrategy2Adapter implements ExclusionStrategy2 {
  private final ExclusionStrategy strategy;
  
  public ExclusionStrategy2Adapter(ExclusionStrategy strategy) {
    Preconditions.checkNotNull(strategy);
    this.strategy = strategy;
  }

  public boolean shouldSkipClass(Class<?> clazz, Mode mode) {
    return strategy.shouldSkipClass(clazz);
  }

  public boolean shouldSkipField(FieldAttributes f, Mode mode) {
    return strategy.shouldSkipField(f);
  }
}
