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

import static com.google.gson.UnknownFieldHandlingPolicy.IGNORE;
import static com.google.gson.UnknownFieldHandlingPolicy.THROW_EXCEPTION;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import junit.framework.Assert;
import junit.framework.TestCase;

public final class UnknownFieldHandlingTest extends TestCase {

  private static final String JSON = "{'one':1,'two':'A','three':null}".replace('\'', '\"');

  @SuppressWarnings("unused") // fields are used reflectively
  private static class ClassWithTwoFields {
    int one;
    String two;
  }

  public void testIgnore() {
    Gson gson = new GsonBuilder().setUnknownFieldHandlingStrategy(IGNORE).create();
    gson.fromJson(JSON, ClassWithTwoFields.class);
  }

  public void testThrowException() {
    Gson gson = new GsonBuilder().setUnknownFieldHandlingStrategy(THROW_EXCEPTION).create();
    try {
      gson.fromJson(JSON, ClassWithTwoFields.class);
      Assert.fail("A JsonParseException was expected");
    } catch (JsonParseException e) {
      String msg = "Unrecognized field \"three\" (Class com.google.gson.functional.UnknownFieldHandlingTest$ClassWithTwoFields)";
      assertEquals(msg, e.getMessage());
    }
  }
}
