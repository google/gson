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

import java.lang.reflect.Field;

import junit.framework.TestCase;

/**
 * Unit test for the {@link ExclusionStrategy2Adapter} class.
 *
 * @author Joel Leitch
 */
public class ExclusionStrategy2AdapterTest extends TestCase {

  public void testConstruction() throws Exception {
    try {
      new ExclusionStrategy2Adapter(null);
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  public void testAdapterDoesSameForBothModes() throws Exception {
    ExclusionStrategy2Adapter adapter =
        new ExclusionStrategy2Adapter(new MockExclusionStrategy(true, false));
    assertTrue(adapter.shouldSkipClass(String.class, Mode.DESERIALIZE));
    assertTrue(adapter.shouldSkipClass(String.class, Mode.SERIALIZE));

    Field f = String.class.getFields()[0];
    assertFalse(adapter.shouldSkipField(new FieldAttributes(String.class, f), Mode.DESERIALIZE));
    assertFalse(adapter.shouldSkipField(new FieldAttributes(String.class, f), Mode.SERIALIZE));
  }
}
