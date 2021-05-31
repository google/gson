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
import junit.framework.TestCase;

/**
 * Unit test for the {@link JsonObject} class.
 *
 * @author Joel Leitch
 */
public class JsonObjectTest extends TestCase {

    public void testAddingAndRemovingObjectProperties() {
        JsonObject jsonObj = new JsonObject();
        String propertyName = "property";
        assertFalse("jsonObj has property", jsonObj.has(propertyName));
        assertNull("jsonObj property is not null", jsonObj.get(propertyName));

        JsonPrimitive value = new JsonPrimitive("blah");
        jsonObj.add(propertyName, value);
        assertEquals("retrieved jsonObj value is not equals to value", value, jsonObj.get(propertyName));

        JsonElement removedElement = jsonObj.remove(propertyName);
        assertEquals("removedElement is not equals to value", value, removedElement);
        assertFalse("jsonObj has property after remove", jsonObj.has(propertyName));
        assertNull("jsonObj property is not null after remove", jsonObj.get(propertyName));
    }

    public void testGettingAsJsonPrimitive() {
        //setup
        JsonObject jsonObj = new JsonObject();
        String propertyName = "property";
        JsonPrimitive value = new JsonPrimitive("blah");
        jsonObj.add(propertyName, value);
        //test method
        JsonPrimitive target = jsonObj.getAsJsonPrimitive(propertyName);
        //compare
        Class expectedClass = JsonPrimitive.class;
        Class targetClass = target.getClass();

        assertEquals(expectedClass, targetClass);
    }

    public void testAddingJsonNullProperty() {
        JsonObject jsonObj = new JsonObject();
        String propertyName = "property";
        JsonPrimitive value = null;
        jsonObj.add(propertyName, value);

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertTrue(jsonElement.isJsonNull());
    }

    public void testAddingStringNullPropertyValue() {
        JsonObject jsonObj = new JsonObject();
        String propertyName = "property";
        String value = null;
        jsonObj.addProperty(propertyName, value);

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertNotNull("jsonElement is null", jsonElement);
        assertTrue("jsonElement is not JsonNull", jsonElement.isJsonNull());
    }

    public void testAddingNumberNullPropertyValue() {
        JsonObject jsonObj = new JsonObject();
        String propertyName = "property";
        Number value = null;
        jsonObj.addProperty(propertyName, value);

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertNotNull("jsonElement is null", jsonElement);
        assertTrue("jsonElement is not JsonNull", jsonElement.isJsonNull());
    }

    public void testAddingBooleanNullPropertyValue() {
        JsonObject jsonObj = new JsonObject();
        String propertyName = "property";
        Boolean value = null;
        jsonObj.addProperty(propertyName, value);

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertNotNull("jsonElement is null", jsonElement);
        assertTrue("jsonElement is not JsonNull", jsonElement.isJsonNull());
    }

    public void testAddingCharacterNullPropertyValue() {
        JsonObject jsonObj = new JsonObject();
        String propertyName = "property";
        Character value = null;
        jsonObj.addProperty(propertyName, value);

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertNotNull("jsonElement is null", jsonElement);
        assertTrue("jsonElement is not JsonNull", jsonElement.isJsonNull());
    }

    public void testAddingNullOrEmptyPropertyName() {
        JsonObject jsonObj = new JsonObject();
        try {
            jsonObj.add(null, JsonNull.INSTANCE);
            fail("Should not allow null property names.");
        } catch (NullPointerException expected) {
        }

        jsonObj.add("", JsonNull.INSTANCE);
        jsonObj.add("   \t", JsonNull.INSTANCE);
    }

    public void testAddingBooleanProperties() {
        String propertyName = "property";
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty(propertyName, true);

        assertTrue("jsonObj does not have property", jsonObj.has(propertyName));

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertNotNull("jsonElement is null", jsonElement);
        assertTrue("jsonElement is not 'true'", jsonElement.getAsBoolean());
    }

    public void testAddingStringProperties() {
        String propertyName = "property";
        String value = "blah";

        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty(propertyName, value);

        assertTrue("jsonObj does not have property", jsonObj.has(propertyName));

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertNotNull("jsonElement is null", jsonElement);
        assertEquals("jsonElement does not equal value", value, jsonElement.getAsString());
    }

    public void testAddingNumberProperties() {
        String propertyName = "property";
        Number value = 5;

        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty(propertyName, value);

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertNotNull("jsonElement is null", jsonElement);
        assertEquals("jsonElement does not equal value", value, jsonElement.getAsInt());
    }

    public void testAddingCharacterProperties() {
        String propertyName = "property";
        char value = 'a';

        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty(propertyName, value);

        assertTrue("jsonObj does not have property", jsonObj.has(propertyName));

        JsonElement jsonElement = jsonObj.get(propertyName);
        assertNotNull("jsonElement is null", jsonElement);
        assertEquals("jsonElement does not equal String value", String.valueOf(value), jsonElement.getAsString());
        assertEquals("jsonElement does not equal Character value", value, jsonElement.getAsCharacter());
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
        JsonObject jsonObj = new JsonParser().parse("{\"\":true}").getAsJsonObject();
        assertEquals(true, jsonObj.get("").getAsBoolean());
    }

    public void testEqualsOnEmptyObject() {
        MoreAsserts.assertEqualsAndHashCode(new JsonObject(), new JsonObject());
    }

    public void testEqualsNonEmptyObject() {
        JsonObject a = new JsonObject();
        JsonObject b = new JsonObject();

        assertEquals("Equals method did not return JsonObject a itself", a, a);

        a.add("foo", new JsonObject());
        assertFalse("a.add(foo,new) equals b", a.equals(b));
        assertFalse("b equals a.add(foo,new)", b.equals(a));

        b.add("foo", new JsonObject());
        MoreAsserts.assertEqualsAndHashCode(a, b);

        a.add("bar", new JsonObject());
        assertFalse("a.add(bar,new) equals b", a.equals(b));
        assertFalse("b equals a.add(bar,new)", b.equals(a));

        b.add("bar", JsonNull.INSTANCE);
        assertFalse("a equals b.add(bar,JsonNull)", a.equals(b));
        assertFalse("b.add(bar,JsonNull) equals a", b.equals(a));
    }

    public void testSize() {
        int emptyObject = 0;
        int singleElement = 1;
        int twoElements = 2;
        JsonObject o = new JsonObject();

        assertEquals("JsonObject is not empty", emptyObject, o.size());

        o.add("Hello", new JsonPrimitive(1));
        assertEquals("JsonObject does not contain a single element", singleElement, o.size());

        o.add("Hi", new JsonPrimitive(1));
        assertEquals("JsonObject does not contain two elements", twoElements, o.size());

        o.remove("Hello");
        assertEquals("JsonObject does not contain a single element after remove", singleElement, o.size());
    }

    public void testDeepCopy() {
        JsonObject original = new JsonObject();
        JsonArray firstEntry = new JsonArray();
        original.add("key", firstEntry);

        JsonObject copy = original.deepCopy();
        firstEntry.add(new JsonPrimitive("z"));

        assertEquals("Size of JsonObject original not 1", 1, original.get("key").getAsJsonArray().size());
        assertEquals("Size of JsonObject copy not 0", 0, copy.get("key").getAsJsonArray().size());
    }

    /**
     * From issue 941
     */
    public void testKeySet() {
        JsonObject a = new JsonObject();

        a.add("foo", new JsonArray());
        a.add("bar", new JsonObject());

        assertEquals("Size of JsonObject a not 2", 2, a.size());
        assertEquals("Size of JsonObject a keySet not 2", 2, a.keySet().size());
        assertTrue("JsonObject a keySet does not contain foo", a.keySet().contains("foo"));
        assertTrue("JsonObject a keySet does not contain bar", a.keySet().contains("bar"));
    }
}
