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

package com.google.gson.functional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassWithObjects;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
/**
 * Functional tests for Json serialization and deserialization of arrays.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
class ArrayTest {
  private Gson gson;

  @BeforeEach
  void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  void testTopLevelArrayOfIntsSerialization() {
    int[] target = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    assertEquals("[1,2,3,4,5,6,7,8,9]", gson.toJson(target));
  }

  @Test
  void testTopLevelArrayOfIntsDeserialization() {
    int[] expected = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    int[] actual = gson.fromJson("[1,2,3,4,5,6,7,8,9]", int[].class);
    assertArrayEquals(expected, actual);
  }

  @Test
  void testInvalidArrayDeserialization() {
    String json = "[1, 2 3, 4, 5]";
    try {
      gson.fromJson(json, int[].class);
      fail("Gson should not deserialize array elements with missing ,");
    } catch (JsonParseException expected) {
    }
  }

  @Test
  void testEmptyArraySerialization() {
    int[] target = {};
    assertEquals("[]", gson.toJson(target));
  }

  @Test
  void testEmptyArrayDeserialization() {
    int[] actualObject = gson.fromJson("[]", int[].class);
    assertTrue(actualObject.length == 0);

    Integer[] actualObject2 = gson.fromJson("[]", Integer[].class);
    assertTrue(actualObject2.length == 0);

    actualObject = gson.fromJson("[ ]", int[].class);
    assertTrue(actualObject.length == 0);
  }

  @Test
  void testNullsInArraySerialization() {
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = gson.toJson(array);
    assertEquals(expected, json);
  }

  @Test
  void testNullsInArrayDeserialization() {
    String json = "[\"foo\",null,\"bar\"]";
    String[] expected = {"foo", null, "bar"};
    String[] target = gson.fromJson(json, expected.getClass());
    for (int i = 0; i < expected.length; ++i) {
      assertEquals(expected[i], target[i]);
    }
  }

  @Test
  void testSingleNullInArraySerialization() {
    BagOfPrimitives[] array = new BagOfPrimitives[1];
    array[0] = null;
    String json = gson.toJson(array);
    assertEquals("[null]", json);
  }

  @Test
  void testSingleNullInArrayDeserialization() {
    BagOfPrimitives[] array = gson.fromJson("[null]", BagOfPrimitives[].class);
    assertNull(array[0]);
  }

  @Test
  void testNullsInArrayWithSerializeNullPropertySetSerialization() {
    gson = new GsonBuilder().serializeNulls().create();
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = gson.toJson(array);
    assertEquals(expected, json);
  }

  @Test
  void testArrayOfStringsSerialization() {
    String[] target = {"Hello", "World"};
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
  }

  @Test
  void testArrayOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    String[] target = gson.fromJson(json, String[].class);
    assertEquals("Hello", target[0]);
    assertEquals("World", target[1]);
  }

  @Test
  void testSingleStringArraySerialization() throws Exception {
    String[] s = { "hello" };
    String output = gson.toJson(s);
    assertEquals("[\"hello\"]", output);
  }

  @Test
  void testSingleStringArrayDeserialization() throws Exception {
    String json = "[\"hello\"]";
    String[] arrayType = gson.fromJson(json, String[].class);
    assertEquals(1, arrayType.length);
    assertEquals("hello", arrayType[0]);
  }

  @Test
  void testArrayOfCollectionSerialization() throws Exception {
    StringBuilder sb = new StringBuilder("[");
    int arraySize = 3;

    Type typeToSerialize = new TypeToken<Collection<Integer>[]>() {}.getType();
    @SuppressWarnings("unchecked")
    Collection<Integer>[] arrayOfCollection = new ArrayList[arraySize];
    for (int i = 0; i < arraySize; ++i) {
      int startValue = (3 * i) + 1;
      sb.append('[').append(startValue).append(',').append(startValue + 1).append(']');
      ArrayList<Integer> tmpList = new ArrayList<Integer>();
      tmpList.add(startValue);
      tmpList.add(startValue + 1);
      arrayOfCollection[i] = tmpList;

      if (i < arraySize - 1) {
        sb.append(',');
      }
    }
    sb.append(']');

    String json = gson.toJson(arrayOfCollection, typeToSerialize);
    assertEquals(sb.toString(), json);
  }

  @Test
  void testArrayOfCollectionDeserialization() throws Exception {
    String json = "[[1,2],[3,4]]";
    Type type = new TypeToken<Collection<Integer>[]>() {}.getType();
    Collection<Integer>[] target = gson.fromJson(json, type);

    assertEquals(2, target.length);
    assertArrayEquals(new Integer[] { 1, 2 }, target[0].toArray(new Integer[0]));
    assertArrayEquals(new Integer[] { 3, 4 }, target[1].toArray(new Integer[0]));
  }

  @Test
  void testArrayOfPrimitivesAsObjectsSerialization() throws Exception {
    Object[] objs = new Object[] {1, "abc", 0.3f, 5L};
    String json = gson.toJson(objs);
    assertTrue(json.contains("abc"));
    assertTrue(json.contains("0.3"));
    assertTrue(json.contains("5"));
  }

  @Test
  void testArrayOfPrimitivesAsObjectsDeserialization() throws Exception {
    String json = "[1,'abc',0.3,1.1,5]";
    Object[] objs = gson.fromJson(json, Object[].class);
    assertEquals(1, ((Number)objs[0]).intValue());
    assertEquals("abc", objs[1]);
    assertEquals(0.3, ((Number)objs[2]).doubleValue());
    assertEquals(new BigDecimal("1.1"), new BigDecimal(objs[3].toString()));
    assertEquals(5, ((Number)objs[4]).shortValue());
  }

  @Test
  void testObjectArrayWithNonPrimitivesSerialization() throws Exception {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String classWithObjectsJson = gson.toJson(classWithObjects);
    String bagOfPrimitivesJson = gson.toJson(bagOfPrimitives);

    Object[] objects = new Object[] { classWithObjects, bagOfPrimitives };
    String json = gson.toJson(objects);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
  }

  @Test
  void testArrayOfNullSerialization() {
    Object[] array = new Object[] {null};
    String json = gson.toJson(array);
    assertEquals("[null]", json);
  }

  @Test
  void testArrayOfNullDeserialization() {
    String[] values = gson.fromJson("[null]", String[].class);
    assertNull(values[0]);
  }

  /**
   * Regression tests for Issue 272
   */
  @Test
  void testMultidimenstionalArraysSerialization() {
    String[][] items = new String[][]{
        {"3m Co", "71.72", "0.02", "0.03", "4/2 12:00am", "Manufacturing"},
        {"Alcoa Inc", "29.01", "0.42", "1.47", "4/1 12:00am", "Manufacturing"}
    };
    String json = gson.toJson(items);
    assertTrue(json.contains("[[\"3m Co"));
    assertTrue(json.contains("Manufacturing\"]]"));
  }

  @Test
  void testMultiDimenstionalObjectArraysSerialization() {
    Object[][] array = new Object[][] { new Object[] { 1, 2 } };
    assertEquals("[[1,2]]", gson.toJson(array));
  }

  /**
   * Regression test for Issue 205
   */
  @Test
  void testMixingTypesInObjectArraySerialization() {
    Object[] array = new Object[] { 1, 2, new Object[] { "one", "two", 3 } };
    assertEquals("[1,2,[\"one\",\"two\",3]]", gson.toJson(array));
  }

  /**
   * Regression tests for Issue 272
   */
  @Test
  void testMultidimenstionalArraysDeserialization() {
    String json = "[['3m Co','71.72','0.02','0.03','4/2 12:00am','Manufacturing'],"
      + "['Alcoa Inc','29.01','0.42','1.47','4/1 12:00am','Manufacturing']]";
    String[][] items = gson.fromJson(json, String[][].class);
    assertEquals("3m Co", items[0][0]);
    assertEquals("Manufacturing", items[1][5]);
  }

  /** http://code.google.com/p/google-gson/issues/detail?id=342 */
  @Test
  void testArrayElementsAreArrays() {
    Object[] stringArrays = {
        new String[] {"test1", "test2"},
        new String[] {"test3", "test4"}
    };
    assertEquals("[[\"test1\",\"test2\"],[\"test3\",\"test4\"]]",
        new Gson().toJson(stringArrays));
  }
}
