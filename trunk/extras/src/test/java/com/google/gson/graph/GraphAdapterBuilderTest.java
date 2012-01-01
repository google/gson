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
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

public final class GraphAdapterBuilderTest extends TestCase {
  public void testSerialization() {
    Roshambo rock = new Roshambo("ROCK");
    Roshambo scissors = new Roshambo("SCISSORS");
    Roshambo paper = new Roshambo("PAPER");
    rock.beats = scissors;
    scissors.beats = paper;
    paper.beats = rock;

    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Roshambo.class)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    assertEquals("{'0x1':{'name':'ROCK','beats':'0x2'}," +
        "'0x2':{'name':'SCISSORS','beats':'0x3'}," +
        "'0x3':{'name':'PAPER','beats':'0x1'}}",
        gson.toJson(rock).replace('"', '\''));
  }

  public void testDeserialization() {
    String json = "{'0x1':{'name':'ROCK','beats':'0x2'}," +
        "'0x2':{'name':'SCISSORS','beats':'0x3'}," +
        "'0x3':{'name':'PAPER','beats':'0x1'}}";

    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Roshambo.class)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    Roshambo rock = gson.fromJson(json, Roshambo.class);
    assertEquals("ROCK", rock.name);
    Roshambo scissors = rock.beats;
    assertEquals("SCISSORS", scissors.name);
    Roshambo paper = scissors.beats;
    assertEquals("PAPER", paper.name);
    assertSame(rock, paper.beats);
  }

  public void testSerializationDirectSelfReference() {
    Roshambo suicide = new Roshambo("SUICIDE");
    suicide.beats = suicide;

    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Roshambo.class)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    assertEquals("{'0x1':{'name':'SUICIDE','beats':'0x1'}}",
        gson.toJson(suicide).replace('"', '\''));
  }

  public void testDeserializationDirectSelfReference() {
    String json = "{'0x1':{'name':'SUICIDE','beats':'0x1'}}";

    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Roshambo.class)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    Roshambo suicide = gson.fromJson(json, Roshambo.class);
    assertEquals("SUICIDE", suicide.name);
    assertSame(suicide, suicide.beats);
  }

  public void testSerializeListOfLists() {
    Type listOfListsType = new TypeToken<List<List<?>>>() {}.getType();
    Type listOfAnyType = new TypeToken<List<?>>() {}.getType();

    List<List<?>> listOfLists = new ArrayList<List<?>>();
    listOfLists.add(listOfLists);
    listOfLists.add(new ArrayList<Object>());

    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(listOfListsType)
        .addType(listOfAnyType)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    String json = gson.toJson(listOfLists, listOfListsType);
    assertEquals("{'0x1':['0x1','0x2'],'0x2':[]}", json.replace('"', '\''));
  }

  public void testDeserializeListOfLists() {
    Type listOfAnyType = new TypeToken<List<?>>() {}.getType();
    Type listOfListsType = new TypeToken<List<List<?>>>() {}.getType();

    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(listOfListsType)
        .addType(listOfAnyType)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    List<List<?>> listOfLists = gson.fromJson("{'0x1':['0x1','0x2'],'0x2':[]}", listOfListsType);
    assertEquals(2, listOfLists.size());
    assertSame(listOfLists, listOfLists.get(0));
    assertEquals(Collections.emptyList(), listOfLists.get(1));
  }

  public void testSerializationWithMultipleTypes() {
    Company google = new Company("Google");
    new Employee("Jesse", google);
    new Employee("Joel", google);

    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Company.class)
        .addType(Employee.class)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    assertEquals("{'0x1':{'name':'Google','employees':['0x2','0x3']},"
        + "'0x2':{'name':'Jesse','company':'0x1'},"
        + "'0x3':{'name':'Joel','company':'0x1'}}",
        gson.toJson(google).replace('"', '\''));
  }

  public void testDeserializationWithMultipleTypes() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    new GraphAdapterBuilder()
        .addType(Company.class)
        .addType(Employee.class)
        .registerOn(gsonBuilder);
    Gson gson = gsonBuilder.create();

    String json = "{'0x1':{'name':'Google','employees':['0x2','0x3']},"
        + "'0x2':{'name':'Jesse','company':'0x1'},"
        + "'0x3':{'name':'Joel','company':'0x1'}}";
    Company company = gson.fromJson(json, Company.class);
    assertEquals("Google", company.name);
    Employee jesse = company.employees.get(0);
    assertEquals("Jesse", jesse.name);
    assertEquals(company, jesse.company);
    Employee joel = company.employees.get(1);
    assertEquals("Joel", joel.name);
    assertEquals(company, joel.company);
  }

  static class Roshambo {
    String name;
    Roshambo beats;
    Roshambo(String name) {
      this.name = name;
    }
  }

  static class Employee {
    final String name;
    final Company company;
    Employee(String name, Company company) {
      this.name = name;
      this.company = company;
      this.company.employees.add(this);
    }
  }

  static class Company {
    final String name;
    final List<Employee> employees = new ArrayList<Employee>();
    Company(String name) {
      this.name = name;
    }
  }
}
