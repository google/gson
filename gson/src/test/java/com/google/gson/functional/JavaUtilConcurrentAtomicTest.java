/*
 * Copyright (C) 2015 Google Inc.
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
import com.google.gson.LongSerializationPolicy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional test for Json serialization and deserialization for classes in java.util.concurrent.atomic
 */
public class JavaUtilConcurrentAtomicTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testAtomicBoolean() {
    AtomicBoolean target = gson.fromJson("true", AtomicBoolean.class);
    assertThat(target.get()).isTrue();
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("true");
  }

  @Test
  public void testAtomicInteger() {
    AtomicInteger target = gson.fromJson("10", AtomicInteger.class);
    assertThat(target.get()).isEqualTo(10);
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("10");
  }

  @Test
  public void testAtomicLong() {
    AtomicLong target = gson.fromJson("10", AtomicLong.class);
    assertThat(target.get()).isEqualTo(10);
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("10");
  }

  @Test
  public void testAtomicLongWithStringSerializationPolicy() {
    Gson gson = new GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create();
    AtomicLongHolder target = gson.fromJson("{'value':'10'}", AtomicLongHolder.class);
    assertThat(target.value.get()).isEqualTo(10);
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("{\"value\":\"10\"}");
  }

  @Test
  public void testAtomicIntegerArray() {
    AtomicIntegerArray target = gson.fromJson("[10, 13, 14]", AtomicIntegerArray.class);
    assertThat(target.length()).isEqualTo(3);
    assertThat(target.get(0)).isEqualTo(10);
    assertThat(target.get(1)).isEqualTo(13);
    assertThat(target.get(2)).isEqualTo(14);
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("[10,13,14]");
  }

  @Test
  public void testAtomicLongArray() {
    AtomicLongArray target = gson.fromJson("[10, 13, 14]", AtomicLongArray.class);
    assertThat(target.length()).isEqualTo(3);
    assertThat(target.get(0)).isEqualTo(10);
    assertThat(target.get(1)).isEqualTo(13);
    assertThat(target.get(2)).isEqualTo(14);
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("[10,13,14]");
  }

  @Test
  public void testAtomicLongArrayWithStringSerializationPolicy() {
    Gson gson = new GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create();
    AtomicLongArray target = gson.fromJson("['10', '13', '14']", AtomicLongArray.class);
    assertThat(target.length()).isEqualTo(3);
    assertThat(target.get(0)).isEqualTo(10);
    assertThat(target.get(1)).isEqualTo(13);
    assertThat(target.get(2)).isEqualTo(14);
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("[\"10\",\"13\",\"14\"]");
  }

  private static class AtomicLongHolder {
    AtomicLong value;
  }
}
