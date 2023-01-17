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

package com.google.gson;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/**
 * Unit test for the {@link LongSerializationPolicy} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class LongSerializationPolicyTest {

  @Test
  public void testDefaultLongSerialization() throws Exception {
    JsonElement element = LongSerializationPolicy.DEFAULT.serialize(1556L);
    assertThat(element.isJsonPrimitive()).isTrue();

    JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
    assertThat(jsonPrimitive.isString()).isFalse();
    assertThat(jsonPrimitive.isNumber()).isTrue();
    assertThat(element.getAsLong()).isEqualTo(1556L);
  }

  @Test
  public void testDefaultLongSerializationIntegration() {
    Gson gson = new GsonBuilder()
      .setLongSerializationPolicy(LongSerializationPolicy.DEFAULT)
      .create();
    assertThat(gson.toJson(new long[] { 1L }, long[].class)).isEqualTo("[1]");
    assertThat(gson.toJson(new Long[] { 1L }, Long[].class)).isEqualTo("[1]");
  }

  @Test
  public void testDefaultLongSerializationNull() {
    LongSerializationPolicy policy = LongSerializationPolicy.DEFAULT;
    assertThat(policy.serialize(null).isJsonNull()).isTrue();

    Gson gson = new GsonBuilder()
      .setLongSerializationPolicy(policy)
      .create();
    assertThat(gson.toJson(null, Long.class)).isEqualTo("null");
  }

  @Test
  public void testStringLongSerialization() throws Exception {
    JsonElement element = LongSerializationPolicy.STRING.serialize(1556L);
    assertThat(element.isJsonPrimitive()).isTrue();

    JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
    assertThat(jsonPrimitive.isNumber()).isFalse();
    assertThat(jsonPrimitive.isString()).isTrue();
    assertThat(element.getAsString()).isEqualTo("1556");
  }

  @Test
  public void testStringLongSerializationIntegration() {
    Gson gson = new GsonBuilder()
      .setLongSerializationPolicy(LongSerializationPolicy.STRING)
      .create();
    assertThat(gson.toJson(new long[] { 1L }, long[].class)).isEqualTo("[\"1\"]");
    assertThat(gson.toJson(new Long[] { 1L }, long[].class)).isEqualTo("[\"1\"]");
  }

  @Test
  public void testStringLongSerializationNull() {
    LongSerializationPolicy policy = LongSerializationPolicy.STRING;
    assertThat(policy.serialize(null).isJsonNull()).isTrue();

    Gson gson = new GsonBuilder()
      .setLongSerializationPolicy(policy)
      .create();
    assertThat(gson.toJson(null, Long.class)).isEqualTo("null");
  }
}
