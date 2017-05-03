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

package com.economic.persistgson;

import com.economic.persistgson.common.MoreAsserts;

import junit.framework.TestCase;

/**
 * Unit test for the {@link com.economic.persistgson.JsonObject} class.
 *
 * @author Joel Leitch
 */
public class JsonObjectTest extends TestCase {

  public void testAddingAndRemovingObjectProperties() throws Exception {
    com.economic.persistgson.JsonObject jsonObj = new com.economic.persistgson.JsonObject();
    String propertyName = "property";
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));

    com.economic.persistgson.JsonPrimitive value = new com.economic.persistgson.JsonPrimitive("blah");
    jsonObj.add(propertyName, value);
    assertEquals(value, jsonObj.get(propertyName));

    com.economic.persistgson.JsonElement removedElement = jsonObj.remove(propertyName);
    assertEquals(value, removedElement);
    assertFalse(jsonObj.has(propertyName));
    assertNull(jsonObj.get(propertyName));
  }

  public void testAddingNullPropertyValue() throws Exception {
    String propertyName = "property";
    com.economic.persistgson.JsonObject jsonObj = new com.economic.persistgson.JsonObject();
    jsonObj.add(propertyName, null);

    assertTrue(jsonObj.has(propertyName));

    com.economic.persistgson.JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.isJsonNull());
  }

  public void testAddingNullOrEmptyPropertyName() throws Exception {
    com.economic.persistgson.JsonObject jsonObj = new com.economic.persistgson.JsonObject();
    try {
      jsonObj.add(null, com.economic.persistgson.JsonNull.INSTANCE);
      fail("Should not allow null property names.");
    } catch (NullPointerException expected) { }

    jsonObj.add("", com.economic.persistgson.JsonNull.INSTANCE);
    jsonObj.add("   \t", com.economic.persistgson.JsonNull.INSTANCE);
  }

  public void testAddingBooleanProperties() throws Exception {
    String propertyName = "property";
    com.economic.persistgson.JsonObject jsonObj = new com.economic.persistgson.JsonObject();
    jsonObj.addProperty(propertyName, true);

    assertTrue(jsonObj.has(propertyName));

    com.economic.persistgson.JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertTrue(jsonElement.getAsBoolean());
  }

  public void testAddingStringProperties() throws Exception {
    String propertyName = "property";
    String value = "blah";

    com.economic.persistgson.JsonObject jsonObj = new com.economic.persistgson.JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    com.economic.persistgson.JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(value, jsonElement.getAsString());
  }

  public void testAddingCharacterProperties() throws Exception {
    String propertyName = "property";
    char value = 'a';

    com.economic.persistgson.JsonObject jsonObj = new com.economic.persistgson.JsonObject();
    jsonObj.addProperty(propertyName, value);

    assertTrue(jsonObj.has(propertyName));

    com.economic.persistgson.JsonElement jsonElement = jsonObj.get(propertyName);
    assertNotNull(jsonElement);
    assertEquals(String.valueOf(value), jsonElement.getAsString());
    assertEquals(value, jsonElement.getAsCharacter());
  }

  /**
   * From bug report http://code.google.com/p/google-gson/issues/detail?id=182
   */
  public void testPropertyWithQuotes() {
    com.economic.persistgson.JsonObject jsonObj = new com.economic.persistgson.JsonObject();
    jsonObj.add("a\"b", new com.economic.persistgson.JsonPrimitive("c\"d"));
    String json = new com.economic.persistgson.Gson().toJson(jsonObj);
    assertEquals("{\"a\\\"b\":\"c\\\"d\"}", json);
  }

  /**
   * From issue 227.
   */
  public void testWritePropertyWithEmptyStringName() {
    com.economic.persistgson.JsonObject jsonObj = new com.economic.persistgson.JsonObject();
    jsonObj.add("", new com.economic.persistgson.JsonPrimitive(true));
    assertEquals("{\"\":true}", new Gson().toJson(jsonObj));

  }

  public void testReadPropertyWithEmptyStringName() {
    com.economic.persistgson.JsonObject jsonObj = new JsonParser().parse("{\"\":true}").getAsJsonObject();
    assertEquals(true, jsonObj.get("").getAsBoolean());
  }

  public void testEqualsOnEmptyObject() {
    MoreAsserts.assertEqualsAndHashCode(new com.economic.persistgson.JsonObject(), new com.economic.persistgson.JsonObject());
  }

  public void testEqualsNonEmptyObject() {
    com.economic.persistgson.JsonObject a = new com.economic.persistgson.JsonObject();
    com.economic.persistgson.JsonObject b = new com.economic.persistgson.JsonObject();

    assertEquals(a, a);

    a.add("foo", new com.economic.persistgson.JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("foo", new com.economic.persistgson.JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add("bar", new com.economic.persistgson.JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add("bar", JsonNull.INSTANCE);
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  public void testSize() {
    com.economic.persistgson.JsonObject o = new com.economic.persistgson.JsonObject();
    assertEquals(0, o.size());

    o.add("Hello", new com.economic.persistgson.JsonPrimitive(1));
    assertEquals(1, o.size());

    o.add("Hi", new com.economic.persistgson.JsonPrimitive(1));
    assertEquals(2, o.size());

    o.remove("Hello");
    assertEquals(1, o.size());
  }

  public void testDeepCopy() {
    com.economic.persistgson.JsonObject original = new com.economic.persistgson.JsonObject();
    com.economic.persistgson.JsonArray firstEntry = new JsonArray();
    original.add("key", firstEntry);

    JsonObject copy = original.deepCopy();
    firstEntry.add(new JsonPrimitive("z"));

    assertEquals(1, original.get("key").getAsJsonArray().size());
    assertEquals(0, copy.get("key").getAsJsonArray().size());
  }

  /**
   * From issue 941
   */
  /*
  public void testKeySet() {
    JsonObject a = new JsonObject();

    a.add("foo", new JsonArray());
    a.add("bar", new JsonObject());

    assertEquals(2, a.size());
    assertEquals(2, a.keySet().size());
    assertTrue(a.keySet().contains("foo"));
    assertTrue(a.keySet().contains("bar"));
  }*/
}
