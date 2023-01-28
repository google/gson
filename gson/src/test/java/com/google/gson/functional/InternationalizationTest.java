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

import static com.google.common.truth.Truth.assertThat;

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
  public void testStringsWithUnicodeChineseCharactersSerialization() {
    String target = "\u597d\u597d\u597d";
    String json = gson.toJson(target);
    String expected = '"' + target + '"';
    assertThat(json).isEqualTo(expected);
  }

  @Test
  public void testStringsWithUnicodeChineseCharactersDeserialization() {
    String expected = "\u597d\u597d\u597d";
    String json = '"' + expected + '"';
    String actual = gson.fromJson(json, String.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testStringsWithUnicodeChineseCharactersEscapedDeserialization() {
    String actual = gson.fromJson("'\\u597d\\u597d\\u597d'", String.class);
    assertThat(actual).isEqualTo("\u597d\u597d\u597d");
  }

  @Test
  public void testSupplementaryUnicodeSerialization() {
    // Supplementary code point U+1F60A
    String supplementaryCodePoint = new String(new int[] {0x1F60A}, 0, 1);
    String json = gson.toJson(supplementaryCodePoint);
    assertThat(json).isEqualTo('"' + supplementaryCodePoint + '"');
  }

  @Test
  public void testSupplementaryUnicodeDeserialization() {
    // Supplementary code point U+1F60A
    String supplementaryCodePoint = new String(new int[] {0x1F60A}, 0, 1);
    String actual = gson.fromJson('"' + supplementaryCodePoint + '"', String.class);
    assertThat(actual).isEqualTo(supplementaryCodePoint);
  }

  @Test
  public void testSupplementaryUnicodeEscapedDeserialization() {
    // Supplementary code point U+1F60A
    String supplementaryCodePoint = new String(new int[] {0x1F60A}, 0, 1);
    String actual = gson.fromJson("\"\\uD83D\\uDE0A\"", String.class);
    assertThat(actual).isEqualTo(supplementaryCodePoint);
  }
}
