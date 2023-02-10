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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.common.MoreAsserts;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.junit.Test;

public final class LinkedTreeMapTest {

  @Test
  public void testIterationOrder() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    assertIterationOrder(map.keySet(), "a", "c", "b");
    assertIterationOrder(map.values(), "android", "cola", "bbq");
  }

  @Test
  public void testRemoveRootDoesNotDoubleUnlink() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
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

  @Test
  public void testPutNullKeyFails() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    try {
      map.put(null, "android");
      fail();
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testPutNonComparableKeyFails() {
    LinkedTreeMap<Object, String> map = new LinkedTreeMap<>();
    try {
      map.put(new Object(), "android");
      fail();
    } catch (ClassCastException expected) {}
  }

  @Test
  public void testPutNullValue() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", null);

    assertThat(map).hasSize(1);
    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.containsValue(null)).isTrue();
    assertThat(map.get("a")).isNull();
  }

  @Test
  public void testPutNullValue_Forbidden() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>(false);
    try {
      map.put("a", null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e.getMessage()).isEqualTo("value == null");
    }
    assertThat(map).hasSize(0);
    assertThat(map).doesNotContainKey("a");
    assertThat(map.containsValue(null)).isFalse();
  }

  @Test
  public void testEntrySetValueNull() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "1");
    assertThat(map.get("a")).isEqualTo("1");
    Entry<String, String> entry = map.entrySet().iterator().next();
    assertThat(entry.getKey()).isEqualTo("a");
    assertThat(entry.getValue()).isEqualTo("1");
    entry.setValue(null);
    assertThat(entry.getValue()).isNull();

    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.containsValue(null)).isTrue();
    assertThat(map.get("a")).isNull();
  }


  @Test
  public void testEntrySetValueNull_Forbidden() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>(false);
    map.put("a", "1");
    Entry<String, String> entry = map.entrySet().iterator().next();
    try {
      entry.setValue(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e.getMessage()).isEqualTo("value == null");
    }
    assertThat(entry.getValue()).isEqualTo("1");
    assertThat(map.get("a")).isEqualTo("1");
    assertThat(map.containsValue(null)).isFalse();
  }

  @Test
  public void testContainsNonComparableKeyReturnsFalse() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "android");
    assertThat(map).doesNotContainKey(new Object());
  }

  @Test
  public void testContainsNullKeyIsAlwaysFalse() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    assertThat(map.containsKey(null)).isFalse();
    map.put("a", "android");
    assertThat(map.containsKey(null)).isFalse();
  }

  @Test
  public void testPutOverrides() throws Exception {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    assertThat(map.put("d", "donut")).isNull();
    assertThat(map.put("e", "eclair")).isNull();
    assertThat(map.put("f", "froyo")).isNull();
    assertThat(map).hasSize(3);

    assertThat(map.get("d")).isEqualTo("donut");
    assertThat(map.put("d", "done")).isEqualTo("donut");
    assertThat(map).hasSize(3);
  }

  @Test
  public void testEmptyStringValues() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "");
    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.get("a")).isEqualTo("");
  }

  @Test
  public void testLargeSetOfRandomKeys() throws Exception {
    Random random = new Random(1367593214724L);
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    String[] keys = new String[1000];
    for (int i = 0; i < keys.length; i++) {
      keys[i] = Integer.toString(Math.abs(random.nextInt()), 36) + "-" + i;
      map.put(keys[i], "" + i);
    }

    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      assertThat(map.containsKey(key)).isTrue();
      assertThat(map.get(key)).isEqualTo("" + i);
    }
  }

  @Test
  public void testClear() {
    LinkedTreeMap<String, String> map = new LinkedTreeMap<>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    map.clear();
    assertIterationOrder(map.keySet());
    assertThat(map).hasSize(0);
  }

  @Test
  public void testEqualsAndHashCode() throws Exception {
    LinkedTreeMap<String, Integer> map1 = new LinkedTreeMap<>();
    map1.put("A", 1);
    map1.put("B", 2);
    map1.put("C", 3);
    map1.put("D", 4);

    LinkedTreeMap<String, Integer> map2 = new LinkedTreeMap<>();
    map2.put("C", 3);
    map2.put("B", 2);
    map2.put("D", 4);
    map2.put("A", 1);

    MoreAsserts.assertEqualsAndHashCode(map1, map2);
  }

  @Test
  public void testJavaSerialization() throws IOException, ClassNotFoundException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    Map<String, Integer> map = new LinkedTreeMap<>();
    map.put("a", 1);
    objOut.writeObject(map);
    objOut.close();

    ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
    @SuppressWarnings("unchecked")
    Map<String, Integer> deserialized = (Map<String, Integer>) objIn.readObject();
    assertThat(deserialized).isEqualTo(Collections.singletonMap("a", 1));
  }

  @SuppressWarnings("varargs")
  @SafeVarargs
  private final <T> void assertIterationOrder(Iterable<T> actual, T... expected) {
    ArrayList<T> actualList = new ArrayList<>();
    for (T t : actual) {
      actualList.add(t);
    }
    assertThat(actualList).isEqualTo(Arrays.asList(expected));
  }
}
