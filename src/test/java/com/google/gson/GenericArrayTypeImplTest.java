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
 * Unit tests for the {@link GenericArrayTypeImpl} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class GenericArrayTypeImplTest extends TestCase {

  private Type parameterizedType;
  private Type genericArrayType;
  private GenericArrayTypeImpl ourType;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    parameterizedType = new TypeToken<List<String>>() {}.getType();
    genericArrayType = new TypeToken<List<String>[]>() {}.getType();
    ourType = new GenericArrayTypeImpl(parameterizedType);
  }

  public void testOurTypeFunctionality() throws Exception {
    assertEquals(parameterizedType, ourType.getGenericComponentType());
    assertEquals(genericArrayType, ourType);
    assertEquals(genericArrayType.hashCode(), ourType.hashCode());
  }

  public void testNotEquals() throws Exception {
    Type differentGenericArrayType = new TypeToken<List<String>[][]>() {}.getType();

    assertFalse(differentGenericArrayType.equals(ourType));
    assertFalse(ourType.equals(differentGenericArrayType));
  }
}
