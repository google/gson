/*
 * Copyright (C) 2017 Gson Authors
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

package com.google.gson;

import javax.persistence.Column;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import junit.framework.TestCase;

/**
 * @author Floyd Wan
 */
public class JpaAnnotationTest extends TestCase {

  private static class Ignored implements FieldAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Annotation annotation, Field field) {
      return new TypeAdapter<T>() {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
          out.nullValue();
        }

        @Override
        public T read(JsonReader in) throws IOException {
          return null;
        }
      };
    }
  }

  private static class StringColumnLimited implements FieldAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Annotation annotation, Field field) {
      if (!String.class.equals(field.getType())) {
        return null;
      }
      Column column = (Column) annotation;
      final int length = column.length();
      if (length < 1) {
        return null;
      }
      @SuppressWarnings("unchecked")
      TypeAdapter<T> adapter = (TypeAdapter<T>) new TypeAdapter<String>() {
        @Override
        public void write(JsonWriter out, String value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          if (value.length() > length) {
            out.value(value.substring(0, length));
          } else {
            out.value(value);
          }
        }

        @Override
        public String read(JsonReader in) throws IOException {
          JsonToken peek = in.peek();
          if (peek == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String nextString = in.nextString();
          if (nextString.length() > length) {
            return nextString.substring(0, length);
          } else {
            return nextString;
          }
        }
      };
      return adapter;
    }
  }

  @javax.persistence.Entity
  private static class Entity {
    long id;

    @Column(length = 10) String desc;

    @Version int ccVersion;

    @Transient Entity parent;
  }

  private Gson gson;

  public void setUp() {
    GsonBuilder gb = new GsonBuilder();
    gb.registerFieldAdapterFactory(Transient.class, new Ignored());
    gb.registerFieldAdapterFactory(Version.class, new Ignored());
    gb.registerFieldAdapterFactory(Column.class, new StringColumnLimited());
    gson = gb.create();
  }

  public void testIgnoringTransient() {
    Entity parent = new Entity(), entity = new Entity();
    parent.id = 1L;
    entity.parent = parent;
    entity.id = 100L;
    entity.ccVersion = 1;

    String json = gson.toJson(entity);
    assertFalse(json.contains("parent"));
    assertFalse(json.contains("ccVersion"));
  }

  public void testColumnLimitedSerialization() {
    Entity entity = new Entity();
    entity.desc = "01234567890"; // length of 11

    String json = gson.toJson(entity);
    assertTrue(json.contains("\"desc\":\"0123456789\"")); // cut to length of 10
  }

  public void testColumnLimitedDeserialization() {
    Entity entity = gson.fromJson("{\"desc\":\"01234567890\"}", Entity.class); // length of 11
    assertEquals("0123456789", entity.desc); // cut to length of 10
  }
}
