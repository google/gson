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

import com.google.gson.DefaultTypeAdapters.DefaultDateTypeAdapter;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

/**
 * Simple unit tests for the {@link JsonSerializerExceptionWrapper} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonSerializerExceptionWrapperTest extends TestCase {

  public void testRethrowJsonParseException() throws Exception {
    String errorMsg = "please rethrow me";
    JsonSerializerExceptionWrapper<String> wrappedJsonSerializer =
        new JsonSerializerExceptionWrapper<String>(
            new ExceptionJsonSerializer(new JsonParseException(errorMsg)));

    try {
      wrappedJsonSerializer.serialize("blah", String.class, null);
      fail("JsonParseException should have been thrown");
    } catch (JsonParseException expected) {
      assertNull(expected.getCause());
      assertEquals(errorMsg, expected.getMessage());
    }
  }

  public void testWrappedExceptionPropagation() throws Exception {
    IllegalArgumentException exceptionToThrow = new IllegalArgumentException();
    JsonSerializerExceptionWrapper<String> wrappedJsonSerializer =
        new JsonSerializerExceptionWrapper<String>(
            new ExceptionJsonSerializer(exceptionToThrow));

    try {
      wrappedJsonSerializer.serialize("blah", String.class, null);
      fail("JsonParseException should have been thrown");
    } catch (JsonParseException expected) {
      assertEquals(exceptionToThrow, expected.getCause());
    }
  }

  public void testProperSerialization() throws Exception {
    Date now = new Date();
    DefaultDateTypeAdapter dateSerializer = new DefaultDateTypeAdapter(DateFormat.LONG);
    JsonSerializerExceptionWrapper<Date> wrappedJsonSerializer =
        new JsonSerializerExceptionWrapper<Date>(dateSerializer);

    JsonElement expected = dateSerializer.serialize(now, Date.class, null);
    JsonElement actual = wrappedJsonSerializer.serialize(now, Date.class, null);
    assertEquals(expected.getAsString(), actual.getAsString());
  }

  private static class ExceptionJsonSerializer implements JsonSerializer<String> {
    private final RuntimeException exceptionToThrow;

    public ExceptionJsonSerializer(RuntimeException exceptionToThrow) {
      this.exceptionToThrow = exceptionToThrow;
    }

    public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
      throw exceptionToThrow;
    }
  }
}
