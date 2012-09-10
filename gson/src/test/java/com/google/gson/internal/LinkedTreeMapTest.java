/*
 * Copyright (C) 2012 Google Inc.
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
import java.util.Arrays;
import junit.framework.TestCase;

public final class LinkedTreeMapTest extends TestCase {
  public void testIterationOrder() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    assertIterationOrder(map.keySet(), "a", "c", "b");
    assertIterationOrder(map.values(), "android", "cola", "bbq");
  }

  // TODO: test contains with non-string key

  private <T> void assertIterationOrder(Iterable<T> actual, T... expected) {
    ArrayList<T> actualList = new ArrayList<T>();
    for (T t : actual) {
      actualList.add(t);
    }
    assertEquals(Arrays.asList(expected), actualList);
  }
}
