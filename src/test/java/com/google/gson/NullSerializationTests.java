package com.google.gson;

import junit.framework.TestCase;

import com.google.gson.TestTypes.ClassWithObjects;

public class NullSerializationTests extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();    
    gson = new GsonBuilder()
        .serializeNulls()
        .create();
  }
  
  public void testExplicitSerializationOfNulls() {
    ClassWithObjects target = new ClassWithObjects(null);
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }

}
