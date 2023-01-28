/*
 * Copyright (C) 2017 Google Inc.
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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.common.MoreAsserts;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

@SuppressWarnings("resource")
public class JsonTreeReaderTest {
  @Test
  public void testSkipValue_emptyJsonObject() throws IOException {
    JsonTreeReader in = new JsonTreeReader(new JsonObject());
    in.skipValue();
    assertThat(in.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    assertThat(in.getPath()).isEqualTo("$");
  }

  @Test
  public void testSkipValue_filledJsonObject() throws IOException {
    JsonObject jsonObject = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    jsonArray.add('c');
    jsonArray.add("text");
    jsonObject.add("a", jsonArray);
    jsonObject.addProperty("b", true);
    jsonObject.addProperty("i", 1);
    jsonObject.add("n", JsonNull.INSTANCE);
    JsonObject jsonObject2 = new JsonObject();
    jsonObject2.addProperty("n", 2L);
    jsonObject.add("o", jsonObject2);
    jsonObject.addProperty("s", "text");
    JsonTreeReader in = new JsonTreeReader(jsonObject);
    in.skipValue();
    assertThat(in.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    assertThat(in.getPath()).isEqualTo("$");
  }

  @Test
  public void testSkipValue_name() throws IOException {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("a", "value");
    JsonTreeReader in = new JsonTreeReader(jsonObject);
    in.beginObject();
    in.skipValue();
    assertThat(in.peek()).isEqualTo(JsonToken.STRING);
    assertThat(in.getPath()).isEqualTo("$.<skipped>");
    assertThat(in.nextString()).isEqualTo("value");
  }

  @Test
  public void testSkipValue_afterEndOfDocument() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonObject());
    reader.beginObject();
    reader.endObject();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);

    assertThat(reader.getPath()).isEqualTo("$");
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test
  public void testSkipValue_atArrayEnd() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonArray());
    reader.beginArray();
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test
  public void testSkipValue_atObjectEnd() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonObject());
    reader.beginObject();
    reader.skipValue();
    assertThat(reader.peek()).isEqualTo(JsonToken.END_DOCUMENT);
    assertThat(reader.getPath()).isEqualTo("$");
  }

  @Test
  public void testHasNext_endOfDocument() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonObject());
    reader.beginObject();
    reader.endObject();
    assertThat(reader.hasNext()).isFalse();
  }

  @Test
  public void testCustomJsonElementSubclass() throws IOException {
    @SuppressWarnings("deprecation") // superclass constructor
    class CustomSubclass extends JsonElement {
      @Override
      public JsonElement deepCopy() {
        return this;
      }
    }

    JsonArray array = new JsonArray();
    array.add(new CustomSubclass());

    JsonTreeReader reader = new JsonTreeReader(array);
    reader.beginArray();
    try {
      // Should fail due to custom JsonElement subclass
      reader.peek();
      fail();
    } catch (MalformedJsonException expected) {
      assertThat(expected.getMessage()).isEqualTo("Custom JsonElement subclass " + CustomSubclass.class.getName() + " is not supported");
    }
  }

  /**
   * {@link JsonTreeReader} effectively replaces the complete reading logic of {@link JsonReader} to
   * read from a {@link JsonElement} instead of a {@link Reader}. Therefore all relevant methods of
   * {@code JsonReader} must be overridden.
   */
  @Test
  public void testOverrides() {
    List<String> ignoredMethods = Arrays.asList("setLenient(boolean)", "isLenient()");
    MoreAsserts.assertOverridesMethods(JsonReader.class, JsonTreeReader.class, ignoredMethods);
  }
}
