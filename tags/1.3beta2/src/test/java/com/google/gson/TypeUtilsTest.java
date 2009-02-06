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

import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Unit tests for {@link TypeUtils}.
 *
 * @author Inderjeet Singh
 */
public class TypeUtilsTest extends TestCase {
  private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();

  public void testGetActualTypeForFirstTypeVariable() {
    assertEquals(String.class, TypeUtils.getActualTypeForFirstTypeVariable(MAP_TYPE));
  }

  public void testIsArrayForNonArrayClasses() {
    assertFalse(TypeUtils.isArray(Boolean.class));
    assertFalse(TypeUtils.isArray(MAP_TYPE));
  }

  public void testIsArrayForArrayClasses() {
    assertTrue(TypeUtils.isArray(String[].class));
    assertTrue(TypeUtils.isArray(Integer[][].class));
    assertTrue(TypeUtils.isArray(Collection[].class));
  }

  public void testToRawClassForNonGenericClasses() {
    assertEquals(String.class, TypeUtils.toRawClass(String.class));
  }

  public void testToRawClassForGenericClasses() {
    assertEquals(Map.class, TypeUtils.toRawClass(MAP_TYPE));
  }
}
