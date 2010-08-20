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
 * Tests for the {@link CamelCaseSeparatorNamingPolicy} class.
 *
 * @author Joel Leitch
 */
public class CamelCaseSeparatorNamingPolicyTest extends TestCase {

  private static final Class<String> CLASS = String.class;
  private static final String UNDERSCORE = "_";
  private static final String MULTI_CHAR_SEPARATOR = "_$_";

  public void testInvalidInstantiation() throws Exception {
    try {
      new CamelCaseSeparatorNamingPolicy(null);
      fail("Null separator string is not supported");
    } catch (IllegalArgumentException expected) { }

    try {
      new CamelCaseSeparatorNamingPolicy("");
      fail("Empty separator string is not supported");
    } catch (IllegalArgumentException expected) { }
  }

  public void testUnderscoreSeparator() throws Exception {
    CamelCaseSeparatorNamingPolicy namingPolicy =
        new CamelCaseSeparatorNamingPolicy(UNDERSCORE);
    String translatedName = namingPolicy.translateName("testUnderscoreBetweenWords", CLASS, null);
    assertEquals("test_Underscore_Between_Words", translatedName);
  }

  public void testMultiCharSeparator() throws Exception {
    CamelCaseSeparatorNamingPolicy namingPolicy =
        new CamelCaseSeparatorNamingPolicy(MULTI_CHAR_SEPARATOR);
    String translatedName = namingPolicy.translateName("testMultCharBetweenWords", CLASS, null);
    assertEquals("test_$_Mult_$_Char_$_Between_$_Words", translatedName);
  }

  public void testNameBeginsWithCapital() throws Exception {
    CamelCaseSeparatorNamingPolicy namingPolicy = new CamelCaseSeparatorNamingPolicy(UNDERSCORE);
    String translatedName = namingPolicy.translateName("TestNameBeginsWithCapital", CLASS, null);
    assertEquals("Test_Name_Begins_With_Capital", translatedName);
  }

  public void testExceptionPossiblyIncorrectSeparation() throws Exception {
    CamelCaseSeparatorNamingPolicy namingPolicy = new CamelCaseSeparatorNamingPolicy(UNDERSCORE);
    String translatedName = namingPolicy.translateName("aURL", CLASS, null);
    assertEquals("a_U_R_L", translatedName);
  }
}
