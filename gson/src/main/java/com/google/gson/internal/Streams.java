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

package com.google.gson.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import java.io.EOFException;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * Reads and writes GSON parse trees over streams.
 */
public final class Streams {
  private Streams() {
    throw new UnsupportedOperationException();
  }

  /**
   * Takes a reader in any state and returns the next value as a JsonElement.
   */
  public static JsonElement parse(JsonReader reader) throws JsonParseException {
    boolean isEmpty = true;
    try {
      reader.peek();
      isEmpty = false;
      return TypeAdapters.JSON_ELEMENT.read(reader);
    } catch (EOFException e) {
      /*
       * For compatibility with JSON 1.5 and earlier, we return a JsonNull for
       * empty documents instead of throwing.
       */
      if (isEmpty) {
        return JsonNull.INSTANCE;
      }
      // The stream ended prematurely so it is likely a syntax error.
      throw new JsonSyntaxException(e);
    } catch (MalformedJsonException e) {
      throw new JsonSyntaxException(e);
    } catch (IOException e) {
      throw new JsonIOException(e);
    } catch (NumberFormatException e) {
      throw new JsonSyntaxException(e);
    }
  }

  /**
   * Writes the JSON element to the writer, recursively.
   */
  public static void write(JsonElement element, JsonWriter writer) throws IOException {
    TypeAdapters.JSON_ELEMENT.write(writer, element);
  }

  public static Writer writerForAppendable(Appendable appendable) {
    return appendable instanceof Writer ? (Writer) appendable : new AppendableWriter(appendable);
  }

  /**
   * Adapts an {@link Appendable} so it can be passed anywhere a {@link Writer}
   * is used.
   */
  private static final class AppendableWriter extends Writer {
    private final Appendable appendable;
    private final CurrentWrite currentWrite = new CurrentWrite();

    AppendableWriter(Appendable appendable) {
      this.appendable = appendable;
    }

    @Override public void write(char[] chars, int offset, int length) throws IOException {
      currentWrite.setChars(chars);
      appendable.append(currentWrite, offset, offset + length);
    }

    @Override public void flush() {}
    @Override public void close() {}

    // Override these methods for better performance
    // They would otherwise unnecessarily create Strings or char arrays

    @Override public void write(int i) throws IOException {
      appendable.append((char) i);
    }

    @Override public void write(String str, int off, int len) throws IOException {
      // Appendable.append turns null -> "null", which is not desired here
      Objects.requireNonNull(str);
      appendable.append(str, off, off + len);
    }

    @Override public Writer append(CharSequence csq) throws IOException {
      appendable.append(csq);
      return this;
    }

    @Override public Writer append(CharSequence csq, int start, int end) throws IOException {
      appendable.append(csq, start, end);
      return this;
    }

    /**
     * A mutable char sequence pointing at a single char[].
     */
    private static class CurrentWrite implements CharSequence {
      private char[] chars;
      private String cachedString;

      void setChars(char[] chars) {
        this.chars = chars;
        this.cachedString = null;
      }

      @Override public int length() {
        return chars.length;
      }
      @Override public char charAt(int i) {
        return chars[i];
      }
      @Override public CharSequence subSequence(int start, int end) {
        return new String(chars, start, end - start);
      }

      // Must return string representation to satisfy toString() contract
      @Override public String toString() {
        if (cachedString == null) {
          cachedString = new String(chars);
        }
        return cachedString;
      }
    }
  }
}
