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
 * Unit tests for the {@link ParameterizedTypeImpl} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ParameterizedTypeImplTest extends TestCase {

  private Type parameterizedType;
  private ParameterizedTypeImpl ourType;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    parameterizedType = new TypeToken<List<String>>() {}.getType();
    ourType = new ParameterizedTypeImpl(List.class, new Type[] { String.class }, null);
  }

  public void testOurTypeFunctionality() throws Exception {
    assertNull(ourType.getOwnerType());
    assertEquals(String.class, ourType.getActualTypeArguments()[0]);
    assertEquals(List.class, ourType.getRawType());
    assertEquals(parameterizedType, ourType);
    assertEquals(parameterizedType.hashCode(), ourType.hashCode());
  }

  public void testNotEquals() throws Exception {
    Type differentParameterizedType = new TypeToken<List<Integer>>() {}.getType();
    assertFalse(differentParameterizedType.equals(ourType));
    assertFalse(ourType.equals(differentParameterizedType));
  }
}
