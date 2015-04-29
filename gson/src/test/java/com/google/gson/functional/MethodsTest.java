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
  private static final String OBJ_FOO_NOT_SERIALIZED_JSON = "{\"mFoo\":\"bar\"}";

  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder().allowGetterAndSetterMethods().create();
  }

  public void testSimpleDeserialization() throws Exception {
  	Simple obj = gson.fromJson(OBJ_JSON, Simple.class);
  	assertEquals(obj.getFoo(), "bar");
  }
  
  
  public void testSimpleSerialization() throws Exception {
  	Simple obj = new Simple("bar");
  	String result = gson.toJson(obj);
    assertEquals(OBJ_JSON, result);
  }
  
  public void testImplDeserialization() {
  	Impl obj = gson.fromJson(OBJ_JSON, Impl.class);
  	assertEquals(obj.getFoo(), "bar");
  }
  
  public void testImplSerialization() {
  	Impl obj = new Impl("bar");
  	String result = gson.toJson(obj);
    assertEquals(OBJ_JSON, result);
  }
  
  public void testImplWithExposedOverridesDeserialization() {
  	ImplWithExposedOverrides obj = gson.fromJson(OBJ_JSON, ImplWithExposedOverrides.class);
  	assertEquals(obj.getFoo(), "bar");
  }
  
  public void testImplWithExposedOverridesSerialization() {
  	ImplWithExposedOverrides obj = new ImplWithExposedOverrides("bar");
  	String result = gson.toJson(obj);
    assertEquals(OBJ_JSON, result);
  }

  public void testImplWithUnExposedOverridesDeserialization() {
  	ImplWithUnExposedOverrides obj = gson.fromJson(OBJ_JSON, ImplWithUnExposedOverrides.class);
  	assertNull(obj.getFoo());
  }
  
  public void testImplWithUnExposedOverridesSerialization() {
  	ImplWithUnExposedOverrides obj = new ImplWithUnExposedOverrides("bar");
  	String result = gson.toJson(obj);
    assertEquals(OBJ_FOO_NOT_SERIALIZED_JSON, result);
  }
  
  public static class Simple {
    private String foo;
    
    private Simple(String value) {
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
  
  public static interface IGetterSetter {
  	@GsonSetter
  	void setFoo(String value);
  	@GsonGetter
  	String getFoo();
  }
  
  public static class Impl implements IGetterSetter {
    private String foo;
    
    private Impl(String value) {
      this.foo = value;
    }
    
    public String getFoo() {
    	return this.foo;
    }
    
    public void setFoo(String value) {
    	this.foo = value;
    }
  }
  
  public static class ImplWithExposedOverrides implements IGetterSetter {
    private String foo;
    
    private ImplWithExposedOverrides(String value) {
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
  
  public static class ImplWithUnExposedOverrides implements IGetterSetter {
  	@Expose(serialize=false, deserialize=false)
    private String mFoo;
    
    private ImplWithUnExposedOverrides(String value) {
      this.mFoo = value;
    }
    
    @GsonGetter(exposed=false)
    public String getFoo() {
    	return this.mFoo;
    }
    
    @GsonSetter(exposed=false)
    public void setFoo(String value) {
    	this.mFoo = value;
    }
  }
  
}
