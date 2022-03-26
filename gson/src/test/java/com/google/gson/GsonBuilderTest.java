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

package com.google.gson;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import junit.framework.TestCase;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Unit tests for {@link GsonBuilder}.
 *
 * @author Inderjeet Singh
 */
public class GsonBuilderTest extends TestCase {
  private static final TypeAdapter<Object> NULL_TYPE_ADAPTER = new TypeAdapter<Object>() {
    @Override public void write(JsonWriter out, Object value) {
      throw new AssertionError();
    }
    @Override public Object read(JsonReader in) {
      throw new AssertionError();
    }
  };

  public void testCreatingMoreThanOnce() {
    GsonBuilder builder = new GsonBuilder();
    builder.create();
    builder.create();
  }

  private static class HierarchyTest1 { }
  private static class HierarchyTest2 { }

  /**
   * Verifies that multiple type hierarchy adapters for the same type overwrite
   * each other.
   */
  public void testRegisterTypeHierarchyAdapter_Overwriting() {
    JsonSerializer<Object> serializer = new JsonSerializer<Object>() {
      @Override public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive("serializer");
      }
    };
    TypeAdapter<Object> typeAdapter = new TypeAdapter<Object>() {
      @Override public Object read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
      }

      @Override public void write(JsonWriter out, Object value) throws IOException {
        out.value("adapter");
      }
    };

    Gson gson = new GsonBuilder()
      // Overwrite TypeAdapter with JsonSerializer for HierarchyTest1
      .registerTypeHierarchyAdapter(HierarchyTest1.class, typeAdapter)
      .registerTypeHierarchyAdapter(HierarchyTest1.class, serializer)
      // Overwrite JsonSerializer with TypeAdapter for HierarchyTest2
      .registerTypeHierarchyAdapter(HierarchyTest2.class, serializer)
      .registerTypeHierarchyAdapter(HierarchyTest2.class, typeAdapter)
      .create();

    String json1 = gson.toJson(new HierarchyTest1());
    assertEquals("\"serializer\"", json1);

    String json2 = gson.toJson(new HierarchyTest2());
    assertEquals("\"adapter\"", json2);
  }

  /**
   * Verifies that a specific type adapter has higher precedence than any
   * type hierarchy adapters, even if it was registered first.
   */
  public void testRegisterTypeHierarchyAdapter_SpecificAdapterPrecedence() {
    TypeAdapter<Object> typeAdapter = new TypeAdapter<Object>() {
      @Override public Object read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
      }

      @Override public void write(JsonWriter out, Object value) throws IOException {
        out.value("adapter");
      }
    };

    Gson gson = new GsonBuilder()
      // TypeAdapter then hierarchy TypeAdapter for HierarchyTest1
      .registerTypeAdapter(HierarchyTest1.class, typeAdapter)
      .registerTypeHierarchyAdapter(HierarchyTest1.class, new TypeAdapter<HierarchyTest1>() {
        @Override public HierarchyTest1 read(JsonReader in) throws IOException {
          throw new UnsupportedOperationException();
        }

        @Override public void write(JsonWriter out, HierarchyTest1 value) throws IOException {
          out.value("hierarchy-adapter");
        }
      })
      // TypeAdapter then hierarchy JsonSerializer for HierarchyTest2
      .registerTypeAdapter(HierarchyTest2.class, typeAdapter)
      .registerTypeHierarchyAdapter(HierarchyTest2.class, new JsonSerializer<HierarchyTest2>() {
        @Override public JsonElement serialize(HierarchyTest2 src, Type typeOfSrc, JsonSerializationContext context) {
          return new JsonPrimitive("hierarchy-serializer");
        }
      })
      .create();

    String json1 = gson.toJson(new HierarchyTest1());
    assertEquals("\"adapter\"", json1);

    String json2 = gson.toJson(new HierarchyTest2());
    assertEquals("\"adapter\"", json2);
  }

  public void testExcludeFieldsWithModifiers() {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.VOLATILE, Modifier.PRIVATE)
        .create();
    assertEquals("{\"d\":\"d\"}", gson.toJson(new HasModifiers()));
  }

  public void testRegisterTypeAdapterForCoreType() {
    Type[] types = {
        byte.class,
        int.class,
        double.class,
        Short.class,
        Long.class,
        String.class,
    };
    for (Type type : types) {
      new GsonBuilder().registerTypeAdapter(type, NULL_TYPE_ADAPTER);
    }
  }

  @SuppressWarnings("unused")
  static class HasModifiers {
    private String a = "a";
    volatile String b = "b";
    private volatile String c = "c";
    String d = "d";
  }

  public void testTransientFieldExclusion() {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers()
        .create();
    assertEquals("{\"a\":\"a\"}", gson.toJson(new HasTransients()));
  }

  static class HasTransients {
    transient String a = "a";
  }

  public void testDisableJdkUnsafe() {
    Gson gson = new GsonBuilder()
        .disableJdkUnsafe()
        .create();
    try {
      gson.fromJson("{}", ClassWithoutNoArgsConstructor.class);
      fail("Expected exception");
    } catch (JsonIOException expected) {
      assertEquals(
        "Unable to create instance of class com.google.gson.GsonBuilderTest$ClassWithoutNoArgsConstructor; "
        + "usage of JDK Unsafe is disabled. Registering an InstanceCreator or a TypeAdapter for this type, "
        + "adding a no-args constructor, or enabling usage of JDK Unsafe may fix this problem.",
        expected.getMessage()
      );
    }
  }

  private static class ClassWithoutNoArgsConstructor {
    @SuppressWarnings("unused")
    public ClassWithoutNoArgsConstructor(String s) {
    }
  }
}
