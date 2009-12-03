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

import java.util.Stack;

/**
 * A stack data structure that only does a memory reference comparison
 * when looking for a particular item in the stack.  This stack does
 * not allow {@code null} values to be added.
 *
 * @author Joel Leitch
 */
final class MemoryRefStack {
  private final Stack<ObjectTypePair> stack = new Stack<ObjectTypePair>();

  /**
   * Adds a new element to the top of the stack.
   *
   * @param obj the object to add to the stack
   * @return the object that was added
   */
  public ObjectTypePair push(ObjectTypePair obj) {
    Preconditions.checkNotNull(obj);

    return stack.push(obj);
  }

  /**
   * Removes the top element from the stack.
   *
   * @return the element being removed from the stack
   * @throws java.util.EmptyStackException thrown if the stack is empty
   */
  public ObjectTypePair pop() {
    return stack.pop();
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }

  /**
   * Retrieves the item from the top of the stack, but does not remove it.
   *
   * @return the item from the top of the stack
   * @throws java.util.EmptyStackException thrown if the stack is empty
   */
  public ObjectTypePair peek() {
    return stack.peek();
  }

  /**
   * Performs a memory reference check to see it the {@code obj} exists in
   * the stack.
   *
   * @param obj the object to search for in the stack
   * @return true if this object is already in the stack otherwise false
   */
  public boolean contains(ObjectTypePair obj) {
    if (obj == null) {
      return false;
    }

    for (ObjectTypePair stackObject : stack) {
      if (stackObject.getObject() == obj.getObject()
          && stackObject.type.equals(obj.type) ) {
        return true;
      }
    }
    return false;
  }
}
