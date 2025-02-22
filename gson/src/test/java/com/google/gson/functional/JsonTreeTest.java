/*
 * Copyright (C) 2009 Google Inc.
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.internal.LazilyParsedNumber;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for {@link Gson#toJsonTree(Object)} and {@link Gson#toJsonTree(Object,
 * java.lang.reflect.Type)}
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonTreeTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testToJsonTree() {
    BagOfPrimitives bag = new BagOfPrimitives(10L, 5, false, "foo");
    JsonElement json = gson.toJsonTree(bag);
    assertThat(json.isJsonObject()).isTrue();
    JsonObject obj = json.getAsJsonObject();
    Set<Entry<String, JsonElement>> children = obj.entrySet();
    assertThat(children).hasSize(4);
    assertContains(obj, new JsonPrimitive(10L));
    assertContains(obj, new JsonPrimitive(5));
    assertContains(obj, new JsonPrimitive(false));
    assertContains(obj, new JsonPrimitive("foo"));
  }

  @Test
  public void testToJsonTreeObjectType() {
    SubTypeOfBagOfPrimitives bag = new SubTypeOfBagOfPrimitives(10L, 5, false, "foo", 1.4F);
    JsonElement json = gson.toJsonTree(bag, BagOfPrimitives.class);
    assertThat(json.isJsonObject()).isTrue();
    JsonObject obj = json.getAsJsonObject();
    Set<Entry<String, JsonElement>> children = obj.entrySet();
    assertThat(children).hasSize(4);
    assertContains(obj, new JsonPrimitive(10L));
    assertContains(obj, new JsonPrimitive(5));
    assertContains(obj, new JsonPrimitive(false));
    assertContains(obj, new JsonPrimitive("foo"));
  }

  @Test
  public void testJsonTreeToString() {
    SubTypeOfBagOfPrimitives bag = new SubTypeOfBagOfPrimitives(10L, 5, false, "foo", 1.4F);
    String json1 = gson.toJson(bag);
    JsonElement jsonElement = gson.toJsonTree(bag, SubTypeOfBagOfPrimitives.class);
    String json2 = gson.toJson(jsonElement);
    assertThat(json2).isEqualTo(json1);
  }

  @Test
  public void testJsonTreeNull() {
    BagOfPrimitives bag = new BagOfPrimitives(10L, 5, false, null);
    JsonObject jsonElement = (JsonObject) gson.toJsonTree(bag, BagOfPrimitives.class);
    assertThat(jsonElement.has("stringValue")).isFalse();
  }

  @Test
  public void testToJsonTreeNumbers() {
    List<Number> numbers =
        List.of(
            (byte) 1,
            (short) 2,
            3,
            4L,
            5f,
            6d,
            BigInteger.valueOf(7),
            new BigDecimal(8),
            new LazilyParsedNumber("9"));

    Gson gsonSpecialFloats = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    for (Number number : numbers) {
      JsonElement json = gson.toJsonTree(number);
      assertIsNumber(json, number);

      json = gsonSpecialFloats.toJsonTree(number);
      assertIsNumber(json, number);
    }
  }

  /**
   * Tests {@link Gson#toJsonTree(Object)} with {@link AtomicInteger} and {@link AtomicLong}, which
   * should be serialized as non-atomic (and non-mutable) numbers.
   */
  @Test
  public void testToJsonTreeAtomicNumbers() {
    JsonElement json = gson.toJsonTree(new AtomicInteger(1));
    // Current implementation converts int to long, because there is only `JsonWriter#value(long)`
    assertIsNumber(json, 1L);

    json = gson.toJsonTree(new AtomicLong(1));
    assertIsNumber(json, 1L);
  }

  /** Tests numeric conversion when using {@link Gson#toJsonTree(Object, Type)}. */
  @Test
  public void testToJsonTreeNumberConversion() {
    JsonElement json = gson.toJsonTree(1.5f, int.class);
    assertIsNumber(json, 1);

    json = gson.toJsonTree(500, byte.class);
    assertIsNumber(json, (byte) -12);

    json = gson.toJsonTree(1, float.class);
    assertIsNumber(json, 1f);

    json = gson.toJsonTree(1, double.class);
    assertIsNumber(json, 1d);

    Gson gsonSpecialFloats = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    json = gsonSpecialFloats.toJsonTree(1, float.class);
    assertIsNumber(json, 1f);

    json = gsonSpecialFloats.toJsonTree(1, double.class);
    assertIsNumber(json, 1d);
  }

  private static void assertContains(JsonObject json, JsonPrimitive child) {
    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
      JsonElement node = entry.getValue();
      if (node.isJsonPrimitive()) {
        if (node.getAsJsonPrimitive().equals(child)) {
          return;
        }
      }
    }
    throw new AssertionError("Does not contain " + child);
  }

  private static void assertIsNumber(JsonElement json, Number expectedNumber) {
    assertThat(json).isInstanceOf(JsonPrimitive.class);
    JsonPrimitive jsonPrimitive = (JsonPrimitive) json;
    assertThat(jsonPrimitive.isNumber()).isTrue();
    Number number = jsonPrimitive.getAsNumber();
    assertThat(number).isEqualTo(expectedNumber);
    // Explicitly check class because Truth's `isEqualTo` is lenient for numeric values
    assertThat(number.getClass()).isEqualTo(expectedNumber.getClass());
  }

  private static class SubTypeOfBagOfPrimitives extends BagOfPrimitives {
    @SuppressWarnings("unused")
    float f = 1.2F;

    public SubTypeOfBagOfPrimitives(long l, int i, boolean b, String string, float f) {
      super(l, i, b, string);
      this.f = f;
    }
  }
}
