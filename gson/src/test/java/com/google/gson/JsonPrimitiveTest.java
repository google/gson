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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.gson.common.MoreAsserts;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

/**
 * Unit test for the {@link JsonPrimitive} class.
 *
 * @author Joel Leitch
 */
public class JsonPrimitiveTest {

  @SuppressWarnings("unused")
  @Test
  public void testNulls() {
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
  public void testBoolean() {
    JsonPrimitive json = new JsonPrimitive(Boolean.TRUE);

    assertThat(json.isBoolean()).isTrue();
    assertThat(json.getAsBoolean()).isTrue();

    // Extra support for booleans
    json = new JsonPrimitive(1);
    assertThat(json.getAsBoolean()).isFalse();

    json = new JsonPrimitive("1");
    assertThat(json.getAsBoolean()).isFalse();

    json = new JsonPrimitive("true");
    assertThat(json.getAsBoolean()).isTrue();

    json = new JsonPrimitive("TrUe");
    assertThat(json.getAsBoolean()).isTrue();

    json = new JsonPrimitive("1.3");
    assertThat(json.getAsBoolean()).isFalse();
  }

  @Test
  public void testParsingStringAsBoolean() {
    JsonPrimitive json = new JsonPrimitive("true");

    assertThat(json.isBoolean()).isFalse();
    assertThat(json.getAsBoolean()).isTrue();
  }

  @Test
  public void testParsingStringAsNumber() {
    JsonPrimitive json = new JsonPrimitive("1");

    assertThat(json.isNumber()).isFalse();
    assertThat(json.getAsDouble()).isEqualTo(1.0);
    assertThat(json.getAsFloat()).isEqualTo(1F);
    assertThat(json.getAsInt()).isEqualTo(1);
    assertThat(json.getAsLong()).isEqualTo(1L);
    assertThat(json.getAsShort()).isEqualTo((short) 1);
    assertThat(json.getAsByte()).isEqualTo((byte) 1);
    assertThat(json.getAsBigInteger()).isEqualTo(new BigInteger("1"));
    assertThat(json.getAsBigDecimal()).isEqualTo(new BigDecimal("1"));
  }

  @Test
  public void testAsNumber_Boolean() {
    JsonPrimitive json = new JsonPrimitive(true);
    try {
      json.getAsNumber();
      fail();
    } catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageThat().isEqualTo("Primitive is neither a number nor a string");
    }
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testStringsAndChar() {
    JsonPrimitive json = new JsonPrimitive("abc");
    assertThat(json.isString()).isTrue();
    assertThat(json.getAsCharacter()).isEqualTo('a');
    assertThat(json.getAsString()).isEqualTo("abc");

    json = new JsonPrimitive('z');
    assertThat(json.isString()).isTrue();
    assertThat(json.getAsCharacter()).isEqualTo('z');
    assertThat(json.getAsString()).isEqualTo("z");

    json = new JsonPrimitive(true);
    assertThat(json.getAsString()).isEqualTo("true");

    json = new JsonPrimitive("");
    assertThat(json.getAsString()).isEqualTo("");
    try {
      json.getAsCharacter();
      fail();
    } catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageThat().isEqualTo("String value is empty");
    }
  }

  @Test
  public void testExponential() {
    JsonPrimitive json = new JsonPrimitive("1E+7");

    assertThat(json.getAsBigDecimal()).isEqualTo(new BigDecimal("1E+7"));
    assertThat(json.getAsDouble()).isEqualTo(1E+7);

    try {
      json.getAsInt();
      fail("Integers can not handle exponents like this.");
    } catch (NumberFormatException expected) { }
  }

  @Test
  public void testByteEqualsShort() {
    JsonPrimitive p1 = new JsonPrimitive((byte) 10);
    JsonPrimitive p2 = new JsonPrimitive((short) 10);
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testByteEqualsInteger() {
    JsonPrimitive p1 = new JsonPrimitive((byte) 10);
    JsonPrimitive p2 = new JsonPrimitive(10);
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testByteEqualsLong() {
    JsonPrimitive p1 = new JsonPrimitive((byte) 10);
    JsonPrimitive p2 = new JsonPrimitive(10L);
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testByteEqualsBigInteger() {
    JsonPrimitive p1 = new JsonPrimitive((byte) 10);
    JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testShortEqualsInteger() {
    JsonPrimitive p1 = new JsonPrimitive((short) 10);
    JsonPrimitive p2 = new JsonPrimitive(10);
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testShortEqualsLong() {
    JsonPrimitive p1 = new JsonPrimitive((short) 10);
    JsonPrimitive p2 = new JsonPrimitive(10L);
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testShortEqualsBigInteger() {
    JsonPrimitive p1 = new JsonPrimitive((short) 10);
    JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testIntegerEqualsLong() {
    JsonPrimitive p1 = new JsonPrimitive(10);
    JsonPrimitive p2 = new JsonPrimitive(10L);
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testIntegerEqualsBigInteger() {
    JsonPrimitive p1 = new JsonPrimitive(10);
    JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testLongEqualsBigInteger() {
    JsonPrimitive p1 = new JsonPrimitive(10L);
    JsonPrimitive p2 = new JsonPrimitive(new BigInteger("10"));
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testFloatEqualsDouble() {
    JsonPrimitive p1 = new JsonPrimitive(10.25F);
    JsonPrimitive p2 = new JsonPrimitive(10.25D);
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testFloatEqualsBigDecimal() {
    JsonPrimitive p1 = new JsonPrimitive(10.25F);
    JsonPrimitive p2 = new JsonPrimitive(new BigDecimal("10.25"));
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testDoubleEqualsBigDecimal() {
    JsonPrimitive p1 = new JsonPrimitive(10.25D);
    JsonPrimitive p2 = new JsonPrimitive(new BigDecimal("10.25"));
    assertThat(p1).isEqualTo(p2);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  public void testValidJsonOnToString() {
    JsonPrimitive json = new JsonPrimitive("Some\nEscaped\nValue");
    assertThat(json.toString()).isEqualTo("\"Some\\nEscaped\\nValue\"");

    json = new JsonPrimitive(new BigDecimal("1.333"));
    assertThat(json.toString()).isEqualTo("1.333");
  }

  @Test
  public void testEquals() {
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
    assertThat(new JsonPrimitive("a").equals(new JsonPrimitive("b"))).isFalse();
    assertThat(new JsonPrimitive(true).equals(new JsonPrimitive(false))).isFalse();
    assertThat(new JsonPrimitive(0).equals(new JsonPrimitive(1))).isFalse();
  }

  @Test
  public void testEqualsAcrossTypes() {
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive("a"), new JsonPrimitive('a'));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(new BigInteger("0")), new JsonPrimitive(0));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(0), new JsonPrimitive(0L));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(new BigInteger("0")), new JsonPrimitive(0));
    MoreAsserts.assertEqualsAndHashCode(new JsonPrimitive(Float.NaN), new JsonPrimitive(Double.NaN));
  }

  @Test
  public void testEqualsIntegerAndBigInteger() {
    JsonPrimitive a = new JsonPrimitive(5L);
    JsonPrimitive b = new JsonPrimitive(new BigInteger("18446744073709551621"));
    assertWithMessage("%s not equals %s", a, b).that(a.equals(b)).isFalse();
  }

  @Test
  public void testEqualsDoesNotEquateStringAndNonStringTypes() {
    assertThat(new JsonPrimitive("true").equals(new JsonPrimitive(true))).isFalse();
    assertThat(new JsonPrimitive("0").equals(new JsonPrimitive(0))).isFalse();
    assertThat(new JsonPrimitive("NaN").equals(new JsonPrimitive(Float.NaN))).isFalse();
  }

  @Test
  public void testDeepCopy() {
    JsonPrimitive a = new JsonPrimitive("a");
    assertThat(a).isSameInstanceAs(a.deepCopy()); // Primitives are immutable!
  }
}
