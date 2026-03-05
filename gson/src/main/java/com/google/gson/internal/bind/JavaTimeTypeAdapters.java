package com.google.gson.internal.bind;

import static java.lang.Math.toIntExact;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters.IntegerFieldsTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Type adapters for {@code java.time} types.
 *
 * <p>These adapters mimic what {@link ReflectiveTypeAdapterFactory} would produce for the same
 * types. That is by no means a natural encoding, given that many of the types have standard ISO
 * representations. If Gson had added support for the types at the same time they appeared (in Java
 * 8, released in 2014), it would surely have used those representations. Unfortunately, in the
 * intervening time, people have been using the reflective representations, and changing that would
 * potentially be incompatible. Meanwhile, depending on the details of private fields in JDK classes
 * is obviously fragile, and it also needs special {@code --add-opens} configuration with more
 * recent JDK versions. So here we freeze the representation that was current with JDK 21, in a way
 * that does not use reflection.
 */
@IgnoreJRERequirement // Protected by a reflective check
final class JavaTimeTypeAdapters implements TypeAdapters.FactorySupplier {

  @Override
  public TypeAdapterFactory get() {
    return JAVA_TIME_FACTORY;
  }

  private static final TypeAdapter<Duration> DURATION =
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

  private static final TypeAdapter<Instant> INSTANT =
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

  private static final TypeAdapter<LocalDate> LOCAL_DATE =
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

  private static TypeAdapter<LocalDateTime> localDateTime(Gson gson) {
    TypeAdapter<LocalDate> localDateAdapter = gson.getAdapter(LocalDate.class);
    TypeAdapter<LocalTime> localTimeAdapter = gson.getAdapter(LocalTime.class);
    return new TypeAdapter<LocalDateTime>() {
      @Override
      public LocalDateTime read(JsonReader in) throws IOException {
        LocalDate localDate = null;
        LocalTime localTime = null;
        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
          String name = in.nextName();
          switch (name) {
            case "date":
              localDate = localDateAdapter.read(in);
              break;
            case "time":
              localTime = localTimeAdapter.read(in);
              break;
            default:
              // Ignore other fields.
              in.skipValue();
          }
        }
        in.endObject();
        return LocalDateTime.of(
            requireNonNullField(localDate, "date", in), requireNonNullField(localTime, "time", in));
      }

      @Override
      public void write(JsonWriter out, LocalDateTime value) throws IOException {
        out.beginObject();
        out.name("date");
        localDateAdapter.write(out, value.toLocalDate());
        out.name("time");
        localTimeAdapter.write(out, value.toLocalTime());
        out.endObject();
      }
    }.nullSafe();
  }

  private static final TypeAdapter<MonthDay> MONTH_DAY =
      new IntegerFieldsTypeAdapter<MonthDay>("month", "day") {
        @Override
        MonthDay create(long[] values) {
          return MonthDay.of(toIntExact(values[0]), toIntExact(values[1]));
        }

        @Override
        long[] integerValues(MonthDay monthDay) {
          return new long[] {monthDay.getMonthValue(), monthDay.getDayOfMonth()};
        }
      };

  private static TypeAdapter<OffsetDateTime> offsetDateTime(Gson gson) {
    TypeAdapter<LocalDateTime> localDateTimeAdapter = localDateTime(gson);
    TypeAdapter<ZoneOffset> zoneOffsetAdapter = gson.getAdapter(ZoneOffset.class);
    return new TypeAdapter<OffsetDateTime>() {
      @Override
      public OffsetDateTime read(JsonReader in) throws IOException {
        in.beginObject();
        LocalDateTime localDateTime = null;
        ZoneOffset zoneOffset = null;
        while (in.peek() != JsonToken.END_OBJECT) {
          String name = in.nextName();
          switch (name) {
            case "dateTime":
              localDateTime = localDateTimeAdapter.read(in);
              break;
            case "offset":
              zoneOffset = zoneOffsetAdapter.read(in);
              break;
            default:
              // Ignore other fields.
              in.skipValue();
          }
        }
        in.endObject();
        return OffsetDateTime.of(
            requireNonNullField(localDateTime, "dateTime", in),
            requireNonNullField(zoneOffset, "offset", in));
      }

      @Override
      public void write(JsonWriter out, OffsetDateTime value) throws IOException {
        out.beginObject();
        out.name("dateTime");
        localDateTimeAdapter.write(out, value.toLocalDateTime());
        out.name("offset");
        zoneOffsetAdapter.write(out, value.getOffset());
        out.endObject();
      }
    }.nullSafe();
  }

  private static TypeAdapter<OffsetTime> offsetTime(Gson gson) {
    TypeAdapter<LocalTime> localTimeAdapter = gson.getAdapter(LocalTime.class);
    TypeAdapter<ZoneOffset> zoneOffsetAdapter = gson.getAdapter(ZoneOffset.class);
    return new TypeAdapter<OffsetTime>() {
      @Override
      public OffsetTime read(JsonReader in) throws IOException {
        in.beginObject();
        LocalTime localTime = null;
        ZoneOffset zoneOffset = null;
        while (in.peek() != JsonToken.END_OBJECT) {
          String name = in.nextName();
          switch (name) {
            case "time":
              localTime = localTimeAdapter.read(in);
              break;
            case "offset":
              zoneOffset = zoneOffsetAdapter.read(in);
              break;
            default:
              // Ignore other fields.
              in.skipValue();
          }
        }
        in.endObject();
        return OffsetTime.of(
            requireNonNullField(localTime, "time", in),
            requireNonNullField(zoneOffset, "offset", in));
      }

      @Override
      public void write(JsonWriter out, OffsetTime value) throws IOException {
        out.beginObject();
        out.name("time");
        localTimeAdapter.write(out, value.toLocalTime());
        out.name("offset");
        zoneOffsetAdapter.write(out, value.getOffset());
        out.endObject();
      }
    }.nullSafe();
  }

  private static final TypeAdapter<Period> PERIOD =
      new IntegerFieldsTypeAdapter<Period>("years", "months", "days") {
        @Override
        Period create(long[] values) {
          return Period.of(toIntExact(values[0]), toIntExact(values[1]), toIntExact(values[2]));
        }

        @Override
        long[] integerValues(Period period) {
          return new long[] {period.getYears(), period.getMonths(), period.getDays()};
        }
      };

  private static final TypeAdapter<Year> YEAR =
      new IntegerFieldsTypeAdapter<Year>("year") {
        @Override
        Year create(long[] values) {
          return Year.of(toIntExact(values[0]));
        }

        @Override
        long[] integerValues(Year year) {
          return new long[] {year.getValue()};
        }
      };

  private static final TypeAdapter<YearMonth> YEAR_MONTH =
      new IntegerFieldsTypeAdapter<YearMonth>("year", "month") {
        @Override
        YearMonth create(long[] values) {
          return YearMonth.of(toIntExact(values[0]), toIntExact(values[1]));
        }

        @Override
        long[] integerValues(YearMonth yearMonth) {
          return new long[] {yearMonth.getYear(), yearMonth.getMonthValue()};
        }
      };

  // A ZoneId is either a ZoneOffset or a ZoneRegion, where ZoneOffset is public and ZoneRegion is
  // not. For compatibility with reflection-based serialization, we need to write the "id" field of
  // ZoneRegion if we have a ZoneRegion, and we need to write the "totalSeconds" field of ZoneOffset
  // if we have a ZoneOffset. When reading, we need to construct the the appropriate thing depending
  // on which of those two fields we see.
  private static final TypeAdapter<ZoneId> ZONE_ID =
      new TypeAdapter<ZoneId>() {
        @Override
        public ZoneId read(JsonReader in) throws IOException {
          in.beginObject();
          String id = null;
          Integer totalSeconds = null;
          while (in.peek() != JsonToken.END_OBJECT) {
            String name = in.nextName();
            switch (name) {
              case "id":
                id = in.nextString();
                break;
              case "totalSeconds":
                totalSeconds = in.nextInt();
                break;
              default:
                // Ignore other fields.
                in.skipValue();
            }
          }
          in.endObject();
          if (id != null) {
            return ZoneId.of(id);
          } else if (totalSeconds != null) {
            return ZoneOffset.ofTotalSeconds(totalSeconds);
          } else {
            throw new JsonSyntaxException(
                "Missing id or totalSeconds field; at path " + in.getPreviousPath());
          }
        }

        @Override
        public void write(JsonWriter out, ZoneId value) throws IOException {
          if (value instanceof ZoneOffset) {
            out.beginObject();
            out.name("totalSeconds");
            out.value(((ZoneOffset) value).getTotalSeconds());
            out.endObject();
          } else {
            out.beginObject();
            out.name("id");
            out.value(value.getId());
            out.endObject();
          }
        }
      }.nullSafe();

  private static TypeAdapter<ZonedDateTime> zonedDateTime(Gson gson) {
    TypeAdapter<LocalDateTime> localDateTimeAdapter = localDateTime(gson);
    TypeAdapter<ZoneOffset> zoneOffsetAdapter = gson.getAdapter(ZoneOffset.class);
    TypeAdapter<ZoneId> zoneIdAdapter = gson.getAdapter(ZoneId.class);
    return new TypeAdapter<ZonedDateTime>() {
      @Override
      public ZonedDateTime read(JsonReader in) throws IOException {
        in.beginObject();
        LocalDateTime localDateTime = null;
        ZoneOffset zoneOffset = null;
        ZoneId zoneId = null;
        while (in.peek() != JsonToken.END_OBJECT) {
          String name = in.nextName();
          switch (name) {
            case "dateTime":
              localDateTime = localDateTimeAdapter.read(in);
              break;
            case "offset":
              zoneOffset = zoneOffsetAdapter.read(in);
              break;
            case "zone":
              zoneId = zoneIdAdapter.read(in);
              break;
            default:
              // Ignore other fields.
              in.skipValue();
          }
        }
        in.endObject();
        return ZonedDateTime.ofInstant(
            requireNonNullField(localDateTime, "dateTime", in),
            requireNonNullField(zoneOffset, "offset", in),
            requireNonNullField(zoneId, "zone", in));
      }

      @Override
      public void write(JsonWriter out, ZonedDateTime value) throws IOException {
        if (value == null) {
          out.nullValue();
          return;
        }
        out.beginObject();
        out.name("dateTime");
        localDateTimeAdapter.write(out, value.toLocalDateTime());
        out.name("offset");
        zoneOffsetAdapter.write(out, value.getOffset());
        out.name("zone");
        zoneIdAdapter.write(out, value.getZone());
        out.endObject();
      }
    }.nullSafe();
  }

  static final TypeAdapterFactory JAVA_TIME_FACTORY =
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
            adapter = localDateTime(gson);
          } else if (rawType == MonthDay.class) {
            adapter = MONTH_DAY;
          } else if (rawType == OffsetDateTime.class) {
            adapter = offsetDateTime(gson);
          } else if (rawType == OffsetTime.class) {
            adapter = offsetTime(gson);
          } else if (rawType == Period.class) {
            adapter = PERIOD;
          } else if (rawType == Year.class) {
            adapter = YEAR;
          } else if (rawType == YearMonth.class) {
            adapter = YEAR_MONTH;
          } else if (rawType == ZoneId.class || rawType == ZoneOffset.class) {
            // We don't check ZoneId.class.isAssignableFrom(rawType) because we don't want to match
            // the non-public class ZoneRegion in the runtime type check in
            // TypeAdapterRuntimeTypeWrapper.write. If we did, then our ZONE_ID would take
            // precedence over a ZoneId adapter that the user might have registered. (This exact
            // situation showed up in a Google-internal test.)
            adapter = ZONE_ID;
          } else if (rawType == ZonedDateTime.class) {
            adapter = zonedDateTime(gson);
          }
          @SuppressWarnings("unchecked")
          TypeAdapter<T> result = (TypeAdapter<T>) adapter;
          return result;
        }
      };

  private static <T> T requireNonNullField(T field, String fieldName, JsonReader reader) {
    if (field == null) {
      throw new JsonSyntaxException(
          "Missing " + fieldName + " field; at path " + reader.getPreviousPath());
    }
    return field;
  }
}
