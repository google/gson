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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@code GenericArrayType}s created by the {@link $Gson$Types} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class GenericArrayTypeTest {
  private GenericArrayType ourType;

  @Before
  public void setUp() throws Exception {
    ourType = $Gson$Types.arrayOf($Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class));
  }

  @Test
  public void testOurTypeFunctionality() throws Exception {
    Type parameterizedType = new TypeToken<List<String>>() {}.getType();
    Type genericArrayType = new TypeToken<List<String>[]>() {}.getType();

    assertEquals(parameterizedType, ourType.getGenericComponentType());
    assertEquals(genericArrayType, ourType);
    assertEquals(genericArrayType.hashCode(), ourType.hashCode());
  }

  @Test
  public void testNotEquals() throws Exception {
    Type differentGenericArrayType = new TypeToken<List<String>[][]>() {}.getType();
    assertFalse(differentGenericArrayType.equals(ourType));
    assertFalse(ourType.equals(differentGenericArrayType));
  }
}
