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
import com.google.gson.JsonParseException;
import com.google.gson.common.MoreAsserts;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.MyEnum;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Functional tests for Json serialization and deserialization of collections.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class CollectionTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testTopLevelCollectionOfIntegersSerialization() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    Type targetType = new TypeToken<Collection<Integer>>() {}.getType();
    String json = gson.toJson(target, targetType);
    assertEquals("[1,2,3,4,5,6,7,8,9]", json);
  }

  public void testTopLevelCollectionOfIntegersDeserialization() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    Type collectionType = new TypeToken<Collection<Integer>>() { }.getType();
    Collection<Integer> target = gson.fromJson(json, collectionType);
    int[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    MoreAsserts.assertEquals(expected, toIntArray(target));
  }

  public void testTopLevelListOfIntegerCollectionsDeserialization() throws Exception {
    String json = "[[1,2,3],[4,5,6],[7,8,9]]";
    Type collectionType = new TypeToken<Collection<Collection<Integer>>>() {}.getType();
    List<Collection<Integer>> target = gson.fromJson(json, collectionType);
    int[][] expected = new int[3][3];
    for (int i = 0; i < 3; ++i) {
      int start = (3 * i) + 1;
      for (int j = 0; j < 3; ++j) {
        expected[i][j] = start + j;
      }
    }

    for (int i = 0; i < 3; i++) {
      MoreAsserts.assertEquals(expected[i], toIntArray(target.get(i)));
    }
  }
  
  public void testLinkedListSerialization() {
    List<String> list = new LinkedList<String>();
    list.add("a1");
    list.add("a2");
    Type linkedListType = new TypeToken<LinkedList<String>>() {}.getType();
    String json = gson.toJson(list, linkedListType);
    assertTrue(json.contains("a1"));
    assertTrue(json.contains("a2"));
  }

  public void testLinkedListDeserialization() {
    String json = "['a1','a2']";
    Type linkedListType = new TypeToken<LinkedList<String>>() {}.getType();
    List<String> list = gson.fromJson(json, linkedListType);
    assertEquals("a1", list.get(0));
    assertEquals("a2", list.get(1));
  }

  public void testQueueSerialization() {
    Queue<String> queue = new LinkedList<String>();
    queue.add("a1");
    queue.add("a2");
    Type queueType = new TypeToken<Queue<String>>() {}.getType();
    String json = gson.toJson(queue, queueType);
    assertTrue(json.contains("a1"));
    assertTrue(json.contains("a2"));
  }

  public void testQueueDeserialization() {
    String json = "['a1','a2']";
    Type queueType = new TypeToken<Queue<String>>() {}.getType();
    Queue<String> queue = gson.fromJson(json, queueType);
    assertEquals("a1", queue.element()); 
    queue.remove();
    assertEquals("a2", queue.element());
  }

  public void testNullsInListSerialization() {
    List<String> list = new ArrayList<String>();
    list.add("foo");
    list.add(null);
    list.add("bar");
    String expected = "[\"foo\",null,\"bar\"]";
    Type typeOfList = new TypeToken<List<String>>() {}.getType();
    String json = gson.toJson(list, typeOfList);
    assertEquals(expected, json);
  }

  public void testNullsInListDeserialization() {
    List<String> expected = new ArrayList<String>();
    expected.add("foo");
    expected.add(null);
    expected.add("bar");
    String json = "[\"foo\",null,\"bar\"]";
    Type expectedType = new TypeToken<List<String>>() {}.getType();
    List<String> target = gson.fromJson(json, expectedType);
    for (int i = 0; i < expected.size(); ++i) {
      assertEquals(expected.get(i), target.get(i));
    }
  }

  public void testCollectionOfObjectSerialization() {
    List<Object> target = new ArrayList<Object>();
    target.add("Hello");
    target.add("World");
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
    
    Type type = new TypeToken<List<Object>>() {}.getType();
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target, type));
  }

  public void testCollectionOfStringsSerialization() {
    List<String> target = new ArrayList<String>();
    target.add("Hello");
    target.add("World");
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
  }

  public void testCollectionOfBagOfPrimitivesSerialization() {
    List<BagOfPrimitives> target = new ArrayList<BagOfPrimitives>();
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    target.add(objA);
    target.add(objB);

    String result = gson.toJson(target);
    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
    for (BagOfPrimitives obj : target) {
      assertTrue(result.contains(obj.getExpectedJson()));
    }
  }

  public void testCollectionOfEnumsSerialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    Collection<MyEnum> target = new ArrayList<MyEnum>();
    target.add(MyEnum.VALUE1);
    target.add(MyEnum.VALUE2);
    String expectedJson = "[\"VALUE1\",\"VALUE2\"]";
    String actualJson = gson.toJson(target);
    assertEquals(expectedJson, actualJson);
    actualJson = gson.toJson(target, type);
    assertEquals(expectedJson, actualJson);
  }

  public void testCollectionOfEnumsDeserialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    String json = "[\"VALUE1\",\"VALUE2\"]";
    Collection<MyEnum> target = gson.fromJson(json, type);
    MoreAsserts.assertContains(target, MyEnum.VALUE1);
    MoreAsserts.assertContains(target, MyEnum.VALUE2);
  }

  public void testCollectionOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    Type collectionType = new TypeToken<Collection<String>>() { }.getType();
    Collection<String> target = gson.fromJson(json, collectionType);

    assertTrue(target.contains("Hello"));
    assertTrue(target.contains("World"));
  }

  public void testRawCollectionOfIntegersSerialization() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals("[1,2,3,4,5,6,7,8,9]", gson.toJson(target));
  }

  @SuppressWarnings("unchecked")
  public void testRawCollectionSerialization() {
    BagOfPrimitives bag1 = new BagOfPrimitives();
    Collection target = Arrays.asList(bag1, bag1);
    String json = gson.toJson(target);
    assertTrue(json.contains(bag1.getExpectedJson()));
  }

  public void testRawCollectionDeserializationNotAlllowed() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    try {
        gson.fromJson(json, Collection.class);
        fail("Can not deserialize a non-genericized collection.");
    } catch (JsonParseException expected) { }

    json = "[\"Hello\", \"World\"]";
    try {
      gson.fromJson(json, Collection.class);
      fail("Can not deserialize a non-genericized collection.");
    } catch (JsonParseException expected) { }
  }

  @SuppressWarnings("unchecked")
  public void testRawCollectionOfBagOfPrimitivesNotAllowed() {
    try {
      BagOfPrimitives bag = new BagOfPrimitives(10, 20, false, "stringValue");
      String json = '[' + bag.getExpectedJson() + ',' + bag.getExpectedJson() + ']';
      Collection target = gson.fromJson(json, Collection.class);
      assertEquals(2, target.size());
      for (BagOfPrimitives bag1 : (Collection<BagOfPrimitives>) target) {
        assertEquals(bag.getExpectedJson(), bag1.getExpectedJson());
      }
      fail("Raw collection of objects should not work");
    } catch (JsonParseException expected) {
    }
  }

  @SuppressWarnings("unchecked")
  private static int[] toIntArray(Collection collection) {
    int[] ints = new int[collection.size()];
    int i = 0;
    for (Iterator iterator = collection.iterator(); iterator.hasNext(); ++i) {
      Object obj = iterator.next();
      if (obj instanceof Integer) {
        ints[i] = ((Integer)obj).intValue();
      } else if (obj instanceof Long) {
        ints[i] = ((Long)obj).intValue();
      }
    }
    return ints;
  }
}
