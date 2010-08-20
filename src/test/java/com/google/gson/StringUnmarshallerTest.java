package com.google.gson;

import junit.framework.TestCase;

/**
 * Unit tests for {@link StringUnmarshaller}. 
 * 
 * @author Inderjeet Singh
 */
public class StringUnmarshallerTest extends TestCase {

  public void testCtrlN() {
    assertEquals("a\nb", StringUnmarshaller.unmarshall("'a\\nb'"));
  }

  public void testCtrlR() {
    assertEquals("a\rb", StringUnmarshaller.unmarshall("'a\\rb'"));
  }
  
  public void testCtrlT() {
    assertEquals("\tb", StringUnmarshaller.unmarshall("'\\tb'"));
  }
  
  public void testBackSpace() {
    assertEquals("\b foo", StringUnmarshaller.unmarshall("'\\b foo'"));
  }
  
  public void testFormFeed() {
    assertEquals("\f bar", StringUnmarshaller.unmarshall("'\\f bar'"));
  }
  
  public void testSingleQuote() {
    assertEquals("a'b", StringUnmarshaller.unmarshall("'a'b'"));
  }
  
  public void testSingleQuoteEscaped() {
    assertEquals("a'b", StringUnmarshaller.unmarshall("'a\\'b'"));
  }
  
  public void testDoubleQuote() {
    assertEquals("a\"b", StringUnmarshaller.unmarshall("'a\"b'"));
  }
  
  public void testDoubleQuoteEscaped() {
    assertEquals("a\"b", StringUnmarshaller.unmarshall("'a\\\"b'"));
  }
  
  public void testBackslash() {
    assertEquals("a\\b", StringUnmarshaller.unmarshall("'a\\\\b'"));
  }
    
  public void testUnicodeString() {
    assertEquals("\u03a9b", StringUnmarshaller.unmarshall("'\\u03a9b'"));
  }
}
