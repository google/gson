/*
 * Copyright (C) 2026 Google Inc.
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
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.protobuf.StructTypeAdapter;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.google.protobuf.NullValue;

public class StructTypeAdapterTest {
    private final Gson gson = new Gson();

    @Test
    public void emptyStructConvertsToEmptyJsonObject() {
        Struct struct = Struct.newBuilder().build();

        JsonObject json = StructTypeAdapter.fromStruct(struct);

        assertThat(json.size()).isEqualTo(0);
    }

    @Test
    public void allValueKindsConvertCorrectly() {
        Struct struct =
                Struct.newBuilder()
                        .putFields("nullField", Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
                        .putFields("numberField", Value.newBuilder().setNumberValue(42.5).build())
                        .putFields("stringField", Value.newBuilder().setStringValue("hello").build())
                        .putFields("boolField", Value.newBuilder().setBoolValue(true).build())
                        .build();

        JsonObject json = StructTypeAdapter.fromStruct(struct);

        assertThat(json.get("nullField").isJsonNull()).isTrue();
        assertThat(json.get("numberField").getAsDouble()).isEqualTo(42.5);
        assertThat(json.get("stringField").getAsString()).isEqualTo("hello");
        assertThat(json.get("boolField").getAsBoolean()).isTrue();
    }

    @Test
    public void valueWithNoKindSetConvertsToJsonNull() {
        Struct struct = Struct.newBuilder().putFields("unset", Value.newBuilder().build()).build();

        JsonObject json = StructTypeAdapter.fromStruct(struct);

        assertThat(json.get("unset").isJsonNull()).isTrue();
    }

    @Test
    public void nestedStructAndListRecurseCorrectly() {
        Struct inner = Struct.newBuilder().putFields("leaf", stringValue("deep")).build();
        ListValue list =
                ListValue.newBuilder()
                        .addValues(stringValue("first"))
                        .addValues(Value.newBuilder().setStructValue(inner).build())
                        .addValues(
                                Value.newBuilder()
                                        .setListValue(ListValue.newBuilder().addValues(numberValue(1)).build())
                                        .build())
                        .build();
        Struct struct =
                Struct.newBuilder()
                        .putFields("nested", Value.newBuilder().setStructValue(inner).build())
                        .putFields("list", Value.newBuilder().setListValue(list).build())
                        .build();

        JsonObject json = StructTypeAdapter.fromStruct(struct);

        assertThat(json.getAsJsonObject("nested").get("leaf").getAsString()).isEqualTo("deep");
        assertThat(json.getAsJsonArray("list").get(0).getAsString()).isEqualTo("first");
        assertThat(
                json.getAsJsonArray("list").get(1).getAsJsonObject().get("leaf").getAsString())
                .isEqualTo("deep");
        assertThat(json.getAsJsonArray("list").get(2).getAsJsonArray().get(0).getAsDouble())
                .isEqualTo(1.0);
    }

    @Test
    public void nanNumberValueThrows() {
        Struct struct = Struct.newBuilder().putFields("bad", numberValue(Double.NaN)).build();

        assertThrows(IllegalArgumentException.class, () -> StructTypeAdapter.fromStruct(struct));
    }

    @Test
    public void infiniteNumberValueThrows() {
        Struct struct =
                Struct.newBuilder().putFields("bad", numberValue(Double.POSITIVE_INFINITY)).build();

        assertThrows(IllegalArgumentException.class, () -> StructTypeAdapter.fromStruct(struct));
    }

    @Test
    public void matchesExistingJsonFormatRoundTripForVariousStructs() throws Exception {
        List<Struct> samples =
                Arrays.asList(
                        Struct.newBuilder().build(),
                        Struct.newBuilder().putFields("a", stringValue("b")).build(),
                        Struct.newBuilder()
                                .putFields("num", numberValue(3))
                                .putFields("flag", Value.newBuilder().setBoolValue(false).build())
                                .putFields(
                                        "nested",
                                        Value.newBuilder()
                                                .setStructValue(
                                                        Struct.newBuilder().putFields("inner", stringValue("x")).build())
                                                .build())
                                .putFields(
                                        "list",
                                        Value.newBuilder()
                                                .setListValue(
                                                        ListValue.newBuilder()
                                                                .addValues(numberValue(1))
                                                                .addValues(stringValue("two"))
                                                                .build())
                                                .build())
                                .build());

        for (Struct struct : samples) {
            String jsonFormatString = JsonFormat.printer().omittingInsignificantWhitespace().print(struct);
            JsonObject expected = gson.fromJson(jsonFormatString, JsonObject.class);

            JsonObject actual = StructTypeAdapter.fromStruct(struct);

            assertThat(actual).isEqualTo(expected);
        }
    }

    private static Value stringValue(String s) {
        return Value.newBuilder().setStringValue(s).build();
    }

    private static Value numberValue(double d) {
        return Value.newBuilder().setNumberValue(d).build();
    }
}