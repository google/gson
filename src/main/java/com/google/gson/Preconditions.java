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
 * A simple utility class used to check method Preconditions.
 *
 * <pre>
 * public long divideBy(long value) {
 *   Preconditions.checkArgument(value != 0);
 *   return this.value / value;
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class Preconditions {
  public static void checkNotNull(Object obj) {
    checkArgument(obj != null);
  }

  public static void checkArgument(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("condition failed: " + condition);
    }
  }
  
  public static void checkState(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("condition failed: " + condition);
    }
  }
}
