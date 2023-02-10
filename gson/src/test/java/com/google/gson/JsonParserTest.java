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
import static org.junit.Assert.fail;

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
    try {
      JsonParser.parseString("[[]");
      fail();
    } catch (JsonSyntaxException expected) { }
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
    String unquotedSentence = "Test is a test..blah blah";
    try {
      JsonParser.parseString(unquotedSentence);
      fail();
    } catch (JsonSyntaxException expected) { }
  }

  @Test
  public void testParseMixedArray() {
    String json = "[{},13,\"stringValue\"]";
    JsonElement e = JsonParser.parseString(json);
    assertThat(e.isJsonArray()).isTrue();

    JsonArray  array = e.getAsJsonArray();
    assertThat(array.get(0).toString()).isEqualTo("{}");
    assertThat(array.get(1).getAsInt()).isEqualTo(13);
    assertThat(array.get(2).getAsString()).isEqualTo("stringValue");
  }

  private static String repeat(String s, int times) {
    StringBuilder stringBuilder = new StringBuilder(s.length() * times);
    for (int i = 0; i < times; i++) {
      stringBuilder.append(s);
    }
    return stringBuilder.toString();
  }

  /** Deeply nested JSON arrays should not cause {@link StackOverflowError} */
  @Test
  public void testParseDeeplyNestedArrays() throws IOException {
    int times = 10000;
    // [[[ ... ]]]
    String json = repeat("[", times) + repeat("]", times);

    int actualTimes = 0;
    JsonArray current = JsonParser.parseString(json).getAsJsonArray();
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
    String json = repeat("{\"a\":", times) + "null" + repeat("}", times);

    int actualTimes = 0;
    JsonObject current = JsonParser.parseString(json).getAsJsonObject();
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
    parser.setLenient(true);
    JsonElement element1 = Streams.parse(parser);
    JsonElement element2 = Streams.parse(parser);
    BagOfPrimitives actualOne = gson.fromJson(element1, BagOfPrimitives.class);
    assertThat(actualOne.stringValue).isEqualTo("one");
    BagOfPrimitives actualTwo = gson.fromJson(element2, BagOfPrimitives.class);
    assertThat(actualTwo.stringValue).isEqualTo("two");
  }
}
