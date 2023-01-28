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
package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import org.junit.Test;

/**
 * Functional tests for versioning support in Gson.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class VersioningTest {
  private static final int A = 0;
  private static final int B = 1;
  private static final int C = 2;
  private static final int D = 3;

  private static Gson gsonWithVersion(double version) {
    return new GsonBuilder().setVersion(version).create();
  }

  @Test
  public void testVersionedUntilSerialization() {
    Version1 target = new Version1();
    Gson gson = gsonWithVersion(1.29);
    String json = gson.toJson(target);
    assertThat(json).contains("\"a\":" + A);

    gson = gsonWithVersion(1.3);
    json = gson.toJson(target);
    assertThat(json).doesNotContain("\"a\":" + A);

    gson = gsonWithVersion(1.31);
    json = gson.toJson(target);
    assertThat(json).doesNotContain("\"a\":" + A);
  }

  @Test
  public void testVersionedUntilDeserialization() {
    String json = "{\"a\":3,\"b\":4,\"c\":5}";

    Gson gson = gsonWithVersion(1.29);
    Version1 version1 = gson.fromJson(json, Version1.class);
    assertThat(version1.a).isEqualTo(3);

    gson = gsonWithVersion(1.3);
    version1 = gson.fromJson(json, Version1.class);
    assertThat(version1.a).isEqualTo(A);

    gson = gsonWithVersion(1.31);
    version1 = gson.fromJson(json, Version1.class);
    assertThat(version1.a).isEqualTo(A);
  }

  @Test
  public void testVersionedClassesSerialization() {
    Gson gson = gsonWithVersion(1.0);
    String json1 = gson.toJson(new Version1());
    String json2 = gson.toJson(new Version1_1());
    assertThat(json2).isEqualTo(json1);
  }

  @Test
  public void testVersionedClassesDeserialization() {
    Gson gson = gsonWithVersion(1.0);
    String json = "{\"a\":3,\"b\":4,\"c\":5}";
    Version1 version1 = gson.fromJson(json, Version1.class);
    assertThat(version1.a).isEqualTo(3);
    assertThat(version1.b).isEqualTo(4);
    Version1_1 version1_1 = gson.fromJson(json, Version1_1.class);
    assertThat(version1_1.a).isEqualTo(3);
    assertThat(version1_1.b).isEqualTo(4);
    assertThat(version1_1.c).isEqualTo(C);
  }

  @Test
  public void testIgnoreLaterVersionClassSerialization() {
    Gson gson = gsonWithVersion(1.0);
    assertThat(gson.toJson(new Version1_2())).isEqualTo("null");
  }

  @Test
  public void testIgnoreLaterVersionClassDeserialization() {
    Gson gson = gsonWithVersion(1.0);
    String json = "{\"a\":3,\"b\":4,\"c\":5,\"d\":6}";
    Version1_2 version1_2 = gson.fromJson(json, Version1_2.class);
    // Since the class is versioned to be after 1.0, we expect null
    // This is the new behavior in Gson 2.0
    assertThat(version1_2).isNull();
  }

  @Test
  public void testVersionedGsonWithUnversionedClassesSerialization() {
    Gson gson = gsonWithVersion(1.0);
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testVersionedGsonWithUnversionedClassesDeserialization() {
    Gson gson = gsonWithVersion(1.0);
    String json = "{\"longValue\":10,\"intValue\":20,\"booleanValue\":false}";

    BagOfPrimitives expected = new BagOfPrimitives();
    expected.longValue = 10;
    expected.intValue = 20;
    expected.booleanValue = false;
    BagOfPrimitives actual = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testVersionedGsonMixingSinceAndUntilSerialization() {
    Gson gson = gsonWithVersion(1.0);
    SinceUntilMixing target = new SinceUntilMixing();
    String json = gson.toJson(target);
    assertThat(json).doesNotContain("\"b\":" + B);

    gson = gsonWithVersion(1.2);
    json = gson.toJson(target);
    assertThat(json).contains("\"b\":" + B);

    gson = gsonWithVersion(1.3);
    json = gson.toJson(target);
    assertThat(json).doesNotContain("\"b\":" + B);

    gson = gsonWithVersion(1.4);
    json = gson.toJson(target);
    assertThat(json).doesNotContain("\"b\":" + B);
  }

  @Test
  public void testVersionedGsonMixingSinceAndUntilDeserialization() {
    String json = "{\"a\":5,\"b\":6}";
    Gson gson = gsonWithVersion(1.0);
    SinceUntilMixing result = gson.fromJson(json, SinceUntilMixing.class);
    assertThat(result.a).isEqualTo(5);
    assertThat(result.b).isEqualTo(B);

    gson = gsonWithVersion(1.2);
    result = gson.fromJson(json, SinceUntilMixing.class);
    assertThat(result.a).isEqualTo(5);
    assertThat(result.b).isEqualTo(6);

    gson = gsonWithVersion(1.3);
    result = gson.fromJson(json, SinceUntilMixing.class);
    assertThat(result.a).isEqualTo(5);
    assertThat(result.b).isEqualTo(B);

    gson = gsonWithVersion(1.4);
    result = gson.fromJson(json, SinceUntilMixing.class);
    assertThat(result.a).isEqualTo(5);
    assertThat(result.b).isEqualTo(B);
  }

  private static class Version1 {
    @Until(1.3) int a = A;
    @Since(1.0) int b = B;
  }

  private static class Version1_1 extends Version1 {
    @Since(1.1) int c = C;
  }

  @Since(1.2)
  private static class Version1_2 extends Version1_1 {
    @SuppressWarnings("unused")
    int d = D;
  }

  private static class SinceUntilMixing {
    int a = A;

    @Since(1.1)
    @Until(1.3)
    int b = B;
  }
}
