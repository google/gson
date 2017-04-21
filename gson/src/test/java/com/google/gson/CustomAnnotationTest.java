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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

/**
 * @author Floyd Wan
 */
public class CustomAnnotationTest extends TestCase {

  /**
   * Most modern browsers fail to give right output on this:
   * <pre>var i = 123456789123456789; alert(i);</pre>
   * Attach this to a long/Long field to make it browser-friendly.
   * Or, to a class so that all long/Long fields get done.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.TYPE})
  private @interface BrowserFriendly {}

  private static class BeingBrowserFriendly implements FieldAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson context, Annotation annotation, Field field) {
      Class<?> fieldType = field.getType();
      if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
        @SuppressWarnings("unchecked")
        TypeAdapter<T> longToString = (TypeAdapter<T>) new TypeAdapter<Long>(){
          @Override
          public void write(JsonWriter out, Long value) throws IOException {
            if (null == value) {
              out.nullValue();
            } else {
              out.value(String.valueOf(value));
            }
          }

          @Override
          public Long read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException();
          }
        };
        return longToString;
      }
      return null;
    }
  }

  private static class EntityOne {
    @BrowserFriendly
    long id1;

    @BrowserFriendly
    Long id2;

    Long id3;
  }

  @BrowserFriendly
  private static class EntityTwo {
    Long id1, id2;
  }

  private static final long LARGE_NUM = 123456789123456789L;

  private Gson gson;

  @Override
  public void setUp() {
    GsonBuilder gb = new GsonBuilder();
    gb.registerFieldAdapterFactory(BrowserFriendly.class, new BeingBrowserFriendly());
    gson = gb.create();
  }

  public void testCustomAnnotationOnField() {
    EntityOne e1 = new EntityOne();
    e1.id1 = e1.id2 = e1.id3 = LARGE_NUM;
    String json = gson.toJson(e1);
    assertTrue(json.contains("\"id1\":" + quoted(LARGE_NUM)));
    assertTrue(json.contains("\"id2\":" + quoted(LARGE_NUM)));
    assertTrue(json.contains("\"id3\":" + LARGE_NUM));
  }

  public void testCustomAnnotationOnClass() {
    EntityTwo e2 = new EntityTwo();
    e2.id1 = e2.id2 = LARGE_NUM;
    String json = gson.toJson(e2);
    assertTrue(json.contains("\"id1\":" + quoted(LARGE_NUM)));
    assertTrue(json.contains("\"id2\":" + quoted(LARGE_NUM)));
  }

  private String quoted(long number) {
    return "\"" + number + "\"";
  }
}
