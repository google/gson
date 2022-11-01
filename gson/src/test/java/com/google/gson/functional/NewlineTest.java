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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.NewlineStyle;
import com.google.gson.common.TestTypes.ArrayOfObjects;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;

/**
 * Functional tests for pretty printing option.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class NewlineTest extends TestCase {

  private Map<String, Integer> map;
  private static final String EXPECTED = "{<EOL>  \"abc\": 1,<EOL>  \"def\": 5<EOL>}";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    map = new LinkedHashMap<>();
    map.put("abc", 1);
    map.put("def", 5);
  }

  public void testNewlineDefault() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", "\n"), json);
  }

  public void testNewlineCrLf() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.WINDOWS)
        .create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", "\r\n"), json);
  }

  public void testNewlineLf() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.MACOS_AND_LINUX)
        .create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", "\n"), json);
  }

  public void testNewlineCr() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.OLD_MACOS)
        .create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", "\r"), json);
  }

  public void testNewlineOS() {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .setNewlineStyle(NewlineStyle.CURRENT_OS)
        .create();
    String json = gson.toJson(map);
    assertEquals(EXPECTED.replace("<EOL>", System.lineSeparator()), json);
  }

}
