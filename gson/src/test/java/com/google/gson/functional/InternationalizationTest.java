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

package com.google.gson.functional;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for internationalized strings.
 *
 * @author Inderjeet Singh
 */
public class InternationalizationTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testStringsWithUnicodeChineseCharactersSerialization() throws Exception {
    String target = "\u597d\u597d\u597d";
    String json = gson.toJson(target);
    String expected = '"' + target + '"';
    assertEquals(expected, json);
  }

  @Test
  public void testStringsWithUnicodeChineseCharactersDeserialization() throws Exception {
    String expected = "\u597d\u597d\u597d";
    String json = '"' + expected + '"';
    String actual = gson.fromJson(json, String.class);
    assertEquals(expected, actual);
  }

  @Test
  public void testStringsWithUnicodeChineseCharactersEscapedDeserialization() throws Exception {
    String actual = gson.fromJson("'\\u597d\\u597d\\u597d'", String.class);
    assertEquals("\u597d\u597d\u597d", actual);
  }

  @Test
  public void testSupplementaryUnicodeSerialization() throws Exception {
    // Supplementary code point U+1F60A
    String supplementaryCodePoint = new String(new int[] {0x1F60A}, 0, 1);
    String json = gson.toJson(supplementaryCodePoint);
    assertEquals('"' + supplementaryCodePoint + '"', json);
  }

  @Test
  public void testSupplementaryUnicodeDeserialization() throws Exception {
    // Supplementary code point U+1F60A
    String supplementaryCodePoint = new String(new int[] {0x1F60A}, 0, 1);
    String actual = gson.fromJson('"' + supplementaryCodePoint + '"', String.class);
    assertEquals(supplementaryCodePoint, actual);
  }

  @Test
  public void testSupplementaryUnicodeEscapedDeserialization() throws Exception {
    // Supplementary code point U+1F60A
    String supplementaryCodePoint = new String(new int[] {0x1F60A}, 0, 1);
    String actual = gson.fromJson("\"\\uD83D\\uDE0A\"", String.class);
    assertEquals(supplementaryCodePoint, actual);
  }
}
