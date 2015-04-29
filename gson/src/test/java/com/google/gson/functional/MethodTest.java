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
import com.google.gson.annotations.GsonGetter;
import com.google.gson.annotations.GsonSetter;

import junit.framework.TestCase;

public class MethodTest extends TestCase {
  private static final String OBJ_JSON = "{\"foo\":\"bar\"}";
  private static final String OBJ_FOO_NOT_SERIALIZED_JSON = "{\"mFoo\":\"bar\"}";
  private static final String OBJ_EMPTY_JSON = "{}";

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
 
  public void testPoorlyNamedGetter() {
  	PoorlyNamedGetter obj = new PoorlyNamedGetter();
  	try {
  		gson.toJson(obj);
  	} catch (IllegalArgumentException e) {
  		return;
  	}
  	fail();
  }
  
  public void testPoorlyNamedSetter() {
  	PoorlyNamedSetter obj = new PoorlyNamedSetter();
  	try {
  		gson.toJson(obj);
  	} catch (IllegalArgumentException e) {
  		return;
  	}
  	fail();
  }
  
  public void testGetterThatReturnsVoid() {
  	GetterThatReturnsVoid obj = new GetterThatReturnsVoid();
  	try {
  		gson.toJson(obj);
  	} catch (IllegalArgumentException e) {
  		return;
  	}
  	fail();
  }
  
  public void testGetterWithParameters() {
  	GetterWithParameters obj = new GetterWithParameters();
  	try {
  		gson.toJson(obj);
  	} catch (IllegalArgumentException e) {
  		return;
  	}
  	fail();
  }
  
  public void testSetterWithNotExactlyOneParameter() {
  	SetterWithNotExactlyOneParameter obj = new SetterWithNotExactlyOneParameter();
  	try {
  		gson.toJson(obj);
  	} catch (IllegalArgumentException e) {
  		return;
  	}
  	fail();
  }
  
  public void testMethodWithGsonGetterAndGsonSetter() {
  	MethodWithGsonGetterAndGsonSetter obj = new MethodWithGsonGetterAndGsonSetter();
  	try {
  		gson.toJson(obj);
  	} catch (IllegalArgumentException e) {
  		return;
  	}
  	fail();

  }
  
  public void testGetterThatThrowsAnException() {
  	GetterThatThrowsException	obj = new GetterThatThrowsException();
  	try {
  		gson.toJson(obj);
  	} catch (RuntimeException e) {
  		return;
  	}
  	fail();
  }
  
  public void testSetterThatThrowsAnException() {
  	try {
  		gson.fromJson(OBJ_JSON, SetterThatThrowsException.class);
  	} catch (RuntimeException e) {
  		return;
  	}
  	fail();	
  }
  
  public void testInheritedMethodsDeserialization() throws Exception {
  	InheritedMethods obj = gson.fromJson(OBJ_JSON, InheritedMethods.class);
  	assertEquals(obj.getFoo(), "bar");
  }
  
  
  public void testInheritedMethodsSerialization() throws Exception {
  	InheritedMethods obj = new InheritedMethods("bar");
  	String result = gson.toJson(obj);
    assertEquals(OBJ_JSON, result);
  }
  
  public void testSkipMethodsDeserialization() {
    Gson gsonNoMethods = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  	Simple obj = gsonNoMethods.fromJson(OBJ_JSON, Simple.class);
  	assertNull(obj.getFoo());
  }
  
  
  public void testSkipMethodsSerialization() {
    Gson gsonNoMethods = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  	Simple obj = new Simple("bar");
  	String result = gsonNoMethods.toJson(obj);
    assertEquals(OBJ_EMPTY_JSON, result);
  }
  
  static class Simple {
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
  
  static class Impl implements IGetterSetter {
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
  
  static class ImplWithExposedOverrides implements IGetterSetter {
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
  
  static class ImplWithUnExposedOverrides implements IGetterSetter {
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
  
  static class InheritedMethods extends Simple {

  	private InheritedMethods(String value) {
  		super(value);
  	}

  }
  
  static class PoorlyNamedGetter {

  	@GsonGetter
  	public String gestFoo() {
  		return "bar";
  	}

  }
  
  static class PoorlyNamedSetter {
  	
  	@GsonSetter
  	public void sestFoo(String value) { }

  }
  
  static class GetterThatReturnsVoid {
  	
  	@GsonGetter
  	public void getFoo() { }
  	
  }
  
  static class SetterWithNotExactlyOneParameter {
  	
  	@GsonSetter
  	public void setFoo(Object bar, Object baz) { }
  	
  }
  
  static class GetterWithParameters {
  	
  	@GsonGetter
  	public String getFoo(Object bar) {
  		return "bar";
  	}
  	
  }
  
  static class MethodWithGsonGetterAndGsonSetter {
  
  	@GsonGetter
  	@GsonSetter
  	public String getFoo() { 
  		return "bar";
  	}
  	
  }
  
  static class GetterThatThrowsException {

  	@GsonGetter
  	public String getFoo() {
  		throw new RuntimeException("bar");
  	}

  }
 
  static class SetterThatThrowsException {
  	
  	@GsonSetter
  	public void setFoo(String value) {
  		throw new RuntimeException("bar");
  	}
  }
}
