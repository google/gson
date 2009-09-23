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

import com.google.gson.ExposeAnnotationBasedExclusionStrategy.Phase;
import com.google.gson.annotations.Expose;

import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * Unit tests for the {@link ExposeAnnotationBasedExclusionStrategy} class.
 *
 * @author Joel Leitch
 */
public class ExposeAnnotationBasedExclusionStrategyTest extends TestCase {
  private ExposeAnnotationBasedExclusionStrategy strategy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    strategy = new ExposeAnnotationBasedExclusionStrategy(Phase.SERIALIZATION);
  }

  public void testNeverSkipClasses() throws Exception {
    assertFalse(strategy.shouldSkipClass(MockObject.class));
  }
  
  public void testSkipNonAnnotatedFields() throws Exception {
    Field f = MockObject.class.getField("hiddenField");
    assertTrue(strategy.shouldSkipField(f));
  }
  
  public void testNeverSkipExposedAnnotatedFields() throws Exception {
    Field f = MockObject.class.getField("exposedField");
    assertFalse(strategy.shouldSkipField(f));
  }

  @SuppressWarnings("unused")
  private static class MockObject {
    @Expose
    private final int exposedField = 0;
    private final int hiddenField = 0;
  }
}
