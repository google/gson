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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.common.MoreAsserts;
import java.math.BigInteger;
import org.junit.Test;

/**
 * @author Jesse Wilson
 */
public final class JsonArrayTest {

  @Test
  public void testEqualsOnEmptyArray() {
    MoreAsserts.assertEqualsAndHashCode(new JsonArray(), new JsonArray());
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testIsEmpty() {
    JsonArray array = new JsonArray();
    assertTrue(array.isEmpty());

    JsonPrimitive a = new JsonPrimitive("a");
    array.add(a);
    assertFalse(array.isEmpty());

    array.remove(0);
    assertTrue(array.isEmpty());
  }

  @Test
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

  @Test
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

  @Test
  public void testStringPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add("Hello");
    jsonArray.add("Goodbye");
    jsonArray.add("Thank you");
    jsonArray.add((String) null);
    jsonArray.add("Yes");

    assertEquals("[\"Hello\",\"Goodbye\",\"Thank you\",null,\"Yes\"]", jsonArray.toString());
  }

  @Test
  public void testIntegerPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    int x = 1;
    jsonArray.add(x);

    x = 2;
    jsonArray.add(x);

    x = -3;
    jsonArray.add(x);

    jsonArray.add((Integer) null);

    x = 4;
    jsonArray.add(x);

    x = 0;
    jsonArray.add(x);

    assertEquals("[1,2,-3,null,4,0]", jsonArray.toString());
  }

  @Test
  public void testDoublePrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    double x = 1.0;
    jsonArray.add(x);

    x = 2.13232;
    jsonArray.add(x);

    x = 0.121;
    jsonArray.add(x);

    jsonArray.add((Double) null);

    x = -0.00234;
    jsonArray.add(x);

    jsonArray.add((Double) null);

    assertEquals("[1.0,2.13232,0.121,null,-0.00234,null]", jsonArray.toString());
  }

  @Test
  public void testBooleanPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add(true);
    jsonArray.add(true);
    jsonArray.add(false);
    jsonArray.add(false);
    jsonArray.add((Boolean) null);
    jsonArray.add(true);

    assertEquals("[true,true,false,false,null,true]", jsonArray.toString());
  }

  @Test
  public void testCharPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add('a');
    jsonArray.add('e');
    jsonArray.add('i');
    jsonArray.add((char) 111);
    jsonArray.add((Character) null);
    jsonArray.add('u');
    jsonArray.add("and sometimes Y");

    assertEquals("[\"a\",\"e\",\"i\",\"o\",null,\"u\",\"and sometimes Y\"]", jsonArray.toString());
  }

  @Test
  public void testMixedPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add('a');
    jsonArray.add("apple");
    jsonArray.add(12121);
    jsonArray.add((char) 111);

    jsonArray.add((Boolean) null);
    assertEquals(JsonNull.INSTANCE, jsonArray.get(jsonArray.size() - 1));

    jsonArray.add((Character) null);
    assertEquals(JsonNull.INSTANCE, jsonArray.get(jsonArray.size() - 1));

    jsonArray.add(12.232);
    jsonArray.add(BigInteger.valueOf(2323));

    assertEquals("[\"a\",\"apple\",12121,\"o\",null,null,12.232,2323]", jsonArray.toString());
  }

  @Test
  public void testNullPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add((Character) null);
    jsonArray.add((Boolean) null);
    jsonArray.add((Integer) null);
    jsonArray.add((Double) null);
    jsonArray.add((Float) null);
    jsonArray.add((BigInteger) null);
    jsonArray.add((String) null);
    jsonArray.add((Boolean) null);
    jsonArray.add((Number) null);

    assertEquals("[null,null,null,null,null,null,null,null,null]", jsonArray.toString());
    for (int i = 0; i < jsonArray.size(); i++) {
      // Verify that they are actually a JsonNull and not a Java null
      assertEquals(JsonNull.INSTANCE, jsonArray.get(i));
    }
  }

  @Test
  public void testNullJsonElementAddition() {
    JsonArray jsonArray = new JsonArray();
    jsonArray.add((JsonElement) null);
    assertEquals(JsonNull.INSTANCE, jsonArray.get(0));
  }

  @Test
  public void testSameAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add('a');
    jsonArray.add('a');
    jsonArray.add(true);
    jsonArray.add(true);
    jsonArray.add(1212);
    jsonArray.add(1212);
    jsonArray.add(34.34);
    jsonArray.add(34.34);
    jsonArray.add((Boolean) null);
    jsonArray.add((Boolean) null);

    assertEquals("[\"a\",\"a\",true,true,1212,1212,34.34,34.34,null,null]", jsonArray.toString());
  }
}
