package com.google.gson;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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
    assertEquals(new JsonPrimitive(1), list.get(0));

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
    assertEquals(JsonNull.INSTANCE, list.get(1));
  }

  @Test
  public void testSize() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertEquals(1, list.size());
    list.add(new JsonPrimitive(2));
    assertEquals(2, list.size());
  }

  @Test
  public void testSet() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    JsonElement old = list.set(0, new JsonPrimitive(2));
    assertEquals(new JsonPrimitive(1), old);
    assertEquals(new JsonPrimitive(2), list.get(0));
    assertEquals(new JsonPrimitive(2), a.get(0));

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
      assertEquals("Element must be non-null", e.getMessage());
    }
  }

  @Test
  public void testAdd() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    list.add(0, new JsonPrimitive(2));
    list.add(1, new JsonPrimitive(3));
    assertTrue(list.add(new JsonPrimitive(4)));
    assertTrue(list.add(JsonNull.INSTANCE));

    List<JsonElement> expectedList = Arrays.<JsonElement>asList(
        new JsonPrimitive(2),
        new JsonPrimitive(3),
        new JsonPrimitive(1),
        new JsonPrimitive(4),
        JsonNull.INSTANCE
    );
    assertEquals(expectedList, list);

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
      assertEquals("Element must be non-null", e.getMessage());
    }
    try {
      list.add(null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("Element must be non-null", e.getMessage());
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
    assertEquals(expectedList, list);

    try {
      list.addAll(0, Collections.<JsonElement>singletonList(null));
      fail();
    } catch (NullPointerException e) {
      assertEquals("Element must be non-null", e.getMessage());
    }
    try {
      list.addAll(Collections.<JsonElement>singletonList(null));
      fail();
    } catch (NullPointerException e) {
      assertEquals("Element must be non-null", e.getMessage());
    }
  }

  @Test
  public void testRemoveIndex() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertEquals(new JsonPrimitive(1), list.remove(0));
    assertEquals(0, list.size());
    assertEquals(0, a.size());

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
    assertTrue(list.remove(new JsonPrimitive(1)));
    assertEquals(0, list.size());
    assertEquals(0, a.size());

    assertFalse(list.remove(new JsonPrimitive(1)));
    assertFalse(list.remove(null));
  }

  @Test
  public void testClear() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    list.clear();
    assertEquals(0, list.size());
    assertEquals(0, a.size());
  }

  @Test
  public void testContains() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertTrue(list.contains(new JsonPrimitive(1)));
    assertFalse(list.contains(new JsonPrimitive(2)));
    assertFalse(list.contains(null));

    @SuppressWarnings({"unlikely-arg-type", "CollectionIncompatibleType"})
    boolean containsInt = list.contains(1); // should only contain JsonPrimitive(1)
    assertFalse(containsInt);
  }

  @Test
  public void testIndexOf() {
    JsonArray a = new JsonArray();
    // Add the same value twice to test indexOf vs. lastIndexOf
    a.add(1);
    a.add(1);

    List<JsonElement> list = a.asList();
    assertEquals(0, list.indexOf(new JsonPrimitive(1)));
    assertEquals(-1, list.indexOf(new JsonPrimitive(2)));
    assertEquals(-1, list.indexOf(null));

    @SuppressWarnings({"unlikely-arg-type", "CollectionIncompatibleType"})
    int indexOfInt = list.indexOf(1); // should only contain JsonPrimitive(1)
    assertEquals(-1, indexOfInt);

    assertEquals(1, list.lastIndexOf(new JsonPrimitive(1)));
    assertEquals(-1, list.lastIndexOf(new JsonPrimitive(2)));
    assertEquals(-1, list.lastIndexOf(null));
  }

  @Test
  public void testToArray() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    assertArrayEquals(new Object[] {new JsonPrimitive(1)}, list.toArray());

    JsonElement[] array = list.toArray(new JsonElement[0]);
    assertArrayEquals(new Object[] {new JsonPrimitive(1)}, array);

    array = new JsonElement[1];
    assertSame(array, list.toArray(array));
    assertArrayEquals(new Object[] {new JsonPrimitive(1)}, array);

    array = new JsonElement[] {null, new JsonPrimitive(2)};
    assertSame(array, list.toArray(array));
    // Should have set existing array element to null
    assertArrayEquals(new Object[] {new JsonPrimitive(1), null}, array);
  }

  @Test
  public void testEqualsHashCode() {
    JsonArray a = new JsonArray();
    a.add(1);

    List<JsonElement> list = a.asList();
    MoreAsserts.assertEqualsAndHashCode(list, Collections.singletonList(new JsonPrimitive(1)));
    assertFalse(list.equals(Collections.emptyList()));
    assertFalse(list.equals(Collections.singletonList(new JsonPrimitive(2))));
  }

  /** Verify that {@code JsonArray} updates are visible to view and vice versa */
  @Test
  public void testViewUpdates() {
    JsonArray a = new JsonArray();
    List<JsonElement> list = a.asList();

    a.add(1);
    assertEquals(1, list.size());
    assertEquals(new JsonPrimitive(1), list.get(0));

    list.add(new JsonPrimitive(2));
    assertEquals(2, a.size());
    assertEquals(new JsonPrimitive(2), a.get(1));
  }
}
