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

  private Map<String, Integer> map;
  private static final String EXPECTED = "{<EOL>  \"abc\": 1,<EOL>  \"def\": 5<EOL>}";

  @Before
  public void setUp() {
    map = new LinkedHashMap<>();
    map.put("abc", 1);
    map.put("def", 5);
  }

  @Test
  public void testNewlineDefault() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", "\n"), json);
  }

  @Test
  public void testNewlineCrLf() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.CRLF)
        .create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", "\r\n"), json);
  }

  @Test
  public void testNewlineLf() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.LF)
        .create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", "\n"), json);
  }

  @Test
  public void testNewlineCr() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.CR)
        .create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", "\r"), json);
  }

  @Test
  public void testNewlineOs() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.CURRENT_OS)
        .create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", System.lineSeparator()), json);
  }

}
