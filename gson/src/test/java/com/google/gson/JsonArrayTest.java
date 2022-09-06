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

import com.google.gson.common.MoreAsserts;
import junit.framework.TestCase;

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

    JsonPrimitive b = new JsonPrimitive("b");
    JsonElement oldValue = array.set(0, b);
    assertEquals(a, oldValue);
    assertEquals("b", array.get(0).getAsString());

    oldValue = array.set(0, null);
    assertEquals(b, oldValue);
    assertEquals(JsonNull.INSTANCE, array.get(0));

    oldValue = array.set(0, new JsonPrimitive("c"));
    assertEquals(JsonNull.INSTANCE, oldValue);
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

  public void testIsEmpty() {
    JsonArray array = new JsonArray();
    assertTrue(array.isEmpty());

    JsonPrimitive a = new JsonPrimitive("a");
    array.add(a);
    assertFalse(array.isEmpty());

    array.remove(0);
    assertTrue(array.isEmpty());
  }

  public void testFailedGetArrayValues() {
    JsonArray jsonArray = new JsonArray();
    jsonArray.add(JsonParser.parseString("{" + "\"key1\":\"value1\"," + "\"key2\":\"value2\"," + "\"key3\":\"value3\"," + "\"key4\":\"value4\"" + "}"));
    try {
      jsonArray.getAsBoolean();
      fail("expected getBoolean to fail");
    } catch (UnsupportedOperationException e) {
      assertEquals("Expected an exception message",
              "JsonObject", e.getMessage());
    }
    try {
      jsonArray.get(-1);
      fail("expected get to fail");
    } catch (IndexOutOfBoundsException e) {
      assertEquals("Expected an exception message",
              "Index -1 out of bounds for length 1", e.getMessage());
    }
    try {
      jsonArray.getAsString();
      fail("expected getString to fail");
    } catch (UnsupportedOperationException e) {
      assertEquals("Expected an exception message",
              "JsonObject", e.getMessage());
    }

    jsonArray.remove(0);
    jsonArray.add("hello");
    try {
      jsonArray.getAsDouble();
      fail("expected getDouble to fail");
    } catch (NumberFormatException e) {
      assertEquals("Expected an exception message",
              "For input string: \"hello\"", e.getMessage());
    }
    try {
      jsonArray.getAsInt();
      fail("expected getInt to fail");
    } catch (NumberFormatException e) {
      assertEquals("Expected an exception message",
              "For input string: \"hello\"", e.getMessage());
    }
    try {
      jsonArray.get(0).getAsJsonArray();
      fail("expected getJSONArray to fail");
    } catch (IllegalStateException e) {
      assertEquals("Expected an exception message",
              "Not a JSON Array: \"hello\"", e.getMessage());
    }
    try {
      jsonArray.getAsJsonObject();
      fail("expected getJSONObject to fail");
    } catch (IllegalStateException e) {
      assertEquals("Expected an exception message",
              "Not a JSON Object: [\"hello\"]", e.getMessage());
    }
    try {
      jsonArray.getAsLong();
      fail("expected getLong to fail");
    } catch (NumberFormatException e) {
      assertEquals("Expected an exception message",
              "For input string: \"hello\"", e.getMessage());
    }
  }

  public void testGetAs_WrongArraySize() {
    JsonArray jsonArray = new JsonArray();
    try {
      jsonArray.getAsByte();
      fail();
    } catch (IllegalStateException e) {
      assertEquals("Array must have size 1, but has size 0", e.getMessage());
    }

    jsonArray.add(true);
    jsonArray.add(false);
    try {
      jsonArray.getAsByte();
      fail();
    } catch (IllegalStateException e) {
      assertEquals("Array must have size 1, but has size 2", e.getMessage());
    }
  }
}
