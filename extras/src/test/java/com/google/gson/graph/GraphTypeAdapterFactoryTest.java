/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson.graph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;

public final class GraphTypeAdapterFactoryTest extends TestCase {
  public void testSerialization() {
    Roshambo rock = new Roshambo("ROCK");
    Roshambo scissors = new Roshambo("SCISSORS");
    Roshambo paper = new Roshambo("PAPER");
    rock.beats = scissors;
    scissors.beats = paper;
    paper.beats = rock;

    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GraphTypeAdapterFactory.of(Roshambo.class))
        .create();

    assertEquals("{'0x1':{'name':'ROCK','beats':'0x2'}," +
        "'0x2':{'name':'SCISSORS','beats':'0x3'}," +
        "'0x3':{'name':'PAPER','beats':'0x1'}}",
        gson.toJson(rock).replace('\"', '\''));
  }

  public void testDeserialization() {
    String json = "{'0x1':{'name':'ROCK','beats':'0x2'}," +
        "'0x2':{'name':'SCISSORS','beats':'0x3'}," +
        "'0x3':{'name':'PAPER','beats':'0x1'}}";

    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(GraphTypeAdapterFactory.of(Roshambo.class))
        .create();

    Roshambo rock = gson.fromJson(json, Roshambo.class);
    assertEquals("ROCK", rock.name);
    Roshambo scissors = rock.beats;
    assertEquals("SCISSORS", scissors.name);
    Roshambo paper = scissors.beats;
    assertEquals("PAPER", paper.name);
    assertSame(rock, paper.beats); // TODO: currently fails
  }

  static class Roshambo {
    String name;
    Roshambo beats;
    Roshambo(String name) {
      this.name = name;
    }
  }
}
