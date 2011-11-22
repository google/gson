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
import java.lang.reflect.Field;
import junit.framework.TestCase;

/**
 * Unit tests for the {@link GsonExclusionStrategy} class.
 *
 * @author Joel Leitch
 */
public class VersionExclusionStrategyTest extends TestCase {
  private static final double VERSION = 5.0D;

  public void testClassAndFieldAreAtSameVersion() throws Exception {
    Class<MockObject> clazz = MockObject.class;
    Field f = clazz.getField("someField");
    GsonExclusionStrategy strategy = new GsonExclusionStrategy(VERSION, 0,
        true, true, true, false, false);
    assertFalse(strategy.shouldSkipClass(clazz));

    FieldAttributes fieldAttributes = new FieldAttributes(f);
    assertFalse(strategy.shouldSkipField(fieldAttributes));
  }

  public void testClassAndFieldAreBehindInVersion() throws Exception {
    Class<MockObject> clazz = MockObject.class;
    Field f = clazz.getField("someField");
    GsonExclusionStrategy strategy = new GsonExclusionStrategy(VERSION + 1, 0,
        true, true, true, false, false);
    assertFalse(strategy.shouldSkipClass(clazz));

    FieldAttributes fieldAttributes = new FieldAttributes(f);
    assertFalse(strategy.shouldSkipField(fieldAttributes));
  }

  public void testClassAndFieldAreAheadInVersion() throws Exception {
    Class<MockObject> clazz = MockObject.class;
    Field f = clazz.getField("someField");
    GsonExclusionStrategy strategy = new GsonExclusionStrategy(VERSION - 1, 0,
        true, true, true, false, false);
    assertTrue(strategy.shouldSkipClass(clazz));

    FieldAttributes fieldAttributes = new FieldAttributes(f);
    assertTrue(strategy.shouldSkipField(fieldAttributes));
  }

  @Since(VERSION)
  private static class MockObject {

    @SuppressWarnings("unused")
    @Since(VERSION)
    public final int someField = 0;
  }
}
