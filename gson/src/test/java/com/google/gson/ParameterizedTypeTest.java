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

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@code ParameterizedType}s created by the {@link $Gson$Types} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ParameterizedTypeTest {
  private ParameterizedType ourType;

  @Before
  public void setUp() throws Exception {
    ourType = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
  }

  @Test
  public void testOurTypeFunctionality() {
    Type parameterizedType = new TypeToken<List<String>>() {}.getType();
    assertThat(ourType.getOwnerType()).isNull();
    assertThat(ourType.getActualTypeArguments()[0]).isSameInstanceAs(String.class);
    assertThat(ourType.getRawType()).isSameInstanceAs(List.class);
    assertThat(ourType).isEqualTo(parameterizedType);
    assertThat(ourType.hashCode()).isEqualTo(parameterizedType.hashCode());
  }

  @Test
  public void testNotEquals() {
    Type differentParameterizedType = new TypeToken<List<Integer>>() {}.getType();
    assertThat(differentParameterizedType.equals(ourType)).isFalse();
    assertThat(ourType.equals(differentParameterizedType)).isFalse();
  }
}
