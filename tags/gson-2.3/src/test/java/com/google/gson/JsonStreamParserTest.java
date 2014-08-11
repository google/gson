/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson;

import junit.framework.TestCase;

import java.util.NoSuchElementException;

/**
 * Unit tests for {@link JsonStreamParser}
 * 
 * @author Inderjeet Singh
 */
public class JsonStreamParserTest extends TestCase {
  private JsonStreamParser parser;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    parser = new JsonStreamParser("'one' 'two'");
  }

  public void testParseTwoStrings() {
    String actualOne = parser.next().getAsString();
    assertEquals("one", actualOne);
    String actualTwo = parser.next().getAsString();
    assertEquals("two", actualTwo);
  }

  public void testIterator() {
    assertTrue(parser.hasNext());
    assertEquals("one", parser.next().getAsString());
    assertTrue(parser.hasNext());
    assertEquals("two", parser.next().getAsString());
    assertFalse(parser.hasNext());
  }

  public void testNoSideEffectForHasNext() throws Exception {
    assertTrue(parser.hasNext());
    assertTrue(parser.hasNext());
    assertTrue(parser.hasNext());
    assertEquals("one", parser.next().getAsString());
    
    assertTrue(parser.hasNext());
    assertTrue(parser.hasNext());
    assertEquals("two", parser.next().getAsString());
    
    assertFalse(parser.hasNext());
    assertFalse(parser.hasNext());
  }

  public void testCallingNextBeyondAvailableInput() {
    parser.next();
    parser.next();
    try {
      parser.next();
      fail("Parser should not go beyond available input");
    } catch (NoSuchElementException expected) {
    }
  }
}
