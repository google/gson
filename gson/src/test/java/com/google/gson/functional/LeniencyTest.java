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
package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import junit.framework.TestCase;

import static java.util.Collections.singletonList;

/**
 * Functional tests for leniency option.
 */
public class LeniencyTest extends TestCase {

  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder().setLenient().create();
  }

  public void testLenientFromJson() {
    List<String> json = gson.fromJson(""
        + "[ # One!\n"
        + "  'Hi' #Element!\n"
        + "] # Array!", new TypeToken<List<String>>() {}.getType());
    assertEquals(singletonList("Hi"), json);
  }
}
