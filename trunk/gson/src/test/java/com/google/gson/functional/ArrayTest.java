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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.common.MoreAsserts;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassWithObjects;
import com.google.gson.common.TestTypes.CrazyLongTypeAdapter;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
/**
 * Functional tests for Json serialization and deserialization of arrays.
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

  public void testInvalidArrayDeserialization() {
    String json = "[1, 2 3, 4, 5]";
    try {
      gson.fromJson(json, int[].class);
      fail("Gson should not deserialize array elements with missing ,");
    } catch (JsonParseException expected) {
    }
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

    actualObject = gson.fromJson("[ ]", int[].class);
    assertTrue(actualObject.length == 0);
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

  public void testSingleNullInArraySerialization() {
    BagOfPrimitives[] array = new BagOfPrimitives[1];
    array[0] = null;
    String json = gson.toJson(array);
    assertEquals("[null]", json);
  }

  public void testSingleNullInArrayDeserialization() {
    BagOfPrimitives[] array = gson.fromJson("[null]", BagOfPrimitives[].class);
    assertNull(array[0]);
  }

  public void testNullsInArrayWithSerializeNullPropertySetSerialization() {
    gson = new GsonBuilder().serializeNulls().create();
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = gson.toJson(array);
    assertEquals(expected, json);
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

  public void testSingleStringArraySerialization() throws Exception {
    String[] s = { "hello" };
    String output = gson.toJson(s);
    assertEquals("[\"hello\"]", output);
  }

  public void testSingleStringArrayDeserialization() throws Exception {
    String json = "[\"hello\"]";
    String[] arrayType = gson.fromJson(json, String[].class);
    assertEquals(1, arrayType.length);
    assertEquals("hello", arrayType[0]);

    String type = gson.fromJson(json, String.class);
    assertEquals("hello", type);
  }

  @SuppressWarnings("unchecked")
  public void testArrayOfCollectionSerialization() throws Exception {
    StringBuilder sb = new StringBuilder("[");
    int arraySize = 3;

    Type typeToSerialize = new TypeToken<Collection<Integer>[]>() {}.getType();
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

  public void testArrayOfCollectionDeserialization() throws Exception {
    String json = "[[1,2],[3,4]]";
    Type type = new TypeToken<Collection<Integer>[]>() {}.getType();
    Collection<Integer>[] target = gson.fromJson(json, type);

    assertEquals(2, target.length);
    MoreAsserts.assertEquals(new Integer[] { 1, 2 }, target[0].toArray(new Integer[0]));
    MoreAsserts.assertEquals(new Integer[] { 3, 4 }, target[1].toArray(new Integer[0]));
  }

  public void testArrayOfPrimitivesWithCustomTypeAdapter() throws Exception {
    CrazyLongTypeAdapter typeAdapter = new CrazyLongTypeAdapter();
    gson = new GsonBuilder()
        .registerTypeAdapter(long.class, typeAdapter)
        .registerTypeAdapter(Long.class, typeAdapter)
        .create();
    long[] value = { 1L };
    String serializedValue = gson.toJson(value);
    String expected = "[" + String.valueOf(value[0] + CrazyLongTypeAdapter.DIFFERENCE) + "]";
    assertEquals(expected, serializedValue);

    long[] deserializedValue = gson.fromJson(serializedValue, long[].class);
    assertEquals(1, deserializedValue.length);
    assertEquals(value[0], deserializedValue[0]);
  }

  public void testArrayOfPrimitivesAsObjectsSerialization() throws Exception {
    Object[] objs = new Object[]{1, "abc", 0.3f, 5L};
    String json = gson.toJson(objs);
    assertTrue(json.contains("abc"));
    assertTrue(json.contains("0.3"));
    assertTrue(json.contains("5"));
  }

  public void testArrayOfPrimitivesAsObjectsDeserialization() throws Exception {
    String json = "[1,'abc',0.3,5]";
    Object[] objs = gson.fromJson(json, Object[].class);
    assertEquals(1, objs[0]);
    assertEquals("abc", objs[1]);
    assertEquals(new BigDecimal("0.3"), objs[2]);
    assertEquals(5, objs[3]);
  }

  public void testArrayOfObjectsWithoutTypeInfoDeserialization() throws Exception {
    String json = "[1,'abc',{a:1},5]";
    try {
      gson.fromJson(json, Object[].class);
      fail("This is crazy....how did we deserialize it!!!");
    } catch (JsonParseException expected) {
    }
  }

  public void testArrayWithoutTypeInfoDeserialization() throws Exception {
    String json = "[1,'abc',[1,2],5]";
    try {
      gson.fromJson(json, Object[].class);
      fail("This is crazy....how did we deserialize it!!!");
    } catch (JsonParseException expected) {
    }
  }

  public void testObjectArrayWithNonPrimitivesSerialization() throws Exception {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String classWithObjectsJson = gson.toJson(classWithObjects);
    String bagOfPrimitivesJson = gson.toJson(bagOfPrimitives);

    Object[] objects = new Object[] { classWithObjects, bagOfPrimitives };
    String json = gson.toJson(objects);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
  }
}
