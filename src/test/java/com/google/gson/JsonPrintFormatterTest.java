package com.google.gson;

import com.google.gson.TestTypes.ArrayOfObjects;
import com.google.gson.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
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
