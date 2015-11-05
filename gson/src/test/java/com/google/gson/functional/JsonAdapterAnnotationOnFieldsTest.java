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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * Functional tests for the {@link com.google.gson.annotations.JsonAdapter} annotation on fields.
 */
public final class JsonAdapterAnnotationOnFieldsTest extends TestCase {
  public void testClassAnnotationAdapterTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer(new User("Inderjeet Singh")));
    assertEquals("{\"user\":\"UserClassAnnotationAdapter\"}", json);
    Computer computer = gson.fromJson("{'user':'Inderjeet Singh'}", Computer.class);
    assertEquals("UserClassAnnotationAdapter", computer.user.name);
  }

  public void testClassAnnotationAdapterFactoryTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    String json = gson.toJson(new Gizmo(new Part("Part")));
    assertEquals("{\"part\":\"GizmoPartTypeAdapterFactory\"}", json);
    Gizmo computer = gson.fromJson("{'part':'Part'}", Gizmo.class);
    assertEquals("GizmoPartTypeAdapterFactory", computer.part.name);
  }

  public void testRegisteredTypeAdapterTakesPrecedenceOverClassAnnotationAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(User.class, new RegisteredUserAdapter())
        .create();
    String json = gson.toJson(new Computer(new User("Inderjeet Singh")));
    assertEquals("{\"user\":\"RegisteredUserAdapter\"}", json);
    Computer computer = gson.fromJson("{'user':'Inderjeet Singh'}", Computer.class);
    assertEquals("RegisteredUserAdapter", computer.user.name);
  }

  public void testFieldAnnotationTakesPrecedenceOverRegisteredTypeAdapter() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Part.class, new TypeAdapter<Part>() {
        @Override public void write(JsonWriter out, Part part) throws IOException {
          throw new AssertionError();
        }

        @Override public Part read(JsonReader in) throws IOException {
          throw new AssertionError();
        }
      }).create();
    String json = gson.toJson(new Gadget(new Part("screen")));
    assertEquals("{\"part\":\"PartJsonFieldAnnotationAdapter\"}", json);
    Gadget gadget = gson.fromJson("{'part':'screen'}", Gadget.class);
    assertEquals("PartJsonFieldAnnotationAdapter", gadget.part.name);
  }

  public void testFieldAnnotationTakesPrecedenceOverClassAnnotation() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer2(new User("Inderjeet Singh")));
    assertEquals("{\"user\":\"UserFieldAnnotationAdapter\"}", json);
    Computer2 target = gson.fromJson("{'user':'Interjeet Singh'}", Computer2.class);
    assertEquals("UserFieldAnnotationAdapter", target.user.name);
  }

  private static final class Gadget {
    @JsonAdapter(PartJsonFieldAnnotationAdapter.class)
    final Part part;
    Gadget(Part part) {
      this.part = part;
    }
  }

  private static final class Gizmo {
    @JsonAdapter(GizmoPartTypeAdapterFactory.class)
    final Part part;
    Gizmo(Part part) {
      this.part = part;
    }
  }

  private static final class Part {
    final String name;
    public Part(String name) {
      this.name = name;
    }
  }

  private static class PartJsonFieldAnnotationAdapter extends TypeAdapter<Part> {
    @Override public void write(JsonWriter out, Part part) throws IOException {
      out.value("PartJsonFieldAnnotationAdapter");
    }
    @Override public Part read(JsonReader in) throws IOException {
      in.nextString();
      return new Part("PartJsonFieldAnnotationAdapter");
    }
  }

  private static class GizmoPartTypeAdapterFactory implements TypeAdapterFactory {
    @Override public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
      return new TypeAdapter<T>() {
        @Override public void write(JsonWriter out, T value) throws IOException {
          out.value("GizmoPartTypeAdapterFactory");
        }
        @SuppressWarnings("unchecked")
        @Override public T read(JsonReader in) throws IOException {
          in.nextString();
          return (T) new Part("GizmoPartTypeAdapterFactory");
        }
      };
    }
  }

  private static final class Computer {
    final User user;
    Computer(User user) {
      this.user = user;
    }
  }

  @JsonAdapter(UserClassAnnotationAdapter.class)
  private static class User {
    public final String name;
    private User(String name) {
      this.name = name;
    }
  }

  private static class UserClassAnnotationAdapter extends TypeAdapter<User> {
    @Override public void write(JsonWriter out, User user) throws IOException {
      out.value("UserClassAnnotationAdapter");
    }
    @Override public User read(JsonReader in) throws IOException {
      in.nextString();
      return new User("UserClassAnnotationAdapter");
    }
  }

  private static final class Computer2 {
    // overrides the JsonAdapter annotation of User with this
    @JsonAdapter(UserFieldAnnotationAdapter.class)
    final User user;
    Computer2(User user) {
      this.user = user;
    }
  }

  private static final class UserFieldAnnotationAdapter extends TypeAdapter<User> {
    @Override public void write(JsonWriter out, User user) throws IOException {
      out.value("UserFieldAnnotationAdapter");
    }
    @Override public User read(JsonReader in) throws IOException {
      in.nextString();
      return new User("UserFieldAnnotationAdapter");
    }
  }

  private static final class RegisteredUserAdapter extends TypeAdapter<User> {
    @Override public void write(JsonWriter out, User user) throws IOException {
      out.value("RegisteredUserAdapter");
    }
    @Override public User read(JsonReader in) throws IOException {
      in.nextString();
      return new User("RegisteredUserAdapter");
    }
  }

  public void testJsonAdapterInvokedOnlyForAnnotatedFields() {
    Gson gson = new Gson();
    String json = "{'part1':'name','part2':{'name':'name2'}}";
    GadgetWithTwoParts gadget = gson.fromJson(json, GadgetWithTwoParts.class);
    assertEquals("PartJsonFieldAnnotationAdapter", gadget.part1.name);
    assertEquals("name2", gadget.part2.name);
  }

  private static final class GadgetWithTwoParts {
    @JsonAdapter(PartJsonFieldAnnotationAdapter.class) final Part part1;
    final Part part2; // Doesn't have the JsonAdapter annotation
    @SuppressWarnings("unused") GadgetWithTwoParts(Part part1, Part part2) {
      this.part1 = part1;
      this.part2 = part2;
    }
  }
}
