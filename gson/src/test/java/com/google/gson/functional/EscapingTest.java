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
import com.google.gson.GsonBuilder;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Performs some functional test involving JSON output escaping.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class EscapingTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testEscapingQuotesInStringArray() {
    String[] valueWithQuotes = { "beforeQuote\"afterQuote" };
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    String[] target = gson.fromJson(jsonRepresentation, String[].class);
    assertThat(target.length).isEqualTo(1);
    assertThat(target[0]).isEqualTo(valueWithQuotes[0]);
  }

  @Test
  public void testEscapeAllHtmlCharacters() {
    List<String> strings = new ArrayList<>();
    strings.add("<");
    strings.add(">");
    strings.add("=");
    strings.add("&");
    strings.add("'");
    strings.add("\"");
    assertThat(gson.toJson(strings)).isEqualTo("[\"\\u003c\",\"\\u003e\",\"\\u003d\",\"\\u0026\",\"\\u0027\",\"\\\"\"]");
  }

  @Test
  public void testEscapingObjectFields() {
    BagOfPrimitives objWithPrimitives = new BagOfPrimitives(1L, 1, true, "test with\" <script>");
    String jsonRepresentation = gson.toJson(objWithPrimitives);
    assertThat(jsonRepresentation).doesNotContain("<");
    assertThat(jsonRepresentation).doesNotContain(">");
    assertThat(jsonRepresentation).contains("\\\"");

    BagOfPrimitives expectedObject = gson.fromJson(jsonRepresentation, BagOfPrimitives.class);
    assertThat(expectedObject.getExpectedJson()).isEqualTo(objWithPrimitives.getExpectedJson());
  }
  
  @Test
  public void testGsonAcceptsEscapedAndNonEscapedJsonDeserialization() {
    Gson escapeHtmlGson = new GsonBuilder().create();
    Gson noEscapeHtmlGson = new GsonBuilder().disableHtmlEscaping().create();
    
    BagOfPrimitives target = new BagOfPrimitives(1L, 1, true, "test' / w'ith\" / \\ <script>");
    String escapedJsonForm = escapeHtmlGson.toJson(target);
    String nonEscapedJsonForm = noEscapeHtmlGson.toJson(target);
    assertThat(escapedJsonForm.equals(nonEscapedJsonForm)).isFalse();
    
    assertThat(noEscapeHtmlGson.fromJson(escapedJsonForm, BagOfPrimitives.class)).isEqualTo(target);
    assertThat(escapeHtmlGson.fromJson(nonEscapedJsonForm, BagOfPrimitives.class)).isEqualTo(target);
  }

  @Test
  public void testGsonDoubleDeserialization() {
    BagOfPrimitives expected = new BagOfPrimitives(3L, 4, true, "value1");
    String json = gson.toJson(gson.toJson(expected));
    String value = gson.fromJson(json, String.class);
    BagOfPrimitives actual = gson.fromJson(value, BagOfPrimitives.class);
    assertThat(actual).isEqualTo(expected);
  }
}
