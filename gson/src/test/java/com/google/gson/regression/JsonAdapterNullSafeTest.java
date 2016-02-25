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
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import junit.framework.TestCase;

public class JsonAdapterNullSafeTest extends TestCase {
  private final Gson gson = new Gson();

  /**
   * The recursiveCall in {@link ControlData.JsonAdapterFactory} is the source of this bug
   * because it returns a null type adapter.
   */
  public void testTypeAdapterFactoryNullSafeBug() throws IOException {
      ControlData control = new ControlData("ec57803e", 2, true, 11211);
      Device device = new Device("device1", control);
      String json = gson.toJson(device);

      json = "\"{\\\"id\\\":\\\"ec57803e2\\\",\\\"category\\\":2,\\\"alwaysOn\\\":true,\\\"codeset_id\\\":11211}\"";
      control = gson.fromJson(json, ControlData.class);
      assertEquals("ec57803e2", control.id);
      assertTrue(control.alwaysOn);
      assertEquals(11211, control.codesetId);
      assertEquals(2, control.category);

      String deviceJson = "{'id':'device1','controlData':null}";
      device = gson.fromJson(deviceJson, Device.class);
      assertNull(device.controlData);

      deviceJson = "{'id':'device1','controlData':{'id':'ec57803e2','category':2,'alwaysOn':true,'codeset_id':12221}}";
      device = gson.fromJson(deviceJson, Device.class);
      assertEquals(12221, device.controlData.codesetId);

      deviceJson = "{'id':'device1','controlData':'\\\"{}\\\"'}";
      device = gson.fromJson(deviceJson, Device.class);

      try {
          deviceJson = "{'id':'device1','controlData':'a'}";
          device = gson.fromJson(deviceJson, Device.class);
          assertNotNull(device.controlData);
          fail();
      } catch (JsonSyntaxException expected) {}

      deviceJson = "{'id':'device1','controlData':'  '}";
      device = gson.fromJson(deviceJson, Device.class);
      assertNull(device.controlData);
  }

  private static final class Device {
    @SuppressWarnings("unused")
    String id;
    ControlData controlData;

    public Device(String id, ControlData controlData) {
      this.id = id;
      this.controlData = controlData;
    }
  }

  @JsonAdapter(ControlData.JsonAdapterFactory.class)
  private static final class ControlData {
    String id;
    int category;
    boolean alwaysOn;
    @SerializedName("codeset_id") int codesetId;
    ControlData(String id, int category, boolean alwaysOn, int codesetId) {
      this.id = id;
      this.category = category;
      this.alwaysOn = alwaysOn;
      this.codesetId = codesetId;
    }

    /**
     * DeviceControlData is received as String in JSON instead of proper JSON.
     * So, we need to write a special type adapter.
     */
    static final class JsonAdapterFactory extends StringifiedJsonAdapterFactory<ControlData> {
      private static final ThreadLocal<Boolean> recursiveCall = new ThreadLocal<Boolean>();
      public JsonAdapterFactory() {
        super(recursiveCall, ControlData.class, true);
      }
    }
  }

  /**
   * Converts an object to Stringified JSON for saving in a JSON field as a string type.
   */
  private static class StringifiedJsonAdapterFactory<R> implements TypeAdapterFactory {
    private final Class<R> targetType;
    private final ThreadLocal<Boolean> recursiveCall;
    private final boolean writeAsJson;

    /**
     * @param recursiveCall provide a static ThreadLocal to workaround a Gson bug where
     *   annotation-based type adapter factories can't be skipped over.
     * @param targetType The class whose instances needs to be written in stringified form.
     * @param writeAsJson Set this to true to write the output as JSON not string.
     */
    public StringifiedJsonAdapterFactory(ThreadLocal<Boolean> recursiveCall, Class<R> targetType,
        boolean writeAsJson) {
      this.recursiveCall = recursiveCall;
      this.targetType = targetType;
      this.writeAsJson = writeAsJson;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
      if (type.getRawType() != targetType || recursiveCall.get() != null) {
        recursiveCall.set(null); // clear for subsequent use
        return null;
      }
      recursiveCall.set(Boolean.TRUE);
      final TypeAdapter<R> delegate = (TypeAdapter) gson.getDelegateAdapter(this, type);
      return (TypeAdapter) new TypeAdapter<R>() {
        @Override public void write(JsonWriter out, R value) throws IOException {
          if (writeAsJson) {
            delegate.write(out, value);
          } else {
            // delegate.toJson(value) will write nulls. avoid that by using gson.toJson()
            String json = gson.toJson(delegate.toJsonTree(value));
            out.value(json);
          }
        }
        @Override public R read(JsonReader in) throws IOException {
          JsonToken token = in.peek();
          JsonElement root;
          if (token == JsonToken.BEGIN_OBJECT) {
            return delegate.read(in);
          } else { // assume to be string
            String json = in.nextString();
            JsonParser parser = new JsonParser();
            root = parseString(parser, json, null);
            return root == null ? null : delegate.fromJsonTree(root);
          }
        }

        private JsonElement parseString(JsonParser parser, String json, String prevJson)
            throws IOException {
          if (json == null || json.trim().isEmpty()) {
            return null;
          }
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
      }.nullSafe();
    }
  }
}
