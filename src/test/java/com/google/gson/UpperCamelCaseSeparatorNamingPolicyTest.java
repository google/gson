/*
 * Copyright (C) 2010 Google Inc.
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
 * Tests for the {@link UpperCamelCaseSeparatorNamingPolicy} class.
 *
 * @author Joel Leitch
 */
public class UpperCamelCaseSeparatorNamingPolicyTest extends TestCase {

  private UpperCamelCaseSeparatorNamingPolicy namingPolicy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    namingPolicy = new UpperCamelCaseSeparatorNamingPolicy(" ");
  }

  public void testAllLowerCase() throws Exception {
    assertEquals("Some Field Name", 
        namingPolicy.translateName("someFieldName", String.class, null));
  }

  public void testAllUpperCase() throws Exception {
    assertEquals("U R L", 
        namingPolicy.translateName("URL", String.class, null));
  }
  
  public void testAllUpperCaseExceptFirst() throws Exception {
    assertEquals("U R L", 
        namingPolicy.translateName("uRL", String.class, null));
  }
  
  public void testAllUpperCaseStartingWithUnderscore() throws Exception {
    assertEquals("_U R L", 
        namingPolicy.translateName("_uRL", String.class, null));
  }

  public void testMixedCase() throws Exception {
    assertEquals("_Some Field Name", 
        namingPolicy.translateName("_someFieldName", String.class, null));
  }
}
