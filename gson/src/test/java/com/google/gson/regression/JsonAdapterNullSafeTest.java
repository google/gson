/*
 * Copyright (C) 2016 Google Inc.
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
package com.google.gson.regression;

import java.io.IOException;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

public class JsonAdapterNullSafeTest extends TestCase {
  // The recursiveCall in {@link Device.JsonAdapterFactory} is the source of this bug
  // because it returns a null type adapter.

  private final Gson gson = new Gson();

  public void testNullSafeBugSerialize() throws Exception {
    Device device = new Device("ec57803e", 2);
    gson.toJson(device);
  }

  public void testNullSafeBugDeserialize() throws Exception {
    String json = "\"{\\\"id\\\":\\\"ec57803e2\\\",\\\"category\\\":2}\"";
    Device device = gson.fromJson(json, Device.class);
    assertEquals("ec57803e2", device.id);
    assertEquals(2, device.category);
  }

  @JsonAdapter(Device.JsonAdapterFactory.class)
  private static final class Device {
    String id;
    int category;
    Device(String id, int category) {
      this.id = id;
      this.category = category;
    }

    /**
     * Write the value as a String, not JSON.
     */
    static final class JsonAdapterFactory implements TypeAdapterFactory {
      private static final ThreadLocal<Boolean> recursiveCall = new ThreadLocal<Boolean>();

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
        if (type.getRawType() != Device.class || recursiveCall.get() != null) {
          recursiveCall.set(null); // clear for subsequent use
          return null;
        }
        recursiveCall.set(Boolean.TRUE);
        final TypeAdapter<Device> delegate = (TypeAdapter) gson.getDelegateAdapter(this, type);
        return (TypeAdapter) new TypeAdapter<Device>() {
          @Override public void write(JsonWriter out, Device value) throws IOException {
            delegate.write(out, value);
          }
          @Override public Device read(JsonReader in) throws IOException {
            String json = in.nextString();
            JsonParser parser = new JsonParser();
            JsonElement root = parseString(parser, json, null);
            return root == null ? null : delegate.fromJsonTree(root);
          }
          private JsonElement parseString(JsonParser parser, String json, String prevJson)
              throws IOException { // called recursively
            JsonElement root = parser.parse(json);
            if (root instanceof JsonPrimitive) {
              prevJson = json;
              json = root.getAsString();
              if (Objects.equals(json, prevJson)) {
                throw new JsonSyntaxException("Unexpected Json: " + json);
              }
              return parseString(parser, json, prevJson);
            }
            return root;
          }
        };
      }
    }
  }
}
