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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Exclude fields based on particular field modifiers.  For a list of possible
 * modifiers, see {@link java.lang.reflect.Modifier}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class ModifierBasedExclusionStrategy implements ExclusionStrategy {
  private final boolean skipSyntheticField;
  private final Collection<Integer> modifiers;

  public ModifierBasedExclusionStrategy(boolean skipSyntheticFields, int... modifiers) {
    this.skipSyntheticField = skipSyntheticFields;
    if (modifiers == null) {
      this.modifiers = ImmutableList.of();
    } else {
      Collection<Integer> tempCollection = Sets.newHashSet();
      for (int modifier : modifiers) {
        tempCollection.add(modifier);
      }
      this.modifiers = ImmutableSet.copyOf(tempCollection);
    }
  }

  public boolean shouldSkipField(Field f) {
    if (skipSyntheticField && f.isSynthetic()) {
      return true;
    } else {
      int objectModifiers = f.getModifiers();
      for (int modifier : modifiers) {
        if ((objectModifiers & modifier) != 0) {
          return true;
        }
      }
      return false;
    }
  }

  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
