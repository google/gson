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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link FieldAttributes} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class FieldAttributesTest {
  private FieldAttributes fieldAttributes;

  @Before
  public void setUp() throws Exception {
    fieldAttributes = new FieldAttributes(Foo.class.getField("bar"));
  }

  @SuppressWarnings("unused")
  @Test
  public void testNullField() {
    try {
      new FieldAttributes(null);
      fail("Field parameter can not be null");
    } catch (NullPointerException expected) { }
  }

  @Test
  public void testDeclaringClass() {
    assertThat(fieldAttributes.getDeclaringClass()).isAssignableTo(Foo.class);
  }

  @Test
  public void testModifiers() {
    assertThat(fieldAttributes.hasModifier(Modifier.STATIC)).isFalse();
    assertThat(fieldAttributes.hasModifier(Modifier.FINAL)).isFalse();
    assertThat(fieldAttributes.hasModifier(Modifier.ABSTRACT)).isFalse();
    assertThat(fieldAttributes.hasModifier(Modifier.VOLATILE)).isFalse();
    assertThat(fieldAttributes.hasModifier(Modifier.PROTECTED)).isFalse();

    assertThat(fieldAttributes.hasModifier(Modifier.PUBLIC)).isTrue();
    assertThat(fieldAttributes.hasModifier(Modifier.TRANSIENT)).isTrue();
  }

  @Test
  public void testName() {
    assertThat(fieldAttributes.getName()).isEqualTo("bar");
  }

  @Test
  public void testDeclaredTypeAndClass() {
    Type expectedType = new TypeToken<List<String>>() {}.getType();
    assertThat(fieldAttributes.getDeclaredType()).isEqualTo(expectedType);
    assertThat(fieldAttributes.getDeclaredClass()).isAssignableTo(List.class);
  }

  private static class Foo {
    @SuppressWarnings("unused")
    public transient List<String> bar;
  }
}
