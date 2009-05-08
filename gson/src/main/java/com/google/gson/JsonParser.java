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

import java.io.Reader;
import java.io.StringReader;

/**
 * A parser to parse Json into a parse tree of {@link JsonElement}s
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @since 1.3
 */
public final class JsonParser {
  
  /**
   * Interface to provide ability to read multiple {@link JsonElement}s from a stream 
   * asynchronously.
   * 
   * @since 1.4
   */
  public interface AsyncReader {

    /**
     * Parse and return one {@link JsonElement} 
     * @since 1.4
     */
    public JsonElement readElement();
  }

  /**
   * Parses the specified JSON string into a parse tree
   * 
   * @param json JSON text
   * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON 
   * @throws JsonParseException if the specified text is not valid JSON
   * @since 1.3
   */
  public JsonElement parse(String json) throws JsonParseException {
    return parse(new StringReader(json));
  }
  
  /**
   * Parses the specified JSON string into a parse tree
   * 
   * @param json JSON text
   * @return a parse tree of {@link JsonElement}s corresponding to the specified JSON 
   * @throws JsonParseException if the specified text is not valid JSON
   * @since 1.3
   */
  public JsonElement parse(Reader json) throws JsonParseException {
    try {
      JsonParserJavacc parser = new JsonParserJavacc(json);
      JsonElement element = parser.parse();
      return element;
    } catch (TokenMgrError e) {
      throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
    } catch (ParseException e) {
      throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
    } catch (StackOverflowError e) {
      throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
    } catch (OutOfMemoryError e) {
      throw new JsonParseException("Failed parsing JSON source: " + json + " to Json", e);
    }
  }
  
  /**
   * Returns {@link AsyncReader} to allow reading of multiple {@link JsonElement}s from the 
   * specified reader asynchronously.
   * 
   * @param json The data stream containing JSON elements concatenated to each other.
   * @return {@link AsyncReader} for reading {@link JsonElement}s asynchronously.
   * @throws JsonParseException if the incoming stream is malformed JSON.
   * @since 1.4
   */
  public AsyncReader parseAsync(Reader json) throws JsonParseException {
    return new AsyncReaderJavacc(json);
  }
  
  private static class AsyncReaderJavacc implements AsyncReader {    
    private final JsonParserJavacc parser;
    private AsyncReaderJavacc(Reader json) {
      parser = new JsonParserJavacc(json);      
    }
    
    public JsonElement readElement() {
      try {
        JsonElement element = parser.parse();
        return element;
      } catch (TokenMgrError e) {
        throw new JsonParseException("Failed parsing JSON source to Json", e);
      } catch (ParseException e) {
        throw new JsonParseException("Failed parsing JSON source to Json", e);
      } catch (StackOverflowError e) {
        throw new JsonParseException("Failed parsing JSON source to Json", e);
      } catch (OutOfMemoryError e) {
        throw new JsonParseException("Failed parsing JSON source to Json", e);
      }
    }    
  }
}
