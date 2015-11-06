/*
 * Copyright (C) 2015 Google Inc.
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

import java.util.Currency;

import com.google.gson.Gson;

import junit.framework.TestCase;

/**
 * Functional test for Json serialization and deserialization for classes in java.util
 */
public class JavaUtilTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testCurrency() throws Exception {
    CurrencyHolder target = gson.fromJson("{'value':'USD'}", CurrencyHolder.class);
    assertEquals("USD", target.value.getCurrencyCode());
    String json = gson.toJson(target);
    assertEquals("{\"value\":\"USD\"}", json);

    // null handling
    target = gson.fromJson("{'value':null}", CurrencyHolder.class);
    assertNull(target.value);
    assertEquals("{}", gson.toJson(target));
  }

  private static class CurrencyHolder {
    Currency value;
  }
}
