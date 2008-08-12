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

  public void testPrimitiveArray() throws Exception {
    TypeInfo arrayTypeInfo = new TypeInfo(int[].class);

    assertTrue(arrayTypeInfo.isArray());
    assertEquals(int.class, arrayTypeInfo.getSecondLevelClass());
    assertFalse(arrayTypeInfo.isPrimitiveOrStringAndNotAnArray());
  }

  public void testObjectArray() throws Exception {
    TypeInfo arrayTypeInfo = new TypeInfo(String[].class);

    assertTrue(arrayTypeInfo.isArray());
    assertEquals(String.class, arrayTypeInfo.getSecondLevelClass());
    assertEquals(String[].class, arrayTypeInfo.getTopLevelClass());
  }

  public void testPrimitive() throws Exception {
    TypeInfo typeInfo = new TypeInfo(boolean.class);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isString());
    assertTrue(typeInfo.isPrimitive());
    assertEquals(boolean.class, typeInfo.getSecondLevelClass());
    assertEquals(Boolean.class, typeInfo.getWrappedClazz());
  }

  public void testPrimitiveWrapper() throws Exception {
    TypeInfo typeInfo = new TypeInfo(Integer.class);

    assertEquals(Integer.class, typeInfo.getSecondLevelClass());
    assertTrue(typeInfo.isPrimitive());
    assertTrue(typeInfo.isPrimitiveOrStringAndNotAnArray());
  }

  public void testString() throws Exception {
    TypeInfo typeInfo = new TypeInfo(String.class);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isPrimitive());
    assertEquals(String.class, typeInfo.getSecondLevelClass());
    assertTrue(typeInfo.isPrimitiveOrStringAndNotAnArray());
  }

  public void testObject() throws Exception {
    TypeInfo typeInfo = new TypeInfo(Object.class);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isPrimitive());
    assertEquals(Object.class, typeInfo.getSecondLevelClass());
    assertFalse(typeInfo.isPrimitiveOrStringAndNotAnArray());
  }

  public void testPrimitiveType() throws Exception {
    TypeInfo typeInfo = new TypeInfo(long[].class);
    assertTrue(typeInfo.isArray());
    assertEquals(long.class, typeInfo.getSecondLevelClass());

    typeInfo = new TypeInfo(long.class);
    assertFalse(typeInfo.isArray());
    assertEquals(long.class, typeInfo.getSecondLevelClass());
  }

  public void testObjectType() throws Exception {
    TypeInfo typeInfo = new TypeInfo(String.class);
    assertFalse(typeInfo.isArray());
    assertTrue(typeInfo.isString());
    assertEquals(String.class, typeInfo.getSecondLevelClass());

    typeInfo = new TypeInfo(String[].class);
    assertTrue(typeInfo.isArray());
    assertEquals(String.class, typeInfo.getSecondLevelClass());
  }

  public void testParameterizedTypes() throws Exception {
    Type type = new TypeToken<List<String>>() {}.getType();
    TypeInfo typeInfo = new TypeInfo(type);

    assertFalse(typeInfo.isArray());
    assertEquals(List.class, typeInfo.getSecondLevelClass());
  }

  public void testArrayAsParameterizedTypes() throws Exception {
    Type type = new TypeToken<List<String>[]>() {}.getType();

    TypeInfo typeInfo = new TypeInfo(type);
    assertTrue(typeInfo.isArray());
    assertEquals(List.class, typeInfo.getSecondLevelClass());
    assertEquals(String.class, typeInfo.getGenericClass());
  }

  public void testGenericizedGenericType() throws Exception {
    Type type = new TypeToken<List<List<String>>>() {}.getType();
    Type genericType = new TypeToken<List<String>>() {}.getType();

    TypeInfo typeInfo = new TypeInfo(type);
    assertFalse(typeInfo.isArray());
    assertEquals(List.class, typeInfo.getSecondLevelClass());
    assertEquals(genericType, typeInfo.getGenericClass());

    typeInfo = new TypeInfo(genericType);
    assertEquals(String.class, typeInfo.getGenericClass());
  }

  public void testStrangeTypeParameters() throws Exception {
    try {
      new TypeInfo(new Type() {});
      fail("Should not be able to determine this unknown type");
    } catch (IllegalArgumentException expected) {
    }
  }
}
