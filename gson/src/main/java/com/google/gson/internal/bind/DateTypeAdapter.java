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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;

import com.google.gson.DateFormatType;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.dateformatter.DateFormatter;
import com.google.gson.internal.bind.dateformatter.ISO8601DateFormatter;
import com.google.gson.internal.bind.dateformatter.MillisDateFormatter;
import com.google.gson.internal.bind.dateformatter.SimpleDateFormatter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

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
  
  private final EnumMap<DateFormatType, DateFormatter> dateFormatters;
	private final EnumSet<DateFormatType> dateFormatTypesToUse;
  
	public DateTypeAdapter() {
		dateFormatters = new EnumMap<DateFormatType, DateFormatter>(DateFormatType.class);

		DateFormat enUsFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
		SimpleDateFormatter enUsFormatter = new SimpleDateFormatter(enUsFormat);
		dateFormatters.put(DateFormatType.EN_US, enUsFormatter);

		DateFormat localFormat = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
		dateFormatters.put(DateFormatType.LOCAL, new SimpleDateFormatter(localFormat));

		dateFormatters.put(DateFormatType.ISO_8601, ISO8601DateFormatter.getInstance());
		dateFormatters.put(DateFormatType.MILLIS, MillisDateFormatter.getInstance());

		// Date type formatters to use. Prevents repeating parsing when Default or
		// Custom are set to EN-US.
		dateFormatTypesToUse = EnumSet.of(DateFormatType.EN_US, DateFormatType.LOCAL, DateFormatType.ISO_8601,
				DateFormatType.MILLIS);
	}

  @Override public Date read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    return deserializeToDate(in.nextString());
  }

	private Date deserializeToDate(String json) {
		ParseException parseExc = null; // Hopefully will not be used

		for (DateFormatType dateFormatType : dateFormatTypesToUse) {
			DateFormatter dateFormatter = dateFormatters.get(dateFormatType);
			try {
				return dateFormatter.parse(json);
			} catch (ParseException e) {
				parseExc = e;
			}
		}

		throw new JsonSyntaxException(json, parseExc);
	}

  @Override public void write(JsonWriter out, Date value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    String dateFormatAsString = dateFormatters.get(DateFormatType.EN_US).format(value);
    out.value(dateFormatAsString);
  }
  
  
}
