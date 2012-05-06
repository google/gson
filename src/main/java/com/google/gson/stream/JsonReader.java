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
import com.google.gson.internal.bind.JsonTreeReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

/**
 * Reads a JSON (<a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>)
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
 * <a name="nonexecuteprefix"/><h3>Non-Execute Prefix</h3>
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

  /** The only non-execute prefix this parser permits */
  private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();

  private static final String TRUE = "true";
  private static final String FALSE = "false";

  private final StringPool stringPool = new StringPool();

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

  /*
   * The offset of the first character in the buffer.
   */
  private int bufferStartLine = 1;
  private int bufferStartColumn = 1;

  /*
   * The nesting stack. Using a manual array rather than an ArrayList saves 20%.
   */
  private JsonScope[] stack = new JsonScope[32];
  private int stackSize = 0;
  {
    push(JsonScope.EMPTY_DOCUMENT);
  }

  /**
   * The type of the next token to be returned by {@link #peek} and {@link
   * #advance}. If null, peek() will assign a value.
   */
  private JsonToken token;

  /** The text of the next name. */
  private String name;

  /*
   * For the next literal value, we may have the text value, or the position
   * and length in the buffer.
   */
  private String value;
  private int valuePos;
  private int valueLength;

  /** True if we're currently handling a skipValue() call. */
  private boolean skipping = false;

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
   * Configure this parser to be  be liberal in what it accepts. By default,
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
    expect(JsonToken.BEGIN_ARRAY);
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is the
   * end of the current array.
   */
  public void endArray() throws IOException {
    expect(JsonToken.END_ARRAY);
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is the
   * beginning of a new object.
   */
  public void beginObject() throws IOException {
    expect(JsonToken.BEGIN_OBJECT);
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is the
   * end of the current object.
   */
  public void endObject() throws IOException {
    expect(JsonToken.END_OBJECT);
  }

  /**
   * Consumes {@code expected}.
   */
  private void expect(JsonToken expected) throws IOException {
    peek();
    if (token != expected) {
      throw new IllegalStateException("Expected " + expected + " but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }
    advance();
  }

  /**
   * Returns true if the current array or object has another element.
   */
  public boolean hasNext() throws IOException {
    peek();
    return token != JsonToken.END_OBJECT && token != JsonToken.END_ARRAY;
  }

  /**
   * Returns the type of the next token without consuming it.
   */
  public JsonToken peek() throws IOException {
    if (token != null) {
      return token;
    }

    switch (stack[stackSize - 1]) {
    case EMPTY_DOCUMENT:
      if (lenient) {
        consumeNonExecutePrefix();
      }
      stack[stackSize - 1] = JsonScope.NONEMPTY_DOCUMENT;
      JsonToken firstToken = nextValue();
      if (!lenient && token != JsonToken.BEGIN_ARRAY && token != JsonToken.BEGIN_OBJECT) {
        throw new IOException("Expected JSON document to start with '[' or '{' but was " + token
            + " at line " + getLineNumber() + " column " + getColumnNumber());
      }
      return firstToken;
    case EMPTY_ARRAY:
      return nextInArray(true);
    case NONEMPTY_ARRAY:
      return nextInArray(false);
    case EMPTY_OBJECT:
      return nextInObject(true);
    case DANGLING_NAME:
      return objectValue();
    case NONEMPTY_OBJECT:
      return nextInObject(false);
    case NONEMPTY_DOCUMENT:
      int c = nextNonWhitespace(false);
      if (c == -1) {
        return JsonToken.END_DOCUMENT;
      }
      pos--;
      if (!lenient) {
        throw syntaxError("Expected EOF");
      }
      return nextValue();
    case CLOSED:
      throw new IllegalStateException("JsonReader is closed");
    default:
      throw new AssertionError();
    }
  }

  /**
   * Consumes the non-execute prefix if it exists.
   */
  private void consumeNonExecutePrefix() throws IOException {
    // fast forward through the leading whitespace
    nextNonWhitespace(true);
    pos--;

    if (pos + NON_EXECUTE_PREFIX.length > limit && !fillBuffer(NON_EXECUTE_PREFIX.length)) {
      return;
    }

    for (int i = 0; i < NON_EXECUTE_PREFIX.length; i++) {
      if (buffer[pos + i] != NON_EXECUTE_PREFIX[i]) {
        return; // not a security token!
      }
    }

    // we consumed a security token!
    pos += NON_EXECUTE_PREFIX.length;
  }

  /**
   * Advances the cursor in the JSON stream to the next token.
   */
  private JsonToken advance() throws IOException {
    peek();

    JsonToken result = token;
    token = null;
    value = null;
    name = null;
    return result;
  }

  /**
   * Returns the next token, a {@link JsonToken#NAME property name}, and
   * consumes it.
   *
   * @throws IOException if the next token in the stream is not a property
   *     name.
   */
  public String nextName() throws IOException {
    peek();
    if (token != JsonToken.NAME) {
      throw new IllegalStateException("Expected a name but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }
    String result = name;
    advance();
    return result;
  }

  /**
   * Returns the {@link JsonToken#STRING string} value of the next token,
   * consuming it. If the next token is a number, this method will return its
   * string form.
   *
   * @throws IllegalStateException if the next token is not a string or if
   *     this reader is closed.
   */
  public String nextString() throws IOException {
    peek();
    if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
      throw new IllegalStateException("Expected a string but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    String result = value;
    advance();
    return result;
  }

  /**
   * Returns the {@link JsonToken#BOOLEAN boolean} value of the next token,
   * consuming it.
   *
   * @throws IllegalStateException if the next token is not a boolean or if
   *     this reader is closed.
   */
  public boolean nextBoolean() throws IOException {
    peek();
    if (token != JsonToken.BOOLEAN) {
        throw new IllegalStateException("Expected a boolean but was " + token
            + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    boolean result = (value == TRUE);
    advance();
    return result;
  }

  /**
   * Consumes the next token from the JSON stream and asserts that it is a
   * literal null.
   *
   * @throws IllegalStateException if the next token is not null or if this
   *     reader is closed.
   */
  public void nextNull() throws IOException {
    peek();
    if (token != JsonToken.NULL) {
      throw new IllegalStateException("Expected null but was " + token
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    advance();
  }

  /**
   * Returns the {@link JsonToken#NUMBER double} value of the next token,
   * consuming it. If the next token is a string, this method will attempt to
   * parse it as a double using {@link Double#parseDouble(String)}.
   *
   * @throws IllegalStateException if the next token is not a literal value.
   * @throws NumberFormatException if the next literal value cannot be parsed
   *     as a double, or is non-finite.
   */
  public double nextDouble() throws IOException {
    peek();
    if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
      throw new IllegalStateException("Expected a double but was " + token
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    double result = Double.parseDouble(value);

    if ((result >= 1.0d && value.startsWith("0"))) {
      throw new MalformedJsonException("JSON forbids octal prefixes: " + value
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }
    if (!lenient && (Double.isNaN(result) || Double.isInfinite(result))) {
      throw new MalformedJsonException("JSON forbids NaN and infinities: " + value
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    advance();
    return result;
  }

  /**
   * Returns the {@link JsonToken#NUMBER long} value of the next token,
   * consuming it. If the next token is a string, this method will attempt to
   * parse it as a long. If the next token's numeric value cannot be exactly
   * represented by a Java {@code long}, this method throws.
   *
   * @throws IllegalStateException if the next token is not a literal value.
   * @throws NumberFormatException if the next literal value cannot be parsed
   *     as a number, or exactly represented as a long.
   */
  public long nextLong() throws IOException {
    peek();
    if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
      throw new IllegalStateException("Expected a long but was " + token
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    long result;
    try {
      result = Long.parseLong(value);
    } catch (NumberFormatException ignored) {
      double asDouble = Double.parseDouble(value); // don't catch this NumberFormatException
      result = (long) asDouble;
      if (result != asDouble) {
        throw new NumberFormatException("Expected a long but was " + value
            + " at line " + getLineNumber() + " column " + getColumnNumber());
      }
    }

    if (result >= 1L && value.startsWith("0")) {
      throw new MalformedJsonException("JSON forbids octal prefixes: " + value
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    advance();
    return result;
  }

  /**
   * Returns the {@link JsonToken#NUMBER int} value of the next token,
   * consuming it. If the next token is a string, this method will attempt to
   * parse it as an int. If the next token's numeric value cannot be exactly
   * represented by a Java {@code int}, this method throws.
   *
   * @throws IllegalStateException if the next token is not a literal value.
   * @throws NumberFormatException if the next literal value cannot be parsed
   *     as a number, or exactly represented as an int.
   */
  public int nextInt() throws IOException {
    peek();
    if (token != JsonToken.STRING && token != JsonToken.NUMBER) {
      throw new IllegalStateException("Expected an int but was " + token
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    int result;
    try {
      result = Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      double asDouble = Double.parseDouble(value); // don't catch this NumberFormatException
      result = (int) asDouble;
      if (result != asDouble) {
        throw new NumberFormatException("Expected an int but was " + value
            + " at line " + getLineNumber() + " column " + getColumnNumber());
      }
    }

    if (result >= 1L && value.startsWith("0")) {
      throw new MalformedJsonException("JSON forbids octal prefixes: " + value
          + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    advance();
    return result;
  }

  /**
   * Closes this JSON reader and the underlying {@link Reader}.
   */
  public void close() throws IOException {
    value = null;
    token = null;
    stack[0] = JsonScope.CLOSED;
    stackSize = 1;
    in.close();
  }

  /**
   * Skips the next value recursively. If it is an object or array, all nested
   * elements are skipped. This method is intended for use when the JSON token
   * stream contains unrecognized or unhandled values.
   */
  public void skipValue() throws IOException {
    skipping = true;
    try {
      int count = 0;
      do {
        JsonToken token = advance();
        if (token == JsonToken.BEGIN_ARRAY || token == JsonToken.BEGIN_OBJECT) {
          count++;
        } else if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
          count--;
        }
      } while (count != 0);
    } finally {
      skipping = false;
    }
  }

  private void push(JsonScope newTop) {
    if (stackSize == stack.length) {
      JsonScope[] newStack = new JsonScope[stackSize * 2];
      System.arraycopy(stack, 0, newStack, 0, stackSize);
      stack = newStack;
    }
    stack[stackSize++] = newTop;
  }

  @SuppressWarnings("fallthrough")
  private JsonToken nextInArray(boolean firstElement) throws IOException {
    if (firstElement) {
      stack[stackSize - 1] = JsonScope.NONEMPTY_ARRAY;
    } else {
      /* Look for a comma before each element after the first element. */
      switch (nextNonWhitespace(true)) {
      case ']':
        stackSize--;
        return token = JsonToken.END_ARRAY;
      case ';':
        checkLenient(); // fall-through
      case ',':
        break;
      default:
        throw syntaxError("Unterminated array");
      }
    }

    switch (nextNonWhitespace(true)) {
    case ']':
      if (firstElement) {
        stackSize--;
        return token = JsonToken.END_ARRAY;
      }
      // fall-through to handle ",]"
    case ';':
    case ',':
      /* In lenient mode, a 0-length literal means 'null' */
      checkLenient();
      pos--;
      value = "null";
      return token = JsonToken.NULL;
    default:
      pos--;
      return nextValue();
    }
  }

  @SuppressWarnings("fallthrough")
  private JsonToken nextInObject(boolean firstElement) throws IOException {
    /*
     * Read delimiters. Either a comma/semicolon separating this and the
     * previous name-value pair, or a close brace to denote the end of the
     * object.
     */
    if (firstElement) {
      /* Peek to see if this is the empty object. */
      switch (nextNonWhitespace(true)) {
      case '}':
        stackSize--;
        return token = JsonToken.END_OBJECT;
      default:
        pos--;
      }
    } else {
      switch (nextNonWhitespace(true)) {
      case '}':
        stackSize--;
        return token = JsonToken.END_OBJECT;
      case ';':
      case ',':
        break;
      default:
        throw syntaxError("Unterminated object");
      }
    }

    /* Read the name. */
    int quote = nextNonWhitespace(true);
    switch (quote) {
    case '\'':
      checkLenient(); // fall-through
    case '"':
      name = nextString((char) quote);
      break;
    default:
      checkLenient();
      pos--;
      name = nextLiteral(false);
      if (name.length() == 0) {
        throw syntaxError("Expected name");
      }
    }

    stack[stackSize - 1] = JsonScope.DANGLING_NAME;
    return token = JsonToken.NAME;
  }

  private JsonToken objectValue() throws IOException {
    /*
     * Read the name/value separator. Usually a colon ':'. In lenient mode
     * we also accept an equals sign '=', or an arrow "=>".
     */
    switch (nextNonWhitespace(true)) {
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

    stack[stackSize - 1] = JsonScope.NONEMPTY_OBJECT;
    return nextValue();
  }

  @SuppressWarnings("fallthrough")
  private JsonToken nextValue() throws IOException {
    int c = nextNonWhitespace(true);
    switch (c) {
    case '{':
      push(JsonScope.EMPTY_OBJECT);
      return token = JsonToken.BEGIN_OBJECT;

    case '[':
      push(JsonScope.EMPTY_ARRAY);
      return token = JsonToken.BEGIN_ARRAY;

    case '\'':
      checkLenient(); // fall-through
    case '"':
      value = nextString((char) c);
      return token = JsonToken.STRING;

    default:
      pos--;
      return readLiteral();
    }
  }

  /**
   * Returns true once {@code limit - pos >= minimum}. If the data is
   * exhausted before that many characters are available, this returns
   * false.
   */
  private boolean fillBuffer(int minimum) throws IOException {
    char[] buffer = this.buffer;

    // Before clobbering the old characters, update where buffer starts
    // Using locals here saves ~2%.
    int line = bufferStartLine;
    int column = bufferStartColumn;
    for (int i = 0, p = pos; i < p; i++) {
      if (buffer[i] == '\n') {
        line++;
        column = 1;
      } else {
        column++;
      }
    }
    bufferStartLine = line;
    bufferStartColumn = column;

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
      if (bufferStartLine == 1 && bufferStartColumn == 1 && limit > 0 && buffer[0] == '\ufeff') {
        pos++;
        bufferStartColumn--;
      }

      if (limit >= minimum) {
        return true;
      }
    }
    return false;
  }

  private int getLineNumber() {
    int result = bufferStartLine;
    for (int i = 0; i < pos; i++) {
      if (buffer[i] == '\n') {
        result++;
      }
    }
    return result;
  }

  private int getColumnNumber() {
    int result = bufferStartColumn;
    for (int i = 0; i < pos; i++) {
      if (buffer[i] == '\n') {
        result = 1;
      } else {
        result++;
      }
    }
    return result;
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
      switch (c) {
      case '\t':
      case ' ':
      case '\n':
      case '\r':
        continue;

      case '/':
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

      case '#':
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
        continue;

      default:
        pos = p;
        return c;
      }
    }
    if (throwOnEof) {
      throw new EOFException("End of input"
          + " at line " + getLineNumber() + " column " + getColumnNumber());
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
      if (c == '\r' || c == '\n') {
        break;
      }
    }
  }

  private boolean skipTo(String toFind) throws IOException {
    outer:
    for (; pos + toFind.length() <= limit || fillBuffer(toFind.length()); pos++) {
      for (int c = 0; c < toFind.length(); c++) {
        if (buffer[pos + c] != toFind.charAt(c)) {
          continue outer;
        }
      }
      return true;
    }
    return false;
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
  private String nextString(char quote) throws IOException {
    // Like nextNonWhitespace, this uses locals 'p' and 'l' to save inner-loop field access.
    char[] buffer = this.buffer;
    StringBuilder builder = null;
    while (true) {
      int p = pos;
      int l = limit;
      /* the index of the first character not yet appended to the builder. */
      int start = p;
      while (p < l) {
        int c = buffer[p++];

        if (c == quote) {
          pos = p;
          if (skipping) {
            return "skipped!";
          } else if (builder == null) {
            return stringPool.get(buffer, start, p - start - 1);
          } else {
            builder.append(buffer, start, p - start - 1);
            return builder.toString();
          }

        } else if (c == '\\') {
          pos = p;
          if (builder == null) {
            builder = new StringBuilder();
          }
          builder.append(buffer, start, p - start - 1);
          builder.append(readEscapeCharacter());
          p = pos;
          l = limit;
          start = p;
        }
      }

      if (builder == null) {
        builder = new StringBuilder();
      }
      builder.append(buffer, start, p - start);
      pos = p;
      if (!fillBuffer(1)) {
        throw syntaxError("Unterminated string");
      }
    }
  }

  /**
   * Reads the value up to but not including any delimiter characters. This
   * does not consume the delimiter character.
   *
   * @param assignOffsetsOnly true for this method to only set the valuePos
   *     and valueLength fields and return a null result. This only works if
   *     the literal is short; a string is returned otherwise.
   */
  @SuppressWarnings("fallthrough")
  private String nextLiteral(boolean assignOffsetsOnly) throws IOException {
    StringBuilder builder = null;
    valuePos = -1;
    valueLength = 0;
    int i = 0;

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
            break findNonLiteralCharacter;
        }
      }

      /*
       * Attempt to load the entire literal into the buffer at once. If
       * we run out of input, add a non-literal character at the end so
       * that decoding doesn't need to do bounds checks.
       */
      if (i < buffer.length) {
        if (fillBuffer(i + 1)) {
          continue;
        } else {
          buffer[limit] = '\0';
          break;
        }
      }

      // use a StringBuilder when the value is too long. It must be an unquoted string.
      if (builder == null) {
        builder = new StringBuilder();
      }
      builder.append(buffer, pos, i);
      valueLength += i;
      pos += i;
      i = 0;
      if (!fillBuffer(1)) {
        break;
      }
    }

    String result;
    if (assignOffsetsOnly && builder == null) {
      valuePos = pos;
      result = null;
    } else if (skipping) {
      result = "skipped!";
    } else if (builder == null) {
      result = stringPool.get(buffer, pos, i);
    } else {
      builder.append(buffer, pos, i);
      result = builder.toString();
    }
    valueLength += i;
    pos += i;
    return result;
  }

  @Override public String toString() {
    return getClass().getSimpleName()
        + " at line " + getLineNumber() + " column " + getColumnNumber();
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
          throw new NumberFormatException("\\u" + stringPool.get(buffer, pos, 4));
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

    case '\'':
    case '"':
    case '\\':
    default:
      return escaped;
    }
  }

  /**
   * Reads a null, boolean, numeric or unquoted string literal value.
   */
  private JsonToken readLiteral() throws IOException {
    value = nextLiteral(true);
    if (valueLength == 0) {
      throw syntaxError("Expected literal value");
    }
    token = decodeLiteral();
    if (token == JsonToken.STRING) {
      checkLenient();
    }
    return token;
  }

  /**
   * Assigns {@code nextToken} based on the value of {@code nextValue}.
   */
  private JsonToken decodeLiteral() throws IOException {
    if (valuePos == -1) {
      // it was too long to fit in the buffer so it can only be a string
      return JsonToken.STRING;
    } else if (valueLength == 4
        && ('n' == buffer[valuePos    ] || 'N' == buffer[valuePos    ])
        && ('u' == buffer[valuePos + 1] || 'U' == buffer[valuePos + 1])
        && ('l' == buffer[valuePos + 2] || 'L' == buffer[valuePos + 2])
        && ('l' == buffer[valuePos + 3] || 'L' == buffer[valuePos + 3])) {
      value = "null";
      return JsonToken.NULL;
    } else if (valueLength == 4
        && ('t' == buffer[valuePos    ] || 'T' == buffer[valuePos    ])
        && ('r' == buffer[valuePos + 1] || 'R' == buffer[valuePos + 1])
        && ('u' == buffer[valuePos + 2] || 'U' == buffer[valuePos + 2])
        && ('e' == buffer[valuePos + 3] || 'E' == buffer[valuePos + 3])) {
      value = TRUE;
      return JsonToken.BOOLEAN;
    } else if (valueLength == 5
        && ('f' == buffer[valuePos    ] || 'F' == buffer[valuePos    ])
        && ('a' == buffer[valuePos + 1] || 'A' == buffer[valuePos + 1])
        && ('l' == buffer[valuePos + 2] || 'L' == buffer[valuePos + 2])
        && ('s' == buffer[valuePos + 3] || 'S' == buffer[valuePos + 3])
        && ('e' == buffer[valuePos + 4] || 'E' == buffer[valuePos + 4])) {
      value = FALSE;
      return JsonToken.BOOLEAN;
    } else {
      value = stringPool.get(buffer, valuePos, valueLength);
      return decodeNumber(buffer, valuePos, valueLength);
    }
  }

  /**
   * Determine whether the characters is a JSON number. Numbers are of the
   * form -12.34e+56. Fractional and exponential parts are optional. Leading
   * zeroes are not allowed in the value or exponential part, but are allowed
   * in the fraction.
   */
  private JsonToken decodeNumber(char[] chars, int offset, int length) {
    int i = offset;
    int c = chars[i];

    if (c == '-') {
      c = chars[++i];
    }

    if (c == '0') {
      c = chars[++i];
    } else if (c >= '1' && c <= '9') {
      c = chars[++i];
      while (c >= '0' && c <= '9') {
        c = chars[++i];
      }
    } else {
      return JsonToken.STRING;
    }

    if (c == '.') {
      c = chars[++i];
      while (c >= '0' && c <= '9') {
        c = chars[++i];
      }
    }

    if (c == 'e' || c == 'E') {
      c = chars[++i];
      if (c == '+' || c == '-') {
        c = chars[++i];
      }
      if (c >= '0' && c <= '9') {
        c = chars[++i];
        while (c >= '0' && c <= '9') {
          c = chars[++i];
        }
      } else {
        return JsonToken.STRING;
      }
    }

    if (i == offset + length) {
      return JsonToken.NUMBER;
    } else {
      return JsonToken.STRING;
    }
  }

  /**
   * Throws a new IO exception with the given message and a context snippet
   * with this reader's content.
   */
  private IOException syntaxError(String message) throws IOException {
    throw new MalformedJsonException(message
        + " at line " + getLineNumber() + " column " + getColumnNumber());
  }

  static {
    JsonReaderInternalAccess.INSTANCE = new JsonReaderInternalAccess() {
      @Override public void promoteNameToValue(JsonReader reader) throws IOException {
        if (reader instanceof JsonTreeReader) {
          ((JsonTreeReader)reader).promoteNameToValue();
          return;
        }
        reader.peek();
        if (reader.token != JsonToken.NAME) {
          throw new IllegalStateException("Expected a name but was " + reader.peek() + " "
              + " at line " + reader.getLineNumber() + " column " + reader.getColumnNumber());
        }
        reader.value = reader.name;
        reader.name = null;
        reader.token = JsonToken.STRING;
      }
    };
  }
}
