/*
 * Copyright (C) 2018 The Gson authors
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
package com.google.gson.internal.reflect;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.junit.Test;

/**
 * Unit tests for {@link UnsafeReflectionAccessor}
 *
 * @author Inderjeet Singh
 */
public class UnsafeReflectionAccessorTest {

  @Test
  public void testMakeAccessibleWithUnsafe() throws Exception {
    UnsafeReflectionAccessor accessor = new UnsafeReflectionAccessor();
    Field field = ClassWithPrivateFinalFields.class.getDeclaredField("a");
    try {
      boolean success = accessor.makeAccessibleWithUnsafe(field);
      assertTrue(success);
    } catch (Exception e) {
      fail("Unsafe didn't work on the JDK");
    }
  }

  @SuppressWarnings("unused")
  private static final class ClassWithPrivateFinalFields {
    private final String a;
    public ClassWithPrivateFinalFields(String a) {
      this.a = a;
    }
  }
}
