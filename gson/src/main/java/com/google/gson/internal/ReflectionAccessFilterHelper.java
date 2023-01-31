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

package com.google.gson.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;

import com.google.gson.ReflectionAccessFilter;
import com.google.gson.ReflectionAccessFilter.FilterResult;

/**
 * Internal helper class for {@link ReflectionAccessFilter}.
 */
public class ReflectionAccessFilterHelper {
  private ReflectionAccessFilterHelper() { }

  // Platform type detection is based on Moshi's Util.isPlatformType(Class)
  // See https://github.com/square/moshi/blob/3c108919ee1cce88a433ffda04eeeddc0341eae7/moshi/src/main/java/com/squareup/moshi/internal/Util.java#L141

  public static boolean isJavaType(Class<?> c) {
    return isJavaType(c.getName());
  }

  private static boolean isJavaType(String className) {
    return className.startsWith("java.") || className.startsWith("javax.");
  }

  public static boolean isAndroidType(Class<?> c) {
    return isAndroidType(c.getName());
  }

  private static boolean isAndroidType(String className) {
    return className.startsWith("android.")
        || className.startsWith("androidx.")
        || isJavaType(className);
  }

  public static boolean isAnyPlatformType(Class<?> c) {
    String className = c.getName();
    return isAndroidType(className) // Covers Android and Java
      || className.startsWith("kotlin.")
      || className.startsWith("kotlinx.")
      || className.startsWith("scala.");
  }

  /**
   * Gets the result of applying all filters until the first one returns a result
   * other than {@link FilterResult#INDECISIVE}, or {@link FilterResult#ALLOW} if
   * the list of filters is empty or all returned {@code INDECISIVE}.
   */
  public static FilterResult getFilterResult(List<ReflectionAccessFilter> reflectionFilters, Class<?> c) {
    for (ReflectionAccessFilter filter : reflectionFilters) {
      FilterResult result = filter.check(c);
      if (result != FilterResult.INDECISIVE) {
        return result;
      }
    }
    return FilterResult.ALLOW;
  }

  /**
   * See {@link AccessibleObject#canAccess(Object)} (Java >= 9)
   */
  public static boolean canAccess(AccessibleObject accessibleObject, Object object) {
    return AccessChecker.INSTANCE.canAccess(accessibleObject, object);
  }

  private static abstract class AccessChecker {
    public static final AccessChecker INSTANCE;
    static {
      AccessChecker accessChecker = null;
      // TODO: Ideally should use Multi-Release JAR for this version specific code
      if (JavaVersion.isJava9OrLater()) {
        try {
          final Method canAccessMethod = AccessibleObject.class.getDeclaredMethod("canAccess", Object.class);
          accessChecker = new AccessChecker() {
            @Override public boolean canAccess(AccessibleObject accessibleObject, Object object) {
              try {
                return (Boolean) canAccessMethod.invoke(accessibleObject, object);
              } catch (Exception e) {
                throw new RuntimeException("Failed invoking canAccess", e);
              }
            }
          };
        } catch (NoSuchMethodException ignored) {
        }
      }

      if (accessChecker == null) {
        accessChecker = new AccessChecker() {
          @Override public boolean canAccess(AccessibleObject accessibleObject, Object object) {
            // Cannot determine whether object can be accessed, so assume it can be accessed
            return true;
          }
        };
      }
      INSTANCE = accessChecker;
    }

    public abstract boolean canAccess(AccessibleObject accessibleObject, Object object);
  }
}
