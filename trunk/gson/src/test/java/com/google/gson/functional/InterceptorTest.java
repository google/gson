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
package com.google.gson.functional;

import java.util.List;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.internal.alpha.Intercept;
import com.google.gson.internal.alpha.JsonPostDeserializer;
import com.google.gson.reflect.TypeToken;

/**
 * Unit tests for {@link Intercept} and {@link JsonPostDeserializer}.
 *
 * @author Inderjeet Singh
 */
public final class InterceptorTest extends TestCase {

  private Gson gson;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.gson = new Gson();
  }

  public void testExceptionsPropagated() {
    try {
      gson.fromJson("{}", User.class);
      fail();
    } catch (JsonParseException expected) {}
  }

  public void testPostDeserializeTopLevelClass() {
    User user = gson.fromJson("{name:'bob',password:'pwd'}", User.class);
    assertEquals(User.DEFAULT_EMAIL, user.email);
  }

  public void testPostDeserializeList() {
    List<User> list = gson.fromJson("[{name:'bob',password:'pwd'}]", new TypeToken<List<User>>(){}.getType());
    User user = list.get(0);
    assertEquals(User.DEFAULT_EMAIL, user.email);
  }

  public void testPostDeserializeField() {
    UserGroup userGroup = gson.fromJson("{user:{name:'bob',password:'pwd'}}", UserGroup.class);
    assertEquals(User.DEFAULT_EMAIL, userGroup.user.email);
  }

  private static final class UserGroup {
    User user;
    String city;
  }

  @Intercept(postDeserialize = UserValidator.class)
  private static final class User {
    static final String DEFAULT_EMAIL = "invalid@invalid.com";
    String name;
    String password;
    String email;
  }

  private static final class UserValidator implements JsonPostDeserializer<User> {
    public void postDeserialize(User user) {
      if (user.name == null || user.password == null) {
        throw new JsonParseException("name and password are required fields.");
      }
      if (user.email == null) user.email = User.DEFAULT_EMAIL;
    }
  }
}