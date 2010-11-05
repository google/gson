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

import com.google.gson.Gson;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Functional test for Gson serialization and deserialization of
 * classes with type variables.
 *
 * @author Joel Leitch
 */
public class TypeVariableTest extends TestCase {

  public void disabled_testAdvancedTypeVariables() throws Exception {
    Gson gson = new Gson();
    Bar bar1 = new Bar("someString", 1, true);
    ArrayList<Integer> arrayList = new ArrayList<Integer>();
    arrayList.add(1);
    arrayList.add(2);
    arrayList.add(3);
    bar1.map.put("key1", arrayList);
    bar1.map.put("key2", new ArrayList<Integer>());
    String json = gson.toJson(bar1);

    Bar bar2 = gson.fromJson(json, Bar.class);
    assertEquals(bar1, bar2);
  }

  public void testBasicTypeVariables() throws Exception {
    Gson gson = new Gson();
    Blue blue1 = new Blue(true);
    String json = gson.toJson(blue1);

    Blue blue2 = gson.fromJson(json, Blue.class);
    assertEquals(blue1, blue2);
  }

  public static class Blue extends Red<Boolean> {
    public Blue() {
      super(false);
    }

    public Blue(boolean value) {
      super(value);
    }

    // Technically, we should implement hashcode too
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Blue)) {
        return false;
      } else {
        Blue blue = (Blue) o;
        return redField.equals(blue.redField);

      }
    }
  }

  public static class Red<S> {
    protected final S redField;

    public Red(S redField) {
      this.redField = redField;
    }
  }

  public static class Foo<S, T> extends Red<Boolean> {
    private final S someSField;
    private final T someTField;
    public final Map<S, List<T>> map = new HashMap<S, List<T>>();

    public Foo(S sValue, T tValue, Boolean redField) {
      super(redField);
      this.someSField = sValue;
      this.someTField = tValue;
    }

    // Technically, we should implement hashcode too
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
      if (!(o instanceof Foo<?, ?>)) {
        return false;
      } else {
        Foo<S, T> realFoo = (Foo<S, T>) o;
        return redField.equals(realFoo.redField)
            && someTField.equals(realFoo.someTField)
            && someSField.equals(realFoo.someSField)
            && map.equals(realFoo.map);

      }
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
