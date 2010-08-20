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

import com.google.gson.MemoryRefStack;

import junit.framework.TestCase;

import java.util.EmptyStackException;

/**
 * Unit tests for the {@link MemoryRefStack} class.
 *
 * @author Joel Leitch
 */
public class MemoryRefStackTest extends TestCase {
  private MemoryRefStack stack;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    stack = new MemoryRefStack();
  }

  public void testPeekEmptyStack() throws Exception {
    try {
      stack.peek();
    } catch (EmptyStackException expected) { }
  }

  public void testPushPeekAndPop() throws Exception {
    ObjectTypePair obj = new ObjectTypePair(this, getClass(), true);

    assertEquals(obj, stack.push(obj));
    assertEquals(obj, stack.peek());
    assertEquals(obj, stack.pop());
  }

  public void testPopTooMany() throws Exception {
    ObjectTypePair obj = new ObjectTypePair(this, getClass(), true);
    stack.push(obj);
    assertEquals(obj, stack.pop());

    try {
      stack.pop();
    } catch (EmptyStackException expected) { }
  }

  public void testContains() throws Exception {
    MockObject objA = new MockObject();
    MockObject objB = new MockObject();
    assertEquals(objA, objB);
    stack.push(new ObjectTypePair(objA, MockObject.class, true));
    assertTrue(stack.contains(new ObjectTypePair(objA, MockObject.class, true)));
    assertFalse(stack.contains(new ObjectTypePair(objB, MockObject.class, true)));
  }

  private static class MockObject {
    private final int value = 1;

    @Override
    public boolean equals(Object obj) {
      return obj instanceof MockObject && value == ((MockObject) obj).value;
    }

    @Override
    public int hashCode() {
      return value;
    }
  }
}
