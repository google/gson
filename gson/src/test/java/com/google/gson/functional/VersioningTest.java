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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    assertTrue(json.contains("\"a\":" + A));

    gson = gsonWithVersion(1.3);
    json = gson.toJson(target);
    assertFalse(json.contains("\"a\":" + A));

    gson = gsonWithVersion(1.31);
    json = gson.toJson(target);
    assertFalse(json.contains("\"a\":" + A));
  }

  @Test
  public void testVersionedUntilDeserialization() {
    String json = "{\"a\":3,\"b\":4,\"c\":5}";

    Gson gson = gsonWithVersion(1.29);
    Version1 version1 = gson.fromJson(json, Version1.class);
    assertEquals(3, version1.a);

    gson = gsonWithVersion(1.3);
    version1 = gson.fromJson(json, Version1.class);
    assertEquals(A, version1.a);

    gson = gsonWithVersion(1.31);
    version1 = gson.fromJson(json, Version1.class);
    assertEquals(A, version1.a);
  }

  @Test
  public void testVersionedClassesSerialization() {
    Gson gson = gsonWithVersion(1.0);
    String json1 = gson.toJson(new Version1());
    String json2 = gson.toJson(new Version1_1());
    assertEquals(json1, json2);
  }

  @Test
  public void testVersionedClassesDeserialization() {
    Gson gson = gsonWithVersion(1.0);
    String json = "{\"a\":3,\"b\":4,\"c\":5}";
    Version1 version1 = gson.fromJson(json, Version1.class);
    assertEquals(3, version1.a);
    assertEquals(4, version1.b);
    Version1_1 version1_1 = gson.fromJson(json, Version1_1.class);
    assertEquals(3, version1_1.a);
    assertEquals(4, version1_1.b);
    assertEquals(C, version1_1.c);
  }

  @Test
  public void testIgnoreLaterVersionClassSerialization() {
    Gson gson = gsonWithVersion(1.0);
    assertEquals("null", gson.toJson(new Version1_2()));
  }

  @Test
  public void testIgnoreLaterVersionClassDeserialization() {
    Gson gson = gsonWithVersion(1.0);
    String json = "{\"a\":3,\"b\":4,\"c\":5,\"d\":6}";
    Version1_2 version1_2 = gson.fromJson(json, Version1_2.class);
    // Since the class is versioned to be after 1.0, we expect null
    // This is the new behavior in Gson 2.0
    assertNull(version1_2);
  }

  @Test
  public void testVersionedGsonWithUnversionedClassesSerialization() {
    Gson gson = gsonWithVersion(1.0);
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedJson(), gson.toJson(target));
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
    assertEquals(expected, actual);
  }

  @Test
  public void testVersionedGsonMixingSinceAndUntilSerialization() {
    Gson gson = gsonWithVersion(1.0);
    SinceUntilMixing target = new SinceUntilMixing();
    String json = gson.toJson(target);
    assertFalse(json.contains("\"b\":" + B));

    gson = gsonWithVersion(1.2);
    json = gson.toJson(target);
    assertTrue(json.contains("\"b\":" + B));

    gson = gsonWithVersion(1.3);
    json = gson.toJson(target);
    assertFalse(json.contains("\"b\":" + B));

    gson = gsonWithVersion(1.4);
    json = gson.toJson(target);
    assertFalse(json.contains("\"b\":" + B));
  }

  @Test
  public void testVersionedGsonMixingSinceAndUntilDeserialization() {
    String json = "{\"a\":5,\"b\":6}";
    Gson gson = gsonWithVersion(1.0);
    SinceUntilMixing result = gson.fromJson(json, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(B, result.b);

    gson = gsonWithVersion(1.2);
    result = gson.fromJson(json, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(6, result.b);

    gson = gsonWithVersion(1.3);
    result = gson.fromJson(json, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(B, result.b);

    gson = gsonWithVersion(1.4);
    result = gson.fromJson(json, SinceUntilMixing.class);
    assertEquals(5, result.a);
    assertEquals(B, result.b);
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
