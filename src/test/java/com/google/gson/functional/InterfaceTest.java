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
 * Functional tests involving interfaces.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class InterfaceTest extends TestCase {
  private static final String OBJ_JSON = "{\"someStringValue\":\"StringValue\"}";

  private Gson gson;
  private TestObject obj;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
    obj = new TestObject("StringValue");
  }

  public void testSerializingObjectImplementingInterface() throws Exception {
    assertEquals(OBJ_JSON, gson.toJson(obj));
  }
  
  public void testSerializingInterfaceObjectField() throws Exception {
    TestObjectWrapper objWrapper = new TestObjectWrapper(obj);
    assertEquals("{\"obj\":" + OBJ_JSON + "}", gson.toJson(objWrapper));
  }

  private static interface TestObjectInterface {
    // Holder
  }
  
  private static class TestObject implements TestObjectInterface {
    @SuppressWarnings("unused")
    private String someStringValue;
    
    private TestObject(String value) {
      this.someStringValue = value;
    }
  }

  private static class TestObjectWrapper {
    @SuppressWarnings("unused")
    private TestObjectInterface obj;
    
    private TestObjectWrapper(TestObjectInterface obj) {
      this.obj = obj;
    }
  }
}
