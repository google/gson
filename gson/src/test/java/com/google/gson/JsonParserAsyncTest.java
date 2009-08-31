package com.google.gson;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Unit tests for {@link JsonParserAsync}
 *
 * @author Inderjeet Singh
 */
public class JsonParserAsyncTest extends TestCase {
  
  public void testParseTwoStrings() {
    JsonParserAsync parser = new JsonParserAsync("'one' 'two'");
    String actualOne = parser.next().getAsString();
    assertEquals("one", actualOne);
    String actualTwo = parser.next().getAsString();
    assertEquals("two", actualTwo);
  }
  
  public void testIterator() {
    Iterator<JsonElement> parser = new JsonParserAsync("'one' 'two'");
    assertTrue(parser.hasNext());
    assertEquals("one", parser.next().getAsString());
    assertTrue(parser.hasNext());
    assertEquals("two", parser.next().getAsString());
    assertFalse(parser.hasNext());
  }
}
