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
import static org.junit.Assert.fail;

import com.google.gson.common.MoreAsserts;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/**
 * Tests for {@link JsonArray#asList()}.
 */
public class JsonArrayAsListTest {
  @Test
  public void testGet() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list.get(0)).isEqualTo(new JsonPrimitive(1));

    try {
      list.get(-1);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }

    try {
      list.get(2);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }

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

    try {
      list.set(-1, new JsonPrimitive(1));
      fail();
    } catch (IndexOutOfBoundsException e) {
    }

    try {
      list.set(2, new JsonPrimitive(1));
      fail();
    } catch (IndexOutOfBoundsException e) {
    }

    try {
      list.set(0, null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
    }
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

    List<JsonElement> expectedList = Arrays.<JsonElement>asList(
        new JsonPrimitive(2),
        new JsonPrimitive(3),
        new JsonPrimitive(1),
        new JsonPrimitive(4),
        JsonNull.INSTANCE
    );
    assertThat(list).isEqualTo(expectedList);

    try {
      list.set(-1, new JsonPrimitive(1));
      fail();
    } catch (IndexOutOfBoundsException e) {
    }

    try {
      list.set(list.size(), new JsonPrimitive(1));
      fail();
    } catch (IndexOutOfBoundsException e) {
    }

    try {
      list.add(0, null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
    }
    try {
      list.add(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
    }
  }

  @Test
  public void testAddAll() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    list.addAll(Arrays.asList(new JsonPrimitive(2), new JsonPrimitive(3)));

    List<JsonElement> expectedList = Arrays.<JsonElement>asList(
        new JsonPrimitive(1),
        new JsonPrimitive(2),
        new JsonPrimitive(3)
    );
    assertThat(list).isEqualTo(expectedList);
    assertThat(list).isEqualTo(expectedList);

    try {
      list.addAll(0, Collections.<JsonElement>singletonList(null));
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
    }
    try {
      list.addAll(Collections.<JsonElement>singletonList(null));
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("Element must be non-null");
    }
  }

  @Test
  public void testRemoveIndex() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertThat(list.remove(0)).isEqualTo(new JsonPrimitive(1));
    assertThat(list).hasSize(0);
    assertThat(a).hasSize(0);
    try {
      list.remove(0);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
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
