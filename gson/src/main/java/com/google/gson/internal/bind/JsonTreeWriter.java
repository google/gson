/*
 * Copyright (C) 2011 Google Inc.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * This writer creates a JsonElement.
 */
public final class JsonTreeWriter extends JsonWriter {
  private static final Writer UNWRITABLE_WRITER = new Writer() {
    @Override public void write(char[] buffer, int offset, int counter) {
      throw new AssertionError();
    }
    @Override public void flush() throws IOException {
      throw new AssertionError();
    }
    @Override public void close() throws IOException {
      throw new AssertionError();
    }
  };
  /** Added to the top of the stack when this writer is closed to cause following ops to fail. */
  private static final JsonPrimitive SENTINEL_CLOSED = new JsonPrimitive("closed");

  private class StringValueWriter extends Writer {
    private final StringBuilder stringBuilder = new StringBuilder();
    private final boolean isWritingName;
    private boolean isClosed = false;

    private StringValueWriter(boolean isWritingName) {
      this.isWritingName = isWritingName;
    }

    private void verifyNotClosed() throws IOException {
      if (isClosed) {
        throw new IOException("Writer is closed");
      }
    }

    @Override public void write(char[] cbuf, int off, int len) throws IOException {
      verifyNotClosed();
      stringBuilder.append(cbuf, off, len);
    }

    // Override because default implementation explicitly converts to String
    @Override public Writer append(CharSequence csq) throws IOException {
      verifyNotClosed();

      if (csq == null) {
        write("null"); // Requirement by Writer.append
      } else {
        append(csq, 0, csq.length());
      }
      return this;
    }

    @Override public Writer append(CharSequence csq, int start, int end) throws IOException {
      verifyNotClosed();
      stringBuilder.append(csq, start, end);
      return this;
    }

    @Override public void write(int c) throws IOException {
      verifyNotClosed();
      stringBuilder.append((char) c);
    }

    @Override public void write(String str, int off, int len) throws IOException {
      // Explicit null check because append would otherwise write "null"
      if (str == null) {
        throw new NullPointerException("str must not be null");
      }
      verifyNotClosed();
      stringBuilder.append(str, off, off + len);
    }

    @Override public void flush() throws IOException {
      verifyNotClosed();
      // Do nothing
    }

    @Override public void close() throws IOException {
      if (!isClosed) {
        isClosed = true;
        // Update enclosing JsonTreeWriter
        isWriterActive = false;
        String value = stringBuilder.toString();
        if (isWritingName) {
          name(value);
        } else {
          value(value);
        }
      }
    }
  }

  /** The JsonElements and JsonArrays under modification, outermost to innermost. */
  private final List<JsonElement> stack = new ArrayList<JsonElement>();

  /** The name for the next JSON object value. If non-null, the top of the stack is a JsonObject. */
  private String pendingName;

  /** the JSON element constructed by this writer. */
  private JsonElement product = JsonNull.INSTANCE; // TODO: is this really what we want?;

  /** Whether a {@code Writer} is currently writing a name or string */
  private boolean isWriterActive = false;

  public JsonTreeWriter() {
    super(UNWRITABLE_WRITER);
  }

  /**
   * Returns the top level object produced by this writer.
   */
  public JsonElement get() {
    verifyNoWriterActive();
    if (!stack.isEmpty()) {
      throw new IllegalStateException("Expected one JSON element but was " + stack);
    }
    return product;
  }

  private JsonElement peek() {
    return stack.get(stack.size() - 1);
  }

  /**
   * @throws IllegalStateException if a {@code Writer} is currently writing a name or string
   */
  private void verifyNoWriterActive() {
    if (isWriterActive) {
      throw new IllegalStateException("Writer is currently writing name or string");
    }
  }

  private void put(JsonElement value) {
    verifyNoWriterActive();
    if (pendingName != null) {
      if (!value.isJsonNull() || getSerializeNulls()) {
        JsonObject object = (JsonObject) peek();
        object.add(pendingName, value);
      }
      pendingName = null;
    } else if (stack.isEmpty()) {
      product = value;
    } else {
      JsonElement element = peek();
      if (element instanceof JsonArray) {
        ((JsonArray) element).add(value);
      } else {
        throw new IllegalStateException();
      }
    }
  }

  @Override public JsonWriter beginArray() throws IOException {
    JsonArray array = new JsonArray();
    put(array);
    stack.add(array);
    return this;
  }

  @Override public JsonWriter endArray() throws IOException {
    verifyNoWriterActive();
    if (stack.isEmpty() || pendingName != null) {
      throw new IllegalStateException();
    }
    JsonElement element = peek();
    if (element instanceof JsonArray) {
      stack.remove(stack.size() - 1);
      return this;
    }
    throw new IllegalStateException();
  }

  @Override public JsonWriter beginObject() throws IOException {
    JsonObject object = new JsonObject();
    put(object);
    stack.add(object);
    return this;
  }

  @Override public JsonWriter endObject() throws IOException {
    verifyNoWriterActive();
    if (stack.isEmpty() || pendingName != null) {
      throw new IllegalStateException();
    }
    JsonElement element = peek();
    if (element instanceof JsonObject) {
      stack.remove(stack.size() - 1);
      return this;
    }
    throw new IllegalStateException();
  }

  @Override public JsonWriter name(String name) throws IOException {
    if (name == null) {
      throw new NullPointerException("name == null");
    }
    verifyNoWriterActive();
    if (stack.isEmpty() || pendingName != null) {
      throw new IllegalStateException();
    }
    JsonElement element = peek();
    if (element instanceof JsonObject) {
      pendingName = name;
      return this;
    }
    throw new IllegalStateException();
  }

  @Override public Writer nameWriter() throws IOException {
    if (stack.isEmpty() || pendingName != null) {
      throw new IllegalStateException();
    }
    JsonElement element = peek();
    if (element instanceof JsonObject) {
      isWriterActive = true;
      return new StringValueWriter(true);
    }
    throw new IllegalStateException();
  }

  @Override public JsonWriter value(String value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    put(new JsonPrimitive(value));
    return this;
  }

  @Override public Writer stringValueWriter() throws IOException {
    verifyNoWriterActive();
    isWriterActive = true;
    return new StringValueWriter(false);
  }

  @Override public JsonWriter nullValue() throws IOException {
    put(JsonNull.INSTANCE);
    return this;
  }

  @Override public JsonWriter value(boolean value) throws IOException {
    put(new JsonPrimitive(value));
    return this;
  }

  @Override public JsonWriter value(Boolean value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    put(new JsonPrimitive(value));
    return this;
  }

  @Override public JsonWriter value(double value) throws IOException {
    if (!isLenient() && (Double.isNaN(value) || Double.isInfinite(value))) {
      throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
    }
    put(new JsonPrimitive(value));
    return this;
  }

  @Override public JsonWriter value(long value) throws IOException {
    put(new JsonPrimitive(value));
    return this;
  }

  @Override public JsonWriter value(Number value) throws IOException {
    if (value == null) {
      return nullValue();
    }

    if (!isLenient()) {
      double d = value.doubleValue();
      if (Double.isNaN(d) || Double.isInfinite(d)) {
        throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
      }
    }

    put(new JsonPrimitive(value));
    return this;
  }

  @Override public void flush() throws IOException {
  }

  @Override public void close() throws IOException {
    if (isWriterActive) {
      throw new IOException("Writer is currently writing name or string");
    } else if (!stack.isEmpty()) {
      throw new IOException("Incomplete document");
    }
    stack.add(SENTINEL_CLOSED);
  }
}
