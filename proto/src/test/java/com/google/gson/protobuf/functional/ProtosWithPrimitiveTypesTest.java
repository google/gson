/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.protobuf.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.protobuf.ProtoTypeAdapter;
import com.google.gson.protobuf.ProtoTypeAdapter.EnumSerialization;
import com.google.gson.protobuf.generated.Bag.SimpleProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.GeneratedMessageV3;
import org.junit.Before;
import org.junit.Test;

public class ProtosWithPrimitiveTypesTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new GsonBuilder().registerTypeHierarchyAdapter(
      GeneratedMessageV3.class, ProtoTypeAdapter.newBuilder()
          .setEnumSerialization(EnumSerialization.NUMBER)
          .build())
      .create();
  }

  @Test
  public void testSerializeEmptyProto() {
    SimpleProto proto = SimpleProto.newBuilder().build();
    String json = gson.toJson(proto);
    assertEquals("{}", json);
  }

  @Test
  public void testDeserializeEmptyProto() {
    SimpleProto proto = gson.fromJson("{}", SimpleProto.class);
    assertFalse(proto.hasCount());
    assertFalse(proto.hasMsg());
  }

  @Test
  public void testSerializeProto() {
    Descriptor descriptor = SimpleProto.getDescriptor();
    SimpleProto proto = SimpleProto.newBuilder()
      .setCount(3)
      .setMsg("foo")
      .build();
    String json = gson.toJson(proto);
    assertTrue(json.contains("\"msg\":\"foo\""));
    assertTrue(json.contains("\"count\":3"));
  }

  @Test
  public void testDeserializeProto() {
    SimpleProto proto = gson.fromJson("{msg:'foo',count:3}", SimpleProto.class);
    assertEquals("foo", proto.getMsg());
    assertEquals(3, proto.getCount());
  }

  @Test
  public void testDeserializeWithExplicitNullValue() {
    SimpleProto proto = gson.fromJson("{msg:'foo',count:null}", SimpleProto.class);
    assertEquals("foo", proto.getMsg());
    assertEquals(0, proto.getCount());
  }

}
