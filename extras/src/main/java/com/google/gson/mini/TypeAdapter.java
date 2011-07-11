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

package com.google.gson.mini;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public abstract class TypeAdapter<T> {
  public abstract T read(JsonReader reader) throws IOException;
  public abstract void write(JsonWriter writer, T value) throws IOException;

  public final String toJson(T value) throws IOException {
    StringWriter stringWriter = new StringWriter();
    write(stringWriter, value);
    return stringWriter.toString();
  }

  public final void write(Writer out, T value) throws IOException {
    JsonWriter writer = new JsonWriter(out);
    write(writer, value);
  }

  public final T fromJson(String json) throws IOException {
    return read(new StringReader(json));
  }

  public final T read(Reader in) throws IOException {
    JsonReader reader = new JsonReader(in);
    reader.setLenient(true); // TODO: why?
    return read(reader);
  }

  interface Factory {
    <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> type);
  }
}
