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

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.StringReader;
import java.math.BigInteger;

import junit.framework.TestCase;

import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

/**
 * Unit test for {@link JsonParser}
 *
 * @author Inderjeet Singh
 */
public class JsonParserTest extends TestCase {

  public void testParseInvalidJson() {
    try {
      JsonParser.parseString("[[]");
      fail();
    } catch (JsonSyntaxException expected) { }
  }

  public void testParseUnquotedStringArrayFails() {
    JsonElement element = JsonParser.parseString("[a,b,c]");
    assertEquals("a", element.getAsJsonArray().get(0).getAsString());
    assertEquals("b", element.getAsJsonArray().get(1).getAsString());
    assertEquals("c", element.getAsJsonArray().get(2).getAsString());
    assertEquals(3, element.getAsJsonArray().size());
  }

  public void testParseString() {
    String json = "{a:10,b:'c'}";
    JsonElement e = JsonParser.parseString(json);
    assertTrue(e.isJsonObject());
    assertEquals(10, e.getAsJsonObject().get("a").getAsInt());
    assertEquals("c", e.getAsJsonObject().get("b").getAsString());
  }

  public void testParseEmptyString() {
    JsonElement e = JsonParser.parseString("\"   \"");
    assertTrue(e.isJsonPrimitive());
    assertEquals("   ", e.getAsString());
  }

  public void testParseEmptyWhitespaceInput() {
    JsonElement e = JsonParser.parseString("     ");
    assertTrue(e.isJsonNull());
  }

  public void testParseUnquotedSingleWordStringFails() {
    assertEquals("Test", JsonParser.parseString("Test").getAsString());
  }

  public void testParseUnquotedMultiWordStringFails() {
    String unquotedSentence = "Test is a test..blah blah";
    try {
      JsonParser.parseString(unquotedSentence);
      fail();
    } catch (JsonSyntaxException expected) { }
  }

  public void testParseMixedArray() {
    String json = "[{},13,\"stringValue\"]";
    JsonElement e = JsonParser.parseString(json);
    assertTrue(e.isJsonArray());

    JsonArray  array = e.getAsJsonArray();
    assertEquals("{}", array.get(0).toString());
    assertEquals(13, array.get(1).getAsInt());
    assertEquals("stringValue", array.get(2).getAsString());
  }

  public void testParseReader() {
    StringReader reader = new StringReader("{a:10,b:'c'}");
    JsonElement e = JsonParser.parseReader(reader);
    assertTrue(e.isJsonObject());
    assertEquals(10, e.getAsJsonObject().get("a").getAsInt());
    assertEquals("c", e.getAsJsonObject().get("b").getAsString());
  }

  public void testParseLongToInt() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("foo", 10000000000L);

    try {
      jsonObject.get("foo").getAsInt();
      fail();
    } catch (NumberFormatException expected) {
    }
  }

  public void testParseIntegerTypes() {

    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("bigIntegerU", BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(200L)) );
    jsonObject.addProperty("bigIntegerL", BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.valueOf(200L)) );
    jsonObject.addProperty("longU", Integer.MAX_VALUE+143L);
    jsonObject.addProperty("longL", Integer.MIN_VALUE-143L);
    jsonObject.addProperty("integerU", Short.MAX_VALUE+32);
    jsonObject.addProperty("integerL", Short.MIN_VALUE-32);
    jsonObject.addProperty("shortU", Byte.MAX_VALUE+17);
    jsonObject.addProperty("shortL", Byte.MIN_VALUE-17);

    try {
      jsonObject.get("bigIntegerU").getAsLong();
      fail("Big Integer values greater than Long.MAX_VALUE should not parse as a Long");
    } catch (NumberFormatException expected) {
    }

    try {
      jsonObject.get("bigIntegerL").getAsLong();
      fail("Big Integer values smaller than Long.MIN_VALUE should not parse as a Long");
    } catch (NumberFormatException expected) {
    }

    try {
      jsonObject.get("longU").getAsInt();
      fail("Long values larger than Integer.MAX_VALUE should not parse as an Integer");
    } catch (NumberFormatException expected) {
    }

    try {
      jsonObject.get("longL").getAsInt();
      fail("Long values smaller than Integer.MIN_VALUE should not parse as an Integer");
    } catch (NumberFormatException expected) {
    }

    try {
      jsonObject.get("integerU").getAsShort();
      fail("Integer values larger than Short.MAX_VALUE should not parse as a Short");
    } catch (NumberFormatException expected) {
    }

    try {
      jsonObject.get("integerL").getAsShort();
      fail("Integer values smaller than Short.MIN_VALUE should not parse as a Short");
    } catch (NumberFormatException expected) {
    }

    try {
      jsonObject.get("shortU").getAsByte();
      fail("Short values larger than Byte.MAX_VALUE should not parse as a Byte");
    } catch (NumberFormatException expected) {
    }

    try {
      jsonObject.get("shortL").getAsByte();
      fail("Short values smaller than Byte.MIN_VALUE should not parse as a Byte");
    } catch (NumberFormatException expected) {
    }

    assertTrue(jsonObject.isJsonObject());
  }
  
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
    assertEquals("one", actualOne.stringValue);
    BagOfPrimitives actualTwo = gson.fromJson(element2, BagOfPrimitives.class);
    assertEquals("two", actualTwo.stringValue);
  }
}
