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
package com.google.gson.native_test;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** Test for (some) {@code java.time} classes. */
class JavaTimeTest {
  private final Gson gson = new Gson();

  @Test
  void testDuration() {
    Duration duration = Duration.ofSeconds(123, 456_789_012);
    String json = "{\"seconds\":123,\"nanos\":456789012}";
    roundTrip(duration, json);
  }

  @Test
  void testInstant() {
    Instant instant = Instant.ofEpochSecond(123, 456_789_012);
    String json = "{\"seconds\":123,\"nanos\":456789012}";
    roundTrip(instant, json);
  }

  @Test
  void testLocalDate() {
    LocalDate localDate = LocalDate.of(2021, 12, 2);
    String json = "{\"year\":2021,\"month\":12,\"day\":2}";
    roundTrip(localDate, json);
  }

  /**
   * Verifies that deserialization of {@code ZoneRegion} (JDK internal subclass of {@link ZoneId})
   * is possible.
   */
  @Test
  void testZoneRegion() {
    String json = "{\"id\":\"Asia/Shanghai\"}";
    assertThat(gson.fromJson(json, ZoneId.class).getId()).isEqualTo("Asia/Shanghai");
  }

  private void roundTrip(Object value, String json) {
    Class<?> valueClass = value.getClass();
    assertThat(gson.toJson(value, valueClass)).isEqualTo(json);
    assertThat(gson.fromJson(json, valueClass)).isEqualTo(value);
  }
}
