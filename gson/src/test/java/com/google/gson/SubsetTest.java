package com.google.gson;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import org.junit.Test;

/** Tests that we can parse and create JSON using the {@link JsonElement} subset. */
public final class SubsetTest {
  @Test
  public void read() {
    JsonElement json = JsonParser.parseString("{\"a\":1,\"b\":[2.1, null]}");
    assertThat(json.isJsonObject()).isTrue();
    JsonObject jsonObject = json.getAsJsonObject();
    assertThat(jsonObject.get("a").getAsInt()).isEqualTo(1);
    assertThat(jsonObject.get("b").getAsJsonArray().asList())
        .isEqualTo(List.of(new JsonPrimitive(2.1), JsonNull.INSTANCE));
  }

  @Test
  public void write() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("a", 1);
    JsonArray array = new JsonArray();
    array.add(2.1);
    array.add(JsonNull.INSTANCE);
    jsonObject.add("b", array);
    assertThat(jsonObject.toString()).isEqualTo("{\"a\":1,\"b\":[2.1,null]}");
  }
}
