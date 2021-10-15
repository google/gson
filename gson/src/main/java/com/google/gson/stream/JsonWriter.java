/*
 * Copyright (C) 2010 Google Inc.
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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import static com.google.gson.stream.JsonScope.DANGLING_NAME;
import static com.google.gson.stream.JsonScope.EMPTY_ARRAY;
import static com.google.gson.stream.JsonScope.EMPTY_DOCUMENT;
import static com.google.gson.stream.JsonScope.EMPTY_OBJECT;
import static com.google.gson.stream.JsonScope.NONEMPTY_ARRAY;
import static com.google.gson.stream.JsonScope.NONEMPTY_DOCUMENT;
import static com.google.gson.stream.JsonScope.NONEMPTY_OBJECT;

/**
 * Writes a JSON (<a href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>)
 * encoded value to a stream, one token at a time. The stream includes both
 * literal values (strings, numbers, booleans and nulls) as well as the begin
 * and end delimiters of objects and arrays.
 *
 * <h3>Encoding JSON</h3>
 * To encode your data as JSON, create a new {@code JsonWriter}. Each JSON
 * document must contain one top-level array or object. Call methods on the
 * writer as you walk the structure's contents, nesting arrays and objects as
 * necessary:
 * <ul>
 *   <li>To write <strong>arrays</strong>, first call {@link #beginArray()}.
 *       Write each of the array's elements with the appropriate {@link #value}
 *       methods or by nesting other arrays and objects. Finally close the array
 *       using {@link #endArray()}.
 *   <li>To write <strong>objects</strong>, first call {@link #beginObject()}.
 *       Write each of the object's properties by alternating calls to
 *       {@link #name} with the property's value. Write property values with the
 *       appropriate {@link #value} method or by nesting other objects or arrays.
 *       Finally close the object using {@link #endObject()}.
 * </ul>
 *
 * <p>Property names and string values of unknown length can be written using
 * the {@code Writer}s created by {@link #nameWriter()} and
 * {@link #stringValueWriter()}. However, while these writers are open no other
 * data can be written.
 *
 * <h3>Example</h3>
 * Suppose we'd like to encode a stream of messages such as the following: <pre> {@code
 * [
 *   {
 *     "id": 912345678901,
 *     "text": "How do I stream JSON in Java?",
 *     "geo": null,
 *     "user": {
 *       "name": "json_newb",
 *       "followers_count": 41
 *      }
 *   },
 *   {
 *     "id": 912345678902,
 *     "text": "@json_newb just use JsonWriter!",
 *     "geo": [50.454722, -104.606667],
 *     "user": {
 *       "name": "jesse",
 *       "followers_count": 2
 *     }
 *   }
 * ]}</pre>
 * This code encodes the above structure: <pre>   {@code
 *   public void writeJsonStream(OutputStream out, List<Message> messages) throws IOException {
 *     JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
 *     writer.setIndent("    ");
 *     writeMessagesArray(writer, messages);
 *     writer.close();
 *   }
 *
 *   public void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
 *     writer.beginArray();
 *     for (Message message : messages) {
 *       writeMessage(writer, message);
 *     }
 *     writer.endArray();
 *   }
 *
 *   public void writeMessage(JsonWriter writer, Message message) throws IOException {
 *     writer.beginObject();
 *     writer.name("id").value(message.getId());
 *     writer.name("text").value(message.getText());
 *     if (message.getGeo() != null) {
 *       writer.name("geo");
 *       writeDoublesArray(writer, message.getGeo());
 *     } else {
 *       writer.name("geo").nullValue();
 *     }
 *     writer.name("user");
 *     writeUser(writer, message.getUser());
 *     writer.endObject();
 *   }
 *
 *   public void writeUser(JsonWriter writer, User user) throws IOException {
 *     writer.beginObject();
 *     writer.name("name").value(user.getName());
 *     writer.name("followers_count").value(user.getFollowersCount());
 *     writer.endObject();
 *   }
 *
 *   public void writeDoublesArray(JsonWriter writer, List<Double> doubles) throws IOException {
 *     writer.beginArray();
 *     for (Double value : doubles) {
 *       writer.value(value);
 *     }
 *     writer.endArray();
 *   }}</pre>
 *
 * <p>Each {@code JsonWriter} may be used to write a single JSON stream.
 * Instances of this class are not thread safe. Calls that would result in a
 * malformed JSON string will fail with an {@link IllegalStateException}.
 *
 * @author Jesse Wilson
 * @since 1.6
 */
public class JsonWriter implements Closeable, Flushable {

  /*
   * From RFC 7159, "All Unicode characters may be placed within the
   * quotation marks except for the characters that must be escaped:
   * quotation mark, reverse solidus, and the control characters
   * (U+0000 through U+001F)."
   *
   * We also escape '\u2028' and '\u2029', which JavaScript interprets as
   * newline characters. This prevents eval() from failing with a syntax
   * error. http://code.google.com/p/google-gson/issues/detail?id=341
   */
  private static final String[] REPLACEMENT_CHARS;
  private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i <= 0x1f; i++) {
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
    }
    REPLACEMENT_CHARS['"'] = "\\\"";
    REPLACEMENT_CHARS['\\'] = "\\\\";
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\b'] = "\\b";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['\f'] = "\\f";
    HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
    HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
    HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
    HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
    HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
    HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
  }

  private class StringValueWriter extends Writer {
    private boolean isClosed = false;

    private void verifyNotClosed() throws IOException {
      if (isClosed) {
        throw new IOException("Writer is closed");
      }
    }

    @Override
    public void write(int c) throws IOException {
      verifyNotClosed();
      stringPiece((char) c);
    }

    /**
     * @param length length of the data, e.g. array length
     * @param offset 0-based offset where the section begins
     * @param sectionLength length of the section
     * @throws IndexOutOfBoundsException if {@code offset} or {@code sectionLength} is invalid
     */
    private void validateIndices(int length, int offset, int sectionLength) {
      if (offset < 0) {
        throw new IndexOutOfBoundsException("offset < 0");
      } else if (sectionLength < 0) {
        throw new IndexOutOfBoundsException("length < 0");
      } else if (sectionLength > length - offset) {
        throw new IndexOutOfBoundsException("length > data.length - offset");
      }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      verifyNotClosed();
      validateIndices(cbuf.length, off, len);
      stringPiece(cbuf, off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
      verifyNotClosed();
      validateIndices(str.length(), off, len);
      stringPiece(str.substring(off, off + len));
    }

    @Override
    public void write(String str) throws IOException {
      verifyNotClosed();
      stringPiece(str);
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
      verifyNotClosed();

      if (csq == null) {
        csq = "null"; // Requirement by Writer.append
      }

      validateIndices(csq.length(), start, end - start);
      stringPiece(csq.subSequence(start, end));
      return this;
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
      verifyNotClosed();

      if (csq == null) {
        stringPiece("null"); // Requirement by Writer.append
      } else {
        stringPiece(csq);
      }
      return this;
    }

    @Override
    public void flush() throws IOException {
      verifyNotClosed();
      JsonWriter.this.flush();
    }

    @Override
    public void close() throws IOException {
      if (!isClosed) {
        endString();
        flush();
        isClosed = true;
        // Update enclosing JsonWriter
        isWriterActive = false;
      }
    }
  }

  /**
   * Abstract class allowing to use the most efficient {@code Writer} methods for
   * certain types.
   */
  private static abstract class WritingCharSequence {
    public abstract int length();
    public abstract char charAt(int index);
    public abstract void writeTo(Writer out, int offset, int length) throws IOException;

    public static WritingCharSequence of(final char[] chars, final int start, final int length) {
      return new WritingCharSequence() {
        @Override
        public void writeTo(Writer out, int off, int len) throws IOException {
          out.write(chars, start + off, len);
        }

        @Override
        public int length() {
          return length;
        }

        @Override
        public char charAt(int index) {
          return chars[start + index];
        }
      };
    }

    public static WritingCharSequence of(final CharSequence charSequence) {
      return new WritingCharSequence() {
        @Override
        public void writeTo(Writer out, int off, int len) throws IOException {
          out.append(charSequence, off, off + len);
        }

        @Override
        public int length() {
          return charSequence.length();
        }

        @Override
        public char charAt(int index) {
          return charSequence.charAt(index);
        }
      };
    }

    public static WritingCharSequence of(final String string) {
      return new WritingCharSequence() {
        @Override
        public void writeTo(Writer out, int off, int len) throws IOException {
          out.write(string, off, len);
        }

        @Override
        public int length() {
          return string.length();
        }

        @Override
        public char charAt(int index) {
          return string.charAt(index);
        }
      };
    }

    public static WritingCharSequence of(final char c) {
      return new WritingCharSequence() {
        @Override
        public void writeTo(Writer out, int off, int len) throws IOException {
          out.write(c);
        }

        @Override
        public int length() {
          return 1;
        }

        @Override
        public char charAt(int index) {
          return c;
        }
      };
    }
  }

  /** The output data, containing at most one top-level array or object. */
  private final Writer out;

  private int[] stack = new int[32];
  private int stackSize = 0;
  {
    push(EMPTY_DOCUMENT);
  }

  /**
   * A string containing a full set of spaces for a single level of
   * indentation, or null for no pretty printing.
   */
  private String indent;

  /**
   * The name/value separator; either ":" or ": ".
   */
  private String separator = ":";

  private boolean lenient;

  private boolean htmlSafe;

  private String deferredName;
  private boolean expectsPropertyValue;

  private boolean serializeNulls = true;

  /** Whether a {@code Writer} is currently writing a name or string */
  private boolean isWriterActive = false;

  /**
   * Creates a new instance that writes a JSON-encoded stream to {@code out}.
   * For best performance, ensure {@link Writer} is buffered; wrapping in
   * {@link java.io.BufferedWriter BufferedWriter} if necessary.
   */
  public JsonWriter(Writer out) {
    if (out == null) {
      throw new NullPointerException("out == null");
    }
    this.out = out;
  }

  /**
   * Sets the indentation string to be repeated for each level of indentation
   * in the encoded document. If {@code indent.isEmpty()} the encoded document
   * will be compact. Otherwise the encoded document will be more
   * human-readable.
   *
   * @param indent a string containing only whitespace.
   */
  public final void setIndent(String indent) {
    if (indent.length() == 0) {
      this.indent = null;
      this.separator = ":";
    } else {
      this.indent = indent;
      this.separator = ": ";
    }
  }

  /**
   * Configure this writer to relax its syntax rules. By default, this writer
   * only emits well-formed JSON as specified by <a
   * href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>. Setting the writer
   * to lenient permits the following:
   * <ul>
   *   <li>Top-level values of any type. With strict writing, the top-level
   *       value must be an object or an array.
   *   <li>Numbers may be {@link Double#isNaN() NaNs} or {@link
   *       Double#isInfinite() infinities}.
   * </ul>
   */
  public final void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

  /**
   * Returns true if this writer has relaxed syntax rules.
   */
  public boolean isLenient() {
    return lenient;
  }

  /**
   * Configure this writer to emit JSON that's safe for direct inclusion in HTML
   * and XML documents. This escapes the HTML characters {@code <}, {@code >},
   * {@code &} and {@code =} before writing them to the stream. Without this
   * setting, your XML/HTML encoder should replace these characters with the
   * corresponding escape sequences.
   */
  public final void setHtmlSafe(boolean htmlSafe) {
    this.htmlSafe = htmlSafe;
  }

  /**
   * Returns true if this writer writes JSON that's safe for inclusion in HTML
   * and XML documents.
   */
  public final boolean isHtmlSafe() {
    return htmlSafe;
  }

  /**
   * Sets whether object members are serialized when their value is null.
   * This has no impact on array elements. The default is true.
   */
  public final void setSerializeNulls(boolean serializeNulls) {
    this.serializeNulls = serializeNulls;
  }

  /**
   * Returns true if object members are serialized when their value is null.
   * This has no impact on array elements. The default is true.
   */
  public final boolean getSerializeNulls() {
    return serializeNulls;
  }

  /**
   * @throws IllegalStateException if a {@code Writer} is currently writing a name or string
   */
  private void verifyNoWriterActive() {
    if (isWriterActive) {
      throw new IllegalStateException("Writer is currently writing name or string");
    }
  }

  /**
   * Begins encoding a new array. Each call to this method must be paired with
   * a call to {@link #endArray}.
   *
   * @return this writer.
   */
  public JsonWriter beginArray() throws IOException {
    verifyNoWriterActive();
    writeDeferredName();
    return open(EMPTY_ARRAY, '[');
  }

  /**
   * Ends encoding the current array.
   *
   * @return this writer.
   */
  public JsonWriter endArray() throws IOException {
    verifyNoWriterActive();
    return close(EMPTY_ARRAY, NONEMPTY_ARRAY, ']');
  }

  /**
   * Begins encoding a new object. Each call to this method must be paired
   * with a call to {@link #endObject}.
   *
   * @return this writer.
   */
  public JsonWriter beginObject() throws IOException {
    verifyNoWriterActive();
    writeDeferredName();
    return open(EMPTY_OBJECT, '{');
  }

  /**
   * Ends encoding the current object.
   *
   * @return this writer.
   */
  public JsonWriter endObject() throws IOException {
    verifyNoWriterActive();
    return close(EMPTY_OBJECT, NONEMPTY_OBJECT, '}');
  }

  /**
   * Enters a new scope by appending any necessary whitespace and the given
   * bracket.
   */
  private JsonWriter open(int empty, char openBracket) throws IOException {
    beforeValue();
    push(empty);
    out.write(openBracket);
    return this;
  }

  /**
   * Closes the current scope by appending any necessary whitespace and the
   * given bracket.
   */
  private JsonWriter close(int empty, int nonempty, char closeBracket)
      throws IOException {
    int context = peek();
    if (context != nonempty && context != empty) {
      throw new IllegalStateException("Nesting problem.");
    }
    if (expectsPropertyValue) {
      throw new IllegalStateException("Expecting property value.");
    }

    stackSize--;
    if (context == nonempty) {
      newline();
    }
    out.write(closeBracket);
    return this;
  }

  private void push(int newTop) {
    if (stackSize == stack.length) {
      stack = Arrays.copyOf(stack, stackSize * 2);
    }
    stack[stackSize++] = newTop;
  }

  /**
   * Returns the value on the top of the stack.
   */
  private int peek() {
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    return stack[stackSize - 1];
  }

  /**
   * Replace the value on the top of the stack with the given value.
   */
  private void replaceTop(int topOfStack) {
    stack[stackSize - 1] = topOfStack;
  }

  /**
   * Encodes the property name.
   *
   * <p>When writing property names of unknown length {@link #nameWriter()}
   * should be preferred.
   *
   * @param name the name of the forthcoming value. May not be null.
   * @return this writer.
   */
  public JsonWriter name(String name) throws IOException {
    verifyNoWriterActive();

    if (name == null) {
      throw new NullPointerException("name == null");
    }
    if (expectsPropertyValue) {
      throw new IllegalStateException("Expecting property value.");
    }
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    deferredName = name;
    expectsPropertyValue = true;
    return this;
  }

  private void writeDeferredName() throws IOException {
    if (deferredName != null) {
      beforeName();
      string(deferredName);
      deferredName = null;
    }

    // nameWriter does not use deferredName
    expectsPropertyValue = false;
  }

  /**
   * Creates a new writer encoding a property name. The returned writer has
   * to be closed to indicate that writing the name has finished. No other
   * data can be written until the writer is closed.
   *
   * <p>For efficiency reasons implementations may always write the property,
   * regardless of whether {@linkplain #getSerializeNulls() null values
   * should be serialized}.
   *
   * @return writer for writing a property name
   * @throws IOException if creating the writer fails
   */
  public Writer nameWriter() throws IOException {
    verifyNoWriterActive();
    beforeName();
    beginString();
    isWriterActive = true;
    expectsPropertyValue = true;
    return new StringValueWriter();
  }

  /**
   * Encodes {@code value}.
   *
   * <p>When writing strings of unknown length {@link #stringValueWriter()}
   * should be preferred.
   *
   * @param value the literal string value, or null to encode a null literal.
   * @return this writer.
   */
  public JsonWriter value(String value) throws IOException {
    verifyNoWriterActive();

    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    string(value);
    return this;
  }

  /**
   * Creates a new writer encoding a string {@code value}. The returned writer
   * has to be closed to indicate that writing the value has finished. No other
   * data can be written until the writer is closed.
   *
   * @return writer for writing a string value
   * @throws IOException if creating the writer fails
   */
  public Writer stringValueWriter() throws IOException {
    verifyNoWriterActive();
    writeDeferredName();
    beforeValue();
    beginString();
    isWriterActive = true;
    return new StringValueWriter();
  }

  /**
   * Writes {@code value} directly to the writer without quoting or
   * escaping.
   *
   * @param value the literal string value, or null to encode a null literal.
   * @return this writer.
   */
  public JsonWriter jsonValue(String value) throws IOException {
    verifyNoWriterActive();

    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    out.append(value);
    return this;
  }

  /**
   * Encodes {@code null}.
   *
   * @return this writer.
   */
  public JsonWriter nullValue() throws IOException {
    verifyNoWriterActive();
    // Clear flag here because writeDeferredName() might not be called
    expectsPropertyValue = false;

    if (deferredName != null) {
      if (serializeNulls) {
        writeDeferredName();
      } else {
        deferredName = null;
        return this; // skip the name and the value
      }
    }
    beforeValue();
    out.write("null");
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public JsonWriter value(boolean value) throws IOException {
    verifyNoWriterActive();
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public JsonWriter value(Boolean value) throws IOException {
    verifyNoWriterActive();
    if (value == null) {
      return nullValue();
    }
    writeDeferredName();
    beforeValue();
    out.write(value ? "true" : "false");
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
   *     {@link Double#isInfinite() infinities}.
   * @return this writer.
   */
  public JsonWriter value(double value) throws IOException {
    verifyNoWriterActive();
    writeDeferredName();
    if (!lenient && (Double.isNaN(value) || Double.isInfinite(value))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    beforeValue();
    out.append(Double.toString(value));
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public JsonWriter value(long value) throws IOException {
    verifyNoWriterActive();
    writeDeferredName();
    beforeValue();
    out.write(Long.toString(value));
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
   *     {@link Double#isInfinite() infinities}.
   * @return this writer.
   */
  public JsonWriter value(Number value) throws IOException {
    verifyNoWriterActive();

    if (value == null) {
      return nullValue();
    }

    writeDeferredName();
    String string = value.toString();
    if (!lenient
        && (string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN"))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    beforeValue();
    out.append(string);
    return this;
  }

  /**
   * Ensures all buffered data is written to the underlying {@link Writer}
   * and flushes that writer.
   */
  public void flush() throws IOException {
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    out.flush();
  }

  /**
   * Flushes and closes this writer and the underlying {@link Writer}.
   *
   * @throws IOException if the JSON document is incomplete.
   */
  public void close() throws IOException {
    out.close();

    if (isWriterActive) {
      throw new IOException("Writer is currently writing name or string");
    }

    int size = stackSize;
    if (size > 1 || size == 1 && stack[size - 1] != NONEMPTY_DOCUMENT) {
      throw new IOException("Incomplete document");
    }
    stackSize = 0;
  }

  private void beginString() throws IOException {
    out.write('\"');
  }

  private void stringPiece(WritingCharSequence charSequence) throws IOException {
    String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
    int last = 0;
    int length = charSequence.length();
    for (int i = 0; i < length; i++) {
      char c = charSequence.charAt(i);
      String replacement;
      if (c < 128) {
        replacement = replacements[c];
        if (replacement == null) {
          continue;
        }
      } else if (c == '\u2028') {
        replacement = "\\u2028";
      } else if (c == '\u2029') {
        replacement = "\\u2029";
      } else {
        continue;
      }
      if (last < i) {
        charSequence.writeTo(out, last, i - last);
      }
      out.write(replacement);
      last = i + 1;
    }
    if (last < length) {
      charSequence.writeTo(out, last, length - last);
    }
  }

  private void stringPiece(char[] chars, int start, int length) throws IOException {
    stringPiece(WritingCharSequence.of(chars, start, length));
  }

  private void stringPiece(CharSequence charSequence) throws IOException {
    stringPiece(WritingCharSequence.of(charSequence));
  }

  private void stringPiece(String string) throws IOException {
    stringPiece(WritingCharSequence.of(string));
  }

  private void stringPiece(char c) throws IOException {
    stringPiece(WritingCharSequence.of(c));
  }

  private void endString() throws IOException {
    out.write('\"');
  }

  private void string(String value) throws IOException {
    beginString();
    stringPiece(value);
    endString();
  }

  private void newline() throws IOException {
    if (indent == null) {
      return;
    }

    out.write('\n');
    for (int i = 1, size = stackSize; i < size; i++) {
      out.write(indent);
    }
  }

  /**
   * Inserts any necessary separators and whitespace before a name. Also
   * adjusts the stack to expect the name's value.
   */
  private void beforeName() throws IOException {
    int context = peek();
    if (context == NONEMPTY_OBJECT) { // first in object
      out.write(',');
    } else if (context != EMPTY_OBJECT) { // not in an object!
      throw new IllegalStateException("Nesting problem.");
    }
    newline();
    replaceTop(DANGLING_NAME);
  }

  /**
   * Inserts any necessary separators and whitespace before a literal value,
   * inline array, or inline object. Also adjusts the stack to expect either a
   * closing bracket or another element.
   */
  @SuppressWarnings("fallthrough")
  private void beforeValue() throws IOException {
    switch (peek()) {
    case NONEMPTY_DOCUMENT:
      if (!lenient) {
        throw new IllegalStateException(
            "JSON must have only one top-level value.");
      }
      // fall-through
    case EMPTY_DOCUMENT: // first in document
      replaceTop(NONEMPTY_DOCUMENT);
      break;

    case EMPTY_ARRAY: // first in array
      replaceTop(NONEMPTY_ARRAY);
      newline();
      break;

    case NONEMPTY_ARRAY: // another in array
      out.append(',');
      newline();
      break;

    case DANGLING_NAME: // value for name
      out.append(separator);
      replaceTop(NONEMPTY_OBJECT);
      break;

    default:
      throw new IllegalStateException("Nesting problem.");
    }
  }
}
