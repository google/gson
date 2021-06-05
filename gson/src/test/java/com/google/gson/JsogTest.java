/*
 * Copyright (C) 2016 The Gson Authors
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

import com.google.gson.annotations.JsogEnabled;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for JSOG
 *
 * This test was partially based on the test for jsog-jackson
 *
 * @author Paulo Costa
 */
public final class JsogTest extends TestCase {
  @JsogEnabled
  public static class Outer {
    public String foo;
    public List<Inner> inner;
  }

  @JsogEnabled
  public static class Inner {
    public String bar;
    public Outer outer;
  }


  /** Expected output */
  private static final String JSOGIFIED = "{\"@id\":\"1\",\"foo\":\"foo\",\"inner\":[{\"@id\":\"2\",\"bar\":\"bar1\",\"outer\":{\"@ref\":\"1\"}},{\"@id\":\"3\",\"bar\":\"bar2\",\"outer\":{\"@ref\":\"1\"}},{\"@ref\":\"2\"}]}";
  private static final String JSOGIFIED_WITH_PREFIX = "{\"@id\":\"JsogTest.Outer-1\",\"foo\":\"foo\",\"inner\":[{\"@id\":\"JsogTest.Inner-1\",\"bar\":\"bar1\",\"outer\":{\"@ref\":\"JsogTest.Outer-1\"}},{\"@id\":\"JsogTest.Inner-2\",\"bar\":\"bar2\",\"outer\":{\"@ref\":\"JsogTest.Outer-1\"}},{\"@ref\":\"JsogTest.Inner-1\"}]}";


  public void testSerialization() {
    Outer outer = new Outer();
    outer.foo = "foo";

    Inner inner1 = new Inner();
    inner1.bar = "bar1";
    inner1.outer = outer;

    Inner inner2 = new Inner();
    inner2.bar = "bar2";
    inner2.outer = outer;

    outer.inner = Arrays.asList(inner1, inner2, inner1);

    String json = new Gson().toJson(outer);
    Assert.assertEquals(
            JSOGIFIED,
            json
    );
  }

  public void testSerializationWithTypePrefix() {
    Outer outer = new Outer();
    outer.foo = "foo";

    Inner inner1 = new Inner();
    inner1.bar = "bar1";
    inner1.outer = outer;

    Inner inner2 = new Inner();
    inner2.bar = "bar2";
    inner2.outer = outer;

    outer.inner = Arrays.asList(inner1, inner2, inner1);

    Gson gson = new GsonBuilder().setJsogPolicy(JsogPolicy.DEFAULT.withTypePrefix()).create();
    Assert.assertEquals(JSOGIFIED_WITH_PREFIX, gson.toJson(outer)
    );
  }


  public void testDeserialization() {
    Outer outer = new Gson().fromJson(JSOGIFIED, Outer.class);
    assert outer == outer.inner.get(0).outer;
    assert outer == outer.inner.get(1).outer;
    assert outer == outer.inner.get(2).outer;
    assert outer.inner.get(0) == outer.inner.get(2);
  }


  public void testDeserializationWithTypePrefix() {
    Outer outer = new Gson().fromJson(JSOGIFIED_WITH_PREFIX, Outer.class);
    assert outer == outer.inner.get(0).outer;
    assert outer == outer.inner.get(1).outer;
    assert outer == outer.inner.get(2).outer;
    assert outer.inner.get(0) == outer.inner.get(2);
  }
}
