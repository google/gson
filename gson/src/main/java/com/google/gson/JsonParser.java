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
package com.google.gson;

import com.google.gson.stream.JsonReader;
import java.io.EOFException;
import java.io.Reader;
import java.io.StringReader;

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
   * @throws JsonParseException if the specified text is not valid JSON
   * @since 1.3
   */
  public JsonElement parse(String json) throws JsonParseException {
    return parse(new StringReader(json));
  }
  
  /**
   * Parses the specified JSON string into a parse tree
   * 
   * @param json JSON text
   * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON 
   * @throws JsonParseException if the specified text is not valid JSON
   * @since 1.3
   */
  public JsonElement parse(Reader json) throws JsonParseException {
    return parse(new JsonReader(json));
  }

  /**
   * Returns the next value from the JSON stream as a parse tree.
   *
   * @throws JsonParseException if there is an IOException or if the specified
   *     text is not valid JSON
   */
  public JsonElement parse(JsonReader json) throws JsonParseException {
    boolean lenient = json.isLenient();
    json.setLenient(true);
    try {
      return Streams.parse(json);
    } catch (StackOverflowError e) {
      throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
    } catch (OutOfMemoryError e) {
      throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
    } catch (JsonParseException e) {
      if (e.getCause() instanceof EOFException) {
        return JsonNull.createJsonNull();
      }
      throw e;
    } finally {
      json.setLenient(lenient);
    }
  }
}
