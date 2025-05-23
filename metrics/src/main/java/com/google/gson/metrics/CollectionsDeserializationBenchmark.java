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

import com.google.caliper.BeforeExperiment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Caliper based micro benchmarks for Gson
 *
 * @author Inderjeet Singh
 */
public class CollectionsDeserializationBenchmark {

  private static final TypeToken<List<BagOfPrimitives>> LIST_TYPE_TOKEN =
      new TypeToken<List<BagOfPrimitives>>() {};
  private static final Type LIST_TYPE = LIST_TYPE_TOKEN.getType();
  private Gson gson;
  private String json;

  public static void main(String[] args) {
    NonUploadingCaliperRunner.run(CollectionsDeserializationBenchmark.class, args);
  }

  @BeforeExperiment
  void setUp() throws Exception {
    this.gson = new Gson();
    List<BagOfPrimitives> bags = new ArrayList<>();
    for (int i = 0; i < 100; ++i) {
      bags.add(new BagOfPrimitives(10L, 1, false, "foo"));
    }
    this.json = gson.toJson(bags, LIST_TYPE);
  }

  /** Benchmark to measure Gson performance for deserializing an object */
  public void timeCollectionsDefault(int reps) {
    for (int i = 0; i < reps; ++i) {
      List<BagOfPrimitives> unused = gson.fromJson(json, LIST_TYPE_TOKEN);
    }
  }

  /** Benchmark to measure deserializing objects by hand */
  @SuppressWarnings("ModifiedButNotUsed")
  public void timeCollectionsStreaming(int reps) throws IOException {
    for (int i = 0; i < reps; ++i) {
      StringReader reader = new StringReader(json);
      JsonReader jr = new JsonReader(reader);
      jr.beginArray();
      List<BagOfPrimitives> bags = new ArrayList<>();
      while (jr.hasNext()) {
        jr.beginObject();
        long longValue = 0;
        int intValue = 0;
        boolean booleanValue = false;
        String stringValue = null;
        while (jr.hasNext()) {
          String name = jr.nextName();
          switch (name) {
            case "longValue":
              longValue = jr.nextLong();
              break;
            case "intValue":
              intValue = jr.nextInt();
              break;
            case "booleanValue":
              booleanValue = jr.nextBoolean();
              break;
            case "stringValue":
              stringValue = jr.nextString();
              break;
            default:
              throw new IOException("Unexpected name: " + name);
          }
        }
        jr.endObject();
        bags.add(new BagOfPrimitives(longValue, intValue, booleanValue, stringValue));
      }
      jr.endArray();
    }
  }

  /**
   * This benchmark measures the ideal Gson performance: the cost of parsing a JSON stream and
   * setting object values by reflection. We should strive to reduce the discrepancy between this
   * and {@link #timeCollectionsDefault(int)} .
   */
  @SuppressWarnings("ModifiedButNotUsed")
  public void timeCollectionsReflectionStreaming(int reps) throws Exception {
    for (int i = 0; i < reps; ++i) {
      StringReader reader = new StringReader(json);
      JsonReader jr = new JsonReader(reader);
      jr.beginArray();
      List<BagOfPrimitives> bags = new ArrayList<>();
      while (jr.hasNext()) {
        jr.beginObject();
        BagOfPrimitives bag = new BagOfPrimitives();
        while (jr.hasNext()) {
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
        bags.add(bag);
      }
      jr.endArray();
    }
  }
}
