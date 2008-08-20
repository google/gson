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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests that the default serializers are registered properly.
 *
 * @author Joel Leitch
 */
public class DefaultCustomSerializersTest extends TestCase {

  public void testSetSerialization() throws Exception {
    Gson gson = new Gson();
    HashSet<String> s = new HashSet<String>();
    s.add("blah");
    String json = gson.toJson(s);
    assertEquals("[\"blah\"]", json);

    json = gson.toJson(s, Set.class);
    assertEquals("[\"blah\"]", json);
  }

  public void testDefaultDateSerialization() throws Exception {
    Gson gson = new Gson();
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + DateFormat.getDateInstance().format(now) + "\"", json);
  }

  public void testDefaultDateSerializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + DateFormat.getDateInstance().format(now) + "\"", json);
  }

  public void testDateSerializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    DateFormat formatter = new SimpleDateFormat(pattern);
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.LONG).setDateFormat(pattern).create();
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + formatter.format(now) + "\"", json);
  }
}
