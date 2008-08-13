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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A default type adapter for a {@link Date} object.
 *
 * @author Joel Leitch
 */
class DefaultDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
  static final DefaultDateTypeAdapter DEFAULT_TYPE_ADAPTER =
      new DefaultDateTypeAdapter(DateFormat.DEFAULT);

  private final DateFormat format;

  public DefaultDateTypeAdapter(String datePattern) {
    this.format = new SimpleDateFormat(datePattern);
  }

  public DefaultDateTypeAdapter(int style) {
    this.format = DateFormat.getDateInstance(style);
  }

  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
    String dateFormatAsString = format.format(src);
    return new JsonPrimitive(dateFormatAsString);
  }

  public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (!(json instanceof JsonPrimitive)) {
      throw new JsonParseException("The date should be a string value");
    }

    try {
      return format.parse(json.getAsString());
    } catch (ParseException e) {
      throw new JsonParseException(e);
    }
  }
}
