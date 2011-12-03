/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.internal.bind.JsonElementWriter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

// TODO: nice documentation

/**
 *
 * @since 2.1
 */
public abstract class TypeAdapter<T> {
  public abstract T read(JsonReader reader) throws IOException;
  public abstract void write(JsonWriter writer, T value) throws IOException;

  public final String toJson(T value) throws IOException {
    StringWriter stringWriter = new StringWriter();
    toJson(stringWriter, value);
    return stringWriter.toString();
  }

  public final void toJson(Writer out, T value) throws IOException {
    JsonWriter writer = new JsonWriter(out);
    write(writer, value);
  }

  public final T fromJson(String json) throws IOException {
    return fromJson(new StringReader(json));
  }

  public final T fromJson(Reader in) throws IOException {
    JsonReader reader = new JsonReader(in);
    reader.setLenient(true);
    return read(reader);
  }

  public JsonElement toJsonTree(T src) {
    try {
      JsonElementWriter jsonWriter = new JsonElementWriter();
      jsonWriter.setLenient(true);
      write(jsonWriter, src);
      return jsonWriter.get();
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  public T fromJsonTree(JsonElement json) {
    try {
      JsonReader jsonReader = new JsonTreeReader(json);
      jsonReader.setLenient(true);
      return read(jsonReader);
    } catch (IOException e) {
      throw new JsonIOException(e);
    }
  }

  /**
   * @since 2.1
   */
  public interface Factory {
    <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type);
  }
}
