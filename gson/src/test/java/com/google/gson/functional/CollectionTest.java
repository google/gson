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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for Json serialization and deserialization of collections.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class CollectionTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testTopLevelCollectionOfIntegersSerialization() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    Type targetType = new TypeToken<Collection<Integer>>() {}.getType();
    String json = gson.toJson(target, targetType);
    assertThat(json).isEqualTo("[1,2,3,4,5,6,7,8,9]");
  }

  @Test
  public void testTopLevelCollectionOfIntegersDeserialization() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    Type collectionType = new TypeToken<Collection<Integer>>() { }.getType();
    Collection<Integer> target = gson.fromJson(json, collectionType);
    int[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    assertThat(toIntArray(target)).isEqualTo(expected);
  }

  @Test
  public void testTopLevelListOfIntegerCollectionsDeserialization() {
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
      assertThat(toIntArray(target.get(i))).isEqualTo(expected[i]);
    }
  }

  @Test
  public void testLinkedListSerialization() {
    List<String> list = new LinkedList<>();
    list.add("a1");
    list.add("a2");
    Type linkedListType = new TypeToken<LinkedList<String>>() {}.getType();
    String json = gson.toJson(list, linkedListType);
    assertThat(json).contains("a1");
    assertThat(json).contains("a2");
  }

  @Test
  public void testLinkedListDeserialization() {
    String json = "['a1','a2']";
    Type linkedListType = new TypeToken<LinkedList<String>>() {}.getType();
    List<String> list = gson.fromJson(json, linkedListType);
    assertThat(list.get(0)).isEqualTo("a1");
    assertThat(list.get(1)).isEqualTo("a2");
  }

  @Test
  public void testQueueSerialization() {
    Queue<String> queue = new LinkedList<>();
    queue.add("a1");
    queue.add("a2");
    Type queueType = new TypeToken<Queue<String>>() {}.getType();
    String json = gson.toJson(queue, queueType);
    assertThat(json).contains("a1");
    assertThat(json).contains("a2");
  }

  @Test
  public void testQueueDeserialization() {
    String json = "['a1','a2']";
    Type queueType = new TypeToken<Queue<String>>() {}.getType();
    Queue<String> queue = gson.fromJson(json, queueType);
    assertThat(queue.element()).isEqualTo("a1");
    queue.remove();
    assertThat(queue.element()).isEqualTo("a2");
  }

  @Test
  public void testPriorityQueue() {
    Type type = new TypeToken<PriorityQueue<Integer>>(){}.getType();
    PriorityQueue<Integer> queue = gson.fromJson("[10, 20, 22]", type);
    assertThat(queue.size()).isEqualTo(3);
    String json = gson.toJson(queue);
    assertThat(queue.remove()).isEqualTo(10);
    assertThat(queue.remove()).isEqualTo(20);
    assertThat(queue.remove()).isEqualTo(22);
    assertThat(json).isEqualTo("[10,20,22]");
  }

  @Test
  public void testVector() {
    Type type = new TypeToken<Vector<Integer>>(){}.getType();
    Vector<Integer> target = gson.fromJson("[10, 20, 31]", type);
    assertThat(target.size()).isEqualTo(3);
    assertThat(target.get(0)).isEqualTo(10);
    assertThat(target.get(1)).isEqualTo(20);
    assertThat(target.get(2)).isEqualTo(31);
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("[10,20,31]");
  }

  @Test
  public void testStack() {
    Type type = new TypeToken<Stack<Integer>>(){}.getType();
    Stack<Integer> target = gson.fromJson("[11, 13, 17]", type);
    assertThat(target.size()).isEqualTo(3);
    String json = gson.toJson(target);
    assertThat(target.pop()).isEqualTo(17);
    assertThat(target.pop()).isEqualTo(13);
    assertThat(target.pop()).isEqualTo(11);
    assertThat(json).isEqualTo("[11,13,17]");
  }

  @Test
  public void testNullsInListSerialization() {
    List<String> list = new ArrayList<>();
    list.add("foo");
    list.add(null);
    list.add("bar");
    String expected = "[\"foo\",null,\"bar\"]";
    Type typeOfList = new TypeToken<List<String>>() {}.getType();
    String json = gson.toJson(list, typeOfList);
    assertThat(json).isEqualTo(expected);
  }

  @Test
  public void testNullsInListDeserialization() {
    List<String> expected = new ArrayList<>();
    expected.add("foo");
    expected.add(null);
    expected.add("bar");
    String json = "[\"foo\",null,\"bar\"]";
    Type expectedType = new TypeToken<List<String>>() {}.getType();
    List<String> target = gson.fromJson(json, expectedType);
    for (int i = 0; i < expected.size(); ++i) {
      assertThat(target.get(i)).isEqualTo(expected.get(i));
    }
  }

  @Test
  public void testCollectionOfObjectSerialization() {
    List<Object> target = new ArrayList<>();
    target.add("Hello");
    target.add("World");
    assertThat(gson.toJson(target)).isEqualTo("[\"Hello\",\"World\"]");

    Type type = new TypeToken<List<Object>>() {}.getType();
    assertThat(gson.toJson(target, type)).isEqualTo("[\"Hello\",\"World\"]");
  }

  @Test
  public void testCollectionOfObjectWithNullSerialization() {
    List<Object> target = new ArrayList<>();
    target.add("Hello");
    target.add(null);
    target.add("World");
    assertThat(gson.toJson(target)).isEqualTo("[\"Hello\",null,\"World\"]");

    Type type = new TypeToken<List<Object>>() {}.getType();
    assertThat(gson.toJson(target, type)).isEqualTo("[\"Hello\",null,\"World\"]");
  }

  @Test
  public void testCollectionOfStringsSerialization() {
    List<String> target = new ArrayList<>();
    target.add("Hello");
    target.add("World");
    assertThat(gson.toJson(target)).isEqualTo("[\"Hello\",\"World\"]");
  }

  @Test
  public void testCollectionOfBagOfPrimitivesSerialization() {
    List<BagOfPrimitives> target = new ArrayList<>();
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    target.add(objA);
    target.add(objB);

    String result = gson.toJson(target);
    assertThat(result.startsWith("[")).isTrue();
    assertThat(result.endsWith("]")).isTrue();
    for (BagOfPrimitives obj : target) {
      assertThat(result).contains(obj.getExpectedJson());
    }
  }

  @Test
  public void testCollectionOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    Type collectionType = new TypeToken<Collection<String>>() { }.getType();
    Collection<String> target = gson.fromJson(json, collectionType);

    assertThat(target).containsExactly("Hello", "World").inOrder();
  }

  @Test
  public void testRawCollectionOfIntegersSerialization() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertThat(gson.toJson(target)).isEqualTo("[1,2,3,4,5,6,7,8,9]");
  }

  @Test
  public void testObjectCollectionSerialization() {
    BagOfPrimitives bag1 = new BagOfPrimitives();
    Collection<?> target = Arrays.asList(bag1, bag1, "test");
    String json = gson.toJson(target);
    assertThat(json).contains(bag1.getExpectedJson());
  }

  @Test
  public void testRawCollectionDeserializationNotAlllowed() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    Collection<?> integers = gson.fromJson(json, Collection.class);
    // JsonReader converts numbers to double by default so we need a floating point comparison
    assertThat(integers).isEqualTo(Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));

    json = "[\"Hello\", \"World\"]";
    Collection<?> strings = gson.fromJson(json, Collection.class);
    assertThat(strings).containsExactly("Hello", "World").inOrder();
  }

  @Test
  public void testRawCollectionOfBagOfPrimitivesNotAllowed() {
    BagOfPrimitives bag = new BagOfPrimitives(10, 20, false, "stringValue");
    String json = '[' + bag.getExpectedJson() + ',' + bag.getExpectedJson() + ']';
    Collection<?> target = gson.fromJson(json, Collection.class);
    assertThat(target.size()).isEqualTo(2);
    for (Object bag1 : target) {
      // Gson 2.0 converts raw objects into maps
      @SuppressWarnings("unchecked")
      Map<String, Object> values = (Map<String, Object>) bag1;
      assertThat(values.containsValue(10.0)).isTrue();
      assertThat(values.containsValue(20.0)).isTrue();
      assertThat(values.containsValue("stringValue")).isTrue();
    }
  }

  @Test
  public void testWildcardPrimitiveCollectionSerilaization() {
    Collection<? extends Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    Type collectionType = new TypeToken<Collection<? extends Integer>>() { }.getType();
    String json = gson.toJson(target, collectionType);
    assertThat(json).isEqualTo("[1,2,3,4,5,6,7,8,9]");

    json = gson.toJson(target);
    assertThat(json).isEqualTo("[1,2,3,4,5,6,7,8,9]");
  }

  @Test
  public void testWildcardPrimitiveCollectionDeserilaization() {
    String json = "[1,2,3,4,5,6,7,8,9]";
    Type collectionType = new TypeToken<Collection<? extends Integer>>() { }.getType();
    Collection<? extends Integer> target = gson.fromJson(json, collectionType);
    assertThat(target.size()).isEqualTo(9);
    assertThat(target).contains(1);
    assertThat(target).contains(2);
  }

  @Test
  public void testWildcardCollectionField() {
    Collection<BagOfPrimitives> collection = new ArrayList<>();
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    collection.add(objA);
    collection.add(objB);

    ObjectWithWildcardCollection target = new ObjectWithWildcardCollection(collection);
    String json = gson.toJson(target);
    assertThat(json).contains(objA.getExpectedJson());
    assertThat(json).contains(objB.getExpectedJson());

    target = gson.fromJson(json, ObjectWithWildcardCollection.class);
    Collection<? extends BagOfPrimitives> deserializedCollection = target.getCollection();
    assertThat(deserializedCollection.size()).isEqualTo(2);
    assertThat(deserializedCollection).contains(objA);
    assertThat(deserializedCollection).contains(objB);
  }

  @Test
  public void testFieldIsArrayList() {
    HasArrayListField object = new HasArrayListField();
    object.longs.add(1L);
    object.longs.add(3L);
    String json = gson.toJson(object, HasArrayListField.class);
    assertThat(json).isEqualTo("{\"longs\":[1,3]}");
    HasArrayListField copy = gson.fromJson("{\"longs\":[1,3]}", HasArrayListField.class);
    assertThat(copy.longs).isEqualTo(Arrays.asList(1L, 3L));
  }

  @Test
  public void testUserCollectionTypeAdapter() {
    Type listOfString = new TypeToken<List<String>>() {}.getType();
    Object stringListSerializer = new JsonSerializer<List<String>>() {
      @Override public JsonElement serialize(List<String> src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive(src.get(0) + ";" + src.get(1));
      }
    };
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(listOfString, stringListSerializer)
        .create();
    assertThat(gson.toJson(Arrays.asList("ab", "cd"), listOfString)).isEqualTo("\"ab;cd\"");
  }

  static class HasArrayListField {
    ArrayList<Long> longs = new ArrayList<>();
  }

  private static int[] toIntArray(Collection<?> collection) {
    int[] ints = new int[collection.size()];
    int i = 0;
    for (Iterator<?> iterator = collection.iterator(); iterator.hasNext(); ++i) {
      Object obj = iterator.next();
      if (obj instanceof Integer) {
        ints[i] = (Integer) obj;
      } else if (obj instanceof Long) {
        ints[i] = ((Long)obj).intValue();
      }
    }
    return ints;
  }

  private static class ObjectWithWildcardCollection {
    private final Collection<? extends BagOfPrimitives> collection;

    public ObjectWithWildcardCollection(Collection<? extends BagOfPrimitives> collection) {
      this.collection = collection;
    }

    public Collection<? extends BagOfPrimitives> getCollection() {
      return collection;
    }
  }

  private static class Entry {
    int value;
    Entry(int value) {
      this.value = value;
    }
  }
  @Test
  public void testSetSerialization() {
    Set<Entry> set = new HashSet<>();
    set.add(new Entry(1));
    set.add(new Entry(2));
    String json = gson.toJson(set);
    assertThat(json).contains("1");
    assertThat(json).contains("2");
  }
  @Test
  public void testSetDeserialization() {
    String json = "[{value:1},{value:2}]";
    Type type = new TypeToken<Set<Entry>>() {}.getType();
    Set<Entry> set = gson.fromJson(json, type);
    assertThat(set.size()).isEqualTo(2);
    for (Entry entry : set) {
      assertThat(entry.value).isAnyOf(1, 2);
    }
  }

  private class BigClass { private Map<String, ? extends List<SmallClass>> inBig; }

  private class SmallClass { private String inSmall; }

  @Test
  public void testIssue1107() {
    String json = "{\n" +
            "  \"inBig\": {\n" +
            "    \"key\": [\n" +
            "      { \"inSmall\": \"hello\" }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
    BigClass bigClass = new Gson().fromJson(json, BigClass.class);
    SmallClass small = bigClass.inBig.get("key").get(0);
    assertThat(small).isNotNull();
    assertThat(small.inSmall).isEqualTo("hello");
  }

}
