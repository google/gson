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
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.Test;

/**
 * Verifies that Gson's {@code module-info.class} properly 'exports' all its packages containing
 * public API.
 */
public class ExportedPackagesTest {
  /** Tests package {@code com.google.gson} */
  @Test
  public void testMainPackage() {
    Gson gson = new Gson();
    assertThat(gson.toJson(1)).isEqualTo("1");
  }

  /** Tests package {@code com.google.gson.annotations} */
  @Test
  public void testAnnotationsPackage() throws Exception {
    class Annotated {
      @SerializedName("custom-name")
      int i;
    }

    Field field = Annotated.class.getDeclaredField("i");
    SerializedName annotation = field.getAnnotation(SerializedName.class);
    assertThat(annotation.value()).isEqualTo("custom-name");
  }

  /** Tests package {@code com.google.gson.reflect} */
  @Test
  public void testReflectPackage() {
    var typeToken = TypeToken.get(String.class);
    assertThat(typeToken.getRawType()).isEqualTo(String.class);
  }

  /** Tests package {@code com.google.gson.stream} */
  @Test
  public void testStreamPackage() throws IOException {
    JsonReader jsonReader = new JsonReader(new StringReader("2"));
    assertThat(jsonReader.nextInt()).isEqualTo(2);
  }

  /** Verifies that Gson packages are only 'exported' but not 'opened' for reflection. */
  @Test
  public void testReflectionInternalField() throws Exception {
    Gson gson = new Gson();

    // Get an arbitrary non-public instance field
    Field field =
        Arrays.stream(Gson.class.getDeclaredFields())
            .filter(
                f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isPublic(f.getModifiers()))
            .findFirst()
            .get();
    assertThrows(InaccessibleObjectException.class, () -> field.setAccessible(true));
    assertThrows(IllegalAccessException.class, () -> field.get(gson));
  }

  @Test
  public void testInaccessiblePackage() throws Exception {
    // Note: In case this class is renamed / removed, can change this to any other internal class
    Class<?> internalClass = Class.forName("com.google.gson.internal.LinkedTreeMap");
    assertThat(Modifier.isPublic(internalClass.getModifiers())).isTrue();
    // Get the public constructor
    Constructor<?> constructor = internalClass.getConstructor();
    assertThrows(InaccessibleObjectException.class, () -> constructor.setAccessible(true));
    assertThrows(IllegalAccessException.class, () -> constructor.newInstance());
  }
}
