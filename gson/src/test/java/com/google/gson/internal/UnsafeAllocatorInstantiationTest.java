/*
 * Copyright (C) 2016 Google Inc.
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
package com.google.gson.internal;

import junit.framework.TestCase;

/**
 * Test unsafe allocator instantiation
 * @author Ugljesa Jovanovic
 */
public final class UnsafeAllocatorInstantiationTest extends TestCase {

  public interface Interface {
  }

  public static abstract class AbstractClass {
  }

  public static class ConcreteClass {
  }

  /**
   * Ensure that the {@link java.lang.UnsupportedOperationException} is thrown when  trying
   * to instantiate an interface
   */
  public void testInterfaceInstantiation() {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    try {
      unsafeAllocator.newInstance(Interface.class);
      fail();
    } catch (Exception e) {
      assertEquals(e.getClass(), UnsupportedOperationException.class);
    }
  }

  /**
   * Ensure that the {@link java.lang.UnsupportedOperationException} is thrown when  trying
   * to instantiate an abstract class
   */
  public void testAbstractClassInstantiation() {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    try {
      unsafeAllocator.newInstance(AbstractClass.class);
      fail();
    } catch (Exception e) {
      assertEquals(e.getClass(), UnsupportedOperationException.class);
    }
  }

  /**
   * Ensure that no exception is thrown when trying to instantiate a concrete class
   */
  public void testConcreteClassInstantiation() {
    UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
    try {
      unsafeAllocator.newInstance(ConcreteClass.class);
    } catch (Exception e) {
      fail();
    }
  }
}
