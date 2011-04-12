/*
 * Copyright (C) 2010 Google Inc.
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
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import java.io.EOFException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Reads and writes GSON parse trees over streams.
 */
final class Streams {

  /**
   * Takes a reader in any state and returns the next value as a JsonElement.
   */
  static JsonElement parse(JsonReader reader) throws JsonParseException {
    boolean isEmpty = true;
    try {
      reader.peek();
      isEmpty = false;
      return parseRecursive(reader);
    } catch (EOFException e) {
      /*
       * For compatibility with JSON 1.5 and earlier, we return a JsonNull for
       * empty documents instead of throwing.
       */
      if (isEmpty) {
        return JsonNull.createJsonNull();
      }
      throw new JsonIOException(e);
    } catch (MalformedJsonException e) {
      throw new JsonSyntaxException(e);
    } catch (IOException e) {
      throw new JsonIOException(e);
    } catch (NumberFormatException e) {
      throw new JsonSyntaxException(e);
    }
  }

  private static JsonElement parseRecursive(JsonReader reader) throws IOException {
    switch (reader.peek()) {
    case STRING:
      return new JsonPrimitive(reader.nextString());
    case NUMBER:
      String number = reader.nextString();
      return new JsonPrimitive(JsonPrimitive.stringToNumber(number));
    case BOOLEAN:
      return new JsonPrimitive(reader.nextBoolean());
    case NULL:
      reader.nextNull();
      return JsonNull.createJsonNull();
    case BEGIN_ARRAY:
      JsonArray array = new JsonArray();
      reader.beginArray();
      while (reader.hasNext()) {
        array.add(parseRecursive(reader));
      }
      reader.endArray();
      return array;
    case BEGIN_OBJECT:
      JsonObject object = new JsonObject();
      reader.beginObject();
      while (reader.hasNext()) {
        object.add(reader.nextName(), parseRecursive(reader));
      }
      reader.endObject();
      return object;
    case END_DOCUMENT:
    case NAME:
    case END_OBJECT:
    case END_ARRAY:
    default:
      throw new IllegalArgumentException();
    }
  }

  /**
   * Writes the JSON element to the writer, recursively.
   */
  static void write(JsonElement element, boolean serializeNulls, JsonWriter writer)
      throws IOException {
    if (element == null || element.isJsonNull()) {
      if (serializeNulls) {
        writer.nullValue();
      }

    } else if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isNumber()) {
        writer.value(primitive.getAsNumber());
      } else if (primitive.isBoolean()) {
        writer.value(primitive.getAsBoolean());
      } else {
        writer.value(primitive.getAsString());
      }

    } else if (element.isJsonArray()) {
      writer.beginArray();
      for (JsonElement e : element.getAsJsonArray()) {
        /* always print null when its parent element is an array! */
        if (e.isJsonNull()) {
          writer.nullValue();
          continue;
        }
        write(e, serializeNulls, writer);
      }
      writer.endArray();

    } else if (element.isJsonObject()) {
      writer.beginObject();
      for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
        JsonElement value = e.getValue();
        if (!serializeNulls && value.isJsonNull()) {
          continue;
        }
        writer.name(e.getKey());
        write(value, serializeNulls, writer);
      }
      writer.endObject();

    } else {
      throw new IllegalArgumentException("Couldn't write " + element.getClass());
    }
  }

  static Writer writerForAppendable(Appendable appendable) {
    return appendable instanceof Writer ? (Writer) appendable : new AppendableWriter(appendable);
  }

  /**
   * Adapts an {@link Appendable} so it can be passed anywhere a {@link Writer}
   * is used.
   */
  private static class AppendableWriter extends Writer {
    private final Appendable appendable;
    private final CurrentWrite currentWrite = new CurrentWrite();

    private AppendableWriter(Appendable appendable) {
      this.appendable = appendable;
    }

    @Override public void write(char[] chars, int offset, int length) throws IOException {
      currentWrite.chars = chars;
      appendable.append(currentWrite, offset, offset + length);
    }

    @Override public void write(int i) throws IOException {
      appendable.append((char) i);
    }

    @Override public void flush() {}
    @Override public void close() {}

    /**
     * A mutable char sequence pointing at a single char[].
     */
    static class CurrentWrite implements CharSequence {
      char[] chars;
      public int length() {
        return chars.length;
      }
      public char charAt(int i) {
        return chars[i];
      }
      public CharSequence subSequence(int start, int end) {
        return new String(chars, start, end - start);
      }
    }
  }
}
