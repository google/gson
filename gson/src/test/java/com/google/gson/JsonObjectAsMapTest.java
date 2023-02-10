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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;

/**
 * Tests for {@link JsonObject#asMap()}.
 */
public class JsonObjectAsMapTest {
  @Test
  public void testSize() {
    JsonObject o = new JsonObject();
    assertThat(o.asMap().size()).isEqualTo(0);

    o.addProperty("a", 1);
    Map<String, JsonElement> map = o.asMap();
    assertThat(map).hasSize(1);

    map.clear();
    assertThat(map).hasSize(0);
    assertThat(o.size()).isEqualTo(0);
  }

  @Test
  public void testContainsKey() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.containsKey("b")).isFalse();
    assertThat(map.containsKey(null)).isFalse();
  }

  @Test
  public void testContainsValue() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);
    o.add("b", JsonNull.INSTANCE);

    Map<String, JsonElement> map = o.asMap();
    assertThat(map.containsValue(new JsonPrimitive(1))).isTrue();
    assertThat(map.containsValue(new JsonPrimitive(2))).isFalse();
    assertThat(map.containsValue(null)).isFalse();

    @SuppressWarnings({"unlikely-arg-type", "CollectionIncompatibleType"})
    boolean containsInt = map.containsValue(1); // should only contain JsonPrimitive(1)
    assertThat(containsInt).isFalse();
  }

  @Test
  public void testGet() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    assertThat(map.get("a")).isEqualTo(new JsonPrimitive(1));
    assertThat(map.get("b")).isNull();
    assertThat(map.get(null)).isNull();
  }

  @Test
  public void testPut() {
    JsonObject o = new JsonObject();
    Map<String, JsonElement> map = o.asMap();

    assertThat(map.put("a", new JsonPrimitive(1))).isNull();
    assertThat(map.get("a")).isEqualTo(new JsonPrimitive(1));

    JsonElement old = map.put("a", new JsonPrimitive(2));
    assertThat(old).isEqualTo(new JsonPrimitive(1));
    assertThat(map).hasSize(1);
    assertThat(map.get("a")).isEqualTo(new JsonPrimitive(2));
    assertThat(o.get("a")).isEqualTo(new JsonPrimitive(2));

    assertThat(map.put("b", JsonNull.INSTANCE)).isNull();
    assertThat(map.get("b")).isEqualTo(JsonNull.INSTANCE);

    try {
      map.put(null, new JsonPrimitive(1));
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("key == null");
    }

    try {
      map.put("a", null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("value == null");
    }
  }

  @Test
  public void testRemove() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    assertThat(map.remove("b")).isNull();
    assertThat(map).hasSize(1);

    JsonElement old = map.remove("a");
    assertThat(old).isEqualTo(new JsonPrimitive(1));
    assertThat(map).hasSize(0);

    assertThat(map.remove("a")).isNull();
    assertThat(map).hasSize(0);
    assertThat(o.size()).isEqualTo(0);

    assertThat(map.remove(null)).isNull();
  }

  @Test
  public void testPutAll() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> otherMap = new HashMap<>();
    otherMap.put("a", new JsonPrimitive(2));
    otherMap.put("b", new JsonPrimitive(3));

    Map<String, JsonElement> map = o.asMap();
    map.putAll(otherMap);
    assertThat(map).hasSize(2);
    assertThat(map.get("a")).isEqualTo(new JsonPrimitive(2));
    assertThat(map.get("b")).isEqualTo(new JsonPrimitive(3));

    try {
      map.putAll(Collections.<String, JsonElement>singletonMap(null, new JsonPrimitive(1)));
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("key == null");
    }

    try {
      map.putAll(Collections.<String, JsonElement>singletonMap("a", null));
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("value == null");
    }
  }

  @Test
  public void testClear() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    map.clear();
    assertThat(map).hasSize(0);
    assertThat(o.size()).isEqualTo(0);
  }

  @Test
  public void testKeySet() {
    JsonObject o = new JsonObject();
    o.addProperty("b", 1);
    o.addProperty("a", 2);

    Map<String, JsonElement> map = o.asMap();
    Set<String> keySet = map.keySet();
    // Should contain keys in same order
    assertThat(keySet).containsExactly("b", "a").inOrder();

    // Key set doesn't support insertions
    try {
      keySet.add("c");
      fail();
    } catch (UnsupportedOperationException e) {
    }

    assertThat(keySet.remove("a")).isTrue();
    assertThat(map.keySet()).isEqualTo(Collections.singleton("b"));
    assertThat(o.keySet()).isEqualTo(Collections.singleton("b"));
  }

  @Test
  public void testValues() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 2);
    o.addProperty("b", 1);

    Map<String, JsonElement> map = o.asMap();
    Collection<JsonElement> values = map.values();
    // Should contain values in same order
    assertThat(values).containsExactly(new JsonPrimitive(2), new JsonPrimitive(1)).inOrder();

    // Values collection doesn't support insertions
    try {
      values.add(new JsonPrimitive(3));
      fail();
    } catch (UnsupportedOperationException e) {
    }

    assertThat(values.remove(new JsonPrimitive(2))).isTrue();
    assertThat(new ArrayList<>(map.values())).isEqualTo(Collections.singletonList(new JsonPrimitive(1)));
    assertThat(o.size()).isEqualTo(1);
    assertThat(o.get("b")).isEqualTo(new JsonPrimitive(1));
  }

  @Test
  public void testEntrySet() {
    JsonObject o = new JsonObject();
    o.addProperty("b", 2);
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    Set<Entry<String, JsonElement>> entrySet = map.entrySet();

    List<Entry<?, ?>> expectedEntrySet = Arrays.<Entry<?, ?>>asList(
        new SimpleEntry<>("b", new JsonPrimitive(2)),
        new SimpleEntry<>("a", new JsonPrimitive(1))
    );
    // Should contain entries in same order
    assertThat(new ArrayList<>(entrySet)).isEqualTo(expectedEntrySet);

    try {
      entrySet.add(new SimpleEntry<String, JsonElement>("c", new JsonPrimitive(3)));
      fail();
    } catch (UnsupportedOperationException e) {
    }

    assertThat(entrySet.remove(new SimpleEntry<>("a", new JsonPrimitive(1)))).isTrue();
    assertThat(map.entrySet()).isEqualTo(Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(2))));
    assertThat(o.entrySet()).isEqualTo(Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(2))));

    // Should return false because entry has already been removed
    assertThat(entrySet.remove(new SimpleEntry<>("a", new JsonPrimitive(1)))).isFalse();

    Entry<String, JsonElement> entry = entrySet.iterator().next();
    JsonElement old = entry.setValue(new JsonPrimitive(3));
    assertThat(old).isEqualTo(new JsonPrimitive(2));
    assertThat(map.entrySet()).isEqualTo(Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(3))));
    assertThat(o.entrySet()).isEqualTo(Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(3))));

    try {
      entry.setValue(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("value == null");
    }
  }

  @Test
  public void testEqualsHashCode() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    MoreAsserts.assertEqualsAndHashCode(map, Collections.singletonMap("a", new JsonPrimitive(1)));
    assertThat(map.equals(Collections.emptyMap())).isFalse();
    assertThat(map.equals(Collections.singletonMap("a", new JsonPrimitive(2)))).isFalse();
  }

  /** Verify that {@code JsonObject} updates are visible to view and vice versa */
  @Test
  public void testViewUpdates() {
    JsonObject o = new JsonObject();
    Map<String, JsonElement> map = o.asMap();

    o.addProperty("a", 1);
    assertThat(map).hasSize(1);
    assertThat(map.get("a")).isEqualTo(new JsonPrimitive(1));

    map.put("b", new JsonPrimitive(2));
    assertThat(o.size()).isEqualTo(2);
    assertThat(map.get("b")).isEqualTo(new JsonPrimitive(2));
  }
}
