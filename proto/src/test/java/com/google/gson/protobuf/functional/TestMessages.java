/*
 * Copyright (C) 2026 Google Inc.
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
package com.google.gson.protobuf.functional;

import com.google.gson.protobuf.TestAllTypes;
import com.google.gson.protobuf.TestAllTypes.NestedEnum;
import com.google.gson.protobuf.TestRecursive;
import com.google.protobuf.ByteString;

/** Helper class for creating test messages. */
final class TestMessages {

  static TestAllTypes testAllTypes() {
    TestAllTypes.Builder builder = TestAllTypes.newBuilder();
    builder.setOptionalInt32(1234);
    builder.setOptionalInt64(1234567890123456789L);
    builder.setOptionalUint32(-1);
    builder.setOptionalUint64(-1L);
    builder.setOptionalSint32(9012);
    builder.setOptionalSint64(3456789012345678901L);
    builder.setOptionalFixed32(3456);
    builder.setOptionalFixed64(4567890123456789012L);
    builder.setOptionalSfixed32(7890);
    builder.setOptionalSfixed64(5678901234567890123L);
    builder.setOptionalFloat(1.5f);
    builder.setOptionalDouble(1.25);
    builder.setOptionalBool(true);
    builder.setOptionalString("Hello world!");
    builder.setOptionalBytes(ByteString.copyFrom(new byte[] {0, 1, -2}));
    builder.setOptionalNestedEnum(NestedEnum.BAR);
    builder.setOptionalRecursive(
        TestRecursive.newBuilder()
            .setValue(100)
            .setNested(TestRecursive.newBuilder().setValue(200)));
    builder.getOptionalNestedMessageBuilder().setValue(100);

    builder.addRepeatedInt32(1234);
    builder.addRepeatedInt64(1234567890123456789L);
    builder.addRepeatedUint32(5678);
    builder.addRepeatedUint64(2345678901234567890L);
    builder.addRepeatedSint32(9012);
    builder.addRepeatedSint64(3456789012345678901L);
    builder.addRepeatedFixed32(3456);
    builder.addRepeatedFixed64(4567890123456789012L);
    builder.addRepeatedSfixed32(7890);
    builder.addRepeatedSfixed64(5678901234567890123L);
    builder.addRepeatedFloat(1.5f);
    builder.addRepeatedDouble(1.25);
    builder.addRepeatedBool(true);
    builder.addRepeatedString("Hello world!");
    builder.addRepeatedBytes(ByteString.copyFrom(new byte[] {0, 1, 2}));
    builder.addRepeatedNestedEnum(NestedEnum.BAR);
    builder.addRepeatedNestedMessageBuilder().setValue(100);

    builder.addRepeatedInt32(234);
    builder.addRepeatedInt64(234567890123456789L);
    builder.addRepeatedUint32(678);
    builder.addRepeatedUint64(345678901234567890L);
    builder.addRepeatedSint32(012);
    builder.addRepeatedSint64(456789012345678901L);
    builder.addRepeatedFixed32(456);
    builder.addRepeatedFixed64(567890123456789012L);
    builder.addRepeatedSfixed32(890);
    builder.addRepeatedSfixed64(678901234567890123L);
    builder.addRepeatedFloat(11.5f);
    builder.addRepeatedDouble(11.25);
    builder.addRepeatedBool(true);
    builder.addRepeatedString("ello world!");
    builder.addRepeatedBytes(ByteString.copyFrom(new byte[] {1, 2}));
    builder.addRepeatedNestedEnum(NestedEnum.BAZ);
    builder.addRepeatedNestedMessageBuilder().setValue(200);

    return builder.build();
  }

  private TestMessages() {
  }
}
