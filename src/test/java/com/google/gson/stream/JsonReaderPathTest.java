/*
 * Copyright (C) 2014 Google Inc.
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

package com.google.gson.stream;

import java.io.IOException;
import java.io.StringReader;
import junit.framework.TestCase;

public class JsonReaderPathTest extends TestCase {
  public void testPath() throws IOException {
    JsonReader reader = new JsonReader(
        new StringReader("{\"a\":[2,true,false,null,\"b\",{\"c\":\"d\"},[3]]}"));
    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPath());
    reader.nextName();
    assertEquals("$.a", reader.getPath());
    reader.beginArray();
    assertEquals("$.a[0]", reader.getPath());
    reader.nextInt();
    assertEquals("$.a[1]", reader.getPath());
    reader.nextBoolean();
    assertEquals("$.a[2]", reader.getPath());
    reader.nextBoolean();
    assertEquals("$.a[3]", reader.getPath());
    reader.nextNull();
    assertEquals("$.a[4]", reader.getPath());
    reader.nextString();
    assertEquals("$.a[5]", reader.getPath());
    reader.beginObject();
    assertEquals("$.a[5].", reader.getPath());
    reader.nextName();
    assertEquals("$.a[5].c", reader.getPath());
    reader.nextString();
    assertEquals("$.a[5].c", reader.getPath());
    reader.endObject();
    assertEquals("$.a[5]", reader.getPath());
    reader.beginArray();
    assertEquals("$.a[5][0]", reader.getPath());
    reader.nextInt();
    assertEquals("$.a[5][1]", reader.getPath());
    reader.endArray();
    assertEquals("$.a[5]", reader.getPath());
    reader.endArray();
    assertEquals("$.a", reader.getPath());
    reader.endObject();
    assertEquals("$", reader.getPath());
  }

  public void testObjectPath() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":1,\"b\":2}"));
    assertEquals("$", reader.getPath());

    reader.peek();
    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPath());

    reader.peek();
    assertEquals("$.", reader.getPath());
    reader.nextName();
    assertEquals("$.a", reader.getPath());

    reader.peek();
    assertEquals("$.a", reader.getPath());
    reader.nextInt();
    assertEquals("$.a", reader.getPath());

    reader.peek();
    assertEquals("$.a", reader.getPath());
    reader.nextName();
    assertEquals("$.b", reader.getPath());

    reader.peek();
    assertEquals("$.b", reader.getPath());
    reader.nextInt();
    assertEquals("$.b", reader.getPath());

    reader.peek();
    assertEquals("$.b", reader.getPath());
    reader.endObject();
    assertEquals("$", reader.getPath());

    reader.peek();
    assertEquals("$", reader.getPath());
    reader.close();
    assertEquals("$", reader.getPath());
  }

  public void testArrayPath() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1,2]"));
    assertEquals("$", reader.getPath());

    reader.peek();
    assertEquals("$", reader.getPath());
    reader.beginArray();
    assertEquals("$[0]", reader.getPath());

    reader.peek();
    assertEquals("$[0]", reader.getPath());
    reader.nextInt();
    assertEquals("$[1]", reader.getPath());

    reader.peek();
    assertEquals("$[1]", reader.getPath());
    reader.nextInt();
    assertEquals("$[2]", reader.getPath());

    reader.peek();
    assertEquals("$[2]", reader.getPath());
    reader.endArray();
    assertEquals("$", reader.getPath());

    reader.peek();
    assertEquals("$", reader.getPath());
    reader.close();
    assertEquals("$", reader.getPath());
  }

  public void testMultipleTopLevelValuesInOneDocument() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[][]"));
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals("$", reader.getPath());
    reader.beginArray();
    reader.endArray();
    assertEquals("$", reader.getPath());
  }

  public void testSkipArrayElements() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1,2,3]"));
    reader.beginArray();
    reader.skipValue();
    reader.skipValue();
    assertEquals("$[2]", reader.getPath());
  }

  public void testSkipObjectNames() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":1}"));
    reader.beginObject();
    reader.skipValue();
    assertEquals("$.null", reader.getPath());
  }

  public void testSkipObjectValues() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":1,\"b\":2}"));
    reader.beginObject();
    reader.nextName();
    reader.skipValue();
    assertEquals("$.null", reader.getPath());
    reader.nextName();
    assertEquals("$.b", reader.getPath());
  }

  public void testSkipNestedStructures() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[[1,2,3],4]"));
    reader.beginArray();
    reader.skipValue();
    assertEquals("$[1]", reader.getPath());
  }
}
