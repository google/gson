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

package com.economic.persistgson.functional;

import com.economic.persistgson.Gson;
import com.economic.persistgson.GsonBuilder;
import com.economic.persistgson.common.TestTypes;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * Performs some functional test involving JSON output escaping.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class EscapingTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testEscapingQuotesInStringArray() throws Exception {
    String[] valueWithQuotes = { "beforeQuote\"afterQuote" };
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    String[] target = gson.fromJson(jsonRepresentation, String[].class);
    assertEquals(1, target.length);
    assertEquals(valueWithQuotes[0], target[0]);
  }

  public void testEscapeAllHtmlCharacters() {
    List<String> strings = new ArrayList<String>();
    strings.add("<");
    strings.add(">");
    strings.add("=");
    strings.add("&");
    strings.add("'");
    strings.add("\"");
    assertEquals("[\"\\u003c\",\"\\u003e\",\"\\u003d\",\"\\u0026\",\"\\u0027\",\"\\\"\"]",
        gson.toJson(strings));
  }

  public void testEscapingObjectFields() throws Exception {
    TestTypes.BagOfPrimitives objWithPrimitives = new TestTypes.BagOfPrimitives(1L, 1, true, "test with\" <script>");
    String jsonRepresentation = gson.toJson(objWithPrimitives);
    assertFalse(jsonRepresentation.contains("<"));
    assertFalse(jsonRepresentation.contains(">"));
    assertTrue(jsonRepresentation.contains("\\\""));

    TestTypes.BagOfPrimitives expectedObject = gson.fromJson(jsonRepresentation, TestTypes.BagOfPrimitives.class);
    assertEquals(objWithPrimitives.getExpectedJson(), expectedObject.getExpectedJson());
  }
  
  public void testGsonAcceptsEscapedAndNonEscapedJsonDeserialization() throws Exception {
    Gson escapeHtmlGson = new GsonBuilder().create();
    Gson noEscapeHtmlGson = new GsonBuilder().disableHtmlEscaping().create();
    
    TestTypes.BagOfPrimitives target = new TestTypes.BagOfPrimitives(1L, 1, true, "test' / w'ith\" / \\ <script>");
    String escapedJsonForm = escapeHtmlGson.toJson(target);
    String nonEscapedJsonForm = noEscapeHtmlGson.toJson(target);
    assertFalse(escapedJsonForm.equals(nonEscapedJsonForm));
    
    assertEquals(target, noEscapeHtmlGson.fromJson(escapedJsonForm, TestTypes.BagOfPrimitives.class));
    assertEquals(target, escapeHtmlGson.fromJson(nonEscapedJsonForm, TestTypes.BagOfPrimitives.class));
  }

  public void testGsonDoubleDeserialization() {
    TestTypes.BagOfPrimitives expected = new TestTypes.BagOfPrimitives(3L, 4, true, "value1");
    String json = gson.toJson(gson.toJson(expected));
    String value = gson.fromJson(json, String.class);
    TestTypes.BagOfPrimitives actual = gson.fromJson(value, TestTypes.BagOfPrimitives.class);
    assertEquals(expected, actual);
  }
}
