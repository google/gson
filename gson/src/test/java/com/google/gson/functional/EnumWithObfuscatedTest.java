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

package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import junit.framework.TestCase;

/**
 * Functional tests for enums with Proguard.
 *
 * @author Young Cha
 */
public class EnumWithObfuscatedTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public enum Gender {
    @SerializedName("MAIL")
    MALE,

    @SerializedName("FEMAIL")
    FEMALE
  }

  public void testEnumClassWithObfuscated() {
    for (Gender enumConstant: Gender.class.getEnumConstants()) {
      try {
        Gender.class.getField(enumConstant.name());
        fail("Enum is not obfuscated");
      } catch (NoSuchFieldException ignore) {
      }
    }

    assertEquals(Gender.MALE, gson.fromJson("\"MAIL\"", Gender.class));
    assertEquals("\"MAIL\"", gson.toJson(Gender.MALE, Gender.class));
  }
}
