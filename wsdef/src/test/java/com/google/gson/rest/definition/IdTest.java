/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.rest.definition;

import java.lang.reflect.ParameterizedType;

import junit.framework.TestCase;

import com.google.gson.reflect.TypeToken;
import com.google.gson.rest.definition.ValueBasedId;

/**
 * Unit test for {@link ValueBasedId}
 *
 * @author inder
 */
public class IdTest extends TestCase {

  public void testRawTypeNotEqualToParameterizedOfConcreteType() {
    ParameterizedType type = (ParameterizedType) new TypeToken<ValueBasedId<Foo>>(){}.getType(); 
    assertFalse(ValueBasedId.areEquivalentTypes(type, ValueBasedId.class));
  }

  public void testRawTypeEqualToParameterizedOfWildcardType() {
    ParameterizedType fooType = (ParameterizedType) new TypeToken<ValueBasedId<?>>(){}.getType(); 
    assertTrue(ValueBasedId.areEquivalentTypes(fooType, ValueBasedId.class));
  }

  public void testStaticEquals() {
    ValueBasedId<Foo> id1 = ValueBasedId.get(3L, Foo.class);
    ValueBasedId<Foo> id2 = ValueBasedId.get(3L, Foo.class);
    ValueBasedId<Foo> id3 = ValueBasedId.get(4L, Foo.class);
    assertTrue(ValueBasedId.equals(id1, id2));
    assertFalse(ValueBasedId.equals(null, id2));
    assertFalse(ValueBasedId.equals(id1, null));
    assertTrue(ValueBasedId.equals(null, null));
    assertFalse(ValueBasedId.equals(id1, id3));
  }

  private static class Foo {
  }
}
