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

import junit.framework.TestCase;

import java.text.DateFormat;
import java.util.Date;

/**
 * Unit tests for {@link GsonBuilder}.
 *
 * @author Inderjeet Singh
 */
public class GsonBuilderTest extends TestCase {

  public void testCreatingMoreThanOnce() {
    GsonBuilder builder = new GsonBuilder();
    builder.create();
    builder.create();
  }

  public void testDefaultDateSerializerExistsWithSimpleCreate() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + DateFormat.getDateInstance().format(now) + "\"", json);
  }
}
