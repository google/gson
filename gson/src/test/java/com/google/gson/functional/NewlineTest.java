/*
 * Copyright (C) 2022 Google Inc.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.NewlineStyle;

/**
 * Functional tests for newline option.
 *
 * @author Mihai Nita
 */
@RunWith(JUnit4.class)
public class NewlineTest {

  private static final String[] INPUT = { "v1", "v2" };
  private static final String EXPECTED = "[<EOL>  \"v1\",<EOL>  \"v2\"<EOL>]";
  private static final String EXPECTED_OS = EXPECTED.replace("<EOL>", System.lineSeparator());
  private static final String EXPECTED_CR = EXPECTED.replace("<EOL>", "\r");
  private static final String EXPECTED_LF = EXPECTED.replace("<EOL>", "\n");
  private static final String EXPECTED_CRLF = EXPECTED.replace("<EOL>", "\r\n");

  @Test
  public void testNewlineDefault() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(INPUT);
    // Make sure the default uses LF, like before.
    assertEquals(EXPECTED_LF, json);
  }

  @Test
  public void testNewlineCrLf() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.CRLF)
        .create();
    String json = gson.toJson(INPUT);
    assertEquals(EXPECTED_CRLF, json);
  }

  @Test
  public void testNewlineLf() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.LF)
        .create();
    String json = gson.toJson(INPUT);
    assertEquals(EXPECTED_LF, json);
  }

  @Test
  public void testNewlineCr() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.CR)
        .create();
    String json = gson.toJson(INPUT);
    assertEquals(EXPECTED_CR, json);
  }

  @Test
  public void testNewlineOs() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.CURRENT_OS)
        .create();
    String json = gson.toJson(INPUT);
    assertEquals(EXPECTED_OS, json);
  }

  @Test
  public void testNewlineInterop() {
    String jsonStringMix = "[\r  'v1',\r\n  'v2'\n]";

    String[] actual;
    // Test all combinations of newline between the parser and input.
    for (NewlineStyle newlineStyle : NewlineStyle.values()) {
      Gson gson = new GsonBuilder()
          .setPrettyPrinting()
          .setNewlineStyle(newlineStyle)
          .create();

      actual = gson.fromJson(EXPECTED_OS, INPUT.getClass());
      assertArrayEquals(INPUT, actual);
      actual = gson.fromJson(EXPECTED_CR, INPUT.getClass());
      assertArrayEquals(INPUT, actual);
      actual = gson.fromJson(EXPECTED_LF, INPUT.getClass());
      assertArrayEquals(INPUT, actual);
      actual = gson.fromJson(EXPECTED_CRLF, INPUT.getClass());
      assertArrayEquals(INPUT, actual);
      actual = gson.fromJson(jsonStringMix, INPUT.getClass());
      assertArrayEquals(INPUT, actual);
    }
  }
}
