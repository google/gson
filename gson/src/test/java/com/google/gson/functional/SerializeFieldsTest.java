
package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Serialize;
import com.google.gson.annotations.Serialize.Inclusion;

import junit.framework.TestCase;


public class SerializeFieldsTest extends TestCase {

    private static void assertIncludes(final String json, final String field, final Object value) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();

        assertTrue(obj.has(field));
        JsonElement element = obj.get(field);

        if (value == null) {
            assertTrue(element.isJsonNull());
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            assertEquals(String.valueOf(value), primitive.getAsString());
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            assertNotNull(object);
            assertEquals(value, object.toString());
        }
    }

    private static void assertExcludes(final String json, final String field) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();

        assertFalse(obj.has(field));
    }

    public void testSerializeDefaultExclusion() {
        final Gson gson = new GsonBuilder().create();
        final TestObject obj = new TestObject();

        final String json = gson.toJson(obj);

        assertExcludes(json, "empty_1");
        assertExcludes(json, "empty_2");
        assertIncludes(json, "empty_3", null);
        assertExcludes(json, "empty_4");
        assertIncludes(json, "value_1", "value-1");
        assertIncludes(json, "value_2", 2L);
        assertIncludes(json, "value_3", 3);
        assertIncludes(json, "value_4", "{\"value\":1}");
    }

    public void testSerializeNonNullExclusion() {
        final Gson gson = new GsonBuilder().serializeNulls().create();
        final TestObject obj = new TestObject();

        final String json = gson.toJson(obj);

        assertIncludes(json, "empty_1", null);
        assertIncludes(json, "empty_2", null);
        assertIncludes(json, "empty_3", null);
        assertExcludes(json, "empty_4");
        assertIncludes(json, "value_1", "value-1");
        assertIncludes(json, "value_2", 2L);
        assertIncludes(json, "value_3", 3);
        assertIncludes(json, "value_4", "{\"value\":1}");
    }

    public void testDeserializeDefaultExclusion() {
        final Gson gson = new GsonBuilder().create();
        final TestObject source = new TestObject();

        final String json = gson.toJson(source);
        final TestObject target = gson.fromJson(json, TestObject.class);

        assertEquals(null, target.empty_1);
        assertEquals(null, target.empty_2);
        assertEquals(null, target.empty_3);
        assertEquals(null, target.empty_4);
        assertEquals("value-1", target.value_1);
        assertEquals(2L, target.value_2);
        assertEquals(3, target.value_3);
        assertEquals(NestedObject.class, target.value_4.getClass());
    }

    public void testDeserializeNonNullExclusion() {
        final Gson gson = new GsonBuilder().serializeNulls().create();
        final TestObject source = new TestObject();

        final String json = gson.toJson(source);
        final TestObject target = gson.fromJson(json, TestObject.class);

        assertEquals(null, target.empty_1);
        assertEquals(null, target.empty_2);
        assertEquals(null, target.empty_3);
        assertEquals(null, target.empty_4);
        assertEquals("value-1", target.value_1);
        assertEquals(2L, target.value_2);
        assertEquals(3, target.value_3);
        assertNotNull(target.value_4);
        assertEquals(NestedObject.class, target.value_4.getClass());
    }

    public static class TestObject {

        public final String       empty_1 = null;

        @Serialize(Inclusion.DEFAULT)
        public final Long         empty_2 = null;

        @Serialize(Inclusion.ALWAYS)
        public final Integer      empty_3 = null;

        @Serialize(Inclusion.NON_NULL)
        public final Object       empty_4 = null;

        public final String       value_1 = "value-1";

        @Serialize(Inclusion.DEFAULT)
        public final long         value_2 = 2L;

        @Serialize(Inclusion.ALWAYS)
        public final int          value_3 = 3;

        @Serialize(Inclusion.NON_NULL)
        public final NestedObject value_4 = new NestedObject();
    }

    public static class NestedObject {

        public final int value = 1;
    }
}
