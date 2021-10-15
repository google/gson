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

import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.bind.AbstractStringValueReaderStringImpl;
import com.google.gson.internal.bind.JsonTreeReader;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;

/**
 * Reads a JSON (<a href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>)
 * encoded value as a stream of tokens. This stream includes both literal
 * values (strings, numbers, booleans, and nulls) as well as the begin and
 * end delimiters of objects and arrays. The tokens are traversed in
 * depth-first order, the same order that they appear in the JSON document.
 * Within JSON objects, name/value pairs are represented by a single token.
 *
 * <h3>Parsing JSON</h3>
 * To create a recursive descent parser for your own JSON streams, first create
 * an entry point method that creates a {@code JsonReader}.
 *
 * <p>Next, create handler methods for each structure in your JSON text. You'll
 * need a method for each object type and for each array type.
 * <ul>
 *   <li>Within <strong>array handling</strong> methods, first call {@link
 *       #beginArray} to consume the array's opening bracket. Then create a
 *       while loop that accumulates values, terminating when {@link #hasNext}
 *       is false. Finally, read the array's closing bracket by calling {@link
 *       #endArray}.
 *   <li>Within <strong>object handling</strong> methods, first call {@link
 *       #beginObject} to consume the object's opening brace. Then create a
 *       while loop that assigns values to local variables based on their name.
 *       This loop should terminate when {@link #hasNext} is false. Finally,
 *       read the object's closing brace by calling {@link #endObject}.
 * </ul>
 * <p>When a nested object or array is encountered, delegate to the
 * corresponding handler method.
 *
 * <p>When an unknown name is encountered, strict parsers should fail with an
 * exception. Lenient parsers should call {@link #skipValue()} to recursively
 * skip the value's nested tokens, which may otherwise conflict.
 *
 * <p>If a value may be null, you should first check using {@link #peek()}.
 * Null literals can be consumed using either {@link #nextNull()} or {@link
 * #skipValue()}.
 *
 * <h3>Example</h3>
 * Suppose we'd like to parse a stream of messages such as the following: <pre> {@code
 * [
 *   {
 *     "id": 912345678901,
 *     "text": "How do I read a JSON stream in Java?",
 *     "geo": null,
 *     "user": {
 *       "name": "json_newb",
 *       "followers_count": 41
 *      }
 *   },
 *   {
 *     "id": 912345678902,
 *     "text": "@json_newb just use JsonReader!",
 *     "geo": [50.454722, -104.606667],
 *     "user": {
 *       "name": "jesse",
 *       "followers_count": 2
 *     }
 *   }
 * ]}</pre>
 * This code implements the parser for the above structure: <pre>   {@code
 *
 *   public List<Message> readJsonStream(InputStream in) throws IOException {
 *     JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
 *     try {
 *       return readMessagesArray(reader);
 *     } finally {
 *       reader.close();
 *     }
 *   }
 *
 *   public List<Message> readMessagesArray(JsonReader reader) throws IOException {
 *     List<Message> messages = new ArrayList<Message>();
 *
 *     reader.beginArray();
 *     while (reader.hasNext()) {
 *       messages.add(readMessage(reader));
 *     }
 *     reader.endArray();
 *     return messages;
 *   }
 *
 *   public Message readMessage(JsonReader reader) throws IOException {
 *     long id = -1;
 *     String text = null;
 *     User user = null;
 *     List<Double> geo = null;
 *
 *     reader.beginObject();
 *     while (reader.hasNext()) {
 *       String name = reader.nextName();
 *       if (name.equals("id")) {
 *         id = reader.nextLong();
 *       } else if (name.equals("text")) {
 *         text = reader.nextString();
 *       } else if (name.equals("geo") && reader.peek() != JsonToken.NULL) {
 *         geo = readDoublesArray(reader);
 *       } else if (name.equals("user")) {
 *         user = readUser(reader);
 *       } else {
 *         reader.skipValue();
 *       }
 *     }
 *     reader.endObject();
 *     return new Message(id, text, user, geo);
 *   }
 *
 *   public List<Double> readDoublesArray(JsonReader reader) throws IOException {
 *     List<Double> doubles = new ArrayList<Double>();
 *
 *     reader.beginArray();
 *     while (reader.hasNext()) {
 *       doubles.add(reader.nextDouble());
 *     }
 *     reader.endArray();
 *     return doubles;
 *   }
 *
 *   public User readUser(JsonReader reader) throws IOException {
 *     String username = null;
 *     int followersCount = -1;
 *
 *     reader.beginObject();
 *     while (reader.hasNext()) {
 *       String name = reader.nextName();
 *       if (name.equals("name")) {
 *         username = reader.nextString();
 *       } else if (name.equals("followers_count")) {
 *         followersCount = reader.nextInt();
 *       } else {
 *         reader.skipValue();
 *       }
 *     }
 *     reader.endObject();
 *     return new User(username, followersCount);
 *   }}</pre>
 *
 * <h3>Number Handling</h3>
 * This reader permits numeric values to be read as strings and string values to
 * be read as numbers. For example, both elements of the JSON array {@code
 * [1, "1"]} may be read using either {@link #nextInt} or {@link #nextString}.
 * This behavior is intended to prevent lossy numeric conversions: double is
 * JavaScript's only numeric type and very large values like {@code
 * 9007199254740993} cannot be represented exactly on that platform. To minimize
 * precision loss, extremely large values should be written and read as strings
 * in JSON.
 *
 * <a id="nonexecuteprefix"/><h3>Non-Execute Prefix</h3>
 * Web servers that serve private data using JSON may be vulnerable to <a
 * href="http://en.wikipedia.org/wiki/JSON#Cross-site_request_forgery">Cross-site
 * request forgery</a> attacks. In such an attack, a malicious site gains access
 * to a private JSON file by executing it with an HTML {@code <script>} tag.
 *
 * <p>Prefixing JSON files with <code>")]}'\n"</code> makes them non-executable
 * by {@code <script>} tags, disarming the attack. Since the prefix is malformed
 * JSON, strict parsing fails when it is encountered. This class permits the
 * non-execute prefix when {@link #setLenient(boolean) lenient parsing} is
 * enabled.
 *
 * <p>Each {@code JsonReader} may be used to read a single JSON stream. Instances
 * of this class are not thread safe.
 *
 * @author Jesse Wilson
 * @since 1.6
 */
public class JsonReader implements Closeable {
  private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;
  /** Special JSON path value indicating that Reader was used for property name */
  private static final String STREAMED_NAME = "#streamedName";
  /** Special JSON path value indicating that property name was skipped */
  private static final String SKIPPED_NAME = "#skippedName"; // Also used in JsonTreeReader

  private static final int PEEKED_NONE = 0;
  private static final int PEEKED_BEGIN_OBJECT = 1;
  private static final int PEEKED_END_OBJECT = 2;
  private static final int PEEKED_BEGIN_ARRAY = 3;
  private static final int PEEKED_END_ARRAY = 4;
  private static final int PEEKED_TRUE = 5;
  private static final int PEEKED_FALSE = 6;
  private static final int PEEKED_NULL = 7;
  private static final int PEEKED_SINGLE_QUOTED = 8;
  private static final int PEEKED_DOUBLE_QUOTED = 9;
  private static final int PEEKED_UNQUOTED = 10;
  /** When this is returned, the string value is stored in peekedString. */
  private static final int PEEKED_BUFFERED = 11;
  private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
  private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
  private static final int PEEKED_UNQUOTED_NAME = 14;
  /** When this is returned, the integer value is stored in peekedLong. */
  private static final int PEEKED_LONG = 15;
  private static final int PEEKED_NUMBER = 16;
  private static final int PEEKED_EOF = 17;
  /** When this is returned, string is currently read by reader */
  private static final int PEEKED_STRING_READER = 18;

  /* State machine when parsing numbers */
  private static final int NUMBER_CHAR_NONE = 0;
  private static final int NUMBER_CHAR_SIGN = 1;
  private static final int NUMBER_CHAR_DIGIT = 2;
  private static final int NUMBER_CHAR_DECIMAL = 3;
  private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
  private static final int NUMBER_CHAR_EXP_E = 5;
  private static final int NUMBER_CHAR_EXP_SIGN = 6;
  private static final int NUMBER_CHAR_EXP_DIGIT = 7;

  private static abstract class CharsConsumer {
    public void accept(char[] chars, int start, int length) {
      for (int i = 0; i < length; i++) {
        accept(chars[start + i]);
      }
    }

    /**
     * Called when no more data follows after this. Implementations can
     * use this hint to process the data more efficiently. It is not
     * required that this method has to be called.
     */
    public void acceptAndFinish(char[] chars, int start, int length) {
      accept(chars, start, length);
    }

    public abstract void accept(char c);
  }

  private static class ArrayCharsConsumer extends CharsConsumer {
    private final char[] array;
    private final int start;
    private int currrentIndex;

    private ArrayCharsConsumer(char[] array, int start) {
      this.array = array;
      this.start = start;
      this.currrentIndex = this.start;
    }

    @Override
    public void accept(char[] chars, int start, int length) {
      System.arraycopy(chars, start, array, currrentIndex, length);
      currrentIndex += length;
    }

    @Override
    public void accept(char c) {
      array[currrentIndex++] = c;
    }

    public int accepted() {
      return currrentIndex - start;
    }
  }

  private static class CharBufferCharsConsumer extends CharsConsumer {
    private final CharBuffer charBuffer;
    private int accepted;

    private CharBufferCharsConsumer(CharBuffer charBuffer) {
      this.charBuffer = charBuffer;
      this.accepted = 0;
    }

    @Override
    public void accept(char[] chars, int start, int length) {
      charBuffer.put(chars, start, length);
      accepted += length;
    }

    @Override
    public void accept(char c) {
      charBuffer.put(c);
      accepted++;
    }

    public int accepted() {
      return accepted;
    }
  }

  private static class StringBuildingCharsConsumer extends CharsConsumer {
    /** {@code null} if nothing has been consumed yet */
    private StringBuilder stringBuilder;
    /**
     * non-{@code null} if string has directly been created, {@link #stringBuilder} is
     * {@code null} then
     */
    private String string;

    private StringBuildingCharsConsumer() {
      stringBuilder = null;
    }

    @Override
    public void accept(char[] chars, int start, int length) {
      if (stringBuilder == null) {
        stringBuilder = new StringBuilder(length <= 8 ? 16 : length * 2);
      }

      stringBuilder.append(chars, start, length);
    }

    @Override
    public void acceptAndFinish(char[] chars, int start, int length) {
      if (stringBuilder == null) {
        string = new String(chars, start, length);
      } else {
        // Might unnecessarily increase capacity to oldCap * 2 + 2
        // Cannot solve this without relying on implementation details (e.g. JEP 280),
        // see https://stackoverflow.com/q/58672391
        stringBuilder.append(chars, start, length);
      }
    }

    @Override
    public void accept(char c) {
      if (stringBuilder == null) {
        stringBuilder = new StringBuilder();
      }

      stringBuilder.append(c);
    }

    public String build() {
      if (string != null) {
        return string;
      } else {
        return stringBuilder == null ? "" : stringBuilder.toString();
      }
    }
  }

  private static class SingleCharConsumer extends CharsConsumer {
    private int c;

    @Override
    public void accept(char c) {
      this.c = c;
    }
  }

  /**
   * A {@link Reader} for reading JSON property names and string values.
   * Reader instances are obtained through the {@link JsonReader#nextNameReader()}
   * and {@link JsonReader#nextStringReader()} methods.
   *
   * <p>In addition to the standard {@code Reader} methods this class defines
   * the following methods which might perform better than the standard methods
   * in certain cases:
   * <ul>
   *   <li>{@link #readAtLeast(char[], int, int, int)}</li>
   *   <li>{@link #readGreedily(char[], int, int)}</li>
   *   <li>{@link #skipExactly(long)}</li>
   *   <li>{@link #skipRemaining()}</li>
   * </ul>
   *
   * <p>When finished reading a value you have to make sure that the end
   * of the stream has been reached, which is the case when either one of
   * the {@code read} methods returned -1 or after calling {@link #skipRemaining()}.
   * To be on the safe side it is recommended to always call {@code skipRemaining()}.
   * Afterwards you have to {@link #close()} this reader.<br>
   * Failing to do either of this can render this reader and the underlying
   * {@code JsonReader} unusable.<br>
   * The recommended pattern is therefore:
   * <pre>
   * try (StringValueReader stringValueReader = jsonReader.nextStringReader()) {
   *     // Use the reader
   *     ...
   *
   *     // Skip any remaining characters before closing the reader
   *     stringValueReader.skipRemaining();
   * }
   * </pre>
   */
  public static abstract class StringValueReader extends Reader {
    /**
     * Tries to read at least {@code minLen} and at most {@code maxLen} characters into
     * the given char array, blocks until at least {@code minLen} characters have been
     * read, an IO error occurs or the end of the stream has been reached. If {@code maxLen}
     * &gt; 0 tries to read at least one character even if {@code minLen} is 0, unless
     * the end of the stream has been reached.
     *
     * @param cbuf destination for the read characters
     * @param off at which 0-based position to start storing the characters in {@code cbuf}
     * @param minLen minimum number of characters to read
     * @param maxLen maximum number of characters to read
     * @return number of read characters; always &gt;= 0 even if the end of the
     *   stream has been reached
     * @throws IndexOutOfBoundsException if any of the following applies
     *   <ul>
     *   <li>{@code off} &lt; 0</li>
     *   <li>{@code minLen} &lt; 0</li>
     *   <li>{@code minLen} &gt; {@code maxLen}</li>
     *   <li>{@code maxLen} &gt; {@code cbuf.length} - {@code off}</li>
     *   </ul>
     * @throws IOException if reading fails
     * @throws EOFException if less than {@code minLen} characters are remaining;
     *   the state of the reader and the content of {@code cbuf} are undefined afterwards,
     *   it is possible that some characters have already been read and stored in {@code cbuf}
     */
    public abstract int readAtLeast(char[] cbuf, int off, int minLen, int maxLen) throws IOException, EOFException;

    /**
     * Tries to read as much characters as possible, only returning when the requested
     * number of characters have been read, an IO error occurs or the end of the
     * stream has been reached.
     *
     * @param cbuf destination for the read characters
     * @param off at which 0-based position to start storing the characters in {@code cbuf}
     * @param len number of characters to read
     * @return number of read characters, or -1 if the end of the
     *   stream has been reached and no characters have been read
     * @throws IndexOutOfBoundsException if any of the following applies
     *   <ul>
     *   <li>{@code off} &lt; 0</li>
     *   <li>{@code len} &lt; 0</li>
     *   <li>{@code len} &gt; {@code cbuf.length} - {@code off}</li>
     *   </ul>
     * @throws IOException if reading fails
     */
    public abstract int readGreedily(char[] cbuf, int off, int len) throws IOException;

    /**
     * Tries to skip exactly {@code skipAmount} characters and blocks until at least
     * that amount of characters have been skipped.
     *
     * @param skipAmount the number of characters to skip
     * @throws IllegalArgumentException if {@code skipAmount} &lt; 0
     * @throws IOException if skipping fails
     * @throws EOFException if less than {@code skipAmount} characters are remaining;
     *   the state of the reader is undefined afterwards, it is possible that some
     *   characters have already been skipped
     */
    public abstract void skipExactly(long skipAmount) throws IOException, EOFException;

    /**
     * Skips all remaining characters and blocks until the end of the stream has
     * been reached. However, the stream is not {@link #close() closed} automatically
     * afterwards.
     *
     * @throws IOException if skipping fails
     */
    public abstract void skipRemaining() throws IOException;

    /**
     * {@inheritDoc}
     *
     * <p>Closing this reader will not consume any remaining characters which
     * have not been consumed yet. This allows usage within a {@code try-with-resources}
     * statement to fail fast in case of an exception. However, it requires
     * that you have to consume all remaining characters (for example using
     * {@link #skipRemaining()}) before closing this reader if you intend to
     * read more JSON tokens. Failing to do so can render the underlying
     * {@code JsonReader} unusable.
     */
    public abstract void close() throws IOException;
  }

  class StringValueReaderImpl extends StringValueReader {
    private static final char NO_QUOTE = '\0';
    /**
     * Minimum number of chars which should be tried to be read.
     * Mostly needed for cases where {@link #in} is rarely {@link Reader#ready()}.
     */
    static final int MIN_DESIRED_ACCEPT = 16; // Might need adjustment

    /**
     * {@code true} if object property name is read,
     * {@code false} if string value is read
     */
    private boolean isReadingName;
    /** Quote char or {@link #NO_QUOTE} */
    private final char quote;
    /** Used only by {@link #read()} to read one char */
    private SingleCharConsumer singleCharConsumer;
    private boolean reachedEnd;
    private boolean isClosed;

    private StringValueReaderImpl(boolean isReadingName, char quote) {
      this.isReadingName = isReadingName;
      this.quote = quote;
      this.singleCharConsumer = new SingleCharConsumer();
      this.reachedEnd = false;
      this.isClosed = false;
    }

    /** Reads unquoted string */
    private StringValueReaderImpl(boolean isReadingName) {
      this(isReadingName, NO_QUOTE);
    }

    private void verifyNotClosed() throws IOException {
      if (isClosed) {
        throw new IOException("Reader is closed");
      }
    }

    /**
     * @param charsConsumer consuming the read chars
     * @param minDesiredAccept desired minimum number of chars which should be read;
     *   might read less chars if end of value is reached before;
     *   <b>must be &gt;= 1</b>
     * @param maxAccept maximum number of chars to pass to charsConsumer;
     *   <b>must be &gt;= 1</b>
     * @return whether the end of the value has been reached
     * @throws IOException if reading fails
     */
    private boolean read(CharsConsumer charsConsumer, int minDesiredAccept, int maxAccept) throws IOException {
      if (reachedEnd) {
        return true;
      }

      if (quote == NO_QUOTE) {
        return reachedEnd = nextUnquotedValue(charsConsumer, minDesiredAccept, maxAccept);
      } else {
        return reachedEnd = nextQuotedValue(quote, charsConsumer, minDesiredAccept, maxAccept);
      }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      if (off < 0) {
        throw new IndexOutOfBoundsException("offset < 0");
      } else if (len < 0) {
        throw new IndexOutOfBoundsException("length < 0");
      } else if (len > cbuf.length - off) {
        throw new IndexOutOfBoundsException("length > arr.length - offset");
      }

      verifyNotClosed();
      if (len == 0) {
        return 0;
      }

      ArrayCharsConsumer charsConsumer = new ArrayCharsConsumer(cbuf, off);
      // Do not have to check if end has already been reached, read does that
      boolean reachedEnd = read(charsConsumer, Math.min(MIN_DESIRED_ACCEPT, len), len);
      int accepted = charsConsumer.accepted();
      return reachedEnd && accepted == 0 ? -1 : accepted;
    }

    @Override
    public int read() throws IOException {
      verifyNotClosed();

      singleCharConsumer.c = -1;
      // Do not have to check if end has already been reached, read does that
      read(singleCharConsumer, 1, 1);
      // If no char was read value is still -1 so don't have to check `read()`
      // return value
      return singleCharConsumer.c;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
      // read(CharBuffer) is not clear about how to behave for full (remaining() == 0)
      // buffer, see also JDK-8222329
      if (target.isReadOnly()) {
        throw new ReadOnlyBufferException();
      }
      verifyNotClosed();

      int length = target.remaining();
      if (length == 0) {
        return 0;
      }

      CharBufferCharsConsumer charsConsumer = new CharBufferCharsConsumer(target);
      // Do not have to check if end has already been reached, read does that
      boolean reachedEnd = read(charsConsumer, Math.min(MIN_DESIRED_ACCEPT, length), length);
      int accepted = charsConsumer.accepted();
      return reachedEnd && accepted == 0 ? -1 : accepted;
    }

    @Override
    public int readAtLeast(char[] cbuf, int off, int minLen, int maxLen) throws IOException {
      if (off < 0) {
        throw new IndexOutOfBoundsException("offset < 0");
      } else if (minLen < 0) {
        throw new IndexOutOfBoundsException("minLen < 0");
      } else if (minLen > maxLen) {
        throw new IndexOutOfBoundsException("minLen > maxLen");
      } else if (maxLen > cbuf.length - off) {
        throw new IndexOutOfBoundsException("maxLen > arr.length - offset");
      }

      verifyNotClosed();
      if (maxLen == 0) {
        return 0;
      }

      ArrayCharsConsumer charsConsumer = new ArrayCharsConsumer(cbuf, off);
      // Always try to at least read MIN_DESIRED_ACCEPT chars to be efficient
      int minDesiredAccept = Math.min(Math.max(MIN_DESIRED_ACCEPT, minLen), maxLen);
      // Do not have to check if end has already been reached, read does that
      boolean reachedEnd = read(charsConsumer, minDesiredAccept, maxLen);
      int accepted = charsConsumer.accepted();
      if (accepted < minLen) {
        assert reachedEnd;
        throw new EOFException("Read less than the requested " + minLen + " chars");
      }
      return accepted;
    }

    @Override
    public int readGreedily(char[] cbuf, int off, int len) throws IOException {
      if (off < 0) {
        throw new IndexOutOfBoundsException("offset < 0");
      } else if (len < 0) {
        throw new IndexOutOfBoundsException("length < 0");
      } else if (len > cbuf.length - off) {
        throw new IndexOutOfBoundsException("length > arr.length - offset");
      }

      verifyNotClosed();
      if (len == 0) {
        return 0;
      }

      ArrayCharsConsumer charsConsumer = new ArrayCharsConsumer(cbuf, off);
      // Do not have to check if end has already been reached, read does that
      boolean reachedEnd = read(charsConsumer, len, len);
      int accepted = charsConsumer.accepted();
      return reachedEnd && accepted == 0 ? -1 : accepted;
    }

    @Override
    public boolean ready() throws IOException {
      verifyNotClosed();

      // If reachedEnd read() won't block, see also JDK-8196767
      if (reachedEnd) {
        return true;
      } else if (limit - pos > 0) {
        // Unquoted string has no escape sequences so will not block
        // In case of quoted string `\` starts an escape sequence and reading it might block
        if (quote == NO_QUOTE || buffer[pos] != '\\') {
          return true;
        }

        int remaining = limit - pos;
        // Either contains at least complete \ + uXXXX escape sequence (6 chars)
        // or is non-unicode escape sequence, e.g. \t (2 chars)
        return remaining >= 6 || (remaining >= 2 && buffer[pos + 1] != 'u');
      } else {
        // Unquoted string does not have escape sequence so if `in` is ready then
        // there will be further input (or end of value)
        return quote == NO_QUOTE && in.ready();
      }
    }

    /**
     * Implementation note: Skips exactly {@code skipAmount} characters
     * unless end of value is reached before.
     */
    @Override
    public long skip(long skipAmount) throws IOException {
      if (skipAmount < 0) {
        throw new IllegalArgumentException("skip amount is negative");
      }
      verifyNotClosed();

      if (skipAmount == 0 || reachedEnd) {
        return 0;
      }

      long skipped;
      if (quote == NO_QUOTE) {
        skipped = skipUnquotedValue(skipAmount);
      } else {
        skipped = skipQuotedValue(quote, skipAmount);
      }

      if (skipped <= 0) {
        reachedEnd = true;
        return -skipped;
      } else {
        return skipped;
      }
    }

    @Override
    public void skipExactly(long skipAmount) throws IOException {
      long skipped = skip(skipAmount);
      if (skipped < skipAmount) {
        throw new EOFException("Skipped less than the requested " + skipAmount + " chars");
      }
    }

    @Override
    public void skipRemaining() throws IOException {
      verifyNotClosed();

      if (!reachedEnd) {
        if (quote == NO_QUOTE) {
          skipUnquotedValue();
        } else {
          skipQuotedValue(quote);
        }
        reachedEnd = true;
      }
    }

    @Override
    public void close() {
      if (!isClosed) {
        isClosed = true;

        /*
         * Update enclosing JsonReader, but only if end has been reached
         * Otherwise it would continue somewhere in the middle of the
         * string value
         *
         * Especially do not try to consume remaining chars until end is reached since that
         * might undesirable, e.g. when reader is used in try-with-resources and an exception
         * occurs within it. In that case code should fail fast and not try to read more data
         * (which could even block!).
         */
        if (reachedEnd) {
          peeked = PEEKED_NONE;

          if (isReadingName) {
            pathNames[stackSize - 1] = STREAMED_NAME;
          } else {
            pathIndices[stackSize - 1]++;
          }
        }
      }
    }
  }

  private class StringValueReaderStringImpl extends AbstractStringValueReaderStringImpl {
    public StringValueReaderStringImpl(String value, boolean isName) {
      super(value, isName);
    }

    @Override
    protected void onClosedAfterReachedEnd() {
      peeked = PEEKED_NONE;

      if (isName) {
        pathNames[stackSize - 1] = value;
      } else {
        pathIndices[stackSize - 1]++;
      }
    }
  }

  /** The input JSON. */
  private final Reader in;

  /** True to accept non-spec compliant JSON */
  private boolean lenient = false;

  /**
   * Use a manual buffer to easily read and unread upcoming characters, and
   * also so we can create strings without an intermediate StringBuilder.
   * We decode literals directly out of this buffer, so it must be at least as
   * long as the longest token that can be reported as a number.
   */
  private final char[] buffer = new char[1024];
  private int pos = 0;
  private int limit = 0;

  private int lineNumber = 0;
  private int lineStart = 0;

  int peeked = PEEKED_NONE;

  /**
   * A peeked value that was composed entirely of digits with an optional
   * leading dash. Positive values may not have a leading 0.
   */
  private long peekedLong;

  /**
   * The number of characters in a peeked number literal. Increment 'pos' by
   * this after reading a number.
   */
  private int peekedNumberLength;

  /**
   * A peeked string that should be parsed on the next double, long or string.
   * This is populated before a numeric value is parsed and used if that parsing
   * fails.
   */
  private String peekedString;

  /*
   * The nesting stack. Using a manual array rather than an ArrayList saves 20%.
   */
  private int[] stack = new int[32];
  private int stackSize = 0;
  {
    stack[stackSize++] = JsonScope.EMPTY_DOCUMENT;
  }

  /*
   * The path members. It corresponds directly to stack: At indices where the
   * stack contains an object (EMPTY_OBJECT, DANGLING_NAME or NONEMPTY_OBJECT),
   * pathNames contains the name at this scope. Where it contains an array
   * (EMPTY_ARRAY, NONEMPTY_ARRAY) pathIndices contains the current index in
   * that array. Otherwise the value is undefined, and we take advantage of that
   * by incrementing pathIndices when doing so isn't useful.
   */
  private String[] pathNames = new String[32];
  private int[] pathIndices = new int[32];

  /**
   * Creates a new instance that reads a JSON-encoded stream from {@code in}.
   */
  public JsonReader(Reader in) {
    if (in == null) {
      throw new NullPointerException("in == null");
    }
    this.in = in;
  }

  /**
   * Configure this parser to be liberal in what it accepts. By default,
   * this parser is strict and only accepts JSON as specified by <a
   * href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>. Setting the
   * parser to lenient causes it to ignore the following syntax errors:
   *
   * <ul>
   *   <li>Streams that start with the <a href="#nonexecuteprefix">non-execute
   *       prefix</a>, <code>")]}'\n"</code>.
   *   <li>Streams that include multiple top-level values. With strict parsing,
   *       each stream must contain exactly one top-level value.
   *   <li>Top-level values of any type. With strict parsing, the top-level
   *       value must be an object or an array.
   *   <li>Numbers may be {@link Double#isNaN() NaNs} or {@link
   *       Double#isInfinite() infinities}.
   *   <li>End of line comments starting with {@code //} or {@code #} and
   *       ending with a newline character.
   *   <li>C-style comments starting with {@code /*} and ending with
   *       {@code *}{@code /}. Such comments may not be nested.
   *   <li>Names that are unquoted or {@code 'single quoted'}.
   *   <li>Strings that are unquoted or {@code 'single quoted'}.
   *   <li>Array elements separated by {@code ;} instead of {@code ,}.
   *   <li>Unnecessary array separators. These are interpreted as if null
   *       was the omitted value.
   *   <li>Names and values separated by {@code =} or {@code =>} instead of
   *       {@code :}.
   *   <li>Name/value pairs separated by {@code ;} instead of {@code ,}.
   * </ul>
   */
  public final void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

  /**
   * Returns true if this parser is liberal in what it accepts.
   */
  public final boolean isLenient() {
    return lenient;
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is the
   * beginning of a new array.
   */
  public void beginArray() throws IOException {
    int p = peekInternal();
    if (p == PEEKED_BEGIN_ARRAY) {
      push(JsonScope.EMPTY_ARRAY);
      pathIndices[stackSize - 1] = 0;
      peeked = PEEKED_NONE;
    } else {
      throw throwUnexpectedTokenError(JsonToken.BEGIN_ARRAY, p);
    }
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is the
   * end of the current array.
   */
  public void endArray() throws IOException {
    int p = peekInternal();
    if (p == PEEKED_END_ARRAY) {
      stackSize--;
      pathIndices[stackSize - 1]++;
      peeked = PEEKED_NONE;
    } else {
      throw throwUnexpectedTokenError(JsonToken.END_ARRAY, p);
    }
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is the
   * beginning of a new object.
   */
  public void beginObject() throws IOException {
    int p = peekInternal();
    if (p == PEEKED_BEGIN_OBJECT) {
      push(JsonScope.EMPTY_OBJECT);
      peeked = PEEKED_NONE;
    } else {
      throw throwUnexpectedTokenError(JsonToken.BEGIN_OBJECT, p);
    }
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is the
   * end of the current object.
   */
  public void endObject() throws IOException {
    int p = peekInternal();
    if (p == PEEKED_END_OBJECT) {
      stackSize--;
      pathNames[stackSize] = null; // Free the last path name so that it can be garbage collected!
      pathIndices[stackSize - 1]++;
      peeked = PEEKED_NONE;
    } else {
      throw throwUnexpectedTokenError(JsonToken.END_OBJECT, p);
    }
  }

  /**
   * Returns true if the current array or object has another element.
   *
   * @throws IllegalStateException if a reader created using {@link #nextStringReader()}
   *     or {@link #nextNameReader()} has not consumed all data yet
   */
  public boolean hasNext() throws IOException {
    int p = peekInternal();

    if (p == PEEKED_STRING_READER) {
      throw throwActiveValueReaderError();
    }

    return p != PEEKED_END_OBJECT && p != PEEKED_END_ARRAY;
  }

  private static JsonToken peekedToToken(int p) {
    switch (p) {
    case PEEKED_BEGIN_OBJECT:
      return JsonToken.BEGIN_OBJECT;
    case PEEKED_END_OBJECT:
      return JsonToken.END_OBJECT;
    case PEEKED_BEGIN_ARRAY:
      return JsonToken.BEGIN_ARRAY;
    case PEEKED_END_ARRAY:
      return JsonToken.END_ARRAY;
    case PEEKED_SINGLE_QUOTED_NAME:
    case PEEKED_DOUBLE_QUOTED_NAME:
    case PEEKED_UNQUOTED_NAME:
      return JsonToken.NAME;
    case PEEKED_TRUE:
    case PEEKED_FALSE:
      return JsonToken.BOOLEAN;
    case PEEKED_NULL:
      return JsonToken.NULL;
    case PEEKED_SINGLE_QUOTED:
    case PEEKED_DOUBLE_QUOTED:
    case PEEKED_UNQUOTED:
    case PEEKED_BUFFERED:
      return JsonToken.STRING;
    case PEEKED_LONG:
    case PEEKED_NUMBER:
      return JsonToken.NUMBER;
    case PEEKED_EOF:
      return JsonToken.END_DOCUMENT;
    case PEEKED_STRING_READER:
      throw throwActiveValueReaderError();
    default:
      throw new AssertionError();
    }
  }

  /**
   * Returns the type of the next token without consuming it.
   *
   * @throws IllegalStateException if a reader created using {@link #nextStringReader()}
   *     or {@link #nextNameReader()} has not consumed all data yet
   */
  public JsonToken peek() throws IOException {
    int p = peekInternal();
    return peekedToToken(p);
  }

  private int peekInternal() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    return p;
  }

  int doPeek() throws IOException {
    int peekStack = stack[stackSize - 1];
    if (peekStack == JsonScope.EMPTY_ARRAY) {
      stack[stackSize - 1] = JsonScope.NONEMPTY_ARRAY;
    } else if (peekStack == JsonScope.NONEMPTY_ARRAY) {
      // Look for a comma before the next element.
      int c = nextNonWhitespace(true);
      switch (c) {
      case ']':
        return peeked = PEEKED_END_ARRAY;
      case ';':
        checkLenient(); // fall-through
      case ',':
        break;
      default:
        throw syntaxError("Unterminated array");
      }
    } else if (peekStack == JsonScope.EMPTY_OBJECT || peekStack == JsonScope.NONEMPTY_OBJECT) {
      stack[stackSize - 1] = JsonScope.DANGLING_NAME;
      // Look for a comma before the next element.
      if (peekStack == JsonScope.NONEMPTY_OBJECT) {
        int c = nextNonWhitespace(true);
        switch (c) {
        case '}':
          return peeked = PEEKED_END_OBJECT;
        case ';':
          checkLenient(); // fall-through
        case ',':
          break;
        default:
          throw syntaxError("Unterminated object");
        }
      }
      int c = nextNonWhitespace(true);
      switch (c) {
      case '"':
        return peeked = PEEKED_DOUBLE_QUOTED_NAME;
      case '\'':
        checkLenient();
        return peeked = PEEKED_SINGLE_QUOTED_NAME;
      case '}':
        if (peekStack != JsonScope.NONEMPTY_OBJECT) {
          return peeked = PEEKED_END_OBJECT;
        } else {
          throw syntaxError("Expected name");
        }
      default:
        checkLenient();
        pos--; // Don't consume the first character in an unquoted string.
        if (isLiteral((char) c)) {
          return peeked = PEEKED_UNQUOTED_NAME;
        } else {
          throw syntaxError("Expected name");
        }
      }
    } else if (peekStack == JsonScope.DANGLING_NAME) {
      stack[stackSize - 1] = JsonScope.NONEMPTY_OBJECT;
      // Look for a colon before the value.
      int c = nextNonWhitespace(true);
      switch (c) {
      case ':':
        break;
      case '=':
        checkLenient();
        if ((pos < limit || fillBuffer(1)) && buffer[pos] == '>') {
          pos++;
        }
        break;
      default:
        throw syntaxError("Expected ':'");
      }
    } else if (peekStack == JsonScope.EMPTY_DOCUMENT) {
      if (lenient) {
        consumeNonExecutePrefix();
      }
      stack[stackSize - 1] = JsonScope.NONEMPTY_DOCUMENT;
    } else if (peekStack == JsonScope.NONEMPTY_DOCUMENT) {
      int c = nextNonWhitespace(false);
      if (c == -1) {
        return peeked = PEEKED_EOF;
      } else {
        checkLenient();
        pos--;
      }
    } else if (peekStack == JsonScope.CLOSED) {
      throw new IllegalStateException("JsonReader is closed");
    }

    int c = nextNonWhitespace(true);
    switch (c) {
    case ']':
      if (peekStack == JsonScope.EMPTY_ARRAY) {
        return peeked = PEEKED_END_ARRAY;
      }
      // fall-through to handle ",]"
    case ';':
    case ',':
      // In lenient mode, a 0-length literal in an array means 'null'.
      if (peekStack == JsonScope.EMPTY_ARRAY || peekStack == JsonScope.NONEMPTY_ARRAY) {
        checkLenient();
        pos--;
        return peeked = PEEKED_NULL;
      } else {
        throw syntaxError("Unexpected value");
      }
    case '\'':
      checkLenient();
      return peeked = PEEKED_SINGLE_QUOTED;
    case '"':
      return peeked = PEEKED_DOUBLE_QUOTED;
    case '[':
      return peeked = PEEKED_BEGIN_ARRAY;
    case '{':
      return peeked = PEEKED_BEGIN_OBJECT;
    default:
      pos--; // Don't consume the first character in a literal value.
    }

    int result = peekKeyword();
    if (result != PEEKED_NONE) {
      return result;
    }

    result = peekNumber();
    if (result != PEEKED_NONE) {
      return result;
    }

    if (!isLiteral(buffer[pos])) {
      throw syntaxError("Expected value");
    }

    checkLenient();
    return peeked = PEEKED_UNQUOTED;
  }

  private int peekKeyword() throws IOException {
    // Figure out which keyword we're matching against by its first character.
    char c = buffer[pos];
    String keyword;
    String keywordUpper;
    int peeking;
    if (c == 't' || c == 'T') {
      keyword = "true";
      keywordUpper = "TRUE";
      peeking = PEEKED_TRUE;
    } else if (c == 'f' || c == 'F') {
      keyword = "false";
      keywordUpper = "FALSE";
      peeking = PEEKED_FALSE;
    } else if (c == 'n' || c == 'N') {
      keyword = "null";
      keywordUpper = "NULL";
      peeking = PEEKED_NULL;
    } else {
      return PEEKED_NONE;
    }

    // Confirm that chars [1..length) match the keyword.
    int length = keyword.length();
    for (int i = 1; i < length; i++) {
      if (pos + i >= limit && !fillBuffer(i + 1)) {
        return PEEKED_NONE;
      }
      c = buffer[pos + i];
      if (c != keyword.charAt(i) && c != keywordUpper.charAt(i)) {
        return PEEKED_NONE;
      }
    }

    if ((pos + length < limit || fillBuffer(length + 1))
        && isLiteral(buffer[pos + length])) {
      return PEEKED_NONE; // Don't match trues, falsey or nullsoft!
    }

    // We've found the keyword followed either by EOF or by a non-literal character.
    pos += length;
    return peeked = peeking;
  }

  private int peekNumber() throws IOException {
    // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
    char[] buffer = this.buffer;
    int p = pos;
    int l = limit;

    long value = 0; // Negative to accommodate Long.MIN_VALUE more easily.
    boolean negative = false;
    boolean fitsInLong = true;
    int last = NUMBER_CHAR_NONE;

    int i = 0;

    charactersOfNumber:
    for (; true; i++) {
      if (p + i == l) {
        if (i == buffer.length) {
          // Though this looks like a well-formed number, it's too long to continue reading. Give up
          // and let the application handle this as an unquoted literal.
          return PEEKED_NONE;
        }
        if (!fillBuffer(i + 1)) {
          break;
        }
        p = pos;
        l = limit;
      }

      char c = buffer[p + i];
      switch (c) {
      case '-':
        if (last == NUMBER_CHAR_NONE) {
          negative = true;
          last = NUMBER_CHAR_SIGN;
          continue;
        } else if (last == NUMBER_CHAR_EXP_E) {
          last = NUMBER_CHAR_EXP_SIGN;
          continue;
        }
        return PEEKED_NONE;

      case '+':
        if (last == NUMBER_CHAR_EXP_E) {
          last = NUMBER_CHAR_EXP_SIGN;
          continue;
        }
        return PEEKED_NONE;

      case 'e':
      case 'E':
        if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT) {
          last = NUMBER_CHAR_EXP_E;
          continue;
        }
        return PEEKED_NONE;

      case '.':
        if (last == NUMBER_CHAR_DIGIT) {
          last = NUMBER_CHAR_DECIMAL;
          continue;
        }
        return PEEKED_NONE;

      default:
        if (c < '0' || c > '9') {
          if (!isLiteral(c)) {
            break charactersOfNumber;
          }
          return PEEKED_NONE;
        }
        if (last == NUMBER_CHAR_SIGN || last == NUMBER_CHAR_NONE) {
          value = -(c - '0');
          last = NUMBER_CHAR_DIGIT;
        } else if (last == NUMBER_CHAR_DIGIT) {
          if (value == 0) {
            return PEEKED_NONE; // Leading '0' prefix is not allowed (since it could be octal).
          }
          long newValue = value * 10 - (c - '0');
          fitsInLong &= value > MIN_INCOMPLETE_INTEGER
              || (value == MIN_INCOMPLETE_INTEGER && newValue < value);
          value = newValue;
        } else if (last == NUMBER_CHAR_DECIMAL) {
          last = NUMBER_CHAR_FRACTION_DIGIT;
        } else if (last == NUMBER_CHAR_EXP_E || last == NUMBER_CHAR_EXP_SIGN) {
          last = NUMBER_CHAR_EXP_DIGIT;
        }
      }
    }

    // We've read a complete number. Decide if it's a PEEKED_LONG or a PEEKED_NUMBER.
    if (last == NUMBER_CHAR_DIGIT && fitsInLong && (value != Long.MIN_VALUE || negative) && (value!=0 || false==negative)) {
      peekedLong = negative ? value : -value;
      pos += i;
      return peeked = PEEKED_LONG;
    } else if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT
        || last == NUMBER_CHAR_EXP_DIGIT) {
      peekedNumberLength = i;
      return peeked = PEEKED_NUMBER;
    } else {
      return PEEKED_NONE;
    }
  }

  private boolean isLiteral(char c) throws IOException {
    switch (c) {
    case '/':
    case '\\':
    case ';':
    case '#':
    case '=':
      checkLenient(); // fall-through
    case '{':
    case '}':
    case '[':
    case ']':
    case ':':
    case ',':
    case ' ':
    case '\t':
    case '\f':
    case '\r':
    case '\n':
      return false;
    default:
      return true;
    }
  }

  /**
   * Returns the next token, a {@link com.google.gson.stream.JsonToken#NAME property name}, and
   * consumes it.
   *
   * <p>When reading large property names {@link #nextNameReader()} should be preferred.
   *
   * @throws IllegalStateException if the next token in the stream is not a property
   *     name.
   */
  public String nextName() throws IOException {
    int p = peekInternal();
    String result;
    if (p == PEEKED_UNQUOTED_NAME) {
      result = nextUnquotedValue();
    } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
      result = nextQuotedValue('\'');
    } else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
      result = nextQuotedValue('"');
    } else {
      throw throwUnexpectedTokenError(JsonToken.NAME, p);
    }
    peeked = PEEKED_NONE;
    pathNames[stackSize - 1] = result;
    return result;
  }

  /**
   * Returns a reader for the next token, a {@link JsonToken#NAME property name}.
   *
   * <p>The complete data of the returned reader has to be consumed (for example using
   * {@link StringValueReader#skipRemaining()}) and the reader has to be closed before
   * a subsequent token can be processed. Failing to do so can render this
   * {@code JsonReader} unusable.<br>
   * Closing this {@code JsonReader} and attempting to read from a name reader afterwards
   * causes unspecified behavior.
   *
   * <p>For efficiency reasons implementations might record the string {@code "#streamedName"}
   * as name returned by {@link #getPath()} instead of the actually read name.
   *
   * @return reader for the next property name token
   * @throws IllegalStateException if the next token is not a property name
   * @throws IOException if creating the reader fails
   */
  public StringValueReader nextNameReader() throws IOException {
    int p = peekInternal();
    StringValueReader r;

    if (p == PEEKED_UNQUOTED_NAME) {
      r = new StringValueReaderImpl(true);
    } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
      r = new StringValueReaderImpl(true, '\'');
    } else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
      r = new StringValueReaderImpl(true, '"');
    } else {
      throw throwUnexpectedTokenError(JsonToken.NAME, p);
    }
    peeked = PEEKED_STRING_READER;
    return r;
  }

  /**
   * Returns the {@link com.google.gson.stream.JsonToken#STRING string} value of the next token,
   * consuming it. If the next token is a number, this method will return its
   * string form.
   *
   * <p>When reading large strings {@link #nextStringReader()} should be preferred.
   *
   * @throws IllegalStateException if the next token is not a string or if
   *     this reader is closed.
   */
  public String nextString() throws IOException {
    int p = peekInternal();
    String result;
    if (p == PEEKED_UNQUOTED) {
      result = nextUnquotedValue();
    } else if (p == PEEKED_SINGLE_QUOTED) {
      result = nextQuotedValue('\'');
    } else if (p == PEEKED_DOUBLE_QUOTED) {
      result = nextQuotedValue('"');
    } else if (p == PEEKED_BUFFERED) {
      result = peekedString;
      peekedString = null;
    } else if (p == PEEKED_LONG) {
      result = Long.toString(peekedLong);
    } else if (p == PEEKED_NUMBER) {
      result = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else {
      throw throwUnexpectedTokenError(JsonToken.STRING, p);
    }
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * Returns a reader for the {@link JsonToken#STRING string} value of the next token. If the
   * next token is a number, this method will return a reader reading its string form.
   *
   * <p>The complete data of the returned reader has to be consumed (for example using
   * {@link StringValueReader#skipRemaining()}) and the reader has to be closed before
   * a subsequent token can be processed. Failing to do so can render this
   * {@code JsonReader} unusable.<br>
   * Closing this {@code JsonReader} and attempting to read from a string reader afterwards
   * causes unspecified behavior.
   *
   * @return reader for the next token
   * @throws IllegalStateException if the next token cannot be read as string
   * @throws IOException if creating the reader fails
   */
  public StringValueReader nextStringReader() throws IOException {
    int p = peekInternal();
    StringValueReader reader;

    if (p == PEEKED_UNQUOTED) {
      reader = new StringValueReaderImpl(false);
    } else if (p == PEEKED_SINGLE_QUOTED) {
      reader = new StringValueReaderImpl(false, '\'');
    } else if (p == PEEKED_DOUBLE_QUOTED) {
      reader = new StringValueReaderImpl(false, '"');
    } else if (p == PEEKED_BUFFERED) {
      reader = new StringValueReaderStringImpl(peekedString, false);
      peekedString = null;
    }
    // For consistency with nextString() read parsed numbers, however there is no performance benefit
    else if (p == PEEKED_LONG) {
      reader = new StringValueReaderStringImpl(Long.toString(peekedLong), false);
    } else if (p == PEEKED_NUMBER) {
      // Note: If necessary could be made more efficient in the future by creating reader
      // which directly reads from buffer instead of creating intermediate String
      reader = new StringValueReaderStringImpl(new String(buffer, pos, peekedNumberLength), false);
      pos += peekedNumberLength;
    } else {
      throw throwUnexpectedTokenError(JsonToken.STRING, p);
    }

    peeked = PEEKED_STRING_READER;
    return reader;
  }

  /**
   * Returns the {@link com.google.gson.stream.JsonToken#BOOLEAN boolean} value of the next token,
   * consuming it.
   *
   * @throws IllegalStateException if the next token is not a boolean or if
   *     this reader is closed.
   */
  public boolean nextBoolean() throws IOException {
    int p = peekInternal();
    if (p == PEEKED_TRUE) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return true;
    } else if (p == PEEKED_FALSE) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return false;
    }
    throw throwUnexpectedTokenError(JsonToken.BOOLEAN, p);
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is a
   * literal null.
   *
   * @throws IllegalStateException if the next token is not null or if this
   *     reader is closed.
   */
  public void nextNull() throws IOException {
    int p = peekInternal();
    if (p == PEEKED_NULL) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
    } else {
      throw throwUnexpectedTokenError(JsonToken.NULL, p);
    }
  }

  /**
   * Returns the {@link com.google.gson.stream.JsonToken#NUMBER double} value of the next token,
   * consuming it. If the next token is a string, this method will attempt to
   * parse it as a double using {@link Double#parseDouble(String)}.
   *
   * @throws IllegalStateException if the next token is not a literal value.
   * @throws NumberFormatException if the next literal value cannot be parsed
   *     as a double, or is non-finite.
   */
  public double nextDouble() throws IOException {
    int p = peekInternal();

    if (p == PEEKED_LONG) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return (double) peekedLong;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
      peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
    } else if (p == PEEKED_UNQUOTED) {
      peekedString = nextUnquotedValue();
    } else if (p != PEEKED_BUFFERED) {
      throw throwUnexpectedTokenError("double", p);
    }

    peeked = PEEKED_BUFFERED;
    double result = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    if (!lenient && (Double.isNaN(result) || Double.isInfinite(result))) {
      throw new MalformedJsonException(
          "JSON forbids NaN and infinities: " + result + locationString());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * Returns the {@link com.google.gson.stream.JsonToken#NUMBER long} value of the next token,
   * consuming it. If the next token is a string, this method will attempt to
   * parse it as a long. If the next token's numeric value cannot be exactly
   * represented by a Java {@code long}, this method throws.
   *
   * @throws IllegalStateException if the next token is not a literal value.
   * @throws NumberFormatException if the next literal value cannot be parsed
   *     as a number, or exactly represented as a long.
   */
  public long nextLong() throws IOException {
    int p = peekInternal();

    if (p == PEEKED_LONG) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return peekedLong;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED || p == PEEKED_UNQUOTED) {
      if (p == PEEKED_UNQUOTED) {
        peekedString = nextUnquotedValue();
      } else {
        peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
      }
      try {
        long result = Long.parseLong(peekedString);
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
      } catch (NumberFormatException ignored) {
        // Fall back to parse as a double below.
      }
    } else {
      throw throwUnexpectedTokenError("long", p);
    }

    peeked = PEEKED_BUFFERED;
    double asDouble = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    long result = (long) asDouble;
    if (result != asDouble) { // Make sure no precision was lost casting to 'long'.
      throw new NumberFormatException("Expected a long but was " + peekedString + locationString());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * See {@link #nextQuotedValue(char)}
   *
   * @param charsConsumer consuming the chars of the value
   * @param minDesiredAccept desired minimum number of chars which should be passed to the
   *   charsConsumer; might read less chars if end of value is reached before;
   *   <b>must be &gt;= 1</b>
   * @param maxAccept maximum number of chars which should be passed to the charsConsumer;
   *     <b>must be &gt;= 1</b>
   * @return whether the end of the value has been reached
   */
  private boolean nextQuotedValue(char quote, CharsConsumer charsConsumer, int minDesiredAccept, int maxAccept) throws IOException {
    char[] buffer = this.buffer;
    int acceptedCnt = 0;

    while (true) {
      int p = pos;
      int l = limit;
      /* the index of the first character not yet consumed by the charsConsumer */
      int start = p;
      while (p < l) {
        int c = buffer[p++];

        if (c == quote) {
          pos = p;
          int len = p - start - 1;
          charsConsumer.acceptAndFinish(buffer, start, len);
          return true;
        }

        if (c == '\n') {
          lineNumber++;
          lineStart = p;
        } else {
          if (c == '\\') {
            pos = p;
            int len = p - start - 1;
            charsConsumer.accept(buffer, start, len);
            // Reading escape sequence might block so only try to read it
            // if not consumed enough chars yet or if escape sequence can
            // be read without blocking
            if (acceptedCnt >= minDesiredAccept && !fillBufferForEscapeCharacter()) {
              pos--; // Don't consume backslash
              return false;
            }
            charsConsumer.accept(readEscapeCharacter());
            p = pos;
            l = limit;
            start = p;
          }
        }

        if (++acceptedCnt >= maxAccept) {
          break;
        }
      }

      charsConsumer.accept(buffer, start, p - start);
      pos = p;

      if (acceptedCnt >= maxAccept || (acceptedCnt >= minDesiredAccept && !in.ready())) {
        return false;
      } else if (!fillBuffer(1)) {
        throw syntaxError("Unterminated string");
      }
    }
  }

  /**
   * Returns the string up to but not including {@code quote}, unescaping any
   * character escape sequences encountered along the way. The opening quote
   * should have already been read. This consumes the closing quote, but does
   * not include it in the returned string.
   *
   * @param quote either ' or ".
   * @throws NumberFormatException if any unicode escape sequences are
   *     malformed.
   */
  private String nextQuotedValue(char quote) throws IOException {
    StringBuildingCharsConsumer charsConsumer = new StringBuildingCharsConsumer();
    boolean reachedEnd = nextQuotedValue(quote, charsConsumer, Integer.MAX_VALUE, Integer.MAX_VALUE);
    if (!reachedEnd) {
      // Currently unreachable because StringBuilder would have thrown exception, however
      // that is an implementation detail
      throw syntaxError("String values > " + Integer.MAX_VALUE + " are not supported");
    }
    return charsConsumer.build();
  }

  /**
   * See {@link #nextUnquotedValue()}
   *
   * @param charsConsumer consuming the chars of the value
   * @param minDesiredAccept desired minimum number of chars which should be passed to the
   *   charsConsumer; might read less chars if end of value is reached before;
   *   <b>must be &gt;= 1</b>
   * @param maxAccept maximum number of chars which should be passed to the charsConsumer;
   *     <b>must be &gt;= 1</b>
   * @return whether the end of the value has been reached
   */
  @SuppressWarnings("fallthrough")
  private boolean nextUnquotedValue(CharsConsumer charsConsumer, int minDesiredAccept, int maxAccept) throws IOException {
    int acceptedCnt = 0;
    int i = 0;
    boolean foundEnd = false;

    findNonLiteralCharacter:
    while (true) {
      for (; pos + i < limit; i++) {
        switch (buffer[pos + i]) {
        case '/':
        case '\\':
        case ';':
        case '#':
        case '=':
          checkLenient(); // fall-through
        case '{':
        case '}':
        case '[':
        case ']':
        case ':':
        case ',':
        case ' ':
        case '\t':
        case '\f':
        case '\r':
        case '\n':
          foundEnd = true;
          break findNonLiteralCharacter;
        }

        if ((++acceptedCnt) >= maxAccept) {
          i++; // Current char should be consumed
          break findNonLiteralCharacter;
        }
      }

      charsConsumer.accept(buffer, pos, i);
      pos += i;
      i = 0;
      if (acceptedCnt >= minDesiredAccept && !in.ready()) {
        foundEnd = false;
        break;
      } else if (!fillBuffer(1)) {
        foundEnd = true;
        break;
      }
    }

    charsConsumer.acceptAndFinish(buffer, pos, i);
    pos += i;
    return foundEnd;
  }

  /**
   * Returns an unquoted value as a string.
   */
  private String nextUnquotedValue() throws IOException {
    StringBuildingCharsConsumer charsConsumer = new StringBuildingCharsConsumer();
    boolean reachedEnd = nextUnquotedValue(charsConsumer, Integer.MAX_VALUE, Integer.MAX_VALUE);
    if (!reachedEnd) {
      // Currently unreachable because StringBuilder would have thrown exception, however
      // that is an implementation detail
      throw syntaxError("String values > " + Integer.MAX_VALUE + " are not supported");
    }
    return charsConsumer.build();
  }

  /**
   * See {@link #skipQuotedValue(char)}
   *
   * @param desiredSkipAmount desired number of chars to be skipped;
   *   might skip less chars if end of value is reached before;
   *   <b>must be &gt;= 1</b>
   * @return actual number of skipped chars; negative or 0 if end of value has been reached.
   *     E.g. {@code -4} means 4 chars were skipped and end has been reached.
   */
  private long skipQuotedValue(char quote, long desiredSkipAmount) throws IOException {
    long skipped = 0;
    // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
    char[] buffer = this.buffer;
    do {
      int p = pos;
      int l = limit;
      while (p < l) {
        int c = buffer[p++];
        if (c == quote) {
          pos = p;
          return -skipped;  // Negate skipped to indicate that end has been reached
        }

        if (c == '\n') {
          lineNumber++;
          lineStart = p;
        } else {
          if (c == '\\') {
            pos = p;
            readEscapeCharacter();
            p = pos;
            l = limit;
          }

          if (++skipped >= desiredSkipAmount) {
            pos = p;
            return skipped;
          }
        }
      }
      pos = p;
    } while (fillBuffer(1));
    throw syntaxError("Unterminated string");
  }

  private void skipQuotedValue(char quote) throws IOException {
    // Call in loop in case value is > Long.MAX_VALUE (rather unlikely)
    while (skipQuotedValue(quote, Long.MAX_VALUE) > 0) { }
  }

  /**
   * See {@link #skipUnquotedValue()}
   *
   * @param desiredSkipAmount desired number of chars to be skipped;
   *   might skip less chars if end of value is reached before;
   *   <b>must be &gt;= 1</b>
   * @return actual number of skipped chars; negative or 0 if end of value has been reached.
   *     E.g. {@code -4} means 4 chars were skipped and end has been reached.
   */
  @SuppressWarnings("fallthrough")
  private long skipUnquotedValue(long desiredSkipAmount) throws IOException {
    long skipped = 0;

    findNonLiteralCharacter:
    do {
      int i = 0;
      for (; pos + i < limit; i++) {
        switch (buffer[pos + i]) {
        case '/':
        case '\\':
        case ';':
        case '#':
        case '=':
          checkLenient(); // fall-through
        case '{':
        case '}':
        case '[':
        case ']':
        case ':':
        case ',':
        case ' ':
        case '\t':
        case '\f':
        case '\r':
        case '\n':
          pos += i;
          break findNonLiteralCharacter;
        }

        if ((++skipped) >= desiredSkipAmount) {
          pos += i + 1; // + 1 to skip current char as well
          return skipped;
        }
      }
      pos += i;
    } while (fillBuffer(1));

    return -skipped; // Negate skipped to indicate that end has been reached
  }

  private void skipUnquotedValue() throws IOException {
    // Call in loop in case value is > Long.MAX_VALUE (rather unlikely)
    while (skipUnquotedValue(Long.MAX_VALUE) > 0) { }
  }

  /**
   * Returns the {@link com.google.gson.stream.JsonToken#NUMBER int} value of the next token,
   * consuming it. If the next token is a string, this method will attempt to
   * parse it as an int. If the next token's numeric value cannot be exactly
   * represented by a Java {@code int}, this method throws.
   *
   * @throws IllegalStateException if the next token is not a literal value.
   * @throws NumberFormatException if the next literal value cannot be parsed
   *     as a number, or exactly represented as an int.
   */
  public int nextInt() throws IOException {
    int p = peekInternal();

    int result;
    if (p == PEEKED_LONG) {
      result = (int) peekedLong;
      if (peekedLong != result) { // Make sure no precision was lost casting to 'int'.
        throw new NumberFormatException("Expected an int but was " + peekedLong + locationString());
      }
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return result;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED || p == PEEKED_UNQUOTED) {
      if (p == PEEKED_UNQUOTED) {
        peekedString = nextUnquotedValue();
      } else {
        peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
      }
      try {
        result = Integer.parseInt(peekedString);
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
      } catch (NumberFormatException ignored) {
        // Fall back to parse as a double below.
      }
    } else {
      throw throwUnexpectedTokenError("int", p);
    }

    peeked = PEEKED_BUFFERED;
    double asDouble = Double.parseDouble(peekedString); // don't catch this NumberFormatException.
    result = (int) asDouble;
    if (result != asDouble) { // Make sure no precision was lost casting to 'int'.
      throw new NumberFormatException("Expected an int but was " + peekedString + locationString());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * Closes this JSON reader and the underlying {@link java.io.Reader}.
   */
  public void close() throws IOException {
    peeked = PEEKED_NONE;
    stack[0] = JsonScope.CLOSED;
    stackSize = 1;
    in.close();
  }

  /**
   * Skips the next value recursively. If it is an object or array, all nested
   * elements are skipped. This method is intended for use when the JSON token
   * stream contains unrecognized or unhandled values.
   *
   * <p>If used to skip a {@link com.google.gson.stream.JsonToken#NAME property name}
   * the string {@code "#skippedName"} is recorded as name returned by
   * {@link #getPath()} instead of the actually skipped name.
   *
   * @throws IllegalStateException if a reader created using {@link #nextStringReader()}
   *     or {@link #nextNameReader()} has not consumed all data yet
   */
  public void skipValue() throws IOException {
    if (peekInternal() == PEEKED_STRING_READER) {
      throw throwActiveValueReaderError();
    }

    int count = 0;
    int p;
    do {
      p = peekInternal();

      if (p == PEEKED_BEGIN_ARRAY) {
        push(JsonScope.EMPTY_ARRAY);
        count++;
      } else if (p == PEEKED_BEGIN_OBJECT) {
        push(JsonScope.EMPTY_OBJECT);
        count++;
      } else if (p == PEEKED_END_ARRAY) {
        stackSize--;
        count--;
      } else if (p == PEEKED_END_OBJECT) {
        stackSize--;
        count--;
      } else if (p == PEEKED_UNQUOTED || p == PEEKED_UNQUOTED_NAME) {
        skipUnquotedValue();
      } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_SINGLE_QUOTED_NAME) {
        skipQuotedValue('\'');
      } else if (p == PEEKED_DOUBLE_QUOTED || p == PEEKED_DOUBLE_QUOTED_NAME) {
        skipQuotedValue('"');
      } else if (p == PEEKED_NUMBER) {
        pos += peekedNumberLength;
      }

      peeked = PEEKED_NONE;
    } while (count != 0);

    // Only update pathNames when last peeked was name
    // That means don't replace name when JSON property value is skipped
    if (p == PEEKED_UNQUOTED_NAME || p == PEEKED_SINGLE_QUOTED_NAME || p == PEEKED_DOUBLE_QUOTED_NAME) {
      pathNames[stackSize - 1] = SKIPPED_NAME;
    } else {
      pathIndices[stackSize - 1]++;
    }
  }

  private void push(int newTop) {
    if (stackSize == stack.length) {
      int newLength = stackSize * 2;
      stack = Arrays.copyOf(stack, newLength);
      pathIndices = Arrays.copyOf(pathIndices, newLength);
      pathNames = Arrays.copyOf(pathNames, newLength);
    }
    stack[stackSize++] = newTop;
  }

  /**
   * Returns true once {@code limit - pos >= minimum}. If the data is
   * exhausted before that many characters are available, this returns
   * false.
   */
  private boolean fillBuffer(int minimum) throws IOException {
    char[] buffer = this.buffer;
    lineStart -= pos;
    if (limit != pos) {
      limit -= pos;
      System.arraycopy(buffer, pos, buffer, 0, limit);
    } else {
      limit = 0;
    }

    pos = 0;
    int total;
    while ((total = in.read(buffer, limit, buffer.length - limit)) != -1) {
      limit += total;

      // if this is the first read, consume an optional byte order mark (BOM) if it exists
      if (lineNumber == 0 && lineStart == 0 && limit > 0 && buffer[0] == '\ufeff') {
        pos++;
        lineStart++;
        minimum++;
      }

      if (limit >= minimum) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the next character in the stream that is neither whitespace nor a
   * part of a comment. When this returns, the returned character is always at
   * {@code buffer[pos-1]}; this means the caller can always push back the
   * returned character by decrementing {@code pos}.
   */
  private int nextNonWhitespace(boolean throwOnEof) throws IOException {
    /*
     * This code uses ugly local variables 'p' and 'l' representing the 'pos'
     * and 'limit' fields respectively. Using locals rather than fields saves
     * a few field reads for each whitespace character in a pretty-printed
     * document, resulting in a 5% speedup. We need to flush 'p' to its field
     * before any (potentially indirect) call to fillBuffer() and reread both
     * 'p' and 'l' after any (potentially indirect) call to the same method.
     */
    char[] buffer = this.buffer;
    int p = pos;
    int l = limit;
    while (true) {
      if (p == l) {
        pos = p;
        if (!fillBuffer(1)) {
          break;
        }
        p = pos;
        l = limit;
      }

      int c = buffer[p++];
      if (c == '\n') {
        lineNumber++;
        lineStart = p;
        continue;
      } else if (c == ' ' || c == '\r' || c == '\t') {
        continue;
      }

      if (c == '/') {
        pos = p;
        if (p == l) {
          pos--; // push back '/' so it's still in the buffer when this method returns
          boolean charsLoaded = fillBuffer(2);
          pos++; // consume the '/' again
          if (!charsLoaded) {
            return c;
          }
        }

        checkLenient();
        char peek = buffer[pos];
        switch (peek) {
        case '*':
          // skip a /* c-style comment */
          pos++;
          if (!skipTo("*/")) {
            throw syntaxError("Unterminated comment");
          }
          p = pos + 2;
          l = limit;
          continue;

        case '/':
          // skip a // end-of-line comment
          pos++;
          skipToEndOfLine();
          p = pos;
          l = limit;
          continue;

        default:
          return c;
        }
      } else if (c == '#') {
        pos = p;
        /*
         * Skip a # hash end-of-line comment. The JSON RFC doesn't
         * specify this behaviour, but it's required to parse
         * existing documents. See http://b/2571423.
         */
        checkLenient();
        skipToEndOfLine();
        p = pos;
        l = limit;
      } else {
        pos = p;
        return c;
      }
    }
    if (throwOnEof) {
      throw new EOFException("End of input" + locationString());
    } else {
      return -1;
    }
  }

  private void checkLenient() throws IOException {
    if (!lenient) {
      throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
    }
  }

  /**
   * Advances the position until after the next newline character. If the line
   * is terminated by "\r\n", the '\n' must be consumed as whitespace by the
   * caller.
   */
  private void skipToEndOfLine() throws IOException {
    while (pos < limit || fillBuffer(1)) {
      char c = buffer[pos++];
      if (c == '\n') {
        lineNumber++;
        lineStart = pos;
        break;
      } else if (c == '\r') {
        break;
      }
    }
  }

  /**
   * @param toFind a string to search for. Must not contain a newline.
   */
  private boolean skipTo(String toFind) throws IOException {
    int length = toFind.length();
    outer:
    for (; pos + length <= limit || fillBuffer(length); pos++) {
      if (buffer[pos] == '\n') {
        lineNumber++;
        lineStart = pos + 1;
        continue;
      }
      for (int c = 0; c < length; c++) {
        if (buffer[pos + c] != toFind.charAt(c)) {
          continue outer;
        }
      }
      return true;
    }
    return false;
  }

  @Override public String toString() {
    return getClass().getSimpleName() + locationString();
  }

  String locationString() {
    int line = lineNumber + 1;
    int column = pos - lineStart + 1;
    return " at line " + line + " column " + column + " path " + getPath();
  }

  /**
   * Returns a <a href="http://goessner.net/articles/JsonPath/">JsonPath</a> to
   * the current location in the JSON value.
   */
  public String getPath() {
    StringBuilder result = new StringBuilder().append('$');
    for (int i = 0, size = stackSize; i < size; i++) {
      switch (stack[i]) {
        case JsonScope.EMPTY_ARRAY:
        case JsonScope.NONEMPTY_ARRAY:
          result.append('[').append(pathIndices[i]).append(']');
          break;

        case JsonScope.EMPTY_OBJECT:
        case JsonScope.DANGLING_NAME:
        case JsonScope.NONEMPTY_OBJECT:
          result.append('.');
          if (pathNames[i] != null) {
            result.append(pathNames[i]);
          }
          break;

        case JsonScope.NONEMPTY_DOCUMENT:
        case JsonScope.EMPTY_DOCUMENT:
        case JsonScope.CLOSED:
          break;
      }
    }
    return result.toString();
  }

  /**
   * Tries to fill the buffer for a '\' escape sequence without blocking.
   * The backslash '\' of the escape sequence should be at {@link #pos} -1.
   *
   * @return {@code true} if the buffer could be filled and now contains the
   *   complete escape sequence; {@code false} if the buffer could not be
   *   filled because the method might have to block for input
   * @see {@link #readEscapeCharacter()}
   */
  private boolean fillBufferForEscapeCharacter() throws IOException {
    if (pos == limit) {
      if (!in.ready()) {
        return false;
      } else if (!fillBuffer(1)) {
        throw syntaxError("Unterminated escape sequence");
      }
    }

    // For unicode escapes have to read all 4 hex digits
    if (buffer[pos] == 'u') {
      while (limit - pos < 5) { // 5 for uXXXX
        if (!in.ready()) {
          return false;
        } else if (!fillBuffer(1)) {
          throw syntaxError("Unterminated escape sequence");
        }
      }
    }

    return true;
  }

  /**
   * Unescapes the character identified by the character or characters that
   * immediately follow a backslash. The backslash '\' should have already
   * been read. This supports both unicode escapes "u000A" and two-character
   * escapes "\n".
   *
   * @throws NumberFormatException if any unicode escape sequences are
   *     malformed.
   */
  private char readEscapeCharacter() throws IOException {
    if (pos == limit && !fillBuffer(1)) {
      throw syntaxError("Unterminated escape sequence");
    }

    char escaped = buffer[pos++];
    switch (escaped) {
    case 'u':
      if (pos + 4 > limit && !fillBuffer(4)) {
        throw syntaxError("Unterminated escape sequence");
      }
      // Equivalent to Integer.parseInt(stringPool.get(buffer, pos, 4), 16);
      char result = 0;
      for (int i = pos, end = i + 4; i < end; i++) {
        char c = buffer[i];
        result <<= 4;
        if (c >= '0' && c <= '9') {
          result += (c - '0');
        } else if (c >= 'a' && c <= 'f') {
          result += (c - 'a' + 10);
        } else if (c >= 'A' && c <= 'F') {
          result += (c - 'A' + 10);
        } else {
          throw new NumberFormatException("\\u" + new String(buffer, pos, 4));
        }
      }
      pos += 4;
      return result;

    case 't':
      return '\t';

    case 'b':
      return '\b';

    case 'n':
      return '\n';

    case 'r':
      return '\r';

    case 'f':
      return '\f';

    case '\n':
      lineNumber++;
      lineStart = pos;
      // fall-through

    case '\'':
    case '"':
    case '\\':
    case '/':
      return escaped;
    default:
      // throw error when none of the above cases are matched
      throw syntaxError("Invalid escape sequence");
    }
  }

  /**
   * Throws a new IO exception with the given message and a context snippet
   * with this reader's content.
   */
  private IOException syntaxError(String message) throws IOException {
    throw new MalformedJsonException(message + locationString());
  }

  /**
   * Throws a new {@code IllegalStateException} indicating that a {@code Reader}
   * created by {@link #nextStringReader()} or {@link #nextNameReader()} is currently
   * consuming the input.
   *
   * @throws IllegalStateException <i>always</i>
   */
  private static IllegalStateException throwActiveValueReaderError() throws IllegalStateException {
    throw new IllegalStateException("Name or string reader has not been closed or did not consume "
        + "all data before it was closed");
  }

  private IllegalStateException throwUnexpectedTokenError(JsonToken expected, int peeked) throws IllegalStateException {
    throw throwUnexpectedTokenError(expected.name(), peeked);
  }

  /**
   * Throws a new {@code IllegalStateException} indicating that the peeked
   * type is unexpected.
   *
   * @param expected expected type name
   * @param peeked peeked type
   * @throws IllegalStateException <i>always</i>
   */
  private IllegalStateException throwUnexpectedTokenError(String expected, int peeked) throws IllegalStateException {
    if (peeked == PEEKED_STRING_READER) {
      throw throwActiveValueReaderError();
    } else {
      JsonToken actual = peekedToToken(peeked);
      throw new IllegalStateException("Expected " + expected + " but was " + actual + locationString());
    }
  }

  /**
   * Consumes the non-execute prefix if it exists.
   */
  private void consumeNonExecutePrefix() throws IOException {
    // fast forward through the leading whitespace
    nextNonWhitespace(true);
    pos--;

    int p = pos;
    if (p + 5 > limit && !fillBuffer(5)) {
      return;
    }

    char[] buf = buffer;
    if(buf[p] != ')' || buf[p + 1] != ']' || buf[p + 2] != '}' || buf[p + 3] != '\'' || buf[p + 4] != '\n') {
      return; // not a security token!
    }

    // we consumed a security token!
    pos += 5;
  }

  static {
    JsonReaderInternalAccess.INSTANCE = new JsonReaderInternalAccess() {
      @Override public void promoteNameToValue(JsonReader reader) throws IOException {
        if (reader instanceof JsonTreeReader) {
          ((JsonTreeReader)reader).promoteNameToValue();
          return;
        }
        int p = reader.peekInternal();
        if (p == PEEKED_DOUBLE_QUOTED_NAME) {
          reader.peeked = PEEKED_DOUBLE_QUOTED;
        } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
          reader.peeked = PEEKED_SINGLE_QUOTED;
        } else if (p == PEEKED_UNQUOTED_NAME) {
          reader.peeked = PEEKED_UNQUOTED;
        } else {
          throw reader.throwUnexpectedTokenError(JsonToken.NAME, p);
        }
      }
    };
  }
}
