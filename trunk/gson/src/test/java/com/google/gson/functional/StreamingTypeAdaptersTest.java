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

package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

public final class StreamingTypeAdaptersTest extends TestCase {
  private Gson miniGson = new GsonBuilder().create();
  private TypeAdapter<Truck> truckAdapter = miniGson.getAdapter(Truck.class);
  private TypeAdapter<Map<String, Double>> mapAdapter
      = miniGson.getAdapter(new TypeToken<Map<String, Double>>() {});

  public void testSerialize() throws IOException {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29));
    truck.horsePower = 300;

    assertEquals("{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}",
        truckAdapter.toJson(truck).replace('\"', '\''));
  }

  public void testDeserialize() throws IOException {
    String json = "{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}";
    Truck truck = truckAdapter.fromJson(json);
    assertEquals(300.0, truck.horsePower);
    assertEquals(Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29)), truck.passengers);
  }

  public void testSerializeNullField() throws IOException {
    Truck truck = new Truck();
    truck.passengers = null;
    assertEquals("{'horsePower':0.0,'passengers':null}",
        truckAdapter.toJson(truck).replace('\"', '\''));
  }

  public void testDeserializeNullField() throws IOException {
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':null}");
    assertNull(truck.passengers);
  }

  public void testSerializeNullObject() throws IOException {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList((Person) null);
    assertEquals("{'horsePower':0.0,'passengers':[null]}",
        truckAdapter.toJson(truck).replace('\"', '\''));
  }

  public void testDeserializeNullObject() throws IOException {
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':[null]}");
    assertEquals(Arrays.asList((Person) null), truck.passengers);
  }

  public void testSerializeWithCustomTypeAdapter() throws IOException {
    usePersonNameAdapter();
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29));
    assertEquals("{'horsePower':0.0,'passengers':['Jesse','Jodie']}",
        truckAdapter.toJson(truck).replace('\"', '\''));
  }

  public void testDeserializeWithCustomTypeAdapter() throws IOException {
    usePersonNameAdapter();
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':['Jesse','Jodie']}");
    assertEquals(Arrays.asList(new Person("Jesse", -1), new Person("Jodie", -1)), truck.passengers);
  }

  private void usePersonNameAdapter() {
    TypeAdapter<Person> personNameAdapter = new TypeAdapter<Person>() {
      @Override public Person read(JsonReader in) throws IOException {
        String name = in.nextString();
        return new Person(name, -1);
      }
      @Override public void write(JsonWriter out, Person value) throws IOException {
        out.value(value.name);
      }
    };
    miniGson = new GsonBuilder().registerTypeAdapter(Person.class, personNameAdapter).create();
    truckAdapter = miniGson.getAdapter(Truck.class);
  }

  public void testSerializeMap() throws IOException {
    Map<String, Double> map = new LinkedHashMap<String, Double>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertEquals("{'a':5.0,'b':10.0}", mapAdapter.toJson(map).replace('"', '\''));
  }

  public void testDeserializeMap() throws IOException {
    Map<String, Double> map = new LinkedHashMap<String, Double>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertEquals(map, mapAdapter.fromJson("{'a':5.0,'b':10.0}"));
  }

  public void testSerialize1dArray() throws IOException {
    TypeAdapter<double[]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[]>() {});
    assertEquals("[1.0,2.0,3.0]", arrayAdapter.toJson(new double[]{1.0, 2.0, 3.0}));
  }

  public void testDeserialize1dArray() throws IOException {
    TypeAdapter<double[]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[]>() {});
    double[] array = arrayAdapter.fromJson("[1.0,2.0,3.0]");
    assertTrue(Arrays.toString(array), Arrays.equals(new double[]{1.0, 2.0, 3.0}, array));
  }

  public void testSerialize2dArray() throws IOException {
    TypeAdapter<double[][]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[][]>() {});
    double[][] array = { {1.0, 2.0 }, { 3.0 } };
    assertEquals("[[1.0,2.0],[3.0]]", arrayAdapter.toJson(array));
  }

  public void testDeserialize2dArray() throws IOException {
    TypeAdapter<double[][]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[][]>() {});
    double[][] array = arrayAdapter.fromJson("[[1.0,2.0],[3.0]]");
    double[][] expected = { {1.0, 2.0 }, { 3.0 } };
    assertTrue(Arrays.toString(array), Arrays.deepEquals(expected, array));
  }

  public void testSerializeRecursive() throws IOException {
    TypeAdapter<Node> nodeAdapter = miniGson.getAdapter(Node.class);
    Node root = new Node("root");
    root.left = new Node("left");
    root.right = new Node("right");
    assertEquals("{'label':'root',"
        + "'left':{'label':'left','left':null,'right':null},"
        + "'right':{'label':'right','left':null,'right':null}}",
        nodeAdapter.toJson(root).replace('"', '\''));
  }

  static class Truck {
    double horsePower;
    List<Person> passengers = Collections.emptyList();
  }

  static class Person {
    int age;
    String name;
    Person(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public Person() {} // TODO: use Joel's constructor code so we don't need this

    @Override public boolean equals(Object o) {
      return o instanceof Person
          && ((Person) o).name.equals(name)
          && ((Person) o).age == age;
    }
    @Override public int hashCode() {
      return name.hashCode() ^ age;
    }
  }

  static class Node {
    String label;
    Node left;
    Node right;
    Node(String label) {
      this.label = label;
    }
    public Node() {} // TODO: use Joel's constructor code so we don't need this
  }
}
