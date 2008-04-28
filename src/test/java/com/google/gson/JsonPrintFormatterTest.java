package com.google.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.common.TestTypes.ArrayOfObjects;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Small test for {@link JsonPrintFormatter}
 * 
 * @author Inderjeet Singh
 */
public class JsonPrintFormatterTest extends TestCase {
  private static int INDENTATION_SIZE = 2;
  private static int PRINT_MARGIN = 80;

  private static boolean DEBUG = false;

  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    JsonFormatter formatter = new JsonPrintFormatter(PRINT_MARGIN, INDENTATION_SIZE);
    gson = new GsonBuilder().setFormatter(formatter).create();
  }

  public void testList() {
    BagOfPrimitives b = new BagOfPrimitives();
    List<BagOfPrimitives> listOfB = ImmutableList.of(b, b, b, b, b, b, b, b, b, b, b, b, b, b);
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
      assert(position < PRINT_MARGIN);
    }
  }
}
