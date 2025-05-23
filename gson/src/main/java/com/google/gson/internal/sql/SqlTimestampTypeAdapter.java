/*
 * Copyright (C) 2020 Google Inc.
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

package com.google.gson.internal.sql;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

@SuppressWarnings("JavaUtilDate")
class SqlTimestampTypeAdapter extends TypeAdapter<Timestamp> {
  static final TypeAdapterFactory FACTORY =
      new TypeAdapterFactory() {
        @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
          if (typeToken.getRawType() == Timestamp.class) {
            TypeAdapter<Date> dateTypeAdapter = gson.getAdapter(Date.class);
            return (TypeAdapter<T>) new SqlTimestampTypeAdapter(dateTypeAdapter);
          } else {
            return null;
          }
        }
      };

  private final TypeAdapter<Date> dateTypeAdapter;

  private SqlTimestampTypeAdapter(TypeAdapter<Date> dateTypeAdapter) {
    this.dateTypeAdapter = dateTypeAdapter;
  }

  @Override
  public Timestamp read(JsonReader in) throws IOException {
    Date date = dateTypeAdapter.read(in);
    return date != null ? new Timestamp(date.getTime()) : null;
  }

  @Override
  public void write(JsonWriter out, Timestamp value) throws IOException {
    dateTypeAdapter.write(out, value);
  }
}
