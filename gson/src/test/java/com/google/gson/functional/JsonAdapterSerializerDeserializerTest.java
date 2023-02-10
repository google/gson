/*
 * Copyright (C) 2016 Google Inc.
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

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;
import java.lang.reflect.Type;
import org.junit.Test;

/**
 * Functional tests for the {@link JsonAdapter} annotation on fields where the value is of
 * type {@link JsonSerializer} or {@link JsonDeserializer}.
 */
public final class JsonAdapterSerializerDeserializerTest {

  @Test
  public void testJsonSerializerDeserializerBasedJsonAdapterOnFields() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer(new User("Inderjeet Singh"), null, new User("Jesse Wilson")));
    assertThat(json).isEqualTo("{\"user1\":\"UserSerializer\",\"user3\":\"UserSerializerDeserializer\"}");
    Computer computer = gson.fromJson("{'user2':'Jesse Wilson','user3':'Jake Wharton'}", Computer.class);
    assertThat(computer.user2.name).isEqualTo("UserSerializer");
    assertThat(computer.user3.name).isEqualTo("UserSerializerDeserializer");
  }

  private static final class Computer {
    @JsonAdapter(UserSerializer.class) final User user1;
    @JsonAdapter(UserDeserializer.class) final User user2;
    @JsonAdapter(UserSerializerDeserializer.class) final User user3;
    Computer(User user1, User user2, User user3) {
      this.user1 = user1;
      this.user2 = user2;
      this.user3 = user3;
    }
  }

  private static final class User {
    public final String name;
    private User(String name) {
      this.name = name;
    }
  }

  private static final class UserSerializer implements JsonSerializer<User> {
    @Override
    public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive("UserSerializer");
    }
  }

  private static final class UserDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return new User("UserSerializer");
    }
  }

  private static final class UserSerializerDeserializer implements JsonSerializer<User>, JsonDeserializer<User> {
    @Override
    public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive("UserSerializerDeserializer");
    }
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return new User("UserSerializerDeserializer");
    }
  }

  @Test
  public void testJsonSerializerDeserializerBasedJsonAdapterOnClass() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer2(new User2("Inderjeet Singh")));
    assertThat(json).isEqualTo("{\"user\":\"UserSerializerDeserializer2\"}");
    Computer2 computer = gson.fromJson("{'user':'Inderjeet Singh'}", Computer2.class);
    assertThat(computer.user.name).isEqualTo("UserSerializerDeserializer2");
  }

  private static final class Computer2 {
    final User2 user;
    Computer2(User2 user) {
      this.user = user;
    }
  }

  @JsonAdapter(UserSerializerDeserializer2.class)
  private static final class User2 {
    public final String name;
    private User2(String name) {
      this.name = name;
    }
  }

  private static final class UserSerializerDeserializer2 implements JsonSerializer<User2>, JsonDeserializer<User2> {
    @Override
    public JsonElement serialize(User2 src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive("UserSerializerDeserializer2");
    }
    @Override
    public User2 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return new User2("UserSerializerDeserializer2");
    }
  }

  @Test
  public void testDifferentJsonAdaptersForGenericFieldsOfSameRawType() {
    Container c = new Container("Foo", 10);
    Gson gson = new Gson();
    String json = gson.toJson(c);
    assertThat(json).contains("\"a\":\"BaseStringAdapter\"");
    assertThat(json).contains("\"b\":\"BaseIntegerAdapter\"");
  }

  private static final class Container {
    @JsonAdapter(BaseStringAdapter.class) Base<String> a;
    @JsonAdapter(BaseIntegerAdapter.class) Base<Integer> b;
    Container(String a, int b) {
      this.a = new Base<>(a);
      this.b = new Base<>(b);
    }
  }

  private static final class Base<T> {
    @SuppressWarnings("unused")
    T value;
    Base(T value) {
      this.value = value;
    }
  }

  private static final class BaseStringAdapter implements JsonSerializer<Base<String>> {
    @Override public JsonElement serialize(Base<String> src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive("BaseStringAdapter");
    }
  }

  private static final class BaseIntegerAdapter implements JsonSerializer<Base<Integer>> {
    @Override public JsonElement serialize(Base<Integer> src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive("BaseIntegerAdapter");
    }
  }

  @Test
  public void testJsonAdapterNullSafe() {
    Gson gson = new Gson();
    String json = gson.toJson(new Computer3(null, null));
    assertThat(json).isEqualTo("{\"user1\":\"UserSerializerDeserializer\"}");
    Computer3 computer3 = gson.fromJson("{\"user1\":null, \"user2\":null}", Computer3.class);
    assertThat(computer3.user1.name).isEqualTo("UserSerializerDeserializer");
    assertThat(computer3.user2).isNull();
  }

  private static final class Computer3 {
    @JsonAdapter(value = UserSerializerDeserializer.class, nullSafe = false) final User user1;
    @JsonAdapter(value = UserSerializerDeserializer.class) final User user2;
    Computer3(User user1, User user2) {
      this.user1 = user1;
      this.user2 = user2;
    }
  }
}
