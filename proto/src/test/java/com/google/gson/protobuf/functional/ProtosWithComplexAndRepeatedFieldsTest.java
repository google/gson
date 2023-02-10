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
import static org.junit.Assert.assertTrue;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.protobuf.ProtoTypeAdapter;
import com.google.gson.protobuf.ProtoTypeAdapter.EnumSerialization;
import com.google.gson.protobuf.generated.Bag.ProtoWithDifferentCaseFormat;
import com.google.gson.protobuf.generated.Bag.ProtoWithRepeatedFields;
import com.google.gson.protobuf.generated.Bag.SimpleProto;
import com.google.protobuf.GeneratedMessageV3;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for protocol buffers using complex and repeated fields
 *
 * @author Inderjeet Singh
 */
public class ProtosWithComplexAndRepeatedFieldsTest {
  private Gson gson;
  private Gson upperCamelGson;

  @Before
  public void setUp() throws Exception {
    gson =
        new GsonBuilder()
            .registerTypeHierarchyAdapter(GeneratedMessageV3.class,
                ProtoTypeAdapter.newBuilder()
                    .setEnumSerialization(EnumSerialization.NUMBER)
                    .build())
            .create();
    upperCamelGson =
        new GsonBuilder()
            .registerTypeHierarchyAdapter(
                GeneratedMessageV3.class, ProtoTypeAdapter.newBuilder()
                    .setFieldNameSerializationFormat(
                        CaseFormat.LOWER_UNDERSCORE, CaseFormat.UPPER_CAMEL)
                    .build())
            .create();
  }

  @Test
  public void testSerializeRepeatedFields() {
    ProtoWithRepeatedFields proto = ProtoWithRepeatedFields.newBuilder()
      .addNumbers(2)
      .addNumbers(3)
      .addSimples(SimpleProto.newBuilder().setMsg("foo").build())
      .addSimples(SimpleProto.newBuilder().setCount(3).build())
      .build();
    String json = gson.toJson(proto);
    assertTrue(json.contains("[2,3]"));
    assertTrue(json.contains("foo"));
    assertTrue(json.contains("count"));
  }

  @Test
  public void testDeserializeRepeatedFieldsProto() {
    String json = "{numbers:[4,6],simples:[{msg:'bar'},{count:7}]}";
    ProtoWithRepeatedFields proto =
      gson.fromJson(json, ProtoWithRepeatedFields.class);
    assertEquals(4, proto.getNumbers(0));
    assertEquals(6, proto.getNumbers(1));
    assertEquals("bar", proto.getSimples(0).getMsg());
    assertEquals(7, proto.getSimples(1).getCount());
  }

  @Test
  public void testSerializeDifferentCaseFormat() {
    final ProtoWithDifferentCaseFormat proto =
      ProtoWithDifferentCaseFormat.newBuilder()
        .setAnotherField("foo")
        .addNameThatTestsCaseFormat("bar")
        .build();
    final JsonObject json = upperCamelGson.toJsonTree(proto).getAsJsonObject();
    assertEquals("foo", json.get("AnotherField").getAsString());
    assertEquals("bar", json.get("NameThatTestsCaseFormat").getAsJsonArray().get(0).getAsString());
  }

  @Test
  public void testDeserializeDifferentCaseFormat() {
    final String json = "{NameThatTestsCaseFormat:['bar'],AnotherField:'foo'}";
    ProtoWithDifferentCaseFormat proto =
      upperCamelGson.fromJson(json, ProtoWithDifferentCaseFormat.class);
    assertEquals("foo", proto.getAnotherField());
    assertEquals("bar", proto.getNameThatTestsCaseFormat(0));
  }
}
