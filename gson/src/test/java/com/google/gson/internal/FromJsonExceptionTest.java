/*
 * Copyright (C) 2025 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.internal;

import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import org.junit.Test;

/** Tests wrapping of JsonParseException into JsonSyntaxException (issue #2816). */
public class FromJsonExceptionTest {
  static class User {}

  static class UserBadDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
      throw new JsonParseException("bad parse");
    }
  }

  @Test
  public void testFromJsonWrapsJsonParseException() {
    Gson gson =
        new GsonBuilder().registerTypeAdapter(User.class, new UserBadDeserializer()).create();
    try {
      gson.fromJson("{}", User.class);
      fail("Expected JsonSyntaxException to be thrown");
    } catch (JsonSyntaxException expected) {
      // success
    }
  }
}
