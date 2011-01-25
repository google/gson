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
package com.google.gson.metrics;

import java.io.IOException;
import java.io.StringReader;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * Caliper based micro benchmarks for Gson
 *
 * @author Inderjeet Singh
 * @author Jesse Wilson
 * @author Joel Leitch
 */
public class GsonBenchmark extends SimpleBenchmark {

  private Gson gson;
  private BagOfPrimitives bag;
  private String json;
  @Param
  private boolean pretty;

  public static void main(String[] args) {
    Runner.main(GsonBenchmark.class, args);
  }
  
  @Override
  protected void setUp() throws Exception {
    this.gson = pretty ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
    this.bag = new BagOfPrimitives(10L, 1, false, "foo");
    this.json = gson.toJson(bag);
  }

  public void timeObjectSerialization(int reps) {
    for (int i=0; i<reps; ++i) {
      gson.toJson(bag);
    }
  }

  public void timeObjectDeserialization(int reps) {
    for (int i=0; i<reps; ++i) {
      gson.fromJson(json, BagOfPrimitives.class);
    }
  }

  public void timeStreamingParserDeserialization(int reps) throws IOException {
    for (int i=0; i<reps; ++i) {
      StringReader reader = new StringReader(json);
      JsonReader jr = new JsonReader(reader);
      jr.beginObject();
      long longValue = 0;
      int intValue = 0;
      boolean booleanValue = false;
      String stringValue = null;
      while(jr.hasNext()) {
        String name = jr.nextName();
        if (name.equals("longValue")) {
          longValue = jr.nextLong();
        } else if (name.equals("intValue")) {
          intValue = jr.nextInt();
        } else if (name.equals("booleanValue")) {
          booleanValue = jr.nextBoolean();
        } else if (name.equals("stringValue")) {
          stringValue = jr.nextString();
        } else {
          throw new IOException("Unexpected name: " + name);
        }
      }
      jr.endObject();
      new BagOfPrimitives(longValue, intValue, booleanValue, stringValue);
    }
  }
}
