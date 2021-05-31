/*
 * Copyright (C) 2019 Gson Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.stream;

import java.io.IOException;
import java.io.Reader;

import com.google.gson.stream.MalformedJsonException;

public final class JsonReaderStrictTest extends AbstractJsonReaderTest {
  @Override
  public void testMixedCaseLiterals() throws IOException {
    try {
      super.testMixedCaseLiterals();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testLowercaseLiterals() throws IOException {
    JsonReader reader = newJsonReader(reader("[true, false, null]"));
    reader.beginArray();
    assertEquals(true, reader.nextBoolean());
    assertEquals(false, reader.nextBoolean());
    reader.nextNull();
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testNonLowercaseLiterals() throws IOException {
    String[] nonLowercaseLiterals = new String[] {
      "tRuE", "TRUE", "faLSe", "FALSE", "NuLl", "NULL"};
    for (String literal : nonLowercaseLiterals) {
      JsonReader reader = newJsonReader(reader(literal));
      try {
        reader.skipValue();
        fail();
      } catch (MalformedJsonException e) {
        assertTrue(e.getMessage().startsWith(
          "Literal must be lowercase (strict mode)"));
      }
    }
  }

  @Override
  public void testControlCharactersInStringsAndNames() throws IOException {
    try {
      super.testControlCharactersInStringsAndNames();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  public void testNextStringEscapedControlCharacters() throws IOException {
    for (char cc = 0; cc <= 0x1F; cc++) {
      String jsonString = String.format("\"\\u%04x\"", (int) cc);
      JsonReader reader = newJsonReader(reader(jsonString));
      assertEquals(String.valueOf(cc), reader.nextString());
      assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
  }

  public void testNextStringUnescapedControlCharacters() throws IOException {
    final String expectedMessage = "Illegal unescaped control character "
      + "(strict mode) at line 1 column 2 path $";
    for (char cc = 0; cc <= 0x1F; cc++) {
      String jsonString = new String(new char[] {'"', cc, '"'});
      JsonReader reader = newJsonReader(reader(jsonString));
      try {
        reader.nextString();
        fail();
      } catch (MalformedJsonException expected) {
        assertEquals(expectedMessage, expected.getMessage());
      }
    }
  }

  public void testSkipValueEscapedControlCharacters() throws IOException {
    for (char cc = 0; cc <= 0x1F; cc++) {
      String jsonString = String.format("\"\\u%04x\"", (int) cc);
      JsonReader reader = newJsonReader(reader(jsonString));
      reader.skipValue();
      assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
  }

  public void testSkipValueUnescapedControlCharacters() throws IOException {
    final String expectedMessage = "Illegal unescaped control character "
      + "(strict mode) at line 1 column 2 path $";
    for (char cc = 0; cc <= 0x1F; cc++) {
      String jsonString = new String(new char[] {'"', cc, '"'});
      JsonReader reader = newJsonReader(reader(jsonString));
      try {
        reader.skipValue();
        fail();
      } catch (MalformedJsonException expected) {
        assertEquals(expectedMessage, expected.getMessage());
      }
    }
  }

  public void testEmbeddedEscapedControlCharacters() throws IOException {
    JsonReader reader = newJsonReader(reader("["
      + "\"\\u0000foo\","
      + "\"text with a\\u000anewline\","
      + "\n\t\n"
      + "\"more embedded \\u001F control\\u0009characters\\u000A\\u000a.\","
      + "\"no control character\""
      + "]"));
    reader.beginArray();
    assertEquals("\u0000foo", reader.nextString());
    assertEquals("text with a\nnewline", reader.nextString());
    assertEquals("more embedded \u001f control\u0009characters\n\n.",
      reader.nextString());
    assertEquals("no control character", reader.nextString());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testEmbeddedUnescapedControlCharacters() throws IOException {
    final String expectedMessage = "Illegal unescaped control character "
      + "(strict mode) at line 1 column 3 path $[0]";
    JsonReader reader = newJsonReader(reader("[\"text with a\nnewline\"]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
      assertEquals(expectedMessage, expected.getMessage());
    }
  }

  public void testNextNameEscapedControlCharacters() throws IOException {
    JsonReader reader = newJsonReader(reader("{"
      + "\"\\u0000\": \"\\u000c\","
      + "\"tab \\u0009 and newline \\u000a\": \"\","
      + "\"no control character\": \"key \\u0009 \\u0014\""
      + "}"));
    reader.beginObject();
    assertEquals("\u0000", reader.nextName());
    assertEquals("\f", reader.nextString());
    assertEquals("tab \t and newline \n", reader.nextName());
    assertEquals("", reader.nextString());
    assertEquals("no control character", reader.nextName());
    assertEquals("key \t \u0014", reader.nextString());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testNextNameUnescapedControlCharacters() throws IOException {
    final String expectedMessage = "Illegal unescaped control character "
      + "(strict mode) at line 1 column 3 path $.";
    JsonReader reader = newJsonReader(reader("{\"foo\n\t\": \"\"}"));
    reader.beginObject();
    try {
      reader.nextName();
      fail();
    } catch (MalformedJsonException e) {
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  public void testLenientModeDisablesStrictMode() throws IOException {
    JsonReader reader = newJsonReader(reader("["
      + "\"unescaped\nnewline\","
      + "unquoted,"
      + "'single quoted'"
      + "]"));
    assertTrue(reader.isStrict());
    assertFalse(reader.isLenient());
    reader.setLenient(true);
    assertFalse(reader.isStrict());
    assertTrue(reader.isLenient());
    reader.beginArray();
    assertEquals("unescaped\nnewline", reader.nextString());
    assertEquals("unquoted", reader.nextString());
    assertEquals("single quoted", reader.nextString());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  public void testStrictModeDisablesLenientMode() throws IOException {
    JsonReader reader = newJsonReader(reader("["
      + "\"escaped\\u000anewline\","
      + "unquoted"
      + "]"));
    reader.setLenient(true);
    assertTrue(reader.isLenient());
    assertFalse(reader.isStrict());
    reader.setStrict(true);
    assertFalse(reader.isLenient());
    assertTrue(reader.isStrict());
    reader.beginArray();
    assertEquals("escaped\nnewline", reader.nextString());
    try {
      reader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
  }

  @Override
  JsonReader newJsonReader(Reader reader) {
    JsonReader jsonReader = new JsonReader(reader);
    jsonReader.setStrict(true);
    return jsonReader;
  }
}
