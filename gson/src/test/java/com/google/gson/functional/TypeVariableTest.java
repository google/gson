/*
 * Copyright (C) 2010 Google Inc.
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
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Functional test for Gson serialization and deserialization of
 * classes with type variables.
 *
 * @author Joel Leitch
 */
public class TypeVariableTest {

  @Test
  public void testAdvancedTypeVariables() {
    Gson gson = new Gson();
    Bar bar1 = new Bar("someString", 1, true);
    ArrayList<Integer> arrayList = new ArrayList<>();
    arrayList.add(1);
    arrayList.add(2);
    arrayList.add(3);
    bar1.map.put("key1", arrayList);
    bar1.map.put("key2", new ArrayList<Integer>());
    String json = gson.toJson(bar1);

    Bar bar2 = gson.fromJson(json, Bar.class);
    assertThat(bar2).isEqualTo(bar1);
  }

  @Test
  public void testTypeVariablesViaTypeParameter() {
    Gson gson = new Gson();
    Foo<String, Integer> original = new Foo<>("e", 5, false);
    original.map.put("f", Arrays.asList(6, 7));
    Type type = new TypeToken<Foo<String, Integer>>() {}.getType();
    String json = gson.toJson(original, type);
    assertThat(json)
        .isEqualTo("{\"someSField\":\"e\",\"someTField\":5,\"map\":{\"f\":[6,7]},\"redField\":false}");
    assertThat(gson.<Foo<String, Integer>>fromJson(json, type)).isEqualTo(original);
  }

  @Test
  public void testBasicTypeVariables() {
    Gson gson = new Gson();
    Blue blue1 = new Blue(true);
    String json = gson.toJson(blue1);

    Blue blue2 = gson.fromJson(json, Blue.class);
    assertThat(blue2).isEqualTo(blue1);
  }

  // for missing hashCode() override
  @SuppressWarnings({"overrides", "EqualsHashCode"})
  public static class Blue extends Red<Boolean> {
    public Blue() {
      super(false);
    }

    public Blue(boolean value) {
      super(value);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Blue)) {
        return false;
      }
      Blue blue = (Blue) o;
      return redField.equals(blue.redField);
    }
  }

  public static class Red<S> {
    protected S redField;

    public Red() {}

    public Red(S redField) {
      this.redField = redField;
    }
  }

  @SuppressWarnings({"overrides", "EqualsHashCode"}) // for missing hashCode() override
  public static class Foo<S, T> extends Red<Boolean> {
    private S someSField;
    private T someTField;
    public final Map<S, List<T>> map = new HashMap<>();

    public Foo() {}

    public Foo(S sValue, T tValue, Boolean redField) {
      super(redField);
      this.someSField = sValue;
      this.someTField = tValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
      if (!(o instanceof Foo<?, ?>)) {
        return false;
      }
      Foo<S, T> realFoo = (Foo<S, T>) o;
      return redField.equals(realFoo.redField)
        && someTField.equals(realFoo.someTField)
        && someSField.equals(realFoo.someSField)
        && map.equals(realFoo.map);
    }
  }

  public static class Bar extends Foo<String, Integer> {
    public Bar() {
      this("", 0, false);
    }

    public Bar(String s, Integer i, boolean b) {
      super(s, i, b);
    }
  }
}
