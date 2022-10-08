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

package com.google.gson;

import com.google.gson.common.MoreAsserts;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Unit test for the {@link JsonObject} class.
 *
 * @author Joel Leitch
 */
public class JsonObjectTest extends TestCase {

  public void testAddingAndRemovingObjectProperties() throws Exception {
    JsonObject jsonObj = new JsonObject();
    String propertyName = "property";
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));

    JsonPrimitive value = new JsonPrimitive("blah");
    jsonObj.add(propertyName, value);
    assertEquals(value, jsonObj.get(propertyName));

    JsonElement removedElement = jsonObj.remove(propertyName);
    assertEquals(value, removedElement);
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));

    assertNull(jsonObj.remove(propertyName));
  }

  public void testAddingNullPropertyValue() throws Exception {
    String propertyName = "property";
    JsonObject jsonObj = new JsonObject();
    jsonObj.add(propertyName, null);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.isJsonNull());
  }

  public void testAddingNullOrEmptyPropertyName() throws Exception {
    JsonObject jsonObj = new JsonObject();
    try {
      jsonObj.add(null, JsonNull.INSTANCE);
      fail("Should not allow null property names.");
    } catch (NullPointerException expected) { }

    jsonObj.add("", JsonNull.INSTANCE);
    jsonObj.add("   \t", JsonNull.INSTANCE);
  }

  public void testAddingBooleanProperties() throws Exception {
    String propertyName = "property";
    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, true);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.getAsBoolean());
  }

  public void testAddingStringProperties() throws Exception {
    String propertyName = "property";
    String value = "blah";

    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(value, jsonElement.getAsString());
  }

  public void testAddingCharacterProperties() throws Exception {
    String propertyName = "property";
    char value = 'a';

    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(String.valueOf(value), jsonElement.getAsString());

    @SuppressWarnings("deprecation")
    char character = jsonElement.getAsCharacter();
    assertEquals(value, character);
  }

  /**
   * From bug report http://code.google.com/p/google-gson/issues/detail?id=182
   */
  public void testPropertyWithQuotes() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.add("a\"b", new JsonPrimitive("c\"d"));
    String json = new Gson().toJson(jsonObj);
    assertEquals("{\"a\\\"b\":\"c\\\"d\"}", json);
  }

  /**
   * From issue 227.
   */
  public void testWritePropertyWithEmptyStringName() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.add("", new JsonPrimitive(true));
    assertEquals("{\"\":true}", new Gson().toJson(jsonObj));

  }

  public void testReadPropertyWithEmptyStringName() {
    JsonObject jsonObj = JsonParser.parseString("{\"\":true}").getAsJsonObject();
    assertEquals(true, jsonObj.get("").getAsBoolean());
  }

  public void testEqualsOnEmptyObject() {
    MoreAsserts.assertEqualsAndHashCode(new JsonObject(), new JsonObject());
  }

  public void testEqualsNonEmptyObject() {
    JsonObject a = new JsonObject();
    JsonObject b = new JsonObject();

    assertEquals(a, a);

    a.add("foo", new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("foo", new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add("bar", new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("bar", JsonNull.INSTANCE);
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  public void testEqualsHashCodeIgnoringOrder() {
    JsonObject a = new JsonObject();
    JsonObject b = new JsonObject();

    a.addProperty("1", true);
    b.addProperty("2", false);

    a.addProperty("2", false);
    b.addProperty("1", true);

    assertEquals(Arrays.asList("1", "2"), new ArrayList<>(a.keySet()));
    assertEquals(Arrays.asList("2", "1"), new ArrayList<>(b.keySet()));

    MoreAsserts.assertEqualsAndHashCode(a, b);
  }

  public void testSize() {
    JsonObject o = new JsonObject();
    assertEquals(0, o.size());

    o.add("Hello", new JsonPrimitive(1));
    assertEquals(1, o.size());

    o.add("Hi", new JsonPrimitive(1));
    assertEquals(2, o.size());

    o.remove("Hello");
    assertEquals(1, o.size());
  }

  public void testDeepCopy() {
    JsonObject original = new JsonObject();
    JsonArray firstEntry = new JsonArray();
    original.add("key", firstEntry);

    JsonObject copy = original.deepCopy();
    firstEntry.add(new JsonPrimitive("z"));

    assertEquals(1, original.get("key").getAsJsonArray().size());
    assertEquals(0, copy.get("key").getAsJsonArray().size());
  }

  /**
   * From issue 941
   */
  public void testKeySet() {
    JsonObject a = new JsonObject();
    assertEquals(0, a.keySet().size());

    a.add("foo", new JsonArray());
    a.add("bar", new JsonObject());

    assertEquals(2, a.size());
    assertEquals(2, a.keySet().size());
    assertTrue(a.keySet().contains("foo"));
    assertTrue(a.keySet().contains("bar"));

    a.addProperty("1", true);
    a.addProperty("2", false);

    // Insertion order should be preserved by keySet()
    Deque<String> expectedKeys = new ArrayDeque<>(Arrays.asList("foo", "bar", "1", "2"));
    // Note: Must wrap in ArrayList because Deque implementations do not implement `equals`
    assertEquals(new ArrayList<>(expectedKeys), new ArrayList<>(a.keySet()));
    Iterator<String> iterator = a.keySet().iterator();

    // Remove keys one by one
    for (int i = a.size(); i >= 1; i--) {
      assertTrue(iterator.hasNext());
      assertEquals(expectedKeys.getFirst(), iterator.next());
      iterator.remove();
      expectedKeys.removeFirst();

      assertEquals(i - 1, a.size());
      assertEquals(new ArrayList<>(expectedKeys), new ArrayList<>(a.keySet()));
    }
  }

  public void testEntrySet() {
    JsonObject o = new JsonObject();
    assertEquals(0, o.entrySet().size());

    o.addProperty("b", true);
    Set<?> expectedEntries = Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(true)));
    assertEquals(expectedEntries, o.entrySet());
    assertEquals(1, o.entrySet().size());

    o.addProperty("a", false);
    // Insertion order should be preserved by entrySet()
    List<?> expectedEntriesList = Arrays.asList(
        new SimpleEntry<>("b", new JsonPrimitive(true)),
        new SimpleEntry<>("a", new JsonPrimitive(false))
      );
    assertEquals(expectedEntriesList, new ArrayList<>(o.entrySet()));

    Iterator<Entry<String, JsonElement>> iterator = o.entrySet().iterator();
    // Test behavior of Entry.setValue
    for (int i = 0; i < o.size(); i++) {
      Entry<String, JsonElement> entry = iterator.next();
      entry.setValue(new JsonPrimitive(i));

      assertEquals(new JsonPrimitive(i), entry.getValue());
    }

    expectedEntriesList = Arrays.asList(
        new SimpleEntry<>("b", new JsonPrimitive(0)),
        new SimpleEntry<>("a", new JsonPrimitive(1))
      );
    assertEquals(expectedEntriesList, new ArrayList<>(o.entrySet()));

    Entry<String, JsonElement> entry = o.entrySet().iterator().next();
    try {
      // null value is not permitted, only JsonNull is supported
      // This intentionally deviates from the behavior of the other JsonObject methods which
      // implicitly convert null -> JsonNull, to match more closely the contract of Map.Entry
      entry.setValue(null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("value == null", e.getMessage());
    }
    assertNotNull(entry.getValue());

    o.addProperty("key1", 1);
    o.addProperty("key2", 2);

    Deque<?> expectedEntriesQueue = new ArrayDeque<>(Arrays.asList(
        new SimpleEntry<>("b", new JsonPrimitive(0)),
        new SimpleEntry<>("a", new JsonPrimitive(1)),
        new SimpleEntry<>("key1", new JsonPrimitive(1)),
        new SimpleEntry<>("key2", new JsonPrimitive(2))
      ));
    // Note: Must wrap in ArrayList because Deque implementations do not implement `equals`
    assertEquals(new ArrayList<>(expectedEntriesQueue), new ArrayList<>(o.entrySet()));
    iterator = o.entrySet().iterator();

    // Remove entries one by one
    for (int i = o.size(); i >= 1; i--) {
      assertTrue(iterator.hasNext());
      assertEquals(expectedEntriesQueue.getFirst(), iterator.next());
      iterator.remove();
      expectedEntriesQueue.removeFirst();

      assertEquals(i - 1, o.size());
      assertEquals(new ArrayList<>(expectedEntriesQueue), new ArrayList<>(o.entrySet()));
    }
  }
}
