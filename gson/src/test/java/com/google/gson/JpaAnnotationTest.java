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
import com.google.gson.stream.JsonWriter;
import junit.framework.TestCase;

/**
 * Demonstrate some meaningful use cases of JPA
 * @author Floyd Wan
 */
public class JpaAnnotationTest extends TestCase {

  private static class Ignored implements FieldAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson context, Annotation annotation, Field field) {
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

  @SuppressWarnings("unchecked")
  private static class ColumnSerializedName implements FieldAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson context, Annotation annotation, Field field) {
      final String columnName = ((Column) annotation).name();
      final TypeAdapter delegate = context.getAdapter(field.getType());
      return new TypeAdapter<T>() {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
          out.nullValue(); // to prevent IllegalStateException
          if (value != null) {
            out.name(columnName);
            delegate.write(out, value);
          }
        }

        @Override
        public T read(JsonReader in) throws IOException {
          return (T) delegate.read(in);
        }
      };
    }
  }

  @javax.persistence.Entity
  private static class Entity {
    long id;

    @Column(name = "description") String desc;

    @Version int ccVersion;

    @Transient Entity parent;
  }

  private Gson gson;

  public void setUp() {
    GsonBuilder gb = new GsonBuilder();
    gb.registerFieldAdapterFactory(Transient.class, new Ignored());
    gb.registerFieldAdapterFactory(Version.class, new Ignored());
    gb.registerFieldAdapterFactory(Column.class, new ColumnSerializedName());
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

  public void testColumnSerializedName() {
    Entity entity = new Entity();
    entity.desc = "123";

    String json = gson.toJson(entity);
    assertTrue(json.contains("\"description\":\"123\""));
  }
}
