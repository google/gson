/*
 * Copyright (C) 2011 Google Inc.
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

import com.google.gson.common.MoreAsserts;
import org.junit.Test;

/**
 * @author Jesse Wilson
 */
public final class JsonNullTest {

  @SuppressWarnings("deprecation")
  @Test
  public void testEqualsAndHashcode() {
    MoreAsserts.assertEqualsAndHashCode(new JsonNull(), new JsonNull());
    MoreAsserts.assertEqualsAndHashCode(new JsonNull(), JsonNull.INSTANCE);
    MoreAsserts.assertEqualsAndHashCode(JsonNull.INSTANCE, JsonNull.INSTANCE);
  }

  @Test
  public void testDeepCopy() {
    @SuppressWarnings("deprecation")
    JsonNull a = new JsonNull();
    assertThat(a.deepCopy()).isSameInstanceAs(JsonNull.INSTANCE);
    assertThat(JsonNull.INSTANCE.deepCopy()).isSameInstanceAs(JsonNull.INSTANCE);
  }
}
