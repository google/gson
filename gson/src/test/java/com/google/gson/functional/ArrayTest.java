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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassWithObjects;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
/**
 * Functional tests for Json serialization and deserialization of arrays.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ArrayTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testTopLevelArrayOfIntsSerialization() {
    int[] target = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    assertThat(gson.toJson(target)).isEqualTo("[1,2,3,4,5,6,7,8,9]");
  }

  @Test
  public void testTopLevelArrayOfIntsDeserialization() {
    int[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    int[] actual = gson.fromJson("[1,2,3,4,5,6,7,8,9]", int[].class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testInvalidArrayDeserialization() {
    String json = "[1, 2 3, 4, 5]";
    try {
      gson.fromJson(json, int[].class);
      fail("Gson should not deserialize array elements with missing ,");
    } catch (JsonParseException expected) {
    }
  }

  @Test
  public void testEmptyArraySerialization() {
    int[] target = {};
    assertThat(gson.toJson(target)).isEqualTo("[]");
  }

  @Test
  public void testEmptyArrayDeserialization() {
    int[] actualObject = gson.fromJson("[]", int[].class);
    assertThat(actualObject).hasLength(0);

    Integer[] actualObject2 = gson.fromJson("[]", Integer[].class);
    assertThat(actualObject2).hasLength(0);

    actualObject = gson.fromJson("[ ]", int[].class);
    assertThat(actualObject).hasLength(0);
  }

  @Test
  public void testNullsInArraySerialization() {
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = gson.toJson(array);
    assertThat(json).isEqualTo(expected);
  }

  @Test
  public void testNullsInArrayDeserialization() {
    String json = "[\"foo\",null,\"bar\"]";
    String[] expected = {"foo", null, "bar"};
    String[] target = gson.fromJson(json, expected.getClass());
    assertThat(target).asList().containsAnyIn(expected);
  }

  @Test
  public void testSingleNullInArraySerialization() {
    BagOfPrimitives[] array = new BagOfPrimitives[1];
    array[0] = null;
    String json = gson.toJson(array);
    assertThat(json).isEqualTo("[null]");
  }

  @Test
  public void testSingleNullInArrayDeserialization() {
    BagOfPrimitives[] array = gson.fromJson("[null]", BagOfPrimitives[].class);
    assertThat(array).asList().containsExactly((Object) null);
  }

  @Test
  public void testNullsInArrayWithSerializeNullPropertySetSerialization() {
    gson = new GsonBuilder().serializeNulls().create();
    String[] array = {"foo", null, "bar"};
    String expected = "[\"foo\",null,\"bar\"]";
    String json = gson.toJson(array);
    assertThat(json).isEqualTo(expected);
  }

  @Test
  public void testArrayOfStringsSerialization() {
    String[] target = {"Hello", "World"};
    assertThat(gson.toJson(target)).isEqualTo("[\"Hello\",\"World\"]");
  }

  @Test
  public void testArrayOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    String[] target = gson.fromJson(json, String[].class);
    assertThat(target).asList().containsExactly("Hello", "World");
  }

  @Test
  public void testSingleStringArraySerialization() {
    String[] s = { "hello" };
    String output = gson.toJson(s);
    assertThat(output).isEqualTo("[\"hello\"]");
  }

  @Test
  public void testSingleStringArrayDeserialization() {
    String json = "[\"hello\"]";
    String[] arrayType = gson.fromJson(json, String[].class);
    assertThat(arrayType).asList().containsExactly("hello");
  }

  @Test
  public void testArrayOfCollectionSerialization() {
    StringBuilder sb = new StringBuilder("[");
    int arraySize = 3;

    Type typeToSerialize = new TypeToken<Collection<Integer>[]>() {}.getType();
    @SuppressWarnings({"rawtypes", "unchecked"})
    Collection<Integer>[] arrayOfCollection = new ArrayList[arraySize];
    for (int i = 0; i < arraySize; ++i) {
      int startValue = (3 * i) + 1;
      sb.append('[').append(startValue).append(',').append(startValue + 1).append(']');
      ArrayList<Integer> tmpList = new ArrayList<>();
      tmpList.add(startValue);
      tmpList.add(startValue + 1);
      arrayOfCollection[i] = tmpList;

      if (i < arraySize - 1) {
        sb.append(',');
      }
    }
    sb.append(']');

    String json = gson.toJson(arrayOfCollection, typeToSerialize);
    assertThat(json).isEqualTo(sb.toString());
  }

  @Test
  public void testArrayOfCollectionDeserialization() {
    String json = "[[1,2],[3,4]]";
    Type type = new TypeToken<Collection<Integer>[]>() {}.getType();
    Collection<Integer>[] target = gson.fromJson(json, type);

    assertThat(target.length).isEqualTo(2);
    assertThat(target[0].toArray(new Integer[0])).isEqualTo(new Integer[] {1, 2});
    assertThat(target[1].toArray(new Integer[0])).isEqualTo(new Integer[] {3, 4});
  }

  @Test
  public void testArrayOfPrimitivesAsObjectsSerialization() {
    Object[] objs = new Object[] {1, "abc", 0.3f, 5L};
    String json = gson.toJson(objs);
    assertThat(json).contains("abc");
    assertThat(json).contains("0.3");
    assertThat(json).contains("5");
  }

  @Test
  public void testArrayOfPrimitivesAsObjectsDeserialization() {
    String json = "[1,'abc',0.3,1.1,5]";
    Object[] objs = gson.fromJson(json, Object[].class);
    assertThat(((Number)objs[0]).intValue()).isEqualTo(1);
    assertThat(objs[1]).isEqualTo("abc");
    assertThat(((Number)objs[2]).doubleValue()).isEqualTo(0.3);
    assertThat(new BigDecimal(objs[3].toString())).isEqualTo(new BigDecimal("1.1"));
    assertThat(((Number)objs[4]).shortValue()).isEqualTo(5);
  }

  @Test
  public void testObjectArrayWithNonPrimitivesSerialization() {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String classWithObjectsJson = gson.toJson(classWithObjects);
    String bagOfPrimitivesJson = gson.toJson(bagOfPrimitives);

    Object[] objects = {classWithObjects, bagOfPrimitives};
    String json = gson.toJson(objects);

    assertThat(json).contains(classWithObjectsJson);
    assertThat(json).contains(bagOfPrimitivesJson);
  }

  @Test
  public void testArrayOfNullSerialization() {
    Object[] array = {null};
    String json = gson.toJson(array);
    assertThat(json).isEqualTo("[null]");
  }

  @Test
  public void testArrayOfNullDeserialization() {
    String[] values = gson.fromJson("[null]", String[].class);
    assertThat(values[0]).isNull();
  }

  /**
   * Regression tests for Issue 272
   */
  @Test
  public void testMultidimensionalArraysSerialization() {
    String[][] items = {
        {"3m Co", "71.72", "0.02", "0.03", "4/2 12:00am", "Manufacturing"},
        {"Alcoa Inc", "29.01", "0.42", "1.47", "4/1 12:00am", "Manufacturing"}
    };
    String json = gson.toJson(items);
    assertThat(json).contains("[[\"3m Co");
    assertThat(json).contains("Manufacturing\"]]");
  }

  @Test
  public void testMultidimensionalObjectArraysSerialization() {
    Object[][] array = {new Object[] { 1, 2 }};
    assertThat(gson.toJson(array)).isEqualTo("[[1,2]]");
  }

  @Test
  public void testMultidimensionalPrimitiveArraysSerialization() {
    int[][] array = {{1, 2}, {3, 4}};
    assertThat(gson.toJson(array)).isEqualTo("[[1,2],[3,4]]");
  }

  /**
   * Regression test for Issue 205
   */
  @Test
  public void testMixingTypesInObjectArraySerialization() {
    Object[] array = {1, 2, new Object[] {"one", "two", 3}};
    assertThat(gson.toJson(array)).isEqualTo("[1,2,[\"one\",\"two\",3]]");
  }

  /**
   * Regression tests for Issue 272
   */
  @Test
  public void testMultidimensionalArraysDeserialization() {
    String json = "[['3m Co','71.72','0.02','0.03','4/2 12:00am','Manufacturing'],"
      + "['Alcoa Inc','29.01','0.42','1.47','4/1 12:00am','Manufacturing']]";
    String[][] items = gson.fromJson(json, String[][].class);
    assertThat(items[0][0]).isEqualTo("3m Co");
    assertThat(items[1][5]).isEqualTo("Manufacturing");
  }

  @Test
  public void testMultidimensionalPrimitiveArraysDeserialization() {
    String json = "[[1,2],[3,4]]";
    int[][] expected = {{1, 2}, {3, 4}};
    assertThat(gson.fromJson(json, int[][].class)).isEqualTo(expected);
  }

  /** http://code.google.com/p/google-gson/issues/detail?id=342 */
  @Test
  public void testArrayElementsAreArrays() {
    Object[] stringArrays = {
        new String[] {"test1", "test2"},
        new String[] {"test3", "test4"}
    };
    assertThat(new Gson().toJson(stringArrays)).isEqualTo("[[\"test1\",\"test2\"],[\"test3\",\"test4\"]]");
  }
}
