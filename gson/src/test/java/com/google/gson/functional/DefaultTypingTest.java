/*
 * Copyright (C) 2020 Google Inc.
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
package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.common.TestTypes.Nested;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.google.gson.common.TestTypes.BagOfPrimitives;


public class DefaultTypingTest extends TestCase  {
    private Gson gson;
    private ComplexTestType testOverThisClass;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gson = new GsonBuilder().enableDefaultTyping().create();
        this.testOverThisClass = prepareComplexType();
    }

    public void testDefaultTypingSerialization() {
        String result = gson.toJson(testOverThisClass);
        assertEquals(getJson(), result);
    }

    public void testDefaultTypingDeserialization() {
        ComplexTestType result = gson.fromJson(getJson(), ComplexTestType.class);
        assertEquals(this.testOverThisClass, result);
        //Explicit check type of some subtypes
        assertTrue(result.listWithSubTypes.get(0) instanceof SubTypeOfNested);
        assertTrue(result.listWithSubTypes.get(1) instanceof Nested);
        assertTrue(result.subTypeOfNested instanceof SubTypeOfNested);
        assertTrue(result.subTypeOfNestedWithGenericObject.object instanceof SubTypeOfNested);
    }

    private static class ComplexTestType {
        List<Nested> listWithSubTypes = new ArrayList<Nested>();
        SubTypeOfNestedWithGenericObject<Nested> subTypeOfNestedWithGenericObject;
        Nested nested;
        Nested subTypeOfNested;
        int primitive;
        BigDecimal bigDecimal;
        public ComplexTestType() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComplexTestType that = (ComplexTestType) o;

            if (primitive != that.primitive) return false;
            if (listWithSubTypes != null ? !listWithSubTypes.equals(that.listWithSubTypes) : that.listWithSubTypes != null)
                return false;
            if (subTypeOfNestedWithGenericObject != null ? !subTypeOfNestedWithGenericObject.equals(that.subTypeOfNestedWithGenericObject) : that.subTypeOfNestedWithGenericObject != null)
                return false;
            if (nested != null ? !nested.equals(that.nested) : that.nested != null) return false;
            if (subTypeOfNested != null ? !subTypeOfNested.equals(that.subTypeOfNested) : that.subTypeOfNested != null)
                return false;
            return bigDecimal != null ? bigDecimal.equals(that.bigDecimal) : that.bigDecimal == null;
        }

    }

    private ComplexTestType prepareComplexType() {
        ComplexTestType complexType = new ComplexTestType();
        complexType.bigDecimal = BigDecimal.valueOf(1.11);
        complexType.primitive = 3;
        BagOfPrimitives bagOfPrimitives1 = new BagOfPrimitives(1L, 1, true, "String1");
        BagOfPrimitives bagOfPrimitives2 = new BagOfPrimitives(2L, 2, true, "String2");
        BagOfPrimitives bagOfPrimitives3 = new BagOfPrimitives(3L, 3, true, "String3");
        BagOfPrimitives bagOfPrimitives4 = new BagOfPrimitives(4L, 4, true, "String4");
        complexType.listWithSubTypes.add(new SubTypeOfNested(bagOfPrimitives1, bagOfPrimitives2));
        complexType.listWithSubTypes.add(new Nested(bagOfPrimitives1, bagOfPrimitives2));
        complexType.nested = new Nested(bagOfPrimitives3, bagOfPrimitives4);
        complexType.subTypeOfNested = new SubTypeOfNested(bagOfPrimitives3, bagOfPrimitives4);
        complexType.subTypeOfNestedWithGenericObject = new SubTypeOfNestedWithGenericObject<Nested>(bagOfPrimitives1, bagOfPrimitives2, new SubTypeOfNested(bagOfPrimitives3, bagOfPrimitives4));
        return complexType;
    }


    private static class SubTypeOfNested extends Nested {
        private final long value = 5;

        public SubTypeOfNested(BagOfPrimitives primitive1, BagOfPrimitives primitive2) {
            super(primitive1, primitive2);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SubTypeOfNested)) return false;
            if (!super.equals(o)) return false;

            SubTypeOfNested that = (SubTypeOfNested) o;

            return value == that.value;
        }
    }

    private static class SubTypeOfNestedWithGenericObject<T> extends Nested {
        private T object;

        public SubTypeOfNestedWithGenericObject(BagOfPrimitives primitive1, BagOfPrimitives primitive2, T object) {
            super(primitive1, primitive2);
            this.object = object;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SubTypeOfNestedWithGenericObject)) return false;
            if (!super.equals(o)) return false;

            SubTypeOfNestedWithGenericObject<?> that = (SubTypeOfNestedWithGenericObject<?>) o;

            return object != null ? object.equals(that.object) : that.object == null;
        }
    }

    private String getJson() {
        return "{\"_type\":\"com.google.gson.functional.DefaultTypingTest$ComplexTestType\",\"_properties\":{\"listWithSubTypes\":[{\"_type\":\"com.google.gson.functional.DefaultTypingTest$SubTypeOfNested\",\"_properties\":{\"value\":5,\"primitive1\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":1,\"intValue\":1,\"booleanValue\":true,\"stringValue\":\"String1\"}},\"primitive2\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":2,\"intValue\":2,\"booleanValue\":true,\"stringValue\":\"String2\"}}}},{\"_type\":\"com.google.gson.common.TestTypes$Nested\",\"_properties\":{\"primitive1\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":1,\"intValue\":1,\"booleanValue\":true,\"stringValue\":\"String1\"}},\"primitive2\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":2,\"intValue\":2,\"booleanValue\":true,\"stringValue\":\"String2\"}}}}],\"subTypeOfNestedWithGenericObject\":{\"_type\":\"com.google.gson.functional.DefaultTypingTest$SubTypeOfNestedWithGenericObject\",\"_properties\":{\"object\":{\"_type\":\"com.google.gson.functional.DefaultTypingTest$SubTypeOfNested\",\"_properties\":{\"value\":5,\"primitive1\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":3,\"intValue\":3,\"booleanValue\":true,\"stringValue\":\"String3\"}},\"primitive2\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":4,\"intValue\":4,\"booleanValue\":true,\"stringValue\":\"String4\"}}}},\"primitive1\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":1,\"intValue\":1,\"booleanValue\":true,\"stringValue\":\"String1\"}},\"primitive2\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":2,\"intValue\":2,\"booleanValue\":true,\"stringValue\":\"String2\"}}}},\"nested\":{\"_type\":\"com.google.gson.common.TestTypes$Nested\",\"_properties\":{\"primitive1\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":3,\"intValue\":3,\"booleanValue\":true,\"stringValue\":\"String3\"}},\"primitive2\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":4,\"intValue\":4,\"booleanValue\":true,\"stringValue\":\"String4\"}}}},\"subTypeOfNested\":{\"_type\":\"com.google.gson.functional.DefaultTypingTest$SubTypeOfNested\",\"_properties\":{\"value\":5,\"primitive1\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":3,\"intValue\":3,\"booleanValue\":true,\"stringValue\":\"String3\"}},\"primitive2\":{\"_type\":\"com.google.gson.common.TestTypes$BagOfPrimitives\",\"_properties\":{\"longValue\":4,\"intValue\":4,\"booleanValue\":true,\"stringValue\":\"String4\"}}}},\"primitive\":3,\"bigDecimal\":1.11}}";
    }
}
