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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
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
  @SuppressWarnings("TruthSelfEquals")
  public void testEqualsNonEmptyArray() {
    JsonArray a = new JsonArray();
    JsonArray b = new JsonArray();

    assertThat(a).isEqualTo(a);

    a.add(new JsonObject());
    assertThat(a.equals(b)).isFalse();
    assertThat(b.equals(a)).isFalse();

    b.add(new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add(new JsonObject());
    assertThat(a.equals(b)).isFalse();
    assertThat(b.equals(a)).isFalse();

    b.add(JsonNull.INSTANCE);
    assertThat(a.equals(b)).isFalse();
    assertThat(b.equals(a)).isFalse();
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
    assertThat(array.remove(a)).isTrue();
    assertThat(array).doesNotContain(a);
    array.add(a);
    array.add(new JsonPrimitive("b"));
    assertThat(array.remove(1).getAsString()).isEqualTo("b");
    assertThat(array).hasSize(1);
    assertThat(array).contains(a);
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
    assertThat(oldValue).isEqualTo(a);
    assertThat(array.get(0).getAsString()).isEqualTo("b");

    oldValue = array.set(0, null);
    assertThat(oldValue).isEqualTo(b);
    assertThat(array.get(0)).isEqualTo(JsonNull.INSTANCE);

    oldValue = array.set(0, new JsonPrimitive("c"));
    assertThat(oldValue).isEqualTo(JsonNull.INSTANCE);
    assertThat(array.get(0).getAsString()).isEqualTo("c");
    assertThat(array).hasSize(1);
  }

  @Test
  public void testDeepCopy() {
    JsonArray original = new JsonArray();
    JsonArray firstEntry = new JsonArray();
    original.add(firstEntry);

    JsonArray copy = original.deepCopy();
    original.add(new JsonPrimitive("y"));

    assertThat(copy).hasSize(1);
    firstEntry.add(new JsonPrimitive("z"));

    assertThat(original.get(0).getAsJsonArray()).hasSize(1);
    assertThat(copy.get(0).getAsJsonArray()).hasSize(0);
  }

  @Test
  public void testIsEmpty() {
    JsonArray array = new JsonArray();
    assertThat(array).isEmpty();

    JsonPrimitive a = new JsonPrimitive("a");
    array.add(a);
    assertThat(array).isNotEmpty();

    array.remove(0);
    assertThat(array).isEmpty();
  }

  @Test
  public void testFailedGetArrayValues() {
    JsonArray jsonArray = new JsonArray();
    jsonArray.add(JsonParser.parseString("{" + "\"key1\":\"value1\"," + "\"key2\":\"value2\"," + "\"key3\":\"value3\"," + "\"key4\":\"value4\"" + "}"));
    try {
      jsonArray.getAsBoolean();
      fail("expected getBoolean to fail");
    } catch (UnsupportedOperationException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("JsonObject");
    }
    try {
      jsonArray.get(-1);
      fail("expected get to fail");
    } catch (IndexOutOfBoundsException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("Index -1 out of bounds for length 1");
    }
    try {
      jsonArray.getAsString();
      fail("expected getString to fail");
    } catch (UnsupportedOperationException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("JsonObject");
    }

    jsonArray.remove(0);
    jsonArray.add("hello");
    try {
      jsonArray.getAsDouble();
      fail("expected getDouble to fail");
    } catch (NumberFormatException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("For input string: \"hello\"");
    }
    try {
      jsonArray.getAsInt();
      fail("expected getInt to fail");
    } catch (NumberFormatException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("For input string: \"hello\"");
    }
    try {
      jsonArray.get(0).getAsJsonArray();
      fail("expected getJSONArray to fail");
    } catch (IllegalStateException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("Not a JSON Array: \"hello\"");
    }
    try {
      jsonArray.getAsJsonObject();
      fail("expected getJSONObject to fail");
    } catch (IllegalStateException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo( "Not a JSON Object: [\"hello\"]");
    }
    try {
      jsonArray.getAsLong();
      fail("expected getLong to fail");
    } catch (NumberFormatException e) {
      assertWithMessage("Expected an exception message")
          .that(e).hasMessageThat().isEqualTo("For input string: \"hello\"");
    }
  }

  @Test
  public void testGetAs_WrongArraySize() {
    JsonArray jsonArray = new JsonArray();
    try {
      jsonArray.getAsByte();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("Array must have size 1, but has size 0");
    }

    jsonArray.add(true);
    jsonArray.add(false);
    try {
      jsonArray.getAsByte();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("Array must have size 1, but has size 2");
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

    assertThat(jsonArray.toString()).isEqualTo("[\"Hello\",\"Goodbye\",\"Thank you\",null,\"Yes\"]");
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

    assertThat(jsonArray.toString()).isEqualTo("[1,2,-3,null,4,0]");
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

    assertThat(jsonArray.toString()).isEqualTo("[1.0,2.13232,0.121,null,-0.00234,null]");
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

    assertThat(jsonArray.toString()).isEqualTo("[true,true,false,false,null,true]");
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

    assertThat(jsonArray.toString()).isEqualTo("[\"a\",\"e\",\"i\",\"o\",null,\"u\",\"and sometimes Y\"]");
  }

  @Test
  public void testMixedPrimitiveAddition() {
    JsonArray jsonArray = new JsonArray();

    jsonArray.add('a');
    jsonArray.add("apple");
    jsonArray.add(12121);
    jsonArray.add((char) 111);

    jsonArray.add((Boolean) null);
    assertThat(jsonArray.get(jsonArray.size() - 1)).isEqualTo(JsonNull.INSTANCE);

    jsonArray.add((Character) null);
    assertThat(jsonArray.get(jsonArray.size() - 1)).isEqualTo(JsonNull.INSTANCE);

    jsonArray.add(12.232);
    jsonArray.add(BigInteger.valueOf(2323));

    assertThat(jsonArray.toString()).isEqualTo("[\"a\",\"apple\",12121,\"o\",null,null,12.232,2323]");
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

    assertThat(jsonArray.toString()).isEqualTo("[null,null,null,null,null,null,null,null,null]");
    for (int i = 0; i < jsonArray.size(); i++) {
      // Verify that they are actually a JsonNull and not a Java null
      assertThat(jsonArray.get(i)).isEqualTo(JsonNull.INSTANCE);
    }
  }

  @Test
  public void testNullJsonElementAddition() {
    JsonArray jsonArray = new JsonArray();
    jsonArray.add((JsonElement) null);
    assertThat(jsonArray.get(0)).isEqualTo(JsonNull.INSTANCE);
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

    assertThat(jsonArray.toString()).isEqualTo("[\"a\",\"a\",true,true,1212,1212,34.34,34.34,null,null]");
  }
}
