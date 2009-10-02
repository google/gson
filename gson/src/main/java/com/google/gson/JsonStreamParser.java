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

import java.io.EOFException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A streaming parser that allows reading of multiple {@link JsonElement}s from the specified reader
 * asynchronously.
 * 
 * <p>This class is thread-compatible. For some more literature on these definitions, refer to
 * Effective Java.
 *
 * <p>To properly use this class across multiple thread, you will need to add some external
 * synchronization to your classes/thread to get this to work properly.  For example:
 * 
 * <pre>
 * JsonStreamParser parser = new JsonStreamParser("blah blah blah");
 * JsonElement element;
 * synchronized (someCommonObject) {
 *   if (parser.hasNext()) {
 *     element = parser.next();
 *   }
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.4
 */
public final class JsonStreamParser implements Iterator<JsonElement> {

  private final JsonParserJavacc parser;
  private final Object lock;
  private JsonElement nextElement;

  /**
   * @param json The string containing JSON elements concatenated to each other.
   * @since 1.4
   */
  public JsonStreamParser(String json) {
    this(new StringReader(json));      
  }
  
  /**
   * @param reader The data stream containing JSON elements concatenated to each other.
   * @since 1.4
   */
  public JsonStreamParser(Reader reader) {
    parser = new JsonParserJavacc(reader);
    lock = new Object();
    nextElement = null;
  }
  
  /**
   * Returns the next available {@link JsonElement} on the reader. Null if none available.
   * 
   * @return the next available {@link JsonElement} on the reader. Null if none available.
   * @throws JsonParseException if the incoming stream is malformed JSON.
   * @since 1.4
   */
  public JsonElement next() throws JsonParseException {
    synchronized (lock) {
      if (nextElement != null) {
        JsonElement returnValue = nextElement;
        nextElement = null;
        return returnValue;
      }
    }

    try {
      return parser.parse();
    } catch (TokenMgrError e) {
      throw new JsonParseException("Failed parsing JSON source to Json", e);
    } catch (ParseException e) {
      throw new JsonParseException("Failed parsing JSON source to Json", e);
    } catch (StackOverflowError e) {
      throw new JsonParseException("Failed parsing JSON source to Json", e);
    } catch (OutOfMemoryError e) {
      throw new JsonParseException("Failed parsing JSON source to Json", e);
    } catch (JsonParseException e) {
      if (e.getCause() instanceof EOFException) {
        throw new NoSuchElementException();
      } else {
        throw e;
      }
    }
  }

  public boolean hasNext() {
    synchronized (lock) {
      try {
        nextElement = next();
        return true;
      } catch (NoSuchElementException e) {
        nextElement = null;
        return false;
      }
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
