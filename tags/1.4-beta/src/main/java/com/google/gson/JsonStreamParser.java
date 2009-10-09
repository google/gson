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
 * <p>This class is conditionally thread-safe (see Item 70, Effective Java second edition). To
 * properly use this class across multiple threads, you will need to add some external
 * synchronization.  For example:
 * 
 * <pre>
 * JsonStreamParser parser = new JsonStreamParser("['first'] {'second':10} 'third'");
 * JsonElement element;
 * synchronized (parser) {  // synchronize on an object shared by threads
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

  /**
   * Returns true if a {@link JsonElement} is available on the input for consumption
   * @return true if a {@link JsonElement} is available on the input, false otherwise
   * @since 1.4
   */
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

  /**
   * This optional {@link Iterator} method is not relevant for stream parsing and hence is not
   * implemented.
   * @since 1.4
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
