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
package com.economic.persistgson.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.economic.persistgson.Gson;
import com.economic.persistgson.GsonBuilder;
import com.economic.persistgson.TypeAdapter;
import com.economic.persistgson.TypeAdapterFactory;
import com.economic.persistgson.common.TestTypes;
import com.economic.persistgson.reflect.TypeToken;
import com.economic.persistgson.stream.JsonReader;
import com.economic.persistgson.stream.JsonWriter;

/**
 * Functional tests for {@link Gson#getDelegateAdapter(TypeAdapterFactory, TypeToken)} method.
 *
 * @author Inderjeet Singh
 */
public class DelegateTypeAdapterTest extends TestCase {

  private StatsTypeAdapterFactory stats;
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    stats = new StatsTypeAdapterFactory();
    gson = new GsonBuilder()
      .registerTypeAdapterFactory(stats)
      .create();
  }

  public void testDelegateInvoked() {
    List<TestTypes.BagOfPrimitives> bags = new ArrayList<TestTypes.BagOfPrimitives>();
    for (int i = 0; i < 10; ++i) {
      bags.add(new TestTypes.BagOfPrimitives(i, i, i % 2 == 0, String.valueOf(i)));
    }
    String json = gson.toJson(bags);
    bags = gson.fromJson(json, new TypeToken<List<TestTypes.BagOfPrimitives>>(){}.getType());
    // 11: 1 list object, and 10 entries. stats invoked on all 5 fields
    assertEquals(51, stats.numReads);
    assertEquals(51, stats.numWrites);
  }

  public void testDelegateInvokedOnStrings() {
    String[] bags = {"1", "2", "3", "4"};
    String json = gson.toJson(bags);
    bags = gson.fromJson(json, String[].class);
    // 1 array object with 4 elements.
    assertEquals(5, stats.numReads);
    assertEquals(5, stats.numWrites);
  }

  private static class StatsTypeAdapterFactory implements TypeAdapterFactory {
    public int numReads = 0;
    public int numWrites = 0;

    @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
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
