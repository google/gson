/*
 * Copyright (C) 2023 Google Inc.
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

package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.TroubleshootingGuide;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * {@code TypeAdapterFactory} which throws an exception when trying to serialize or
 * deserialize unsupported classes from third-party JSON libraries.
 *
 * <p>This is mainly intended as help for users who accidentally mix Gson and non-Gson
 * code and are then surprised by unexpected JSON data or issues when trying to
 * deserialize the JSON data.
 */
public class UnsupportedJsonLibraryTypeAdapterFactory implements TypeAdapterFactory {
  public static final UnsupportedJsonLibraryTypeAdapterFactory INSTANCE = new UnsupportedJsonLibraryTypeAdapterFactory();

  private UnsupportedJsonLibraryTypeAdapterFactory() {
  }

  // Cover JSON classes from popular libraries which might be used by accident with Gson
  // Don't have to cover classes which implement `Collection` / `List` or `Map` because
  // Gson's built-in adapters for these types should be able to handle them just fine
  private static final Set<String> UNSUPPORTED_CLASS_NAMES = new HashSet<>(Arrays.asList(
      // https://github.com/stleary/JSON-java and Android
      "org.json.JSONArray",
      "org.json.JSONObject",
      // https://github.com/eclipse-vertx/vert.x
      "io.vertx.core.json.JsonArray",
      "io.vertx.core.json.JsonObject"
  ));

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    final String className = type.getRawType().getName();
    if (!UNSUPPORTED_CLASS_NAMES.contains(className)) {
      return null;
    }

    // Don't directly throw exception here in case no instance of the class is every serialized
    // or deserialized, instead only thrown when actual serialization or deserialization attempt
    // occurs
    return new TypeAdapter<T>() {
      private RuntimeException createException() {
        // TODO: Use more specific exception type; also adjust Troubleshooting.md entry then
        return new RuntimeException("Unsupported class from other JSON library: " + className
            + "\nSee " + TroubleshootingGuide.createUrl("unsupported-json-library-class"));
      }

      @Override
      public T read(JsonReader in) throws IOException {
        throw createException();
      }

      @Override
      public void write(JsonWriter out, T value) throws IOException {
        throw createException();
      }
    };
  }
}
