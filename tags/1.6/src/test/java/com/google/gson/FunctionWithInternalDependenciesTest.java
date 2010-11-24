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

package com.google.gson;

import java.lang.reflect.Modifier;
import java.util.LinkedList;

import junit.framework.TestCase;

import com.google.gson.common.TestTypes;
import com.google.gson.common.TestTypes.ClassWithNoFields;

/**
 * Functional tests for Gson that depend on some internal package-protected elements of
 * com.google.gson package and hence must be placed in the same package. We should make every
 * attempt to migrate tests out of this class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class FunctionWithInternalDependenciesTest extends TestCase {

  public void testAnonymousLocalClassesSerialization() throws Exception {
    LinkedList<ExclusionStrategy> strategies = new LinkedList<ExclusionStrategy>();
    strategies.add(new SyntheticFieldExclusionStrategy(true));
    strategies.add(new ModifierBasedExclusionStrategy(Modifier.TRANSIENT, Modifier.STATIC));
    ExclusionStrategy exclusionStrategy = new DisjunctionExclusionStrategy(strategies);
    Gson gson = new Gson(exclusionStrategy, exclusionStrategy, Gson.DEFAULT_NAMING_POLICY,
        new MappedObjectConstructor(DefaultTypeAdapters.getDefaultInstanceCreators()),
        false, DefaultTypeAdapters.getDefaultSerializers(),
        DefaultTypeAdapters.getDefaultDeserializers(), Gson.DEFAULT_JSON_NON_EXECUTABLE, true,
        false);
    assertEquals("{}", gson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  // TODO(Joel): Move this to some other functional test once exclusion policies are
  // available to the public
  public void testUserDefinedExclusionPolicies() throws Exception {
    Gson gson = new GsonBuilder()
        .setExclusionStrategies(new UserDefinedExclusionStrategy(String.class))
        .create();

    String json = gson.toJson(new TestTypes.StringWrapper("someValue"));
    assertEquals("{}", json);
  }

  private static class UserDefinedExclusionStrategy implements ExclusionStrategy {
    private final Class<?> excludedThisClass;

    UserDefinedExclusionStrategy(Class<?> excludedThisClass) {
      this.excludedThisClass = excludedThisClass;
    }

    public boolean shouldSkipClass(Class<?> clazz) {
      return excludedThisClass.equals(clazz);
    }

    public boolean shouldSkipField(FieldAttributes f) {
      return excludedThisClass.equals(f.getDeclaredClass());
    }

  }
}
