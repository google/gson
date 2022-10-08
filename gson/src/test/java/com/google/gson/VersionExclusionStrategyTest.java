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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.gson.internal.Excluder;
import org.junit.Test;

/**
 * Unit tests for the {@link Excluder} class.
 *
 * @author Joel Leitch
 */
public class VersionExclusionStrategyTest {
  private static final double VERSION = 5.0D;

  @Test
  public void testSameVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION);
    assertFalse(excluder.excludeClass(MockClassSince.class, true));
    assertFalse(excluder.excludeField(MockClassSince.class.getField("someField"), true));

    // Until version is exclusive
    assertTrue(excluder.excludeClass(MockClassUntil.class, true));
    assertTrue(excluder.excludeField(MockClassUntil.class.getField("someField"), true));

    assertFalse(excluder.excludeClass(MockClassBoth.class, true));
    assertFalse(excluder.excludeField(MockClassBoth.class.getField("someField"), true));
  }

  @Test
  public void testNewerVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION + 5);
    assertFalse(excluder.excludeClass(MockClassSince.class, true));
    assertFalse(excluder.excludeField(MockClassSince.class.getField("someField"), true));

    assertTrue(excluder.excludeClass(MockClassUntil.class, true));
    assertTrue(excluder.excludeField(MockClassUntil.class.getField("someField"), true));

    assertTrue(excluder.excludeClass(MockClassBoth.class, true));
    assertTrue(excluder.excludeField(MockClassBoth.class.getField("someField"), true));
  }

  @Test
  public void testOlderVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION - 5);
    assertTrue(excluder.excludeClass(MockClassSince.class, true));
    assertTrue(excluder.excludeField(MockClassSince.class.getField("someField"), true));

    assertFalse(excluder.excludeClass(MockClassUntil.class, true));
    assertFalse(excluder.excludeField(MockClassUntil.class.getField("someField"), true));

    assertTrue(excluder.excludeClass(MockClassBoth.class, true));
    assertTrue(excluder.excludeField(MockClassBoth.class.getField("someField"), true));
  }

  @Since(VERSION)
  private static class MockClassSince {

    @Since(VERSION)
    public final int someField = 0;
  }

  @Until(VERSION)
  private static class MockClassUntil {

    @Until(VERSION)
    public final int someField = 0;
  }

  @Since(VERSION)
  @Until(VERSION + 2)
  private static class MockClassBoth {

    @Since(VERSION)
    @Until(VERSION + 2)
    public final int someField = 0;
  }
}
