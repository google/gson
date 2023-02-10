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

package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.junit.Test;

/**
 * Test that the hierarchy adapter works when subtypes are used.
 */
public final class TypeHierarchyAdapterTest {

  @Test
  public void testTypeHierarchy() {
    Manager andy = new Manager();
    andy.userid = "andy";
    andy.startDate = 2005;
    andy.minions = new Employee[] {
        new Employee("inder", 2007),
        new Employee("joel", 2006),
        new Employee("jesse", 2006),
    };

    CEO eric = new CEO();
    eric.userid = "eric";
    eric.startDate = 2001;
    eric.assistant = new Employee("jerome", 2006);

    eric.minions = new Employee[] {
        new Employee("larry", 1998),
        new Employee("sergey", 1998),
        andy,
    };

    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Employee.class, new EmployeeAdapter())
        .setPrettyPrinting()
        .create();

    Company company = new Company();
    company.ceo = eric;

    String json = gson.toJson(company, Company.class);
    assertThat(json).isEqualTo("{\n" +
        "  \"ceo\": {\n" +
        "    \"userid\": \"eric\",\n" +
        "    \"startDate\": 2001,\n" +
        "    \"minions\": [\n" +
        "      {\n" +
        "        \"userid\": \"larry\",\n" +
        "        \"startDate\": 1998\n" +
        "      },\n" +
        "      {\n" +
        "        \"userid\": \"sergey\",\n" +
        "        \"startDate\": 1998\n" +
        "      },\n" +
        "      {\n" +
        "        \"userid\": \"andy\",\n" +
        "        \"startDate\": 2005,\n" +
        "        \"minions\": [\n" +
        "          {\n" +
        "            \"userid\": \"inder\",\n" +
        "            \"startDate\": 2007\n" +
        "          },\n" +
        "          {\n" +
        "            \"userid\": \"joel\",\n" +
        "            \"startDate\": 2006\n" +
        "          },\n" +
        "          {\n" +
        "            \"userid\": \"jesse\",\n" +
        "            \"startDate\": 2006\n" +
        "          }\n" +
        "        ]\n" +
        "      }\n" +
        "    ],\n" +
        "    \"assistant\": {\n" +
        "      \"userid\": \"jerome\",\n" +
        "      \"startDate\": 2006\n" +
        "    }\n" +
        "  }\n" +
        "}");

    Company copied = gson.fromJson(json, Company.class);
    assertThat(gson.toJson(copied, Company.class)).isEqualTo(json);
    assertThat(company.ceo.userid).isEqualTo(copied.ceo.userid);
    assertThat(company.ceo.assistant.userid).isEqualTo(copied.ceo.assistant.userid);
    assertThat(company.ceo.minions[0].userid).isEqualTo(copied.ceo.minions[0].userid);
    assertThat(company.ceo.minions[1].userid).isEqualTo(copied.ceo.minions[1].userid);
    assertThat(company.ceo.minions[2].userid).isEqualTo(copied.ceo.minions[2].userid);
    assertThat(((Manager) company.ceo.minions[2]).minions[0].userid).isEqualTo(((Manager) copied.ceo.minions[2]).minions[0].userid);
    assertThat(((Manager) company.ceo.minions[2]).minions[1].userid).isEqualTo(((Manager) copied.ceo.minions[2]).minions[1].userid);
  }

  @Test
  public void testRegisterSuperTypeFirst() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Employee.class, new EmployeeAdapter())
        .registerTypeHierarchyAdapter(Manager.class, new ManagerAdapter())
        .create();

    Manager manager = new Manager();
    manager.userid = "inder";

    String json = gson.toJson(manager, Manager.class);
    assertThat(json).isEqualTo("\"inder\"");
    Manager copied = gson.fromJson(json, Manager.class);
    assertThat(copied.userid).isEqualTo(manager.userid);
  }

  /** This behaviour changed in Gson 2.1; it used to throw. */
  @Test
  public void testRegisterSubTypeFirstAllowed() {
    new GsonBuilder()
        .registerTypeHierarchyAdapter(Manager.class, new ManagerAdapter())
        .registerTypeHierarchyAdapter(Employee.class, new EmployeeAdapter())
        .create();
  }

  static class ManagerAdapter implements JsonSerializer<Manager>, JsonDeserializer<Manager> {
    @Override public Manager deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
      Manager result = new Manager();
      result.userid = json.getAsString();
      return result;
    }
    @Override public JsonElement serialize(Manager src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.userid);
    }
  }

  static class EmployeeAdapter implements JsonSerializer<Employee>, JsonDeserializer<Employee> {
    @Override public JsonElement serialize(Employee employee, Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject result = new JsonObject();
      result.add("userid", context.serialize(employee.userid, String.class));
      result.add("startDate", context.serialize(employee.startDate, long.class));
      if (employee instanceof Manager) {
        result.add("minions", context.serialize(((Manager) employee).minions, Employee[].class));
        if (employee instanceof CEO) {
          result.add("assistant", context.serialize(((CEO) employee).assistant, Employee.class));
        }
      }
      return result;
    }

    @Override public Employee deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      JsonObject object = json.getAsJsonObject();
      Employee result = null;

      // if the employee has an assistant, she must be the CEO
      JsonElement assistant = object.get("assistant");
      if (assistant != null) {
        result = new CEO();
        ((CEO) result).assistant = context.deserialize(assistant, Employee.class);
      }

      // only managers have minions
      JsonElement minons = object.get("minions");
      if (minons != null) {
        if (result == null) {
          result = new Manager();
        }
        ((Manager) result).minions = context.deserialize(minons, Employee[].class);
      }

      if (result == null) {
        result = new Employee();
      }
      result.userid = context.deserialize(object.get("userid"), String.class);
      result.startDate = context.<Long>deserialize(object.get("startDate"), long.class);
      return result;
    }
  }

  static class Employee {
    String userid;
    long startDate;

    Employee(String userid, long startDate) {
      this.userid = userid;
      this.startDate = startDate;
    }

    Employee() {}
  }

  static class Manager extends Employee {
    Employee[] minions;
  }

  static class CEO extends Manager {
    Employee assistant;
  }

  static class Company {
    CEO ceo;
  }
}
