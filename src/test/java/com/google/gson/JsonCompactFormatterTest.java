package com.google.gson;

import com.google.gson.TestTypes.BagOfPrimitives;
import com.google.gson.TestTypes.ClassWithTransientFields;
import com.google.gson.TestTypes.Nested;
import com.google.gson.TestTypes.PrimitiveArray;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class JsonCompactFormatterTest extends TestCase {

  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    JsonFormatter formatter = new JsonCompactFormatter();
    gson = new GsonBuilder().setFormatter(formatter).create();
  }

  @SuppressWarnings("unchecked")
  public void testNoWhiteSpace() {
    List list = new ArrayList();
    list.add(new BagOfPrimitives());
    list.add(new Nested());
    list.add(new PrimitiveArray());
    list.add(new ClassWithTransientFields());

    String json = gson.toJson(list);
    assertContainsNoWhiteSpace(json);
  }

  private void assertContainsNoWhiteSpace(String str) {
    for (char c : str.toCharArray()) {
      assertFalse(Character.isWhitespace(c));
    }
  }
}
