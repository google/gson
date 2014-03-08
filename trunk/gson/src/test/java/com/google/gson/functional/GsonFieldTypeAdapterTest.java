/*
 * Copyright (C) 2014 Google Inc.
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

package com.google.gson.functional;

import java.io.IOException;
import java.lang.reflect.Type;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Functional tests for the magic field GSON_TYPE_ADAPTER present in a class.
 */
@SuppressWarnings("unused")
public final class GsonFieldTypeAdapterTest extends TestCase {

  public void testFieldAdapterInvoked() {
    String json = new Gson().toJson(new ClassWithFieldAdapter("bar"));
    assertEquals("\"fieldAdapter\"", json);
  }

  public void testRegisteredAdapterOverridesFieldAdapter() {
    TypeAdapter<ClassWithFieldAdapter> typeAdapter = new TypeAdapter<ClassWithFieldAdapter>() {
      @Override public void write(JsonWriter out, ClassWithFieldAdapter value) throws IOException {
        out.value("registeredAdapter");
      }
      @Override public ClassWithFieldAdapter read(JsonReader in) throws IOException {
        return new ClassWithFieldAdapter(in.nextString());
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(ClassWithFieldAdapter.class, typeAdapter)
      .create();
    String json = gson.toJson(new ClassWithFieldAdapter("abcd"));
    assertEquals("\"registeredAdapter\"", json);
  }

  /**
   * The serializer overrides field adapter, but for deserializer the fieldAdapter is used.
   */
  public void testRegisteredSerializerOverridesFieldAdapter() {
    JsonSerializer<ClassWithFieldAdapter> serializer = new JsonSerializer<ClassWithFieldAdapter>() {
      public JsonElement serialize(ClassWithFieldAdapter src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive("registeredSerializer");
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(ClassWithFieldAdapter.class, serializer)
      .create();
    String json = gson.toJson(new ClassWithFieldAdapter("abcd"));
    assertEquals("\"registeredSerializer\"", json);
    ClassWithFieldAdapter target = gson.fromJson("abcd", ClassWithFieldAdapter.class);
    assertEquals("fieldAdapter", target.value);
  }

  /**
   * The deserializer overrides field adapter, but for serializer the fieldAdapter is used.
   */
  public void testRegisteredDeserializerOverridesFieldAdapter() {
    JsonDeserializer<ClassWithFieldAdapter> deserializer = new JsonDeserializer<ClassWithFieldAdapter>() {
      public ClassWithFieldAdapter deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        return new ClassWithFieldAdapter("registeredDeserializer");
      }
    };
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(ClassWithFieldAdapter.class, deserializer)
      .create();
    String json = gson.toJson(new ClassWithFieldAdapter("abcd"));
    assertEquals("\"fieldAdapter\"", json);
    ClassWithFieldAdapter target = gson.fromJson("abcd", ClassWithFieldAdapter.class);
    assertEquals("registeredDeserializer", target.value);
  }

  public void testFieldAdapterNotInvokedIfNull() {
    String json = new Gson().toJson(new ClassWithNullFieldAdapter("bar"));
    assertEquals("{\"value\":\"bar\"}", json);
  }

  public void testNonStaticFieldAdapterNotInvoked() {
    String json = new Gson().toJson(new ClassWithNonStaticFieldAdapter("bar"));
    assertFalse(json.contains("fieldAdapter"));
  }

  public void testIncorrectTypeAdapterNotInvoked() {
    String json = new Gson().toJson(new ClassWithIncorrectFieldAdapter("bar"));
    assertFalse(json.contains("fieldAdapter"));
  }

  public void testSuperclassTypeAdapterNotInvoked() {
    String json = new Gson().toJson(new ClassWithSuperClassFieldAdapter("bar"));
    assertFalse(json.contains("fieldAdapter"));
  }

  private static class ClassWithFieldAdapter {
    final String value;
    ClassWithFieldAdapter(String value) {
      this.value = value;
    }
    private static final TypeAdapter<ClassWithFieldAdapter> GSON_TYPE_ADAPTER =
        new TypeAdapter<GsonFieldTypeAdapterTest.ClassWithFieldAdapter>() {
      @Override public void write(JsonWriter out, ClassWithFieldAdapter value) throws IOException {
        out.value("fieldAdapter");
      }
      @Override public ClassWithFieldAdapter read(JsonReader in) throws IOException {
        in.nextString();
        return new ClassWithFieldAdapter("fieldAdapter");
      }
    };
  }

  private static final class ClassWithSuperClassFieldAdapter extends ClassWithFieldAdapter {
    ClassWithSuperClassFieldAdapter(String value) {
      super(value);
    }
  }

  private static final class ClassWithNullFieldAdapter {
    final String value;
    ClassWithNullFieldAdapter(String value) {
      this.value = value;
    }
    private static final TypeAdapter<ClassWithFieldAdapter> GSON_TYPE_ADAPTER = null;
  }

  private static final class ClassWithNonStaticFieldAdapter {
    final String value;
    ClassWithNonStaticFieldAdapter(String value) {
      this.value = value;
    }
    private final TypeAdapter<ClassWithNonStaticFieldAdapter> GSON_TYPE_ADAPTER =
        new TypeAdapter<ClassWithNonStaticFieldAdapter>() {
      @Override public void write(JsonWriter out, ClassWithNonStaticFieldAdapter value) throws IOException {
        out.value("fieldAdapter");
      }
      @Override public ClassWithNonStaticFieldAdapter read(JsonReader in) throws IOException {
        in.nextString();
        return new ClassWithNonStaticFieldAdapter("fieldAdapter");
      }
    };
  }

  private static final class ClassWithIncorrectFieldAdapter {
    final String value;
    ClassWithIncorrectFieldAdapter(String value) {
      this.value = value;
    }
    // Note that the type is NOT TypeAdapter<ClassWithIncorrectFieldAdapter> so this
    // field should be ignored.
    private static final TypeAdapter<ClassWithFieldAdapter> GSON_TYPE_ADAPTER =
        new TypeAdapter<GsonFieldTypeAdapterTest.ClassWithFieldAdapter>() {
      @Override public void write(JsonWriter out, ClassWithFieldAdapter value) throws IOException {
        out.value("fieldAdapter");
      }
      @Override public ClassWithFieldAdapter read(JsonReader in) throws IOException {
        in.nextString();
        return new ClassWithFieldAdapter("fieldAdapter");
      }
    };
  }
}
