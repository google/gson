/*
 * Copyright (C) 2012 Google Inc.
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

package com.google.gson;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import org.junit.Test;

/**
 * @author Jesse Wilson
 */
public class OverrideCoreTypeAdaptersTest {
  private static final TypeAdapter<Boolean> booleanAsIntAdapter = new TypeAdapter<Boolean>() {
    @Override public void write(JsonWriter out, Boolean value) throws IOException {
      out.value(value ? 1 : 0);
    }
    @Override public Boolean read(JsonReader in) throws IOException {
      int value = in.nextInt();
      return value != 0;
    }
  };

  private static final TypeAdapter<String> swapCaseStringAdapter = new TypeAdapter<String>() {
    @Override public void write(JsonWriter out, String value) throws IOException {
      out.value(value.toUpperCase(Locale.US));
    }
    @Override public String read(JsonReader in) throws IOException {
      return in.nextString().toLowerCase(Locale.US);
    }
  };

  @Test
  public void testOverrideWrapperBooleanAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
        .create();
    assertThat(gson.toJson(true, boolean.class)).isEqualTo("true");
    assertThat(gson.toJson(true, Boolean.class)).isEqualTo("1");
    assertThat(gson.fromJson("true", boolean.class)).isEqualTo(Boolean.TRUE);
    assertThat(gson.fromJson("1", Boolean.class)).isEqualTo(Boolean.TRUE);
    assertThat(gson.fromJson("0", Boolean.class)).isEqualTo(Boolean.FALSE);
  }

  @Test
  public void testOverridePrimitiveBooleanAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
        .create();
    assertThat(gson.toJson(true, boolean.class)).isEqualTo("1");
    assertThat(gson.toJson(true, Boolean.class)).isEqualTo("true");
    assertThat(gson.fromJson("1", boolean.class)).isEqualTo(Boolean.TRUE);
    assertThat(gson.fromJson("true", Boolean.class)).isEqualTo(Boolean.TRUE);
    assertThat(gson.toJson(false, boolean.class)).isEqualTo("0");
  }

  @Test
  public void testOverrideStringAdapter() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(String.class, swapCaseStringAdapter)
        .create();
    assertThat(gson.toJson("Hello", String.class)).isEqualTo("\"HELLO\"");
    assertThat(gson.fromJson("\"Hello\"", String.class)).isEqualTo("hello");
  }
}
