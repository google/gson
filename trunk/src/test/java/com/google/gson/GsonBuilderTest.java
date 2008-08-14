package com.google.gson;

import junit.framework.TestCase;

/**
 * Unit tests for {@link GsonBuilder}. 
 * 
 * @author Inderjeet Singh
 */
public class GsonBuilderTest extends TestCase {

  public void testCreatingMoreThanOnce() {
    GsonBuilder builder = new GsonBuilder();
    builder.create();
    try {
      builder.create();
      fail("GsonBuilder should not allow create() call more than once.");
    } catch (IllegalStateException e) {      
    }
  }
}
