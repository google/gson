/*
 * Copyright (C) 2016 The Gson Authors
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

import com.google.gson.internal.Excluder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.TestCase;

/**
 * Unit tests for {@link Gson}.
 *
 * @author Ryan Harter
 */
public final class GsonTest extends TestCase {

  private static final Excluder CUSTOM_EXCLUDER = Excluder.DEFAULT
      .excludeFieldsWithoutExposeAnnotation()
      .disableInnerClassSerialization();

  private static final FieldNamingStrategy CUSTOM_FIELD_NAMING_STRATEGY = new FieldNamingStrategy() {
    @Override public String translateName(Field f) {
      return "foo";
    }
  };

  public void testOverridesDefaultExcluder() {
    Gson gson = new Gson(CUSTOM_EXCLUDER, CUSTOM_FIELD_NAMING_STRATEGY,
        new HashMap<Type, InstanceCreator<?>>(), true, false, true, false,
        true, true, false, LongSerializationPolicy.DEFAULT, null, DateFormat.DEFAULT,
        DateFormat.DEFAULT, new ArrayList<TypeAdapterFactory>(),
        new ArrayList<TypeAdapterFactory>(), new ArrayList<TypeAdapterFactory>());

    assertEquals(CUSTOM_EXCLUDER, gson.excluder());
    assertEquals(CUSTOM_FIELD_NAMING_STRATEGY, gson.fieldNamingStrategy());
    assertEquals(true, gson.serializeNulls());
    assertEquals(false, gson.htmlSafe());
  }

  public void testClonedTypeAdapterFactoryListsAreIndependent() {
    Gson original = new Gson(CUSTOM_EXCLUDER, CUSTOM_FIELD_NAMING_STRATEGY,
        new HashMap<Type, InstanceCreator<?>>(), true, false, true, false,
        true, true, false, LongSerializationPolicy.DEFAULT, null, DateFormat.DEFAULT,
        DateFormat.DEFAULT, new ArrayList<TypeAdapterFactory>(),
        new ArrayList<TypeAdapterFactory>(), new ArrayList<TypeAdapterFactory>());

    Gson clone = original.newBuilder()
        .registerTypeAdapter(Object.class, new TestTypeAdapter())
        .create();

    assertEquals(original.factories.size() + 1, clone.factories.size());
  }

  private static final class TestTypeAdapter extends TypeAdapter<Object> {
    @Override public void write(JsonWriter out, Object value) throws IOException {
      // Test stub.
    }
    @Override public Object read(JsonReader in) throws IOException { return null; }
  }

  /**
   * Modifying a GsonBuilder obtained from {@link Gson#newBuilder()} of a
   * {@code new Gson()} should not affect the Gson instance it came from.
   */
  public void testDefaultGsonNewBuilderModification() {
    Gson gson = new Gson();
    GsonBuilder gsonBuilder = gson.newBuilder();

    // Modifications of `gsonBuilder` should not affect `gson` object
    gsonBuilder.registerTypeAdapter(CustomClass1.class, new TypeAdapter<CustomClass1>() {
      @Override public CustomClass1 read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
      }

      @Override public void write(JsonWriter out, CustomClass1 value) throws IOException {
        out.value("custom-adapter");
      }
    });
    gsonBuilder.registerTypeHierarchyAdapter(CustomClass2.class, new JsonSerializer<CustomClass2>() {
      @Override public JsonElement serialize(CustomClass2 src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive("custom-hierarchy-adapter");
      }
    });
    gsonBuilder.registerTypeAdapter(CustomClass3.class, new InstanceCreator<CustomClass3>() {
      @Override public CustomClass3 createInstance(Type type) {
        return new CustomClass3("custom-instance");
      }
    });

    assertDefaultGson(gson);
    // New GsonBuilder created from `gson` should not have been affected by changes either
    assertDefaultGson(gson.newBuilder().create());

    // But new Gson instance from `gsonBuilder` should use custom adapters
    assertCustomGson(gsonBuilder.create());
  }

  private static void assertDefaultGson(Gson gson) {
    // Should use default reflective adapter
    String json1 = gson.toJson(new CustomClass1());
    assertEquals("{}", json1);

    // Should use default reflective adapter
    String json2 = gson.toJson(new CustomClass2());
    assertEquals("{}", json2);

    // Should use default instance creator
    CustomClass3 customClass3 = gson.fromJson("{}", CustomClass3.class);
    assertEquals(CustomClass3.NO_ARG_CONSTRUCTOR_VALUE, customClass3.s);
  }

  /**
   * Modifying a GsonBuilder obtained from {@link Gson#newBuilder()} of a custom
   * Gson instance (created using a GsonBuilder) should not affect the Gson instance
   * it came from.
   */
  public void testNewBuilderModification() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(CustomClass1.class, new TypeAdapter<CustomClass1>() {
        @Override public CustomClass1 read(JsonReader in) throws IOException {
          throw new UnsupportedOperationException();
        }

        @Override public void write(JsonWriter out, CustomClass1 value) throws IOException {
          out.value("custom-adapter");
        }
      })
      .registerTypeHierarchyAdapter(CustomClass2.class, new JsonSerializer<CustomClass2>() {
        @Override public JsonElement serialize(CustomClass2 src, Type typeOfSrc, JsonSerializationContext context) {
          return new JsonPrimitive("custom-hierarchy-adapter");
        }
      })
      .registerTypeAdapter(CustomClass3.class, new InstanceCreator<CustomClass3>() {
        @Override public CustomClass3 createInstance(Type type) {
          return new CustomClass3("custom-instance");
        }
      })
      .create();

    assertCustomGson(gson);

    // Modify `gson.newBuilder()`
    GsonBuilder gsonBuilder = gson.newBuilder();
    gsonBuilder.registerTypeAdapter(CustomClass1.class, new TypeAdapter<CustomClass1>() {
      @Override public CustomClass1 read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
      }

      @Override public void write(JsonWriter out, CustomClass1 value) throws IOException {
        out.value("overwritten custom-adapter");
      }
    });
    gsonBuilder.registerTypeHierarchyAdapter(CustomClass2.class, new JsonSerializer<CustomClass2>() {
      @Override public JsonElement serialize(CustomClass2 src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive("overwritten custom-hierarchy-adapter");
      }
    });
    gsonBuilder.registerTypeAdapter(CustomClass3.class, new InstanceCreator<CustomClass3>() {
      @Override public CustomClass3 createInstance(Type type) {
        return new CustomClass3("overwritten custom-instance");
      }
    });

    // `gson` object should not have been affected by changes to new GsonBuilder
    assertCustomGson(gson);
    // New GsonBuilder based on `gson` should not have been affected either
    assertCustomGson(gson.newBuilder().create());

    // But new Gson instance from `gsonBuilder` should be affected by changes
    Gson otherGson = gsonBuilder.create();
    String json1 = otherGson.toJson(new CustomClass1());
    assertEquals("\"overwritten custom-adapter\"", json1);

    String json2 = otherGson.toJson(new CustomClass2());
    assertEquals("\"overwritten custom-hierarchy-adapter\"", json2);

    CustomClass3 customClass3 = otherGson.fromJson("{}", CustomClass3.class);
    assertEquals("overwritten custom-instance", customClass3.s);
  }

  private static void assertCustomGson(Gson gson) {
    String json1 = gson.toJson(new CustomClass1());
    assertEquals("\"custom-adapter\"", json1);

    String json2 = gson.toJson(new CustomClass2());
    assertEquals("\"custom-hierarchy-adapter\"", json2);

    CustomClass3 customClass3 = gson.fromJson("{}", CustomClass3.class);
    assertEquals("custom-instance", customClass3.s);
  }

  static class CustomClass1 { }
  static class CustomClass2 { }
  static class CustomClass3 {
    static final String NO_ARG_CONSTRUCTOR_VALUE = "default instance";

    final String s;

    public CustomClass3(String s) {
      this.s = s;
    }

    public CustomClass3() {
      this(NO_ARG_CONSTRUCTOR_VALUE);
    }
  }
}
