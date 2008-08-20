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

import com.google.gson.TestTypes.ArrayOfObjects;
import com.google.gson.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Small test for {@link JsonPrintFormatter}
 *
 * @author Inderjeet Singh
 */
public class JsonPrintFormatterTest extends TestCase {
  private static int INDENTATION_SIZE = 2;
  private static int PRINT_MARGIN = 100;
  private static int RIGHT_MARGIN = 8;

  private static boolean DEBUG = false;

  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    JsonFormatter formatter = new JsonPrintFormatter(PRINT_MARGIN, INDENTATION_SIZE, RIGHT_MARGIN);
    gson = new GsonBuilder().setFormatter(formatter).create();
  }

  public void testList() {
    BagOfPrimitives b = new BagOfPrimitives();
    List<BagOfPrimitives> listOfB = new LinkedList<BagOfPrimitives>();
    for (int i = 0; i < 15; ++i) {
      listOfB.add(b);
    }
    Type typeOfSrc = new TypeToken<List<BagOfPrimitives>>() {}.getType();
    String json = gson.toJson(listOfB, typeOfSrc);
    print(json);
    assertPrintMargin(json);
  }

  public void testArrayOfObjects() {
    ArrayOfObjects target = new ArrayOfObjects();
    String json = gson.toJson(target);
    print(json);
    assertPrintMargin(json);
  }

  public void testArrayOfPrimitives() {
    int[] ints = new int[] { 1, 2, 3, 4, 5 };
    String json = gson.toJson(ints);
    assertEquals("[1,2,3,4,5]\n", json);
  }

  public void testArrayOfPrimitiveArrays() {
    int[][] ints = new int[][] { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 }, { 9, 0 }, { 10 } };
    String json = gson.toJson(ints);
    assertEquals("[[1,2],[3,4],[5,6],[7,8],[9,0],[10]]\n", json);
  }

  public void testListOfPrimitiveArrays() {
    List<Integer[]> list = Arrays.asList(new Integer[][] { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 }, { 9, 0 }, { 10 } });
    String json = gson.toJson(list);
    assertEquals("[[1,2],[3,4],[5,6],[7,8],[9,0],[10]]\n", json);
  }

  public void testMultipleArrays() {
    int[][][] ints = new int[][][] { {  { 1 }, { 2 } } };
    String json = gson.toJson(ints);
    assertEquals("[[[1],[2]]]\n", json);
  }

  private void print(String msg) {
    if (DEBUG) {
      System.out.println(msg);
    }
  }

  private void assertPrintMargin(String str) {
    int position = 0;
    char[] chars = str.toCharArray();
    for (int i = 0; i < chars.length; ++i, ++position) {
      char c = chars[i];
      if (c == '\n') {
        position = 0;
      }
      assertTrue(position < PRINT_MARGIN + RIGHT_MARGIN);
    }
  }
}
