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
import com.google.gson.internal.Excluder;
import junit.framework.TestCase;

/**
 * Unit tests for the {@link Excluder} class.
 *
 * @author Joel Leitch
 */
public class VersionExclusionStrategyTest extends TestCase {
  private static final double VERSION = 5.0D;

  public void testClassAndFieldAreAtSameVersion() throws Exception {
    this.testClassAndFieldWithVersionTemplate(new TestClassAndFieldWithVersionAssertFalse(), VERSION);
  }

  public void testClassAndFieldAreBehindInVersion() throws Exception {
    this.testClassAndFieldWithVersionTemplate(new TestClassAndFieldWithVersionAssertFalse(), VERSION + 1);
  }

  public void testClassAndFieldAreAheadInVersion() throws Exception {
    this.testClassAndFieldWithVersionTemplate(new TestClassAndFieldWithVersionAssertTrue(), VERSION - 1);
  }

  @Since(VERSION)
  private static class MockObject {

    @Since(VERSION)
    public final int someField = 0;
  }

  private void testClassAndFieldWithVersionTemplate(
          TestClassAndFieldWithVersionAssertionAdapter adapter, double version) throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(version);
    adapter.assertAction(excluder.excludeClass(MockObject.class, true));
    adapter.assertAction(excluder.excludeField(MockObject.class.getField("someField"), true));
  }

  interface TestClassAndFieldWithVersionAssertionAdapter {
    void assertAction(boolean b1);
  }

  class TestClassAndFieldWithVersionAssertFalse implements TestClassAndFieldWithVersionAssertionAdapter {
    public void assertAction(boolean b1) {
      assertFalse(b1);
    }
  }

  class TestClassAndFieldWithVersionAssertTrue implements TestClassAndFieldWithVersionAssertionAdapter {
    public void assertAction(boolean b1) {
      assertTrue(b1);
    }
  }
}
