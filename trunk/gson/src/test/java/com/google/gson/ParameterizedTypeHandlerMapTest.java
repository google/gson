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

import com.google.gson.common.TestTypes.Base;
import com.google.gson.common.TestTypes.Sub;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Unit tests for the {@link ParameterizedTypeHandlerMap} class.
 *
 * @author Joel Leitch
 */
public class ParameterizedTypeHandlerMapTest extends TestCase {
  private ParameterizedTypeHandlerMap<String> paramMap;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    paramMap = new ParameterizedTypeHandlerMap<String>();
  }

  public void testNullMap() throws Exception {
    assertFalse(paramMap.hasSpecificHandlerFor(String.class));
    assertNull(paramMap.getHandlerFor(String.class));
    assertNull(paramMap.getHandlerFor(String.class));
  }

  public void testHasGenericButNotSpecific() throws Exception {
    Type specificType = new TypeToken<List<String>>() {}.getType();
    String handler = "blah";
    paramMap.register(List.class, handler);

    assertFalse(paramMap.hasSpecificHandlerFor(specificType));
    assertTrue(paramMap.hasSpecificHandlerFor(List.class));
    assertNotNull(paramMap.getHandlerFor(specificType));
    assertNotNull(paramMap.getHandlerFor(List.class));
    assertEquals(handler, paramMap.getHandlerFor(specificType));
  }

  public void testHasSpecificType() throws Exception {
    Type specificType = new TypeToken<List<String>>() {}.getType();
    String handler = "blah";
    paramMap.register(specificType, handler);

    assertTrue(paramMap.hasSpecificHandlerFor(specificType));
    assertFalse(paramMap.hasSpecificHandlerFor(List.class));
    assertNotNull(paramMap.getHandlerFor(specificType));
    assertNull(paramMap.getHandlerFor(List.class));
    assertEquals(handler, paramMap.getHandlerFor(specificType));
  }

  public void testTypeOverridding() throws Exception {
    String handler1 = "blah1";
    String handler2 = "blah2";
    paramMap.register(String.class, handler1);
    paramMap.register(String.class, handler2);

    assertTrue(paramMap.hasSpecificHandlerFor(String.class));
    assertEquals(handler2, paramMap.getHandlerFor(String.class));
  }

  public void testMakeUnmodifiable() throws Exception {
    paramMap.makeUnmodifiable();
    try {
     paramMap.register(String.class, "blah");
     fail("Can not register handlers when map is unmodifiable");
    } catch (IllegalStateException expected) { }
  }

  public void testTypeHierarchy() {
    paramMap.registerForTypeHierarchy(Base.class, "baseHandler");
    String handler = paramMap.getHandlerFor(Sub.class);
    assertEquals("baseHandler", handler);
  }

  public void testTypeHierarchyMultipleHandlers() {
    paramMap.registerForTypeHierarchy(Base.class, "baseHandler");
    paramMap.registerForTypeHierarchy(Sub.class, "subHandler");
    String handler = paramMap.getHandlerFor(SubOfSub.class);
    assertEquals("subHandler", handler);
  }

  public void testTypeHierarchyRegisterIfAbsent() {
    paramMap.registerForTypeHierarchy(Base.class, "baseHandler");
    ParameterizedTypeHandlerMap<String> otherMap = new ParameterizedTypeHandlerMap<String>();
    otherMap.registerForTypeHierarchy(Base.class, "baseHandler2");
    paramMap.registerIfAbsent(otherMap);
    String handler = paramMap.getHandlerFor(Base.class);
    assertEquals("baseHandler", handler);
  }

  public void testReplaceExistingTypeHierarchyHandler() {
    paramMap.registerForTypeHierarchy(Base.class, "baseHandler");
    paramMap.registerForTypeHierarchy(Base.class, "base2Handler");
    String handler = paramMap.getHandlerFor(Base.class);
    assertEquals("base2Handler", handler);
  }

  public void testHidingExistingTypeHierarchyHandlerIsDisallowed() {
    paramMap.registerForTypeHierarchy(Sub.class, "subHandler");
    try {
      paramMap.registerForTypeHierarchy(Base.class, "baseHandler");
      fail("A handler that hides an existing type hierarchy handler is not allowed");
    } catch (IllegalArgumentException expected) {
    }
  }
  private static class SubOfSub extends Sub {
  }
}
