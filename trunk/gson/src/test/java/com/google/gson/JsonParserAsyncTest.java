package com.google.gson;

import junit.framework.TestCase;

/**
 * Unit tests for {@link JsonParserAsync}
 *
 * @author Inderjeet Singh
 */
public class JsonParserAsyncTest extends TestCase {
  
  public void testParseTwoStrings() {
    JsonParserAsync parser = new JsonParserAsync("'one' 'two'");
    String actualOne = parser.nextElement().getAsString();
    assertEquals("one", actualOne);
    String actualTwo = parser.nextElement().getAsString();
    assertEquals("two", actualTwo);
  }
}
