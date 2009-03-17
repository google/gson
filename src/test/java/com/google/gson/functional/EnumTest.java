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

package com.google.gson.functional;

import com.google.gson.Gson;

import junit.framework.TestCase;

/**
 * Functional tests for Java 5.0 enums.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class EnumTest extends TestCase {
  
  private Gson gson;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }
  
  public void testEnumSerialization() throws Exception {
    String result = gson.toJson(TestEnum.TEST_1);
    assertEquals('"' + TestEnum.TEST_1.toString() + '"', result);
  }
  
  public void testEnumDeserialization() throws Exception {
    TestEnum result = gson.fromJson('"' + TestEnum.TEST_1.toString() + '"', TestEnum.class);
    assertEquals(TestEnum.TEST_1, result);
  }
  
  public void testEnumFieldSerialization() throws Exception {
    String result = gson.toJson(TestEnum.TEST_1);
    assertEquals('"' + TestEnum.TEST_1.toString() + '"', result);
  }
  
  public void testEnumFieldDeserialization() throws Exception {
    Foo result = gson.fromJson("{\"f\":\"TEST_1\"}", Foo.class);
    assertEquals(TestEnum.TEST_1, result.f);
  }

  private static enum TestEnum {
    TEST_1,
    TEST_2;
  }
  
  private static class Foo {
    private TestEnum f;
    
    public Foo() {
      this(TestEnum.TEST_1);
    }
    
    public Foo(TestEnum f) {
      this.f = f;
    }
  }
}
