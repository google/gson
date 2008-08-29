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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for the default JSON map serialization object located in the
 * {@link DefaultTypeAdapters} class.
 *
 * @author Joel Leitch
 */
public class TypeInfoMapTest extends TestCase {

  public void testInvalidConstruction() throws Exception {
    try {
      new TypeInfoMap(String.class);
      fail("Must be a ParameterizedType");
    } catch (IllegalArgumentException expected) { }
  }

  public void testNonMapConstruction() throws Exception {
    try {
      Type parameterizedMapType = new TypeToken<List<String>>() {}.getType();
      new TypeInfoMap(parameterizedMapType);
      fail("The raw type must be a Map");
    } catch (IllegalArgumentException expected) { }
  }

  public void testBasicGetters() throws Exception {
    Type parameterizedMapType = new TypeToken<Map<String, Integer>>() {}.getType();
    TypeInfoMap mapTypeInfo = new TypeInfoMap(parameterizedMapType);

    assertEquals(String.class, mapTypeInfo.getKeyType());
    assertEquals(Integer.class, mapTypeInfo.getValueType());
  }

  public void testMapImplementations() throws Exception {
    Type parameterizedMapType = new TypeToken<HashMap<String, Integer>>() {}.getType();
    TypeInfoMap mapTypeInfo = new TypeInfoMap(parameterizedMapType);

    assertEquals(String.class, mapTypeInfo.getKeyType());
    assertEquals(Integer.class, mapTypeInfo.getValueType());
  }
}
