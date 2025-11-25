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

package com.google.gson.internal.bind;

import static java.lang.Math.toIntExact;
import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.NumberLimits;
import com.google.gson.internal.TroubleshootingGuide;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Type adapters for basic types. More complex adapters exist as separate classes in the enclosing
 * package.
 */
public final class TypeAdapters {
  private TypeAdapters() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("rawtypes")
  public static final TypeAdapter<Class> CLASS =
      new TypeAdapter<Class>() {
        @Override
        public void write(JsonWriter out, Class value) throws IOException {
          throw new UnsupportedOperationException(
              "Attempted to serialize java.lang.Class: "
                  + value.getName()
                  + ". Forgot to register a type adapter?"
                  + "\nSee "
                  + TroubleshootingGuide.createUrl("java-lang-class-unsupported"));
        }

        @Override
        public Class read(JsonReader in) throws IOException {
          throw new UnsupportedOperationException(
              "Attempted to deserialize a java.lang.Class. Forgot to register a type adapter?"
                  + "\nSee "
                  + TroubleshootingGuide.createUrl("java-lang-class-unsupported"));
        }
      }.nullSafe();

  public static final TypeAdapterFactory CLASS_FACTORY = newFactory(Class.class, CLASS);

  public static final TypeAdapter<BitSet> BIT_SET =
      new TypeAdapter<BitSet>() {
        @Override
        public BitSet read(JsonReader in) throws IOException {
          BitSet bitset = new BitSet();
          in.beginArray();
          int i = 0;
          JsonToken tokenType = in.peek();
          while (tokenType != JsonToken.END_ARRAY) {
            boolean set;
            switch (tokenType) {
              case NUMBER:
              case STRING:
                int intValue = in.nextInt();
                if (intValue == 0) {
                  set = false;
                } else if (intValue == 1) {
                  set = true;
                } else {
                  throw new JsonSyntaxException(
                      "Invalid bitset value "
                          + intValue
                          + ", expected 0 or 1; at path "
                          + in.getPreviousPath());
                }
                break;
              case BOOLEAN:
                set = in.nextBoolean();
                break;
              default:
                throw new JsonSyntaxException(
                    "Invalid bitset value type: " + tokenType + "; at path " + in.getPath());
            }
            if (set) {
              bitset.set(i);
            }
            ++i;
            tokenType = in.peek();
          }
          in.endArray();
          return bitset;
        }

        @Override
        public void write(JsonWriter out, BitSet src) throws IOException {
          out.beginArray();
          for (int i = 0, length = src.length(); i < length; i++) {
            int value = src.get(i) ? 1 : 0;
            out.value(value);
          }
          out.endArray();
        }
      }.nullSafe();

  public static final TypeAdapterFactory BIT_SET_FACTORY = newFactory(BitSet.class, BIT_SET);

  public static final TypeAdapter<Boolean> BOOLEAN =
      new TypeAdapter<Boolean>() {
        @Override
        public Boolean read(JsonReader in) throws IOException {
          JsonToken peek = in.peek();
          if (peek == JsonToken.NULL) {
            in.nextNull();
            return null;
          } else if (peek == JsonToken.STRING) {
            // support strings for compatibility with GSON 1.7
            return Boolean.parseBoolean(in.nextString());
          }
          return in.nextBoolean();
        }

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
          out.value(value);
        }
      };

  /**
   * Writes a boolean as a string. Useful for map keys, where booleans aren't otherwise permitted.
   */
  public static final TypeAdapter<Boolean> BOOLEAN_AS_STRING =
      new TypeAdapter<Boolean>() {
        @Override
        public Boolean read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return Boolean.valueOf(in.nextString());
        }

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
          out.value(value == null ? "null" : value.toString());
        }
      };

  public static final TypeAdapterFactory BOOLEAN_FACTORY =
      newFactory(boolean.class, Boolean.class, BOOLEAN);

  public static final TypeAdapter<Number> BYTE =
      new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }

          int intValue;
          try {
            intValue = in.nextInt();
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
          }
          // Allow up to 255 to support unsigned values
          if (intValue > 255 || intValue < Byte.MIN_VALUE) {
            throw new JsonSyntaxException(
                "Lossy conversion from " + intValue + " to byte; at path " + in.getPreviousPath());
          }
          return (byte) intValue;
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
          if (value == null) {
            out.nullValue();
          } else {
            out.value(value.byteValue());
          }
        }
      };

  public static final TypeAdapterFactory BYTE_FACTORY = newFactory(byte.class, Byte.class, BYTE);

  public static final TypeAdapter<Number> SHORT =
      new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }

          int intValue;
          try {
            intValue = in.nextInt();
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
          }
          // Allow up to 65535 to support unsigned values
          if (intValue > 65535 || intValue < Short.MIN_VALUE) {
            throw new JsonSyntaxException(
                "Lossy conversion from " + intValue + " to short; at path " + in.getPreviousPath());
          }
          return (short) intValue;
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
          if (value == null) {
            out.nullValue();
          } else {
            out.value(value.shortValue());
          }
        }
      };

  public static final TypeAdapterFactory SHORT_FACTORY =
      newFactory(short.class, Short.class, SHORT);

  public static final TypeAdapter<Number> INTEGER =
      new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          try {
            return in.nextInt();
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
          }
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
          if (value == null) {
            out.nullValue();
          } else {
            out.value(value.intValue());
          }
        }
      };
  public static final TypeAdapterFactory INTEGER_FACTORY =
      newFactory(int.class, Integer.class, INTEGER);

  public static final TypeAdapter<AtomicInteger> ATOMIC_INTEGER =
      new TypeAdapter<AtomicInteger>() {
        @Override
        public AtomicInteger read(JsonReader in) throws IOException {
          try {
            return new AtomicInteger(in.nextInt());
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
          }
        }

        @Override
        public void write(JsonWriter out, AtomicInteger value) throws IOException {
          out.value(value.get());
        }
      }.nullSafe();
  public static final TypeAdapterFactory ATOMIC_INTEGER_FACTORY =
      newFactory(AtomicInteger.class, TypeAdapters.ATOMIC_INTEGER);

  public static final TypeAdapter<AtomicBoolean> ATOMIC_BOOLEAN =
      new TypeAdapter<AtomicBoolean>() {
        @Override
        public AtomicBoolean read(JsonReader in) throws IOException {
          return new AtomicBoolean(in.nextBoolean());
        }

        @Override
        public void write(JsonWriter out, AtomicBoolean value) throws IOException {
          out.value(value.get());
        }
      }.nullSafe();
  public static final TypeAdapterFactory ATOMIC_BOOLEAN_FACTORY =
      newFactory(AtomicBoolean.class, TypeAdapters.ATOMIC_BOOLEAN);

  public static final TypeAdapter<AtomicIntegerArray> ATOMIC_INTEGER_ARRAY =
      new TypeAdapter<AtomicIntegerArray>() {
        @Override
        public AtomicIntegerArray read(JsonReader in) throws IOException {
          List<Integer> list = new ArrayList<>();
          in.beginArray();
          while (in.hasNext()) {
            try {
              int integer = in.nextInt();
              list.add(integer);
            } catch (NumberFormatException e) {
              throw new JsonSyntaxException(e);
            }
          }
          in.endArray();
          int length = list.size();
          AtomicIntegerArray array = new AtomicIntegerArray(length);
          for (int i = 0; i < length; ++i) {
            array.set(i, list.get(i));
          }
          return array;
        }

        @Override
        public void write(JsonWriter out, AtomicIntegerArray value) throws IOException {
          out.beginArray();
          for (int i = 0, length = value.length(); i < length; i++) {
            out.value(value.get(i));
          }
          out.endArray();
        }
      }.nullSafe();
  public static final TypeAdapterFactory ATOMIC_INTEGER_ARRAY_FACTORY =
      newFactory(AtomicIntegerArray.class, TypeAdapters.ATOMIC_INTEGER_ARRAY);

  public static final TypeAdapter<Number> LONG =
      new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          try {
            return in.nextLong();
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
          }
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
          if (value == null) {
            out.nullValue();
          } else {
            out.value(value.longValue());
          }
        }
      };

  public static final TypeAdapter<Number> FLOAT =
      new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return (float) in.nextDouble();
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
          if (value == null) {
            out.nullValue();
          } else {
            // For backward compatibility don't call `JsonWriter.value(float)` because that method
            // has been newly added and not all custom JsonWriter implementations might override
            // it yet
            Number floatNumber = value instanceof Float ? value : value.floatValue();
            out.value(floatNumber);
          }
        }
      };

  public static final TypeAdapter<Number> DOUBLE =
      new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return in.nextDouble();
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
          if (value == null) {
            out.nullValue();
          } else {
            out.value(value.doubleValue());
          }
        }
      };

  public static final TypeAdapter<Character> CHARACTER =
      new TypeAdapter<Character>() {
        @Override
        public Character read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String str = in.nextString();
          if (str.length() != 1) {
            throw new JsonSyntaxException(
                "Expecting character, got: " + str + "; at " + in.getPreviousPath());
          }
          return str.charAt(0);
        }

        @Override
        public void write(JsonWriter out, Character value) throws IOException {
          out.value(value == null ? null : String.valueOf(value));
        }
      };

  public static final TypeAdapterFactory CHARACTER_FACTORY =
      newFactory(char.class, Character.class, CHARACTER);

  public static final TypeAdapter<String> STRING =
      new TypeAdapter<String>() {
        @Override
        public String read(JsonReader in) throws IOException {
          JsonToken peek = in.peek();
          if (peek == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          /* coerce booleans to strings for backwards compatibility */
          if (peek == JsonToken.BOOLEAN) {
            return Boolean.toString(in.nextBoolean());
          }
          return in.nextString();
        }

        @Override
        public void write(JsonWriter out, String value) throws IOException {
          out.value(value);
        }
      };

  public static final TypeAdapter<BigDecimal> BIG_DECIMAL =
      new TypeAdapter<BigDecimal>() {
        @Override
        public BigDecimal read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String s = in.nextString();
          try {
            return NumberLimits.parseBigDecimal(s);
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(
                "Failed parsing '" + s + "' as BigDecimal; at path " + in.getPreviousPath(), e);
          }
        }

        @Override
        public void write(JsonWriter out, BigDecimal value) throws IOException {
          out.value(value);
        }
      };

  public static final TypeAdapter<BigInteger> BIG_INTEGER =
      new TypeAdapter<BigInteger>() {
        @Override
        public BigInteger read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String s = in.nextString();
          try {
            return NumberLimits.parseBigInteger(s);
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(
                "Failed parsing '" + s + "' as BigInteger; at path " + in.getPreviousPath(), e);
          }
        }

        @Override
        public void write(JsonWriter out, BigInteger value) throws IOException {
          out.value(value);
        }
      };

  public static final TypeAdapter<LazilyParsedNumber> LAZILY_PARSED_NUMBER =
      new TypeAdapter<LazilyParsedNumber>() {
        // Normally users should not be able to access and deserialize LazilyParsedNumber because
        // it is an internal type, but implement this nonetheless in case there are legit corner
        // cases where this is possible
        @Override
        public LazilyParsedNumber read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return new LazilyParsedNumber(in.nextString());
        }

        @Override
        public void write(JsonWriter out, LazilyParsedNumber value) throws IOException {
          out.value(value);
        }
      };

  public static final TypeAdapterFactory STRING_FACTORY = newFactory(String.class, STRING);

  public static final TypeAdapter<StringBuilder> STRING_BUILDER =
      new TypeAdapter<StringBuilder>() {
        @Override
        public StringBuilder read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return new StringBuilder(in.nextString());
        }

        @Override
        public void write(JsonWriter out, StringBuilder value) throws IOException {
          out.value(value == null ? null : value.toString());
        }
      };

  public static final TypeAdapterFactory STRING_BUILDER_FACTORY =
      newFactory(StringBuilder.class, STRING_BUILDER);

  public static final TypeAdapter<StringBuffer> STRING_BUFFER =
      new TypeAdapter<StringBuffer>() {
        @Override
        public StringBuffer read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return new StringBuffer(in.nextString());
        }

        @Override
        public void write(JsonWriter out, StringBuffer value) throws IOException {
          out.value(value == null ? null : value.toString());
        }
      };

  public static final TypeAdapterFactory STRING_BUFFER_FACTORY =
      newFactory(StringBuffer.class, STRING_BUFFER);

  public static final TypeAdapter<URL> URL =
      new TypeAdapter<URL>() {
        @Override
        public URL read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String nextString = in.nextString();
          return nextString.equals("null") ? null : new URL(nextString);
        }

        @Override
        public void write(JsonWriter out, URL value) throws IOException {
          out.value(value == null ? null : value.toExternalForm());
        }
      };

  public static final TypeAdapterFactory URL_FACTORY = newFactory(URL.class, URL);

  public static final TypeAdapter<URI> URI =
      new TypeAdapter<URI>() {
        @Override
        public URI read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          try {
            String nextString = in.nextString();
            return nextString.equals("null") ? null : new URI(nextString);
          } catch (URISyntaxException e) {
            throw new JsonIOException(e);
          }
        }

        @Override
        public void write(JsonWriter out, URI value) throws IOException {
          out.value(value == null ? null : value.toASCIIString());
        }
      };

  public static final TypeAdapterFactory URI_FACTORY = newFactory(URI.class, URI);

  public static final TypeAdapter<InetAddress> INET_ADDRESS =
      new TypeAdapter<InetAddress>() {
        @Override
        public InetAddress read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          // regrettably, this should have included both the host name and the host address
          // For compatibility, we use InetAddress.getByName rather than the possibly-better
          // .getAllByName
          @SuppressWarnings("AddressSelection")
          InetAddress addr = InetAddress.getByName(in.nextString());
          return addr;
        }

        @Override
        public void write(JsonWriter out, InetAddress value) throws IOException {
          out.value(value == null ? null : value.getHostAddress());
        }
      };

  public static final TypeAdapterFactory INET_ADDRESS_FACTORY =
      newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);

  public static final TypeAdapter<UUID> UUID =
      new TypeAdapter<UUID>() {
        @Override
        public UUID read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String s = in.nextString();
          try {
            return java.util.UUID.fromString(s);
          } catch (IllegalArgumentException e) {
            throw new JsonSyntaxException(
                "Failed parsing '" + s + "' as UUID; at path " + in.getPreviousPath(), e);
          }
        }

        @Override
        public void write(JsonWriter out, UUID value) throws IOException {
          out.value(value == null ? null : value.toString());
        }
      };

  public static final TypeAdapterFactory UUID_FACTORY = newFactory(UUID.class, UUID);

  public static final TypeAdapter<Currency> CURRENCY =
      new TypeAdapter<Currency>() {
        @Override
        public Currency read(JsonReader in) throws IOException {
          String s = in.nextString();
          try {
            return Currency.getInstance(s);
          } catch (IllegalArgumentException e) {
            throw new JsonSyntaxException(
                "Failed parsing '" + s + "' as Currency; at path " + in.getPreviousPath(), e);
          }
        }

        @Override
        public void write(JsonWriter out, Currency value) throws IOException {
          out.value(value.getCurrencyCode());
        }
      }.nullSafe();
  public static final TypeAdapterFactory CURRENCY_FACTORY = newFactory(Currency.class, CURRENCY);

  /**
   * An abstract {@link TypeAdapter} for classes whose JSON serialization consists of a fixed set of
   * integer fields. That is the case for the legacy serialization of various {@code java.time}
   * types.
   *
   * <p>This is a base class for {@link Calendar}, {@link Duration}, {@link Instant}, {@link
   * LocalDate}, {@link LocalTime}, {@link LocalDateTime}, and {@link ZoneOffset}.
   */
  private abstract static class IntegerFieldsTypeAdapter<T> extends TypeAdapter<T> {
    private final List<String> fields;

    IntegerFieldsTypeAdapter(String... fields) {
      this.fields = Arrays.asList(fields);
    }

    abstract T create(long[] values);

    abstract long[] integerValues(T t);

    @Override
    public T read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      in.beginObject();
      long[] values = new long[fields.size()];
      while (in.peek() != JsonToken.END_OBJECT) {
        String name = in.nextName();
        int index = fields.indexOf(name);
        if (index >= 0) {
          values[index] = in.nextLong();
        }
      }
      in.endObject();
      return create(values);
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.beginObject();
      long[] values = integerValues(value);
      for (int i = 0; i < fields.size(); i++) {
        out.name(fields.get(i));
        out.value(values[i]);
      }
      out.endObject();
    }
  }

  public static final TypeAdapter<Calendar> CALENDAR =
      new IntegerFieldsTypeAdapter<Calendar>(
          "year", "month", "dayOfMonth", "hourOfDay", "minute", "second") {

        @Override
        Calendar create(long[] values) {
          return new GregorianCalendar(
              toIntExact(values[0]),
              toIntExact(values[1]),
              toIntExact(values[2]),
              toIntExact(values[3]),
              toIntExact(values[4]),
              toIntExact(values[5]));
        }

        @Override
        long[] integerValues(Calendar calendar) {
          return new long[] {
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
          };
        }
      };

  public static final TypeAdapterFactory CALENDAR_FACTORY =
      newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, CALENDAR);

  public static final TypeAdapter<Duration> DURATION =
      new IntegerFieldsTypeAdapter<Duration>("seconds", "nanos") {
        @Override
        Duration create(long[] values) {
          return Duration.ofSeconds(values[0], values[1]);
        }

        @Override
        @SuppressWarnings("JavaDurationGetSecondsGetNano")
        long[] integerValues(Duration duration) {
          return new long[] {duration.getSeconds(), duration.getNano()};
        }
      };

  public static final TypeAdapter<Instant> INSTANT =
      new IntegerFieldsTypeAdapter<Instant>("seconds", "nanos") {
        @Override
        Instant create(long[] values) {
          return Instant.ofEpochSecond(values[0], values[1]);
        }

        @Override
        @SuppressWarnings("JavaInstantGetSecondsGetNano")
        long[] integerValues(Instant instant) {
          return new long[] {instant.getEpochSecond(), instant.getNano()};
        }
      };

  public static final TypeAdapter<LocalDate> LOCAL_DATE =
      new IntegerFieldsTypeAdapter<LocalDate>("year", "month", "day") {
        @Override
        LocalDate create(long[] values) {
          return LocalDate.of(toIntExact(values[0]), toIntExact(values[1]), toIntExact(values[2]));
        }

        @Override
        long[] integerValues(LocalDate localDate) {
          return new long[] {
            localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()
          };
        }
      };

  public static final TypeAdapter<LocalTime> LOCAL_TIME =
      new IntegerFieldsTypeAdapter<LocalTime>("hour", "minute", "second", "nano") {
        @Override
        LocalTime create(long[] values) {
          return LocalTime.of(
              toIntExact(values[0]),
              toIntExact(values[1]),
              toIntExact(values[2]),
              toIntExact(values[3]));
        }

        @Override
        long[] integerValues(LocalTime localTime) {
          return new long[] {
            localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano()
          };
        }
      };

  public static final TypeAdapter<LocalDateTime> LOCAL_DATE_TIME =
      new TypeAdapter<LocalDateTime>() {
        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          LocalDate localDate = null;
          LocalTime localTime = null;
          in.beginObject();
          while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            switch (name) {
              case "date":
                localDate = LOCAL_DATE.read(in);
                break;
              case "time":
                localTime = LOCAL_TIME.read(in);
                break;
              default:
                // Ignore other fields.
            }
          }
          in.endObject();
          return requireNonNull(localTime, "Missing time field")
              .atDate(requireNonNull(localDate, "Missing date field"));
        }

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          out.beginObject();
          out.name("date");
          LOCAL_DATE.write(out, value.toLocalDate());
          out.name("time");
          LOCAL_TIME.write(out, value.toLocalTime());
          out.endObject();
        }
      };

  public static final TypeAdapter<ZoneOffset> ZONE_OFFSET =
      new IntegerFieldsTypeAdapter<ZoneOffset>("totalSeconds") {
        @Override
        ZoneOffset create(long[] values) {
          return ZoneOffset.ofTotalSeconds(toIntExact(values[0]));
        }

        @Override
        long[] integerValues(ZoneOffset zoneOffset) {
          return new long[] {zoneOffset.getTotalSeconds()};
        }
      };

  // The ZoneRegion class is not public and is the other possible implementation of ZoneId alongside
  // ZoneOffset. If we have a ZoneId that is not a ZoneOffset, we assume it is a ZoneRegion.
  private static final TypeAdapter<ZoneId> ZONE_REGION =
      new TypeAdapter<ZoneId>() {
        @Override
        public ZoneId read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          in.beginObject();
          ZoneId zoneId = null;
          while (in.peek() != JsonToken.END_OBJECT) {
            if (in.nextName().equals("id")) {
              String id = in.nextString();
              try {
                zoneId = ZoneId.of(id);
              } catch (DateTimeException e) {
                throw new JsonSyntaxException(
                    "Failed parsing '" + id + "' as ZoneId; at path " + in.getPreviousPath(), e);
              }
            }
          }
          in.endObject();
          if (zoneId == null) {
            throw new JsonSyntaxException("Missing id field; at path " + in.getPreviousPath());
          }
          return zoneId;
        }

        @Override
        public void write(JsonWriter out, ZoneId value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          out.beginObject();
          out.name("id");
          out.value(value.getId());
          out.endObject();
        }
      };

  // TODO: this does not currently work correctly.
  public static final TypeAdapter<ZonedDateTime> ZONED_DATE_TIME =
      new TypeAdapter<ZonedDateTime>() {
        @Override
        public ZonedDateTime read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          in.beginObject();
          LocalDateTime localDateTime = null;
          ZoneOffset zoneOffset = null;
          ZoneId zoneId = null;
          while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            switch (name) {
              case "dateTime":
                localDateTime = LOCAL_DATE_TIME.read(in);
                break;
              case "offset":
                zoneOffset = ZONE_OFFSET.read(in);
                break;
              case "zone":
                zoneId = ZONE_REGION.read(in);
                break;
              default:
                // Ignore other fields.
            }
          }
          in.endObject();
          return ZonedDateTime.ofInstant(
              requireNonNull(localDateTime, "Missing dateTime field"),
              requireNonNull(zoneOffset, "Missing offset field"),
              requireNonNull(zoneId, "Missing zone field"));
        }

        @Override
        public void write(JsonWriter out, ZonedDateTime value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          out.beginObject();
          out.name("dateTime");
          LOCAL_DATE_TIME.write(out, value.toLocalDateTime());
          out.name("offset");
          ZONE_OFFSET.write(out, value.getOffset());
          out.name("zone");
          ZONE_REGION.write(out, value.getZone());
          out.endObject();
        }
      };

  public static final TypeAdapterFactory JAVA_TIME_FACTORY =
      new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
          Class<? super T> rawType = typeToken.getRawType();
          if (!rawType.getName().startsWith("java.time.")) {
            // Immediately return null so we don't load all these classes when nobody's doing
            // anything with java.time.
            return null;
          }
          TypeAdapter<?> adapter = null;
          if (rawType == Duration.class) {
            adapter = DURATION;
          } else if (rawType == Instant.class) {
            adapter = INSTANT;
          } else if (rawType == LocalDate.class) {
            adapter = LOCAL_DATE;
          } else if (rawType == LocalTime.class) {
            adapter = LOCAL_TIME;
          } else if (rawType == LocalDateTime.class) {
            adapter = LOCAL_DATE_TIME;
          } else if (rawType == ZoneOffset.class) {
            adapter = ZONE_OFFSET;
          } else if (ZoneId.class.isAssignableFrom(rawType)) {
            adapter = ZONE_REGION;
            // } else if (rawType == ZonedDateTime.class) {
            //   adapter = ZONED_DATE_TIME;
          }
          @SuppressWarnings("unchecked")
          TypeAdapter<T> result = (TypeAdapter<T>) adapter;
          return result;
        }
      };

  public static final TypeAdapter<Locale> LOCALE =
      new TypeAdapter<Locale>() {
        @Override
        public Locale read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String locale = in.nextString();
          StringTokenizer tokenizer = new StringTokenizer(locale, "_");
          String language = null;
          String country = null;
          String variant = null;
          if (tokenizer.hasMoreElements()) {
            language = tokenizer.nextToken();
          }
          if (tokenizer.hasMoreElements()) {
            country = tokenizer.nextToken();
          }
          if (tokenizer.hasMoreElements()) {
            variant = tokenizer.nextToken();
          }
          if (country == null && variant == null) {
            return new Locale(language);
          } else if (variant == null) {
            return new Locale(language, country);
          } else {
            return new Locale(language, country, variant);
          }
        }

        @Override
        public void write(JsonWriter out, Locale value) throws IOException {
          out.value(value == null ? null : value.toString());
        }
      };

  public static final TypeAdapterFactory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);

  public static final TypeAdapter<JsonElement> JSON_ELEMENT = JsonElementTypeAdapter.ADAPTER;

  public static final TypeAdapterFactory JSON_ELEMENT_FACTORY =
      newTypeHierarchyFactory(JsonElement.class, JSON_ELEMENT);

  public static final TypeAdapterFactory ENUM_FACTORY = EnumTypeAdapter.FACTORY;

  @SuppressWarnings("TypeParameterNaming")
  public static <TT> TypeAdapterFactory newFactory(
      TypeToken<TT> type, TypeAdapter<TT> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        return typeToken.equals(type) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  @SuppressWarnings("TypeParameterNaming")
  public static <TT> TypeAdapterFactory newFactory(Class<TT> type, TypeAdapter<TT> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        return typeToken.getRawType() == type ? (TypeAdapter<T>) typeAdapter : null;
      }

      @Override
      public String toString() {
        return "Factory[type=" + type.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }

  @SuppressWarnings("TypeParameterNaming")
  public static <TT> TypeAdapterFactory newFactory(
      Class<TT> unboxed, Class<TT> boxed, TypeAdapter<? super TT> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == unboxed || rawType == boxed) ? (TypeAdapter<T>) typeAdapter : null;
      }

      @Override
      public String toString() {
        return "Factory[type="
            + boxed.getName()
            + "+"
            + unboxed.getName()
            + ",adapter="
            + typeAdapter
            + "]";
      }
    };
  }

  @SuppressWarnings("TypeParameterNaming")
  public static <TT> TypeAdapterFactory newFactoryForMultipleTypes(
      Class<TT> base, Class<? extends TT> sub, TypeAdapter<? super TT> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == base || rawType == sub) ? (TypeAdapter<T>) typeAdapter : null;
      }

      @Override
      public String toString() {
        return "Factory[type="
            + base.getName()
            + "+"
            + sub.getName()
            + ",adapter="
            + typeAdapter
            + "]";
      }
    };
  }

  /**
   * Returns a factory for all subtypes of {@code typeAdapter}. We do a runtime check to confirm
   * that the deserialized type matches the type requested.
   */
  public static <T1> TypeAdapterFactory newTypeHierarchyFactory(
      Class<T1> clazz, TypeAdapter<T1> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      @Override
      public <T2> TypeAdapter<T2> create(Gson gson, TypeToken<T2> typeToken) {
        Class<? super T2> requestedType = typeToken.getRawType();
        if (!clazz.isAssignableFrom(requestedType)) {
          return null;
        }
        return (TypeAdapter<T2>)
            new TypeAdapter<T1>() {
              @Override
              public void write(JsonWriter out, T1 value) throws IOException {
                typeAdapter.write(out, value);
              }

              @Override
              public T1 read(JsonReader in) throws IOException {
                T1 result = typeAdapter.read(in);
                if (result != null && !requestedType.isInstance(result)) {
                  throw new JsonSyntaxException(
                      "Expected a "
                          + requestedType.getName()
                          + " but was "
                          + result.getClass().getName()
                          + "; at path "
                          + in.getPreviousPath());
                }
                return result;
              }
            };
      }

      @Override
      public String toString() {
        return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }
}
