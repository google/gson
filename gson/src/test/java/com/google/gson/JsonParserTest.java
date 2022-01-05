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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link JsonParser}
 *
 * @author Inderjeet Singh
 */
class JsonParserTest {

  @Test
  void testParseInvalidJson() {
    try {
      JsonParser.parseString("[[]");
      fail();
    } catch (JsonSyntaxException expected) { }
  }

  @Test
  void testParseUnquotedStringArrayFails() {
    JsonElement element = JsonParser.parseString("[a,b,c]");
    assertEquals("a", element.getAsJsonArray().get(0).getAsString());
    assertEquals("b", element.getAsJsonArray().get(1).getAsString());
    assertEquals("c", element.getAsJsonArray().get(2).getAsString());
    assertEquals(3, element.getAsJsonArray().size());
  }

  @Test
  void testParseString() {
    String json = "{a:10,b:'c'}";
    JsonElement e = JsonParser.parseString(json);
    assertTrue(e.isJsonObject());
    assertEquals(10, e.getAsJsonObject().get("a").getAsInt());
    assertEquals("c", e.getAsJsonObject().get("b").getAsString());
  }

  @Test
  void testParseEmptyString() {
    JsonElement e = JsonParser.parseString("\"   \"");
    assertTrue(e.isJsonPrimitive());
    assertEquals("   ", e.getAsString());
  }

  @Test
  void testParseEmptyWhitespaceInput() {
    JsonElement e = JsonParser.parseString("     ");
    assertTrue(e.isJsonNull());
  }

  @Test
  void testParseUnquotedSingleWordStringFails() {
    assertEquals("Test", JsonParser.parseString("Test").getAsString());
  }

  @Test
  void testParseUnquotedMultiWordStringFails() {
    String unquotedSentence = "Test is a test..blah blah";
    try {
      JsonParser.parseString(unquotedSentence);
      fail();
    } catch (JsonSyntaxException expected) { }
  }

  @Test
  void testParseMixedArray() {
    String json = "[{},13,\"stringValue\"]";
    JsonElement e = JsonParser.parseString(json);
    assertTrue(e.isJsonArray());

    JsonArray  array = e.getAsJsonArray();
    assertEquals("{}", array.get(0).toString());
    assertEquals(13, array.get(1).getAsInt());
    assertEquals("stringValue", array.get(2).getAsString());
  }

  @Test
  void testParseReader() {
    StringReader reader = new StringReader("{a:10,b:'c'}");
    JsonElement e = JsonParser.parseReader(reader);
    assertTrue(e.isJsonObject());
    assertEquals(10, e.getAsJsonObject().get("a").getAsInt());
    assertEquals("c", e.getAsJsonObject().get("b").getAsString());
  }

  @Test
  void testReadWriteTwoObjects() throws Exception {
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
    assertEquals("one", actualOne.stringValue);
    BagOfPrimitives actualTwo = gson.fromJson(element2, BagOfPrimitives.class);
    assertEquals("two", actualTwo.stringValue);
  }
}
