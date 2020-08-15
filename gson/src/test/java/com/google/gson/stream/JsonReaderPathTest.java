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

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.stream.JsonReader.StringValueReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("resource")
@RunWith(Parameterized.class)
public class JsonReaderPathTest {
  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> parameters() {
    return Arrays.asList(
        new Object[] { Factory.STREAM_READER, true },
        new Object[] { Factory.TREE_READER, false }
    );
  }

  @Parameterized.Parameter(0)
  public Factory factory;
  /** Whether this JsonReader's {@code nextNameReader()} is streaming the name */
  @Parameterized.Parameter(1)
  public boolean isStreamingName;

  @Test public void path() throws IOException {
    JsonReader reader = factory.create("{\"a\":[2,true,false,null,\"b\",{\"c\":\"d\"},[3]]}");
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
    assertEquals("$.a[6]", reader.getPath());
    reader.beginArray();
    assertEquals("$.a[6][0]", reader.getPath());
    reader.nextInt();
    assertEquals("$.a[6][1]", reader.getPath());
    reader.endArray();
    assertEquals("$.a[7]", reader.getPath());
    reader.endArray();
    assertEquals("$.a", reader.getPath());
    reader.endObject();
    assertEquals("$", reader.getPath());
  }

  @Test public void objectPath() throws IOException {
    JsonReader reader = factory.create("{\"a\":1,\"b\":2}");
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

  @Test public void arrayPath() throws IOException {
    JsonReader reader = factory.create("[1,2]");
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

  @Test public void multipleTopLevelValuesInOneDocument() throws IOException {
    assumeTrue(factory == Factory.STREAM_READER);

    JsonReader reader = factory.create("[][]");
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertEquals("$", reader.getPath());
    reader.beginArray();
    reader.endArray();
    assertEquals("$", reader.getPath());
  }

  @Test public void skipArrayElements() throws IOException {
    JsonReader reader = factory.create("[1,2,3]");
    reader.beginArray();
    reader.skipValue();
    reader.skipValue();
    assertEquals("$[2]", reader.getPath());
  }

  @Test public void skipObjectNames() throws IOException {
    JsonReader reader = factory.create("{\"a\":1}");
    reader.beginObject();
    reader.skipValue();
    assertEquals("$.#skippedName", reader.getPath());
  }

  @Test public void skipObjectValues() throws IOException {
    JsonReader reader = factory.create("{\"a\":1,\"b\":2}");
    reader.beginObject();
    assertEquals("$.", reader.getPath());
    reader.nextName();
    reader.skipValue();
    assertEquals("$.a", reader.getPath());
    reader.nextName();
    assertEquals("$.b", reader.getPath());
  }

  @Test public void skipNestedStructures() throws IOException {
    JsonReader reader = factory.create("[[1,2,3],4]");
    reader.beginArray();
    reader.skipValue();
    assertEquals("$[1]", reader.getPath());
  }

  @Test public void arrayOfObjects() throws IOException {
    JsonReader reader = factory.create("[{},{},{}]");
    reader.beginArray();
    assertEquals("$[0]", reader.getPath());
    reader.beginObject();
    assertEquals("$[0].", reader.getPath());
    reader.endObject();
    assertEquals("$[1]", reader.getPath());
    reader.beginObject();
    assertEquals("$[1].", reader.getPath());
    reader.endObject();
    assertEquals("$[2]", reader.getPath());
    reader.beginObject();
    assertEquals("$[2].", reader.getPath());
    reader.endObject();
    assertEquals("$[3]", reader.getPath());
    reader.endArray();
    assertEquals("$", reader.getPath());
  }

  @Test public void arrayOfArrays() throws IOException {
    JsonReader reader = factory.create("[[],[],[]]");
    reader.beginArray();
    assertEquals("$[0]", reader.getPath());
    reader.beginArray();
    assertEquals("$[0][0]", reader.getPath());
    reader.endArray();
    assertEquals("$[1]", reader.getPath());
    reader.beginArray();
    assertEquals("$[1][0]", reader.getPath());
    reader.endArray();
    assertEquals("$[2]", reader.getPath());
    reader.beginArray();
    assertEquals("$[2][0]", reader.getPath());
    reader.endArray();
    assertEquals("$[3]", reader.getPath());
    reader.endArray();
    assertEquals("$", reader.getPath());
  }

  /**
   * Tests JSON path when {@code nextNameReader()} is used
   */
  @Test public void nameReader() throws IOException {
    JsonReader reader = factory.create("{\"abc\":1}");
    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPath());
    StringValueReader nameReader = reader.nextNameReader();
    nameReader.skipRemaining();
    // When reader is not closed yet path should not be updated
    assertEquals("$.", reader.getPath());
    nameReader.close();

    String expectedPropertyName = isStreamingName ? "#streamedName" : "abc";
    assertEquals("$." + expectedPropertyName, reader.getPath());
    reader.skipValue();
    assertEquals("$." + expectedPropertyName, reader.getPath());

    reader.endObject();
    assertEquals("$", reader.getPath());
  }

  /**
   * Tests JSON path when {@code nextNameReader()} has been closed before
   * all data has been consumed
   */
  @Test public void nameReaderPartial() throws IOException {
    JsonReader reader = factory.create("{\"abc\":1}");
    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPath());
    StringValueReader nameReader = reader.nextNameReader();
    // When reader is not closed yet path should not be updated
    assertEquals("$.", reader.getPath());
    nameReader.close();
    // Name has not been consumed completely so path should not be updated
    assertEquals("$.", reader.getPath());

    // Should not be able to use JsonReader when not complete name has
    // been consumed, even though nameReader was closed
    try {
      reader.peek();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  /**
   * Tests JSON path when {@code nextStringReader()} is used
   */
  @Test public void arrayStringReader() throws IOException {
    JsonReader reader = factory.create("[\"abc\"]");
    assertEquals("$", reader.getPath());
    reader.beginArray();
    assertEquals("$[0]", reader.getPath());
    StringValueReader stringReader = reader.nextStringReader();
    stringReader.skipRemaining();
    // When reader is not closed yet path should not be updated
    assertEquals("$[0]", reader.getPath());
    stringReader.close();
    assertEquals("$[1]", reader.getPath());
    reader.endArray();
    assertEquals("$", reader.getPath());
  }

  /**
   * Tests JSON path when {@code nextStringReader()} has been closed before
   * all data has been consumed
   */
  @Test public void arrayStringReaderPartial() throws IOException {
    JsonReader reader = factory.create("[\"abc\"]");
    assertEquals("$", reader.getPath());
    reader.beginArray();
    assertEquals("$[0]", reader.getPath());
    StringValueReader stringReader = reader.nextStringReader();
    // When reader is not closed yet path should not be updated
    assertEquals("$[0]", reader.getPath());
    stringReader.close();
    // String value has not been consumed completely so path should not be updated
    assertEquals("$[0]", reader.getPath());

    // Should not be able to use JsonReader when not complete string value
    // has been consumed, even though stringReader was closed
    try {
      reader.peek();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  enum Factory {
    STREAM_READER {
      @Override public JsonReader create(String data) {
        return new JsonReader(new StringReader(data));
      }
    },
    TREE_READER {
      @Override public JsonReader create(String data) {
        JsonElement element = Streams.parse(new JsonReader(new StringReader(data)));
        return new JsonTreeReader(element);
      }
    };

    abstract JsonReader create(String data);
  }
}
