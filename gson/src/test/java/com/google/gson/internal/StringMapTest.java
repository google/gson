/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

public final class StringMapTest extends TestCase {
  public void testFallbackFromTooManyCollisions() {
    int count = 10000;
    StringMap<Integer> map = new StringMap<Integer>();
    int index = 0;
    List<String> collidingStrings = collidingStrings(1 << 20, count);
    for (String string : collidingStrings) {
      map.put(string, index++);
    }
    assertEquals(collidingStrings.size(), map.size());
    Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
    for (int i = 0; i < count; i++) {
      Map.Entry<String, Integer> entry = iterator.next();
      assertEquals(collidingStrings.get(i), entry.getKey());
      assertEquals(Integer.valueOf(i), entry.getValue());
    }
  }

  /**
   * @param h0 the hash code of the generated strings
   */
  private List<String> collidingStrings(int h0, int count) {
    List<String> result = new ArrayList<String>(count);
    int p1 = 31;
    int p0 = 31 * 31;
    int maxChar = Character.MAX_VALUE;
    for (char c0 = 0; c0 <= maxChar && c0 <= h0 / p0; c0++) {
      int h1 = h0 - c0 * p0;
      for (char c1 = 0; c1 <= maxChar && c1 <= h1 / p1; c1++) {
        int h2 = h1 - c1 * p1;
        char c2 = (char) h2;
        if (h2 != c2) {
          continue;
        }
        result.add(new String(new char[] { c0, c1, c2 } ));
        if (result.size() == count) {
          return result;
        }
      }
    }
    throw new IllegalArgumentException("Couldn't find " + count + " strings with hashCode " + h0);
  }
}
