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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.JsonTreeReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings("resource")
@RunWith(Parameterized.class)
public class JsonReaderPathTest {
  @Parameterized.Parameters(name = "{0}")
  public static List<Object[]> parameters() {
    return Arrays.asList(
        new Object[] { Factory.STRING_READER },
        new Object[] { Factory.OBJECT_READER }
    );
  }

  @Parameterized.Parameter
  public Factory factory;

  @Test public void path() throws IOException {
    JsonReader reader = factory.create("{\"a\":[2,true,false,null,\"b\",{\"c\":\"d\"},[3]]}");
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPath()).isEqualTo("$.");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[0]");
    assertThat(reader.getPath()).isEqualTo("$.a[0]");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[0]");
    assertThat(reader.getPath()).isEqualTo("$.a[1]");
    reader.nextBoolean();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[1]");
    assertThat(reader.getPath()).isEqualTo("$.a[2]");
    reader.nextBoolean();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[2]");
    assertThat(reader.getPath()).isEqualTo("$.a[3]");
    reader.nextNull();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[3]");
    assertThat(reader.getPath()).isEqualTo("$.a[4]");
    reader.nextString();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[4]");
    assertThat(reader.getPath()).isEqualTo("$.a[5]");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[5].");
    assertThat(reader.getPath()).isEqualTo("$.a[5].");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[5].c");
    assertThat(reader.getPath()).isEqualTo("$.a[5].c");
    reader.nextString();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[5].c");
    assertThat(reader.getPath()).isEqualTo("$.a[5].c");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[5]");
    assertThat(reader.getPath()).isEqualTo("$.a[6]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[6][0]");
    assertThat(reader.getPath()).isEqualTo("$.a[6][0]");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[6][0]");
    assertThat(reader.getPath()).isEqualTo("$.a[6][1]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a[6]");
    assertThat(reader.getPath()).isEqualTo("$.a[7]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test public void objectPath() throws IOException {
    JsonReader reader = factory.create("{\"a\":1,\"b\":2}");
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPath()).isEqualTo("$.");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPath()).isEqualTo("$.");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPath()).isEqualTo("$.b");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPath()).isEqualTo("$.b");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPath()).isEqualTo("$.b");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPath()).isEqualTo("$.b");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
    reader.close();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test public void arrayPath() throws IOException {
    JsonReader reader = factory.create("[1,2]");
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[0]");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[0]");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[1]");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[1]");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPath()).isEqualTo("$[2]");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPath()).isEqualTo("$[2]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");

    reader.peek();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
    reader.close();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test public void multipleTopLevelValuesInOneDocument() throws IOException {
    assumeTrue(factory == Factory.STRING_READER);

    JsonReader reader = factory.create("[][]");
    reader.setLenient(true);
    reader.beginArray();
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
    reader.beginArray();
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test public void skipArrayElements() throws IOException {
    JsonReader reader = factory.create("[1,2,3]");
    reader.beginArray();
    reader.skipValue();
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPath()).isEqualTo("$[2]");
  }

  @Test public void skipArrayEnd() throws IOException {
    JsonReader reader = factory.create("[[],1]");
    reader.beginArray();
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0][0]");
    assertThat(reader.getPath()).isEqualTo("$[0][0]");
    reader.skipValue(); // skip end of array
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[1]");
  }

  @Test public void skipObjectNames() throws IOException {
    JsonReader reader = factory.create("{\"a\":[]}");
    reader.beginObject();
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$.<skipped>");
    assertThat(reader.getPath()).isEqualTo("$.<skipped>");

    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$.<skipped>[0]");
    assertThat(reader.getPath()).isEqualTo("$.<skipped>[0]");
  }

  @Test public void skipObjectValues() throws IOException {
    JsonReader reader = factory.create("{\"a\":1,\"b\":2}");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPath()).isEqualTo("$.");
    reader.nextName();
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPath()).isEqualTo("$.b");
  }

  @Test public void skipObjectEnd() throws IOException {
    JsonReader reader = factory.create("{\"a\":{},\"b\":2}");
    reader.beginObject();
    reader.nextName();
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.");
    assertThat(reader.getPath()).isEqualTo("$.a.");
    reader.skipValue(); // skip end of object
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");
  }

  @Test public void skipNestedStructures() throws IOException {
    JsonReader reader = factory.create("[[1,2,3],4]");
    reader.beginArray();
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[1]");
  }

  @Test public void skipEndOfDocument() throws IOException {
    JsonReader reader = factory.create("[]");
    reader.beginArray();
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
    reader.skipValue();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test public void arrayOfObjects() throws IOException {
    JsonReader reader = factory.create("[{},{},{}]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[0]");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0].");
    assertThat(reader.getPath()).isEqualTo("$[0].");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[1]");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1].");
    assertThat(reader.getPath()).isEqualTo("$[1].");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPath()).isEqualTo("$[2]");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[2].");
    assertThat(reader.getPath()).isEqualTo("$[2].");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$[2]");
    assertThat(reader.getPath()).isEqualTo("$[3]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test public void arrayOfArrays() throws IOException {
    JsonReader reader = factory.create("[[],[],[]]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[0]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0][0]");
    assertThat(reader.getPath()).isEqualTo("$[0][0]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[0]");
    assertThat(reader.getPath()).isEqualTo("$[1]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1][0]");
    assertThat(reader.getPath()).isEqualTo("$[1][0]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[1]");
    assertThat(reader.getPath()).isEqualTo("$[2]");
    reader.beginArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[2][0]");
    assertThat(reader.getPath()).isEqualTo("$[2][0]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$[2]");
    assertThat(reader.getPath()).isEqualTo("$[3]");
    reader.endArray();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test public void objectOfObjects() throws IOException {
    JsonReader reader = factory.create("{\"a\":{\"a1\":1,\"a2\":2},\"b\":{\"b1\":1}}");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.");
    assertThat(reader.getPath()).isEqualTo("$.");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.");
    assertThat(reader.getPath()).isEqualTo("$.a.");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.a1");
    assertThat(reader.getPath()).isEqualTo("$.a.a1");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.a1");
    assertThat(reader.getPath()).isEqualTo("$.a.a1");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.a2");
    assertThat(reader.getPath()).isEqualTo("$.a.a2");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a.a2");
    assertThat(reader.getPath()).isEqualTo("$.a.a2");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.a");
    assertThat(reader.getPath()).isEqualTo("$.a");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPath()).isEqualTo("$.b");
    reader.beginObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b.");
    assertThat(reader.getPath()).isEqualTo("$.b.");
    reader.nextName();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b.b1");
    assertThat(reader.getPath()).isEqualTo("$.b.b1");
    reader.nextInt();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b.b1");
    assertThat(reader.getPath()).isEqualTo("$.b.b1");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$.b");
    assertThat(reader.getPath()).isEqualTo("$.b");
    reader.endObject();
    assertThat(reader.getPreviousPath()).isEqualTo("$");
    assertThat(reader.getPath()).isEqualTo("$");
  }

  public enum Factory {
    STRING_READER {
      @Override public JsonReader create(String data) {
        return new JsonReader(new StringReader(data));
      }
    },
    OBJECT_READER {
      @Override public JsonReader create(String data) {
        JsonElement element = Streams.parse(new JsonReader(new StringReader(data)));
        return new JsonTreeReader(element);
      }
    };

    abstract JsonReader create(String data);
  }
}
