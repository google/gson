/*
 * Copyright (C) 2009 Google Inc.
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
import static org.junit.Assert.assertThrows;

import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

/**
 * Unit test for {@link JsonParser}
 *
 * @author Inderjeet Singh
 */
public class JsonParserTest {

  @Test
  public void testParseInvalidJson() {
    assertThrows(JsonSyntaxException.class, () -> JsonParser.parseString("[[]"));
  }

  @Test
  public void testParseUnquotedStringArrayFails() {
    JsonElement element = JsonParser.parseString("[a,b,c]");
    assertThat(element.getAsJsonArray().get(0).getAsString()).isEqualTo("a");
    assertThat(element.getAsJsonArray().get(1).getAsString()).isEqualTo("b");
    assertThat(element.getAsJsonArray().get(2).getAsString()).isEqualTo("c");
    assertThat(element.getAsJsonArray()).hasSize(3);
  }

  @Test
  public void testParseString() {
    String json = "{a:10,b:'c'}";
    JsonElement e = JsonParser.parseString(json);
    assertThat(e.isJsonObject()).isTrue();
    assertThat(e.getAsJsonObject().get("a").getAsInt()).isEqualTo(10);
    assertThat(e.getAsJsonObject().get("b").getAsString()).isEqualTo("c");
  }

  @Test
  public void testParseEmptyString() {
    JsonElement e = JsonParser.parseString("\"   \"");
    assertThat(e.isJsonPrimitive()).isTrue();
    assertThat(e.getAsString()).isEqualTo("   ");
  }

  @Test
  public void testParseEmptyWhitespaceInput() {
    JsonElement e = JsonParser.parseString("     ");
    assertThat(e.isJsonNull()).isTrue();
  }

  @Test
  public void testParseUnquotedSingleWordStringFails() {
    assertThat(JsonParser.parseString("Test").getAsString()).isEqualTo("Test");
  }

  @Test
  public void testParseUnquotedMultiWordStringFails() {
    assertThrows(
        JsonSyntaxException.class, () -> JsonParser.parseString("Test is a test..blah blah"));
  }

  @Test
  public void testParseMixedArray() {
    String json = "[{},13,\"stringValue\"]";
    JsonElement e = JsonParser.parseString(json);
    assertThat(e.isJsonArray()).isTrue();

    JsonArray array = e.getAsJsonArray();
    assertThat(array.get(0).toString()).isEqualTo("{}");
    assertThat(array.get(1).getAsInt()).isEqualTo(13);
    assertThat(array.get(2).getAsString()).isEqualTo("stringValue");
  }

  /** Deeply nested JSON arrays should not cause {@link StackOverflowError} */
  @Test
  public void testParseDeeplyNestedArrays() throws IOException {
    int times = 10000;
    // [[[ ... ]]]
    String json = "[".repeat(times) + "]".repeat(times);
    JsonReader jsonReader = new JsonReader(new StringReader(json));
    jsonReader.setNestingLimit(Integer.MAX_VALUE);

    int actualTimes = 0;
    JsonArray current = JsonParser.parseReader(jsonReader).getAsJsonArray();
    while (true) {
      actualTimes++;
      if (current.isEmpty()) {
        break;
      }
      assertThat(current.size()).isEqualTo(1);
      current = current.get(0).getAsJsonArray();
    }
    assertThat(actualTimes).isEqualTo(times);
  }

  /** Deeply nested JSON objects should not cause {@link StackOverflowError} */
  @Test
  public void testParseDeeplyNestedObjects() throws IOException {
    int times = 10000;
    // {"a":{"a": ... {"a":null} ... }}
    String json = "{\"a\":".repeat(times) + "null" + "}".repeat(times);
    JsonReader jsonReader = new JsonReader(new StringReader(json));
    jsonReader.setNestingLimit(Integer.MAX_VALUE);

    int actualTimes = 0;
    JsonObject current = JsonParser.parseReader(jsonReader).getAsJsonObject();
    while (true) {
      assertThat(current.size()).isEqualTo(1);
      actualTimes++;
      JsonElement next = current.get("a");
      if (next.isJsonNull()) {
        break;
      } else {
        current = next.getAsJsonObject();
      }
    }
    assertThat(actualTimes).isEqualTo(times);
  }

  @Test
  public void testParseReader() {
    StringReader reader = new StringReader("{a:10,b:'c'}");
    JsonElement e = JsonParser.parseReader(reader);
    assertThat(e.isJsonObject()).isTrue();
    assertThat(e.getAsJsonObject().get("a").getAsInt()).isEqualTo(10);
    assertThat(e.getAsJsonObject().get("b").getAsString()).isEqualTo("c");
  }

  @Test
  public void testReadWriteTwoObjects() throws Exception {
    Gson gson = new Gson();
    CharArrayWriter writer = new CharArrayWriter();
    BagOfPrimitives expectedOne = new BagOfPrimitives(1, 1, true, "one");
    writer.write(gson.toJson(expectedOne).toCharArray());
    BagOfPrimitives expectedTwo = new BagOfPrimitives(2, 2, false, "two");
    writer.write(gson.toJson(expectedTwo).toCharArray());
    CharArrayReader reader = new CharArrayReader(writer.toCharArray());

    JsonReader parser = new JsonReader(reader);
    parser.setStrictness(Strictness.LENIENT);
    JsonElement element1 = Streams.parse(parser);
    JsonElement element2 = Streams.parse(parser);
    BagOfPrimitives actualOne = gson.fromJson(element1, BagOfPrimitives.class);
    assertThat(actualOne.stringValue).isEqualTo("one");
    BagOfPrimitives actualTwo = gson.fromJson(element2, BagOfPrimitives.class);
    assertThat(actualTwo.stringValue).isEqualTo("two");
  }

  @Test
  public void testLegacyStrict() {
    JsonReader reader = new JsonReader(new StringReader("unquoted"));
    Strictness strictness = Strictness.LEGACY_STRICT;
    // LEGACY_STRICT is ignored by JsonParser later; parses in lenient mode instead
    reader.setStrictness(strictness);

    assertThat(JsonParser.parseReader(reader)).isEqualTo(new JsonPrimitive("unquoted"));
    // Original strictness was restored
    assertThat(reader.getStrictness()).isEqualTo(strictness);
  }

  @Test
  public void testStrict() {
    JsonReader reader = new JsonReader(new StringReader("faLsE"));
    Strictness strictness = Strictness.STRICT;
    reader.setStrictness(strictness);

    var e = assertThrows(JsonSyntaxException.class, () -> JsonParser.parseReader(reader));
    assertThat(e)
        .hasCauseThat()
        .hasMessageThat()
        .startsWith("Use JsonReader.setStrictness(Strictness.LENIENT) to accept malformed JSON");
    // Original strictness was kept
    assertThat(reader.getStrictness()).isEqualTo(strictness);
  }
}
