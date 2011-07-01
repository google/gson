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

import java.lang.reflect.Type;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Functional tests for serialize default behavior where a custom type adapter is allowed to invoke
 * context.serialize on self.
 *
 * @author Inderjeet Singh
 */
public class SystemOnlyTypeAdaptersTest extends TestCase {

  private Gson gson;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.gson = new GsonBuilder().registerTypeAdapter(Foo.class, new FooTypeAdapter()).create();
  }

  public void testSerializeDefault() {
    String json = gson.toJson(new Foo());
    assertEquals("{\"a\":10,\"secret-key\":\"abracadabra\"}", json);
  }

  public void testDeserializeDefault() {
    String json = "{a:5,'secret-key':'abracadabra'}";
    Foo foo = gson.fromJson(json, Foo.class);
    assertEquals(5, foo.a);
  }

  private static class Foo {
    int a = 10;
  }

  private static class FooTypeAdapter implements JsonSerializer<Foo>, JsonDeserializer<Foo> {
    public JsonElement serialize(Foo src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject json = context.serializeDefault(src, typeOfSrc).getAsJsonObject();
      json.addProperty("secret-key", "abracadabra");
      return json;
    }

    public Foo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      if (!"abracadabra".equals(json.getAsJsonObject().get("secret-key").getAsString())) {
        throw new IllegalArgumentException("invalid key");
      }
      return context.deserializeDefault(json, typeOfT);
    }
  }
}
