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

package com.google.gson.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Writer;
import org.junit.Test;

public class StreamsTest {
  @Test
  public void testWriterForAppendable() throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    Writer writer = Streams.writerForAppendable(stringBuilder);

    writer.append('a');
    writer.append('\u1234');
    writer.append("test");
    writer.append(null); // test custom null handling mandated by `append`
    writer.append("abcdef", 2, 4);
    writer.append(null, 1, 3); // test custom null handling mandated by `append`
    writer.append(',');

    writer.write('a');
    writer.write('\u1234');
    // Should only consider the 16 low-order bits
    writer.write(0x4321_1234);
    writer.append(',');

    writer.write("chars".toCharArray());
    try {
      writer.write((char[]) null);
      fail();
    } catch (NullPointerException e) {
    }

    writer.write("chars".toCharArray(), 1, 2);
    try {
      writer.write((char[]) null, 1, 2);
      fail();
    } catch (NullPointerException e) {
    }
    writer.append(',');

    writer.write("string");
    try {
      writer.write((String) null);
      fail();
    } catch (NullPointerException e) {
    }

    writer.write("string", 1, 2);
    try {
      writer.write((String) null, 1, 2);
      fail();
    } catch (NullPointerException e) {
    }

    String actualOutput = stringBuilder.toString();
    assertThat(actualOutput).isEqualTo("a\u1234testnullcdul,a\u1234\u1234,charsha,stringtr");

    writer.flush();
    writer.close();

    // flush() and close() calls should have had no effect
    assertThat(stringBuilder.toString()).isEqualTo(actualOutput);
  }
}
