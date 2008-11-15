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
import java.util.List;

/**
 * Exercising the construction of the Parameter object and ensure the
 * proper types are returned.
 *
 * @author Joel Leitch
 */
public class TypeInfoTest extends TestCase {

  public void testPrimitive() throws Exception {
    TypeInfo typeInfo = new TypeInfo(boolean.class);

    assertFalse(typeInfo.isArray());
    assertTrue(typeInfo.isPrimitive());
    assertEquals(boolean.class, typeInfo.getRawClass());
    assertEquals(Boolean.class, typeInfo.getWrappedClass());
  }

  public void testPrimitiveWrapper() throws Exception {
    TypeInfo typeInfo = new TypeInfo(Integer.class);

    assertEquals(Integer.class, typeInfo.getRawClass());
    assertTrue(typeInfo.isPrimitive());
    assertFalse(typeInfo.isArray());
  }

  public void testString() throws Exception {
    TypeInfo typeInfo = new TypeInfo(String.class);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isPrimitive());
    assertEquals(String.class, typeInfo.getRawClass());
  }

  public void testObject() throws Exception {
    TypeInfo typeInfo = new TypeInfo(Object.class);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isPrimitive());
    assertEquals(Object.class, typeInfo.getRawClass());
  }

  public void testPrimitiveType() throws Exception {
    TypeInfo typeInfo = new TypeInfo(long.class);
    assertFalse(typeInfo.isArray());
    assertEquals(long.class, typeInfo.getRawClass());
  }

  public void testObjectType() throws Exception {
    TypeInfo typeInfo = new TypeInfo(String.class);
    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isPrimitive());
    assertEquals(String.class, typeInfo.getRawClass());
  }

  public void testParameterizedTypes() throws Exception {
    Type type = new TypeToken<List<String>>() {}.getType();
    TypeInfo typeInfo = new TypeInfo(type);

    assertFalse(typeInfo.isArray());
    assertEquals(List.class, typeInfo.getRawClass());
    assertEquals(type, typeInfo.getActualType());
  }

  public void testGenericizedGenericType() throws Exception {
    Type type = new TypeToken<List<List<String>>>() {}.getType();
    Type genericType = new TypeToken<List<String>>() {}.getType();

    TypeInfo typeInfo = new TypeInfo(type);
    assertFalse(typeInfo.isArray());
    assertEquals(List.class, typeInfo.getRawClass());
    Type actualTypeForFirstTypeVariable = TypeUtils.getActualTypeForFirstTypeVariable(type);
    assertEquals(genericType, actualTypeForFirstTypeVariable);

    typeInfo = new TypeInfo(genericType);
    actualTypeForFirstTypeVariable = TypeUtils.getActualTypeForFirstTypeVariable(genericType);
    assertEquals(String.class, actualTypeForFirstTypeVariable);
  }

  public void testStrangeTypeParameters() throws Exception {
    try {
      new TypeInfo(new Type() {});
      fail("Should not be able to determine this unknown type");
    } catch (IllegalArgumentException expected) {
    }
  }
}
