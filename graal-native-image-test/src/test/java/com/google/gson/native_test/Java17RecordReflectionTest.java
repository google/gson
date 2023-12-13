/*
 * Copyright (C) 2023 Google Inc.
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
package com.google.gson.native_test;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class Java17RecordReflectionTest {
  public record PublicRecord(int i) {}

  @Test
  void testPublicRecord() {
    Gson gson = new Gson();
    PublicRecord r = gson.fromJson("{\"i\":1}", PublicRecord.class);
    assertThat(r.i).isEqualTo(1);
  }

  // Private record has implicit private canonical constructor
  private record PrivateRecord(int i) {}

  @Test
  void testPrivateRecord() {
    Gson gson = new Gson();
    PrivateRecord r = gson.fromJson("{\"i\":1}", PrivateRecord.class);
    assertThat(r.i).isEqualTo(1);
  }

  @Test
  void testLocalRecord() {
    record LocalRecordDeserialization(int i) {}

    Gson gson = new Gson();
    LocalRecordDeserialization r = gson.fromJson("{\"i\":1}", LocalRecordDeserialization.class);
    assertThat(r.i).isEqualTo(1);
  }

  @Test
  void testLocalRecordSerialization() {
    record LocalRecordSerialization(int i) {}

    Gson gson = new Gson();
    assertThat(gson.toJson(new LocalRecordSerialization(1))).isEqualTo("{\"i\":1}");
  }

  private record RecordWithSerializedName(@SerializedName("custom-name") int i) {}

  @Test
  void testSerializedName() {
    Gson gson = new Gson();
    RecordWithSerializedName r =
        gson.fromJson("{\"custom-name\":1}", RecordWithSerializedName.class);
    assertThat(r.i).isEqualTo(1);

    assertThat(gson.toJson(new RecordWithSerializedName(2))).isEqualTo("{\"custom-name\":2}");
  }

  private record RecordWithCustomConstructor(int i) {
    @SuppressWarnings("unused")
    RecordWithCustomConstructor {
      i += 5;
    }
  }

  @Test
  void testCustomConstructor() {
    Gson gson = new Gson();
    RecordWithCustomConstructor r = gson.fromJson("{\"i\":1}", RecordWithCustomConstructor.class);
    assertThat(r.i).isEqualTo(6);
  }

  private record RecordWithCustomAccessor(int i) {
    @SuppressWarnings("UnusedMethod")
    @Override
    public int i() {
      return i + 5;
    }
  }

  @Test
  void testCustomAccessor() {
    Gson gson = new Gson();
    assertThat(gson.toJson(new RecordWithCustomAccessor(2))).isEqualTo("{\"i\":7}");
  }

  @JsonAdapter(RecordWithCustomClassAdapter.CustomAdapter.class)
  private record RecordWithCustomClassAdapter(int i) {
    private static class CustomAdapter extends TypeAdapter<RecordWithCustomClassAdapter> {
      @Override
      public RecordWithCustomClassAdapter read(JsonReader in) throws IOException {
        return new RecordWithCustomClassAdapter(in.nextInt() + 5);
      }

      @Override
      public void write(JsonWriter out, RecordWithCustomClassAdapter value) throws IOException {
        out.value(value.i + 6);
      }
    }
  }

  @Test
  void testCustomClassAdapter() {
    Gson gson = new Gson();
    RecordWithCustomClassAdapter r = gson.fromJson("1", RecordWithCustomClassAdapter.class);
    assertThat(r.i).isEqualTo(6);

    assertThat(gson.toJson(new RecordWithCustomClassAdapter(1))).isEqualTo("7");
  }

  private record RecordWithCustomFieldAdapter(
      @JsonAdapter(RecordWithCustomFieldAdapter.CustomAdapter.class) int i) {
    private static class CustomAdapter extends TypeAdapter<Integer> {
      @Override
      public Integer read(JsonReader in) throws IOException {
        return in.nextInt() + 5;
      }

      @Override
      public void write(JsonWriter out, Integer value) throws IOException {
        out.value(value + 6);
      }
    }
  }

  @Test
  void testCustomFieldAdapter() {
    Gson gson = new Gson();
    RecordWithCustomFieldAdapter r = gson.fromJson("{\"i\":1}", RecordWithCustomFieldAdapter.class);
    assertThat(r.i).isEqualTo(6);

    assertThat(gson.toJson(new RecordWithCustomFieldAdapter(1))).isEqualTo("{\"i\":7}");
  }

  private record RecordWithRegisteredAdapter(int i) {}

  @Test
  void testCustomAdapter() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                RecordWithRegisteredAdapter.class,
                new TypeAdapter<RecordWithRegisteredAdapter>() {
                  @Override
                  public RecordWithRegisteredAdapter read(JsonReader in) throws IOException {
                    return new RecordWithRegisteredAdapter(in.nextInt() + 5);
                  }

                  @Override
                  public void write(JsonWriter out, RecordWithRegisteredAdapter value)
                      throws IOException {
                    out.value(value.i + 6);
                  }
                })
            .create();

    RecordWithRegisteredAdapter r = gson.fromJson("1", RecordWithRegisteredAdapter.class);
    assertThat(r.i).isEqualTo(6);

    assertThat(gson.toJson(new RecordWithRegisteredAdapter(1))).isEqualTo("7");
  }
}
