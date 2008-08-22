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

import com.google.gson.TestTypes.PrimitiveArray;
import com.google.gson.common.MoreAsserts;

import junit.framework.TestCase;

/**
 * Unit tests for Json serialization and deserialization of arrays.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ArrayTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testTopLevelArrayOfIntsSerialization() {
    int[] target = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    assertEquals("[1,2,3,4,5,6,7,8,9]", gson.toJson(target));
  }

  public void testTopLevelArrayOfIntsDeserialization() {
    int[] expected = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    int[] actual = gson.fromJson("[1,2,3,4,5,6,7,8,9]", int[].class);
    MoreAsserts.assertEquals(expected, actual);
  }

  public void testEmptyArraySerialization() {
    int[] target = {};
    assertEquals("[]", gson.toJson(target));
  }

  public void testEmptyArrayDeserialization() {
    int[] actualObject = gson.fromJson("[]", int[].class);
    assertTrue(actualObject.length == 0);

    Integer[] actualObject2 = gson.fromJson("[]", Integer[].class);
    assertTrue(actualObject2.length == 0);
  }

  public void testNullsInArraySerialization() {
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = gson.toJson(array);
    assertEquals(expected, json);
  }

  public void testNullsInArrayDeserialization() {
    String json = "[\"foo\",null,\"bar\"]";
    String[] expected = {"foo", null, "bar"};
    String[] target = gson.fromJson(json, expected.getClass());
    for (int i = 0; i < expected.length; ++i) {
      assertEquals(expected[i], target[i]);
    }
  }

  public void testArrayOfStringsSerialization() {
    String[] target = {"Hello", "World"};
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
  }

  public void testArrayOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    String[] target = gson.fromJson(json, String[].class);
    assertEquals("Hello", target[0]);
    assertEquals("World", target[1]);
  }

  public void testPrimitiveArrayFieldSerialization() throws Exception {
    PrimitiveArray target = new PrimitiveArray(new long[] { 1L, 2L, 3L });
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testArrayOfPrimitivesDeserialization() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    int[] target = gson.fromJson(json, int[].class);
    int[] expected = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    MoreAsserts.assertEquals(expected, target);
  }
}
