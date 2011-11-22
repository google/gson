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

import com.google.gson.annotations.Expose;

import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * Unit tests for GsonBuilder.REQUIRE_EXPOSE_DESERIALIZE.
 *
 * @author Joel Leitch
 */
public class ExposeAnnotationExclusionStrategyTest extends TestCase {
  private ExclusionStrategy serializationStrategy = new GsonExclusionStrategy(
      GsonExclusionStrategy.IGNORE_VERSIONS, 0, true, true, true, true, false);
  private ExclusionStrategy deserializationStrategy = new GsonExclusionStrategy(
      GsonExclusionStrategy.IGNORE_VERSIONS, 0, true, true, true, false, true);

  public void testNeverSkipClasses() throws Exception {
    assertFalse(deserializationStrategy.shouldSkipClass(MockObject.class));
    assertFalse(serializationStrategy.shouldSkipClass(MockObject.class));
  }

  public void testSkipNonAnnotatedFields() throws Exception {
    FieldAttributes f = createFieldAttributes("hiddenField");
    assertTrue(deserializationStrategy.shouldSkipField(f));
    assertTrue(serializationStrategy.shouldSkipField(f));
  }

  public void testSkipExplicitlySkippedFields() throws Exception {
    FieldAttributes f = createFieldAttributes("explicitlyHiddenField");
    assertTrue(deserializationStrategy.shouldSkipField(f));
    assertTrue(serializationStrategy.shouldSkipField(f));
  }

  public void testNeverSkipExposedAnnotatedFields() throws Exception {
    FieldAttributes f = createFieldAttributes("exposedField");
    assertFalse(deserializationStrategy.shouldSkipField(f));
    assertFalse(serializationStrategy.shouldSkipField(f));
  }

  public void testNeverSkipExplicitlyExposedAnnotatedFields() throws Exception {
    FieldAttributes f = createFieldAttributes("explicitlyExposedField");
    assertFalse(deserializationStrategy.shouldSkipField(f));
    assertFalse(serializationStrategy.shouldSkipField(f));
  }

  public void testDifferentSerializeAndDeserializeField() throws Exception {
    FieldAttributes f = createFieldAttributes("explicitlyDifferentModeField");
    assertTrue(deserializationStrategy.shouldSkipField(f));
    assertFalse(serializationStrategy.shouldSkipField(f));
  }

  private static FieldAttributes createFieldAttributes(String fieldName) throws Exception {
    Field f = MockObject.class.getField(fieldName);
    return new FieldAttributes(f);
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
