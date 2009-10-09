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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Small test to ensure that the TypeInfoFactory is return the object that we expect.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class TypeInfoFactoryTest extends TestCase {

  private static Type OBJ_TYPE = new TypeToken<ObjectWithDifferentFields<Integer>>() {}.getType();
  private ObjectWithDifferentFields<Integer> obj;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    obj = new ObjectWithDifferentFields<Integer>();
  }

  public void testSimpleField() throws Exception {
    Field f = obj.getClass().getField("simpleField");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(String.class, typeInfo.getActualType());
    assertEquals(String.class, typeInfo.getRawClass());
  }

  public void testEnumField() throws Exception {
    Field f = obj.getClass().getField("enumField");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertTrue(typeInfo.isEnum());
    assertEquals(ObjectWithDifferentFields.TestEnum.class, typeInfo.getActualType());
    assertEquals(ObjectWithDifferentFields.TestEnum.class, typeInfo.getRawClass());
  }

  public void testParameterizedTypeField() throws Exception {
    Type listType = new TypeToken<List<String>>() {}.getType();
    Field f = obj.getClass().getField("simpleParameterizedType");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

  public void testNestedParameterizedTypeField() throws Exception {
    Type listType = new TypeToken<List<List<String>>>() {}.getType();
    Field f = obj.getClass().getField("simpleNestedParameterizedType");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

  public void testGenericArrayTypeField() throws Exception {
    Type listType = new TypeToken<List<String>[]>() {}.getType();
    Field f = obj.getClass().getField("simpleGenericArray");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

  public void testTypeVariableField() throws Exception {
    Field f = obj.getClass().getField("typeVariableObj");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(Integer.class, typeInfo.getActualType());
    assertEquals(Integer.class, typeInfo.getRawClass());
  }

  public void testTypeVariableArrayField() throws Exception {
    Field f = obj.getClass().getField("typeVariableArray");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(Integer[].class, typeInfo.getActualType());
    assertEquals(Integer[].class, typeInfo.getRawClass());
  }

  public void testMutliDimensionalTypeVariableArrayField() throws Exception {
    Field f = obj.getClass().getField("mutliDimensionalTypeVariableArray");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(Integer[][][].class, typeInfo.getActualType());
    assertEquals(Integer[][][].class, typeInfo.getRawClass());
  }

  public void testParameterizedTypeVariableField() throws Exception {
    Type listType = new TypeToken<List<Integer>>() {}.getType();
    Field f = obj.getClass().getField("listOfTypeVariables");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

  public void testNestedParameterizedTypeVariableField() throws Exception {
    Type listType = new TypeToken<List<List<Integer>>>() {}.getType();
    Field f = obj.getClass().getField("listOfListsOfTypeVariables");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

  public void testParameterizedTypeVariableArrayField() throws Exception {
    Type listType = new TypeToken<List<Integer>[]>() {}.getType();
    Field f = obj.getClass().getField("listOfTypeVariablesArray");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

  public void testWildcardField() throws Exception {
    Type listType = new TypeToken<List<Object>>() {}.getType();
    Field f = obj.getClass().getField("listWithWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

  public void testArrayOfWildcardField() throws Exception {
    Type listType = new TypeToken<List<Object>[]>() {}.getType();
    Field f = obj.getClass().getField("arrayOfListWithWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

  public void testListStringWildcardField() throws Exception {
    Type listType = new TypeToken<List<String>>() {}.getType();
    Field f = obj.getClass().getField("listWithStringWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

  public void testArrayOfListStringWildcardField() throws Exception {
    Type listType = new TypeToken<List<String>[]>() {}.getType();
    Field f = obj.getClass().getField("arrayOfListWithStringWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

  public void testListTypeVariableWildcardField() throws Exception {
    Type listType = new TypeToken<List<Integer>>() {}.getType();
    Field f = obj.getClass().getField("listWithTypeVariableWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertFalse(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List.class, typeInfo.getRawClass());
  }

  public void testArrayOfListTypeVariableWildcardField() throws Exception {
    Type listType = new TypeToken<List<Integer>[]>() {}.getType();
    Field f = obj.getClass().getField("arrayOfListWithTypeVariableWildcard");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(f, OBJ_TYPE);

    assertTrue(typeInfo.isArray());
    assertFalse(typeInfo.isEnum());
    assertEquals(listType, typeInfo.getActualType());
    assertEquals(List[].class, typeInfo.getRawClass());
  }

  @SuppressWarnings("unused")
  private static class ObjectWithDifferentFields<T> {
    public static enum TestEnum {
      TEST_1, TEST_2;
    }

    public String simpleField;
    public TestEnum enumField;
    public List<String> simpleParameterizedType;
    public List<List<String>> simpleNestedParameterizedType;
    public List<String>[] simpleGenericArray;

    public T typeVariableObj;
    public T[] typeVariableArray;
    public T[][][] mutliDimensionalTypeVariableArray;
    public List<T> listOfTypeVariables;
    public List<List<T>> listOfListsOfTypeVariables;
    public List<T>[] listOfTypeVariablesArray;

    public List<?> listWithWildcard;
    public List<?>[] arrayOfListWithWildcard;
    public List<? extends String> listWithStringWildcard;
    public List<? extends String>[] arrayOfListWithStringWildcard;

    public List<? extends T> listWithTypeVariableWildcard;
    public List<? extends T>[] arrayOfListWithTypeVariableWildcard;
  }
}
