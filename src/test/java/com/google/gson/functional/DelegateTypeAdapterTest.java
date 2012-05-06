/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Functional tests for {@link Gson#getDelegateAdapter(TypeAdapterFactory, TypeToken)} method.
 *
 * @author Inderjeet Singh
 */
public class DelegateTypeAdapterTest extends TestCase {

  private StatsTypeAdapterFactory stats;
  private Gson gson;
  protected void setUp() throws Exception {
    super.setUp();
    stats = new StatsTypeAdapterFactory();
    gson = new GsonBuilder()
      .registerTypeAdapterFactory(stats)
      .create();
  }

  public void testDelegateInvokedOnObjectsButNotOnPrimitives() {
    List<BagOfPrimitives> bags = new ArrayList<BagOfPrimitives>();
    for (int i = 0; i < 10; ++i) {
      bags.add(new BagOfPrimitives(i, i, i % 2 == 0, String.valueOf(i)));
    }
    String json = gson.toJson(bags);
    bags = gson.fromJson(json, new TypeToken<List<BagOfPrimitives>>(){}.getType());
    // 11: 1 list object, and 10 entries. stats not invoked on individual fields of
    // BagOfPrimitives since those are primitives.
    assertEquals(11, stats.numReads);
    assertEquals(11, stats.numWrites);
  }

  public void testDelegateNotInvokedOnStrings() {
    String[] bags = {"1", "2", "3", "4"};
    String json = gson.toJson(bags);
    bags = gson.fromJson(json, String[].class);
    // Only 1 array object. stats not invoked on individual strings.
    assertEquals(1, stats.numReads);
    assertEquals(1, stats.numWrites);
  }

  private static class StatsTypeAdapterFactory implements TypeAdapterFactory {
    public int numReads = 0;
    public int numWrites = 0;

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
      return new TypeAdapter<T>() {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
          ++numWrites;
          delegate.write(out, value);
        }

        @Override
        public T read(JsonReader in) throws IOException {
          ++numReads;
          return delegate.read(in);
        }
      };
    }
  }
}
