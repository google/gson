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

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.protobuf.ProtoTypeAdapter;
import com.google.gson.protobuf.ProtoTypeAdapter.EnumSerialization;
import com.google.gson.protobuf.generated.Bag.SimpleProto;
import com.google.protobuf.GeneratedMessage;
import org.junit.Before;
import org.junit.Test;

public class ProtosWithPrimitiveTypesTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson =
        new GsonBuilder()
            .registerTypeHierarchyAdapter(
                GeneratedMessage.class,
                ProtoTypeAdapter.newBuilder()
                    .setEnumSerialization(EnumSerialization.NUMBER)
                    .build())
            .create();
  }

  @Test
  public void testSerializeEmptyProto() {
    SimpleProto proto = SimpleProto.newBuilder().build();
    String json = gson.toJson(proto);
    assertThat(json).isEqualTo("{}");
  }

  @Test
  public void testDeserializeEmptyProto() {
    SimpleProto proto = gson.fromJson("{}", SimpleProto.class);
    assertThat(proto.hasCount()).isFalse();
    assertThat(proto.hasMsg()).isFalse();
  }

  @Test
  public void testSerializeProto() {
    SimpleProto proto = SimpleProto.newBuilder().setCount(3).setMsg("foo").build();
    String json = gson.toJson(proto);
    assertThat(json).isEqualTo("{\"msg\":\"foo\",\"count\":3}");
  }

  @Test
  public void testDeserializeProto() {
    SimpleProto proto = gson.fromJson("{msg:'foo',count:3}", SimpleProto.class);
    assertThat(proto.getMsg()).isEqualTo("foo");
    assertThat(proto.getCount()).isEqualTo(3);
  }

  @Test
  public void testDeserializeWithExplicitNullValue() {
    SimpleProto proto = gson.fromJson("{msg:'foo',count:null}", SimpleProto.class);
    assertThat(proto.getMsg()).isEqualTo("foo");
    assertThat(proto.getCount()).isEqualTo(0);
  }
}
