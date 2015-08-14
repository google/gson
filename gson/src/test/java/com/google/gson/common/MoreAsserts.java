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

import junit.framework.Assert;

import java.util.Collection;

/**
 * Handy asserts that we wish were present in {@link Assert}
 * so that we didn't have to write them.
 *
 * @author Inderjeet Singh
 */
public class MoreAsserts {

  public static void assertEquals(int[] expected, int[] target) {
    if (expected == null) {
      Assert.assertNull(target);
    }
    Assert.assertEquals(expected.length, target.length);
    for (int i = 0; i < expected.length; ++i) {
      Assert.assertEquals(expected[i], target[i]);
    }
  }

  public static void assertEquals(Integer[] expected, Integer[] target) {
    if (expected == null) {
      Assert.assertNull(target);
    }
    Assert.assertEquals(expected.length, target.length);
    for (int i = 0; i < expected.length; ++i) {
      Assert.assertEquals(expected[i], target[i]);
    }
  }

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

}
