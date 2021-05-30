/*
 * Copyright (C) 2011 Google Inc.
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

import java.net.InetAddress;

/**
 * Unit tests for the default serializer/deserializer for the {@code InetAddress} type.
 * 
 * @author Joel Leitch
 */
public class ReflectiveTypeAdapterTest extends TestCase {
  private Gson gson;
  private Gson deserializeNullsGson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
    deserializeNullsGson = new GsonBuilder().deserializeNulls(true).create();
  }
  
  public void testDeserialization() throws Exception {
    String json = "{\"name\":null,\"sex\":\"男\"}";
    TestBean testBean = gson.fromJson(json, TestBean.class);
    assertNull(testBean.name);

    String json2 = "{\"name\":null,\"sex\":\"男\"}";
    TestBean testBean2 = deserializeNullsGson.fromJson(json2, TestBean.class);
    assertEquals("name",testBean2.name);
  }

  public static class TestBean{
    private String name = "name";
    private int age;
    private String sex;
  }
}
