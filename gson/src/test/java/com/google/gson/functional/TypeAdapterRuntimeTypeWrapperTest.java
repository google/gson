/*
 * Copyright (C) 2022 Google Inc.
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

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import org.junit.Test;

public class TypeAdapterRuntimeTypeWrapperTest {
  private static class Base {
  }
  private static class Subclass extends Base {
    @SuppressWarnings("unused")
    String f = "test";
  }
  private static class Container {
    @SuppressWarnings("unused")
    Base b = new Subclass();
  }
  private static class Deserializer implements JsonDeserializer<Base> {
    @Override
    public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
      throw new AssertionError("not needed for this test");
    }
  }

  /**
   * When custom {@link JsonSerializer} is registered for Base should
   * prefer that over reflective adapter for Subclass for serialization.
   */
  @Test
  public void testJsonSerializer() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonSerializer<Base>() {
        @Override
        public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
          return new JsonPrimitive("serializer");
        }
      })
      .create();

    String json = gson.toJson(new Container());
    assertThat(json).isEqualTo("{\"b\":\"serializer\"}");
  }

  /**
   * When only {@link JsonDeserializer} is registered for Base, then on
   * serialization should prefer reflective adapter for Subclass since
   * Base would use reflective adapter as delegate.
   */
  @Test
  public void testJsonDeserializer_ReflectiveSerializerDelegate() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new Deserializer())
      .create();

    String json = gson.toJson(new Container());
    assertThat(json).isEqualTo("{\"b\":{\"f\":\"test\"}}");
  }

  /**
   * When {@link JsonDeserializer} with custom adapter as delegate is
   * registered for Base, then on serialization should prefer custom adapter
   * delegate for Base over reflective adapter for Subclass.
   */
  @Test
  public void testJsonDeserializer_CustomSerializerDelegate() {
    Gson gson = new GsonBuilder()
      // Register custom delegate
      .registerTypeAdapter(Base.class, new TypeAdapter<Base>() {
        @Override
        public Base read(JsonReader in) throws IOException {
          throw new UnsupportedOperationException();
        }
        @Override
        public void write(JsonWriter out, Base value) throws IOException {
          out.value("custom delegate");
        }
      })
      .registerTypeAdapter(Base.class, new Deserializer())
      .create();

    String json = gson.toJson(new Container());
    assertThat(json).isEqualTo("{\"b\":\"custom delegate\"}");
  }

  /**
   * When two (or more) {@link JsonDeserializer}s are registered for Base
   * which eventually fall back to reflective adapter as delegate, then on
   * serialization should prefer reflective adapter for Subclass.
   */
  @Test
  public void testJsonDeserializer_ReflectiveTreeSerializerDelegate() {
    Gson gson = new GsonBuilder()
      // Register delegate which itself falls back to reflective serialization
      .registerTypeAdapter(Base.class, new Deserializer())
      .registerTypeAdapter(Base.class, new Deserializer())
      .create();

    String json = gson.toJson(new Container());
    assertThat(json).isEqualTo("{\"b\":{\"f\":\"test\"}}");
  }

  /**
   * When {@link JsonDeserializer} with {@link JsonSerializer} as delegate
   * is registered for Base, then on serialization should prefer
   * {@code JsonSerializer} over reflective adapter for Subclass.
   */
  @Test
  public void testJsonDeserializer_JsonSerializerDelegate() {
    Gson gson = new GsonBuilder()
      // Register JsonSerializer as delegate
      .registerTypeAdapter(Base.class, new JsonSerializer<Base>() {
        @Override
        public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
          return new JsonPrimitive("custom delegate");
        }
      })
      .registerTypeAdapter(Base.class, new Deserializer())
      .create();

    String json = gson.toJson(new Container());
    assertThat(json).isEqualTo("{\"b\":\"custom delegate\"}");
  }

  /**
   * When a {@link JsonDeserializer} is registered for Subclass, and a custom
   * {@link JsonSerializer} is registered for Base, then Gson should prefer
   * the reflective adapter for Subclass for backward compatibility (see
   * https://github.com/google/gson/pull/1787#issuecomment-1222175189) even
   * though normally TypeAdapterRuntimeTypeWrapper should prefer the custom
   * serializer for Base.
   */
  @Test
  public void testJsonDeserializer_SubclassBackwardCompatibility() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Subclass.class, new JsonDeserializer<Subclass>() {
        @Override
        public Subclass deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
          throw new AssertionError("not needed for this test");
        }
      })
      .registerTypeAdapter(Base.class, new JsonSerializer<Base>() {
        @Override
        public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
          return new JsonPrimitive("base");
        }
      })
      .create();

    String json = gson.toJson(new Container());
    assertThat(json).isEqualTo("{\"b\":{\"f\":\"test\"}}");
  }

  private static class CyclicBase {
    @SuppressWarnings("unused")
    CyclicBase f;
  }

  private static class CyclicSub extends CyclicBase {
    @SuppressWarnings("unused")
    int i;

    public CyclicSub(int i) {
      this.i = i;
    }
  }

  /**
   * Tests behavior when the type of a field refers to a type whose adapter is
   * currently in the process of being created. For these cases {@link Gson}
   * uses a future adapter for the type. That adapter later uses the actual
   * adapter as delegate.
   */
  @Test
  public void testGsonFutureAdapter() {
    CyclicBase b = new CyclicBase();
    b.f = new CyclicSub(2);
    String json = new Gson().toJson(b);
    assertThat(json).isEqualTo("{\"f\":{\"i\":2}}");
  }
}
