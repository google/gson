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

package com.economic.persistgson.functional;

import com.economic.persistgson.Gson;
import com.economic.persistgson.GsonBuilder;
import com.economic.persistgson.InstanceCreator;

import com.economic.persistgson.common.TestTypes;
import com.economic.persistgson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Functional Test exercising custom serialization only.  When test applies to both
 * serialization and deserialization then add it to CustomTypeAdapterTest.
 *
 * @author Inderjeet Singh
 */
public class InstanceCreatorTest extends TestCase {

  public void testInstanceCreatorReturnsBaseType() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(TestTypes.Base.class, new InstanceCreator<TestTypes.Base>() {
        @Override public TestTypes.Base createInstance(Type type) {
         return new TestTypes.Base();
       }
      })
      .create();
    String json = "{baseName:'BaseRevised',subName:'Sub'}";
    TestTypes.Base base = gson.fromJson(json, TestTypes.Base.class);
    assertEquals("BaseRevised", base.baseName);
  }

  public void testInstanceCreatorReturnsSubTypeForTopLevelObject() {
    Gson gson = new GsonBuilder()
    .registerTypeAdapter(TestTypes.Base.class, new InstanceCreator<TestTypes.Base>() {
      @Override public TestTypes.Base createInstance(Type type) {
        return new TestTypes.Sub();
      }
    })
    .create();

    String json = "{baseName:'Base',subName:'SubRevised'}";
    TestTypes.Base base = gson.fromJson(json, TestTypes.Base.class);
    assertTrue(base instanceof TestTypes.Sub);

    TestTypes.Sub sub = (TestTypes.Sub) base;
    assertFalse("SubRevised".equals(sub.subName));
    assertEquals(TestTypes.Sub.SUB_NAME, sub.subName);
  }

  public void testInstanceCreatorReturnsSubTypeForField() {
    Gson gson = new GsonBuilder()
    .registerTypeAdapter(TestTypes.Base.class, new InstanceCreator<TestTypes.Base>() {
      @Override public TestTypes.Base createInstance(Type type) {
        return new TestTypes.Sub();
      }
    })
    .create();
    String json = "{base:{baseName:'Base',subName:'SubRevised'}}";
    TestTypes.ClassWithBaseField target = gson.fromJson(json, TestTypes.ClassWithBaseField.class);
    assertTrue(target.base instanceof TestTypes.Sub);
    assertEquals(TestTypes.Sub.SUB_NAME, ((TestTypes.Sub)target.base).subName);
  }

  // This regressed in Gson 2.0 and 2.1
  public void testInstanceCreatorForCollectionType() {
    @SuppressWarnings("serial")
    class SubArrayList<T> extends ArrayList<T> {}
    InstanceCreator<List<String>> listCreator = new InstanceCreator<List<String>>() {
      @Override public List<String> createInstance(Type type) {
        return new SubArrayList<String>();
      }
    };
    Type listOfStringType = new TypeToken<List<String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(listOfStringType, listCreator)
        .create();
    List<String> list = gson.fromJson("[\"a\"]", listOfStringType);
    assertEquals(SubArrayList.class, list.getClass());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void testInstanceCreatorForParametrizedType() throws Exception {
    @SuppressWarnings("serial")
    class SubTreeSet<T> extends TreeSet<T> {}
    InstanceCreator<SortedSet> sortedSetCreator = new InstanceCreator<SortedSet>() {
      @Override public SortedSet createInstance(Type type) {
        return new SubTreeSet();
      }
    };
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(SortedSet.class, sortedSetCreator)
        .create();

    Type sortedSetType = new TypeToken<SortedSet<String>>() {}.getType();
    SortedSet<String> set = gson.fromJson("[\"a\"]", sortedSetType);
    assertEquals(set.first(), "a");
    assertEquals(SubTreeSet.class, set.getClass());

    set = gson.fromJson("[\"b\"]", SortedSet.class);
    assertEquals(set.first(), "b");
    assertEquals(SubTreeSet.class, set.getClass());
  }
}
