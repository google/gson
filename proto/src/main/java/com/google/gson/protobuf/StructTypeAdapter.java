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
package com.google.gson.protobuf;

import static java.util.Objects.requireNonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.util.Map;

/**
 * Converts between {@link com.google.protobuf.Struct} and Gson's {@link JsonObject}, without going
 * through an intermediate JSON string.
 *
 * <p>{@code Struct} is one of protobuf's "well-known types" for representing arbitrary, schema-less
 * JSON-like data. It is structurally a map from string keys to {@link Value}, where each {@code
 * Value} holds exactly one of: null, a number, a string, a boolean, a nested {@code Struct}, or a
 * {@link ListValue} (a repeated {@code Value}).
 *
 * <p>The output of {@link #fromStruct} matches what {@code
 * com.google.protobuf.util.JsonFormat.printer().print(struct)} would produce, reparsed into a
 * {@code JsonObject}.
 *
 * @since $next-version$
 */
public final class StructTypeAdapter {
  private StructTypeAdapter() {}

  /**
   * Converts a {@link Struct} directly into a Gson {@link JsonObject}.
   *
   * @throws NullPointerException if {@code struct} is {@code null}
   * @since $next-version$
   */
  public static JsonObject fromStruct(Struct struct) {
    requireNonNull(struct);
    JsonObject result = new JsonObject();
    for (Map.Entry<String, Value> entry : struct.getFieldsMap().entrySet()) {
      result.add(entry.getKey(), fromValue(entry.getValue()));
    }
    return result;
  }

  private static JsonElement fromValue(Value value) {
    switch (value.getKindCase()) {
      case NUMBER_VALUE:
        double number = value.getNumberValue();
        if (Double.isNaN(number) || Double.isInfinite(number)) {
          throw new IllegalArgumentException(
              "Struct contains a NaN or Infinite number value, which has no JSON representation: "
                  + number);
        }
        return new JsonPrimitive(number);
      case STRING_VALUE:
        return new JsonPrimitive(value.getStringValue());
      case BOOL_VALUE:
        return new JsonPrimitive(value.getBoolValue());
      case STRUCT_VALUE:
        return fromStruct(value.getStructValue());
      case LIST_VALUE:
        return fromListValue(value.getListValue());
      case NULL_VALUE:
      case KIND_NOT_SET:
        return JsonNull.INSTANCE;
    }
    throw new AssertionError("Unreachable: " + value.getKindCase());
  }

  private static JsonArray fromListValue(ListValue listValue) {
    JsonArray array = new JsonArray();
    for (Value value : listValue.getValuesList()) {
      array.add(fromValue(value));
    }
    return array;
  }
}
