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
import com.google.caliper.Param;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Caliper based micro benchmarks for Gson serialization
 *
 * @author Inderjeet Singh
 * @author Jesse Wilson
 * @author Joel Leitch
 */
public class SerializationBenchmark {

  private Gson gson;
  private BagOfPrimitives bag;
  @Param private boolean pretty;

  public static void main(String[] args) {
    NonUploadingCaliperRunner.run(SerializationBenchmark.class, args);
  }

  @BeforeExperiment
  void setUp() throws Exception {
    this.gson = pretty ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
    this.bag = new BagOfPrimitives(10L, 1, false, "foo");
  }

  public void timeObjectSerialization(int reps) {
    for (int i = 0; i < reps; ++i) {
      String unused = gson.toJson(bag);
    }
  }
}
