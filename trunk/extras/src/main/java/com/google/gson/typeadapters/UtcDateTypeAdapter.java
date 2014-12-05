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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class UtcDateTypeAdapter extends TypeAdapter<Date> {
  private final DateFormat iso8601Format;

  public UtcDateTypeAdapter() {
    this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
    this.iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
  // See issue 162
  @Override
  public synchronized void write(JsonWriter out, Date date) throws IOException {
    if (date == null) {
      out.nullValue();
    }
    out.value(iso8601Format.format(date));
  }

  // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
  // See issue 162
  @Override
  public synchronized Date read(JsonReader in) throws IOException {
    try {
      switch (in.peek()) {
      case NULL: return null;
      default: return iso8601Format.parse(in.nextString());
      }
    } catch (ParseException e) {
      throw new JsonParseException(e);
    }
  }
}
