/*
 * Copyright (C) 2022 Google Inc.
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
import static org.junit.Assert.assertThrows;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

public class TypeAdapterTest {
  @Test
  public void testNullSafe() throws IOException {
    TypeAdapter<String> adapter = assertionErrorAdapter.nullSafe();

    assertThat(adapter.toJson(null)).isEqualTo("null");
    assertThat(adapter.fromJson("null")).isNull();
  }

  @Test
  public void testNullSafe_ReturningSameInstanceOnceNullSafe() {
    TypeAdapter<?> nullSafeAdapter = assertionErrorAdapter.nullSafe();

    assertThat(nullSafeAdapter.nullSafe()).isSameInstanceAs(nullSafeAdapter);
    assertThat(nullSafeAdapter.nullSafe().nullSafe()).isSameInstanceAs(nullSafeAdapter);
    assertThat(nullSafeAdapter.nullSafe().nullSafe().nullSafe()).isSameInstanceAs(nullSafeAdapter);
  }

  @Test
  public void testNullSafe_ToString() {
    TypeAdapter<?> adapter = assertionErrorAdapter;

    assertThat(adapter.toString()).isEqualTo("assertionErrorAdapter");
    assertThat(adapter.nullSafe().toString())
        .isEqualTo("NullSafeTypeAdapter[assertionErrorAdapter]");
    assertThat(adapter.nullSafe().nullSafe().toString())
        .isEqualTo("NullSafeTypeAdapter[assertionErrorAdapter]");
  }

  private static final TypeAdapter<String> assertionErrorAdapter =
      new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, String value) {
          throw new AssertionError("unexpected call");
        }

        @Override
        public String read(JsonReader in) {
          throw new AssertionError("unexpected call");
        }

        @Override
        public String toString() {
          return "assertionErrorAdapter";
        }
      };

  /**
   * Tests behavior when {@link TypeAdapter#write(JsonWriter, Object)} manually throws {@link
   * IOException} which is not caused by writer usage.
   */
  @Test
  public void testToJson_ThrowingIOException() {
    IOException exception = new IOException("test");
    TypeAdapter<Integer> adapter =
        new TypeAdapter<>() {
          @Override
          public void write(JsonWriter out, Integer value) throws IOException {
            throw exception;
          }

          @Override
          public Integer read(JsonReader in) {
            throw new AssertionError("not needed by this test");
          }
        };

    JsonIOException e = assertThrows(JsonIOException.class, () -> adapter.toJson(1));
    assertThat(e).hasCauseThat().isEqualTo(exception);

    e = assertThrows(JsonIOException.class, () -> adapter.toJsonTree(1));
    assertThat(e).hasCauseThat().isEqualTo(exception);
  }

  private static final TypeAdapter<String> adapter =
      new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, String value) throws IOException {
          out.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
          return in.nextString();
        }
      };

  // Note: This test just verifies the current behavior; it is a bit questionable
  // whether that behavior is actually desired
  @Test
  public void testFromJson_Reader_TrailingData() throws IOException {
    assertThat(adapter.fromJson(new StringReader("\"a\"1"))).isEqualTo("a");
  }

  // Note: This test just verifies the current behavior; it is a bit questionable
  // whether that behavior is actually desired
  @Test
  public void testFromJson_String_TrailingData() throws IOException {
    assertThat(adapter.fromJson("\"a\"1")).isEqualTo("a");
  }
}
