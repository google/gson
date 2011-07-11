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

package com.google.gson.mini;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

public final class MiniGsonTest extends TestCase {

  public void testSerialize() throws IOException {
    Person jesse = new Person("Jesse", 29);
    Person jodie = new Person("Jodie", 29);
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(jesse, jodie);
    truck.horsePower = 300;

    MiniGson miniGson = new MiniGson.Builder().build();
    TypeAdapter<Truck> truckAdapter = miniGson.getAdapter(Truck.class);

    String json = truckAdapter.toJson(truck);
    assertEquals("{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}",
        json.replace('\"', '\''));
  }

  public void testDeserialize() throws IOException {
    String json = "{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}";

    MiniGson miniGson = new MiniGson.Builder().build();
    TypeAdapter<Truck> truckAdapter = miniGson.getAdapter(Truck.class);
    Truck truck = truckAdapter.fromJson(json);

    assertEquals(300.0, truck.horsePower);
    Person jesse = truck.passengers.get(0);
    assertEquals("Jesse", jesse.name);
    assertEquals(29, jesse.age);
    Person jodie = truck.passengers.get(1);
    assertEquals("Jodie", jodie.name);
    assertEquals(29, jodie.age);
  }

  static class Truck {
    double horsePower;
    List<Person> passengers;
  }

  static class Person {
    int age;
    String name;

    Person(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public Person() {} // TODO: use Joel's constructor code so we don't need this
  }
}
