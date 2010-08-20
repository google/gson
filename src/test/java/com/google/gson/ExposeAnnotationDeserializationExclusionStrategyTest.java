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

import java.lang.reflect.Field;

import junit.framework.TestCase;

import com.google.gson.annotations.Expose;

/**
 * Unit tests for the {@link ExposeAnnotationDeserializationExclusionStrategy} class.
 *
 * @author Joel Leitch
 */
public class ExposeAnnotationDeserializationExclusionStrategyTest extends TestCase {
  private ExposeAnnotationDeserializationExclusionStrategy strategy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    strategy = new ExposeAnnotationDeserializationExclusionStrategy();
  }

  public void testNeverSkipClasses() throws Exception {
    assertFalse(strategy.shouldSkipClass(MockObject.class));
  }

  public void testSkipNonAnnotatedFields() throws Exception {
    Field f = MockObject.class.getField("hiddenField");
    assertTrue(strategy.shouldSkipField(new FieldAttributes(MockObject.class, f)));
  }

  public void testSkipExplicitlySkippedFields() throws Exception {
    Field f = MockObject.class.getField("explicitlyHiddenField");
    assertTrue(strategy.shouldSkipField(new FieldAttributes(MockObject.class, f)));
  }

  public void testNeverSkipExposedAnnotatedFields() throws Exception {
    Field f = MockObject.class.getField("exposedField");
    assertFalse(strategy.shouldSkipField(new FieldAttributes(MockObject.class, f)));
  }

  public void testNeverSkipExplicitlyExposedAnnotatedFields() throws Exception {
    Field f = MockObject.class.getField("explicitlyExposedField");
    assertFalse(strategy.shouldSkipField(new FieldAttributes(MockObject.class, f)));
  }

  @SuppressWarnings("unused")
  private static class MockObject {
    @Expose
    public final int exposedField = 0;

    @Expose(deserialize=true)
    public final int explicitlyExposedField = 0;

    @Expose(deserialize=false)
    public final int explicitlyHiddenField = 0;

    public final int hiddenField = 0;
  }
}
