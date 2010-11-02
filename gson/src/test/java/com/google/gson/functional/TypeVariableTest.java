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
 * @author Joel Leitch
 */
public class TypeVariableTest extends TestCase {

  public void testSingle() throws Exception {
    Gson gson = new Gson();
    Bar bar1 = new Bar("someString", 1);
    ArrayList<Integer> arrayList = new ArrayList<Integer>();
    arrayList.add(1);
    arrayList.add(2);
    bar1.map.put("key1", arrayList);
    bar1.map.put("key2", new ArrayList<Integer>());
    String json = gson.toJson(bar1);
    System.out.println(json);

    Bar bar2 = gson.fromJson(json, Bar.class);
    assertEquals(bar1, bar2);
  }

  public static class Foo<S, T> {
    private final S someSField;
    private final T someTField;
    public final Map<S, List<T>> map = new HashMap<S, List<T>>();

    public Foo(S sValue, T tValue) {
      this.someSField = sValue;
      this.someTField = tValue;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Foo)) {
        return false;
      } else {
        Foo<S, T> realFoo = (Foo<S, T>) o;
        return someTField.equals(realFoo.someTField)
          && someSField.equals(realFoo.someSField)
          && map.equals(realFoo.map);
      }
    }
  }

  public static class Bar extends Foo<String, Integer> {
    public Bar() {
      this("", 0);
    }

    public Bar(String s, Integer i) {
      super(s, i);
    }
  }
}
