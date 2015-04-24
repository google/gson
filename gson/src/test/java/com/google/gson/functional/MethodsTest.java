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
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.GsonGetter;
import com.google.gson.annotations.GsonSetter;

import junit.framework.TestCase;

public class MethodsTest extends TestCase {
  private static final String OBJ_JSON = "{\"foo\":\"bar\"}";

  private Gson gson;
  private TestObject obj;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
    obj = new TestObject("bar");
  }

  public void testGetterAndSetterSerialization() throws Exception {
	TestObject obj = gson.fromJson(OBJ_JSON, TestObject.class);
	assertEquals(obj.getFoo(), "bar");
  }
  
  
  public void testGetterAndSetterDeserialization() throws Exception {
	String hmm = gson.toJson(obj);
    assertEquals(OBJ_JSON, gson.toJson(obj));
  }
  
  public static class TestObject {
    private String foo;
    
    private TestObject(String value) {
      this.foo = value;
    }
    
    @GsonGetter
    public String getFoo() {
    	return this.foo;
    }
    
    @GsonSetter 
    public void setFoo(String value) {
    	this.foo = value;
    }
  }
}
