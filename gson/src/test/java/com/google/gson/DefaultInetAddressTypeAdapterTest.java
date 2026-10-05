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
import static org.junit.Assert.assertThrows;

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
    @SuppressWarnings("AddressSelection") // we really do want this method
    InetAddress address = InetAddress.getByName("8.8.8.8");
    String jsonAddress = gson.toJson(address);
    assertThat(jsonAddress).isEqualTo("\"8.8.8.8\"");

    InetAddress value = gson.fromJson(jsonAddress, InetAddress.class);
    assertThat(address).isEqualTo(value);
  }

  @Test
  public void testInetAddressSerializationAndDeserializationIpv6() throws Exception {
    @SuppressWarnings("AddressSelection") // we really do want this method
    InetAddress address = InetAddress.getByName("::1"); // IPv6 loopback address
    String jsonAddress = gson.toJson(address);
    InetAddress actual = gson.fromJson(jsonAddress, InetAddress.class);
    assertThat(actual).isEqualTo(address);
  }

  @Test
  public void testInetAddressDeserializeNonIpAddress() {
    String jsonAddress = "\"localhost\"";
    JsonSyntaxException e =
        assertThrows(
            JsonSyntaxException.class, () -> gson.fromJson(jsonAddress, InetAddress.class));
    assertThat(e)
        .hasMessageThat()
        .startsWith("Failed parsing 'localhost' as InetAddress; at path $");
  }

  @Test
  public void testInetAddressDeserializeIpLikeNonIpAddress() {
    // These look like IP addresses but are not IP literals that InetAddress.getByName parses.
    for (String address :
        new String[] {"256.1.1.1", "300.1.1.1", "1.2.3.999", "0001.2.3.4", "zz:1", "g::1"}) {
      String jsonAddress = "\"" + address + "\"";
      JsonSyntaxException e =
          assertThrows(
              JsonSyntaxException.class, () -> gson.fromJson(jsonAddress, InetAddress.class));
      assertThat(e)
          .hasMessageThat()
          .startsWith("Failed parsing '" + address + "' as InetAddress; at path $");
    }
  }

  @Test
  public void testInetAddressDeserializeIpAddressForms() throws Exception {
    for (String address :
        new String[] {
          "0.0.0.0",
          "255.255.255.255",
          "01.2.3.4",
          "001.002.003.004",
          "::1",
          "[::1]",
          "::ffff:1.2.3.4",
          "fe80::1%1"
        }) {
      String jsonAddress = "\"" + address + "\"";
      assertThat(gson.fromJson(jsonAddress, InetAddress.class))
          .isEqualTo(InetAddress.getByName(address));
    }
  }

  @Test
  public void testInetAddressDeserializeNonIpAddressAllowed() throws Exception {
    String jsonAddress = "\"localhost\"";
    InetAddress expected = InetAddress.getByName("localhost");
    System.setProperty("gson.allowDnsInetAddress", "true");
    try {
      assertThat(gson.fromJson(jsonAddress, InetAddress.class)).isEqualTo(expected);
    } finally {
      System.clearProperty("gson.allowDnsInetAddress");
    }
  }
}
