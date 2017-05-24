/*
 * Copyright (C) 2009 Google Inc.
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
package com.economic.persistgson;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.economic.persistgson.internal.Streams;
import com.economic.persistgson.stream.JsonReader;
import com.economic.persistgson.stream.JsonToken;
import com.economic.persistgson.stream.MalformedJsonException;
import com.economic.persistgson.JsonParseException;

/**
 * A parser to parse Json into a parse tree of {@link JsonElement}s
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.3
 */
public final class JsonParser {

  /**
   * Parses the specified JSON string into a parse tree
   *
   * @param json JSON text
   * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON
   * @throws com.economic.persistgson.JsonParseException if the specified text is not valid JSON
   * @since 1.3
   */
  public JsonElement parse(String json) throws com.economic.persistgson.JsonSyntaxException {
    return parse(new StringReader(json));
  }

  /**
   * Parses the specified JSON string into a parse tree
   *
   * @param json JSON text
   * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON
   * @throws com.economic.persistgson.JsonParseException if the specified text is not valid JSON
   * @since 1.3
   */
  public JsonElement parse(Reader json) throws JsonIOException, com.economic.persistgson.JsonSyntaxException {
    try {
      JsonReader jsonReader = new JsonReader(json);
      JsonElement element = parse(jsonReader);
      if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
        throw new com.economic.persistgson.JsonSyntaxException("Did not consume the entire document.");
      }
      return element;
    } catch (MalformedJsonException e) {
      throw new com.economic.persistgson.JsonSyntaxException(e);
    } catch (IOException e) {
      throw new JsonIOException(e);
    } catch (NumberFormatException e) {
      throw new com.economic.persistgson.JsonSyntaxException(e);
    }
  }

  /**
   * Returns the next value from the JSON stream as a parse tree.
   *
   * @throws com.economic.persistgson.JsonParseException if there is an IOException or if the specified
   *     text is not valid JSON
   * @since 1.6
   */
  public JsonElement parse(JsonReader json) throws JsonIOException, com.economic.persistgson.JsonSyntaxException {
    boolean lenient = json.isLenient();
    json.setLenient(true);
    try {
      return Streams.parse(json);
    } catch (StackOverflowError e) {
      throw new com.economic.persistgson.JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
    } catch (OutOfMemoryError e) {
      throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
    } finally {
      json.setLenient(lenient);
    }
  }
}
