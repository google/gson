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

package com.google.gson.reflect;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * A wrapper class used to collect numerous {@link ExclusionStrategy} objects
 * and perform a short-circuited OR operation.
 *
 * @author Joel Leitch
 */
public final class DisjunctionExclusionStrategy implements ExclusionStrategy {
  private final Collection<ExclusionStrategy> strategies;

  public DisjunctionExclusionStrategy(ExclusionStrategy... strategies) {
    Preconditions.checkNotNull(strategies);
    Preconditions.checkArgument(strategies.length > 0);

    this.strategies = ImmutableList.of(strategies);
  }

  public DisjunctionExclusionStrategy(Collection<ExclusionStrategy> strategies) {
    Preconditions.checkNotNull(strategies);
    Preconditions.checkArgument(!strategies.isEmpty());

    this.strategies = ImmutableList.copyOf(strategies);
  }

  public boolean shouldSkipField(Field f) {
    for (ExclusionStrategy strategy : strategies) {
      if (strategy.shouldSkipField(f)) {
        return true;
      }
    }
    return false;
  }

  public boolean shouldSkipClass(Class<?> clazz) {
    for (ExclusionStrategy strategy : strategies) {
      if (strategy.shouldSkipClass(clazz)) {
        return true;
      }
    }
    return false;
  }
}
