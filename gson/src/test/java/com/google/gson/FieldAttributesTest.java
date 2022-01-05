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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link FieldAttributes} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
class FieldAttributesTest {
  private FieldAttributes fieldAttributes;

  @BeforeEach
  void setUp() throws Exception {
    fieldAttributes = new FieldAttributes(Foo.class.getField("bar"));
  }

  @Test
  void testNullField() throws Exception {
    try {
      new FieldAttributes(null);
      fail("Field parameter can not be null");
    } catch (NullPointerException expected) { }
  }

  @Test
  void testDeclaringClass() throws Exception {
    assertEquals(Foo.class, fieldAttributes.getDeclaringClass());
  }

  @Test
  void testModifiers() throws Exception {
    assertFalse(fieldAttributes.hasModifier(Modifier.STATIC));
    assertFalse(fieldAttributes.hasModifier(Modifier.FINAL));
    assertFalse(fieldAttributes.hasModifier(Modifier.ABSTRACT));
    assertFalse(fieldAttributes.hasModifier(Modifier.VOLATILE));
    assertFalse(fieldAttributes.hasModifier(Modifier.PROTECTED));

    assertTrue(fieldAttributes.hasModifier(Modifier.PUBLIC));
    assertTrue(fieldAttributes.hasModifier(Modifier.TRANSIENT));
  }

  @Test
  void testIsSynthetic() throws Exception {
    assertFalse(fieldAttributes.isSynthetic());
  }

  @Test
  void testName() throws Exception {
    assertEquals("bar", fieldAttributes.getName());
  }

  @Test
  void testDeclaredTypeAndClass() throws Exception {
    Type expectedType = new TypeToken<List<String>>() {}.getType();
    assertEquals(expectedType, fieldAttributes.getDeclaredType());
    assertEquals(List.class, fieldAttributes.getDeclaredClass());
  }

  private static class Foo {
    @SuppressWarnings("unused")
    public transient List<String> bar;
  }
}
