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
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.protobuf.ProtoTypeAdapter;
import com.google.gson.protobuf.ProtoTypeAdapter.EnumSerialization;
import com.google.gson.protobuf.generated.Annotations;
import com.google.gson.protobuf.generated.Bag.OuterMessage;
import com.google.gson.protobuf.generated.Bag.ProtoWithAnnotations;
import com.google.gson.protobuf.generated.Bag.ProtoWithAnnotations.InnerMessage;
import com.google.gson.protobuf.generated.Bag.ProtoWithAnnotations.InnerMessage.Data;
import com.google.gson.protobuf.generated.Bag.ProtoWithAnnotations.InnerMessage.Type;
import com.google.protobuf.GeneratedMessageV3;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for protocol buffers using annotations for field names and enum values.
 *
 * @author Emmanuel Cron
 */
public class ProtosWithAnnotationsTest {
  private Gson gson;
  private Gson gsonWithEnumNumbers;
  private Gson gsonWithLowerHyphen;

  @Before
  public void setUp() throws Exception {
    ProtoTypeAdapter.Builder protoTypeAdapter = ProtoTypeAdapter.newBuilder()
        .setEnumSerialization(EnumSerialization.NAME)
        .addSerializedNameExtension(Annotations.serializedName)
        .addSerializedEnumValueExtension(Annotations.serializedValue);
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(GeneratedMessageV3.class, protoTypeAdapter.build())
        .create();
    gsonWithEnumNumbers = new GsonBuilder()
        .registerTypeHierarchyAdapter(GeneratedMessageV3.class, protoTypeAdapter
            .setEnumSerialization(EnumSerialization.NUMBER)
            .build())
        .create();
    gsonWithLowerHyphen = new GsonBuilder()
        .registerTypeHierarchyAdapter(GeneratedMessageV3.class, protoTypeAdapter
            .setFieldNameSerializationFormat(CaseFormat.LOWER_UNDERSCORE, CaseFormat.LOWER_HYPHEN)
            .build())
        .create();
  }

  @Test
  public void testProtoWithAnnotations_deserialize() {
    String json = String.format("{  %n"
        + "   \"id\":\"41e5e7fd6065d101b97018a465ffff01\",%n"
        + "   \"expiration_date\":{  %n"
        + "      \"month\":\"12\",%n"
        + "      \"year\":\"2017\",%n"
        + "      \"timeStamp\":\"9864653135687\",%n"
        + "      \"countryCode5f55\":\"en_US\"%n"
        + "   },%n"
        // Don't define innerMessage1
        + "   \"innerMessage2\":{  %n"
        // Set a number as a string; it should work
        + "      \"nIdCt\":\"98798465\",%n"
        + "      \"content\":\"text/plain\",%n"
        + "      \"$binary_data$\":[  %n"
        + "         {  %n"
        + "            \"data\":\"OFIN8e9fhwoeh8((⁹8efywoih\",%n"
        // Don't define width
        + "            \"height\":665%n"
        + "         },%n"
        + "         {  %n"
        // Define as an int; it should work
        + "            \"data\":65,%n"
        + "            \"width\":-56684%n"
        // Don't define height
        + "         }%n"
        + "      ]%n"
        + "   },%n"
        // Define a bunch of non recognizable data
        + "   \"non_existing\":\"foobar\",%n"
        + "   \"stillNot\":{  %n"
        + "      \"bunch\":\"of_useless data\"%n"
        + "   }%n"
        + "}");
    ProtoWithAnnotations proto = gson.fromJson(json, ProtoWithAnnotations.class);
    assertThat(proto.getId()).isEqualTo("41e5e7fd6065d101b97018a465ffff01");
    assertThat(proto.getOuterMessage()).isEqualTo(OuterMessage.newBuilder()
        .setMonth(12)
        .setYear(2017)
        .setLongTimestamp(9864653135687L)
        .setCountryCode5F55("en_US")
        .build());
    assertThat(proto.hasInnerMessage1()).isFalse();
    assertThat(proto.getInnerMessage2()).isEqualTo(InnerMessage.newBuilder()
        .setNIdCt(98798465)
        .setContent(Type.TEXT)
        .addData(Data.newBuilder()
            .setData("OFIN8e9fhwoeh8((⁹8efywoih")
            .setHeight(665))
        .addData(Data.newBuilder()
            .setData("65")
            .setWidth(-56684))
        .build());

    String rebuilt = gson.toJson(proto);
    assertThat(rebuilt).isEqualTo("{"
        + "\"id\":\"41e5e7fd6065d101b97018a465ffff01\","
        + "\"expiration_date\":{"
        + "\"month\":12,"
        + "\"year\":2017,"
        + "\"timeStamp\":9864653135687,"
        + "\"countryCode5f55\":\"en_US\""
        + "},"
        + "\"innerMessage2\":{"
        + "\"nIdCt\":98798465,"
        + "\"content\":\"text/plain\","
        + "\"$binary_data$\":["
        + "{"
        + "\"data\":\"OFIN8e9fhwoeh8((⁹8efywoih\","
        + "\"height\":665"
        + "},"
        + "{"
        + "\"data\":\"65\","
        + "\"width\":-56684"
        + "}]}}");
  }

  @Test
  public void testProtoWithAnnotations_deserializeUnknownEnumValue() {
    String json = String.format("{  %n"
        + "   \"content\":\"UNKNOWN\"%n"
        + "}");
    InnerMessage proto = gson.fromJson(json, InnerMessage.class);
    assertThat(proto.getContent()).isEqualTo(Type.UNKNOWN);
  }

  @Test
  public void testProtoWithAnnotations_deserializeUnrecognizedEnumValue() {
    String json = String.format("{  %n"
        + "   \"content\":\"UNRECOGNIZED\"%n"
        + "}");
    try {
      gson.fromJson(json, InnerMessage.class);
      assertWithMessage("Should have thrown").fail();
    } catch (JsonParseException e) {
      // expected
    }
  }

  @Test
  public void testProtoWithAnnotations_deserializeWithEnumNumbers() {
    String json = String.format("{  %n"
        + "   \"content\":\"0\"%n"
        + "}");
    InnerMessage proto = gsonWithEnumNumbers.fromJson(json, InnerMessage.class);
    assertThat(proto.getContent()).isEqualTo(Type.UNKNOWN);
    String rebuilt = gsonWithEnumNumbers.toJson(proto);
    assertThat(rebuilt).isEqualTo("{\"content\":0}");

    json = String.format("{  %n"
        + "   \"content\":\"2\"%n"
        + "}");
    proto = gsonWithEnumNumbers.fromJson(json, InnerMessage.class);
    assertThat(proto.getContent()).isEqualTo(Type.IMAGE);
    rebuilt = gsonWithEnumNumbers.toJson(proto);
    assertThat(rebuilt).isEqualTo("{\"content\":2}");
  }

  @Test
  public void testProtoWithAnnotations_serialize() {
    ProtoWithAnnotations proto = ProtoWithAnnotations.newBuilder()
        .setId("09f3j20839h032y0329hf30932h0nffn")
        .setOuterMessage(OuterMessage.newBuilder()
            .setMonth(14)
            .setYear(6650)
            .setLongTimestamp(468406876880768L))
        .setInnerMessage1(InnerMessage.newBuilder()
            .setNIdCt(12)
            .setContent(Type.IMAGE)
            .addData(Data.newBuilder()
                .setData("data$$")
                .setWidth(200))
            .addData(Data.newBuilder()
                .setHeight(56)))
        .build();

    String json = gsonWithLowerHyphen.toJson(proto);
    assertThat(json).isEqualTo(
        "{\"id\":\"09f3j20839h032y0329hf30932h0nffn\","
        + "\"expiration_date\":{"
            + "\"month\":14,"
            + "\"year\":6650,"
            + "\"timeStamp\":468406876880768"
        + "},"
        // This field should be using hyphens
        + "\"inner-message-1\":{"
            + "\"n--id-ct\":12,"
            + "\"content\":2,"
            + "\"$binary_data$\":["
              + "{"
                  + "\"data\":\"data$$\","
                  + "\"width\":200"
              + "},"
              + "{"
                  + "\"height\":56"
              + "}]"
            + "}"
        + "}");

    ProtoWithAnnotations rebuilt = gsonWithLowerHyphen.fromJson(json, ProtoWithAnnotations.class);
    assertThat(rebuilt).isEqualTo(proto);
  }
}
