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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public final class StreamingTypeAdaptersTest {
  private Gson miniGson = new GsonBuilder().create();
  private TypeAdapter<Truck> truckAdapter = miniGson.getAdapter(Truck.class);
  private TypeAdapter<Map<String, Double>> mapAdapter
      = miniGson.getAdapter(new TypeToken<Map<String, Double>>() {});

  @Test
  public void testSerialize() {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29));
    truck.horsePower = 300;

    assertThat(truckAdapter.toJson(truck).replace('\"', '\''))
        .isEqualTo("{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}");
  }

  @Test
  public void testDeserialize() throws IOException {
    String json = "{'horsePower':300.0,"
        + "'passengers':[{'age':29,'name':'Jesse'},{'age':29,'name':'Jodie'}]}";
    Truck truck = truckAdapter.fromJson(json.replace('\'', '\"'));
    assertThat(truck.horsePower).isEqualTo(300.0);
    assertThat(truck.passengers)
        .isEqualTo(Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29)));
  }

  @Test
  public void testSerializeNullField() {
    Truck truck = new Truck();
    truck.passengers = null;
    assertThat(truckAdapter.toJson(truck).replace('\"', '\''))
        .isEqualTo("{'horsePower':0.0,'passengers':null}");
  }

  @Test
  public void testDeserializeNullField() throws IOException {
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':null}".replace('\'', '\"'));
    assertThat(truck.passengers).isNull();
  }

  @Test
  public void testSerializeNullObject() {
    Truck truck = new Truck();
    truck.passengers = Arrays.asList((Person) null);
    assertThat(truckAdapter.toJson(truck).replace('\"', '\''))
        .isEqualTo("{'horsePower':0.0,'passengers':[null]}");
  }

  @Test
  public void testDeserializeNullObject() throws IOException {
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':[null]}".replace('\'', '\"'));
    assertThat(truck.passengers).isEqualTo(Arrays.asList((Person) null));
  }

  @Test
  public void testSerializeWithCustomTypeAdapter() {
    usePersonNameAdapter();
    Truck truck = new Truck();
    truck.passengers = Arrays.asList(new Person("Jesse", 29), new Person("Jodie", 29));
    assertThat(truckAdapter.toJson(truck).replace('\"', '\''))
        .isEqualTo("{'horsePower':0.0,'passengers':['Jesse','Jodie']}");
  }

  @Test
  public void testDeserializeWithCustomTypeAdapter() throws IOException {
    usePersonNameAdapter();
    Truck truck = truckAdapter.fromJson("{'horsePower':0.0,'passengers':['Jesse','Jodie']}".replace('\'', '\"'));
    assertThat(truck.passengers).isEqualTo(Arrays.asList(new Person("Jesse", -1), new Person("Jodie", -1)));
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

  @Test
  public void testSerializeMap() {
    Map<String, Double> map = new LinkedHashMap<>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertThat(mapAdapter.toJson(map).replace('"', '\'')).isEqualTo("{'a':5.0,'b':10.0}");
  }

  @Test
  public void testDeserializeMap() throws IOException {
    Map<String, Double> map = new LinkedHashMap<>();
    map.put("a", 5.0);
    map.put("b", 10.0);
    assertThat(mapAdapter.fromJson("{'a':5.0,'b':10.0}".replace('\'', '\"'))).isEqualTo(map);
  }

  @Test
  public void testSerialize1dArray() {
    TypeAdapter<double[]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[]>() {});
    assertThat(arrayAdapter.toJson(new double[]{ 1.0, 2.0, 3.0 })).isEqualTo("[1.0,2.0,3.0]");
  }

  @Test
  public void testDeserialize1dArray() throws IOException {
    TypeAdapter<double[]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[]>() {});
    double[] array = arrayAdapter.fromJson("[1.0,2.0,3.0]");
    assertWithMessage(Arrays.toString(array)).that(Arrays.equals(new double[]{1.0, 2.0, 3.0}, array)).isTrue();
  }

  @Test
  public void testSerialize2dArray() {
    TypeAdapter<double[][]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[][]>() {});
    double[][] array = { {1.0, 2.0 }, { 3.0 } };
    assertThat(arrayAdapter.toJson(array)).isEqualTo("[[1.0,2.0],[3.0]]");
  }

  @Test
  public void testDeserialize2dArray() throws IOException {
    TypeAdapter<double[][]> arrayAdapter = miniGson.getAdapter(new TypeToken<double[][]>() {});
    double[][] array = arrayAdapter.fromJson("[[1.0,2.0],[3.0]]");
    double[][] expected = { {1.0, 2.0 }, { 3.0 } };
    assertWithMessage(Arrays.toString(array)).that(Arrays.deepEquals(expected, array)).isTrue();
  }

  @Test
  public void testNullSafe() {
    TypeAdapter<Person> typeAdapter = new TypeAdapter<Person>() {
      @Override public Person read(JsonReader in) throws IOException {
        String[] values = in.nextString().split(",");
        return new Person(values[0], Integer.parseInt(values[1]));
      }
      @Override public void write(JsonWriter out, Person person) throws IOException {
        out.value(person.name + "," + person.age);
      }
    };
    Gson gson = new GsonBuilder().registerTypeAdapter(
        Person.class, typeAdapter).create();
    Truck truck = new Truck();
    truck.horsePower = 1.0D;
    truck.passengers = new ArrayList<>();
    truck.passengers.add(null);
    truck.passengers.add(new Person("jesse", 30));
    try {
      gson.toJson(truck, Truck.class);
      fail();
    } catch (NullPointerException expected) {}
    String json = "{horsePower:1.0,passengers:[null,'jesse,30']}";
    try {
      gson.fromJson(json, Truck.class);
      fail();
    } catch (JsonSyntaxException expected) {}
    gson = new GsonBuilder().registerTypeAdapter(Person.class, typeAdapter.nullSafe()).create();
    assertThat(gson.toJson(truck, Truck.class))
        .isEqualTo("{\"horsePower\":1.0,\"passengers\":[null,\"jesse,30\"]}");
    truck = gson.fromJson(json, Truck.class);
    assertThat(truck.horsePower).isEqualTo(1.0D);
    assertThat(truck.passengers.get(0)).isNull();
    assertThat(truck.passengers.get(1).name).isEqualTo("jesse");
  }

  @Test
  public void testSerializeRecursive() {
    TypeAdapter<Node> nodeAdapter = miniGson.getAdapter(Node.class);
    Node root = new Node("root");
    root.left = new Node("left");
    root.right = new Node("right");
    assertThat(nodeAdapter.toJson(root).replace('"', '\''))
        .isEqualTo("{'label':'root',"
            + "'left':{'label':'left','left':null,'right':null},"
            + "'right':{'label':'right','left':null,'right':null}}");
  }
  
  @Test
  public void testFromJsonTree() {
    JsonObject truckObject = new JsonObject();
    truckObject.add("horsePower", new JsonPrimitive(300));
    JsonArray passengersArray = new JsonArray();
    JsonObject jesseObject = new JsonObject();
    jesseObject.add("age", new JsonPrimitive(30));
    jesseObject.add("name", new JsonPrimitive("Jesse"));
    passengersArray.add(jesseObject);
    truckObject.add("passengers", passengersArray);

    Truck truck = truckAdapter.fromJsonTree(truckObject);
    assertThat(truck.horsePower).isEqualTo(300.0);
    assertThat(truck.passengers).isEqualTo(Arrays.asList(new Person("Jesse", 30)));
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
  }
}
