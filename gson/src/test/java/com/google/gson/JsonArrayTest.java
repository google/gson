/*
 * Copyright (C) 2011 Google Inc.
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

import junit.framework.TestCase;

import com.google.gson.common.MoreAsserts;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Jesse Wilson
 */
public final class JsonArrayTest extends TestCase {

    public void testEqualsOnEmptyArray() {
        MoreAsserts.assertEqualsAndHashCode(new JsonArray(), new JsonArray());
    }

    public void testEqualsNonEmptyArray() {
        JsonArray a = new JsonArray();
        JsonArray b = new JsonArray();

        assertEquals(a, a);

        a.add(new JsonObject());
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));

        b.add(new JsonObject());
        MoreAsserts.assertEqualsAndHashCode(a, b);

        a.add(new JsonObject());
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));

        b.add(JsonNull.INSTANCE);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    public void testRemove() {
        JsonArray array = new JsonArray();
        try {
            array.remove(0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        JsonPrimitive a = new JsonPrimitive("a");
        array.add(a);
        assertTrue(array.remove(a));
        assertFalse(array.contains(a));
        array.add(a);
        array.add(new JsonPrimitive("b"));
        assertEquals("b", array.remove(1).getAsString());
        assertEquals(1, array.size());
        assertTrue(array.contains(a));
    }

    public void testSet() {
        JsonArray array = new JsonArray();
        try {
            array.set(0, new JsonPrimitive(1));
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        JsonPrimitive a = new JsonPrimitive("a");
        array.add(a);
        array.set(0, new JsonPrimitive("b"));
        assertEquals("b", array.get(0).getAsString());
        array.set(0, null);
        assertNull(array.get(0));
        array.set(0, new JsonPrimitive("c"));
        assertEquals("c", array.get(0).getAsString());
        assertEquals(1, array.size());
    }

    public void testDeepCopy() {
        JsonArray original = new JsonArray();
        JsonArray firstEntry = new JsonArray();
        original.add(firstEntry);

        JsonArray copy = original.deepCopy();
        original.add(new JsonPrimitive("y"));

        assertEquals(1, copy.size());
        firstEntry.add(new JsonPrimitive("z"));

        assertEquals(1, original.get(0).getAsJsonArray().size());
        assertEquals(0, copy.get(0).getAsJsonArray().size());
    }

    public void testAddNullElement() {
        JsonArray jsonArray = new JsonArray();
        JsonElement element = null;
        jsonArray.add(element);
        assertEquals(JsonNull.INSTANCE, jsonArray.get(0));
    }

    public void testAddAll() {
        //set up Arrays
        JsonArray jsonArrayToAdd = new JsonArray();
        JsonArray jsonArray = new JsonArray();

        //initialize variables to add
        Number number = 1;
        String string = "test";
        double d = 10.10;
        BigDecimal bigDecimal = new BigDecimal("215.87");
        BigInteger bigInteger = new BigInteger("215");
        float f = 10.4f;
        long l = 12345678910L;
        int i = 10;
        byte b = 10;
        char c = 'a';
        short s = 10;
        boolean bool = true;

        //add variables to Array
        jsonArrayToAdd.add(number);
        jsonArrayToAdd.add(string);
        jsonArrayToAdd.add(d);
        jsonArrayToAdd.add(bigDecimal);
        jsonArrayToAdd.add(bigInteger);
        jsonArrayToAdd.add(f);
        jsonArrayToAdd.add(l);
        jsonArrayToAdd.add(i);
        jsonArrayToAdd.add(b);
        jsonArrayToAdd.add(c);
        jsonArrayToAdd.add(s);
        jsonArrayToAdd.add(bool);

        //execute method
        jsonArray.addAll(jsonArrayToAdd);

        //check
        assertEquals(jsonArrayToAdd, jsonArray);
    }

    public void testGetSingleElementAsNumber() {
        JsonArray singleElement = new JsonArray();
        Number number = 1;
        singleElement.add(number);
        assertEquals(number, singleElement.getAsNumber());
    }

    public void testGetElementsAsNumberException() {
        JsonArray singleElement = new JsonArray();
        Number number = 1;
        singleElement.add(number);
        singleElement.add(number);
        try {
            singleElement.getAsNumber();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsString() {
        JsonArray singleElement = new JsonArray();
        String string = "test";
        singleElement.add(string);
        assertEquals(string, singleElement.getAsString());
    }

    public void testGetElementsAsStringException() {
        JsonArray singleElement = new JsonArray();
        String string = "test";
        singleElement.add(string);
        singleElement.add(string);
        try {
            singleElement.getAsString();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsDouble() {
        JsonArray singleElement = new JsonArray();
        double d = 10.10;
        singleElement.add(d);
        assertEquals(d, singleElement.getAsDouble());
    }

    public void testGetElementsAsDoubleException() {
        JsonArray singleElement = new JsonArray();
        double d = 10.10;
        singleElement.add(d);
        singleElement.add(d);
        try {
            singleElement.getAsDouble();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsBigDecimal() {
        JsonArray singleElement = new JsonArray();
        BigDecimal bigDecimal = new BigDecimal("215.87");
        singleElement.add(bigDecimal);
        assertEquals(bigDecimal, singleElement.getAsBigDecimal());
    }

    public void testGetElementsAsBigDecimalException() {
        JsonArray singleElement = new JsonArray();
        BigDecimal bigDecimal = new BigDecimal("215.87");
        singleElement.add(bigDecimal);
        singleElement.add(bigDecimal);
        try {
            singleElement.getAsBigDecimal();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsBigInteger() {
        JsonArray singleElement = new JsonArray();
        BigInteger bigInteger = new BigInteger("215");
        singleElement.add(bigInteger);
        assertEquals(bigInteger, singleElement.getAsBigInteger());
    }

    public void testGetElementsAsBigIntegerException() {
        JsonArray singleElement = new JsonArray();
        BigInteger bigInteger = new BigInteger("215");
        singleElement.add(bigInteger);
        singleElement.add(bigInteger);
        try {
            singleElement.getAsBigInteger();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsFloat() {
        JsonArray singleElement = new JsonArray();
        float f = 10.4f;
        singleElement.add(f);
        assertEquals(f, singleElement.getAsFloat());
    }

    public void testGetElementsAsFloatException() {
        JsonArray singleElement = new JsonArray();
        float f = 10.4f;
        singleElement.add(f);
        singleElement.add(f);
        try {
            singleElement.getAsFloat();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsLong() {
        JsonArray singleElement = new JsonArray();
        long l = 12345678910L;
        singleElement.add(l);
        assertEquals(l, singleElement.getAsLong());
    }

    public void testGetElementsAsLongException() {
        JsonArray singleElement = new JsonArray();
        long l = 12345678910L;
        singleElement.add(l);
        singleElement.add(l);
        try {
            singleElement.getAsLong();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsInt() {
        JsonArray singleElement = new JsonArray();
        int i = 10;
        singleElement.add(i);
        assertEquals(i, singleElement.getAsInt());
    }

    public void testGetElementsAsIntException() {
        JsonArray singleElement = new JsonArray();
        int i = 10;
        singleElement.add(i);
        singleElement.add(i);
        try {
            singleElement.getAsInt();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsByte() {
        JsonArray singleElement = new JsonArray();
        byte b = 10;
        singleElement.add(b);
        assertEquals(b, singleElement.getAsByte());
    }

    public void testGetElementsAsByteException() {
        JsonArray singleElement = new JsonArray();
        byte b = 10;
        singleElement.add(b);
        singleElement.add(b);
        try {
            singleElement.getAsByte();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsCharacter() {
        JsonArray singleElement = new JsonArray();
        char c = 'a';
        singleElement.add(c);
        assertEquals(c, singleElement.getAsCharacter());
    }

    public void testGetElementsAsCharacterException() {
        JsonArray singleElement = new JsonArray();
        char c = 'a';
        singleElement.add(c);
        singleElement.add(c);
        try {
            singleElement.getAsCharacter();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsShort() {
        JsonArray singleElement = new JsonArray();
        short s = 10;
        singleElement.add(s);
        assertEquals(s, singleElement.getAsShort());
    }

    public void testGetElementsAsShortException() {
        JsonArray singleElement = new JsonArray();
        short s = 10;
        singleElement.add(s);
        singleElement.add(s);
        try {
            singleElement.getAsShort();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

    public void testGetSingleElementAsBoolean() {
        JsonArray singleElement = new JsonArray();
        boolean bool = true;
        singleElement.add(bool);
        assertEquals(bool, singleElement.getAsBoolean());
    }

    public void testGetElementsAsBooleanException() {
        JsonArray singleElement = new JsonArray();
        boolean bool = true;
        singleElement.add(bool);
        singleElement.add(bool);
        try {
            singleElement.getAsBoolean();
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException expectedException) {
        }
    }

}
