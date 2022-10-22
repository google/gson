/*
 * Copyright (C) 2022 Google Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class Java17RecordTest {
  private final Gson gson = new Gson();

  @Test
  public void testFirstNameIsChosenForSerialization() {
    MyRecord target = new MyRecord("v1", "v2");
    // Ensure name1 occurs exactly once, and name2 and name3 don't appear
    assertEquals("{\"name\":\"modified-v1\",\"name1\":\"v2\"}", gson.toJson(target));
  }

  @Test
  public void testMultipleNamesDeserializedCorrectly() {
    assertEquals("modified-v1", gson.fromJson("{'name':'v1'}", MyRecord.class).a);

    // Both name1 and name2 gets deserialized to b
    assertEquals("v11", gson.fromJson("{'name': 'v1', 'name1':'v11'}", MyRecord.class).b);
    assertEquals("v2", gson.fromJson("{'name': 'v1', 'name2':'v2'}", MyRecord.class).b);
    assertEquals("v3", gson.fromJson("{'name': 'v1', 'name3':'v3'}", MyRecord.class).b);
  }

  @Test
  public void testMultipleNamesInTheSameString() {
    // The last value takes precedence
    assertEquals("v3",
        gson.fromJson("{'name': 'foo', 'name1':'v1','name2':'v2','name3':'v3'}", MyRecord.class).b);
  }

  @Test
  public void testConstructorRuns() {
    assertEquals(new MyRecord(null, null),
        gson.fromJson("{'name1': null, 'name2': null}", MyRecord.class));
  }

  @Test
  public void testPrimitiveDefaultValues() {
    RecordWithPrimitives expected = new RecordWithPrimitives("s", (byte) 0, (short) 0, 0, 0, 0, 0, '\0', false);
    assertEquals(expected, gson.fromJson("{'aString': 's'}", RecordWithPrimitives.class));
  }

  @Test
  public void testPrimitiveNullValues() {
    RecordWithPrimitives expected = new RecordWithPrimitives("s", (byte) 0, (short) 0, 0, 0, 0, 0, '\0', false);
    // TODO(eamonnmcmanus): consider forbidding null for primitives
    String s = "{'aString': 's', 'aByte': null, 'aShort': null, 'anInt': null, 'aLong': null, 'aFloat': null, 'aDouble': null, 'aChar': null, 'aBoolean': null}";
    assertEquals(expected, gson.fromJson(s, RecordWithPrimitives.class));
  }

  public record MyRecord(
      @SerializedName("name") String a,
      @SerializedName(value = "name1", alternate = {"name2", "name3"}) String b) {
    public MyRecord {
      a = "modified-" + a;
    }
  }

  public record RecordWithPrimitives(
      String aString, byte aByte, short aShort, int anInt, long aLong, float aFloat, double aDouble, char aChar, boolean aBoolean) {}
}
