package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;

import java.util.*;

/**
 * Functional tests for pretty printing option with custom indents
 *
 * @author Vasyl Franchuk
 */
public class PrettyPrintingWithIndentTest extends TestCase {

  public void testPrettyPrintArrayOfPrimitives() {
    Gson gson = new GsonBuilder().setPrettyPrintingByTabs(1).create();
    int[] ints = new int[]{1, 2, 3, 4, 5};
    String json = gson.toJson(ints);
    assertEquals("[\n\t1,\n\t2,\n\t3,\n\t4,\n\t5\n]", json);
  }

  public void testPrettyPrintArrayOfPrimitiveArrays() {
    Gson gson = new GsonBuilder().setPrettyPrintingByTabs(2).create();
    int[][] ints = new int[][]{{1, 2}, {3, 4}, {5, 6}, {7, 8}, {9, 0}, {10}};
    String json = gson.toJson(ints);
    assertEquals("[\n\t\t[\n\t\t\t\t1,\n\t\t\t\t2\n\t\t],\n\t\t[\n\t\t\t\t3,\n\t\t\t\t4\n\t\t]," +
            "\n\t\t[\n\t\t\t\t5,\n\t\t\t\t6\n\t\t],\n\t\t[\n\t\t\t\t7,\n\t\t\t\t8\n\t\t]," +
            "\n\t\t[\n\t\t\t\t9,\n\t\t\t\t0\n\t\t],\n\t\t[\n\t\t\t\t10\n\t\t]\n]", json);
  }

  public void testMap() {
    Gson gson = new GsonBuilder().setPrettyPrintingBySpaces(1).create();
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", 1);
    map.put("def", 5);
    String json = gson.toJson(map);
    assertEquals("{\n \"abc\": 1,\n \"def\": 5\n}", json);
  }

  public void testMultipleArrays() {
    Gson gson = new GsonBuilder().setPrettyPrintingBySpaces(2).create();
    int[][][] ints = new int[][][]{{{1}, {2}}};
    String json = gson.toJson(ints);
    assertEquals("[\n  [\n    [\n      1\n    ],\n    [\n      2\n    ]\n  ]\n]", json);
  }

  public void testArrayListOfSets() {
    Gson gson = new GsonBuilder().setPrettyPrintingBySpaces(4).create();
    ArrayList<Set<Integer>> ints = new ArrayList<Set<Integer>>();
    Set<Integer> set1 = new TreeSet<Integer>();
    set1.add(1);
    set1.add(2);
    Set<Integer> set2 = new TreeSet<Integer>();
    set2.add(3);
    set2.add(4);
    ints.add(set1);
    ints.add(set2);
    String json = gson.toJson(ints);
    assertEquals("[\n    [\n        1,\n        2\n    ],\n    [\n        3,\n        4\n    ]\n]", json);
  }

  public void testEmptyTabsIndent() {
    Gson gson = new GsonBuilder().setPrettyPrintingByTabs(0).create();
    int[] ints = new int[]{1, 2, 3, 4, 5};
    String json = gson.toJson(ints);
    assertEquals("[\n1,\n2,\n3,\n4,\n5\n]", json);
  }

  public void testEmptySpaceIndent() {
    Gson gson = new GsonBuilder().setPrettyPrintingBySpaces(0).create();
    Map<Integer, String> map = new LinkedHashMap<Integer, String>();
    map.put(1, "first");
    map.put(2, "second");
    String json = gson.toJson(map);
    assertEquals("{\n\"1\":\"first\",\n\"2\":\"second\"\n}", json);
  }
}
