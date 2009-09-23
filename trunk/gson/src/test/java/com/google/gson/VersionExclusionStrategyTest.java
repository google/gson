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

import com.google.gson.annotations.Since;

import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * Unit tests for the {@link VersionExclusionStrategy} class.
 *
 * @author Joel Leitch
 */
public class VersionExclusionStrategyTest extends TestCase {
  private static final double VERSION = 5.0D;

  public void testDisallowNegativeValuesAndFailFast() throws Exception {
    try {
      new VersionExclusionStrategy(-1.0D);
      fail("should have thrown an exception.");
    } catch (IllegalArgumentException expected) { }
  }

  public void testClassAndFieldAreAtSameVersion() throws Exception {
    Class<MockObject> clazz = MockObject.class;
    Field f = clazz.getField("someField");
    VersionExclusionStrategy strategy = new VersionExclusionStrategy(VERSION);
    
    assertFalse(strategy.shouldSkipClass(clazz));
    assertFalse(strategy.shouldSkipField(f));
  }

  public void testClassAndFieldAreBehindInVersion() throws Exception {
    Class<MockObject> clazz = MockObject.class;
    Field f = clazz.getField("someField");
    VersionExclusionStrategy strategy = new VersionExclusionStrategy(VERSION + 1);
    
    assertFalse(strategy.shouldSkipClass(clazz));
    assertFalse(strategy.shouldSkipField(f));
  }

  public void testClassAndFieldAreAheadInVersion() throws Exception {
    Class<MockObject> clazz = MockObject.class;
    Field f = clazz.getField("someField");
    VersionExclusionStrategy strategy = new VersionExclusionStrategy(VERSION - 1);
    
    assertTrue(strategy.shouldSkipClass(clazz));
    assertTrue(strategy.shouldSkipField(f));
  }

  @Since(VERSION)
  private static class MockObject {

    @SuppressWarnings("unused")
    @Since(VERSION)
    private final int someField = 0;
  }
}
