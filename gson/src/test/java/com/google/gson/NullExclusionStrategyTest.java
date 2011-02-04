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

import junit.framework.TestCase;

/**
 * Unit test for the {@link NullExclusionStrategy} class.
 *
 * @author Joel Leitch
 */
public class NullExclusionStrategyTest extends TestCase {
  private NullExclusionStrategy strategy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    strategy = new NullExclusionStrategy();
  }

  public void testNeverSkipsClass() throws Exception {
    assertFalse(strategy.shouldSkipClass(String.class));
  }

  public void testNeverSkipsField() throws Exception {
    assertFalse(strategy.shouldSkipField(
        new FieldAttributes(String.class, String.class.getFields()[0])));
  }
}
