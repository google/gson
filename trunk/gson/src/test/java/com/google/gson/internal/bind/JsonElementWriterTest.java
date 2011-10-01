/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson.internal.bind;

import java.io.IOException;
import junit.framework.TestCase;

public final class JsonElementWriterTest extends TestCase {

  // TODO: more tests
  // TODO: figure out what should be returned by an empty writer

  public void testArray() throws IOException {
    JsonElementWriter writer = new JsonElementWriter();
    writer.beginArray();
    writer.value(1);
    writer.value(2);
    writer.value(3);
    writer.endArray();
    assertEquals("[1,2,3]", writer.get().toString());
  }

  public void testNestedArray() throws IOException {
    JsonElementWriter writer = new JsonElementWriter();
    writer.beginArray();
    writer.beginArray();
    writer.endArray();
    writer.beginArray();
    writer.beginArray();
    writer.endArray();
    writer.endArray();
    writer.endArray();
    assertEquals("[[],[[]]]", writer.get().toString());
  }

  public void testObject() throws IOException {
    JsonElementWriter writer = new JsonElementWriter();
    writer.beginObject();
    writer.name("A").value(1);
    writer.name("B").value(2);
    writer.endObject();
    assertEquals("{\"A\":1,\"B\":2}", writer.get().toString());
  }

  public void testNestedObject() throws IOException {
    JsonElementWriter writer = new JsonElementWriter();
    writer.beginObject();
    writer.name("A");
    writer.beginObject();
    writer.name("B");
    writer.beginObject();
    writer.endObject();
    writer.endObject();
    writer.name("C");
    writer.beginObject();
    writer.endObject();
    writer.endObject();
    assertEquals("{\"A\":{\"B\":{}},\"C\":{}}", writer.get().toString());
  }

  public void testWriteAfterClose() throws Exception {
    JsonElementWriter writer = new JsonElementWriter();
    writer.setLenient(true);
    writer.beginArray();
    writer.value("A");
    writer.endArray();
    writer.close();
    try {
      writer.beginArray();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testPrematureClose() throws Exception {
    JsonElementWriter writer = new JsonElementWriter();
    writer.setLenient(true);
    writer.beginArray();
    try {
      writer.close();
      fail();
    } catch (IOException expected) {
    }
  }

  public void testSerializeNullsFalse() throws IOException {
    JsonElementWriter writer = new JsonElementWriter();
    writer.setSerializeNulls(false);
    writer.beginObject();
    writer.name("A");
    writer.nullValue();
    writer.endObject();
    assertEquals("{}", writer.get().toString());
  }

  public void testSerializeNullsTrue() throws IOException {
    JsonElementWriter writer = new JsonElementWriter();
    writer.setSerializeNulls(true);
    writer.beginObject();
    writer.name("A");
    writer.nullValue();
    writer.endObject();
    assertEquals("{\"A\":null}", writer.get().toString());
  }
}
