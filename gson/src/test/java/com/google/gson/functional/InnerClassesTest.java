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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

public class InnerClassesTest {
  private static final String VALUE = "blah_1234";

  private Outer outer = new Outer();

  @Test
  public void testDefaultInnerClassExclusionSerialization() {
    Gson gson = new Gson();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = gson.toJson(target);
    assertEquals(target.toJson(), result);

    assertEquals("{\"inner\":" + target.toJson() + "}", gson.toJson(new WithInnerClassField(target)));

    gson = new GsonBuilder().create();
    target = outer.new Inner(VALUE);
    result = gson.toJson(target);
    assertEquals(target.toJson(), result);
  }

  @Test
  public void testDefaultInnerClassExclusionDeserialization() {
    Gson gson = new Gson();
    Outer.Inner deserialized = gson.fromJson("{\"value\":\"a\"}", Outer.Inner.class);
    assertNotNull(deserialized);
    assertEquals("a", deserialized.value);

    WithInnerClassField deserializedWithField = gson.fromJson("{\"inner\":{\"value\":\"a\"}}", WithInnerClassField.class);
    deserialized = deserializedWithField.inner;
    assertNotNull(deserialized);
    assertEquals("a", deserialized.value);

    gson = new GsonBuilder().create();
    deserialized = gson.fromJson("{\"value\":\"a\"}", Outer.Inner.class);
    assertNotNull(deserialized);
    assertEquals("a", deserialized.value);
  }

  @Test
  public void testInnerClassExclusionSerialization() {
    Gson gson = new GsonBuilder().disableInnerClassSerialization().create();
    Outer.Inner target = outer.new Inner(VALUE);
    String result = gson.toJson(target);
    assertEquals("null", result);

    assertEquals("{}", gson.toJson(new WithInnerClassField(target)));
  }

  @Test
  public void testInnerClassExclusionDeserialization() {
    Gson gson = new GsonBuilder().disableInnerClassSerialization().create();
    Outer.Inner deserialized = gson.fromJson("{\"value\":\"a\"}", Outer.Inner.class);
    assertNull(deserialized);

    WithInnerClassField deserializedWithField = gson.fromJson("{\"inner\":{\"value\":\"a\"}}", WithInnerClassField.class);
    deserialized = deserializedWithField.inner;
    assertNull(deserialized);
  }

  private static class Outer {
    private class Inner extends NestedClass {
      public Inner(String value) {
        super(value);
      }
    }
  }

  private static class NestedClass {
    final String value;
    public NestedClass(String value) {
      this.value = value;
    }

    public String toJson() {
      return "{\"value\":\"" + value + "\"}";
    }
  }

  private static class WithInnerClassField {
    Outer.Inner inner;

    WithInnerClassField(Outer.Inner inner) {
      this.inner = inner;
    }
  }
}
