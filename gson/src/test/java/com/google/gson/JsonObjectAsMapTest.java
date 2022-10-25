package com.google.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
    assertEquals(0, o.asMap().size());

    o.addProperty("a", 1);
    Map<String, JsonElement> map = o.asMap();
    assertEquals(1, map.size());

    map.clear();
    assertEquals(0, map.size());
    assertEquals(0, o.size());
  }

  @Test
  public void testContainsKey() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    assertTrue(map.containsKey("a"));
    assertFalse(map.containsKey("b"));
    assertFalse(map.containsKey(null));
  }

  @Test
  public void testContainsValue() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);
    o.add("b", JsonNull.INSTANCE);

    Map<String, JsonElement> map = o.asMap();
    assertTrue(map.containsValue(new JsonPrimitive(1)));
    assertFalse(map.containsValue(new JsonPrimitive(2)));
    assertFalse(map.containsValue(null));

    @SuppressWarnings({"unlikely-arg-type", "CollectionIncompatibleType"})
    boolean containsInt = map.containsValue(1); // should only contain JsonPrimitive(1)
    assertFalse(containsInt);
  }

  @Test
  public void testGet() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    assertEquals(new JsonPrimitive(1), map.get("a"));
    assertNull(map.get("b"));
    assertNull(map.get(null));
  }

  @Test
  public void testPut() {
    JsonObject o = new JsonObject();
    Map<String, JsonElement> map = o.asMap();

    assertNull(map.put("a", new JsonPrimitive(1)));
    assertEquals(1, map.size());
    assertEquals(new JsonPrimitive(1), map.get("a"));

    JsonElement old = map.put("a", new JsonPrimitive(2));
    assertEquals(new JsonPrimitive(1), old);
    assertEquals(1, map.size());
    assertEquals(new JsonPrimitive(2), map.get("a"));
    assertEquals(new JsonPrimitive(2), o.get("a"));

    assertNull(map.put("b", JsonNull.INSTANCE));
    assertEquals(JsonNull.INSTANCE, map.get("b"));

    try {
      map.put(null, new JsonPrimitive(1));
      fail();
    } catch (NullPointerException e) {
      assertEquals("key == null", e.getMessage());
    }

    try {
      map.put("a", null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("value == null", e.getMessage());
    }
  }

  @Test
  public void testRemove() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    assertNull(map.remove("b"));
    assertEquals(1, map.size());

    JsonElement old = map.remove("a");
    assertEquals(new JsonPrimitive(1), old);
    assertEquals(0, map.size());

    assertNull(map.remove("a"));
    assertEquals(0, map.size());
    assertEquals(0, o.size());

    assertNull(map.remove(null));
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
    assertEquals(2, map.size());
    assertEquals(new JsonPrimitive(2), map.get("a"));
    assertEquals(new JsonPrimitive(3), map.get("b"));

    try {
      map.putAll(Collections.<String, JsonElement>singletonMap(null, new JsonPrimitive(1)));
      fail();
    } catch (NullPointerException e) {
      assertEquals("key == null", e.getMessage());
    }

    try {
      map.putAll(Collections.<String, JsonElement>singletonMap("a", null));
      fail();
    } catch (NullPointerException e) {
      assertEquals("value == null", e.getMessage());
    }
  }

  @Test
  public void testClear() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    map.clear();
    assertEquals(0, map.size());
    assertEquals(0, o.size());
  }

  @Test
  public void testKeySet() {
    JsonObject o = new JsonObject();
    o.addProperty("b", 1);
    o.addProperty("a", 2);

    Map<String, JsonElement> map = o.asMap();
    Set<String> keySet = map.keySet();
    // Should contain keys in same order
    assertEquals(Arrays.asList("b", "a"), new ArrayList<>(keySet));

    // Key set doesn't support insertions
    try {
      keySet.add("c");
      fail();
    } catch (UnsupportedOperationException e) {
    }

    assertTrue(keySet.remove("a"));
    assertEquals(Collections.singleton("b"), map.keySet());
    assertEquals(Collections.singleton("b"), o.keySet());
  }

  @Test
  public void testValues() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 2);
    o.addProperty("b", 1);

    Map<String, JsonElement> map = o.asMap();
    Collection<JsonElement> values = map.values();
    // Should contain values in same order
    assertEquals(Arrays.asList(new JsonPrimitive(2), new JsonPrimitive(1)), new ArrayList<>(values));

    // Values collection doesn't support insertions
    try {
      values.add(new JsonPrimitive(3));
      fail();
    } catch (UnsupportedOperationException e) {
    }

    assertTrue(values.remove(new JsonPrimitive(2)));
    assertEquals(Collections.singletonList(new JsonPrimitive(1)), new ArrayList<>(map.values()));
    assertEquals(1, o.size());
    assertEquals(new JsonPrimitive(1), o.get("b"));
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
    assertEquals(expectedEntrySet, new ArrayList<>(entrySet));

    try {
      entrySet.add(new SimpleEntry<String, JsonElement>("c", new JsonPrimitive(3)));
      fail();
    } catch (UnsupportedOperationException e) {
    }

    assertTrue(entrySet.remove(new SimpleEntry<>("a", new JsonPrimitive(1))));
    assertEquals(Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(2))), map.entrySet());
    assertEquals(Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(2))), o.entrySet());

    // Should return false because entry has already been removed
    assertFalse(entrySet.remove(new SimpleEntry<>("a", new JsonPrimitive(1))));

    Entry<String, JsonElement> entry = entrySet.iterator().next();
    JsonElement old = entry.setValue(new JsonPrimitive(3));
    assertEquals(new JsonPrimitive(2), old);
    assertEquals(Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(3))), map.entrySet());
    assertEquals(Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(3))), o.entrySet());

    try {
      entry.setValue(null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("value == null", e.getMessage());
    }
  }

  @Test
  public void testEqualsHashCode() {
    JsonObject o = new JsonObject();
    o.addProperty("a", 1);

    Map<String, JsonElement> map = o.asMap();
    MoreAsserts.assertEqualsAndHashCode(map, Collections.singletonMap("a", new JsonPrimitive(1)));
    assertFalse(map.equals(Collections.emptyMap()));
    assertFalse(map.equals(Collections.singletonMap("a", new JsonPrimitive(2))));
  }

  /** Verify that {@code JsonObject} updates are visible to view and vice versa */
  @Test
  public void testViewUpdates() {
    JsonObject o = new JsonObject();
    Map<String, JsonElement> map = o.asMap();

    o.addProperty("a", 1);
    assertEquals(1, map.size());
    assertEquals(new JsonPrimitive(1), map.get("a"));

    map.put("b", new JsonPrimitive(2));
    assertEquals(2, o.size());
    assertEquals(new JsonPrimitive(2), o.get("b"));
  }
}
