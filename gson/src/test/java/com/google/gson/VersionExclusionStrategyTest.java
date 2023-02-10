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

import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.gson.internal.Excluder;
import org.junit.Test;

/**
 * Unit tests for the {@link Excluder} class.
 *
 * @author Joel Leitch
 */
public class VersionExclusionStrategyTest {
  private static final double VERSION = 5.0D;

  @Test
  public void testSameVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION);
    assertThat(excluder.excludeClass(MockClassSince.class, true)).isFalse();
    assertThat(excluder.excludeField(MockClassSince.class.getField("someField"), true)).isFalse();

    // Until version is exclusive
    assertThat(excluder.excludeClass(MockClassUntil.class, true)).isTrue();
    assertThat(excluder.excludeField(MockClassUntil.class.getField("someField"), true)).isTrue();

    assertThat(excluder.excludeClass(MockClassBoth.class, true)).isFalse();
    assertThat(excluder.excludeField(MockClassBoth.class.getField("someField"), true)).isFalse();
  }

  @Test
  public void testNewerVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION + 5);
    assertThat(excluder.excludeClass(MockClassSince.class, true)).isFalse();
    assertThat(excluder.excludeField(MockClassSince.class.getField("someField"), true)).isFalse();

    assertThat(excluder.excludeClass(MockClassUntil.class, true)).isTrue();
    assertThat(excluder.excludeField(MockClassUntil.class.getField("someField"), true)).isTrue();

    assertThat(excluder.excludeClass(MockClassBoth.class, true)).isTrue();
    assertThat(excluder.excludeField(MockClassBoth.class.getField("someField"), true)).isTrue();
  }

  @Test
  public void testOlderVersion() throws Exception {
    Excluder excluder = Excluder.DEFAULT.withVersion(VERSION - 5);
    assertThat(excluder.excludeClass(MockClassSince.class, true)).isTrue();
    assertThat(excluder.excludeField(MockClassSince.class.getField("someField"), true)).isTrue();

    assertThat(excluder.excludeClass(MockClassUntil.class, true)).isFalse();
    assertThat(excluder.excludeField(MockClassUntil.class.getField("someField"), true)).isFalse();

    assertThat(excluder.excludeClass(MockClassBoth.class, true)).isTrue();
    assertThat(excluder.excludeField(MockClassBoth.class.getField("someField"), true)).isTrue();
  }

  @Since(VERSION)
  private static class MockClassSince {

    @Since(VERSION)
    public final int someField = 0;
  }

  @Until(VERSION)
  private static class MockClassUntil {

    @Until(VERSION)
    public final int someField = 0;
  }

  @Since(VERSION)
  @Until(VERSION + 2)
  private static class MockClassBoth {

    @Since(VERSION)
    @Until(VERSION + 2)
    public final int someField = 0;
  }
}
