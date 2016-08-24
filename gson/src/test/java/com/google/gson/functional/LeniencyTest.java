/*
 * Copyright (C) 2016 The Gson Authors
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.util.List;
import junit.framework.TestCase;

import static java.util.Collections.singletonList;

import java.io.StringReader;
import java.lang.reflect.Type;

/**
 * Functional tests for leniency option.
 */
public class LeniencyTest extends TestCase {

  private Gson lenientGson;
  private Gson strictGson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    lenientGson = new GsonBuilder().create();
    strictGson = new GsonBuilder().setLenient(false).create();
  }

  public void testLenientFromJson() {
    final String testString = ""
        + "[ # One!\n"
        + "  'Hi' #Element!\n"
        + "] # Array!";
    final String expectedResult = "Hi";
    Type type = new TypeToken<List<String>>() {}.getType();

    JsonReader reader = new JsonReader(new StringReader(testString));
    reader.setLenient(true);
    // fromJson(JsonReader, Type) should respect the leniency of 
    // the JsonReader
    List<String> json = strictGson.fromJson(reader, type);
    assertEquals(singletonList(expectedResult), json);

    // the factory method should set the leniency of the created factory 
    // to be the same as the leniency of the Gson instance
    reader = lenientGson.newJsonReader(new StringReader(testString));
    assertTrue(reader.isLenient());
    // fromJson(JsonReader, Type) should respect the leniency of 
    // the JsonReader
    json = strictGson.fromJson(reader, type);
    assertEquals(singletonList(expectedResult), json);    

    json = lenientGson.fromJson(testString, type);
    assertEquals(singletonList(expectedResult), json);
  }

  public void testStrictFromJson() {
    final String testString = ""
        + "[ # One!\n"
        + "  'Hi' #Element!\n"
        + "]";
    Type type = new TypeToken<List<String>>(){}.getType();

    try {
      // JsonReader is strict by default
      JsonReader reader = new JsonReader(new StringReader(testString));
      // fromJson(JsonReader, Type) should respect the leniency of 
      // the JsonReader
      lenientGson.fromJson(reader, type);
      fail();
    } catch (JsonSyntaxException expected) { }

    try {
      // the factory method should set the leniency of the created factory 
      // to be the same as the leniency of the Gson instance
      JsonReader reader = strictGson.newJsonReader(new StringReader(testString));
      assertFalse(reader.isLenient());
      // fromJson(JsonReader, Type) should respect the leniency of 
      // the JsonReader
      lenientGson.fromJson(reader, type);
      fail();
    } catch(JsonSyntaxException expected) { }

    try {
      strictGson.fromJson(testString, type);
      fail();
    } catch (JsonSyntaxException expected) { }
  }
}
