/*
 * Copyright (C) 2023 Google Inc.
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
package com.google.gson.native_test;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReflectionTest {
  private static class ClassWithDefaultConstructor {
    private int i;
  }

  @Test
  void testDefaultConstructor() {
    Gson gson = new Gson();

    ClassWithDefaultConstructor c = gson.fromJson("{\"i\":1}", ClassWithDefaultConstructor.class);
    assertThat(c.i).isEqualTo(1);
  }

  private static class ClassWithCustomDefaultConstructor {
    private int i;

    private ClassWithCustomDefaultConstructor() {
      i = 1;
    }
  }

  @Test
  void testCustomDefaultConstructor() {
    Gson gson = new Gson();

    ClassWithCustomDefaultConstructor c =
        gson.fromJson("{\"i\":2}", ClassWithCustomDefaultConstructor.class);
    assertThat(c.i).isEqualTo(2);

    c = gson.fromJson("{}", ClassWithCustomDefaultConstructor.class);
    assertThat(c.i).isEqualTo(1);
  }

  private static class ClassWithoutDefaultConstructor {
    private int i = -1;

    // Explicit constructor with args to remove implicit no-args default constructor
    private ClassWithoutDefaultConstructor(int i) {
      this.i = i;
    }
  }

  /**
   * Tests deserializing a class without default constructor.
   *
   * <p>This should use JDK Unsafe, and would normally require specifying {@code "unsafeAllocated":
   * true} in the reflection metadata for GraalVM, though for some reason it also seems to work
   * without it? Possibly because GraalVM seems to have special support for Gson, see its class
   * {@code com.oracle.svm.thirdparty.gson.GsonFeature}.
   */
  @Test
  void testClassWithoutDefaultConstructor() {
    Gson gson = new Gson();

    ClassWithoutDefaultConstructor c =
        gson.fromJson("{\"i\":1}", ClassWithoutDefaultConstructor.class);
    assertThat(c.i).isEqualTo(1);

    c = gson.fromJson("{}", ClassWithoutDefaultConstructor.class);
    // Class is instantiated with JDK Unsafe, therefore field keeps its default value instead of
    // assigned -1
    assertThat(c.i).isEqualTo(0);
  }

  @Test
  void testInstanceCreator() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                ClassWithoutDefaultConstructor.class,
                new InstanceCreator<ClassWithoutDefaultConstructor>() {
                  @Override
                  public ClassWithoutDefaultConstructor createInstance(Type type) {
                    return new ClassWithoutDefaultConstructor(-2);
                  }
                })
            .create();

    ClassWithoutDefaultConstructor c =
        gson.fromJson("{\"i\":1}", ClassWithoutDefaultConstructor.class);
    assertThat(c.i).isEqualTo(1);

    c = gson.fromJson("{}", ClassWithoutDefaultConstructor.class);
    // Uses default value specified by InstanceCreator
    assertThat(c.i).isEqualTo(-2);
  }

  private static class ClassWithFinalField {
    // Initialize with value which is not inlined by compiler
    private final int i = nonConstant();

    private static int nonConstant() {
      return "a".length(); // = 1
    }
  }

  @Test
  void testFinalField() {
    Gson gson = new Gson();

    ClassWithFinalField c = gson.fromJson("{\"i\":2}", ClassWithFinalField.class);
    assertThat(c.i).isEqualTo(2);

    c = gson.fromJson("{}", ClassWithFinalField.class);
    assertThat(c.i).isEqualTo(1);
  }

  private static class ClassWithSerializedName {
    @SerializedName("custom-name")
    private int i;
  }

  @Test
  void testSerializedName() {
    Gson gson = new Gson();
    ClassWithSerializedName c = gson.fromJson("{\"custom-name\":1}", ClassWithSerializedName.class);
    assertThat(c.i).isEqualTo(1);

    c = new ClassWithSerializedName();
    c.i = 2;
    assertThat(gson.toJson(c)).isEqualTo("{\"custom-name\":2}");
  }

  @JsonAdapter(ClassWithCustomClassAdapter.CustomAdapter.class)
  private static class ClassWithCustomClassAdapter {
    private static class CustomAdapter extends TypeAdapter<ClassWithCustomClassAdapter> {
      @Override
      public ClassWithCustomClassAdapter read(JsonReader in) throws IOException {
        return new ClassWithCustomClassAdapter(in.nextInt() + 5);
      }

      @Override
      public void write(JsonWriter out, ClassWithCustomClassAdapter value) throws IOException {
        out.value(value.i + 6);
      }
    }

    private int i;

    private ClassWithCustomClassAdapter(int i) {
      this.i = i;
    }
  }

  @Test
  void testCustomClassAdapter() {
    Gson gson = new Gson();
    ClassWithCustomClassAdapter c = gson.fromJson("1", ClassWithCustomClassAdapter.class);
    assertThat(c.i).isEqualTo(6);

    assertThat(gson.toJson(new ClassWithCustomClassAdapter(1))).isEqualTo("7");
  }

  private static class ClassWithCustomFieldAdapter {
    private static class CustomAdapter extends TypeAdapter<Integer> {
      @Override
      public Integer read(JsonReader in) throws IOException {
        return in.nextInt() + 5;
      }

      @Override
      public void write(JsonWriter out, Integer value) throws IOException {
        out.value(value + 6);
      }
    }

    @JsonAdapter(ClassWithCustomFieldAdapter.CustomAdapter.class)
    private int i;

    private ClassWithCustomFieldAdapter(int i) {
      this.i = i;
    }

    private ClassWithCustomFieldAdapter() {
      this(-1);
    }
  }

  @Test
  void testCustomFieldAdapter() {
    Gson gson = new Gson();
    ClassWithCustomFieldAdapter c = gson.fromJson("{\"i\":1}", ClassWithCustomFieldAdapter.class);
    assertThat(c.i).isEqualTo(6);

    assertThat(gson.toJson(new ClassWithCustomFieldAdapter(1))).isEqualTo("{\"i\":7}");
  }

  private static class ClassWithRegisteredAdapter {
    private int i;

    private ClassWithRegisteredAdapter(int i) {
      this.i = i;
    }
  }

  @Test
  void testCustomAdapter() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                ClassWithRegisteredAdapter.class,
                new TypeAdapter<ClassWithRegisteredAdapter>() {
                  @Override
                  public ClassWithRegisteredAdapter read(JsonReader in) throws IOException {
                    return new ClassWithRegisteredAdapter(in.nextInt() + 5);
                  }

                  @Override
                  public void write(JsonWriter out, ClassWithRegisteredAdapter value)
                      throws IOException {
                    out.value(value.i + 6);
                  }
                })
            .create();

    ClassWithRegisteredAdapter c = gson.fromJson("1", ClassWithRegisteredAdapter.class);
    assertThat(c.i).isEqualTo(6);

    assertThat(gson.toJson(new ClassWithRegisteredAdapter(1))).isEqualTo("7");
  }

  @Test
  void testGenerics() {
    Gson gson = new Gson();

    List<ClassWithDefaultConstructor> list =
        gson.fromJson("[{\"i\":1}]", new TypeToken<List<ClassWithDefaultConstructor>>() {});
    assertThat(list).hasSize(1);
    assertThat(list.get(0).i).isEqualTo(1);

    @SuppressWarnings("unchecked")
    List<ClassWithDefaultConstructor> list2 =
        (List<ClassWithDefaultConstructor>)
            gson.fromJson(
                "[{\"i\":1}]",
                TypeToken.getParameterized(List.class, ClassWithDefaultConstructor.class));
    assertThat(list2).hasSize(1);
    assertThat(list2.get(0).i).isEqualTo(1);
  }
}
