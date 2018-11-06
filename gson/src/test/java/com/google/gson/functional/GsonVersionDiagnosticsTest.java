/*
 * Copyright (C) 2018 Gson Authors
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

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

/**
 * Functional tests to validate printing of Gson version on AssertionErrors
 *
 * @author Inderjeet Singh
 */
public class GsonVersionDiagnosticsTest extends TestCase {
  private static final Pattern GSON_VERSION_PATTERN = Pattern.compile("(\\(GSON \\d\\.\\d\\.\\d)(?:[-.][A-Z]+)?\\)$");

  private Gson gson;

  @Before
  public void setUp() {
    gson = new GsonBuilder().registerTypeAdapter(TestType.class, new TypeAdapter<TestType>() {
      @Override public void write(JsonWriter out, TestType value) {
        throw new AssertionError("Expected during serialization");
      }
      @Override public TestType read(JsonReader in) throws IOException {
        throw new AssertionError("Expected during deserialization");
      }
    }).create();
  }

  @Test
  public void testVersionPattern() {
    assertTrue(GSON_VERSION_PATTERN.matcher("(GSON 2.8.5)").matches());
    assertTrue(GSON_VERSION_PATTERN.matcher("(GSON 2.8.5-SNAPSHOT)").matches());
  }

  @Test
  public void testAssertionErrorInSerializationPrintsVersion() {
    try {
      gson.toJson(new TestType());
      fail();
    } catch (AssertionError expected) {
      ensureAssertionErrorPrintsGsonVersion(expected);
    }
  }

  @Test
  public void testAssertionErrorInDeserializationPrintsVersion() {
    try {
      gson.fromJson("{'a':'abc'}", TestType.class);
      fail();
    } catch (AssertionError expected) {
      ensureAssertionErrorPrintsGsonVersion(expected);
    }
  }

  private void ensureAssertionErrorPrintsGsonVersion(AssertionError expected) {
    String msg = expected.getMessage();
    // System.err.println(msg);
    int start = msg.indexOf("(GSON");
    assertTrue(start > 0);
    int end = msg.indexOf("):") + 1;
    assertTrue(end > 0 && end > start + 6);
    String version = msg.substring(start, end);
    // System.err.println(version);
    assertTrue(GSON_VERSION_PATTERN.matcher(version).matches());
  }

  private static final class TestType {
    @SuppressWarnings("unused")
    String a;
  }
}
