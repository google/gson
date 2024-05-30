/*
 * Copyright (C) 2024 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This package is opened for reflection, see `module-info.java`
package com.google.gson.jpms_test.opened;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import org.junit.Test;

/**
 * Verifies that Gson can use reflection for classes in a package if it has been 'opened' to the
 * Gson module in the {@code module-info.java} of the project.
 */
public class ReflectionTest {
  private static class MyClass {
    int i;
  }

  @Test
  public void testDeserialization() {
    Gson gson = new Gson();
    MyClass deserialized = gson.fromJson("{\"i\":1}", MyClass.class);
    assertThat(deserialized.i).isEqualTo(1);
  }

  @Test
  public void testSerialization() {
    Gson gson = new Gson();

    MyClass obj = new MyClass();
    obj.i = 1;
    String serialized = gson.toJson(obj);
    assertThat(serialized).isEqualTo("{\"i\":1}");
  }
}
