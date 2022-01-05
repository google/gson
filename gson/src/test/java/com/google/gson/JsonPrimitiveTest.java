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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.common.MoreAsserts;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the {@link JsonPrimitive} class.
 *
 * @author Joel Leitch
 */
 class JsonPrimitiveTest {

  @SuppressWarnings("unused")
  @Test
  void testNulls() {
    try {
      new JsonPrimitive((Boolean) null);
      fail();
    } catch (NullPointerException ignored) {
    }
    try {
      new JsonPrimitive((Number) null);
      fail();
    } catch (NullPointerException ignored) {
    }
    try {
      new JsonPrimitive((String) null);
      fail();
    } catch (NullPointerException ignored) {
    }
    try {
      new JsonPrimitive((Character) null);
      fail();
    } catch (NullPointerException ignored) {
    }
  }

  @Test
  void testBoolean() throws Exception {
    JsonPrimitive json = new JsonPrimitive(Boolean.TRUE);

    assertTrue(json.isBoolean());
    assertTrue(json.getAsBoolean());

    // Extra support for booleans
    json = new JsonPrimitive(1);
    assertFalse(json.getAsBoolean());

    json = new JsonPrimitive("1");
    assertFalse(json.getAsBoolean());

    json = new JsonPrimitive("true");
    assertTrue(json.getAsBoolean());

    json = new JsonPrimitive("TrUe");
    assertTrue(json.getAsBoolean());

    json = new JsonPrimitive("1.3");
    assertFalse(json.getAsBoolean());
  }

  @Test
  void testParsingStringAsBoolean() throws Exception {
    JsonPrimitive json = new JsonPrimitive("true");

    assertFalse(json.isBoolean());
    assertTrue(json.getAsBoolean());
  }

  @Test
  void testParsingStringAsNumber() throws Exception {
    JsonPrimitive json = new JsonPrimitive("1");

    assertFalse(json.isNumber());
    assertEquals(1D, json.getAsDouble(), 0.00001);
    assertEquals(1F, json.getAsFloat(), 0.00001);
    assertEquals(1, json.getAsInt());
    assertEquals(1L, json.getAsLong());
    assertEquals((short) 1, json.getAsShort());
    assertEquals((byte) 1, json.getAsByte());
    assertEquals(new BigInteger("1"), json.getAsBigInteger());
    assertEquals(new BigDecimal("1"), json.getAsBigDecimal());
  }

  @Test
  void testStringsAndChar() throws Exception {
    JsonPrimitive json = new JsonPrimitive("abc");
    assertTrue(json.isString());
    assertEquals('a', json.getAsCharacter());
    assertEquals("abc", json.getAsString());

    json = new JsonPrimitive('z');
    assertTrue(json.isString());
    assertEquals('z', json.getAsCharacter());
    assertEquals("z", json.getAsString());

    json = new JsonPrimitive(true);
    assertEquals("true", json.getAsString());
  }

  @Test
  void testExponential() throws Exception {
    JsonPrimitive json = new JsonPrimitive("1E+7");

    assertEquals(new BigDecimal("1E+7"), json.getAsBigDecimal());
    assertEquals(1E+7, json.getAsDouble(), 0.00001);
    assertEquals(1E+7, json.getAsDouble(), 0.00001);

    try {
      json.getAsInt();
      fail("Integers can not handle exponents like this.");
    } catch (NumberFormatException expected) { }
  }

  @Test
  void testByteEqualsShort() {
    JsonPrimitive p1 = new JsonPrimitive(Byte.valueOf((byte)10));
    JsonPrimitive p2 = new JsonPrimitive(Short.valueOf((short)10));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testByteEqualsInteger() {
    JsonPrimitive p1 = new JsonPrimitive(Byte.valueOf((byte)10));
    JsonPrimitive p2 = new JsonPrimitive(Integer.valueOf(10));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testByteEqualsLong() {
    JsonPrimitive p1 = new JsonPrimitive(Byte.valueOf((byte)10));
    JsonPrimitive p2 = new JsonPrimitive(Long.valueOf(10L));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testByteEqualsBigInteger() {
    JsonPrimitive p1 = new JsonPrimitive(Byte.valueOf((byte)10));
    JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testShortEqualsInteger() {
    JsonPrimitive p1 = new JsonPrimitive(Short.valueOf((short)10));
    JsonPrimitive p2 = new JsonPrimitive(Integer.valueOf(10));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testShortEqualsLong() {
    JsonPrimitive p1 = new JsonPrimitive(Short.valueOf((short)10));
    JsonPrimitive p2 = new JsonPrimitive(Long.valueOf(10));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testShortEqualsBigInteger() {
    JsonPrimitive p1 = new JsonPrimitive(Short.valueOf((short)10));
    JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testIntegerEqualsLong() {
    JsonPrimitive p1 = new JsonPrimitive(Integer.valueOf(10));
    JsonPrimitive p2 = new JsonPrimitive(Long.valueOf(10L));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testIntegerEqualsBigInteger() {
    JsonPrimitive p1 = new JsonPrimitive(Integer.valueOf(10));
    JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testLongEqualsBigInteger() {
    JsonPrimitive p1 = new JsonPrimitive(Long.valueOf(10L));
    JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testFloatEqualsDouble() {
    JsonPrimitive p1 = new JsonPrimitive(Float.valueOf(10.25F));
    JsonPrimitive p2 = new JsonPrimitive(Double.valueOf(10.25D));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testFloatEqualsBigDecimal() {
    JsonPrimitive p1 = new JsonPrimitive(Float.valueOf(10.25F));
    JsonPrimitive p2 = new JsonPrimitive(new BigDecimal("10.25"));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testDoubleEqualsBigDecimal() {
    JsonPrimitive p1 = new JsonPrimitive(Double.valueOf(10.25D));
    JsonPrimitive p2 = new JsonPrimitive(new BigDecimal("10.25"));
    MoreAsserts.assertEqualsAndHashCode(p1, p2);
  }

  @Test
  void testValidJsonOnToString() throws Exception {
    JsonPrimitive json = new JsonPrimitive("Some\nEscaped\nValue");
    assertEquals("\"Some\\nEscaped\\nValue\"", json.toString());

    json = new JsonPrimitive(new BigDecimal("1.333"));
    assertEquals("1.333", json.toString());
  }

  @Test
  void testEquals() {
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive("A"), new JsonPrimitive("A"));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(true), new JsonPrimitive(true));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(5L), new JsonPrimitive(5L));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive('a'), new JsonPrimitive('a'));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.NaN), new JsonPrimitive(Float.NaN));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.NEGATIVE_INFINITY),
        new JsonPrimitive(Float.NEGATIVE_INFINITY));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.POSITIVE_INFINITY),
        new JsonPrimitive(Float.POSITIVE_INFINITY));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Double.NaN), new JsonPrimitive(Double.NaN));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Double.NEGATIVE_INFINITY),
        new JsonPrimitive(Double.NEGATIVE_INFINITY));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Double.POSITIVE_INFINITY),
        new JsonPrimitive(Double.POSITIVE_INFINITY));
    assertFalse(new JsonPrimitive("a").equals(new JsonPrimitive("b")));
    assertFalse(new JsonPrimitive(true).equals(new JsonPrimitive(false)));
    assertFalse(new JsonPrimitive(0).equals(new JsonPrimitive(1)));
  }

  @Test
  void testEqualsAcrossTypes() {
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive("a"), new JsonPrimitive('a'));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(new BigInteger("0")), new JsonPrimitive(0));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(0), new JsonPrimitive(0L));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(new BigInteger("0")), new JsonPrimitive(0));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.NaN), new JsonPrimitive(Double.NaN));
  }

  @Test
  void testEqualsIntegerAndBigInteger() {
    JsonPrimitive a = new JsonPrimitive(5L);
    JsonPrimitive b = new JsonPrimitive(new BigInteger("18446744073709551621")); // 2^64 + 5
    // Ideally, the following assertion should have failed but the price is too much to pay 
    // assertFalse(a + " equals " + b, a.equals(b));
    assertTrue(a.equals(b), a + " equals " + b);
  }

  @Test
  void testEqualsDoesNotEquateStringAndNonStringTypes() {
    assertFalse(new JsonPrimitive("true").equals(new JsonPrimitive(true)));
    assertFalse(new JsonPrimitive("0").equals(new JsonPrimitive(0)));
    assertFalse(new JsonPrimitive("NaN").equals(new JsonPrimitive(Float.NaN)));
  }

  @Test
  void testDeepCopy() {
    JsonPrimitive a = new JsonPrimitive("a");
    assertSame(a, a.deepCopy()); // Primitives are immutable!
  }
}
