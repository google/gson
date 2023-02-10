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

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import java.util.Currency;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional test for Json serialization and deserialization for classes in java.util
 */
public class JavaUtilTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testCurrency() {
    CurrencyHolder target = gson.fromJson("{'value':'USD'}", CurrencyHolder.class);
    assertThat(target.value.getCurrencyCode()).isEqualTo("USD");
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("{\"value\":\"USD\"}");

    // null handling
    target = gson.fromJson("{'value':null}", CurrencyHolder.class);
    assertThat(target.value).isNull();
    assertThat(gson.toJson(target)).isEqualTo("{}");
  }

  private static class CurrencyHolder {
    Currency value;
  }

  @Test
  public void testProperties() {
    Properties props = gson.fromJson("{'a':'v1','b':'v2'}", Properties.class);
    assertThat(props.getProperty("a")).isEqualTo("v1");
    assertThat(props.getProperty("b")).isEqualTo("v2");
    String json = gson.toJson(props);
    assertThat(json).contains("\"a\":\"v1\"");
    assertThat(json).contains("\"b\":\"v2\"");
  }
}
