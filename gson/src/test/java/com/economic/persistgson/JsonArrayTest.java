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

package com.economic.persistgson;

import junit.framework.TestCase;

import com.economic.persistgson.common.MoreAsserts;

/**
 * @author Jesse Wilson
 */
public final class JsonArrayTest extends TestCase {

  public void testEqualsOnEmptyArray() {
    MoreAsserts.assertEqualsAndHashCode(new com.economic.persistgson.JsonArray(), new com.economic.persistgson.JsonArray());
  }

  public void testEqualsNonEmptyArray() {
    com.economic.persistgson.JsonArray a = new com.economic.persistgson.JsonArray();
    com.economic.persistgson.JsonArray b = new com.economic.persistgson.JsonArray();

    assertEquals(a, a);

    a.add(new com.economic.persistgson.JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add(new com.economic.persistgson.JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add(new com.economic.persistgson.JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add(JsonNull.INSTANCE);
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  public void testRemove() {
    com.economic.persistgson.JsonArray array = new com.economic.persistgson.JsonArray();
    try {
      array.remove(0);
      fail();
    } catch (IndexOutOfBoundsException expected) {}
    com.economic.persistgson.JsonPrimitive a = new com.economic.persistgson.JsonPrimitive("a");
    array.add(a);
    assertTrue(array.remove(a));
    assertFalse(array.contains(a));
    array.add(a);
    array.add(new com.economic.persistgson.JsonPrimitive("b"));
    assertEquals("b", array.remove(1).getAsString());
    assertEquals(1, array.size());
    assertTrue(array.contains(a));
  }

  public void testSet() {
    com.economic.persistgson.JsonArray array = new com.economic.persistgson.JsonArray();
    try {
      array.set(0, new com.economic.persistgson.JsonPrimitive(1));
      fail();
    } catch (IndexOutOfBoundsException expected) {}
    com.economic.persistgson.JsonPrimitive a = new com.economic.persistgson.JsonPrimitive("a");
    array.add(a);
    array.set(0, new com.economic.persistgson.JsonPrimitive("b"));
    assertEquals("b", array.get(0).getAsString());
    array.set(0, null);
    assertNull(array.get(0));
    array.set(0, new com.economic.persistgson.JsonPrimitive("c"));
    assertEquals("c", array.get(0).getAsString());
    assertEquals(1, array.size());
  }

  public void testDeepCopy() {
    com.economic.persistgson.JsonArray original = new com.economic.persistgson.JsonArray();
    com.economic.persistgson.JsonArray firstEntry = new com.economic.persistgson.JsonArray();
    original.add(firstEntry);

    JsonArray copy = original.deepCopy();
    original.add(new com.economic.persistgson.JsonPrimitive("y"));

    assertEquals(1, copy.size());
    firstEntry.add(new JsonPrimitive("z"));

    assertEquals(1, original.get(0).getAsJsonArray().size());
    assertEquals(0, copy.get(0).getAsJsonArray().size());
  }
}
