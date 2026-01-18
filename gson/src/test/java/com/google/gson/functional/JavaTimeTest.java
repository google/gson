/*
 * Copyright (C) 2026 Google Inc.
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
package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@code java.time} classes.
 *
 * <p>If reflective access to JDK classes is possible, this test also verifies that the custom
 * adapters behave identically to the reflection-based approach (to ensure backward compatibility),
 * see {@link #JAVA_TIME_FIELDS_ARE_ACCESSIBLE}.
 */
public class JavaTimeTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testNullSafe() {
    assertNullSafe(DayOfWeek.class); // uses standard enum adapter
    assertNullSafe(Duration.class);
    assertNullSafe(Instant.class);
    assertNullSafe(LocalDate.class);
    assertNullSafe(LocalTime.class);
    assertNullSafe(LocalDateTime.class);
    assertNullSafe(Month.class); // uses standard enum adapter
    assertNullSafe(MonthDay.class);
    assertNullSafe(Period.class);
    assertNullSafe(Year.class);
    assertNullSafe(YearMonth.class);
    assertNullSafe(ZoneId.class);
    assertNullSafe(ZonedDateTime.class);
  }

  private void assertNullSafe(Class<?> c) {
    DefaultTypeAdaptersTest.testNullSerializationAndDeserialization(gson, c);
  }

  // uses standard enum adapter
  @Test
  public void testDayOfWeek() {
    DayOfWeek day = DayOfWeek.TUESDAY;
    String json = "\"TUESDAY\"";
    assertThat(gson.toJson(day)).isEqualTo(json);
    assertThat(gson.fromJson(json, DayOfWeek.class)).isEqualTo(day);
  }

  @Test
  public void testDuration() {
    Duration duration = Duration.ofSeconds(123, 456_789_012);
    String json = "{\"seconds\":123,\"nanos\":456789012}";
    roundTrip(duration, json);
  }

  @Test
  public void testDurationWithUnknownFields() {
    Duration duration = Duration.ofSeconds(123, 456_789_012);
    String json = "{\"seconds\":123,\"nanos\":456789012,\"tiddly\":\"pom\",\"wibble\":\"wobble\"}";
    assertThat(gson.fromJson(json, Duration.class)).isEqualTo(duration);
  }

  @Test
  public void testInstant() {
    Instant instant = Instant.ofEpochSecond(123, 456_789_012);
    String json = "{\"seconds\":123,\"nanos\":456789012}";
    roundTrip(instant, json);
  }

  @Test
  public void testLocalDate() {
    LocalDate localDate = LocalDate.of(2021, 12, 2);
    String json = "{\"year\":2021,\"month\":12,\"day\":2}";
    roundTrip(localDate, json);
  }

  @Test
  public void testLocalTime() {
    LocalTime localTime = LocalTime.of(12, 34, 56, 789_012_345);
    String json = "{\"hour\":12,\"minute\":34,\"second\":56,\"nano\":789012345}";
    roundTrip(localTime, json);
  }

  @Test
  public void testLocalDateTime() {
    LocalDateTime localDateTime = LocalDateTime.of(2021, 12, 2, 12, 34, 56, 789_012_345);
    String json =
        "{\"date\":{\"year\":2021,\"month\":12,\"day\":2},"
            + "\"time\":{\"hour\":12,\"minute\":34,\"second\":56,\"nano\":789012345}}";
    roundTrip(localDateTime, json);
  }

  // uses standard enum adapter
  @Test
  public void testMonth() {
    Month month = Month.FEBRUARY;
    String json = "\"FEBRUARY\"";
    assertThat(gson.toJson(month)).isEqualTo(json);
    assertThat(gson.fromJson(json, Month.class)).isEqualTo(month);
  }

  @Test
  public void testMonthDay() {
    MonthDay monthDay = MonthDay.of(2, 17);
    String json = "{\"month\":2,\"day\":17}";
    roundTrip(monthDay, json);
  }

  @Test
  public void testOffsetDateTime() {
    OffsetDateTime offsetDateTime =
        OffsetDateTime.of(
            LocalDate.of(2021, 12, 2), LocalTime.of(12, 34, 56, 789_012_345), ZoneOffset.UTC);
    String json =
        "{\"dateTime\":{\"date\":{\"year\":2021,\"month\":12,\"day\":2},"
            + "\"time\":{\"hour\":12,\"minute\":34,\"second\":56,\"nano\":789012345}},"
            + "\"offset\":{\"totalSeconds\":0}}";
    roundTrip(offsetDateTime, json);
  }

  @Test
  public void testOffsetTime() {
    OffsetTime offsetTime = OffsetTime.of(LocalTime.of(12, 34, 56, 789_012_345), ZoneOffset.UTC);
    String json =
        "{\"time\":{\"hour\":12,\"minute\":34,\"second\":56,\"nano\":789012345},"
            + "\"offset\":{\"totalSeconds\":0}}";
    roundTrip(offsetTime, json);
  }

  @Test
  public void testPeriod() {
    Period period = Period.of(2025, 2, 3);
    String json = "{\"years\":2025,\"months\":2,\"days\":3}";
    roundTrip(period, json);
  }

  @Test
  public void testYear() {
    Year year = Year.of(2025);
    String json = "{\"year\":2025}";
    roundTrip(year, json);
  }

  @Test
  public void testYearMonth() {
    YearMonth yearMonth = YearMonth.of(2025, 2);
    String json = "{\"year\":2025,\"month\":2}";
    roundTrip(yearMonth, json);
  }

  @Test
  public void testZoneOffset() {
    ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(-8 * 60 * 60);
    String json = "{\"totalSeconds\":-28800}";
    roundTrip(zoneOffset, json);
  }

  @Test
  public void testZoneRegion() {
    ZoneId zoneId = ZoneId.of("Asia/Shanghai");
    String json = "{\"id\":\"Asia/Shanghai\"}";
    // Object class is actually the JDK-internal ZoneRegion, but request the ZoneId adapter here
    roundTrip(zoneId, ZoneId.class, json);
  }

  @Test
  public void testZonedDateTimeWithZoneOffset() {
    ZonedDateTime zonedDateTime =
        ZonedDateTime.of(
            LocalDate.of(2021, 12, 2), LocalTime.of(12, 34, 56, 789_012_345), ZoneOffset.UTC);
    String json =
        "{\"dateTime\":{\"date\":{\"year\":2021,\"month\":12,\"day\":2},"
            + "\"time\":{\"hour\":12,\"minute\":34,\"second\":56,\"nano\":789012345}},"
            + "\"offset\":{\"totalSeconds\":0},"
            + "\"zone\":{\"totalSeconds\":0}}";
    roundTrip(zonedDateTime, json);
  }

  @Test
  public void testZonedDateTimeWithZoneId() {
    ZoneId zoneId = ZoneId.of("UTC+01:00");
    int totalSeconds = ((ZoneOffset) zoneId.normalized()).getTotalSeconds();
    ZonedDateTime zonedDateTime =
        ZonedDateTime.of(LocalDate.of(2021, 12, 2), LocalTime.of(12, 34, 56, 789_012_345), zoneId);
    String json =
        "{\"dateTime\":{\"date\":{\"year\":2021,\"month\":12,\"day\":2},"
            + "\"time\":{\"hour\":12,\"minute\":34,\"second\":56,\"nano\":789012345}},"
            + "\"offset\":{\"totalSeconds\":"
            + totalSeconds
            + "},"
            + "\"zone\":{\"id\":\""
            + zoneId.getId()
            + "\"}}";
    roundTrip(zonedDateTime, json);
  }

  @Test
  public void testZonedDateTimeWithZoneIdThatHasAdapter() {
    TypeAdapter<ZoneId> zoneIdAdapter =
        new TypeAdapter<>() {
          @Override
          public void write(JsonWriter out, ZoneId value) throws IOException {
            out.value(value.getId());
          }

          @Override
          public ZoneId read(JsonReader in) throws IOException {
            return ZoneId.of(in.nextString());
          }
        };
    Gson customGson = new GsonBuilder().registerTypeAdapter(ZoneId.class, zoneIdAdapter).create();
    ZoneId zoneId = ZoneId.of("UTC+01:00");
    int totalSeconds = ((ZoneOffset) zoneId.normalized()).getTotalSeconds();
    ZonedDateTime zonedDateTime =
        ZonedDateTime.of(LocalDate.of(2021, 12, 2), LocalTime.of(12, 34, 56, 789_012_345), zoneId);
    String json =
        "{\"dateTime\":{\"date\":{\"year\":2021,\"month\":12,\"day\":2},"
            + "\"time\":{\"hour\":12,\"minute\":34,\"second\":56,\"nano\":789012345}},"
            + "\"offset\":{\"totalSeconds\":"
            + totalSeconds
            + "},"
            + "\"zone\":\""
            + zoneId.getId()
            + "\"}";
    roundTrip(customGson, zonedDateTime, ZonedDateTime.class, json);
  }

  /**
   * Verifies that custom adapters for {@code java.time} classes have higher precedence than
   * built-in ones.
   */
  @Test
  public void testCustomAdapter() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                Duration.class,
                new TypeAdapter<Duration>() {
                  @Override
                  public void write(JsonWriter out, Duration value) throws IOException {
                    out.value(value.toSeconds() * 3);
                  }

                  @Override
                  public Duration read(JsonReader in) throws IOException {
                    return Duration.ofSeconds(in.nextLong() / 3);
                  }
                })
            .create();

    assertThat(gson.toJson(Duration.ofSeconds(111))).isEqualTo("333");
    assertThat(gson.fromJson("333", Duration.class)).isEqualTo(Duration.ofSeconds(111));
  }

  /** Tests handling of {@code java.time} classes without a built-in adapter. */
  @Test
  public void testUnsupportedClass() {
    var e = assertThrows(JsonIOException.class, () -> gson.fromJson("{}", InstantSource.class));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo(
            "Interfaces can't be instantiated! Register an InstanceCreator or a TypeAdapter for"
                + " this type. Interface name: "
                + InstantSource.class.getName());
  }

  /** Whether fields of {@code java.time} classes are accessible through reflection. */
  private static final boolean JAVA_TIME_FIELDS_ARE_ACCESSIBLE;

  static {
    boolean accessible = false;
    try {
      Instant.class.getDeclaredField("seconds").setAccessible(true);
      accessible = true;
    } catch (InaccessibleObjectException e) {
      // OK: we can't reflect on java.time fields
    } catch (NoSuchFieldException e) {
      // JDK implementation has changed and we no longer have an Instant.seconds field.
      throw new AssertionError(e);
    }
    JAVA_TIME_FIELDS_ARE_ACCESSIBLE = accessible;

    // Print this to console to make troubleshooting Maven test execution easier
    debugPrint("java.time fields are accessible: " + JAVA_TIME_FIELDS_ARE_ACCESSIBLE);
  }

  @SuppressWarnings("SystemOut")
  private static void debugPrint(String s) {
    System.out.println(s);
  }

  private void roundTrip(Object value, String expectedJson) {
    roundTrip(value, value.getClass(), expectedJson);
  }

  private void roundTrip(Object value, Class<?> valueClass, String expectedJson) {
    roundTrip(gson, value, valueClass, expectedJson);
    if (JAVA_TIME_FIELDS_ARE_ACCESSIBLE) {
      checkReflectiveTypeAdapterFactory(value, expectedJson);
    }
  }

  private static void roundTrip(
      Gson customGson, Object value, Class<?> valueClass, String expectedJson) {
    assertUsesCustomAdapter(customGson, valueClass);
    assertThat(customGson.toJson(value, valueClass)).isEqualTo(expectedJson);
    assertThat(customGson.fromJson(expectedJson, valueClass)).isEqualTo(value);
  }

  private static void assertUsesCustomAdapter(Gson customGson, Class<?> valueClass) {
    Class<?> adapterClass = customGson.getAdapter(valueClass).getClass();
    assertThat(adapterClass).isNotInstanceOf(ReflectiveTypeAdapterFactory.Adapter.class);
    // To be safe also check the class name (in case the adapter factory has other nested adapter
    // classes as well)
    assertThat(adapterClass.getName()).doesNotContain("Reflective");
  }

  // Assuming we have reflective access to the fields of java.time classes, check that
  // ReflectiveTypeAdapterFactory would produce the same JSON. This ensures that we are preserving
  // a compatible JSON format for those classes even though we no longer use reflection.
  private void checkReflectiveTypeAdapterFactory(Object value, String expectedJson) {
    List<?> factories;
    try {
      Field factoriesField = gson.getClass().getDeclaredField("factories");
      factoriesField.setAccessible(true);
      factories = (List<?>) factoriesField.get(gson);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
    ReflectiveTypeAdapterFactory adapterFactory =
        factories.stream()
            .filter(f -> f instanceof ReflectiveTypeAdapterFactory)
            .map(f -> (ReflectiveTypeAdapterFactory) f)
            .findFirst()
            .orElseThrow();
    TypeToken<?> typeToken = TypeToken.get(value.getClass());
    @SuppressWarnings("unchecked")
    TypeAdapter<Object> adapter = (TypeAdapter<Object>) adapterFactory.create(gson, typeToken);
    assertThat(adapter).isNotNull();
    assertThat(adapter.toJson(value)).isEqualTo(expectedJson);
  }
}
