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
 * Simple unit tests for the {@link JsonDeserializerExceptionWrapper} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonDeserializerExceptionWrapperTest extends TestCase {

  private static final String DATE_STRING =
      DateFormat.getDateInstance(DateFormat.LONG).format(new Date());
  private static final JsonPrimitive PRIMITIVE_ELEMENT = new JsonPrimitive(DATE_STRING);

  public void testRethrowJsonParseException() throws Exception {
    String errorMsg = "please rethrow me";
    JsonDeserializerExceptionWrapper<String> wrappedJsonSerializer =
        new JsonDeserializerExceptionWrapper<String>(
            new ExceptionJsonDeserializer(new JsonParseException(errorMsg)));

    try {
      wrappedJsonSerializer.deserialize(PRIMITIVE_ELEMENT, String.class, null);
      fail("JsonParseException should have been thrown");
    } catch (JsonParseException expected) {
      assertNull(expected.getCause());
      assertEquals(errorMsg, expected.getMessage());
    }
  }

  public void testWrappedExceptionPropagation() throws Exception {
    IllegalArgumentException exceptionToThrow = new IllegalArgumentException();
    JsonDeserializerExceptionWrapper<String> wrappedJsonSerializer =
        new JsonDeserializerExceptionWrapper<String>(
            new ExceptionJsonDeserializer(exceptionToThrow));

    try {
      wrappedJsonSerializer.deserialize(PRIMITIVE_ELEMENT, String.class, null);
      fail("JsonParseException should have been thrown");
    } catch (JsonParseException expected) {
      assertEquals(exceptionToThrow, expected.getCause());
    }
  }

  public void testProperSerialization() throws Exception {
    DefaultDateTypeAdapter dateSerializer = new DefaultDateTypeAdapter(DateFormat.LONG);
    JsonDeserializerExceptionWrapper<Date> wrappedJsonSerializer =
        new JsonDeserializerExceptionWrapper<Date>(dateSerializer);

    Date expected = dateSerializer.deserialize(PRIMITIVE_ELEMENT, Date.class, null);
    Date actual = wrappedJsonSerializer.deserialize(PRIMITIVE_ELEMENT, Date.class, null);
    assertEquals(expected, actual);
  }

  private static class ExceptionJsonDeserializer implements JsonDeserializer<String> {
    private final RuntimeException exceptionToThrow;

    public ExceptionJsonDeserializer(RuntimeException exceptionToThrow) {
      this.exceptionToThrow = exceptionToThrow;
    }

    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      throw exceptionToThrow;
    }
  }
}