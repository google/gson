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

import static com.google.common.truth.Truth.assertThat;

import com.google.errorprone.annotations.Keep;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.gson.internal.Excluder;
import java.lang.reflect.Field;
import org.junit.Test;

/**
 * Unit tests for the {@link Excluder} class.
 *
 * @author Joel Leitch
 */
public class VersionExclusionStrategyTest {
  private static final double VERSION = 5.0D;

  private static void assertIncludesClass(Excluder excluder, Class<?> c) {
    assertThat(excluder.excludeClass(c, true)).isFalse();
    assertThat(excluder.excludeClass(c, false)).isFalse();
  }

  private static void assertExcludesClass(Excluder excluder, Class<?> c) {
    assertThat(excluder.excludeClass(c, true)).isTrue();
    assertThat(excluder.excludeClass(c, false)).isTrue();
  }

  private static void assertIncludesField(Excluder excluder, Field f) {
    assertThat(excluder.excludeField(f, true)).isFalse();
    assertThat(excluder.excludeField(f, false)).isFalse();
  }

  private static void assertExcludesField(Excluder excluder, Field f) {
    assertThat(excluder.excludeField(f, true)).isTrue();
    assertThat(excluder.excludeField(f, false)).isTrue();
  }

  @Test
  public void testSameVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION);
    assertIncludesClass(excluder, MockClassSince.class);
    assertIncludesField(excluder, MockClassSince.class.getField("someField"));

    // Until version is exclusive
    assertExcludesClass(excluder, MockClassUntil.class);
    assertExcludesField(excluder, MockClassUntil.class.getField("someField"));

    assertIncludesClass(excluder, MockClassBoth.class);
    assertIncludesField(excluder, MockClassBoth.class.getField("someField"));
  }

  @Test
  public void testNewerVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION + 5);
    assertIncludesClass(excluder, MockClassSince.class);
    assertIncludesField(excluder, MockClassSince.class.getField("someField"));

    assertExcludesClass(excluder, MockClassUntil.class);
    assertExcludesField(excluder, MockClassUntil.class.getField("someField"));

    assertExcludesClass(excluder, MockClassBoth.class);
    assertExcludesField(excluder, MockClassBoth.class.getField("someField"));
  }

  @Test
  public void testOlderVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION - 5);
    assertExcludesClass(excluder, MockClassSince.class);
    assertExcludesField(excluder, MockClassSince.class.getField("someField"));

    assertIncludesClass(excluder, MockClassUntil.class);
    assertIncludesField(excluder, MockClassUntil.class.getField("someField"));

    assertExcludesClass(excluder, MockClassBoth.class);
    assertExcludesField(excluder, MockClassBoth.class.getField("someField"));
  }

  @Since(VERSION)
  private static class MockClassSince {

    @Since(VERSION)
    @Keep
    public final int someField = 0;
  }

  @Until(VERSION)
  private static class MockClassUntil {

    @Until(VERSION)
    @Keep
    public final int someField = 0;
  }

  @Since(VERSION)
  @Until(VERSION + 2)
  private static class MockClassBoth {

    @Since(VERSION)
    @Until(VERSION + 2)
    @Keep
    public final int someField = 0;
  }
}
