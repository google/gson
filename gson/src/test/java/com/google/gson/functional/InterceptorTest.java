/*
 * Copyright (C) 2012 Google Inc.
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

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.internal.alpha.Intercept;
import com.google.gson.internal.alpha.JsonPostDeserializer;

/**
 * Unit tests for {@link Intercept} and {@link JsonPostDeserializer}.
 *
 * @author Inderjeet Singh
 */
public final class InterceptorTest extends TestCase {

  private Gson gson;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.gson = new Gson();
  }

  public void testPostDeserialize() {
    MyObject target = gson.fromJson("{}", MyObject.class);
    assertEquals(MyObject.DEFAULT_VALUE, target.value);
    assertEquals(MyObject.DEFAULT_MESSAGE, target.message);
  }

  @Intercept(postDeserialize = MyObjectInterceptor.class)
  private static final class MyObject {
    static final int DEFAULT_VALUE = 10;
    static final String DEFAULT_MESSAGE = "hello";

    int value = 0;
    String message = null;
  }

  private static final class MyObjectInterceptor implements JsonPostDeserializer<MyObject> {
    public void postDeserialize(MyObject o) {
      if (o.value == 0) {
        o.value = MyObject.DEFAULT_VALUE;
      }
      if (o.message == null) {
        o.message = MyObject.DEFAULT_MESSAGE;
      }
    }
  }
}