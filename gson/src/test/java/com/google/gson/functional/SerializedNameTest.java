/*
 * Copyright (C) 2015 Google Inc.
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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.junit.Test;

public final class SerializedNameTest {
  private final Gson gson = new Gson();

  @Test
  public void testFirstNameIsChosenForSerialization() {
    MyClass target = new MyClass("v1", "v2");
    // Ensure name1 occurs exactly once, and name2 and name3 don't appear
    assertThat(gson.toJson(target)).isEqualTo("{\"name\":\"v1\",\"name1\":\"v2\"}");
  }

  @Test
  public void testMultipleNamesDeserializedCorrectly() {
    assertThat(gson.fromJson("{'name':'v1'}", MyClass.class).a).isEqualTo("v1");

    // Both name1 and name2 gets deserialized to b
    assertThat(gson.fromJson("{'name1':'v11'}", MyClass.class).b).isEqualTo("v11");
    assertThat(gson.fromJson("{'name2':'v2'}", MyClass.class).b).isEqualTo("v2");
    assertThat(gson.fromJson("{'name3':'v3'}", MyClass.class).b).isEqualTo("v3");
  }

  @Test
  public void testMultipleNamesInTheSameString() {
    // The last value takes precedence
    assertThat(gson.fromJson("{'name1':'v1','name2':'v2','name3':'v3'}", MyClass.class).b).isEqualTo("v3");
  }

  private static final class MyClass {
    @SerializedName("name") String a;
    @SerializedName(value="name1", alternate={"name2", "name3"}) String b;
    MyClass(String a, String b) {
      this.a = a;
      this.b = b;
    }
  }
}
