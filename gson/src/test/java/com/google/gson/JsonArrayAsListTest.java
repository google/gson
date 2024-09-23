/*
 * Copyright (C) 2022 Google Inc.
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
import static org.junit.Assert.assertThrows;

import com.google.gson.common.MoreAsserts;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.Test;

/** Tests for {@link JsonArray#asList()}. */
public class JsonArrayAsListTest {
  @Test
  public void testGet() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list.get(0)).isEqualTo(new JsonPrimitive(1));

    assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(2));

    a.add((JsonElement) null);
    assertThat(list.get(1)).isEqualTo(JsonNull.INSTANCE);
  }

  @Test
  public void testSize() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list).hasSize(1);
    list.add(new JsonPrimitive(2));
    assertThat(list).hasSize(2);
  }

  @Test
  public void testSet() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    JsonElement old = list.set(0, new JsonPrimitive(2));
    assertThat(old).isEqualTo(new JsonPrimitive(1));
    assertThat(list.get(0)).isEqualTo(new JsonPrimitive(2));
    assertThat(a.get(0)).isEqualTo(new JsonPrimitive(2));

    assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, new JsonPrimitive(1)));
    assertThrows(IndexOutOfBoundsException.class, () -> list.set(2, new JsonPrimitive(1)));

    NullPointerException e = assertThrows(NullPointerException.class, () -> list.set(0, null));
    assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
  }

  @Test
  public void testAdd() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    list.add(0, new JsonPrimitive(2));
    list.add(1, new JsonPrimitive(3));
    assertThat(list.add(new JsonPrimitive(4))).isTrue();
    assertThat(list.add(JsonNull.INSTANCE)).isTrue();

    List<JsonElement> expectedList =
        Arrays.asList(
            new JsonPrimitive(2),
            new JsonPrimitive(3),
            new JsonPrimitive(1),
            new JsonPrimitive(4),
            JsonNull.INSTANCE);
    assertThat(list).isEqualTo(expectedList);

    assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, new JsonPrimitive(1)));
    assertThrows(
        IndexOutOfBoundsException.class, () -> list.set(list.size(), new JsonPrimitive(1)));

    NullPointerException e = assertThrows(NullPointerException.class, () -> list.add(0, null));
    assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");

    e = assertThrows(NullPointerException.class, () -> list.add(null));
    assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
  }

  @Test
  public void testAddAll() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    list.addAll(Arrays.asList(new JsonPrimitive(2), new JsonPrimitive(3)));

    List<JsonElement> expectedList =
        Arrays.asList(new JsonPrimitive(1), new JsonPrimitive(2), new JsonPrimitive(3));
    assertThat(list).isEqualTo(expectedList);
    assertThat(list).isEqualTo(expectedList);

    NullPointerException e =
        assertThrows(
            NullPointerException.class, () -> list.addAll(0, Collections.singletonList(null)));
    assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");

    e =
        assertThrows(
            NullPointerException.class, () -> list.addAll(Collections.singletonList(null)));
    assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
  }

  @Test
  public void testRemoveIndex() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list.remove(0)).isEqualTo(new JsonPrimitive(1));
    assertThat(list).hasSize(0);
    assertThat(a).hasSize(0);

    assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
  }

  @Test
  public void testRemoveElement() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list.remove(new JsonPrimitive(1))).isTrue();
    assertThat(list).hasSize(0);
    assertThat(a).hasSize(0);

    assertThat(list.remove(new JsonPrimitive(1))).isFalse();
    assertThat(list.remove(null)).isFalse();
  }

  @Test
  public void testClear() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    list.clear();
    assertThat(list).hasSize(0);
    assertThat(a).hasSize(0);
  }

  @Test
  public void testContains() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list).contains(new JsonPrimitive(1));
    assertThat(list).doesNotContain(new JsonPrimitive(2));
    assertThat(list).doesNotContain(null);

    @SuppressWarnings({"unlikely-arg-type", "CollectionIncompatibleType"})
    boolean containsInt = list.contains(1); // should only contain JsonPrimitive(1)
    assertThat(containsInt).isFalse();
  }

  @Test
  public void testIndexOf() {
    JsonArray a = new JsonArray();
    // Add the same value twice to test indexOf vs. lastIndexOf
    a.add(1);
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list.indexOf(new JsonPrimitive(1))).isEqualTo(0);
    assertThat(list.indexOf(new JsonPrimitive(2))).isEqualTo(-1);
    assertThat(list.indexOf(null)).isEqualTo(-1);

    @SuppressWarnings({"unlikely-arg-type", "CollectionIncompatibleType"})
    int indexOfInt = list.indexOf(1); // should only contain JsonPrimitive(1)
    assertThat(indexOfInt).isEqualTo(-1);

    assertThat(list.lastIndexOf(new JsonPrimitive(1))).isEqualTo(1);
    assertThat(list.lastIndexOf(new JsonPrimitive(2))).isEqualTo(-1);
    assertThat(list.lastIndexOf(null)).isEqualTo(-1);
  }

  private <T> List<T> spliteratorToList(Spliterator<T> spliterator) {
    return StreamSupport.stream(spliterator, false).collect(Collectors.toList());
  }

  @Test
  public void testSpliterator() {
    JsonArray a = new JsonArray();
    a.add(1);
    a.add(3);
    a.add(2);

    List<JsonElement> list = a.asList();
    List<JsonElement> values = spliteratorToList(list.spliterator());
    assertThat(values)
        .containsExactly(new JsonPrimitive(1), new JsonPrimitive(3), new JsonPrimitive(2))
        .inOrder();

    list = new JsonArray().asList();
    assertThat(spliteratorToList(list.spliterator())).isEmpty();
  }

  @Test
  public void testSort() {
    JsonArray a = new JsonArray();
    a.add(1);
    a.add(3);
    a.add(2);

    List<JsonElement> list = a.asList();
    // JsonElement does not implement Comparable
    assertThrows(ClassCastException.class, () -> list.sort(null));

    list.sort(Comparator.comparingInt(JsonElement::getAsInt));
    assertThat(list)
        .containsExactly(new JsonPrimitive(1), new JsonPrimitive(2), new JsonPrimitive(3))
        .inOrder();
    assertThat(a)
        .containsExactly(new JsonPrimitive(1), new JsonPrimitive(2), new JsonPrimitive(3))
        .inOrder();
  }

  @Test
  public void testReplaceAll() {
    JsonArray a = new JsonArray();
    a.add(1);
    a.add(3);
    a.add(2);

    List<JsonElement> list = a.asList();
    list.replaceAll(element -> new JsonPrimitive(-element.getAsInt()));
    assertThat(list)
        .containsExactly(new JsonPrimitive(-1), new JsonPrimitive(-3), new JsonPrimitive(-2))
        .inOrder();
    assertThat(a)
        .containsExactly(new JsonPrimitive(-1), new JsonPrimitive(-3), new JsonPrimitive(-2))
        .inOrder();

    var e = assertThrows(NullPointerException.class, () -> list.replaceAll(element -> null));
    assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
  }

  @Test
  public void testToArray() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list.toArray()).isEqualTo(new Object[] {new JsonPrimitive(1)});

    JsonElement[] array = list.toArray(new JsonElement[0]);
    assertThat(array).isEqualTo(new Object[] {new JsonPrimitive(1)});

    array = new JsonElement[1];
    assertThat(list.toArray(array)).isEqualTo(array);
    assertThat(array).isEqualTo(new Object[] {new JsonPrimitive(1)});

    array = new JsonElement[] {null, new JsonPrimitive(2)};
    assertThat(list.toArray(array)).isEqualTo(array);
    // Should have set existing array element to null
    assertThat(array).isEqualTo(new Object[] {new JsonPrimitive(1), null});
  }

  @Test
  public void testEqualsHashCode() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    MoreAsserts.assertEqualsAndHashCode(list, Collections.singletonList(new JsonPrimitive(1)));
    assertThat(list.equals(Collections.emptyList())).isFalse();
    assertThat(list.equals(Collections.singletonList(new JsonPrimitive(2)))).isFalse();
  }

  /** Verify that {@code JsonArray} updates are visible to view and vice versa */
  @Test
  public void testViewUpdates() {
    JsonArray a = new JsonArray();
    List<JsonElement> list = a.asList();

    a.add(1);
    assertThat(list).hasSize(1);
    assertThat(list.get(0)).isEqualTo(new JsonPrimitive(1));

    list.add(new JsonPrimitive(2));
    assertThat(a).hasSize(2);
    assertThat(a.get(1)).isEqualTo(new JsonPrimitive(2));
  }
}
