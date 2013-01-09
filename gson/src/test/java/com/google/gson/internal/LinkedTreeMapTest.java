/*
 * Copyright (C) 2012 Google Inc.
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

import com.google.gson.common.MoreAsserts;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for {@code LinkedTreeMap} class.
 *
 * @author Joel Leitch
 */
public class LinkedTreeMapTest extends TestCase {

  public void testPutAndGet() throws Exception {
    LinkedTreeMap<String, Integer> map = new LinkedTreeMap<String, Integer>();
    map.put("B", 2);
    map.put("A", 1);
    map.put("C", 3);

    assertTrue(map.containsKey("A"));
    assertTrue(map.containsKey("B"));
    assertTrue(map.containsKey("C"));
    assertFalse(map.containsKey("D"));

    assertEquals(1, (int) map.get("A"));
    assertEquals(2, (int) map.get("B"));
    assertEquals(3, (int) map.get("C"));
    assertEquals(3, map.entrySet().size());

    assertEquals(1, (int) map.put("A", 4));
    assertTrue(map.containsKey("A"));
    assertEquals(4, (int) map.get("A"));
    assertEquals(3, map.entrySet().size());

    // Ensure entry set size is same as map size
    assertEquals(map.size(), map.entrySet().size());
  }

  public void testGetAndContainsNullKey() throws Exception {
    LinkedTreeMap<String, Integer> map = new LinkedTreeMap<String, Integer>();
    assertFalse(map.containsKey(null));
    assertNull(map.get(null));

    map.put("A", 1);
    assertFalse(map.containsKey(null));
    assertNull(map.get(null));
  }

  public void testDisallowPutForNullKeys() throws Exception {
    LinkedTreeMap<String, Integer> map = new LinkedTreeMap<String, Integer>();
    try {
      map.put(null, 1);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testSingleElement() throws Exception {
    LinkedTreeMap<String, Integer> map = new LinkedTreeMap<String, Integer>();
    map.put("A", 1);
    assertEquals(1, map.size());

    assertEquals(1, (int) map.get("A"));
    map.remove("A");
    assertEquals(0, map.size());

    // Ensure the map and entry set are empty
    assertTrue(map.entrySet().isEmpty());
    assertTrue(map.isEmpty());
  }

  public void testAddAndRemove() throws Exception {
    LinkedTreeMap<String, Integer> map = new LinkedTreeMap<String, Integer>();
    map.put("A", 1);
    map.put("B", 2);
    map.put("C", 3);
    map.put("D", 4);
    map.put("E", 5);
    map.put("F", 6);

    assertEquals(3, (int) map.remove("C"));
    assertEquals(5, map.size());
    assertIterationOrder(map.entrySet(),
        new String[] { "A", "B", "D", "E", "F" }, new int[] { 1, 2, 4, 5, 6 });

    // Remove a non-existent key
    assertNull(map.remove("G"));
    assertEquals(5, map.size());

    // Remove the first element
    assertEquals(1, (int) map.remove("A"));
    assertIterationOrder(map.entrySet(),
        new String[] { "B", "D", "E", "F" }, new int[] { 2, 4, 5, 6 });

    // Remove the last element
    assertEquals(6, (int) map.remove("F"));
    assertIterationOrder(map.entrySet(),
        new String[] { "B", "D", "E" }, new int[] { 2, 4, 5 });
  }

  public void testInsertionOrderPreserved() throws Exception {
    LinkedTreeMap<String, Integer> map = new LinkedTreeMap<String, Integer>();
    String[] keys = { "B", "A", "D", "C", "Z", "W", "E", "F", "T" };
    int[] values = new int[keys.length];
    for (int i = 0; i < keys.length; ++i) {
      values[i] = i;
      map.put(keys[i], i);
    }

    Set<Map.Entry<String,Integer>> entries = map.entrySet();
    assertEquals(keys.length, entries.size());
    assertIterationOrder(entries, keys, values);
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

  private void assertIterationOrder(Set<Map.Entry<String, Integer>> entries, String[] keys, int[] values) {
    int i = 0;
    for (Iterator<Map.Entry<String, Integer>> iterator = entries.iterator(); iterator.hasNext(); ++i) {
      Map.Entry<String, Integer> entry = iterator.next();
      assertEquals(keys[i], entry.getKey());
      assertEquals(values[i], (int) entry.getValue());
    }
  }
}
