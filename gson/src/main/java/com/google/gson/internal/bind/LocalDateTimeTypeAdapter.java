package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.util.ISO8601Util;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * Adapter for <code>java.time.LocalDateTime</code>.
 *
 */
public final class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      return type.getRawType() == LocalDateTime.class ? (TypeAdapter<T>) new LocalDateTimeTypeAdapter() : null;
    }
  };

  @Override
  public LocalDateTime read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return null;
    }
    try {
      return ISO8601Util.toLocalDateTime(reader.nextString());
    } catch (ParseException e) {
      throw new IOException("Unable to convert value into LocalDateTime.", e);
    }
  }

  @Override
  public void write(JsonWriter writer, LocalDateTime value) throws IOException {
    if (value == null) {
      writer.nullValue();
      return;
    }
    writer.value(ISO8601Util.fromLocalDateTime(value));
  }
}