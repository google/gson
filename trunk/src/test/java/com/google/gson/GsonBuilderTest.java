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
    builder.create();
  }
}
