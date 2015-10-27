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

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.protobuf.ProtoTypeAdapter;
import com.google.gson.protobuf.ProtoTypeAdapter.EnumSerialization;
import com.google.gson.protobuf.generated.Bag.ProtoWithRepeatedFields;
import com.google.gson.protobuf.generated.Bag.SimpleProto;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;

import junit.framework.TestCase;

/**
 * Functional tests for protocol buffers using complex and repeated fields
 *
 * @author Inderjeet Singh
 */
public class ProtosWithComplexAndRepeatedFieldsTest extends TestCase {
  private Gson gson;

  public static final ByteString PAYLOAD_DATA1 =
      ByteString.copyFrom("The first", Charsets.UTF_8);
  public static final ByteString PAYLOAD_DATA2 =
      ByteString.copyFrom("Another", Charsets.UTF_8);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson =
        new GsonBuilder()
            .registerTypeHierarchyAdapter(GeneratedMessage.class,
                ProtoTypeAdapter.newBuilder()
                    .setEnumSerialization(EnumSerialization.NUMBER)
                    .build())
            .create();
  }

  public void testSerializeRepeatedFields() {
    ProtoWithRepeatedFields proto = ProtoWithRepeatedFields.newBuilder()
      .addNumbers(2)
      .addNumbers(3)
      .addSimples(SimpleProto.newBuilder().setMsg("foo").build())
      .addSimples(SimpleProto.newBuilder().setCount(3).build())
      .addPayloads(PAYLOAD_DATA1)
      .addPayloads(PAYLOAD_DATA2)
      .build();
    String json = gson.toJson(proto);
    assertTrue(json.contains("[2,3]"));
    assertTrue(json.contains("foo"));
    assertTrue(json.contains("count"));
    assertTrue(json.contains("\"payloads\":[\"VGhlIGZpcnN0\",\"QW5vdGhlcg\\u003d\\u003d\"]"));
  }

  public void testDeserializeRepeatedFieldsProto() {
    String json = "{numbers:[4,6],simples:[{msg:'bar'},{count:7}],payloads:[\"VGhlIGZpcnN0\",\"QW5vdGhlcg\\u003d\\u003d\"]}";
    ProtoWithRepeatedFields proto =
      gson.fromJson(json, ProtoWithRepeatedFields.class);
    assertEquals(4, proto.getNumbers(0));
    assertEquals(6, proto.getNumbers(1));
    assertEquals("bar", proto.getSimples(0).getMsg());
    assertEquals(7, proto.getSimples(1).getCount());
    assertEquals(2, proto.getPayloadsCount());
    assertEquals(PAYLOAD_DATA1, proto.getPayloads(0));
    assertEquals(PAYLOAD_DATA2, proto.getPayloads(1));
  }
}
