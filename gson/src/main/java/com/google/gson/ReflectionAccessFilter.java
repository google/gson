/*
 * Copyright (C) 2022 Google Inc.
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

import com.google.gson.internal.ReflectionAccessFilterHelper;
import java.lang.reflect.AccessibleObject;

/**
 * Filter for determining whether reflection based serialization and
 * deserialization is allowed for a class.
 *
 * <p>A filter can be useful in multiple scenarios, for example when
 * upgrading to newer Java versions which use the Java Platform Module
 * System (JPMS). A filter then allows to {@linkplain FilterResult#BLOCK_INACCESSIBLE
 * prevent making inaccessible members accessible}, even if the used
 * Java version might still allow illegal access (but logs a warning),
 * or if {@code java} command line arguments are used to open the inaccessible
 * packages to other parts of the application. This interface defines some
 * convenience filters for this task, such as {@link #BLOCK_INACCESSIBLE_JAVA}.
 *
 * <p>A filter can also be useful to prevent mixing model classes of a
 * project with other non-model classes; the filter could
 * {@linkplain FilterResult#BLOCK_ALL block all reflective access} to
 * non-model classes.
 *
 * <p>A reflection access filter is similar to an {@link ExclusionStrategy}
 * with the major difference that a filter will cause an exception to be
 * thrown when access is disallowed while an exclusion strategy just skips
 * fields and classes.
 *
 * @see GsonBuilder#addReflectionAccessFilter(ReflectionAccessFilter)
 * @since 2.9.1
 */
public interface ReflectionAccessFilter {
  /**
   * Result of a filter check.
   *
   * @since 2.9.1
   */
  enum FilterResult {
    /**
     * Reflection access for the class is allowed.
     *
     * <p>Note that this does not affect the Java access checks in any way,
     * it only permits Gson to try using reflection for a class. The Java
     * runtime might still deny such access.
     */
    ALLOW,
    /**
     * The filter is indecisive whether reflection access should be allowed.
     * The next registered filter will be consulted to get the result. If
     * there is no next filter, this result acts like {@link #ALLOW}.
     */
    INDECISIVE,
    /**
     * Blocks reflection access if a member of the class is not accessible
     * by default and would have to be made accessible. This is unaffected
     * by any {@code java} command line arguments being used to make packages
     * accessible, or by module declaration directives which <i>open</i> the
     * complete module or certain packages for reflection and will consider
     * such packages inaccessible.
     *
     * <p>Note that this <b>only works for Java 9 and higher</b>, for older
     * Java versions its functionality will be limited and it might behave like
     * {@link #ALLOW}. Access checks are only performed as defined by the Java
     * Language Specification (<a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-6.html#jls-6.6">JLS 11 &sect;6.6</a>),
     * restrictions imposed by a {@link SecurityManager} are not considered.
     *
     * <p>This result type is mainly intended to help enforce the access checks of
     * the Java Platform Module System. It allows detecting illegal access, even if
     * the used Java version would only log a warning, or is configured to open
     * packages for reflection using command line arguments.
     *
     * @see AccessibleObject#canAccess(Object)
     */
    BLOCK_INACCESSIBLE,
    /**
     * Blocks all reflection access for the class. Other means for serializing
     * and deserializing the class, such as a {@link TypeAdapter}, have to
     * be used.
     */
    BLOCK_ALL
  }

  /**
   * Blocks all reflection access to members of standard Java classes which are
   * not accessible by default. However, reflection access is still allowed for
   * classes for which all fields are accessible and which have an accessible
   * no-args constructor (or for which an {@link InstanceCreator} has been registered).
   *
   * <p>If this filter encounters a class other than a standard Java class it
   * returns {@link FilterResult#INDECISIVE}.
   *
   * <p>This filter is mainly intended to help enforcing the access checks of
   * Java Platform Module System. It allows detecting illegal access, even if
   * the used Java version would only log a warning, or is configured to open
   * packages for reflection. However, this filter <b>only works for Java 9 and
   * higher</b>, when using an older Java version its functionality will be
   * limited.
   *
   * <p>Note that this filter might not cover all standard Java classes. Currently
   * only classes in a {@code java.*} or {@code javax.*} package are considered. The
   * set of detected classes might be expanded in the future without prior notice.
   *
   * @see FilterResult#BLOCK_INACCESSIBLE
   */
  ReflectionAccessFilter BLOCK_INACCESSIBLE_JAVA = new ReflectionAccessFilter() {
    @Override public FilterResult check(Class<?> rawClass) {
      return ReflectionAccessFilterHelper.isJavaType(rawClass)
        ? FilterResult.BLOCK_INACCESSIBLE
        : FilterResult.INDECISIVE;
    }
  };

  /**
   * Blocks all reflection access to members of standard Java classes.
   *
   * <p>If this filter encounters a class other than a standard Java class it
   * returns {@link FilterResult#INDECISIVE}.
   *
   * <p>This filter is mainly intended to prevent depending on implementation
   * details of the Java platform and to help applications prepare for upgrading
   * to the Java Platform Module System.
   *
   * <p>Note that this filter might not cover all standard Java classes. Currently
   * only classes in a {@code java.*} or {@code javax.*} package are considered. The
   * set of detected classes might be expanded in the future without prior notice.
   *
   * @see #BLOCK_INACCESSIBLE_JAVA
   * @see FilterResult#BLOCK_ALL
   */
  ReflectionAccessFilter BLOCK_ALL_JAVA = new ReflectionAccessFilter() {
    @Override public FilterResult check(Class<?> rawClass) {
      return ReflectionAccessFilterHelper.isJavaType(rawClass)
        ? FilterResult.BLOCK_ALL
        : FilterResult.INDECISIVE;
    }
  };

  /**
   * Blocks all reflection access to members of standard Android classes.
   *
   * <p>If this filter encounters a class other than a standard Android class it
   * returns {@link FilterResult#INDECISIVE}.
   *
   * <p>This filter is mainly intended to prevent depending on implementation
   * details of the Android platform.
   *
   * <p>Note that this filter might not cover all standard Android classes. Currently
   * only classes in an {@code android.*} or {@code androidx.*} package, and standard
   * Java classes in a {@code java.*} or {@code javax.*} package are considered. The
   * set of detected classes might be expanded in the future without prior notice.
   *
   * @see FilterResult#BLOCK_ALL
   */
  ReflectionAccessFilter BLOCK_ALL_ANDROID = new ReflectionAccessFilter() {
    @Override public FilterResult check(Class<?> rawClass) {
      return ReflectionAccessFilterHelper.isAndroidType(rawClass)
        ? FilterResult.BLOCK_ALL
        : FilterResult.INDECISIVE;
    }
  };

  /**
   * Blocks all reflection access to members of classes belonging to programming
   * language platforms, such as Java, Android, Kotlin or Scala.
   *
   * <p>If this filter encounters a class other than a standard platform class it
   * returns {@link FilterResult#INDECISIVE}.
   *
   * <p>This filter is mainly intended to prevent depending on implementation
   * details of the platform classes.
   *
   * <p>Note that this filter might not cover all platform classes. Currently it
   * combines the filters {@link #BLOCK_ALL_JAVA} and {@link #BLOCK_ALL_ANDROID},
   * and checks for other language-specific platform classes like {@code kotlin.*}.
   * The set of detected classes might be expanded in the future without prior notice.
   *
   * @see FilterResult#BLOCK_ALL
   */
  ReflectionAccessFilter BLOCK_ALL_PLATFORM = new ReflectionAccessFilter() {
    @Override public FilterResult check(Class<?> rawClass) {
      return ReflectionAccessFilterHelper.isAnyPlatformType(rawClass)
        ? FilterResult.BLOCK_ALL
        : FilterResult.INDECISIVE;
    }
  };

  /**
   * Checks if reflection access should be allowed for a class.
   *
   * @param rawClass
   *    Class to check
   * @return
   *    Result indicating whether reflection access is allowed
   */
  FilterResult check(Class<?> rawClass);
}
