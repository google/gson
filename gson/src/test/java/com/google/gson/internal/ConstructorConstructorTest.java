/*
 * Copyright (C) 2022 Google Inc.
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

package com.google.gson.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.InstanceCreator;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import org.junit.Test;

public class ConstructorConstructorTest {
  private ConstructorConstructor constructorConstructor = new ConstructorConstructor(
      Collections.<Type, InstanceCreator<?>>emptyMap(), true,
      Collections.<ReflectionAccessFilter>emptyList()
  );

  private abstract static class AbstractClass {
    @SuppressWarnings("unused")
    public AbstractClass() { }
  }
  private interface Interface { }

  /**
   * Verify that ConstructorConstructor does not try to invoke no-arg constructor
   * of abstract class.
   */
  @Test
  public void testGet_AbstractClassNoArgConstructor() {
    ObjectConstructor<AbstractClass> constructor = constructorConstructor.get(TypeToken.get(AbstractClass.class));
    try {
      constructor.construct();
      fail("Expected exception");
    } catch (RuntimeException exception) {
      assertThat(exception).hasMessageThat().isEqualTo("Abstract classes can't be instantiated! "
          + "Register an InstanceCreator or a TypeAdapter for this type. "
          + "Class name: com.google.gson.internal.ConstructorConstructorTest$AbstractClass");
    }
  }

  @Test
  public void testGet_Interface() {
    ObjectConstructor<Interface> constructor = constructorConstructor.get(TypeToken.get(Interface.class));
    try {
      constructor.construct();
      fail("Expected exception");
    } catch (RuntimeException exception) {
      assertThat(exception).hasMessageThat().isEqualTo("Interfaces can't be instantiated! "
          + "Register an InstanceCreator or a TypeAdapter for this type. "
          + "Interface name: com.google.gson.internal.ConstructorConstructorTest$Interface");
    }
  }
}
