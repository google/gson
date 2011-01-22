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
 * A strategy (or policy) definition that is used to decide whether or not a field or top-level
 * class should be serialized or deserialized as part of the JSON output/input. For serialization,
 * if the {@link #shouldSkipClass(Class, Mode)} method returns false then that class or field 
 * type will not be part of the JSON output.  For deserialization, if
 * {@link #shouldSkipClass(Class, , Context)} returns false, then it will not be set as part of
 * the Java object structure.
 *
 * <p>The following are a few examples that shows how you can use this exclusion mechanism.
 *
 * <p><strong>Exclude fields and objects based on a particular class type for both serialization
 * and deserialization:</strong>
 * <pre class="code">
 * public class SpecificClassExclusionStrategy implements ExclusionStrategy2 {
 *   private final Class&lt;?&gt; excludedThisClass;
 *
 *   public SpecificClassExclusionStrategy(Class&lt;?&gt; excludedThisClass) {
 *     this.excludedThisClass = excludedThisClass;
 *   }
 *
 *   public boolean shouldSkipClass(Class&lt;?&gt; clazz, Context context) {
 *     return excludedThisClass.equals(clazz);
 *   }
 *
 *   public boolean shouldSkipField(FieldAttributes f, Context context) {
 *     return excludedThisClass.equals(f.getDeclaredClass());
 *   }
 * }
 * </pre>
 *
 * <p><strong>Excludes fields and objects based on a particular annotation for both serialization
 * and deserialization:</strong>
 * <pre class="code">
 * public &#64interface FooAnnotation {
 *   // some implementation here
 * }
 *
 * // Excludes any field (or class) that is tagged with an "&#64FooAnnotation"
 * public class FooAnnotationExclusionStrategy implements ExclusionStrategy2 {
 *   public boolean shouldSkipClass(Class&lt;?&gt; clazz, Context context) {
 *     return clazz.getAnnotation(FooAnnotation.class) != null;
 *   }
 *
 *   public boolean shouldSkipField(FieldAttributes f, Context context) {
 *     return f.getAnnotation(FooAnnotation.class) != null;
 *   }
 * }
 * </pre>
 * 
 * <p><strong>Exclude fields and objects based on a particular class type for serialization
 * only:</strong>
 * <pre class="code">
 * public class SpecificClassExclusionStrategy implements ExclusionStrategy2 {
 *   private final Class&lt;?&gt; excludedThisClass;
 *
 *   public SpecificClassExclusionStrategy(Class&lt;?&gt; excludedThisClass) {
 *     this.excludedThisClass = excludedThisClass;
 *   }
 *
 *   public boolean shouldSkipClass(Class&lt;?&gt; clazz, Context context) {
 *     if (context == Context.SERIALIZE) {
 *       return excludedThisClass.equals(clazz);
 *     } else {
 *       return false;
 *     }
 *   }
 *
 *   public boolean shouldSkipField(FieldAttributes f, Context context) {
 *   if (context == Context.SERIALIZE) {
 *       return excludedThisClass.equals(f.getDeclaredClass());
 *     } else {
 *       return false;
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>Now if you want to configure {@code Gson} to use a user defined exclusion strategy, then
 * the {@code GsonBuilder} is required. The following is an example of how you can use the
 * {@code GsonBuilder} to configure Gson to use one of the above sample:
 * <pre class="code">
 * ExclusionStrategy2 excludeStrings = new UserDefinedExclusionStrategy(String.class);
 * Gson gson = new GsonBuilder()
 *     .setExclusionStrategies(excludeStrings)
 *     .create();
 * </pre>
 *
 * @author Joel Leitch
 * 
 * @since 1.7
 */
public interface ExclusionStrategy2 {

  /**
   * @param f the field object that is under test
   * @param mode the current mode the Gson is running in
   * @return true if the field should be ignored; otherwise false
   */
  public boolean shouldSkipField(FieldAttributes f, Mode mode);

  /**
   * @param clazz the class object that is under test
   * @param mode the current mode the Gson is running in
   * @return true if the class should be ignored; otherwise false
   */
  public boolean shouldSkipClass(Class<?> clazz, Mode mode);
}
