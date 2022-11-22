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
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
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
        CUSTOM_OBJECT_TO_NUMBER_STRATEGY, CUSTOM_NUMBER_TO_NUMBER_STRATEGY);

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
        CUSTOM_OBJECT_TO_NUMBER_STRATEGY, CUSTOM_NUMBER_TO_NUMBER_STRATEGY);

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
   * Verifies that {@link Gson#getAdapter(TypeToken)} does not put broken adapters
   * into {@code typeTokenCache} when caller of nested {@code getAdapter} discards
   * exception, e.g.:
   *
   * Field dependencies:
   * ClassA
   *   -> ClassB1
   *     -> ClassC -> ClassB1
   *     -> ClassX
   *   | ClassB2
   *
   * Let's assume the factory for ClassX throws an exception.
   * 1. Factory for ClassA finds field of type ClassB1
   * 2. Factory for ClassB1 finds field of type ClassC
   * 3. Factory for ClassC find fields of type ClassB1 => stores future adapter
   * 4. Factory for ClassB1 finds field of type ClassX => ClassX factory throws exception
   * 5. Factory for ClassA ignores exception from getAdapter(ClassB1) and tries as alternative getting
   *    adapter for ClassB2
   *
   * Then Gson must not cache adapter for ClassC because it refers to broken adapter
   * for ClassB1 (since ClassX threw exception).
   */
  public void testGetAdapterDiscardedException() throws Exception {
    final TypeAdapter<?> alternativeAdapter = new DummyAdapter<>();
    final AtomicReference<TypeAdapter<?>> leakedAdapter = new AtomicReference<>();

    Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
          if (type.getRawType() == CustomClassA.class) {
            // Factory will throw for CustomClassB1; discard exception
            try {
              gson.getAdapter(CustomClassB1.class);
              fail("Expected exception");
            } catch (Exception e) {
              assertEquals("test exception", e.getMessage());
            }

            @SuppressWarnings("unchecked")
            TypeAdapter<T> adapter = (TypeAdapter<T>) alternativeAdapter;
            return adapter;
          }
          else if (type.getRawType() == CustomClassB1.class) {
            gson.getAdapter(CustomClassC.class);
            // Will throw exception
            gson.getAdapter(CustomClassX.class);

            throw new AssertionError("Factory should have thrown exception for CustomClassX");
          }
          else if (type.getRawType() == CustomClassC.class) {
            // Will return future adapter due to cyclic dependency B1 -> C -> B1
            TypeAdapter<?> adapter = gson.getAdapter(CustomClassB1.class);
            assertTrue(adapter instanceof FutureTypeAdapter);
            // Pretend this factory somehow leaks this FutureTypeAdapter
            leakedAdapter.set(adapter);
            return new DummyAdapter<T>();
          }
          else if (type.getRawType() == CustomClassX.class) {
            // Always throw exception
            throw new RuntimeException("test exception");
          }

          throw new AssertionError("Requested adapter for unexpected type: " + type);
        }
      })
      .create();

    assertSame(alternativeAdapter, gson.getAdapter(CustomClassA.class));
    // Gson must not have cached broken adapters for CustomClassB1 and CustomClassC
    try {
      gson.getAdapter(CustomClassB1.class);
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("test exception", e.getMessage());
    }
    try {
      gson.getAdapter(CustomClassC.class);
      fail("Expected exception");
    } catch (Exception e) {
      assertEquals("test exception", e.getMessage());
    }

    // Leaked adapter should have been marked as "broken"
    try {
      leakedAdapter.get().fromJson("{}");
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertEquals("Broken adapter has been leaked by TypeAdapterFactory", e.getMessage());
    }
  }

  /**
   * Verifies that two threads calling {@link Gson#getAdapter(TypeToken)} do not see the
   * same unresolved {@link FutureTypeAdapter} instance, since that would not be thread-safe.
   *
   * This test constructs the cyclic dependency CustomClassA -> CustomClassB1 -> CustomClassA
   * and lets one thread wait after the adapter for CustomClassB1 has been obtained (which still
   * contains the nested unresolved FutureTypeAdapter for CustomClassA).
   */
  public void testGetAdapterFutureAdapterConcurrency() throws Exception {
    /**
     * Adapter which wraps another adapter. Can be imagined as a simplified version of the
     * ReflectiveTypeAdapterFactory$Adapter.
     */
    class WrappingAdapter<T> extends TypeAdapter<T> {
      final TypeAdapter<?> wrapped;
      int callCount = 0;

      WrappingAdapter(TypeAdapter<?> wrapped) {
        this.wrapped = wrapped;
      }

      @Override public void write(JsonWriter out, T value) throws IOException {
        // Due to how this test is set up there is infinite recursion, therefore
        // need to track how deeply nested this call is
        if (callCount == 0) {
          callCount++;
          out.beginArray();
          wrapped.write(out, null);
          out.endArray();
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

          if (raw == CustomClassA.class) {
            // Retrieves a WrappingAdapter containing a nested FutureAdapter for CustomClassA
            TypeAdapter<?> adapter = gson.getAdapter(CustomClassB1.class);

            // Let thread wait so the FutureAdapter for CustomClassA nested in the adapter
            // for CustomClassB1 has not been resolved yet
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
          else if (raw == CustomClassB1.class) {
            TypeAdapter<?> adapter = gson.getAdapter(CustomClassA.class);
            assertTrue(adapter instanceof FutureTypeAdapter);
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
        otherThreadAdapter.set(gson.getAdapter(CustomClassA.class));
      }
    };
    thread.start();

    // Wait until other thread has obtained FutureAdapter
    isThreadWaiting.await();
    TypeAdapter<?> adapter = gson.getAdapter(CustomClassA.class);
    // Should not fail due to referring to unresolved FutureTypeAdapter
    assertEquals("[[\"wrapped-nested\"]]", adapter.toJson(null));

    // Let other thread proceed and have it resolve its FutureTypeAdapter
    canThreadProceed.countDown();
    thread.join();
    assertEquals("[[\"wrapped-nested\"]]", otherThreadAdapter.get().toJson(null));
  }

  private static class DummyAdapter<T> extends TypeAdapter<T> {
    @Override public void write(JsonWriter out, T value) throws IOException {
      throw new AssertionError("not needed for this test");
    }

    @Override public T read(JsonReader in) throws IOException {
      throw new AssertionError("not needed for this test");
    }
  }

  private static class CustomClassA {
  }
  private static class CustomClassB1 {
  }
  private static class CustomClassC {
  }
  private static class CustomClassX {
  }
}
