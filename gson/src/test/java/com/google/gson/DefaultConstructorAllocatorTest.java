/*
 * Copyright (C) 2011 Google Inc.
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

import com.google.gson.DefaultConstructorAllocator;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Unit tests for the default constructor allocator class.
 *
 * @author Joel Leitch
 */
public class DefaultConstructorAllocatorTest extends TestCase {
  private DefaultConstructorAllocator allocator;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    allocator = new DefaultConstructorAllocator();
  }

  @SuppressWarnings("unchecked")
  public void testObjectConstructor() throws Exception {
    ArrayList<Object> arrayList = allocator.newInstance(ArrayList.class);
    assertTrue(arrayList.isEmpty());
    assertInCache(ArrayList.class);

    LinkedList<Object> linkedList = allocator.newInstance(LinkedList.class);
    assertTrue(linkedList.isEmpty());
    assertInCache(LinkedList.class);
  }

  public void testMissingDefaultConstructor() throws Exception {
    assertNull(allocator.newInstance(NoDefaultConstructor.class));
    assertInCache(NoDefaultConstructor.class);
  }

  private void assertInCache(Class<?> clazz) {
    assertNotNull(allocator.constructorCache.getElement(clazz));
  }

  private static class NoDefaultConstructor {
    @SuppressWarnings("unused")
    public NoDefaultConstructor(int i) {
      // do nothing
    }
  }
}
