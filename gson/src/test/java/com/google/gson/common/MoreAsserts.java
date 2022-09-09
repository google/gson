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

package com.google.gson.common;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;

/**
 * Handy asserts that we wish were present in {@link Assert}
 * so that we didn't have to write them.
 *
 * @author Inderjeet Singh
 */
public class MoreAsserts {

  /**
   * Asserts that the specified {@code value} is not present in {@code collection}
   * @param collection the collection to look into
   * @param value the value that needs to be checked for presence
   */
  public static <T> void assertContains(Collection<T> collection, T value) {
    for (T entry : collection) {
      if (entry.equals(value)) {
        return;
      }
    }
    Assert.fail(value + " not present in " + collection);
  }

  public static void assertEqualsAndHashCode(Object a, Object b) {
    Assert.assertTrue(a.equals(b));
    Assert.assertTrue(b.equals(a));
    Assert.assertEquals(a.hashCode(), b.hashCode());
    Assert.assertFalse(a.equals(null));
    Assert.assertFalse(a.equals(new Object()));
  }

  private static boolean isProtectedOrPublic(Method method) {
    int modifiers = method.getModifiers();
    return Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers);
  }

  private static String getMethodSignature(Method method) {
    StringBuilder builder = new StringBuilder(method.getName());
    builder.append('(');

    String sep = "";
    for (Class<?> paramType : method.getParameterTypes()) {
      builder.append(sep).append(paramType.getName());
      sep = ",";
    }

    builder.append(')');
    return builder.toString();
  }

  /**
   * Asserts that {@code subClass} overrides all protected and public methods declared by
   * {@code baseClass} except for the ones whose signatures are in {@code ignoredMethods}.
   */
  public static void assertOverridesMethods(Class<?> baseClass, Class<?> subClass, List<String> ignoredMethods) {
    Set<String> requiredOverriddenMethods = new LinkedHashSet<>();
    for (Method method : baseClass.getDeclaredMethods()) {
      // Note: Do not filter out `final` methods; maybe they should not be `final` and subclass needs
      // to override them
      if (isProtectedOrPublic(method)) {
        requiredOverriddenMethods.add(getMethodSignature(method));
      }
    }

    for (Method method : subClass.getDeclaredMethods()) {
      requiredOverriddenMethods.remove(getMethodSignature(method));
    }

    for (String ignoredMethod : ignoredMethods) {
      boolean foundIgnored = requiredOverriddenMethods.remove(ignoredMethod);
      if (!foundIgnored) {
        throw new IllegalArgumentException("Method '" + ignoredMethod + "' does not exist or is already overridden");
      }
    }

    if (!requiredOverriddenMethods.isEmpty()) {
      Assert.fail(subClass.getSimpleName() + " must override these methods: " + requiredOverriddenMethods);
    }
  }
}
