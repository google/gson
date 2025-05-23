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

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.protobuf.ProtoTypeAdapter;
import com.google.gson.protobuf.ProtoTypeAdapter.EnumSerialization;
import com.google.gson.protobuf.generated.Bag.ProtoWithDifferentCaseFormat;
import com.google.gson.protobuf.generated.Bag.ProtoWithRepeatedFields;
import com.google.gson.protobuf.generated.Bag.SimpleProto;
import com.google.protobuf.GeneratedMessage;
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
            .registerTypeHierarchyAdapter(
                GeneratedMessage.class,
                ProtoTypeAdapter.newBuilder()
                    .setEnumSerialization(EnumSerialization.NUMBER)
                    .build())
            .create();
    upperCamelGson =
        new GsonBuilder()
            .registerTypeHierarchyAdapter(
                GeneratedMessage.class,
                ProtoTypeAdapter.newBuilder()
                    .setFieldNameSerializationFormat(
                        CaseFormat.LOWER_UNDERSCORE, CaseFormat.UPPER_CAMEL)
                    .build())
            .create();
  }

  @Test
  public void testSerializeRepeatedFields() {
    ProtoWithRepeatedFields proto =
        ProtoWithRepeatedFields.newBuilder()
            .addNumbers(2)
            .addNumbers(3)
            .addSimples(SimpleProto.newBuilder().setMsg("foo").build())
            .addSimples(SimpleProto.newBuilder().setCount(3).build())
            .build();
    String json = gson.toJson(proto);
    assertThat(json).isEqualTo("{\"numbers\":[2,3],\"simples\":[{\"msg\":\"foo\"},{\"count\":3}]}");
  }

  @Test
  public void testDeserializeRepeatedFieldsProto() {
    String json = "{numbers:[4,6],simples:[{msg:'bar'},{count:7}]}";
    ProtoWithRepeatedFields proto = gson.fromJson(json, ProtoWithRepeatedFields.class);
    assertThat(proto.getNumbers(0)).isEqualTo(4);
    assertThat(proto.getNumbers(1)).isEqualTo(6);
    assertThat(proto.getSimples(0).getMsg()).isEqualTo("bar");
    assertThat(proto.getSimples(1).getCount()).isEqualTo(7);
  }

  @Test
  public void testSerializeDifferentCaseFormat() {
    ProtoWithDifferentCaseFormat proto =
        ProtoWithDifferentCaseFormat.newBuilder()
            .setAnotherField("foo")
            .addNameThatTestsCaseFormat("bar")
            .build();
    JsonObject json = upperCamelGson.toJsonTree(proto).getAsJsonObject();
    assertThat(json.get("AnotherField").getAsString()).isEqualTo("foo");
    assertThat(json.get("NameThatTestsCaseFormat").getAsJsonArray().get(0).getAsString())
        .isEqualTo("bar");
  }

  @Test
  public void testDeserializeDifferentCaseFormat() {
    String json = "{NameThatTestsCaseFormat:['bar'],AnotherField:'foo'}";
    ProtoWithDifferentCaseFormat proto =
        upperCamelGson.fromJson(json, ProtoWithDifferentCaseFormat.class);
    assertThat(proto.getAnotherField()).isEqualTo("foo");
    assertThat(proto.getNameThatTestsCaseFormat(0)).isEqualTo("bar");
  }
}
