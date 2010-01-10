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

package com.google.gson;

import java.lang.reflect.Field;

import junit.framework.TestCase;

/**
 * Unit test for the {@link FieldNamingStrategy2Adapter} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class FieldNamingStrategy2AdapterTest extends TestCase {

  public void testSimpleAdapter() throws Exception {
    Field field = String.class.getFields()[0];
    String expectedFieldName = field.getName().toUpperCase();
    FieldNamingStrategy2 adapter =
        new FieldNamingStrategy2Adapter(new UpperCaseNamingStrategy());
    assertEquals(expectedFieldName, adapter.translateName(
        new FieldAttributes(String.class, field)));
  }
  
  private static class UpperCaseNamingStrategy implements FieldNamingStrategy {
    public String translateName(Field f) {
      return f.getName().toUpperCase();
    }
  }
}
