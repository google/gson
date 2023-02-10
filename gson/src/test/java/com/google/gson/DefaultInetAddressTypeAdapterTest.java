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

import static com.google.common.truth.Truth.assertThat;

import java.net.InetAddress;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the default serializer/deserializer for the {@code InetAddress} type.
 * 
 * @author Joel Leitch
 */
public class DefaultInetAddressTypeAdapterTest {
  private Gson gson;
  
  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }
  
  @Test
  public void testInetAddressSerializationAndDeserialization() throws Exception {
    InetAddress address = InetAddress.getByName("8.8.8.8");
    String jsonAddress = gson.toJson(address);
    assertThat(jsonAddress).isEqualTo("\"8.8.8.8\"");

    InetAddress value = gson.fromJson(jsonAddress, InetAddress.class);
    assertThat(address).isEqualTo(value);
  }
}
