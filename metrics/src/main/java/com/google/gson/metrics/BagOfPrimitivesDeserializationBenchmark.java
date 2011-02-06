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
import java.lang.reflect.Field;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * Caliper based micro benchmarks for Gson
 *
 * @author Inderjeet Singh
 * @author Jesse Wilson
 * @author Joel Leitch
 */
public class BagOfPrimitivesDeserializationBenchmark extends SimpleBenchmark {

  private Gson gson;
  private String json;

  public static void main(String[] args) {
    Runner.main(BagOfPrimitivesDeserializationBenchmark.class, args);
  }
  
  @Override
  protected void setUp() throws Exception {
    this.gson = new Gson();
    BagOfPrimitives bag = new BagOfPrimitives(10L, 1, false, "foo");
    this.json = gson.toJson(bag);
  }

  /** 
   * Benchmark to measure Gson performance for deserializing an object
   */
  public void timeBagOfPrimitivesDefault(int reps) {
    for (int i=0; i<reps; ++i) {
      gson.fromJson(json, BagOfPrimitives.class);
    }
  }

  /**
   * Benchmark to measure deserializing objects by hand
   */
  public void timeBagOfPrimitivesStreaming(int reps) throws IOException {
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

  /**
   * This benchmark measures the ideal Gson performance: the cost of parsing a JSON stream and
   * setting object values by reflection. We should strive to reduce the discrepancy between this
   * and {@link #timeBagOfPrimitivesDefault(int)} .
   */
  public void timeBagOfPrimitivesReflectionStreaming(int reps) throws Exception {
    for (int i=0; i<reps; ++i) {
      StringReader reader = new StringReader(json);
      JsonReader jr = new JsonReader(reader);
      jr.beginObject();
      BagOfPrimitives bag = new BagOfPrimitives();
      while(jr.hasNext()) {
        String name = jr.nextName();
        for (Field field : BagOfPrimitives.class.getDeclaredFields()) {
          if (field.getName().equals(name)) {
            Class<?> fieldType = field.getType();
            if (fieldType.equals(long.class)) {
              field.setLong(bag, jr.nextLong());
            } else if (fieldType.equals(int.class)) {
              field.setInt(bag, jr.nextInt());
            } else if (fieldType.equals(boolean.class)) {
              field.setBoolean(bag, jr.nextBoolean());
            } else if (fieldType.equals(String.class)) {
              field.set(bag, jr.nextString());
            } else {
              throw new RuntimeException("Unexpected: type: " + fieldType + ", name: " + name);
            }
          }
        }
      }
      jr.endObject();
    }
  }
}
