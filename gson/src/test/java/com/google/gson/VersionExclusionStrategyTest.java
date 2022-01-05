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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.annotations.Since;
import com.google.gson.internal.Excluder;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Excluder} class.
 *
 * @author Joel Leitch
 */
class VersionExclusionStrategyTest {
  private static final double VERSION = 5.0D;

  @Test
  void testClassAndFieldAreAtSameVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION);
    assertFalse(excluder.excludeClass(MockObject.class, true));
    assertFalse(excluder.excludeField(MockObject.class.getField("someField"), true));
  }

  @Test
  void testClassAndFieldAreBehindInVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION + 1);
    assertFalse(excluder.excludeClass(MockObject.class, true));
    assertFalse(excluder.excludeField(MockObject.class.getField("someField"), true));
  }

  @Test
  void testClassAndFieldAreAheadInVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION - 1);
    assertTrue(excluder.excludeClass(MockObject.class, true));
    assertTrue(excluder.excludeField(MockObject.class.getField("someField"), true));
  }

  @Since(VERSION)
  private static class MockObject {

    @Since(VERSION)
    public final int someField = 0;
  }
}
