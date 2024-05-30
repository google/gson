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

package com.google.gson.jpms_test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.junit.Test;

/**
 * Verifies that Gson cannot use reflection for classes in a package if it has not been 'opened' to
 * the Gson module in the {@code module-info.java} of the project.
 */
public class ReflectionInaccessibleTest {
  private static class MyClass {
    @SuppressWarnings("unused")
    int i;
  }

  @Test
  public void testDeserialization() {
    Gson gson = new Gson();
    var e = assertThrows(JsonIOException.class, () -> gson.fromJson("{\"i\":1}", MyClass.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Failed making field '"
                + MyClass.class.getName()
                + "#i' accessible; either increase its visibility or write a custom TypeAdapter for"
                + " its declaring type.\n"
                + "See https://github.com/google/gson/blob/main/Troubleshooting.md#reflection-inaccessible-to-module-gson");
  }

  @Test
  public void testSerialization() {
    Gson gson = new Gson();

    MyClass obj = new MyClass();
    obj.i = 1;
    var e = assertThrows(JsonIOException.class, () -> gson.toJson(obj));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Failed making field '"
                + MyClass.class.getName()
                + "#i' accessible; either increase its visibility or write a custom TypeAdapter for"
                + " its declaring type.\n"
                + "See https://github.com/google/gson/blob/main/Troubleshooting.md#reflection-inaccessible-to-module-gson");
  }
}
