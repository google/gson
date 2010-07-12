// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.gson.webservice.definition;

import junit.framework.TestCase;

/**
 * Unit tests for {@link TypedKey}
 *
 * @author inder
 */
public class TypedKeyTest extends TestCase {

  public void testEqualsForSameName() {
    assertEquals(new TypedKey<String>("name", String.class),
        new TypedKey<String>(new String("name"), String.class));
  }

  public void testEqualsFailsForDifferentClasses() {
    assertFalse(new TypedKey<Object>("name", Object.class).equals(
        new TypedKey<String>("name", String.class)));
  }
}
