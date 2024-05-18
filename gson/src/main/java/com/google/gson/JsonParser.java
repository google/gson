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

import com.google.errorprone.annotations.InlineMe;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * A parser to parse JSON into a parse tree of {@link JsonElement}s.
 *
 * <p>The JSON data is parsed in {@linkplain JsonReader#setStrictness(Strictness) lenient mode}.
 *
 * <p>Here's an example of parsing from a string:
 *
 * <pre>
 * String json = "{\"key\": \"value\"}";
 * JsonElement jsonElement = JsonParser.parseString(json);
 * JsonObject jsonObject = jsonElement.getAsJsonObject();
 * </pre>
 *
 * <p>It can also parse from a reader:
 *
 * <pre>
 * try (Reader reader = new FileReader("my-data.json", StandardCharsets.UTF_8)) {
 *   JsonElement jsonElement = JsonParser.parseReader(reader);
 *   JsonObject jsonObject = jsonElement.getAsJsonObject();
 * }
 * </pre>
 *
 * <p>If you want to parse from a {@link JsonReader} for more customized parsing requirements, the
 * following example demonstrates how to achieve it:
 *
 * <pre>
 * String json = "{\"skipObj\": {\"skipKey\": \"skipValue\"}, \"obj\": {\"key\": \"value\"}}";
 * try (JsonReader jsonReader = new JsonReader(new StringReader(json))) {
 *   jsonReader.beginObject();
 *   while (jsonReader.hasNext()) {
 *     String fieldName = jsonReader.nextName();
 *     if (fieldName.equals("skipObj")) {
 *       jsonReader.skipValue();
 *     } else {
 *       JsonElement jsonElement = JsonParser.parseReader(jsonReader);
 *       JsonObject jsonObject = jsonElement.getAsJsonObject();
 *     }
 *   }
 *   jsonReader.endObject();
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.3
 */
public final class JsonParser {
  /**
   * @deprecated No need to instantiate this class, use the static methods instead.
   */
  @Deprecated
  public JsonParser() {}

  /**
   * Parses the specified JSON string into a parse tree. An exception is thrown if the JSON string
   * has multiple top-level JSON elements, or if there is trailing data.
   *
   * <p>The JSON string is parsed in {@linkplain JsonReader#setStrictness(Strictness) lenient mode}.
   *
   * @param json JSON text
   * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON
   * @throws JsonParseException if the specified text is not valid JSON
   * @since 2.8.6
   */
  public static JsonElement parseString(String json) throws JsonSyntaxException {
    return parseReader(new StringReader(json));
  }

  /**
   * Parses the complete JSON string provided by the reader into a parse tree. An exception is
   * thrown if the JSON string has multiple top-level JSON elements, or if there is trailing data.
   *
   * <p>The JSON data is parsed in {@linkplain JsonReader#setStrictness(Strictness) lenient mode}.
   *
   * @param reader JSON text
   * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON
   * @throws JsonParseException if there is an IOException or if the specified text is not valid
   *     JSON
   * @since 2.8.6
   */
  public static JsonElement parseReader(Reader reader) throws JsonIOException, JsonSyntaxException {
    try {
      JsonReader jsonReader = new JsonReader(reader);
      JsonElement element = parseReader(jsonReader);
      if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
        throw new JsonSyntaxException("Did not consume the entire document.");
      }
      return element;
    } catch (MalformedJsonException e) {
      throw new JsonSyntaxException(e);
    } catch (IOException e) {
      throw new JsonIOException(e);
    } catch (NumberFormatException e) {
      throw new JsonSyntaxException(e);
    }
  }

  /**
   * Returns the next value from the JSON stream as a parse tree. Unlike the other {@code parse}
   * methods, no exception is thrown if the JSON data has multiple top-level JSON elements, or if
   * there is trailing data.
   *
   * <p>If the {@linkplain JsonReader#getStrictness() strictness of the reader} is {@link
   * Strictness#STRICT}, that strictness will be used for parsing. Otherwise the strictness will be
   * temporarily changed to {@link Strictness#LENIENT} and will be restored once this method
   * returns.
   *
   * @throws JsonParseException if there is an IOException or if the specified text is not valid
   *     JSON
   * @since 2.8.6
   */
  public static JsonElement parseReader(JsonReader reader)
      throws JsonIOException, JsonSyntaxException {
    Strictness strictness = reader.getStrictness();
    if (strictness == Strictness.LEGACY_STRICT) {
      // For backward compatibility change to LENIENT if reader has default strictness LEGACY_STRICT
      reader.setStrictness(Strictness.LENIENT);
    }
    try {
      return Streams.parse(reader);
    } catch (StackOverflowError e) {
      throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", e);
    } catch (OutOfMemoryError e) {
      throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", e);
    } finally {
      reader.setStrictness(strictness);
    }
  }

  /**
   * @deprecated Use {@link JsonParser#parseString}
   */
  @Deprecated
  @InlineMe(replacement = "JsonParser.parseString(json)", imports = "com.google.gson.JsonParser")
  public JsonElement parse(String json) throws JsonSyntaxException {
    return parseString(json);
  }

  /**
   * @deprecated Use {@link JsonParser#parseReader(Reader)}
   */
  @Deprecated
  @InlineMe(replacement = "JsonParser.parseReader(json)", imports = "com.google.gson.JsonParser")
  public JsonElement parse(Reader json) throws JsonIOException, JsonSyntaxException {
    return parseReader(json);
  }

  /**
   * @deprecated Use {@link JsonParser#parseReader(JsonReader)}
   */
  @Deprecated
  @InlineMe(replacement = "JsonParser.parseReader(json)", imports = "com.google.gson.JsonParser")
  public JsonElement parse(JsonReader json) throws JsonIOException, JsonSyntaxException {
    return parseReader(json);
  }
}
