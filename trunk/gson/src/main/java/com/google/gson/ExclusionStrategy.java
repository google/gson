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
 * A strategy pattern (see "Design Patterns" written by GoF for some literature on this pattern)
 * definition that is used to decide whether or not a field or top-level class should be serialized
 * (or deserialized) as part of the JSON output/input.
 *
 * <p>The following example show an implementation of an {@code ExclusionStrategy} where a specific
 * type will be excluded from the output.
 *
 * <p><pre class="code">
 * private static class UserDefinedExclusionStrategy implements ExclusionStrategy {
 *   private final Class&lt;?&gt; excludedThisClass;
 *
 *   UserDefinedExclusionStrategy(Class&lt;?&gt; excludedThisClass) {
 *     this.excludedThisClass = excludedThisClass;
 *   }
 *
 *   public boolean shouldSkipClass(Class&lt;?&gt; clazz) {
 *     return excludedThisClass.equals(clazz);
 *   }
 *
 *   public boolean shouldSkipField(FieldAttributes f) {
 *     return excludedThisClass.equals(f.getDeclaredClass());
 *   }
 * }
 *
 * ExclusionStrategy excludeStrings = new UserDefinedExclusionStrategy(String.class);
 * Gson gson = new GsonBuilder()
 *     .setExclusionStrategies(excludeStrings)
 *     .create();
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 *
 * @see GsonBuilder#setExclusionStrategies(ExclusionStrategy...)
 *
 * @since 1.4
 */
interface ExclusionStrategy {

  /**
   * @param f the field object that is under test
   * @return true if the field should be ignored; otherwise false
   */
  public boolean shouldSkipField(FieldAttributes f);

  /**
   * @param clazz the class object that is under test
   * @return true if the class should be ignored; otherwise false
   */
  public boolean shouldSkipClass(Class<?> clazz);
}
