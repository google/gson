/*
 * Copyright (C) 2011 Google Inc.
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Adapter for java.sql.Date. Although this class appears stateless, it is not. DateFormat captures
 * its time zone and locale when it is created, which gives this class state. DateFormat isn't
 * thread safe either, so this class has to synchronize its read and write methods.
 */
@SuppressWarnings("JavaUtilDate")
final class SqlDateTypeAdapter extends TypeAdapter<java.sql.Date> {
  static final TypeAdapterFactory FACTORY =
      new TypeAdapterFactory() {
        @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
          return typeToken.getRawType() == java.sql.Date.class
              ? (TypeAdapter<T>) new SqlDateTypeAdapter()
              : null;
        }
      };

  private final DateFormat format = new SimpleDateFormat("MMM d, yyyy");

  private SqlDateTypeAdapter() {}

  @Override
  public java.sql.Date read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    String s = in.nextString();
    synchronized (this) {
      TimeZone originalTimeZone = format.getTimeZone(); // Save the original time zone
      try {
        Date utilDate = format.parse(s);
        return new java.sql.Date(utilDate.getTime());
      } catch (ParseException e) {
        throw new JsonSyntaxException(
            "Failed parsing '" + s + "' as SQL Date; at path " + in.getPreviousPath(), e);
      } finally {
        format.setTimeZone(originalTimeZone); // Restore the original time zone after parsing
      }
    }
  }

  @Override
  public void write(JsonWriter out, java.sql.Date value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    String dateString;
    synchronized (this) {
      dateString = format.format(value);
    }
    out.value(dateString);
  }
}
