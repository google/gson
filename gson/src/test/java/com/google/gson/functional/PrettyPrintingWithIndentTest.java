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

  private Gson gson;
  StringBuilder sb = new StringBuilder();
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder().setPrettyPrinting("\t").create();
  }

  public void testPrettyPrintArrayOfPrimitives() {
    int[] ints = new int[]{1, 2, 3, 4, 5};
    String json = gson.toJson(ints);
    assertEquals("[\n\t1,\n\t2,\n\t3,\n\t4,\n\t5\n]", json);
  }


  public void testPrettyPrintArrayOfPrimitiveArrays() {
    int[][] ints = new int[][]{{1, 2}, {3, 4}, {5, 6}, {7, 8}, {9, 0}, {10}};
    String json = gson.toJson(ints);
    assertEquals("[\n\t[\n\t\t1,\n\t\t2\n\t],\n\t[\n\t\t3,\n\t\t4\n\t],\n\t[\n\t\t5,\n\t\t6\n\t],"
        + "\n\t[\n\t\t7,\n\t\t8\n\t],\n\t[\n\t\t9,\n\t\t0\n\t],\n\t[\n\t\t10\n\t]\n]", json);
  }


  public void testMap() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("abc", 1);
    map.put("def", 5);
    String json = gson.toJson(map);
    assertEquals("{\n\t\"abc\": 1,\n\t\"def\": 5\n}", json);
  }

  public void testMultipleArrays() {
    int[][][] ints = new int[][][]{{{1}, {2}}};
    String json = gson.toJson(ints);
    assertEquals("[\n\t[\n\t\t[\n\t\t\t1\n\t\t],\n\t\t[\n\t\t\t2\n\t\t]\n\t]\n]", json);
  }

  public void testArrayListOfSets() {
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
    assertEquals("[\n\t[\n\t\t1,\n\t\t2\n\t],\n\t[\n\t\t3,\n\t\t4\n\t]\n]", json);
  }
}
