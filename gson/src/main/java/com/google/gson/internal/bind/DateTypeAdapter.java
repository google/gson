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

package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for Date. Although this class appears stateless, it is not.
 * DateFormat captures its time zone and locale when it is created, which gives
 * this class state. DateFormat isn't thread safe either, so this class has
 * to synchronize its read and write methods.
 */
public final class DateTypeAdapter extends TypeAdapter<Date> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
    @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      return typeToken.getRawType() == Date.class ? (TypeAdapter<T>) new DateTypeAdapter() : null;
    }
  };

  private final DateFormat enUsFormat
      = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
  private final DateFormat localFormat
      = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);

  @Override public Date read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    return deserializeToDate(in.nextString());
  }

  private synchronized Date deserializeToDate(String json) {
    try {
      return localFormat.parse(json);
    } catch (ParseException ignored) {
    }
    try {
      return enUsFormat.parse(json);
    } catch (ParseException ignored) {
    }
    try {
    	return ISO8601Utils.parse(json, new ParsePosition(0));
    } catch (ParseException e) {
      throw new JsonSyntaxException(json, e);
    }
  }

  @Override public synchronized void write(JsonWriter out, Date value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    String dateFormatAsString = enUsFormat.format(value);
    out.value(dateFormatAsString);
  }
  
  
}
