/*
 * Copyright (C) 2008 Google Inc.
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
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  private static @interface BrowserFriendly {}

  private static class BeingBrowserFriendly implements FieldAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Annotation annotation, Field field) {
      Class<?> fieldType = field.getType();
      if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
        if (field.isAnnotationPresent(BrowserFriendly.class)) {
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
      }
      return null;
    }
  }

  private static class Person {
    @BrowserFriendly
    long id1;

    @BrowserFriendly
    Long id2;

    Long id3;
  }

  public void testBrowserFriendlyInSerialization() {
    GsonBuilder gb = new GsonBuilder();
    gb.registerFieldAdapterFactory(BrowserFriendly.class, new BeingBrowserFriendly());
    Gson gson = gb.create();

    Person p = new Person();
    long largeNumber = 123456789123456789L;
    p.id1 = p.id2 = p.id3 = largeNumber;
    String json = gson.toJson(p);
    assertTrue(json.contains("\"id1\":" + quoted(largeNumber)));
    assertTrue(json.contains("\"id2\":" + quoted(largeNumber)));
    assertTrue(json.contains("\"id3\":" + largeNumber));
  }

  private String quoted(long number) {
    return "\"" + number + "\"";
  }
}
