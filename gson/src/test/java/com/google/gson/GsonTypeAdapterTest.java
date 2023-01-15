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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;

/**
 * Contains numerous tests involving registered type converters with a Gson instance.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class GsonTypeAdapterTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(AtomicLong.class, new ExceptionTypeAdapter())
        .registerTypeAdapter(AtomicInteger.class, new AtomicIntegerTypeAdapter())
        .create();
  }

  @Test
  public void testDefaultTypeAdapterThrowsParseException() throws Exception {
    try {
      gson.fromJson("{\"abc\":123}", BigInteger.class);
      fail("Should have thrown a JsonParseException");
    } catch (JsonParseException expected) { }
  }

  @Test
  public void testTypeAdapterThrowsException() throws Exception {
    try {
      gson.toJson(new AtomicLong(0));
      fail("Type Adapter should have thrown an exception");
    } catch (IllegalStateException expected) { }

    // Verify that serializer is made null-safe, i.e. it is not called for null
    assertThat(gson.toJson(null, AtomicLong.class)).isEqualTo("null");

    try {
      gson.fromJson("123", AtomicLong.class);
      fail("Type Adapter should have thrown an exception");
    } catch (JsonParseException expected) { }

    // Verify that deserializer is made null-safe, i.e. it is not called for null
    assertThat(gson.fromJson(JsonNull.INSTANCE, AtomicLong.class)).isNull();
  }

  @Test
  public void testTypeAdapterProperlyConvertsTypes() {
    int intialValue = 1;
    AtomicInteger atomicInt = new AtomicInteger(intialValue);
    String json = gson.toJson(atomicInt);
    assertThat(Integer.parseInt(json)).isEqualTo(intialValue + 1);

    atomicInt = gson.fromJson(json, AtomicInteger.class);
    assertThat(atomicInt.get()).isEqualTo(intialValue);
  }

  @Test
  public void testTypeAdapterDoesNotAffectNonAdaptedTypes() {
    String expected = "blah";
    String actual = gson.toJson(expected);
    assertThat(actual).isEqualTo("\"" + expected + "\"");

    actual = gson.fromJson(actual, String.class);
    assertThat(actual).isEqualTo(expected);
  }

  private static class ExceptionTypeAdapter
      implements JsonSerializer<AtomicLong>, JsonDeserializer<AtomicLong> {
    @Override public JsonElement serialize(
        AtomicLong src, Type typeOfSrc, JsonSerializationContext context) {
      throw new IllegalStateException();
    }
    @Override public AtomicLong deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      throw new IllegalStateException();
    }
  }

  private static class AtomicIntegerTypeAdapter
      implements JsonSerializer<AtomicInteger>, JsonDeserializer<AtomicInteger> {
    @Override public JsonElement serialize(AtomicInteger src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.incrementAndGet());
    }

    @Override public AtomicInteger deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      int intValue = json.getAsInt();
      return new AtomicInteger(--intValue);
    }
  }

  static abstract class Abstract {
    String a;
  }

  static class Concrete extends Abstract {
    String b;
  }

  // https://groups.google.com/d/topic/google-gson/EBmOCa8kJPE/discussion
  @Test
  public void testDeserializerForAbstractClass() {
    Concrete instance = new Concrete();
    instance.a = "android";
    instance.b = "beep";
    assertSerialized("{\"a\":\"android\"}", Abstract.class, true, true, instance);
    assertSerialized("{\"a\":\"android\"}", Abstract.class, true, false, instance);
    assertSerialized("{\"a\":\"android\"}", Abstract.class, false, true, instance);
    assertSerialized("{\"a\":\"android\"}", Abstract.class, false, false, instance);
    assertSerialized("{\"b\":\"beep\",\"a\":\"android\"}", Concrete.class, true, true, instance);
    assertSerialized("{\"b\":\"beep\",\"a\":\"android\"}", Concrete.class, true, false, instance);
    assertSerialized("{\"b\":\"beep\",\"a\":\"android\"}", Concrete.class, false, true, instance);
    assertSerialized("{\"b\":\"beep\",\"a\":\"android\"}", Concrete.class, false, false, instance);
  }

  private void assertSerialized(String expected, Class<?> instanceType, boolean registerAbstractDeserializer,
      boolean registerAbstractHierarchyDeserializer, Object instance) {
    JsonDeserializer<Abstract> deserializer = new JsonDeserializer<Abstract>() {
      @Override public Abstract deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        throw new AssertionError();
      }
    };
    GsonBuilder builder = new GsonBuilder();
    if (registerAbstractDeserializer) {
      builder.registerTypeAdapter(Abstract.class, deserializer);
    }
    if (registerAbstractHierarchyDeserializer) {
      builder.registerTypeHierarchyAdapter(Abstract.class, deserializer);
    }
    Gson gson = builder.create();
    assertThat(gson.toJson(instance, instanceType)).isEqualTo(expected);
  }
}
