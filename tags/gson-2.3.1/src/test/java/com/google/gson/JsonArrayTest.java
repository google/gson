/*
 * Copyright (C) 2011 Google Inc.
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

import junit.framework.TestCase;

import com.google.gson.common.MoreAsserts;

/**
 * @author Jesse Wilson
 */
public final class JsonArrayTest extends TestCase {

  public void testEqualsOnEmptyArray() {
    MoreAsserts.assertEqualsAndHashCode(new JsonArray(), new JsonArray());
  }

  public void testEqualsNonEmptyArray() {
    JsonArray a = new JsonArray();
    JsonArray b = new JsonArray();

    assertEquals(a, a);

    a.add(new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add(new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add(new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add(JsonNull.INSTANCE);
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  public void testRemove() {
    JsonArray array = new JsonArray();
    try {
      array.remove(0);
      fail();
    } catch (IndexOutOfBoundsException expected) {}
    JsonPrimitive a = new JsonPrimitive("a");
    array.add(a);
    assertTrue(array.remove(a));
    assertFalse(array.contains(a));
    array.add(a);
    array.add(new JsonPrimitive("b"));
    assertEquals("b", array.remove(1).getAsString());
    assertEquals(1, array.size());
    assertTrue(array.contains(a));
  }

  public void testSet() {
    JsonArray array = new JsonArray();
    try {
      array.set(0, new JsonPrimitive(1));
      fail();
    } catch (IndexOutOfBoundsException expected) {}
    JsonPrimitive a = new JsonPrimitive("a");
    array.add(a);
    array.set(0, new JsonPrimitive("b"));
    assertEquals("b", array.get(0).getAsString());
    array.set(0, null);
    assertNull(array.get(0));
    array.set(0, new JsonPrimitive("c"));
    assertEquals("c", array.get(0).getAsString());
    assertEquals(1, array.size());
  }

  public void testDeepCopy() {
    JsonArray original = new JsonArray();
    JsonArray firstEntry = new JsonArray();
    original.add(firstEntry);

    JsonArray copy = original.deepCopy();
    original.add(new JsonPrimitive("y"));

    assertEquals(1, copy.size());
    firstEntry.add(new JsonPrimitive("z"));

    assertEquals(1, original.get(0).getAsJsonArray().size());
    assertEquals(0, copy.get(0).getAsJsonArray().size());
  }
}
