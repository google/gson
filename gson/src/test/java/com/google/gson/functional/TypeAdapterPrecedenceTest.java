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

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import org.junit.Test;

public final class TypeAdapterPrecedenceTest {
  @Test
  public void testNonstreamingFollowedByNonstreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newSerializer("serializer 1"))
        .registerTypeAdapter(Foo.class, newSerializer("serializer 2"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer 1"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer 2"))
        .create();
    assertThat(gson.toJson(new Foo("foo"))).isEqualTo("\"foo via serializer 2\"");
    assertThat(gson.fromJson("foo", Foo.class).name).isEqualTo("foo via deserializer 2");
  }

  @Test
  public void testStreamingFollowedByStreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter 1"))
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter 2"))
        .create();
    assertThat(gson.toJson(new Foo("foo"))).isEqualTo("\"foo via type adapter 2\"");
    assertThat(gson.fromJson("foo", Foo.class).name).isEqualTo("foo via type adapter 2");
  }

  @Test
  public void testSerializeNonstreamingTypeAdapterFollowedByStreamingTypeAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer"))
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter"))
        .create();
    assertThat(gson.toJson(new Foo("foo"))).isEqualTo("\"foo via type adapter\"");
    assertThat(gson.fromJson("foo", Foo.class).name).isEqualTo("foo via type adapter");
  }

  @Test
  public void testStreamingFollowedByNonstreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter"))
        .registerTypeAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer"))
        .create();
    assertThat(gson.toJson(new Foo("foo"))).isEqualTo("\"foo via serializer\"");
    assertThat( gson.fromJson("foo", Foo.class).name).isEqualTo("foo via deserializer");
  }

  @Test
  public void testStreamingHierarchicalFollowedByNonstreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Foo.class, newTypeAdapter("type adapter"))
        .registerTypeAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeAdapter(Foo.class, newDeserializer("deserializer"))
        .create();
    assertThat(gson.toJson(new Foo("foo"))).isEqualTo("\"foo via serializer\"");
    assertThat(gson.fromJson("foo", Foo.class).name).isEqualTo("foo via deserializer");
  }

  @Test
  public void testStreamingFollowedByNonstreamingHierarchical() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Foo.class, newTypeAdapter("type adapter"))
        .registerTypeHierarchyAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeHierarchyAdapter(Foo.class, newDeserializer("deserializer"))
        .create();
    assertThat(gson.toJson(new Foo("foo"))).isEqualTo("\"foo via type adapter\"");
    assertThat(gson.fromJson("foo", Foo.class).name).isEqualTo("foo via type adapter");
  }

  @Test
  public void testStreamingHierarchicalFollowedByNonstreamingHierarchical() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Foo.class, newSerializer("serializer"))
        .registerTypeHierarchyAdapter(Foo.class, newDeserializer("deserializer"))
        .registerTypeHierarchyAdapter(Foo.class, newTypeAdapter("type adapter"))
        .create();
    assertThat(gson.toJson(new Foo("foo"))).isEqualTo("\"foo via type adapter\"");
    assertThat(gson.fromJson("foo", Foo.class).name).isEqualTo("foo via type adapter");
  }

  @Test
  public void testNonstreamingHierarchicalFollowedByNonstreaming() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Foo.class, newSerializer("hierarchical"))
        .registerTypeHierarchyAdapter(Foo.class, newDeserializer("hierarchical"))
        .registerTypeAdapter(Foo.class, newSerializer("non hierarchical"))
        .registerTypeAdapter(Foo.class, newDeserializer("non hierarchical"))
        .create();
    assertThat(gson.toJson(new Foo("foo"))).isEqualTo("\"foo via non hierarchical\"");
    assertThat(gson.fromJson("foo", Foo.class).name).isEqualTo("foo via non hierarchical");
  }

  private static class Foo {
    final String name;
    private Foo(String name) {
      this.name = name;
    }
  }

  private JsonSerializer<Foo> newSerializer(final String name) {
    return new JsonSerializer<Foo>() {
      @Override
      public JsonElement serialize(Foo src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name + " via " + name);
      }
    };
  }

  private JsonDeserializer<Foo> newDeserializer(final String name) {
    return new JsonDeserializer<Foo>() {
      @Override
      public Foo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new Foo(json.getAsString() + " via " + name);
      }
    };
  }

  private TypeAdapter<Foo> newTypeAdapter(final String name) {
    return new TypeAdapter<Foo>() {
      @Override public Foo read(JsonReader in) throws IOException {
        return new Foo(in.nextString() + " via " + name);
      }
      @Override public void write(JsonWriter out, Foo value) throws IOException {
        out.value(value.name + " via " + name);
      }
    };
  }
}
