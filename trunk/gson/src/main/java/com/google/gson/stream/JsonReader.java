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
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a JSON (<a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>)
 * encoded value as a stream of tokens. This stream includes both literal
 * values (strings, numbers, booleans, and nulls) as well as the begin and
 * end delimiters of objects and arrays. The tokens are traversed in
 * depth-first order, the same order that they appear in the JSON document.
 * Within JSON objects, name/value pairs are represented by a single token.
 *
 * <h3>Parsing JSON</h3>
 * To create a recursive descent parser your own JSON streams, first create an
 * entry point method that creates a {@code JsonReader}.
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
 *     return readMessagesArray(reader);
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
public final class JsonReader implements Closeable {

  /** The only non-execute prefix this parser permits */
  private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();

  /** The input JSON. */
  private final Reader in;

  /** True to accept non-spec compliant JSON */
  private boolean lenient = false;

  /**
   * Use a manual buffer to easily read and unread upcoming characters, and
   * also so we can create strings without an intermediate StringBuilder.
   */
  private final char[] buffer = new char[1024];
  private int pos = 0;
  private int limit = 0;

  private final List<JsonScope> stack = new ArrayList<JsonScope>();
  {
    push(JsonScope.EMPTY_DOCUMENT);
  }

  /**
   * True if we've already read the next token. If we have, the string value
   * for that token will be assigned to {@code value} if such a string value
   * exists. And the token type will be assigned to {@code token} if the token
   * type is known. The token type may be null for literals, since we derive
   * that lazily.
   */
  private boolean hasToken;

  /**
   * The type of the next token to be returned by {@link #peek} and {@link
   * #advance}, or {@code null} if it is unknown and must first be derived
   * from {@code value}. This value is undefined if {@code hasToken} is false.
   */
  private JsonToken token;

  /** The text of the next name. */
  private String name;

  /** The text of the next literal value. */
  private String value;

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
  public void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

  /**
   * Returns true if this parser is liberal in what it accepts.
   */
  public boolean isLenient() {
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
   * end of the current array.
   */
  public void endObject() throws IOException {
    expect(JsonToken.END_OBJECT);
  }

  /**
   * Consumes {@code expected}.
   */
  private void expect(JsonToken expected) throws IOException {
    quickPeek();
    if (token != expected) {
      throw new IllegalStateException("Expected " + expected + " but was " + peek());
    }
    advance();
  }

  /**
   * Returns true if the current array or object has another element.
   */
  public boolean hasNext() throws IOException {
    quickPeek();
    return token != JsonToken.END_OBJECT && token != JsonToken.END_ARRAY;
  }

  /**
   * Returns the type of the next token without consuming it.
   */
  public JsonToken peek() throws IOException {
    quickPeek();

    if (token == null) {
      decodeLiteral();
    }

    return token;
  }

  /**
   * Ensures that a token is ready. After this call either {@code token} or
   * {@code value} will be non-null. To ensure {@code token} has a definitive
   * value, use {@link #peek()}
   */
  private JsonToken quickPeek() throws IOException {
    if (hasToken) {
      return token;
    }

    switch (peekStack()) {
    case EMPTY_DOCUMENT:
      if (lenient) {
        consumeNonExecutePrefix();
      }
      replaceTop(JsonScope.NONEMPTY_DOCUMENT);
      JsonToken firstToken = nextValue();
      if (!lenient && firstToken != JsonToken.BEGIN_ARRAY && firstToken != JsonToken.BEGIN_OBJECT) {
        syntaxError("Expected JSON document to start with '[' or '{'");
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
      try {
        JsonToken token = nextValue();
        if (lenient) {
          return token;
        }
        throw syntaxError("Expected EOF");
      } catch (EOFException e) {
        hasToken = true; // TODO: avoid throwing here?
        return token = JsonToken.END_DOCUMENT;
      }
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
    nextNonWhitespace();
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
    quickPeek();

    JsonToken result = token;
    hasToken = false;
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
    quickPeek();
    if (token != JsonToken.NAME) {
      throw new IllegalStateException("Expected a name but was " + peek());
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
    if (value == null || (token != JsonToken.STRING && token != JsonToken.NUMBER)) {
      throw new IllegalStateException("Expected a string but was " + peek());
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
    quickPeek();
    if (value == null || token == JsonToken.STRING) {
      throw new IllegalStateException("Expected a boolean but was " + peek());
    }

    boolean result;
    if (value.equalsIgnoreCase("true")) {
      result = true;
    } else if (value.equalsIgnoreCase("false")) {
      result = false;
    } else {
      throw new IllegalStateException("Not a boolean: " + value);
    }

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
    quickPeek();
    if (value == null || token == JsonToken.STRING) {
      throw new IllegalStateException("Expected null but was " + peek());
    }

    if (!value.equalsIgnoreCase("null")) {
      throw new IllegalStateException("Not a null: " + value);
    }

    advance();
  }

  /**
   * Returns the {@link JsonToken#NUMBER double} value of the next token,
   * consuming it. If the next token is a string, this method will attempt to
   * parse it as a double.
   *
   * @throws IllegalStateException if the next token is not a literal value.
   * @throws NumberFormatException if the next literal value cannot be parsed
   *     as a double, or is non-finite.
   */
  public double nextDouble() throws IOException {
    quickPeek();
    if (value == null) {
      throw new IllegalStateException("Expected a double but was " + peek());
    }

    double result = Double.parseDouble(value);

    if ((result >= 1.0d && value.startsWith("0"))) {
      throw new NumberFormatException("JSON forbids octal prefixes: " + value);
    }

    if (!lenient && (Double.isNaN(result) || Double.isInfinite(result))) {
      throw new NumberFormatException("JSON forbids NaN and infinities: " + value);
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
    quickPeek();
    if (value == null) {
      throw new IllegalStateException("Expected a long but was " + peek());
    }

    long result;
    try {
      result = Long.parseLong(value);
    } catch (NumberFormatException ignored) {
      double asDouble = Double.parseDouble(value); // don't catch this NumberFormatException
      result = (long) asDouble;
      if (result != asDouble) {
        throw new NumberFormatException(value);
      }
    }

    if (result >= 1L && value.startsWith("0")) {
      throw new NumberFormatException("JSON forbids octal prefixes: " + value);
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
    quickPeek();
    if (value == null) {
      throw new IllegalStateException("Expected an int but was " + peek());
    }

    int result;
    try {
      result = Integer.parseInt(value);
    } catch (NumberFormatException ignored) {
      double asDouble = Double.parseDouble(value); // don't catch this NumberFormatException
      result = (int) asDouble;
      if (result != asDouble) {
        throw new NumberFormatException(value);
      }
    }

    if (result >= 1L && value.startsWith("0")) {
      throw new NumberFormatException("JSON forbids octal prefixes: " + value);
    }

    advance();
    return result;
  }

  /**
   * Closes this JSON reader and the underlying {@link Reader}.
   */
  public void close() throws IOException {
    hasToken = false;
    value = null;
    token = null;
    stack.clear();
    stack.add(JsonScope.CLOSED);
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

  private JsonScope peekStack() {
    return stack.get(stack.size() - 1);
  }

  private JsonScope pop() {
    return stack.remove(stack.size() - 1);
  }

  private void push(JsonScope newTop) {
    stack.add(newTop);
  }

  /**
   * Replace the value on the top of the stack with the given value.
   */
  private void replaceTop(JsonScope newTop) {
    stack.set(stack.size() - 1, newTop);
  }

  @SuppressWarnings("fallthrough")
  private JsonToken nextInArray(boolean firstElement) throws IOException {
    if (firstElement) {
      replaceTop(JsonScope.NONEMPTY_ARRAY);
    } else {
      /* Look for a comma before each element after the first element. */
      switch (nextNonWhitespace()) {
      case ']':
        pop();
        hasToken = true;
        return token = JsonToken.END_ARRAY;
      case ';':
        checkLenient(); // fall-through
      case ',':
        break;
      default:
        throw syntaxError("Unterminated array");
      }
    }

    switch (nextNonWhitespace()) {
    case ']':
      if (firstElement) {
        pop();
        hasToken = true;
        return token = JsonToken.END_ARRAY;
      }
      // fall-through to handle ",]"
    case ';':
    case ',':
      /* In lenient mode, a 0-length literal means 'null' */
      checkLenient();
      pos--;
      hasToken = true;
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
      switch (nextNonWhitespace()) {
      case '}':
        pop();
        hasToken = true;
        return token = JsonToken.END_OBJECT;
      default:
        pos--;
      }
    } else {
      switch (nextNonWhitespace()) {
      case '}':
        pop();
        hasToken = true;
        return token = JsonToken.END_OBJECT;
      case ';':
      case ',':
        break;
      default:
        throw syntaxError("Unterminated object");
      }
    }

    /* Read the name. */
    int quote = nextNonWhitespace();
    switch (quote) {
    case '\'':
      checkLenient(); // fall-through
    case '"':
      name = nextString((char) quote);
      break;
    default:
      checkLenient();
      pos--;
      name = nextLiteral();
      if (name.length() == 0) {
        throw syntaxError("Expected name");
      }
    }

    replaceTop(JsonScope.DANGLING_NAME);
    hasToken = true;
    return token = JsonToken.NAME;
  }

  private JsonToken objectValue() throws IOException {
    /*
     * Read the name/value separator. Usually a colon ':'. In lenient mode
     * we also accept an equals sign '=', or an arrow "=>".
     */
    switch (nextNonWhitespace()) {
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

    replaceTop(JsonScope.NONEMPTY_OBJECT);
    return nextValue();
  }

  @SuppressWarnings("fallthrough")
  private JsonToken nextValue() throws IOException {
    int c = nextNonWhitespace();
    switch (c) {
    case '{':
      push(JsonScope.EMPTY_OBJECT);
      hasToken = true;
      return token = JsonToken.BEGIN_OBJECT;

    case '[':
      push(JsonScope.EMPTY_ARRAY);
      hasToken = true;
      return token = JsonToken.BEGIN_ARRAY;

    case '\'':
      checkLenient(); // fall-through
    case '"':
      value = nextString((char) c);
      hasToken = true;
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
      if (limit >= minimum) {
        return true;
      }
    }
    return false;
  }

  private int nextNonWhitespace() throws IOException {
    while (pos < limit || fillBuffer(1)) {
      int c = buffer[pos++];
      switch (c) {
      case '\t':
      case ' ':
      case '\n':
      case '\r':
        continue;

      case '/':
        if (pos == limit && !fillBuffer(1)) {
          return c;
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
          pos += 2;
          continue;

        case '/':
          // skip a // end-of-line comment
          pos++;
          skipToEndOfLine();
          continue;

        default:
          return c;
        }

      case '#':
        /*
         * Skip a # hash end-of-line comment. The JSON RFC doesn't
         * specify this behaviour, but it's required to parse
         * existing documents. See http://b/2571423.
         */
        checkLenient();
        skipToEndOfLine();
        continue;

      default:
        return c;
      }
    }
    throw new EOFException("End of input");
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
    for (; pos + toFind.length() < limit || fillBuffer(toFind.length()); pos++) {
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
    StringBuilder builder = null;
    do {
      /* the index of the first character not yet appended to the builder. */
      int start = pos;
      while (pos < limit) {
        int c = buffer[pos++];

        if (c == quote) {
          if (skipping) {
            return "skipped!";
          } else if (builder == null) {
            return new String(buffer, start, pos - start - 1);
          } else {
            builder.append(buffer, start, pos - start - 1);
            return builder.toString();
          }

        } else if (c == '\\') {
          if (builder == null) {
            builder = new StringBuilder();
          }
          builder.append(buffer, start, pos - start - 1);
          builder.append(readEscapeCharacter());
          start = pos;
        }
      }

      if (builder == null) {
        builder = new StringBuilder();
      }
      builder.append(buffer, start, pos - start);
    } while (fillBuffer(1));

    throw syntaxError("Unterminated string");
  }

  /**
   * Returns the string up to but not including any delimiter characters. This
   * does not consume the delimiter character.
   */
  @SuppressWarnings("fallthrough")
  private String nextLiteral() throws IOException {
    StringBuilder builder = null;
    do {
      /* the index of the first character not yet appended to the builder. */
      int start = pos;
      while (pos < limit) {
        int c = buffer[pos++];
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
          pos--;
          if (skipping) {
            return "skipped!";
          } else if (builder == null) {
            return new String(buffer, start, pos - start);
          } else {
            builder.append(buffer, start, pos - start);
            return builder.toString();
          }
        }
      }

      if (builder == null) {
        builder = new StringBuilder();
      }
      builder.append(buffer, start, pos - start);
    } while (fillBuffer(1));

    return builder.toString();
  }

  @Override public String toString() {
    return getClass().getSimpleName() + " near " + getSnippet();
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
      String hex = new String(buffer, pos, 4);
      pos += 4;
      return (char) Integer.parseInt(hex, 16);

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
    String literal = nextLiteral();
    if (literal.length() == 0) {
      throw syntaxError("Expected literal value");
    }
    value = literal;
    hasToken = true;
    return token = null; // use decodeLiteral() to get the token type
  }

  /**
   * Assigns {@code nextToken} based on the value of {@code nextValue}.
   */
  private void decodeLiteral() throws IOException {
    if (value.equalsIgnoreCase("null")) {
      token = JsonToken.NULL;
    } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
      token = JsonToken.BOOLEAN;
    } else {
      try {
        Double.parseDouble(value); // this work could potentially be cached
        token = JsonToken.NUMBER;
      } catch (NumberFormatException ignored) {
        // this must be an unquoted string
        throw syntaxError("invalid number or unquoted string");
      }
    }
  }

  /**
   * Throws a new IO exception with the given message and a context snippet
   * with this reader's content.
   */
  private IOException syntaxError(String message) throws IOException {
    throw new MalformedJsonException(message + " near " + getSnippet());
  }

  private CharSequence getSnippet() {
    StringBuilder snippet = new StringBuilder();
    int beforePos = Math.min(pos, 20);
    snippet.append(buffer, pos - beforePos, beforePos);
    int afterPos = Math.min(limit - pos, 20);
    snippet.append(buffer, pos, afterPos);
    return snippet;
  }
}
