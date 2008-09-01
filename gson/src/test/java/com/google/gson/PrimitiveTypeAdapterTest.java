/*
 * Copyright (C) 2008 Google Inc.
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

/**
 * Performs some unit testing for the {@link PrimitiveTypeAdapter} class.
 *
 * @author Joel Leitch
 */
public class PrimitiveTypeAdapterTest extends TestCase {
  private PrimitiveTypeAdapter typeAdapter;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    typeAdapter = new PrimitiveTypeAdapter();
  }

  public void testImproperConversion() throws Exception {
    double someValue = 1.0;
    try {
      typeAdapter.adaptType(someValue, String.class);
      fail("Should not be able to convert incompatible types.");
    } catch (JsonParseException expected) { }
  }

  public void testImproperCharacterConversion() throws Exception {
    String someValue = "test123";
    try {
      typeAdapter.adaptType(someValue, Character.class);
      fail("Should not be able to convert incompatible types.");
    } catch (JsonParseException expected) { }
  }

  public void testProperPrimitiveConversions() throws Exception {
    String stringValue = "1.0";
    Double actualValue = typeAdapter.adaptType(stringValue, Double.class);
    assertEquals(1.0, actualValue.doubleValue(), 0.0001);

    Double doubleValue = 1.0;
    actualValue = typeAdapter.adaptType(doubleValue, Double.class);
    assertEquals(1.0, actualValue.doubleValue(), 0.0001);

    stringValue = "A";
    Character actualCharacter = typeAdapter.adaptType(stringValue, Character.class);
    assertEquals('A', actualCharacter.charValue());
  }

  public void testProperEnumConversions() throws Exception {
    TestEnum expected = TestEnum.TEST1;
    TestEnum actual = typeAdapter.adaptType(expected, TestEnum.class);
    assertEquals(expected, actual);
  }

  private static enum TestEnum {
    TEST1, TEST2, TEST3
  }
}
