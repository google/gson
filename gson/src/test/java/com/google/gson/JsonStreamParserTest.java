package com.google.gson;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Unit tests for {@link JsonStreamParser}
 *
 * @author Inderjeet Singh
 */
public class JsonStreamParserTest extends TestCase {
  
  public void testParseTwoStrings() {
    JsonStreamParser parser = new JsonStreamParser("'one' 'two'");
    String actualOne = parser.next().getAsString();
    assertEquals("one", actualOne);
    String actualTwo = parser.next().getAsString();
    assertEquals("two", actualTwo);
  }
  
  public void testIterator() {
    Iterator<JsonElement> parser = new JsonStreamParser("'one' 'two'");
    assertTrue(parser.hasNext());
    assertEquals("one", parser.next().getAsString());
    assertTrue(parser.hasNext());
    assertEquals("two", parser.next().getAsString());
    assertFalse(parser.hasNext());
  }
}
