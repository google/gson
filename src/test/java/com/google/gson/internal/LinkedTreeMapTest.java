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
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

import com.google.gson.common.MoreAsserts;

public final class LinkedTreeMapTest extends TestCase {

  public void testIterationOrder() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    assertIterationOrder(map.keySet(), "a", "c", "b");
    assertIterationOrder(map.values(), "android", "cola", "bbq");
  }

  public void testRemoveRootDoesNotDoubleUnlink() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertIterationOrder(map.keySet(), "a", "c");
  }

  public void testPutNullKeyFails() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    try {
      map.put(null, "android");
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testPutNonComparableKeyFails() {
    LinkedTreeMap<Object, String> map = new LinkedTreeMap<Object, String>();
    try {
      map.put(new Object(), "android");
      fail();
    } catch (ClassCastException expected) {}
  }

  public void testContainsNonComparableKeyReturnsFalse() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    map.put("a", "android");
    assertFalse(map.containsKey(new Object()));
  }

  public void testContainsNullKeyIsAlwaysFalse() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    map.put("a", "android");
    assertFalse(map.containsKey(null));
  }

  public void testPutOverrides() throws Exception {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    assertNull(map.put("d", "donut"));
    assertNull(map.put("e", "eclair"));
    assertNull(map.put("f", "froyo"));
    assertEquals(3, map.size());

    assertEquals("donut", map.get("d"));
    assertEquals("donut", map.put("d", "done"));
    assertEquals(3, map.size());
  }

  public void testEmptyStringValues() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    map.put("a", "");
    assertTrue(map.containsKey("a"));
    assertEquals("", map.get("a"));
  }

  public void testLargeSetOfRandomKeys() throws Exception {
    Random random = new Random(1367593214724L);
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    String[] keys = new String[1000];
    for (int i = 0; i < keys.length; i++) {
      keys[i] = Integer.toString(Math.abs(random.nextInt()), 36) + "-" + i;
      map.put(keys[i], "" + i);
    }

    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      assertTrue(map.containsKey(key));
      assertEquals("" + i, map.get(key));
    }
  }

  public void testClear() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<String, String>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    map.clear();
    assertIterationOrder(map.keySet());
    assertEquals(0, map.size());
  }

  public void testEqualsAndHashCode() throws Exception {
    LinkedTreeMap<String, Integer> map1 = new LinkedTreeMap<String, Integer>();
    map1.put("A", 1);
    map1.put("B", 2);
    map1.put("C", 3);
    map1.put("D", 4);

    LinkedTreeMap<String, Integer> map2 = new LinkedTreeMap<String, Integer>();
    map2.put("C", 3);
    map2.put("B", 2);
    map2.put("D", 4);
    map2.put("A", 1);

    MoreAsserts.assertEqualsAndHashCode(map1, map2);
  }

  private <T> void assertIterationOrder(Iterable<T> actual, T... expected) {
    ArrayList<T> actualList = new ArrayList<T>();
    for (T t : actual) {
      actualList.add(t);
    }
    assertEquals(Arrays.asList(expected), actualList);
  }
}
