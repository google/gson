/*
 * Copyright (C) 2025 Google Inc.
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

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.gson.Gson;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/** Functional tests for Json serialization and deserialization of Guava collections. */
public class GuavaTypeAdaptersTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  // Many test methods here were copied from CollectionTest.

  @Test
  public void testTopLevelListOfIntegerCollectionsDeserialization() {
    String json = "[[1,2,3],[4,5,6],[7,8,9]]";
    Type collectionType = new TypeToken<ImmutableList<ImmutableList<Integer>>>() {}.getType();
    ImmutableList<ImmutableList<Integer>> target = gson.fromJson(json, collectionType);
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
  public void testCollectionOfObjectSerialization() {
    ImmutableList<Object> target = ImmutableList.of("Hello", "World");
    assertThat(gson.toJson(target)).isEqualTo("[\"Hello\",\"World\"]");

    Type type = new TypeToken<ImmutableList<Object>>() {}.getType();
    assertThat(gson.toJson(target, type)).isEqualTo("[\"Hello\",\"World\"]");
  }

  @Test
  public void testCollectionOfStringsSerialization() {
    ImmutableList<String> target = ImmutableList.of("Hello", "World");
    assertThat(gson.toJson(target)).isEqualTo("[\"Hello\",\"World\"]");
    Type type = new TypeToken<ImmutableList<Object>>() {}.getType();
    assertThat(gson.toJson(target, type)).isEqualTo("[\"Hello\",\"World\"]");
  }

  @Test
  public void testCollectionOfBagOfPrimitivesSerialization() {
    ImmutableList<BagOfPrimitives> target =
        ImmutableList.of(
            new BagOfPrimitives(3L, 1, true, "blah"), new BagOfPrimitives(2L, 6, false, "blahB"));

    String result =
        gson.toJson(target, new TypeToken<ImmutableList<BagOfPrimitives>>() {}.getType());
    assertThat(result).startsWith("[");
    assertThat(result).endsWith("]");
    for (BagOfPrimitives obj : target) {
      assertThat(result).contains(obj.getExpectedJson());
    }
  }

  @Test
  public void testCollectionOfStringsDeserialization() {
    String json = "[\"Hello\",\"World\"]";
    Type collectionType = new TypeToken<ImmutableList<String>>() {}.getType();
    ImmutableList<String> target = gson.fromJson(json, collectionType);

    assertThat(target).containsExactly("Hello", "World").inOrder();
  }

  @Test
  public void testRawCollectionDeserialization() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    ImmutableList<?> integers = gson.fromJson(json, ImmutableList.class);
    // JsonReader converts numbers to double by default so we need a floating point comparison
    assertThat(integers)
        .containsExactly(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
        .inOrder();

    json = "[\"Hello\", \"World\"]";
    Collection<?> strings = gson.fromJson(json, ImmutableList.class);
    assertThat(strings).containsExactly("Hello", "World").inOrder();
  }

  @Test
  public void testRawCollectionOfBagOfPrimitivesNotAllowed() {
    BagOfPrimitives bag = new BagOfPrimitives(10, 20, false, "stringValue");
    String json = '[' + bag.getExpectedJson() + ',' + bag.getExpectedJson() + ']';
    ImmutableList<?> target = gson.fromJson(json, ImmutableList.class);
    assertThat(target.size()).isEqualTo(2);
    for (Object bag1 : target) {
      // Gson 2.0 converts raw objects into maps
      @SuppressWarnings("unchecked")
      Map<String, Object> map = (Map<String, Object>) bag1;
      assertThat(map.values()).containsExactly(10.0, 20.0, false, "stringValue");
    }
  }

  @Test
  public void testWildcardPrimitiveCollectionSerilaization() {
    ImmutableList<? extends Integer> target = ImmutableList.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
    Type collectionType = new TypeToken<ImmutableList<? extends Integer>>() {}.getType();
    String json = gson.toJson(target, collectionType);
    assertThat(json).isEqualTo("[1,2,3,4,5,6,7,8,9]");

    json = gson.toJson(target);
    assertThat(json).isEqualTo("[1,2,3,4,5,6,7,8,9]");
  }

  @Test
  public void testWildcardPrimitiveCollectionDeserilaization() {
    String json = "[1,2,3,4,5,6,7,8,9]";
    Type collectionType = new TypeToken<ImmutableList<? extends Integer>>() {}.getType();
    ImmutableList<? extends Integer> target = gson.fromJson(json, collectionType);
    assertThat(target.size()).isEqualTo(9);
    assertThat(target).contains(1);
    assertThat(target).contains(2);
  }

  @Test
  public void testWildcardCollectionField() {
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    ImmutableList<BagOfPrimitives> collection = ImmutableList.of(objA, objB);

    ObjectWithWildcardImmutableList target = new ObjectWithWildcardImmutableList(collection);
    String json = gson.toJson(target);
    assertThat(json).contains(objA.getExpectedJson());
    assertThat(json).contains(objB.getExpectedJson());

    target = gson.fromJson(json, ObjectWithWildcardImmutableList.class);
    ImmutableList<? extends BagOfPrimitives> deserializedCollection = target.getCollection();
    assertThat(deserializedCollection).containsExactly(objA, objB).inOrder();
  }

  private static int[] toIntArray(Collection<?> collection) {
    int[] ints = new int[collection.size()];
    int i = 0;
    for (Iterator<?> iterator = collection.iterator(); iterator.hasNext(); ++i) {
      Object obj = iterator.next();
      if (obj instanceof Integer) {
        ints[i] = (Integer) obj;
      } else if (obj instanceof Long) {
        ints[i] = ((Long) obj).intValue();
      }
    }
    return ints;
  }

  private static class ObjectWithWildcardImmutableList {
    private final ImmutableList<? extends BagOfPrimitives> collection;

    public ObjectWithWildcardImmutableList(ImmutableList<? extends BagOfPrimitives> collection) {
      this.collection = collection;
    }

    public ImmutableList<? extends BagOfPrimitives> getCollection() {
      return collection;
    }
  }

  private static class Entry implements Comparable<Entry> {
    int value;

    Entry(int value) {
      this.value = value;
    }

    @Override
    public int compareTo(Entry other) {
      return Integer.compare(value, other.value);
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof Entry) {
        return value == ((Entry) other).value;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return value;
    }
  }

  @Test
  public void testSetSerialization() {
    ImmutableSet<Entry> set = ImmutableSet.of(new Entry(1), new Entry(2));
    String json = gson.toJson(set);
    assertThat(json).contains("1");
    assertThat(json).contains("2");
  }

  @Test
  public void testSetDeserialization() {
    String json = "[{value:1},{value:2}]";
    Type type = new TypeToken<ImmutableSet<Entry>>() {}.getType();
    ImmutableSet<Entry> set = gson.fromJson(json, type);
    assertThat(set).containsExactly(new Entry(1), new Entry(2)).inOrder();
  }

  @Test
  public void testSortedSetDeserialization() {
    String json = "[{value:2},{value:1}]";
    Type type = new TypeToken<ImmutableSortedSet<Entry>>() {}.getType();
    ImmutableSortedSet<Entry> set = gson.fromJson(json, type);
    assertThat(set).containsExactly(new Entry(1), new Entry(2)).inOrder();
  }

  @Test
  public void testMultiset() {
    ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "a", "a", "b", "b", "c");
    String json = gson.toJson(multiset);
    assertThat(json).isEqualTo("[\"a\",\"a\",\"a\",\"b\",\"b\",\"c\"]");
    ImmutableMultiset<String> deserialized =
        gson.fromJson(json, new TypeToken<ImmutableMultiset<String>>() {});
    assertThat(deserialized).containsExactly("a", "a", "a", "b", "b", "c");
  }

  @Test
  public void testImmutableMap() {
    ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);
    String json = gson.toJson(map);
    assertThat(json).isEqualTo("{\"a\":1,\"b\":2}");

    ImmutableMap<String, Integer> deserialized =
        gson.fromJson(json, new TypeToken<ImmutableMap<String, Integer>>() {}.getType());
    assertThat(deserialized).containsExactly("a", 1, "b", 2);
  }

  @Test
  public void testImmutableMap_complexKeys() {
    Gson complexKeyGson = gson.newBuilder().enableComplexMapKeySerialization().create();
    ImmutableMap<ImmutableList<Integer>, String> map =
        ImmutableMap.of(ImmutableList.of(1, 2), "a", ImmutableList.of(3, 4), "b");
    String json = complexKeyGson.toJson(map);
    assertThat(json).isEqualTo("[[[1,2],\"a\"],[[3,4],\"b\"]]");

    ImmutableMap<ImmutableList<Integer>, String> deserialized =
        complexKeyGson.fromJson(
            json, new TypeToken<ImmutableMap<ImmutableList<Integer>, String>>() {}.getType());
    assertThat(deserialized)
        .containsExactly(ImmutableList.of(1, 2), "a", ImmutableList.of(3, 4), "b");
  }

  @Test
  public void testImmutableBiMap() {
    ImmutableBiMap<String, Integer> map = ImmutableBiMap.of("a", 1, "b", 2);
    String json = gson.toJson(map);
    assertThat(json).isEqualTo("{\"a\":1,\"b\":2}");

    ImmutableBiMap<String, Integer> deserialized =
        gson.fromJson(json, new TypeToken<ImmutableBiMap<String, Integer>>() {}.getType());
    assertThat(deserialized).containsExactly("a", 1, "b", 2);
  }

  @Test
  public void testImmutableSortedMap() {
    ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("a", 1, "b", 2);
    String json = gson.toJson(map);
    assertThat(json).isEqualTo("{\"a\":1,\"b\":2}");

    ImmutableSortedMap<String, Integer> deserialized =
        gson.fromJson(json, new TypeToken<ImmutableSortedMap<String, Integer>>() {}.getType());
    assertThat(deserialized).containsExactly("a", 1, "b", 2);
  }

  @Test
  public void testImmutableMultimap() {
    ImmutableMultimap<String, Integer> map = ImmutableMultimap.of("a", 1, "a", 2, "b", 3);
    String json = gson.toJson(map);
    assertThat(json).isEqualTo("{\"a\":[1,2],\"b\":[3]}");

    ImmutableMultimap<String, Integer> deserialized =
        gson.fromJson(json, new TypeToken<ImmutableMultimap<String, Integer>>() {}.getType());
    assertThat(deserialized).containsExactly("a", 1, "a", 2, "b", 3);
  }

  @Test
  public void testImmutableListMultimap() {
    ImmutableListMultimap<String, Integer> map = ImmutableListMultimap.of("a", 1, "a", 2, "b", 3);
    String json = gson.toJson(map);
    assertThat(json).isEqualTo("{\"a\":[1,2],\"b\":[3]}");

    ImmutableListMultimap<String, Integer> deserialized =
        gson.fromJson(json, new TypeToken<ImmutableListMultimap<String, Integer>>() {}.getType());
    assertThat(deserialized).containsExactly("a", 1, "a", 2, "b", 3);
  }

  @Test
  public void testImmutableSet() {
    ImmutableSetMultimap<String, Integer> map =
        ImmutableSetMultimap.of("a", 1, "a", 2, "a", 1, "b", 3);
    String json = gson.toJson(map);
    assertThat(json).isEqualTo("{\"a\":[1,2],\"b\":[3]}");

    ImmutableSetMultimap<String, Integer> deserialized =
        gson.fromJson(json, new TypeToken<ImmutableSetMultimap<String, Integer>>() {}.getType());
    assertThat(deserialized).containsExactly("a", 1, "a", 2, "b", 3);
  }
}
