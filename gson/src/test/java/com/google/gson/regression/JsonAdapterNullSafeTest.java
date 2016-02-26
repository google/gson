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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

public class JsonAdapterNullSafeTest extends TestCase {
  private final Gson gson = new Gson();

  public void testNullSafeBugSerialize() throws Exception {
    Device device = new Device("ec57803e");
    gson.toJson(device);
  }

  public void testNullSafeBugDeserialize() throws Exception {
    Device device = gson.fromJson("{'id':'ec57803e2'}", Device.class);
    assertEquals("ec57803e2", device.id);
  }

  @JsonAdapter(Device.JsonAdapterFactory.class)
  private static final class Device {
    String id;
    Device(String id) {
      this.id = id;
    }

    static final class JsonAdapterFactory implements TypeAdapterFactory {
      // The recursiveCall in {@link Device.JsonAdapterFactory} is the source of this bug
      // because we use it to return a null type adapter on a recursive call.
      private static final ThreadLocal<Boolean> recursiveCall = new ThreadLocal<Boolean>();

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
        if (type.getRawType() != Device.class || recursiveCall.get() != null) {
          recursiveCall.set(null); // clear for subsequent use
          return null;
        }
        recursiveCall.set(Boolean.TRUE);
        return (TypeAdapter) gson.getDelegateAdapter(this, type);
      }
    }
  }
}
