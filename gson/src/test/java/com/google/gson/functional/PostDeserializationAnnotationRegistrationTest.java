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

package com.google.gson.functional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import junit.framework.TestCase;

public class PostDeserializationAnnotationRegistrationTest extends TestCase {

  public void testRegisterPostConstructAnnotation() {
    Gson gson = new GsonBuilder()
        .registerPostDeserializationAnnotation(PostDeserialize.class)
        .create();
    Foo foo = gson.fromJson("{'a':'1','s':'abc'}", Foo.class);
    assertEquals(2, foo.a); // post construct annotation sets a to 2
    try {
      gson.fromJson("{}", Foo.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

  static final class Foo {
    int a;
    String s;
    @PostDeserialize void reviseA() {
      a = 2;
      if (s == null) throw new JsonSyntaxException("s must not be null");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public @interface PostDeserialize {

  }
}
