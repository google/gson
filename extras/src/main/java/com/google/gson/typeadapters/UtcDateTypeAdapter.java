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

package com.google.gson.typeadapters;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class UtcDateTypeAdapter extends TypeAdapter<Date> {
  private final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

  @Override
  public void write(JsonWriter out, Date date) throws IOException {
    if (date == null) {
      out.nullValue();
    } else {
      String value = FORMATTER.format(date.toInstant());
      out.value(value);
    }
  }

  @Override
  public Date read(JsonReader in) throws IOException {
    if (in.peek().equals(JsonToken.NULL)) {
      in.nextNull();
      return null;
    } else {
      String date = in.nextString();
      try {
        // Parse the ISO 8601 string directly to Instant
        Instant instant = ZonedDateTime.parse(date, FORMATTER).toInstant();
        return Date.from(instant);
      } catch (Exception e) {
        throw new JsonParseException("Failed to parse date: " + date, e);
      }
    }
  }
}
