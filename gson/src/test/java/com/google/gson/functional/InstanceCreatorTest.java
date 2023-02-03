/*
 * Copyright (C) 2009 Google Inc.
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
import com.google.gson.InstanceCreator;
import com.google.gson.common.TestTypes.Base;
import com.google.gson.common.TestTypes.ClassWithBaseField;
import com.google.gson.common.TestTypes.Sub;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Test;

/**
 * Functional Test exercising custom serialization only. When test applies to both
 * serialization and deserialization then add it to CustomTypeAdapterTest.
 *
 * @author Inderjeet Singh
 */
public class InstanceCreatorTest {

  @Test
  public void testInstanceCreatorReturnsBaseType() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new InstanceCreator<Base>() {
        @Override public Base createInstance(Type type) {
         return new Base();
       }
      })
      .create();
    String json = "{baseName:'BaseRevised',subName:'Sub'}";
    Base base = gson.fromJson(json, Base.class);
    assertThat(base.baseName).isEqualTo("BaseRevised");
  }

  @Test
  public void testInstanceCreatorReturnsSubTypeForTopLevelObject() {
    Gson gson = new GsonBuilder()
    .registerTypeAdapter(Base.class, new InstanceCreator<Base>() {
      @Override public Base createInstance(Type type) {
        return new Sub();
      }
    })
    .create();

    String json = "{baseName:'Base',subName:'SubRevised'}";
    Base base = gson.fromJson(json, Base.class);
    assertThat(base instanceof Sub).isTrue();

    Sub sub = (Sub) base;
    assertThat("SubRevised".equals(sub.subName)).isFalse();
    assertThat(sub.subName).isEqualTo(Sub.SUB_NAME);
  }

  @Test
  public void testInstanceCreatorReturnsSubTypeForField() {
    Gson gson = new GsonBuilder()
    .registerTypeAdapter(Base.class, new InstanceCreator<Base>() {
      @Override public Base createInstance(Type type) {
        return new Sub();
      }
    })
    .create();
    String json = "{base:{baseName:'Base',subName:'SubRevised'}}";
    ClassWithBaseField target = gson.fromJson(json, ClassWithBaseField.class);
    assertThat(target.base instanceof Sub).isTrue();
    assertThat(((Sub)target.base).subName).isEqualTo(Sub.SUB_NAME);
  }

  // This regressed in Gson 2.0 and 2.1
  @Test
  public void testInstanceCreatorForCollectionType() {
    @SuppressWarnings("serial")
    class SubArrayList<T> extends ArrayList<T> {}
    InstanceCreator<List<String>> listCreator = new InstanceCreator<List<String>>() {
      @Override public List<String> createInstance(Type type) {
        return new SubArrayList<>();
      }
    };
    Type listOfStringType = new TypeToken<List<String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(listOfStringType, listCreator)
        .create();
    List<String> list = gson.fromJson("[\"a\"]", listOfStringType);
    assertThat(list.getClass()).isEqualTo(SubArrayList.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInstanceCreatorForParametrizedType() {
    @SuppressWarnings("serial")
    class SubTreeSet<T> extends TreeSet<T> {}
    InstanceCreator<SortedSet<?>> sortedSetCreator = new InstanceCreator<SortedSet<?>>() {
      @Override public SortedSet<?> createInstance(Type type) {
        return new SubTreeSet<>();
      }
    };
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(SortedSet.class, sortedSetCreator)
        .create();

    Type sortedSetType = new TypeToken<SortedSet<String>>() {}.getType();
    SortedSet<String> set = gson.fromJson("[\"a\"]", sortedSetType);
    assertThat(set.first()).isEqualTo("a");
    assertThat(set.getClass()).isEqualTo(SubTreeSet.class);

    set = gson.fromJson("[\"b\"]", SortedSet.class);
    assertThat(set.first()).isEqualTo("b");
    assertThat(set.getClass()).isEqualTo(SubTreeSet.class);
  }
}
