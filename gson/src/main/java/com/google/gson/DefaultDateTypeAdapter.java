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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;

import com.google.gson.internal.bind.dateformatter.DateFormatter;
import com.google.gson.internal.bind.dateformatter.ISO8601DateFormatter;
import com.google.gson.internal.bind.dateformatter.MillisDateFormatter;
import com.google.gson.internal.bind.dateformatter.SimpleDateFormatter;
import com.google.gson.internal.bind.dateformatter.UnixDateFormatter;

/**
 * This type adapter supports three subclasses of date: Date, Timestamp, and
 * java.sql.Date.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

  // TODO: migrate to streaming adapter
	
	private final EnumMap<DateFormatType, DateFormatter> dateFormatters;
	private final EnumSet<DateFormatType> dateParsersToUse;
	private final DateFormatType outputDateFormatType;

  DefaultDateTypeAdapter() {
    this(DateFormatType.EN_US);
  }
  
  DefaultDateTypeAdapter(DateFormatType outputFormatType) {
    this(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US),
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT),
        outputFormatType);
  }

  DefaultDateTypeAdapter(String datePattern) {
    this(datePattern,
    		DateFormatType.EN_US);
  }
  
  DefaultDateTypeAdapter(String datePattern, DateFormatType outputFormatType) {
    this(new SimpleDateFormat(datePattern, Locale.US),
    		new SimpleDateFormat(datePattern),
    		DateFormatType.EN_US);
  }
  
  DefaultDateTypeAdapter(DateFormat dateFormat, DateFormatType outputFormatType) {
  	this(outputFormatType);
    dateFormatters.put(DateFormatType.CUSTOM, new SimpleDateFormatter(dateFormat));
    dateParsersToUse.add(DateFormatType.CUSTOM);
  }
  
  DefaultDateTypeAdapter(DateFormat dateFormat) {
  	this(dateFormat, DateFormatType.EN_US);
  }

  DefaultDateTypeAdapter(int style) {
    this(DateFormat.getDateInstance(style, Locale.US),
    		DateFormat.getDateInstance(style),
    		DateFormatType.EN_US);
  }
  
  public DefaultDateTypeAdapter(int dateStyle, int timeStyle, DateFormatType outputFormatType) {
    this(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US),
        DateFormat.getDateTimeInstance(dateStyle, timeStyle),
        outputFormatType);
  }

  public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
    this(dateStyle,
    		timeStyle,
        DateFormatType.EN_US);
  }

  DefaultDateTypeAdapter(DateFormat enUsFormat, DateFormat localFormat, DateFormatType outputDateFormatType) {
  	
  	// Make sure every DateFormatType is present in dateFormats
  	dateFormatters = new EnumMap<DateFormatType, DateFormatter>(DateFormatType.class);
  	SimpleDateFormatter enUsFormatter = new SimpleDateFormatter(enUsFormat);
  	dateFormatters.put(DateFormatType.EN_US, enUsFormatter);
  	
  	// Set Custom and Default to EN-US to prevent null pointer
  	dateFormatters.put(DateFormatType.CUSTOM, enUsFormatter);
  	dateFormatters.put(DateFormatType.DEFAULT, enUsFormatter);
  	
  	dateFormatters.put(DateFormatType.LOCAL, new SimpleDateFormatter(localFormat));
  	dateFormatters.put(DateFormatType.ISO_8601, ISO8601DateFormatter.getInstance());
  	dateFormatters.put(DateFormatType.UNIX, UnixDateFormatter.getInstance());
  	dateFormatters.put(DateFormatType.MILLIS, MillisDateFormatter.getInstance());
  	
  	// Date type formatters to use. Prevents repeating parsing when Default or Custom are set to EN-US.
  	dateParsersToUse = EnumSet.of(DateFormatType.EN_US, DateFormatType.LOCAL, DateFormatType.ISO_8601);
  	
  	// Add date formatter for millis or unix. Millis as default.
  	DateFormatType formatForLong = outputDateFormatType == DateFormatType.UNIX ? outputDateFormatType : DateFormatType.MILLIS;
  	dateParsersToUse.add(formatForLong);
  	
  	// DateFormatter type to use for serialization
  	this.outputDateFormatType = outputDateFormatType;
  }

  @Override
  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
  	String dateFormatAsString = dateFormatters.get(outputDateFormatType).format(src);
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
  	
  	for(DateFormatType dateFormatType : dateParsersToUse )
  	{
  		DateFormatter dateFormatter = dateFormatters.get(dateFormatType);
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
    sb.append('(').append(dateFormatters.get(DateFormatType.LOCAL).toString()).append(')');
    return sb.toString();
  }
}
