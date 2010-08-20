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

import java.util.Collection;
import java.util.HashSet;

/**
 * Exclude fields based on particular field modifiers.  For a list of possible
 * modifiers, see {@link java.lang.reflect.Modifier}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class ModifierBasedExclusionStrategy implements ExclusionStrategy {
  private final Collection<Integer> modifiers;

  public ModifierBasedExclusionStrategy(int... modifiers) {
    this.modifiers = new HashSet<Integer>();
    if (modifiers != null) {
      for (int modifier : modifiers) {
        this.modifiers.add(modifier);
      }
    }
  }

  public boolean shouldSkipField(FieldAttributes f) {
    for (int modifier : modifiers) {
      if (f.hasModifier(modifier)) {
        return true;
      }
    }
    return false;
  }

  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
