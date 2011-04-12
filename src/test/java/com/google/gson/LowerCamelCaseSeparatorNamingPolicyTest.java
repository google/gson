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
 * Tests for the {@link LowerCamelCaseSeparatorNamingPolicy} class.
 *
 * @author Joel Leitch
 */
public class LowerCamelCaseSeparatorNamingPolicyTest extends TestCase {

  private static final Class<String> CLASS = String.class;
  private static final String UNDERSCORE = "_";

  private LowerCamelCaseSeparatorNamingPolicy namingPolicy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    namingPolicy = new LowerCamelCaseSeparatorNamingPolicy(UNDERSCORE);
  }

  public void testNameBeginsWithLowerCase() throws Exception {
    String translatedName = namingPolicy.translateName("testNameBeginsWithLower", CLASS, null);
    assertEquals("test_name_begins_with_lower", translatedName);
  }

  public void testNameBeginsWithUpperCase() throws Exception {
    String translatedName = namingPolicy.translateName("TestNameBeginsWithUpper", CLASS, null);
    assertEquals("test_name_begins_with_upper", translatedName);
  }

  public void testExceptionPossiblyIncorrectSeparation() throws Exception {
    String translatedName = namingPolicy.translateName("aURL", CLASS, null);
    assertEquals("a_u_r_l", translatedName);
  }
  
  public void testUsingDashesInstead() throws Exception {
    namingPolicy = new LowerCamelCaseSeparatorNamingPolicy("-");
    String translatedName = namingPolicy.translateName("testUsingDashesInstead", CLASS, null);
    assertEquals("test-using-dashes-instead", translatedName);
  }
}
