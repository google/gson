/*
 * Copyright (C) 2017 Google Inc.
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.MissingFieldHandlingStrategy;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link MissingFieldHandlingStrategy}
 *
 * @author Prateek Jain
 */
public class MissingFieldHandlingStrategyTest {

  @Test
  public void shouldAssignFieldToDefaultValuesUsingDefaultStrategy() throws Exception {
    Gson gson = new GsonBuilder()
        .registerDefaultValue(Optional.class, Optional.absent())
        .create();
    JsonObject jsonObject = new JsonObject();

    ObjectWithOptionalFields objectWithOptionalFields =
        gson.fromJson(jsonObject, ObjectWithOptionalFields.class);

    assertFalse(objectWithOptionalFields.name.isPresent());
    assertFalse(objectWithOptionalFields.version.isPresent());
  }

  @Test
  public void shouldNotTreatNullFieldAsMissingField() throws Exception {
    Gson gson = new GsonBuilder()
        .registerDefaultValue(Optional.class, Optional.absent())
        .create();

    JsonObject jsonObject = new JsonObject();
    jsonObject.add("name", null);
    jsonObject.add("version", null);

    ObjectWithOptionalFields objectWithOptionalFields =
        gson.fromJson(jsonObject, ObjectWithOptionalFields.class);

    assertNull(objectWithOptionalFields.name);
    assertNull(objectWithOptionalFields.version);
  }

  @Test
  public void shouldUseCustomMissingFieldHandlingStrategy() throws Exception {
    MissingFieldHandlingStrategy strategy = new MissingFieldHandlingStrategy() {
      @Override
      public Object handle(TypeToken typeToken, String fieldName) {
        if ("f1".equals(fieldName))
          return "1.1";
        return "";
      }
    };
    Gson gson = new GsonBuilder().
        useMissingFieldHandlingStrategy(strategy).create();
    JsonObject jsonObject = new JsonObject();

    assertThat(gson.fromJson(jsonObject, ObjectWithSerializedAnnotation.class).v, is("1.1"));
  }

  @Test
  public void shouldNotOverrideNoArgsConstructorValuesIfDefaultValueIsNotRegistered() throws Exception {
    Gson gson = new GsonBuilder().create();
    JsonObject jsonObject = new JsonObject();

    assertThat(gson.fromJson(jsonObject, ObjectWithNoArgConstructor.class).stringValues, is("DEFAULT"));
  }

  private static class ObjectWithOptionalFields {
    private Optional<String> name;
    private Optional<String> version;
  }

  private static class ObjectWithSerializedAnnotation {
    @SerializedName(value = "f1")
    private String v;
  }

  private static class ObjectWithNoArgConstructor {
    private String stringValues;

    public ObjectWithNoArgConstructor() {
      this.stringValues = "DEFAULT";
    }
  }

  static abstract class Optional<T> {
    abstract boolean isPresent();

    abstract T get();

    static Optional absent() {
      return new Absent();
    }

    static <T> Optional<T> of(T object) {
      return new Present(object);
    }
  }

  static class Absent<T> extends Optional<T> {
    @Override
    boolean isPresent() {
      return false;
    }

    @Override
    T get() {
      throw new IllegalStateException("Optional.get() cannot be called on an absent value");
    }
  }

  static class Present<T> extends Optional<T> {
    T obj;

    Present(T obj) {
      this.obj = obj;
    }

    @Override
    boolean isPresent() {
      return true;
    }

    @Override
    T get() {
      return obj;
    }
  }
}
