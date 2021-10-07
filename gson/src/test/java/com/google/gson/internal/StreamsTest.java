/*
 * Copyright (C) 2020 Google Inc.
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

import com.google.gson.stream.JsonReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.io.StringReader;
import junit.framework.TestCase;

/**
 * Unit test for {@link Streams}
 *
 * @author Xiangxi Guo
 */
public class StreamsTest extends TestCase {

  public void testParseEmptyLenientReader() throws JsonParseException {
    JsonReader reader = new JsonReader(new StringReader(""));
    reader.setLenient(true);
    assertEquals(JsonNull.INSTANCE, Streams.parse(reader));
  }

  public void testParseReader() throws JsonParseException {
    JsonReader reader = new JsonReader(new StringReader("true"));
    JsonElement tru = new JsonPrimitive(true);
    assertEquals(tru, Streams.parse(reader));
  }
}
