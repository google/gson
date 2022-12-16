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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.TestCase;

/**
 * Performs some functional testing to ensure GSON infrastructure properly serializes/deserializes
 * fields that either should or should not be included in the output based on the GSON
 * configuration.
 *
 * @author Joel Leitch
 */
public class FieldExclusionTest extends TestCase {
  private static final String VALUE = "blah_1234";

  private Outer outer;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    outer = new Outer();
  }

  public void testDefaultInnerClassExclusion() throws Exception {
    Gson gson = new Gson();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = gson.toJson(target);
    assertEquals(target.toJson(), result);

    gson = new GsonBuilder().create();
    target = outer.new Inner(VALUE);
    result = gson.toJson(target);
    assertEquals(target.toJson(), result);
  }

  public void testInnerClassExclusion() throws Exception {
    Gson gson = new GsonBuilder().disableInnerClassSerialization().create();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = gson.toJson(target);
    assertEquals("null", result);
  }

  public void testDefaultNestedStaticClassIncluded() throws Exception {
    Gson gson = new Gson();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = gson.toJson(target);
    assertEquals(target.toJson(), result);

    gson = new GsonBuilder().create();
    target = outer.new Inner(VALUE);
    result = gson.toJson(target);
    assertEquals(target.toJson(), result);
  }

  private static class Outer {
    private class Inner extends NestedClass {
      public Inner(String value) {
        super(value);
      }
    }

  }

  private static class NestedClass {
    private final String value;
    public NestedClass(String value) {
      this.value = value;
    }

    public String toJson() {
      return "{\"value\":\"" + value + "\"}";
    }
  }
}
