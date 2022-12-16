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

import java.net.InetAddress;

import junit.framework.TestCase;

/**
 * Unit tests for the default serializer/deserializer for the {@code InetAddress} type.
 * 
 * @author Joel Leitch
 */
public class DefaultInetAddressTypeAdapterTest extends TestCase {
  private Gson gson;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }
  
  public void testInetAddressSerializationAndDeserialization() throws Exception {
    InetAddress address = InetAddress.getByName("8.8.8.8");
    String jsonAddress = gson.toJson(address);
    assertEquals("\"8.8.8.8\"", jsonAddress);
    
    InetAddress value = gson.fromJson(jsonAddress, InetAddress.class);
    assertEquals(value, address);
  } 
}
