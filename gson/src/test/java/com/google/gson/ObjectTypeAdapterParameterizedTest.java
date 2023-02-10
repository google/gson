/*
 * Copyright (C) 2022 Google Inc.
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

import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ObjectTypeAdapterParameterizedTest {
  @Parameters
  public static Iterable<String> data() {
    return Arrays.asList(
      "[]",
      "{}",
      "null",
      "1.0",
      "true",
      "\"string\"",
      "[true,1.0,null,{},2.0,{\"a\":[false]},[3.0,\"test\"],4.0]",
      "{\"\":1.0,\"a\":true,\"b\":null,\"c\":[],\"d\":{\"a1\":2.0,\"b2\":[true,{\"a3\":3.0}]},\"e\":[{\"f\":4.0},\"test\"]}"
    );
  }

  private final TypeAdapter<Object> adapter = new Gson().getAdapter(Object.class);
  @Parameter
  public String json;

  @Test
  public void testReadWrite() throws IOException {
    Object deserialized = adapter.fromJson(json);
    String actualSerialized = adapter.toJson(deserialized);

    // Serialized Object should be the same as original JSON
    assertThat(actualSerialized).isEqualTo(json);
  }
}
