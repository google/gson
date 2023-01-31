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
 * Functional tests for Json serialization and deserialization of strings.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class StringTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testStringValueSerialization() {
    String value = "someRandomStringValue";
    assertThat(gson.toJson(value)).isEqualTo('"' + value + '"');
  }

  @Test
  public void testStringValueDeserialization() {
    String value = "someRandomStringValue";
    String actual = gson.fromJson("\"" + value + "\"", String.class);
    assertThat(actual).isEqualTo(value);
  }

  @Test
  public void testSingleQuoteInStringSerialization() {
    String valueWithQuotes = "beforeQuote'afterQuote";
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    assertThat(gson.fromJson(jsonRepresentation, String.class)).isEqualTo(valueWithQuotes);
  }

  @Test
  public void testEscapedCtrlNInStringSerialization() {
    String value = "a\nb";
    String json = gson.toJson(value);
    assertThat(json).isEqualTo("\"a\\nb\"");
  }

  @Test
  public void testEscapedCtrlNInStringDeserialization() {
    String json = "'a\\nb'";
    String actual = gson.fromJson(json, String.class);
    assertThat(actual).isEqualTo("a\nb");
  }

  @Test
  public void testEscapedCtrlRInStringSerialization() {
    String value = "a\rb";
    String json = gson.toJson(value);
    assertThat(json).isEqualTo("\"a\\rb\"");
  }

  @Test
  public void testEscapedCtrlRInStringDeserialization() {
    String json = "'a\\rb'";
    String actual = gson.fromJson(json, String.class);
    assertThat(actual).isEqualTo("a\rb");
  }

  @Test
  public void testEscapedBackslashInStringSerialization() {
    String value = "a\\b";
    String json = gson.toJson(value);
    assertThat(json).isEqualTo("\"a\\\\b\"");
  }

  @Test
  public void testEscapedBackslashInStringDeserialization() {
    String actual = gson.fromJson("'a\\\\b'", String.class);
    assertThat(actual).isEqualTo("a\\b");
  }

  @Test
  public void testSingleQuoteInStringDeserialization() {
    String value = "beforeQuote'afterQuote";
    String actual = gson.fromJson("\"" + value + "\"", String.class);
    assertThat(actual).isEqualTo(value);
  }

  @Test
  public void testEscapingQuotesInStringSerialization() {
    String valueWithQuotes = "beforeQuote\"afterQuote";
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    String target = gson.fromJson(jsonRepresentation, String.class);
    assertThat(target).isEqualTo(valueWithQuotes);
  }

  @Test
  public void testEscapingQuotesInStringDeserialization() {
    String value = "beforeQuote\\\"afterQuote";
    String actual = gson.fromJson("\"" + value + "\"", String.class);
    String expected = "beforeQuote\"afterQuote";
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testStringValueAsSingleElementArraySerialization() {
    String[] target = {"abc"};
    assertThat(gson.toJson(target)).isEqualTo("[\"abc\"]");
    assertThat(gson.toJson(target, String[].class)).isEqualTo("[\"abc\"]");
  }

  @Test
  public void testStringWithEscapedSlashDeserialization() {
    String value = "/";
    String json = "'\\/'";
    String actual = gson.fromJson(json, String.class);
    assertThat(actual).isEqualTo(value);
  }

  /**
   * Created in response to http://groups.google.com/group/google-gson/browse_thread/thread/2431d4a3d0d6cb23
   */
  @Test
  public void testAssignmentCharSerialization() {
    String value = "abc=";
    String json = gson.toJson(value);
    assertThat(json).isEqualTo("\"abc\\u003d\"");
  }

  /**
   * Created in response to http://groups.google.com/group/google-gson/browse_thread/thread/2431d4a3d0d6cb23
   */
  @Test
  public void testAssignmentCharDeserialization() {
    String json = "\"abc=\"";
    String value = gson.fromJson(json, String.class);
    assertThat(value).isEqualTo("abc=");

    json = "'abc\u003d'";
    value = gson.fromJson(json, String.class);
    assertThat(value).isEqualTo("abc=");
  }

  @Test
  public void testJavascriptKeywordsInStringSerialization() {
    String value = "null true false function";
    String json = gson.toJson(value);
    assertThat(json).isEqualTo("\"" + value + "\"");
  }

  @Test
  public void testJavascriptKeywordsInStringDeserialization() {
    String json = "'null true false function'";
    String value = gson.fromJson(json, String.class);
    assertThat(json.substring(1, json.length() - 1)).isEqualTo(value);
  }
}
