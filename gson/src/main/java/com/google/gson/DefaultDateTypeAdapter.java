/*
 * Copyright (C) 2008 Google Inc.
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

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.internal.bind.util.ISO8601Utils;

/**
 * This type adapter supports three subclasses of date: Date, Timestamp, and
 * java.sql.Date.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

  // TODO: migrate to streaming adapter
	
	private final EnumMap<DateFormatType, DateFormatter> dateFormats;
	private final DateFormatType outputDateFormatType;
	
  private final DateFormat enUsFormat;
  private final DateFormat localFormat;

  DefaultDateTypeAdapter() {
    this(DateFormatType.EN_US);
  }
  
  DefaultDateTypeAdapter(DateFormatType outputFormat) {
    this(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US),
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT),
        outputFormat);
  }

  DefaultDateTypeAdapter(String datePattern) {
    this(new SimpleDateFormat(datePattern, Locale.US),
    		new SimpleDateFormat(datePattern),
    		DateFormatType.EN_US);
  }
  
  DefaultDateTypeAdapter(DateFormat dateFormat) {
  	this(DateFormatType.CUSTOM);
    dateFormats.put(DateFormatType.CUSTOM, new SimpleDateFormatter(dateFormat));
  }

  DefaultDateTypeAdapter(int style) {
    this(DateFormat.getDateInstance(style, Locale.US),
    		DateFormat.getDateInstance(style),
    		DateFormatType.EN_US);
  }

  public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
    this(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US),
        DateFormat.getDateTimeInstance(dateStyle, timeStyle),
        DateFormatType.EN_US);
  }

  DefaultDateTypeAdapter(DateFormat enUsFormat, DateFormat localFormat, DateFormatType outputFormat) {
  	dateFormats = new EnumMap<DateFormatType, DateFormatter>(DateFormatType.class);
  	SimpleDateFormatter usFormatter = new SimpleDateFormatter(enUsFormat);
  	dateFormats.put(DateFormatType.EN_US, usFormatter);
  	
  	// Set Custom to US to prevent null pointer
  	dateFormats.put(DateFormatType.CUSTOM, usFormatter);
  	dateFormats.put(DateFormatType.LOCAL, new SimpleDateFormatter(localFormat));
  	dateFormats.put(DateFormatType.ISO_8601, ISO8601DateFormater.getInstance());
  	
  	outputDateFormatType = outputFormat;
    this.enUsFormat = enUsFormat;
    this.localFormat = localFormat;
  }

  @Override
  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
  	String dateFormatAsString = dateFormats.get(outputDateFormatType).format(src);
  	return new JsonPrimitive(dateFormatAsString);
  }

  @Override
  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (!(json instanceof JsonPrimitive)) {
      throw new JsonParseException("The date should be a string value");
    }
    Date date = deserializeToDate(json);
    if (typeOfT == Date.class) {
      return date;
    } else if (typeOfT == Timestamp.class) {
      return new Timestamp(date.getTime());
    } else if (typeOfT == java.sql.Date.class) {
      return new java.sql.Date(date.getTime());
    } else {
      throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
    }
  }

  private Date deserializeToDate(JsonElement json) {
  	String jsonString = json.getAsString();
  	
  	ParseException parseExc = null; // Hopefully will not be used
  	
  	for(DateFormatter dateFormatter : dateFormats.values() )
  	{
  		try{
  			return dateFormatter.parse(jsonString);
  		} catch (ParseException e) {
  			parseExc = e;
  		}
  	}
  	
  	throw new JsonSyntaxException(jsonString, parseExc);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(DefaultDateTypeAdapter.class.getSimpleName());
    sb.append('(').append(localFormat.getClass().getSimpleName()).append(')');
    return sb.toString();
  }
}
