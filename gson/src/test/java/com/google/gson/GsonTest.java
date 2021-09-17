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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

  static class WithNoArgConstructor {
    boolean calledConstructor = false;
    WithNoArgConstructor() {
      calledConstructor = true;
    }
  }
  static class WithoutNoArgConstructor {
    boolean calledConstructor = false;
    WithoutNoArgConstructor(int i) {
      calledConstructor = true;
    }
  }
  public void testCreateInstance_Default() {
    Gson gson = new Gson();

    {
      ArrayList<String> list = gson.createInstance(new TypeToken<ArrayList<String>>() {});
      assertNotNull(list);
      assertEquals(0, list.size());
    }

    {
      WithNoArgConstructor instance = gson.createInstance(TypeToken.get(WithNoArgConstructor.class));
      assertNotNull(instance);
      // Verify that instance constructor was called (instead of being created using Unsafe)
      assertTrue(instance.calledConstructor);
    }

    {
      WithoutNoArgConstructor instance = gson.createInstance(TypeToken.get(WithoutNoArgConstructor.class));
      assertNotNull(instance);
      // Instance was created using Unsafe
      assertFalse(instance.calledConstructor);
    }
  }

  public void testCreateInstance_Custom() {
    @SuppressWarnings("serial")
    class MyListClass extends ArrayList<String> { }
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(TypeToken.get(List.class).getType(), new InstanceCreator<List<?>>() {
        @Override
        public List<?> createInstance(Type type) {
          return new MyListClass();
        }
      })
      .create();

    List<?> instance = gson.createInstance(TypeToken.get(List.class));
    assertTrue(instance instanceof MyListClass);
  }
}
