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

import com.google.gson.stream.JsonReader;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for {@link JsonReader} using {@link JsonReader#setMarkAtEnd(boolean)}
 *
 * @author Bartłomiej Mazur
 */
public class JsonReaderWithMarkTest extends TestCase {
  private final String extraData = " extra data: {c: 5} ";
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  private JsonReader createReader(StringReader inputReader) {
    JsonReader jsonReader = new JsonReader(inputReader);
    jsonReader.setMarkAtEnd(true);
    jsonReader.setLenient(true);
    return jsonReader;
  }

  private String repeat(char c, int count) {
    char[] array = new char[count];
    Arrays.fill(array, c);
    return new String(array);
  }

  public void testCurrentPosition() throws IOException {
    StringReader inputReader = new StringReader("{a: 10, b: 'c'}" + extraData);
    JsonReader reader = createReader(inputReader);
    reader.setMarkAtEnd(false);
    inputReader.mark(Integer.MAX_VALUE);

    Map map = gson.fromJson(reader, HashMap.class);
    assertEquals(10.0, map.get("a"));
    assertEquals("c", map.get("b"));
    assertFalse(map.containsKey("c"));
    inputReader.reset();
    inputReader.skip(reader.getCurrentPosition());
    char[] charBuffer = new char[extraData.length()];
    assertEquals(extraData.length(), inputReader.read(charBuffer));
    assertEquals(extraData, new String(charBuffer));
  }

  public void testObject() throws IOException {
    StringReader inputReader = new StringReader("{a: 10, b: 'c'}" + extraData);
    JsonReader reader = createReader(inputReader);

    Map map = gson.fromJson(reader, HashMap.class);
    assertEquals(10.0, map.get("a"));
    assertEquals("c", map.get("b"));
    assertFalse(map.containsKey("c"));
    assertPreservedExtraData(inputReader);
  }

  public void testArray() throws IOException {
    StringReader inputReader = new StringReader("[55, {}, [1, [2]], \"string data\"]" + extraData);
    JsonReader reader = createReader(inputReader);

    Object[] array = gson.fromJson(reader, Object[].class);
    assertEquals(4, array.length);
    assertEquals(55.0, array[0]);
    assertTrue(((Map) array[1]).isEmpty());
    assertEquals(2, ((List) array[2]).size());
    assertEquals("string data", array[3]);
    assertPreservedExtraData(inputReader);
  }

  public void testString() throws IOException {
    StringReader inputReader = new StringReader("string" + extraData);
    JsonReader reader = createReader(inputReader);
    assertEquals("string", gson.fromJson(reader, String.class));
    assertPreservedExtraData(inputReader);
  }

  public void testStringLong() throws IOException {
    String input = "long " + repeat('x', 2048) + " string";
    StringReader inputReader = new StringReader('"' + input + '"' + extraData);
    JsonReader reader = createReader(inputReader);
    assertEquals(input, gson.fromJson(reader, String.class));
    assertPreservedExtraData(inputReader);
  }

  public void testNumber() throws IOException {
    StringReader inputReader = new StringReader("55.45" + extraData);
    JsonReader reader = createReader(inputReader);
    assertEquals(55.45, gson.fromJson(reader, Double.class));
    assertPreservedExtraData(inputReader);
  }

  public void testBigInteger() throws IOException {
    BigInteger input = new BigInteger(repeat('5', 555));
    StringReader inputReader = new StringReader(input + extraData);
    JsonReader reader = createReader(inputReader);
    assertEquals(input, gson.fromJson(reader, BigInteger.class));
    assertPreservedExtraData(inputReader);
  }

  private void assertPreservedExtraData(StringReader inputReader) throws IOException {
    inputReader.reset();
    char[] charBuffer = new char[extraData.length()];
    assertEquals(extraData.length(), inputReader.read(charBuffer));
    assertEquals(extraData, new String(charBuffer));
  }
}
