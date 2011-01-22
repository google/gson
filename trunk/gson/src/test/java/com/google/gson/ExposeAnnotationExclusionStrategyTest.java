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

import com.google.gson.annotations.Expose;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link ExposeAnnotationExclusionStrategy} class.
 *
 * @author Joel Leitch
 */
public class ExposeAnnotationExclusionStrategyTest extends TestCase {
  private ExposeAnnotationExclusionStrategy strategy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    strategy = new ExposeAnnotationExclusionStrategy();
  }

  public void testNeverSkipClasses() throws Exception {
    assertFalse(strategy.shouldSkipClass(MockObject.class, Mode.DESERIALIZE));
    assertFalse(strategy.shouldSkipClass(MockObject.class, Mode.SERIALIZE));
  }

  public void testSkipNonAnnotatedFields() throws Exception {
    FieldAttributes f = createFieldAttributes("hiddenField");
    assertTrue(strategy.shouldSkipField(f, Mode.DESERIALIZE));
    assertTrue(strategy.shouldSkipField(f, Mode.SERIALIZE));
  }

  public void testSkipExplicitlySkippedFields() throws Exception {
    FieldAttributes f = createFieldAttributes("explicitlyHiddenField");
    assertTrue(strategy.shouldSkipField(f, Mode.DESERIALIZE));
    assertTrue(strategy.shouldSkipField(f, Mode.SERIALIZE));
  }

  public void testNeverSkipExposedAnnotatedFields() throws Exception {
    FieldAttributes f = createFieldAttributes("exposedField");
    assertFalse(strategy.shouldSkipField(f, Mode.DESERIALIZE));
    assertFalse(strategy.shouldSkipField(f, Mode.SERIALIZE));
  }

  public void testNeverSkipExplicitlyExposedAnnotatedFields() throws Exception {
    FieldAttributes f = createFieldAttributes("explicitlyExposedField");
    assertFalse(strategy.shouldSkipField(f, Mode.DESERIALIZE));
    assertFalse(strategy.shouldSkipField(f, Mode.SERIALIZE));
  }
  
  public void testDifferentSerializeAndDeserializeField() throws Exception {
    FieldAttributes f = createFieldAttributes("explicitlyDifferentModeField");
    assertTrue(strategy.shouldSkipField(f, Mode.DESERIALIZE));
    assertFalse(strategy.shouldSkipField(f, Mode.SERIALIZE));
  }
  
  private static FieldAttributes createFieldAttributes(String fieldName) throws Exception {
    Field f = MockObject.class.getField(fieldName);
    return new FieldAttributes(MockObject.class, f);
  }
  
  @SuppressWarnings("unused")
  private static class MockObject {
    @Expose
    public final int exposedField = 0;

    @Expose(serialize=true, deserialize=true)
    public final int explicitlyExposedField = 0;

    @Expose(serialize=false, deserialize=false)
    public final int explicitlyHiddenField = 0;
    
    @Expose(serialize=true, deserialize=false)
    public final int explicitlyDifferentModeField = 0;

    public final int hiddenField = 0;
  }
}
