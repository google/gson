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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

  private static final ToNumberStrategy CUSTOM_OBJECT_TO_NUMBER_STRATEGY = ToNumberPolicy.DOUBLE;
  private static final ToNumberStrategy CUSTOM_NUMBER_TO_NUMBER_STRATEGY = ToNumberPolicy.LAZILY_PARSED_NUMBER;

  public void testOverridesDefaultExcluder() {
    Gson gson = new Gson(CUSTOM_EXCLUDER, CUSTOM_FIELD_NAMING_STRATEGY,
        new HashMap<Type, InstanceCreator<?>>(), true, false, true, false,
        true, true, false, true, LongSerializationPolicy.DEFAULT, null, DateFormat.DEFAULT,
        DateFormat.DEFAULT, new ArrayList<TypeAdapterFactory>(),
        new ArrayList<TypeAdapterFactory>(), new ArrayList<TypeAdapterFactory>(),
        CUSTOM_OBJECT_TO_NUMBER_STRATEGY, CUSTOM_NUMBER_TO_NUMBER_STRATEGY,
        Collections.<ReflectionAccessFilter>emptyList());

    assertEquals(CUSTOM_EXCLUDER, gson.excluder);
    assertEquals(CUSTOM_FIELD_NAMING_STRATEGY, gson.fieldNamingStrategy());
    assertEquals(true, gson.serializeNulls());
    assertEquals(false, gson.htmlSafe());
  }

  public void testClonedTypeAdapterFactoryListsAreIndependent() {
    Gson original = new Gson(CUSTOM_EXCLUDER, CUSTOM_FIELD_NAMING_STRATEGY,
        new HashMap<Type, InstanceCreator<?>>(), true, false, true, false,
        true, true, false, true, LongSerializationPolicy.DEFAULT, null, DateFormat.DEFAULT,
        DateFormat.DEFAULT, new ArrayList<TypeAdapterFactory>(),
        new ArrayList<TypeAdapterFactory>(), new ArrayList<TypeAdapterFactory>(),
        CUSTOM_OBJECT_TO_NUMBER_STRATEGY, CUSTOM_NUMBER_TO_NUMBER_STRATEGY,
        Collections.<ReflectionAccessFilter>emptyList());

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

  public void testGetAdapter_Null() {
    Gson gson = new Gson();
    try {
      gson.getAdapter((TypeToken<?>) null);
      fail();
    } catch (NullPointerException e) {
      assertEquals("type must not be null", e.getMessage());
    }
  }

  public void testGetAdapter_Concurrency() {
    class DummyAdapter<T> extends TypeAdapter<T> {
      @Override public void write(JsonWriter out, T value) throws IOException {
        throw new AssertionError("not needed for test");
      }

      @Override public T read(JsonReader in) throws IOException {
        throw new AssertionError("not needed for test");
      }
    }

    final AtomicInteger adapterInstancesCreated = new AtomicInteger(0);
    final AtomicReference<TypeAdapter<?>> threadAdapter = new AtomicReference<>();
    final Class<?> requestedType = Number.class;

    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new TypeAdapterFactory() {
          private volatile boolean isFirstCall = true;

          @Override public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
            if (isFirstCall) {
              isFirstCall = false;

              // Create a separate thread which requests an adapter for the same type
              // This will cause this factory to return a different adapter instance than
              // the one it is currently creating
              Thread thread = new Thread() {
                @Override public void run() {
                  threadAdapter.set(gson.getAdapter(requestedType));
                }
              };
              thread.start();
              try {
                thread.join();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            }

            // Create a new dummy adapter instance
            adapterInstancesCreated.incrementAndGet();
            return new DummyAdapter<>();
          }
        })
        .create();

    TypeAdapter<?> adapter = gson.getAdapter(requestedType);
    assertTrue(adapter instanceof DummyAdapter);
    assertEquals(2, adapterInstancesCreated.get());
    // Should be the same adapter instance the concurrent thread received
    assertSame(threadAdapter.get(), adapter);
  }

  public void testNewJsonWriter_Default() throws IOException {
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new Gson().newJsonWriter(writer);
    jsonWriter.beginObject();
    jsonWriter.name("test");
    jsonWriter.nullValue();
    jsonWriter.name("<test2");
    jsonWriter.value(true);
    jsonWriter.endObject();

    try {
      // Additional top-level value
      jsonWriter.value(1);
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("JSON must have only one top-level value.", expected.getMessage());
    }

    jsonWriter.close();
    assertEquals("{\"\\u003ctest2\":true}", writer.toString());
  }

  public void testNewJsonWriter_Custom() throws IOException {
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new GsonBuilder()
      .disableHtmlEscaping()
      .generateNonExecutableJson()
      .setPrettyPrinting()
      .serializeNulls()
      .setLenient()
      .create()
      .newJsonWriter(writer);
    jsonWriter.beginObject();
    jsonWriter.name("test");
    jsonWriter.nullValue();
    jsonWriter.name("<test2");
    jsonWriter.value(true);
    jsonWriter.endObject();

    // Additional top-level value
    jsonWriter.value(1);

    jsonWriter.close();
    assertEquals(")]}'\n{\n  \"test\": null,\n  \"<test2\": true\n}1", writer.toString());
  }

  public void testNewJsonReader_Default() throws IOException {
    String json = "test"; // String without quotes
    JsonReader jsonReader = new Gson().newJsonReader(new StringReader(json));
    try {
      jsonReader.nextString();
      fail();
    } catch (MalformedJsonException expected) {
    }
    jsonReader.close();
  }

  public void testNewJsonReader_Custom() throws IOException {
    String json = "test"; // String without quotes
    JsonReader jsonReader = new GsonBuilder()
      .setLenient()
      .create()
      .newJsonReader(new StringReader(json));
    assertEquals("test", jsonReader.nextString());
    jsonReader.close();
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
