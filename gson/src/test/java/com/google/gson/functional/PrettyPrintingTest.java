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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.common.TestTypes.ArrayOfObjects;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;

/**
 * Functional tests for pretty printing option.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PrettyPrintingTest extends TestCase {

  private static final boolean DEBUG = false;

  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder().setPrettyPrinting().create();
  }

  public void testPrettyPrintList() {
    BagOfPrimitives b = new BagOfPrimitives();
    List<BagOfPrimitives> listOfB = new LinkedList<BagOfPrimitives>();
    for (int i = 0; i < 15; ++i) {
      listOfB.add(b);
    }
    Type typeOfSrc = new TypeToken<List<BagOfPrimitives>>() {}.getType();
    String json = gson.toJson(listOfB, typeOfSrc);
    print(json);
  }

  public void testPrettyPrintArrayOfObjects() {
    ArrayOfObjects target = new ArrayOfObjects();
    String json = gson.toJson(target);
    print(json);
  }

  public void testPrettyPrintArrayOfPrimitives() {
    int[] ints = new int[] { 1, 2, 3, 4, 5 };
    String json = gson.toJson(ints);
    assertEquals("[\n  1,\n  2,\n  3,\n  4,\n  5\n]", json);
  }

  public void testPrettyPrintArrayOfPrimitiveArrays() {
    int[][] ints = new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 },
        { 9, 0 }, { 10 } };
    String json = gson.toJson(ints);
    assertEquals("[\n  [\n    1,\n    2\n  ],\n  [\n    3,\n    4\n  ],\n  [\n    5,\n    6\n  ],"
        + "\n  [\n    7,\n    8\n  ],\n  [\n    9,\n    0\n  ],\n  [\n    10\n  ]\n]", json);
  }

  public void testPrettyPrintListOfPrimitiveArrays() {
    List<Integer[]> list = Arrays.asList(new Integer[][] { { 1, 2 }, { 3, 4 },
        { 5, 6 }, { 7, 8 }, { 9, 0 }, { 10 } });
    String json = gson.toJson(list);
    assertEquals("[\n  [\n    1,\n    2\n  ],\n  [\n    3,\n    4\n  ],\n  [\n    5,\n    6\n  ],"
        + "\n  [\n    7,\n    8\n  ],\n  [\n    9,\n    0\n  ],\n  [\n    10\n  ]\n]", json);
  }
  
  public void testMap() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", 1);
    map.put("def", 5);
    String json = gson.toJson(map);
    assertEquals("{\n  \"abc\": 1,\n  \"def\": 5\n}", json);
  }

  // In response to bug 153
  public void testEmptyMapField() {
    ClassWithMap obj = new ClassWithMap();
    obj.map = new LinkedHashMap<String, Integer>();
    String json = gson.toJson(obj);
    assertTrue(json.contains("{\n  \"map\": {},\n  \"value\": 2\n}"));
  }

  @SuppressWarnings("unused")
  private static class ClassWithMap {
    Map<String, Integer> map;
    int value = 2;
  }

  public void testMultipleArrays() {
    int[][][] ints = new int[][][] { { { 1 }, { 2 } } };
    String json = gson.toJson(ints);
    assertEquals("[\n  [\n    [\n      1\n    ],\n    [\n      2\n    ]\n  ]\n]", json);
  }

  private void print(String msg) {
    if (DEBUG) {
      System.out.println(msg);
    }
  }
}
