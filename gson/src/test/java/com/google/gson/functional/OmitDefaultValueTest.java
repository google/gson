package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.OmitDefaultValue;

import junit.framework.Assert;
import junit.framework.TestCase;

public class OmitDefaultValueTest extends TestCase {
  @OmitDefaultValue
  static class C {
    int n = 43;
    int n0;
    String s = "hallo";
    String s0;
  }

  public void testDefault() {
    helper("{}");
  }
  
  public void testChangedPrimitive() {
    helper("{\"n\":1}");
    helper("{\"n0\":1}");
  }
  
  public void testChangedString() {
    helper("{\"s\":\"bye\"}");
    helper("{\"s0\":\"bye\"}");
  }

  public void testNullString() {
    helper("{\"s\":null}");
  }

  public void testRedundant() {
    helper("{\"s\":\"hallo\",\"n\":43}", "{}");
    helper("{\"s\":\"bye\",\"s0\":null}", "{\"s\":\"bye\"}");
  }

 /**
  * Test that deserializing and serializing {@code json} doesn't change it.
  */
  private void helper(String json) {
    helper(json, json);
  }

 /**
  * Test that deserializing and serializing {@code json} returns the {@code expected} result.
  */
  private void helper(String json, String expected) {
    System.out.println(json);
    final C fromJson = gson.fromJson(json, C.class);
    final String json2 = gson.toJson(fromJson);
    Assert.assertEquals(expected, json2);
  }
  
  private final static Gson gson = new GsonBuilder().create();
}
