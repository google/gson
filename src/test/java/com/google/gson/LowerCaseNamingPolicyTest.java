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

import com.google.gson.LowerCaseNamingPolicy;

import junit.framework.TestCase;

/**
 * Tests for the {@link LowerCaseNamingPolicy} class.
 *
 * @author Joel Leitch
 */
public class LowerCaseNamingPolicyTest extends TestCase {
  private static final String ALL_LOWER = "abcdefg";
  private static final String ALL_UPPER = "ABCDEFG";
  private static final String MIXED = "aBcdeFg";

  private LowerCaseNamingPolicy namingPolicy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    namingPolicy = new LowerCaseNamingPolicy();
  }

  public void testAllLowerCase() throws Exception {
    assertEquals(ALL_LOWER, namingPolicy.translateName(ALL_LOWER, String.class, null));
  }

  public void testAllUpperCase() throws Exception {
    assertEquals(ALL_LOWER, namingPolicy.translateName(ALL_UPPER, String.class, null));
  }

  public void testMixedCase() throws Exception {
    assertEquals(ALL_LOWER, namingPolicy.translateName(MIXED, String.class, null));
  }
}
