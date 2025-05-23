/*
 * Copyright (C) 2012 Google Inc.
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
package com.google.gson.interceptors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Intercept} and {@link JsonPostDeserializer}.
 *
 * @author Inderjeet Singh
 */
public final class InterceptorTest {

  private Gson gson;

  @Before
  public void setUp() throws Exception {
    this.gson =
        new GsonBuilder()
            .registerTypeAdapterFactory(new InterceptorFactory())
            .enableComplexMapKeySerialization()
            .create();
  }

  @Test
  public void testExceptionsPropagated() {
    var e = assertThrows(JsonParseException.class, () -> gson.fromJson("{}", User.class));
    assertThat(e).hasMessageThat().isEqualTo("name and password are required fields.");
  }

  @Test
  public void testTopLevelClass() {
    User user = gson.fromJson("{name:'bob',password:'pwd'}", User.class);
    assertThat(user.email).isEqualTo(User.DEFAULT_EMAIL);
  }

  @Test
  public void testList() {
    List<User> list =
        gson.fromJson("[{name:'bob',password:'pwd'}]", new TypeToken<List<User>>() {}.getType());
    User user = list.get(0);
    assertThat(user.email).isEqualTo(User.DEFAULT_EMAIL);
  }

  @Test
  public void testCollection() {
    Collection<User> list =
        gson.fromJson(
            "[{name:'bob',password:'pwd'}]", new TypeToken<Collection<User>>() {}.getType());
    User user = list.iterator().next();
    assertThat(user.email).isEqualTo(User.DEFAULT_EMAIL);
  }

  @Test
  public void testMapKeyAndValues() {
    Type mapType = new TypeToken<Map<User, Address>>() {}.getType();
    var e =
        assertThrows(
            JsonSyntaxException.class,
            () -> gson.fromJson("[[{name:'bob',password:'pwd'},{}]]", mapType));
    assertThat(e).hasMessageThat().isEqualTo("Address city, state and zip are required fields.");

    Map<User, Address> map =
        gson.fromJson(
            "[[{name:'bob',password:'pwd'},{city:'Mountain View',state:'CA',zip:'94043'}]]",
            mapType);
    Entry<User, Address> entry = map.entrySet().iterator().next();
    assertThat(entry.getKey().email).isEqualTo(User.DEFAULT_EMAIL);
    assertThat(entry.getValue().firstLine).isEqualTo(Address.DEFAULT_FIRST_LINE);
  }

  @Test
  public void testField() {
    UserGroup userGroup = gson.fromJson("{user:{name:'bob',password:'pwd'}}", UserGroup.class);
    assertThat(userGroup.user.email).isEqualTo(User.DEFAULT_EMAIL);
  }

  @Test
  public void testCustomTypeAdapter() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                User.class,
                new TypeAdapter<User>() {
                  @Override
                  public void write(JsonWriter out, User value) throws IOException {
                    throw new UnsupportedOperationException();
                  }

                  @Override
                  public User read(JsonReader in) throws IOException {
                    in.beginObject();
                    assertThat(in.nextName()).isEqualTo("name");
                    String name = in.nextString();
                    assertThat(in.nextName()).isEqualTo("password");
                    String password = in.nextString();
                    in.endObject();
                    return new User(name, password);
                  }
                })
            .registerTypeAdapterFactory(new InterceptorFactory())
            .create();
    UserGroup userGroup = gson.fromJson("{user:{name:'bob',password:'pwd'}}", UserGroup.class);
    assertThat(userGroup.user.email).isEqualTo(User.DEFAULT_EMAIL);
  }

  @Test
  public void testDirectInvocationOfTypeAdapter() throws Exception {
    TypeAdapter<UserGroup> adapter = gson.getAdapter(UserGroup.class);
    UserGroup userGroup = adapter.fromJson("{\"user\":{\"name\":\"bob\",\"password\":\"pwd\"}}");
    assertThat(userGroup.user.email).isEqualTo(User.DEFAULT_EMAIL);
  }

  @SuppressWarnings("unused")
  private static final class UserGroup {
    User user;
    String city;
  }

  @Intercept(postDeserialize = UserValidator.class)
  @SuppressWarnings("unused")
  private static final class User {
    static final String DEFAULT_EMAIL = "invalid@invalid.com";
    String name;
    String password;
    String email;
    Address address;

    public User(String name, String password) {
      this.name = name;
      this.password = password;
    }
  }

  public static final class UserValidator implements JsonPostDeserializer<User> {
    @Override
    public void postDeserialize(User user) {
      if (user.name == null || user.password == null) {
        throw new JsonSyntaxException("name and password are required fields.");
      }
      if (user.email == null) {
        user.email = User.DEFAULT_EMAIL;
      }
    }
  }

  @Intercept(postDeserialize = AddressValidator.class)
  @SuppressWarnings("unused")
  private static final class Address {
    static final String DEFAULT_FIRST_LINE = "unknown";
    String firstLine;
    String secondLine;
    String city;
    String state;
    String zip;
  }

  public static final class AddressValidator implements JsonPostDeserializer<Address> {
    @Override
    public void postDeserialize(Address address) {
      if (address.city == null || address.state == null || address.zip == null) {
        throw new JsonSyntaxException("Address city, state and zip are required fields.");
      }
      if (address.firstLine == null) {
        address.firstLine = Address.DEFAULT_FIRST_LINE;
      }
    }
  }
}
