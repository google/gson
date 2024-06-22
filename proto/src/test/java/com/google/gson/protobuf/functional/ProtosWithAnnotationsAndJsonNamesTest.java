/*
 * Copyright (C) 2024 Google Inc.
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
import com.google.gson.protobuf.generated.Annotations;
import com.google.gson.protobuf.generated.Bag.ProtoWithAnnotationsAndJsonNames;
import com.google.protobuf.GeneratedMessage;
import java.util.Map;
import org.junit.Test;

/**
 * Functional tests for protocol buffers using annotations and custom json_name values for field
 * names.
 *
 * @author Andrew Szeto
 */
public class ProtosWithAnnotationsAndJsonNamesTest {
  private static final Gson GSON_PLAIN =
      new GsonBuilder()
          .registerTypeHierarchyAdapter(
              GeneratedMessage.class, ProtoTypeAdapter.newBuilder().build())
          .create();
  private static final Gson GSON_WITH_SERIALIZED_NAME =
      new GsonBuilder()
          .registerTypeHierarchyAdapter(
              GeneratedMessage.class,
              ProtoTypeAdapter.newBuilder()
                  .addSerializedNameExtension(Annotations.serializedName)
                  .setShouldUseJsonNameFieldOption(false)
                  .build())
          .create();
  private static final Gson GSON_WITH_JSON_NAME =
      new GsonBuilder()
          .registerTypeHierarchyAdapter(
              GeneratedMessage.class,
              ProtoTypeAdapter.newBuilder().setShouldUseJsonNameFieldOption(true).build())
          .create();
  private static final Gson GSON_WITH_SERIALIZED_NAME_AND_JSON_NAME =
      new GsonBuilder()
          .registerTypeHierarchyAdapter(
              GeneratedMessage.class,
              ProtoTypeAdapter.newBuilder()
                  .addSerializedNameExtension(Annotations.serializedName)
                  .setShouldUseJsonNameFieldOption(true)
                  .build())
          .create();

  private static final Map<Gson, String> JSON_OUTPUTS =
      Map.of(
          GSON_PLAIN,
              "{\"neither\":\"xxx\",\"jsonNameOnly\":\"yyy\",\"annotationOnly\":\"zzz\",\"both\":\"www\"}",
          GSON_WITH_JSON_NAME,
              "{\"neither\":\"xxx\",\"aaa\":\"yyy\",\"annotationOnly\":\"zzz\",\"ccc\":\"www\"}",
          GSON_WITH_SERIALIZED_NAME,
              "{\"neither\":\"xxx\",\"jsonNameOnly\":\"yyy\",\"bbb\":\"zzz\",\"ddd\":\"www\"}",
          GSON_WITH_SERIALIZED_NAME_AND_JSON_NAME,
              "{\"neither\":\"xxx\",\"aaa\":\"yyy\",\"bbb\":\"zzz\",\"ddd\":\"www\"}");

  private static final ProtoWithAnnotationsAndJsonNames PROTO =
      ProtoWithAnnotationsAndJsonNames.newBuilder()
          .setNeither("xxx")
          .setJsonNameOnly("yyy")
          .setAnnotationOnly("zzz")
          .setBoth("www")
          .build();

  @Test
  public void testProtoWithAnnotationsAndJsonNames_basicConversions() {
    JSON_OUTPUTS.forEach(
        (gson, json) -> {
          assertThat(gson.fromJson(json, ProtoWithAnnotationsAndJsonNames.class)).isEqualTo(PROTO);
          assertThat(gson.toJson(PROTO)).isEqualTo(json);
        });
  }

  @Test
  public void testProtoWithAnnotationsAndJsonNames_basicRoundTrips() {
    JSON_OUTPUTS.forEach(
        (gson, json) -> {
          assertThat(roundTrip(gson, gson, json)).isEqualTo(json);
          assertThat(roundTrip(gson, gson, PROTO)).isEqualTo(PROTO);
        });
  }

  @Test
  public void testProtoWithAnnotationsAndJsonNames_unannotatedField() {
    ProtoWithAnnotationsAndJsonNames proto =
        ProtoWithAnnotationsAndJsonNames.newBuilder().setNeither("zzz").build();
    String json = "{\"neither\":\"zzz\"}";

    for (Gson gson1 : JSON_OUTPUTS.keySet()) {
      for (Gson gson2 : JSON_OUTPUTS.keySet()) {
        // all configs should match with each other in how they serialize this proto, and they
        // should be able to deserialize any other config's serialization of the proto back to its
        // original form
        assertThat(gson1.toJson(proto)).isEqualTo(gson2.toJson(proto));
        assertThat(roundTrip(gson1, gson2, proto)).isEqualTo(proto);
        // the same, but in the other direction
        assertThat(gson1.fromJson(json, ProtoWithAnnotationsAndJsonNames.class))
            .isEqualTo(gson2.fromJson(json, ProtoWithAnnotationsAndJsonNames.class));
        assertThat(roundTrip(gson1, gson2, json)).isEqualTo(json);
      }
    }
  }

  @Test
  public void testProtoWithAnnotationsAndJsonNames_fieldWithJsonName() {
    ProtoWithAnnotationsAndJsonNames proto =
        ProtoWithAnnotationsAndJsonNames.newBuilder().setJsonNameOnly("zzz").build();
    String jsonWithoutJsonName = "{\"jsonNameOnly\":\"zzz\"}";
    String jsonWithJsonName = "{\"aaa\":\"zzz\"}";

    // the ProtoTypeAdapter that checks for the custom annotation should default to the basic name
    assertThat(GSON_PLAIN.toJson(proto)).isEqualTo(jsonWithoutJsonName);
    assertThat(GSON_WITH_SERIALIZED_NAME.toJson(proto)).isEqualTo(GSON_PLAIN.toJson(proto));

    // the ProtoTypeAdapter that respects the `json_name` option should not have the same output as
    // the base case
    assertThat(GSON_WITH_JSON_NAME.toJson(proto)).isNotEqualTo(GSON_PLAIN.toJson(proto));

    // both ProtoTypeAdapters that set shouldUseJsonNameFieldOption to true should match in output
    assertThat(GSON_WITH_JSON_NAME.toJson(proto)).isEqualTo(jsonWithJsonName);
    assertThat(GSON_WITH_JSON_NAME.toJson(proto))
        .isEqualTo(GSON_WITH_SERIALIZED_NAME_AND_JSON_NAME.toJson(proto));

    // should fail to round-trip if we serialize via the `json_name` and deserialize without it or
    // vice versa
    assertThat(roundTrip(GSON_PLAIN, GSON_WITH_JSON_NAME, proto)).isNotEqualTo(proto);
    assertThat(roundTrip(GSON_WITH_JSON_NAME, GSON_PLAIN, proto)).isNotEqualTo(proto);
  }

  @Test
  public void testProtoWithAnnotationsAndJsonNames_fieldWithCustomSerializedName() {
    ProtoWithAnnotationsAndJsonNames proto =
        ProtoWithAnnotationsAndJsonNames.newBuilder().setAnnotationOnly("zzz").build();
    String jsonWithoutCustomName = "{\"annotationOnly\":\"zzz\"}";
    String jsonWithCustomName = "{\"bbb\":\"zzz\"}";

    // the ProtoTypeAdapter that checks for the json name should default to the basic name
    assertThat(GSON_PLAIN.toJson(proto)).isEqualTo(jsonWithoutCustomName);
    assertThat(GSON_WITH_JSON_NAME.toJson(proto)).isEqualTo(GSON_PLAIN.toJson(proto));

    // the ProtoTypeAdapter that checks for the custom serialized name should not have the same
    // output as the base case
    assertThat(GSON_WITH_SERIALIZED_NAME.toJson(proto)).isNotEqualTo(GSON_PLAIN.toJson(proto));

    // both ProtoTypeAdapters that check for the custom serialized name should match in output
    assertThat(GSON_WITH_SERIALIZED_NAME.toJson(proto)).isEqualTo(jsonWithCustomName);
    assertThat(GSON_WITH_SERIALIZED_NAME.toJson(proto))
        .isEqualTo(GSON_WITH_SERIALIZED_NAME_AND_JSON_NAME.toJson(proto));

    // should fail to round-trip if we serialize via the custom name and deserialize without it or
    // vice versa
    assertThat(roundTrip(GSON_PLAIN, GSON_WITH_SERIALIZED_NAME, proto)).isNotEqualTo(proto);
    assertThat(roundTrip(GSON_WITH_SERIALIZED_NAME, GSON_PLAIN, proto)).isNotEqualTo(proto);
  }

  @Test
  public void testProtoWithAnnotationsAndJsonNames_fieldWithJsonNameAndCustomSerializedName() {
    ProtoWithAnnotationsAndJsonNames proto =
        ProtoWithAnnotationsAndJsonNames.newBuilder().setBoth("zzz").build();
    String jsonPlain = "{\"both\":\"zzz\"}";
    String jsonWithJsonName = "{\"ccc\":\"zzz\"}";
    String jsonWithCustomName = "{\"ddd\":\"zzz\"}";

    // the three different configs serialize to three different values
    assertThat(GSON_PLAIN.toJson(proto)).isEqualTo(jsonPlain);
    assertThat(GSON_WITH_JSON_NAME.toJson(proto)).isEqualTo(jsonWithJsonName);
    assertThat(GSON_WITH_SERIALIZED_NAME.toJson(proto)).isEqualTo(jsonWithCustomName);

    // the case where both configs are enabled will prefer the custom annotation
    assertThat(GSON_WITH_SERIALIZED_NAME_AND_JSON_NAME.toJson(proto))
        .isEqualTo(GSON_WITH_SERIALIZED_NAME.toJson(proto));
  }

  private static String roundTrip(Gson jsonToProto, Gson protoToJson, String json) {
    return protoToJson.toJson(jsonToProto.fromJson(json, ProtoWithAnnotationsAndJsonNames.class));
  }

  private static ProtoWithAnnotationsAndJsonNames roundTrip(
      Gson protoToJson, Gson jsonToProto, ProtoWithAnnotationsAndJsonNames proto) {
    return jsonToProto.fromJson(protoToJson.toJson(proto), ProtoWithAnnotationsAndJsonNames.class);
  }
}
