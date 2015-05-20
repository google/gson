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

package com.google.gson;

import com.google.gson.common.MoreAsserts;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Unit test for the {@link JsonObject} class.
 *
 * @author Joel Leitch
 */
public class JsonObjectTest extends TestCase {

  public void testAddingAndRemovingObjectProperties() throws Exception {
    JsonObject jsonObj = new JsonObject();
    String propertyName = "property";
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));

    JsonPrimitive value = new JsonPrimitive("blah");
    jsonObj.add(propertyName, value);
    assertEquals(value, jsonObj.get(propertyName));

    JsonElement removedElement = jsonObj.remove(propertyName);
    assertEquals(value, removedElement);
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));
  }

  public void testAddingNullPropertyValue() throws Exception {
    String propertyName = "property";
    JsonObject jsonObj = new JsonObject();
    jsonObj.add(propertyName, null);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.isJsonNull());
  }

  public void testAddingNullOrEmptyPropertyName() throws Exception {
    JsonObject jsonObj = new JsonObject();
    try {
      jsonObj.add(null, JsonNull.INSTANCE);
      fail("Should not allow null property names.");
    } catch (NullPointerException expected) { }

    jsonObj.add("", JsonNull.INSTANCE);
    jsonObj.add("   \t", JsonNull.INSTANCE);
  }

  public void testAddingBooleanProperties() throws Exception {
    String propertyName = "property";
    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, true);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.getAsBoolean());
  }

  public void testAddingStringProperties() throws Exception {
    String propertyName = "property";
    String value = "blah";

    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(value, jsonElement.getAsString());
  }

  public void testAddingCharacterProperties() throws Exception {
    String propertyName = "property";
    char value = 'a';

    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(String.valueOf(value), jsonElement.getAsString());
    assertEquals(value, jsonElement.getAsCharacter());
  }

  /**
   * From bug report http://code.google.com/p/google-gson/issues/detail?id=182
   */
  public void testPropertyWithQuotes() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.add("a\"b", new JsonPrimitive("c\"d"));
    String json = new Gson().toJson(jsonObj);
    assertEquals("{\"a\\\"b\":\"c\\\"d\"}", json);
  }

  /**
   * From issue 227.
   */
  public void testWritePropertyWithEmptyStringName() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.add("", new JsonPrimitive(true));
    assertEquals("{\"\":true}", new Gson().toJson(jsonObj));

  }

  public void testReadPropertyWithEmptyStringName() {
    JsonObject jsonObj = new JsonParser().parse("{\"\":true}").getAsJsonObject();
    assertEquals(true, jsonObj.get("").getAsBoolean());
  }

  public void testEqualsOnEmptyObject() {
    MoreAsserts.assertEqualsAndHashCode(new JsonObject(), new JsonObject());
  }

  public void testEqualsNonEmptyObject() {
    JsonObject a = new JsonObject();
    JsonObject b = new JsonObject();

    assertEquals(a, a);

    a.add("foo", new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("foo", new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add("bar", new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("bar", JsonNull.INSTANCE);
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  public void testConvenienceMethods() {
    JsonObject jsonObject = new JsonObject();

    // primitive
    JsonPrimitive jsonPrimitive = new JsonPrimitive(42);
    JsonPrimitive jsonPrimitiveTwo = new JsonPrimitive(43);
    jsonObject.add("primitive", jsonPrimitive);
    assertEquals(jsonPrimitive, jsonObject.getAsJsonPrimitive("primitive", jsonPrimitive));
    assertEquals(jsonPrimitive, jsonObject.getAsJsonPrimitive("primitive", jsonPrimitiveTwo));
    assertEquals(jsonPrimitive, jsonObject.getAsJsonPrimitive("nonexisting", jsonPrimitive));
    assertNotSame(jsonPrimitive, jsonObject.getAsJsonPrimitive("nonexisting", jsonPrimitiveTwo));

    // array
    JsonArray jsonArray = new JsonArray();
    jsonObject.add("empty_array", jsonArray);
    JsonArray jsonArrayTwo = new JsonArray();
    jsonArrayTwo.add(jsonPrimitive);
    jsonArrayTwo.add(jsonPrimitiveTwo);
    jsonObject.add("array_with_two_elements", jsonArrayTwo);
    assertEquals(jsonArray, jsonObject.getAsJsonArray("empty_array", jsonArray));
    assertEquals(jsonArray, jsonObject.getAsJsonArray("empty_array", jsonArrayTwo));
    assertEquals(jsonArray, jsonObject.getAsJsonArray("nonexisting", jsonArray));
    assertNotSame(jsonArray, jsonObject.getAsJsonArray("nonexisting", jsonArrayTwo));

    // object
    JsonObject jsonObjectTwo = new JsonObject();
    JsonObject jsonObjectThree = new JsonObject();
    jsonObject.add("empty_object", jsonObjectTwo);
    assertEquals(jsonObjectTwo, jsonObject.getAsJsonObject("empty_object", jsonObjectTwo));
    assertEquals(jsonObjectTwo, jsonObject.getAsJsonObject("empty_object", jsonObjectThree));
    assertEquals(jsonObjectTwo, jsonObject.getAsJsonObject("nonexisting", jsonObjectTwo));
    assertNotSame(jsonObjectTwo, jsonObject.getAsJsonObject("nonexisting", jsonObjectThree));

    // get with default
    assertEquals(jsonPrimitive, jsonObject.get("primitive", jsonPrimitiveTwo));
    assertEquals(jsonPrimitive, jsonObject.get("nonexisting", jsonPrimitive));
    assertNotSame(jsonPrimitive, jsonObject.get("nonexisting", jsonPrimitiveTwo));

    // boolean, Boolean
    jsonObject.addProperty("true_boolean", true);
    jsonObject.addProperty("false_boolean", false);
    JsonArray arrayWithOneBoolean = new JsonArray();
    arrayWithOneBoolean.add(new JsonPrimitive(true));
    jsonObject.add("array_with_one_boolean", arrayWithOneBoolean);
    assertTrue(jsonObject.getAsBoolean("true_boolean", true));
    assertTrue(jsonObject.getAsBoolean("true_boolean", false));
    assertFalse(jsonObject.getAsBoolean("false_boolean", true));
    assertFalse(jsonObject.getAsBoolean("false_boolean", false));
    assertTrue(jsonObject.getAsBoolean("nonexisting", true));
    assertFalse(jsonObject.getAsBoolean("nonexisting", false));
    try {
      jsonObject.getAsBoolean("empty_object", true);
      fail("Expected ClassCastException when attempting to parse JsonObject as a boolean");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsBoolean("empty_array", true);
      fail("Expected IllegalStateException when attempting to parse empty JsonArray as a boolean");
    } catch (IllegalStateException e) {
      // expected
    }
    assertTrue(jsonObject.getAsBoolean("array_with_one_boolean", false));
    try {
      jsonObject.getAsBoolean("array_with_two_elements", true);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a boolean");
    } catch (IllegalStateException e) {
      // expected
    }
    jsonObject.addProperty("true_boolean_wrapper", Boolean.TRUE);
    jsonObject.addProperty("false_boolean_wrapper", Boolean.FALSE);
    assertTrue(jsonObject.getAsBooleanWrapper("true_boolean_wrapper", Boolean.TRUE));
    assertTrue(jsonObject.getAsBooleanWrapper("true_boolean_wrapper", Boolean.FALSE));
    assertFalse(jsonObject.getAsBooleanWrapper("false_boolean_wrapper", Boolean.TRUE));
    assertFalse(jsonObject.getAsBooleanWrapper("false_boolean_wrapper", Boolean.FALSE));
    assertTrue(jsonObject.getAsBooleanWrapper("nonexisting", Boolean.TRUE));
    assertFalse(jsonObject.getAsBooleanWrapper("nonexisting", Boolean.FALSE));
    try {
      jsonObject.getAsBooleanWrapper("empty_object", Boolean.TRUE);
      fail("Expected exception when attempting to parse JsonObject as a Boolean");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsBooleanWrapper("empty_array", Boolean.TRUE);
      fail("Expected exception when attempting to parse empty JsonArray as a Boolean");
    } catch (IllegalStateException e) {
      // expected
    }
    assertTrue(jsonObject.getAsBooleanWrapper("array_with_one_boolean", Boolean.FALSE));
    try {
      jsonObject.getAsBooleanWrapper("array_with_two_elements", Boolean.TRUE);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a Boolean");
    } catch (IllegalStateException e) {
      // expected
    }

    // character
    jsonObject.addProperty("character", 'c');
    JsonArray arrayWithOneChar = new JsonArray();
    arrayWithOneChar.add(new JsonPrimitive('c'));
    jsonObject.add("array_with_one_char", arrayWithOneChar);
    assertEquals('c', jsonObject.getAsCharacter("character", 'c'));
    assertEquals('c', jsonObject.getAsCharacter("character", 'd'));
    assertEquals('c', jsonObject.getAsCharacter("nonexisting", 'c'));
    assertNotSame('c', jsonObject.getAsCharacter("nonexisting", 'd'));
    try {
      jsonObject.getAsCharacter("empty_object", 'c');
      fail("Expected exception when attempting to parse JsonObject as a char");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsCharacter("empty_array", 'c');
      fail("Expected exception when attempting to parse empty JsonArray as a char");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals('c', jsonObject.getAsCharacter("array_with_one_char", 'd'));
    try {
      jsonObject.getAsCharacter("array_with_two_elements", 'c');
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a char");
    } catch (IllegalStateException e) {
      // expected
    }

    // long, int, short, byte, Number, BigInteger
    jsonObject.addProperty("integer", 42);
    JsonArray arrayWithOneNumber = new JsonArray();
    arrayWithOneNumber.add(new JsonPrimitive(42));
    jsonObject.add("array_with_one_number", arrayWithOneNumber);
    assertEquals((long) 42, jsonObject.getAsLong("integer", (long) 42));
    assertEquals((long) 42, jsonObject.getAsLong("integer", (long) 43));
    assertEquals((long) 42, jsonObject.getAsLong("nonexisting", (long) 42));
    assertNotSame((long) 42, jsonObject.getAsLong("nonexisting", (long) 43));
    try {
      jsonObject.getAsLong("empty_object", (long) 42);
      fail("Expected exception when attempting to parse JsonObject as a long");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsLong("empty_array", (long) 42);
      fail("Expected exception when attempting to parse empty JsonArray as a long");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((long) 42, jsonObject.getAsLong("array_with_one_number", (long) 43));
    try {
      jsonObject.getAsLong("array_with_two_elements", (long) 42);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a long");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals(42, jsonObject.getAsInt("integer", 42));
    assertEquals(42, jsonObject.getAsInt("integer", 43));
    assertEquals(42, jsonObject.getAsInt("nonexisting", 42));
    assertNotSame(42, jsonObject.getAsInt("nonexisting", 43));
    try {
      jsonObject.getAsInt("empty_object", 42);
      fail("Expected exception when attempting to parse JsonObject as an integer");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsInt("empty_array", 42);
      fail("Expected exception when attempting to parse empty JsonArray as an integer");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals(42, jsonObject.getAsInt("array_with_one_number", 43));
    try {
      jsonObject.getAsInt("array_with_two_elements", 42);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as an integer");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((short) 42, jsonObject.getAsShort("integer", (short) 42));
    assertEquals((short) 42, jsonObject.getAsShort("integer", (short) 43));
    assertEquals((short) 42, jsonObject.getAsShort("nonexisting", (short) 42));
    assertNotSame((short) 42, jsonObject.getAsShort("nonexisting", (short) 43));
    try {
      jsonObject.getAsShort("empty_object", (short) 42);
      fail("Expected exception when attempting to parse JsonObject as a short");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsShort("empty_array", (short) 42);
      fail("Expected exception when attempting to parse empty JsonArray as a short");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((short) 42, jsonObject.getAsShort("array_with_one_number", (short) 43));
    try {
      jsonObject.getAsShort("array_with_two_elements", (short) 42);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a short");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((byte) 42, jsonObject.getAsByte("integer", (byte) 42));
    assertEquals((byte) 42, jsonObject.getAsByte("integer", (byte) 43));
    assertEquals((byte) 42, jsonObject.getAsByte("nonexisting", (byte) 42));
    assertNotSame((byte) 42, jsonObject.getAsByte("nonexisting", (byte) 43));
    try {
      jsonObject.getAsByte("empty_object", (byte) 42);
      fail("Expected exception when attempting to parse JsonObject as a byte");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsByte("empty_array", (byte) 42);
      fail("Expected exception when attempting to parse empty JsonArray as a byte");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((byte) 42, jsonObject.getAsByte("array_with_one_number", (byte) 43));
    try {
      jsonObject.getAsByte("array_with_two_elements", (byte) 42);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a byte");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((Number) 42, jsonObject.getAsNumber("integer", 42));
    assertEquals((Number) 42, jsonObject.getAsNumber("integer", 43));
    assertEquals((Number) 42, jsonObject.getAsNumber("nonexisting", 42));
    assertNotSame((Number) 42, jsonObject.getAsNumber("nonexisting", 43));
    try {
      jsonObject.getAsNumber("empty_object", 42);
      fail("Expected exception when attempting to parse JsonObject as a Number");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsNumber("empty_array", 42);
      fail("Expected exception when attempting to parse empty JsonArray as a Number");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((Number) 42, jsonObject.getAsNumber("array_with_one_number", 43));
    try {
      jsonObject.getAsNumber("array_with_two_elements", 42);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a Number");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals(BigInteger.valueOf(42), jsonObject.getAsBigInteger("integer", BigInteger.valueOf(42)));
    assertEquals(BigInteger.valueOf(42), jsonObject.getAsBigInteger("integer", BigInteger.valueOf(43)));
    assertEquals(BigInteger.valueOf(42), jsonObject.getAsBigInteger("nonexisting", BigInteger.valueOf(42)));
    assertNotSame(BigInteger.valueOf(42), jsonObject.getAsBigInteger("nonexisting", BigInteger.valueOf(43)));
    try {
      jsonObject.getAsBigInteger("empty_object", BigInteger.valueOf(42));
      fail("Expected exception when attempting to parse JsonObject as a BigInteger");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsBigInteger("empty_array", BigInteger.valueOf(42));
      fail("Expected exception when attempting to parse empty JsonArray as a BigInteger");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals(BigInteger.valueOf(42), jsonObject.getAsBigInteger("array_with_one_number", BigInteger.valueOf(43)));
    try {
      jsonObject.getAsBigInteger("array_with_two_elements", BigInteger.valueOf(42));
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a BigInteger");
    } catch (IllegalStateException e) {
      // expected
    }

    // double, float, BigDecimal
    jsonObject.addProperty("real", 3.1415);
    JsonArray arrayWithOneReal = new JsonArray();
    arrayWithOneReal.add(new JsonPrimitive(3.1415));
    jsonObject.add("array_with_one_real", arrayWithOneReal);
    assertEquals(3.1415, jsonObject.getAsDouble("real", 3.1415));
    assertEquals(3.1415, jsonObject.getAsDouble("real", 2.7183));
    assertEquals(3.1415, jsonObject.getAsDouble("nonexisting", 3.1415));
    assertNotSame(3.1415, jsonObject.getAsDouble("nonexisting", 2.7183));
    try {
      jsonObject.getAsDouble("empty_object", 3.1415);
      fail("Expected exception when attempting to parse JsonObject as a double");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsDouble("empty_array", 3.1415);
      fail("Expected exception when attempting to parse empty JsonArray as a double");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals(3.1415, jsonObject.getAsDouble("array_with_one_real", 2.7183));
    try {
      jsonObject.getAsDouble("array_with_two_elements", 3.1415);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a double");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((float) 3.1415, jsonObject.getAsFloat("real", (float) 3.1415));
    assertEquals((float) 3.1415, jsonObject.getAsFloat("real", (float) 2.7183));
    assertEquals((float) 3.1415, jsonObject.getAsFloat("nonexisting", (float) 3.1415));
    assertNotSame((float) 3.1415, jsonObject.getAsFloat("nonexisting", (float) 2.7183));
    try {
      jsonObject.getAsFloat("empty_object", (float) 3.1415);
      fail("Expected exception when attempting to parse JsonObject as a float");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsFloat("empty_array", (float) 3.1415);
      fail("Expected exception when attempting to parse empty JsonArray as a float");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals((float) 3.1415, jsonObject.getAsFloat("array_with_one_real", (float) 2.7183));
    try {
      jsonObject.getAsFloat("array_with_two_elements", (float) 3.1415);
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a float");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals(BigDecimal.valueOf(3.1415), jsonObject.getAsBigDecimal("real", BigDecimal.valueOf(3.1415)));
    assertEquals(BigDecimal.valueOf(3.1415), jsonObject.getAsBigDecimal("real", BigDecimal.valueOf(2.7183)));
    assertEquals(BigDecimal.valueOf(3.1415), jsonObject.getAsBigDecimal("nonexisting", BigDecimal.valueOf(3.1415)));
    assertNotSame(BigDecimal.valueOf(3.1415), jsonObject.getAsBigDecimal("nonexisting", BigDecimal.valueOf(2.7183)));
    try {
      jsonObject.getAsBigDecimal("empty_object", BigDecimal.valueOf(3.1415));
      fail("Expected exception when attempting to parse JsonObject as a BigDecimal");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsBigDecimal("empty_array", BigDecimal.valueOf(3.1415));
      fail("Expected exception when attempting to parse empty JsonArray as a BigDecimal");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals(BigDecimal.valueOf(3.1415), jsonObject.getAsBigDecimal("array_with_one_real", BigDecimal.valueOf(2.7183)));
    try {
      jsonObject.getAsBigDecimal("array_with_two_elements", BigDecimal.valueOf(3.1415));
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a BigDecimal");
    } catch (IllegalStateException e) {
      // expected
    }

    // string
    jsonObject.addProperty("string", "gson");
    JsonArray arrayWithOneString = new JsonArray();
    arrayWithOneString.add(new JsonPrimitive("gson"));
    jsonObject.add("array_with_one_string", arrayWithOneString);
    assertEquals("gson", jsonObject.getAsString("string", "gson"));
    assertEquals("gson", jsonObject.getAsString("string", "nosg"));
    assertEquals("gson", jsonObject.getAsString("nonexisting", "gson"));
    assertNotSame("gson", jsonObject.getAsString("nonexisting", "nosg"));
    try {
      jsonObject.getAsString("empty_object", "gson");
      fail("Expected exception when attempting to parse JsonObject as a String");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      jsonObject.getAsString("empty_array", "gson");
      fail("Expected exception when attempting to parse empty JsonArray as a String");
    } catch (IllegalStateException e) {
      // expected
    }
    assertEquals("gson", jsonObject.getAsString("array_with_one_string", "nosg"));
    try {
      jsonObject.getAsString("array_with_two_elements", "gson");
      fail("Expected IllegalStateException when attempting to parse JsonArray with two elements as a String");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  public void testDeepCopy() {
    JsonObject original = new JsonObject();
    JsonArray firstEntry = new JsonArray();
    original.add("key", firstEntry);

    JsonObject copy = original.deepCopy();
    firstEntry.add(new JsonPrimitive("z"));

    assertEquals(1, original.get("key").getAsJsonArray().size());
    assertEquals(0, copy.get("key").getAsJsonArray().size());
  }
}
