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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

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
import org.junit.Test;

/**
 * Unit test for the {@link JsonObject} class.
 *
 * @author Joel Leitch
 */
public class JsonObjectTest {

  @Test
  public void testAddingAndRemovingObjectProperties() {
    JsonObject jsonObj = new JsonObject();
    String propertyName = "property";
    assertThat(jsonObj.has(propertyName)).isFalse();
    assertThat(jsonObj.get(propertyName)).isNull();

    JsonPrimitive value = new JsonPrimitive("blah");
    jsonObj.add(propertyName, value);
    assertThat(jsonObj.get(propertyName)).isEqualTo(value);

    JsonElement removedElement = jsonObj.remove(propertyName);
    assertThat(removedElement).isEqualTo(value);
    assertThat(jsonObj.has(propertyName)).isFalse();
    assertThat(jsonObj.get(propertyName)).isNull();

    assertThat(jsonObj.remove(propertyName)).isNull();
  }

  @Test
  public void testAddingNullPropertyValue() {
    String propertyName = "property";
    JsonObject jsonObj = new JsonObject();
    jsonObj.add(propertyName, null);

    assertThat(jsonObj.has(propertyName)).isTrue();

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertThat(jsonElement).isNotNull();
    assertThat(jsonElement.isJsonNull()).isTrue();
  }

  @Test
  public void testAddingNullOrEmptyPropertyName() {
    JsonObject jsonObj = new JsonObject();
    try {
      jsonObj.add(null, JsonNull.INSTANCE);
      fail("Should not allow null property names.");
    } catch (NullPointerException expected) { }

    jsonObj.add("", JsonNull.INSTANCE);
    jsonObj.add("   \t", JsonNull.INSTANCE);
  }

  @Test
  public void testAddingBooleanProperties() {
    String propertyName = "property";
    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, true);

    assertThat(jsonObj.has(propertyName)).isTrue();

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertThat(jsonElement).isNotNull();
    assertThat(jsonElement.getAsBoolean()).isTrue();
  }

  @Test
  public void testAddingStringProperties() {
    String propertyName = "property";
    String value = "blah";

    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertThat(jsonObj.has(propertyName)).isTrue();

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertThat(jsonElement).isNotNull();
    assertThat(jsonElement.getAsString()).isEqualTo(value);
  }

  @Test
  public void testAddingCharacterProperties() {
    String propertyName = "property";
    char value = 'a';

    JsonObject jsonObj = new JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertThat(jsonObj.has(propertyName)).isTrue();

    JsonElement jsonElement = jsonObj.get(propertyName);
    assertThat(jsonElement).isNotNull();
    assertThat(jsonElement.getAsString()).isEqualTo(String.valueOf(value));

    @SuppressWarnings("deprecation")
    char character = jsonElement.getAsCharacter();
    assertThat(character).isEqualTo(value);
  }

  /**
   * From bug report http://code.google.com/p/google-gson/issues/detail?id=182
   */
  @Test
  public void testPropertyWithQuotes() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.add("a\"b", new JsonPrimitive("c\"d"));
    String json = new Gson().toJson(jsonObj);
    assertThat(json).isEqualTo("{\"a\\\"b\":\"c\\\"d\"}");
  }

  /**
   * From issue 227.
   */
  @Test
  public void testWritePropertyWithEmptyStringName() {
    JsonObject jsonObj = new JsonObject();
    jsonObj.add("", new JsonPrimitive(true));
    assertThat(new Gson().toJson(jsonObj)).isEqualTo("{\"\":true}");
  }

  @Test
  public void testReadPropertyWithEmptyStringName() {
    JsonObject jsonObj = JsonParser.parseString("{\"\":true}").getAsJsonObject();
    assertThat(jsonObj.get("").getAsBoolean()).isTrue();
  }

  @Test
  public void testEqualsOnEmptyObject() {
    MoreAsserts.assertEqualsAndHashCode(new JsonObject(), new JsonObject());
  }

  @Test
  @SuppressWarnings("TruthSelfEquals")
  public void testEqualsNonEmptyObject() {
    JsonObject a = new JsonObject();
    JsonObject b = new JsonObject();

    assertThat(a).isEqualTo(a);

    a.add("foo", new JsonObject());
    assertThat(a.equals(b)).isFalse();
    assertThat(b.equals(a)).isFalse();

    b.add("foo", new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add("bar", new JsonObject());
    assertThat(a.equals(b)).isFalse();
    assertThat(b.equals(a)).isFalse();

    b.add("bar", JsonNull.INSTANCE);
    assertThat(a.equals(b)).isFalse();
    assertThat(b.equals(a)).isFalse();
  }

  @Test
  public void testEqualsHashCodeIgnoringOrder() {
    JsonObject a = new JsonObject();
    JsonObject b = new JsonObject();

    a.addProperty("1", true);
    b.addProperty("2", false);

    a.addProperty("2", false);
    b.addProperty("1", true);

    assertThat(new ArrayList<>(a.keySet())).containsExactly("1", "2").inOrder();
    assertThat(new ArrayList<>(b.keySet())).containsExactly("2", "1").inOrder();

    MoreAsserts.assertEqualsAndHashCode(a, b);
  }

  @Test
  public void testSize() {
    JsonObject o = new JsonObject();
    assertThat(o.size()).isEqualTo(0);

    o.add("Hello", new JsonPrimitive(1));
    assertThat(o.size()).isEqualTo(1);

    o.add("Hi", new JsonPrimitive(1));
    assertThat(o.size()).isEqualTo(2);

    o.remove("Hello");
    assertThat(o.size()).isEqualTo(1);
  }

  @Test
  public void testIsEmpty() {
    JsonObject o = new JsonObject();
    assertThat(o.isEmpty()).isTrue();

    o.add("Hello", new JsonPrimitive(1));
    assertThat(o.isEmpty()).isFalse();

    o.remove("Hello");
    assertThat(o.isEmpty()).isTrue();
  }

  @Test
  public void testDeepCopy() {
    JsonObject original = new JsonObject();
    JsonArray firstEntry = new JsonArray();
    original.add("key", firstEntry);

    JsonObject copy = original.deepCopy();
    firstEntry.add(new JsonPrimitive("z"));

    assertThat(original.get("key").getAsJsonArray()).hasSize(1);
    assertThat(copy.get("key").getAsJsonArray()).hasSize(0);
  }

  /**
   * From issue 941
   */
  @Test
  public void testKeySet() {
    JsonObject a = new JsonObject();
    assertThat(a.keySet()).hasSize(0);

    a.add("foo", new JsonArray());
    a.add("bar", new JsonObject());

    assertThat(a.size()).isEqualTo(2);
    assertThat(a.keySet()).hasSize(2);
    assertThat(a.keySet()).containsExactly("foo", "bar").inOrder();

    a.addProperty("1", true);
    a.addProperty("2", false);

    // Insertion order should be preserved by keySet()
    Deque<String> expectedKeys = new ArrayDeque<>(Arrays.asList("foo", "bar", "1", "2"));
    // Note: Must wrap in ArrayList because Deque implementations do not implement `equals`
    assertThat(new ArrayList<>(a.keySet())).isEqualTo(new ArrayList<>(expectedKeys));
    Iterator<String> iterator = a.keySet().iterator();

    // Remove keys one by one
    for (int i = a.size(); i >= 1; i--) {
      assertThat(iterator.hasNext()).isTrue();
      assertThat(iterator.next()).isEqualTo(expectedKeys.getFirst());
      iterator.remove();
      expectedKeys.removeFirst();

      assertThat(a.size()).isEqualTo(i - 1);
      assertThat(new ArrayList<>(a.keySet())).isEqualTo(new ArrayList<>(expectedKeys));
    }
  }

  @Test
  public void testEntrySet() {
    JsonObject o = new JsonObject();
    assertThat(o.entrySet()).hasSize(0);

    o.addProperty("b", true);
    Set<?> expectedEntries = Collections.singleton(new SimpleEntry<>("b", new JsonPrimitive(true)));
    assertThat(o.entrySet()).isEqualTo(expectedEntries);
    assertThat(o.entrySet()).hasSize(1);

    o.addProperty("a", false);
    // Insertion order should be preserved by entrySet()
    List<?> expectedEntriesList = Arrays.asList(
        new SimpleEntry<>("b", new JsonPrimitive(true)),
        new SimpleEntry<>("a", new JsonPrimitive(false))
      );
    assertThat(new ArrayList<>(o.entrySet())).isEqualTo(expectedEntriesList);

    Iterator<Entry<String, JsonElement>> iterator = o.entrySet().iterator();
    // Test behavior of Entry.setValue
    for (int i = 0; i < o.size(); i++) {
      Entry<String, JsonElement> entry = iterator.next();
      entry.setValue(new JsonPrimitive(i));

      assertThat(entry.getValue()).isEqualTo(new JsonPrimitive(i));
    }

    expectedEntriesList = Arrays.asList(
        new SimpleEntry<>("b", new JsonPrimitive(0)),
        new SimpleEntry<>("a", new JsonPrimitive(1))
      );
    assertThat(new ArrayList<>(o.entrySet())).isEqualTo(expectedEntriesList);

    Entry<String, JsonElement> entry = o.entrySet().iterator().next();
    try {
      // null value is not permitted, only JsonNull is supported
      // This intentionally deviates from the behavior of the other JsonObject methods which
      // implicitly convert null -> JsonNull, to match more closely the contract of Map.Entry
      entry.setValue(null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("value == null");
    }
    assertThat(entry.getValue()).isNotNull();

    o.addProperty("key1", 1);
    o.addProperty("key2", 2);

    Deque<?> expectedEntriesQueue = new ArrayDeque<>(Arrays.asList(
        new SimpleEntry<>("b", new JsonPrimitive(0)),
        new SimpleEntry<>("a", new JsonPrimitive(1)),
        new SimpleEntry<>("key1", new JsonPrimitive(1)),
        new SimpleEntry<>("key2", new JsonPrimitive(2))
      ));
    // Note: Must wrap in ArrayList because Deque implementations do not implement `equals`
    assertThat(new ArrayList<>(o.entrySet())).isEqualTo(new ArrayList<>(expectedEntriesQueue));
    iterator = o.entrySet().iterator();

    // Remove entries one by one
    for (int i = o.size(); i >= 1; i--) {
      assertThat(iterator.hasNext()).isTrue();
      assertThat(iterator.next()).isEqualTo(expectedEntriesQueue.getFirst());
      iterator.remove();
      expectedEntriesQueue.removeFirst();

      assertThat(o.size()).isEqualTo(i - 1);
      assertThat(new ArrayList<>(o.entrySet())).isEqualTo(new ArrayList<>(expectedEntriesQueue));
    }
  }
}
