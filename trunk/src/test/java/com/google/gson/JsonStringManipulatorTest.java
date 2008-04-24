/*
 * Copyright (C) 2008 Google Inc.
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

import com.google.gson.JsonStringManipulator;

import junit.framework.TestCase;

/**
 * Small tests for {@link JsonStringManipulator}
 *
 * @author Inderjeet Singh
 */
public class JsonStringManipulatorTest extends TestCase {

  private String[] JSON_TEST_STRINGS = {
      "{\"postedCart\":{\"merchantPrivateData\":\"private data\"},\"signature\":\"A.r\"}",
      "{\"postedCart\" :  {\"merchantPrivateData\":\"private data\"},\"signature\":\"A.r\"}",
      "{\"signature\":\"A.r\", \"postedCart\" :  [\"merchantPrivateData\",\"private data\"]}",
      "{\"signature\":\"signature\", \"postedCart\" : {\"merchantPr\\}eData\":\"private data\"}}",
      "{\"signature\":\"postedCart\", \"postedCart\" : \"mercha\\\"rivateData\"}"
  };

  public void testExtractElementValue() {
    String[] expected = {
        "{\"merchantPrivateData\":\"private data\"}",
        "{\"merchantPrivateData\":\"private data\"}",
        "[\"merchantPrivateData\",\"private data\"]",
        "{\"merchantPr\\}eData\":\"private data\"}",
        "\"mercha\\\"rivateData\""
    };
    for (int i = 0; i < JSON_TEST_STRINGS.length; ++i) {
      JsonStringManipulator jsm = new JsonStringManipulator(JSON_TEST_STRINGS[i]);
      assertEquals(expected[i], jsm.extractElementValueString("\"postedCart\""));
    }
  }

  public void testFindIndexOfMatchingChar() {
    String str = JSON_TEST_STRINGS[4];
    int expected = str.length();
    assertEquals(expected, JsonStringManipulator.findIndexOfMatchingChar(str, 0, '{', '}'));
  }

  public void testIsEscapedCharacter() {
    String json = JSON_TEST_STRINGS[3];
    char[] data = json.toCharArray();
    assertTrue(JsonStringManipulator.isEscapedCharacter(data, 54));
  }

  public void testIndexOfValueOfElement() {
    int[] expected = {14, 17, 36, 41, 42};
    for (int i = 0; i < JSON_TEST_STRINGS.length; ++i) {
      String json = JSON_TEST_STRINGS[i];
      assertEquals(expected[i],
          JsonStringManipulator.indexOfValueOfElement(json, 0, "\"postedCart\""));
    }
  }

  public void testFindIndexOfNextNonWhiteSpaceChar() {
    int[] index = {14, 15, 35, 41, 42};
    int[] expected = {14, 17, 36, 41, 42};
    for (int i = 0; i < JSON_TEST_STRINGS.length; ++i) {
      String json = JSON_TEST_STRINGS[i];
      assertEquals(expected[i],
          JsonStringManipulator.findIndexOfNextNonWhiteSpaceChar(json, index[i]));
    }
  }
}
