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

import com.google.gson.annotations.SerializedName;

/**
 * Unit tests for the {@link SerializedNameAnnotationInterceptingNamingPolicy} class.
 *
 * @author Joel Leitch
 */
public class SerializedNameAnnotationInterceptingNamingPolicyTest extends TestCase {
  private static final String ANNOTATED_FIELD_NAME = "annotatedFieldName";

  private SerializedNameAnnotationInterceptingNamingPolicy policy;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    policy = new SerializedNameAnnotationInterceptingNamingPolicy(new JavaFieldNamingPolicy());
  }

  public void testFieldWithAnnotation() throws Exception {
    String fieldName = "fieldWithAnnotation";
    FieldAttributes f = new FieldAttributes(
        SomeObject.class, SomeObject.class.getField(fieldName));

    assertFalse(ANNOTATED_FIELD_NAME.equals(fieldName));
    assertEquals(ANNOTATED_FIELD_NAME, policy.translateName(f));
  }

  public void testFieldWithoutAnnotation() throws Exception {
    String fieldName = "fieldWithoutAnnotation";
    FieldAttributes f = new FieldAttributes(
        SomeObject.class, SomeObject.class.getField(fieldName));

    assertEquals(fieldName, policy.translateName(f));
  }

  @SuppressWarnings("unused")
  private static class SomeObject {
    @SerializedName(ANNOTATED_FIELD_NAME) public final int fieldWithAnnotation = 1;
    public final int fieldWithoutAnnotation = 1;
  }
}
