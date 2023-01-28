/*
 * Copyright (C) 2011 Google Inc.
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
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Gson serialization of a sub-class object while encountering a base-class type
 *
 * @author Inderjeet Singh
 */
@SuppressWarnings("unused")
public class MoreSpecificTypeSerializationTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testSubclassFields() {
    ClassWithBaseFields target = new ClassWithBaseFields(new Sub(1, 2));
    String json = gson.toJson(target);
    assertThat(json).contains("\"b\":1");
    assertThat(json).contains("\"s\":2");
  }

  @Test
  public void testListOfSubclassFields() {
    Collection<Base> list = new ArrayList<>();
    list.add(new Base(1));
    list.add(new Sub(2, 3));
    ClassWithContainersOfBaseFields target = new ClassWithContainersOfBaseFields(list, null);
    String json = gson.toJson(target);
    assertWithMessage(json).that(json).contains("{\"b\":1}");
    assertWithMessage(json).that(json).contains("{\"s\":3,\"b\":2}");
  }

  @Test
  public void testMapOfSubclassFields() {
    Map<String, Base> map = new HashMap<>();
    map.put("base", new Base(1));
    map.put("sub", new Sub(2, 3));
    ClassWithContainersOfBaseFields target = new ClassWithContainersOfBaseFields(null, map);
    JsonObject json = gson.toJsonTree(target).getAsJsonObject().get("map").getAsJsonObject();
    assertThat(json.get("base").getAsJsonObject().get("b").getAsInt()).isEqualTo(1);
    JsonObject sub = json.get("sub").getAsJsonObject();
    assertThat(sub.get("b").getAsInt()).isEqualTo(2);
    assertThat(sub.get("s").getAsInt()).isEqualTo(3);
  }

  /**
   * For parameterized type, Gson ignores the more-specific type and sticks to the declared type
   */
  @Test
  public void testParameterizedSubclassFields() {
    ClassWithParameterizedBaseFields target = new ClassWithParameterizedBaseFields(
        new ParameterizedSub<>("one", "two"));
    String json = gson.toJson(target);
    assertThat(json).contains("\"t\":\"one\"");
    assertThat(json).doesNotContain("\"s\"");
  }

  /**
   * For parameterized type in a List, Gson ignores the more-specific type and sticks to
   * the declared type
   */
  @Test
  public void testListOfParameterizedSubclassFields() {
    Collection<ParameterizedBase<String>> list = new ArrayList<>();
    list.add(new ParameterizedBase<>("one"));
    list.add(new ParameterizedSub<>("two", "three"));
    ClassWithContainersOfParameterizedBaseFields target =
      new ClassWithContainersOfParameterizedBaseFields(list, null);
    String json = gson.toJson(target);
    assertWithMessage(json).that(json).contains("{\"t\":\"one\"}");
    assertWithMessage(json).that(json).doesNotContain("\"s\":");
  }

  /**
   * For parameterized type in a map, Gson ignores the more-specific type and sticks to the
   * declared type
   */
  @Test
  public void testMapOfParameterizedSubclassFields() {
    Map<String, ParameterizedBase<String>> map = new HashMap<>();
    map.put("base", new ParameterizedBase<>("one"));
    map.put("sub", new ParameterizedSub<>("two", "three"));
    ClassWithContainersOfParameterizedBaseFields target =
      new ClassWithContainersOfParameterizedBaseFields(null, map);
    JsonObject json = gson.toJsonTree(target).getAsJsonObject().get("map").getAsJsonObject();
    assertThat(json.get("base").getAsJsonObject().get("t").getAsString()).isEqualTo("one");
    JsonObject sub = json.get("sub").getAsJsonObject();
    assertThat(sub.get("t").getAsString()).isEqualTo("two");
    assertThat(sub.get("s")).isNull();
  }

  private static class Base {
    int b;
    Base(int b) {
      this.b = b;
    }
  }

  private static class Sub extends Base {
    int s;
    Sub(int b, int s) {
      super(b);
      this.s = s;
    }
  }

  private static class ClassWithBaseFields {
    Base b;
    ClassWithBaseFields(Base b) {
      this.b = b;
    }
  }

  private static class ClassWithContainersOfBaseFields {
    Collection<Base> collection;
    Map<String, Base> map;
    ClassWithContainersOfBaseFields(Collection<Base> collection, Map<String, Base> map) {
      this.collection = collection;
      this.map = map;
    }
  }

  private static class ParameterizedBase<T> {
    T t;
    ParameterizedBase(T t) {
      this.t = t;
    }
  }

  private static class ParameterizedSub<T> extends ParameterizedBase<T> {
    T s;
    ParameterizedSub(T t, T s) {
      super(t);
      this.s = s;
    }
  }

  private static class ClassWithParameterizedBaseFields {
    ParameterizedBase<String> b;
    ClassWithParameterizedBaseFields(ParameterizedBase<String> b) {
      this.b = b;
    }
  }

  private static class ClassWithContainersOfParameterizedBaseFields {
    Collection<ParameterizedBase<String>> collection;
    Map<String, ParameterizedBase<String>> map;
    ClassWithContainersOfParameterizedBaseFields(Collection<ParameterizedBase<String>> collection,
        Map<String, ParameterizedBase<String>> map) {
      this.collection = collection;
      this.map = map;
    }
  }
}
