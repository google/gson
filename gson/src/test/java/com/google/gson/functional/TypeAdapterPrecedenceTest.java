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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import junit.framework.TestCase;

public final class TypeAdapterPrecedenceTest extends TestCase {
  private static final JsonSerializer<Foo> FOO_SERIALIZER = new JsonSerializer<Foo>() {
    public JsonElement serialize(Foo src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.name + " (via FOO_SERIALIZER)");
    }
  };

  private static final JsonDeserializer<Foo> FOO_DESERIALIZER = new JsonDeserializer<Foo>() {
    public Foo deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return new Foo(json.getAsString() + " (via FOO_DESERIALIZER)");
    }
  };

  private static final TypeAdapter<Foo> FOO_TYPE_ADAPTER = new TypeAdapter<Foo>() {
    @Override public Foo read(JsonReader reader) throws IOException {
      return new Foo(reader.nextString() + " (via FOO_TYPE_ADAPTER)");
    }
    @Override public void write(JsonWriter writer, Foo value) throws IOException {
      writer.value(value.name + " (via FOO_TYPE_ADAPTER)");
    }
  };

  public void testSerializeNonstreamingTypeAdapterFollowedByStreamingTypeAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, FOO_SERIALIZER)
        .typeAdapter(Foo.class, FOO_TYPE_ADAPTER)
        .create();
    assertEquals("\"foo (via FOO_SERIALIZER)\"", gson.toJson(new Foo("foo")));
  }

  public void testSerializeStreamingTypeAdapterFollowedByNonstreamingTypeAdapter() {
    Gson gson = new GsonBuilder()
        .typeAdapter(Foo.class, FOO_TYPE_ADAPTER)
        .registerTypeAdapter(Foo.class, FOO_SERIALIZER)
        .create();
    assertEquals("\"foo (via FOO_TYPE_ADAPTER)\"", gson.toJson(new Foo("foo")));
  }

  public void testDeserializeNonstreamingTypeAdapterFollowedByStreamingTypeAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, FOO_DESERIALIZER)
        .typeAdapter(Foo.class, FOO_TYPE_ADAPTER)
        .create();
    assertEquals("foo (via FOO_DESERIALIZER)", gson.fromJson("foo", Foo.class).name);
  }

  public void testDeserializeStreamingTypeAdapterFollowedByNonstreamingTypeAdapter() {
    Gson gson = new GsonBuilder()
        .typeAdapter(Foo.class, FOO_TYPE_ADAPTER)
        .registerTypeAdapter(Foo.class, FOO_DESERIALIZER)
        .create();
    assertEquals("foo (via FOO_TYPE_ADAPTER)", gson.fromJson("foo", Foo.class).name);
  }

  private static class Foo {
    private final String name;
    private Foo(String name) {
      this.name = name;
    }
  }
}
