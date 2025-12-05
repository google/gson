package com.google.gson;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class JsonElementTest {
    @Test
    public void testParseNull() {
        assertThat(JsonElement.parse(null)).isEqualTo(JsonNull.INSTANCE);
    }

    @Test
    public void testParseJsonElement() {
        JsonElement element = new JsonObject();
        assertThat(JsonElement.parse(element)).isSameInstanceAs(element);
    }

    @Test
    public void testParseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        JsonElement element = JsonElement.parse(map);
        assertThat(element.isJsonObject()).isTrue();
        assertThat(element.getAsJsonObject().get("key").getAsString()).isEqualTo("value");
    }

    @Test
    public void testParseIterable() {
        JsonElement element = JsonElement.parse(Arrays.asList("a", "b"));
        assertThat(element.isJsonArray()).isTrue();
        assertThat(element.getAsJsonArray().get(0).getAsString()).isEqualTo("a");
    }

    @Test
    public void testParsePrimitive() {
        assertThat(JsonElement.parse("string").getAsString()).isEqualTo("string");
        assertThat(JsonElement.parse(1).getAsInt()).isEqualTo(1);
        assertThat(JsonElement.parse(true).getAsBoolean()).isTrue();
        assertThat(JsonElement.parse('c').getAsString()).isEqualTo("c");
    }

    @Test
    public void testParseUnsupported() {
        assertThrows(IllegalArgumentException.class, () -> JsonElement.parse(new Object()));
    }
}
