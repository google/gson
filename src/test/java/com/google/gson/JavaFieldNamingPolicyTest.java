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
 * Tests for the {@link JavaFieldNamingPolicy} class.
 *
 * @author Joel Leitch
 */
public class JavaFieldNamingPolicyTest extends TestCase {

  private JavaFieldNamingPolicy namingPolicy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    namingPolicy = new JavaFieldNamingPolicy();
  }

  public void testFieldNamingPolicy() throws Exception {
    FieldAttributes f = new FieldAttributes(String.class, String.class.getFields()[0]);
    assertEquals(f.getName(), namingPolicy.translateName(f));
  }

  public void testNullField() throws Exception {
    try {
      namingPolicy.translateName((FieldAttributes) null);
      fail("Should have thrown an exception");
    } catch (IllegalArgumentException expected) { }
  }
}
