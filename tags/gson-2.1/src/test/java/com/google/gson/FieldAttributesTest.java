/*
 * Copyright (C) 2009 Google Inc.
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

import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Unit tests for the {@link FieldAttributes} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class FieldAttributesTest extends TestCase {
  private FieldAttributes fieldAttributes;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    fieldAttributes = new FieldAttributes(Foo.class.getField("bar"));
  }

  public void testNullField() throws Exception {
    try {
      new FieldAttributes(null);
      fail("Field parameter can not be null");
    } catch (NullPointerException expected) { }
  }

  public void testDeclaringClass() throws Exception {
    assertEquals(Foo.class, fieldAttributes.getDeclaringClass());
  }

  public void testModifiers() throws Exception {
    assertFalse(fieldAttributes.hasModifier(Modifier.STATIC));
    assertFalse(fieldAttributes.hasModifier(Modifier.FINAL));
    assertFalse(fieldAttributes.hasModifier(Modifier.ABSTRACT));
    assertFalse(fieldAttributes.hasModifier(Modifier.VOLATILE));
    assertFalse(fieldAttributes.hasModifier(Modifier.PROTECTED));

    assertTrue(fieldAttributes.hasModifier(Modifier.PUBLIC));
    assertTrue(fieldAttributes.hasModifier(Modifier.TRANSIENT));
  }

  public void testIsSynthetic() throws Exception {
    assertFalse(fieldAttributes.isSynthetic());
  }

  public void testName() throws Exception {
    assertEquals("bar", fieldAttributes.getName());
  }

  public void testDeclaredTypeAndClass() throws Exception {
    Type expectedType = new TypeToken<List<String>>() {}.getType();
    assertEquals(expectedType, fieldAttributes.getDeclaredType());
    assertEquals(List.class, fieldAttributes.getDeclaredClass());
  }

  private static class Foo {
    @SuppressWarnings("unused")
    public transient List<String> bar;
  }
}
