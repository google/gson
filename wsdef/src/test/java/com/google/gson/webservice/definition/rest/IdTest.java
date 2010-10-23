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
package com.google.gson.webservice.definition.rest;

import java.lang.reflect.ParameterizedType;

import junit.framework.TestCase;

import com.google.gson.reflect.TypeToken;

/**
 * Unit test for {@link Id}
 *
 * @author inder
 */
public class IdTest extends TestCase {

  public void testRawTypeNotEqualToParameterizedOfConcreteType() {
    ParameterizedType type = (ParameterizedType) new TypeToken<Id<Foo>>(){}.getType(); 
    assertFalse(Id.areEquivalentTypes(type, Id.class));
  }

  public void testRawTypeEqualToParameterizedOfWildcardType() {
    ParameterizedType fooType = (ParameterizedType) new TypeToken<Id<?>>(){}.getType(); 
    assertTrue(Id.areEquivalentTypes(fooType, Id.class));
  }

  private static class Foo {
  }
}
