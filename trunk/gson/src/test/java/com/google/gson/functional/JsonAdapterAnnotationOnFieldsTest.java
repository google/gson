/*
 * Copyright (C) 2014 Google Inc.
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

import java.io.IOException;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Functional tests for the {@link com.google.gson.annotations.JsonAdapter} annotation on fields.
 */
public final class JsonAdapterAnnotationOnFieldsTest extends TestCase {

  public void testJsonAdapterInvoked() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer(new User("Inderjeet Singh")));
    assertEquals("{\"user\":{\"firstName\":\"Inderjeet\",\"lastName\":\"Singh\"}}", json);
    Computer computer = gson.fromJson("{'user':{'firstName':'Jesse','lastName':'Wilson'}}", Computer.class);
    assertEquals("Jesse Wilson", computer.user.name);
  }

  public void testRegisteredTypeAdapterOverridesFieldAnnotation() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Part.class, new TypeAdapter<Part>() {
        @Override public void write(JsonWriter out, Part part) throws IOException {
          out.value("registeredAdapter");
        }
        @Override public Part read(JsonReader in) throws IOException {
          return new Part(in.nextString());
        }
      }).create();
    String json = gson.toJson(new Gadget(new Part("screen")));
    assertEquals("{\"part\":\"registeredAdapter\"}", json);
    Gadget gadget = gson.fromJson("{'part':'registeredAdapterValue'}", Gadget.class);
    assertEquals("registeredAdapterValue", gadget.part.name);
  }

  public void testFieldAnnotationSupersedesClassAnnotation() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer2(new User("Inderjeet Singh")));
    assertEquals("{\"user\":\"userJsonAdapter2\"}", json);
    Computer2 target = gson.fromJson("{'user':'userJsonAdapter2Value'}", Computer2.class);
    assertEquals("userJsonAdapter2Value", target.user.name);
  }

  private static final class Gadget {
    @JsonAdapter(PartJsonAdapter.class)
    final Part part;
    Gadget(Part part) {
      this.part = part;
    }
  }

  private static final class Part {
    final String name;
    Part(String name) {
      this.name = name;
    }
  }

  private static class PartJsonAdapter extends TypeAdapter<Part> {
    @Override public void write(JsonWriter out, Part part) throws IOException {
      out.value(part.name);
    }
    @Override public Part read(JsonReader in) throws IOException {
      in.nextString();
      return new Part("partJsonAdapter");
    }
  }

  private static final class Computer {
    final User user;
    Computer(User user) {
      this.user = user;
    }
  }

  @JsonAdapter(UserJsonAdapter.class)
  private static class User {
    public final String name;
    private User(String name) {
      this.name = name;
    }
  }

  private static class UserJsonAdapter extends TypeAdapter<User> {
    @Override public void write(JsonWriter out, User user) throws IOException {
      // implement write: combine firstName and lastName into name
      out.beginObject();
      String[] parts = user.name.split(" ");
      out.name("firstName");
      out.value(parts[0]);
      out.name("lastName");
      out.value(parts[1]);
      out.endObject();
    }
    @Override public User read(JsonReader in) throws IOException {
      // implement read: split name into firstName and lastName
      in.beginObject();
      in.nextName();
      String firstName = in.nextString();
      in.nextName();
      String lastName = in.nextString();
      in.endObject();
      return new User(firstName + " " + lastName);
    }
  }

  private static final class Computer2 {
    // overrides the JsonAdapter annotation of User with this
    @JsonAdapter(UserJsonAdapter2.class)
    final User user;
    Computer2(User user) {
      this.user = user;
    }
  }
  private static final class UserJsonAdapter2 extends TypeAdapter<User> {
    @Override public void write(JsonWriter out, User user) throws IOException {
      out.value("userJsonAdapter2");
    }
    @Override public User read(JsonReader in) throws IOException {
      return new User(in.nextString());
    }
  }
}
