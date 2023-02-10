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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson.FutureTypeAdapter;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

/**
 * Unit tests for {@link Gson}.
 *
 * @author Ryan Harter
 */
public final class GsonTest {

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

  @Test
  public void testOverridesDefaultExcluder() {
    Gson gson = new Gson(CUSTOM_EXCLUDER, CUSTOM_FIELD_NAMING_STRATEGY,
        new HashMap<Type, InstanceCreator<?>>(), true, false, true, false,
        FormattingStyle.DEFAULT, true, false, true,
        LongSerializationPolicy.DEFAULT, null, DateFormat.DEFAULT,
        DateFormat.DEFAULT, new ArrayList<TypeAdapterFactory>(),
        new ArrayList<TypeAdapterFactory>(), new ArrayList<TypeAdapterFactory>(),
        CUSTOM_OBJECT_TO_NUMBER_STRATEGY, CUSTOM_NUMBER_TO_NUMBER_STRATEGY,
        Collections.<ReflectionAccessFilter>emptyList());

    assertThat(gson.excluder).isEqualTo(CUSTOM_EXCLUDER);
    assertThat(gson.fieldNamingStrategy()).isEqualTo(CUSTOM_FIELD_NAMING_STRATEGY);
    assertThat(gson.serializeNulls()).isTrue();
    assertThat(gson.htmlSafe()).isFalse();
  }

  @Test
  public void testClonedTypeAdapterFactoryListsAreIndependent() {
    Gson original = new Gson(CUSTOM_EXCLUDER, CUSTOM_FIELD_NAMING_STRATEGY,
        new HashMap<Type, InstanceCreator<?>>(), true, false, true, false,
        FormattingStyle.DEFAULT, true, false, true,
        LongSerializationPolicy.DEFAULT, null, DateFormat.DEFAULT,
        DateFormat.DEFAULT, new ArrayList<TypeAdapterFactory>(),
        new ArrayList<TypeAdapterFactory>(), new ArrayList<TypeAdapterFactory>(),
        CUSTOM_OBJECT_TO_NUMBER_STRATEGY, CUSTOM_NUMBER_TO_NUMBER_STRATEGY,
        Collections.<ReflectionAccessFilter>emptyList());

    Gson clone = original.newBuilder()
        .registerTypeAdapter(Object.class, new TestTypeAdapter())
        .create();

    assertThat(clone.factories).hasSize(original.factories.size() + 1);
  }

  private static final class TestTypeAdapter extends TypeAdapter<Object> {
    @Override public void write(JsonWriter out, Object value) {
      // Test stub.
    }
    @Override public Object read(JsonReader in) { return null; }
  }

  @Test
  public void testGetAdapter_Null() {
    Gson gson = new Gson();
    try {
      gson.getAdapter((TypeToken<?>) null);
      fail();
    } catch (NullPointerException e) {
      assertThat(e).hasMessageThat().isEqualTo("type must not be null");
    }
  }

  @Test
  public void testGetAdapter_Concurrency() {
    class DummyAdapter<T> extends TypeAdapter<T> {
      @Override public void write(JsonWriter out, T value) throws IOException {
        throw new AssertionError("not needed for this test");
      }

      @Override public T read(JsonReader in) throws IOException {
        throw new AssertionError("not needed for this test");
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
    assertThat(adapterInstancesCreated.get()).isEqualTo(2);
    assertThat(adapter).isInstanceOf(DummyAdapter.class);
    assertThat(threadAdapter.get()).isInstanceOf(DummyAdapter.class);
  }

  /**
   * Verifies that two threads calling {@link Gson#getAdapter(TypeToken)} do not see the
   * same unresolved {@link FutureTypeAdapter} instance, since that would not be thread-safe.
   *
   * This test constructs the cyclic dependency {@literal CustomClass1 -> CustomClass2 -> CustomClass1}
   * and lets one thread wait after the adapter for CustomClass2 has been obtained (which still
   * refers to the nested unresolved FutureTypeAdapter for CustomClass1).
   */
  @Test
  public void testGetAdapter_FutureAdapterConcurrency() throws Exception {
    /**
     * Adapter which wraps another adapter. Can be imagined as a simplified version of the
     * {@code ReflectiveTypeAdapterFactory$Adapter}.
     */
    class WrappingAdapter<T> extends TypeAdapter<T> {
      final TypeAdapter<?> wrapped;
      boolean isFirstCall = true;

      WrappingAdapter(TypeAdapter<?> wrapped) {
        this.wrapped = wrapped;
      }

      @Override public void write(JsonWriter out, T value) throws IOException {
        // Due to how this test is set up there is infinite recursion, therefore
        // need to track how deeply nested this call is
        if (isFirstCall) {
          isFirstCall = false;
          out.beginArray();
          wrapped.write(out, null);
          out.endArray();
          isFirstCall = true;
        } else {
          out.value("wrapped-nested");
        }
      }

      @Override public T read(JsonReader in) throws IOException {
        throw new AssertionError("not needed for this test");
      }
    }

    final CountDownLatch isThreadWaiting = new CountDownLatch(1);
    final CountDownLatch canThreadProceed = new CountDownLatch(1);

    final Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new TypeAdapterFactory() {
        // volatile instead of AtomicBoolean is safe here because CountDownLatch prevents
        // "true" concurrency
        volatile boolean isFirstCaller = true;

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
          Class<?> raw = type.getRawType();

          if (raw == CustomClass1.class) {
            // Retrieves a WrappingAdapter containing a nested FutureAdapter for CustomClass1
            TypeAdapter<?> adapter = gson.getAdapter(CustomClass2.class);

            // Let thread wait so the FutureAdapter for CustomClass1 nested in the adapter
            // for CustomClass2 is not resolved yet
            if (isFirstCaller) {
              isFirstCaller = false;
              isThreadWaiting.countDown();

              try {
                canThreadProceed.await();
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            }

            return new WrappingAdapter<>(adapter);
          }
          else if (raw == CustomClass2.class) {
            TypeAdapter<?> adapter = gson.getAdapter(CustomClass1.class);
            assertThat(adapter).isInstanceOf(FutureTypeAdapter.class);
            return new WrappingAdapter<>(adapter);
          }
          else {
            throw new AssertionError("Adapter for unexpected type requested: " + raw);
          }
        }
      })
      .create();

    final AtomicReference<TypeAdapter<?>> otherThreadAdapter = new AtomicReference<>();
    Thread thread = new Thread() {
      @Override
      public void run() {
        otherThreadAdapter.set(gson.getAdapter(CustomClass1.class));
      }
    };
    thread.start();

    // Wait until other thread has obtained FutureAdapter
    isThreadWaiting.await();
    TypeAdapter<?> adapter = gson.getAdapter(CustomClass1.class);
    // Should not fail due to referring to unresolved FutureTypeAdapter
    assertThat(adapter.toJson(null)).isEqualTo("[[\"wrapped-nested\"]]");

    // Let other thread proceed and have it resolve its FutureTypeAdapter
    canThreadProceed.countDown();
    thread.join();
    assertThat(otherThreadAdapter.get().toJson(null)).isEqualTo("[[\"wrapped-nested\"]]");
  }

  @Test
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
      assertThat(expected).hasMessageThat().isEqualTo("JSON must have only one top-level value.");
    }

    jsonWriter.close();
    assertThat(writer.toString()).isEqualTo("{\"\\u003ctest2\":true}");
  }

  @Test
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
    assertThat(writer.toString()).isEqualTo(")]}'\n{\n  \"test\": null,\n  \"<test2\": true\n}1");
  }

  @Test
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

  @Test
  public void testNewJsonReader_Custom() throws IOException {
    String json = "test"; // String without quotes
    JsonReader jsonReader = new GsonBuilder()
      .setLenient()
      .create()
      .newJsonReader(new StringReader(json));
    assertThat(jsonReader.nextString()).isEqualTo("test");
    jsonReader.close();
  }

  /**
   * Modifying a GsonBuilder obtained from {@link Gson#newBuilder()} of a
   * {@code new Gson()} should not affect the Gson instance it came from.
   */
  @Test
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
    assertThat(json1).isEqualTo("{}");

    // Should use default reflective adapter
    String json2 = gson.toJson(new CustomClass2());
    assertThat(json2).isEqualTo("{}");

    // Should use default instance creator
    CustomClass3 customClass3 = gson.fromJson("{}", CustomClass3.class);
    assertThat(customClass3.s).isEqualTo(CustomClass3.NO_ARG_CONSTRUCTOR_VALUE);
  }

  /**
   * Modifying a GsonBuilder obtained from {@link Gson#newBuilder()} of a custom
   * Gson instance (created using a GsonBuilder) should not affect the Gson instance
   * it came from.
   */
  @Test
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
    assertThat(json1).isEqualTo("\"overwritten custom-adapter\"");

    String json2 = otherGson.toJson(new CustomClass2());
    assertThat(json2).isEqualTo("\"overwritten custom-hierarchy-adapter\"");

    CustomClass3 customClass3 = otherGson.fromJson("{}", CustomClass3.class);
    assertThat(customClass3.s).isEqualTo("overwritten custom-instance");
  }

  private static void assertCustomGson(Gson gson) {
    String json1 = gson.toJson(new CustomClass1());
    assertThat(json1).isEqualTo("\"custom-adapter\"");

    String json2 = gson.toJson(new CustomClass2());
    assertThat(json2).isEqualTo("\"custom-hierarchy-adapter\"");

    CustomClass3 customClass3 = gson.fromJson("{}", CustomClass3.class);
    assertThat(customClass3.s).isEqualTo("custom-instance");
  }

  private static class CustomClass1 { }
  private static class CustomClass2 { }
  private static class CustomClass3 {
    static final String NO_ARG_CONSTRUCTOR_VALUE = "default instance";

    final String s;

    public CustomClass3(String s) {
      this.s = s;
    }

    @SuppressWarnings("unused") // called by Gson
    public CustomClass3() {
      this(NO_ARG_CONSTRUCTOR_VALUE);
    }
  }
}
