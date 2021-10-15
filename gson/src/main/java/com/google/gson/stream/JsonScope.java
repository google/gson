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

/**
 * Lexical scoping elements within a JSON reader or writer.
 *
 * @author Jesse Wilson
 * @since 1.6
 */
final class JsonScope {

  /**
   * An array with no elements requires no separator before it is closed.
   */
  static final int EMPTY_ARRAY = 1;

  /**
   * An array with at least one value requires an element separator (e.g. comma) before
   * the next element.
   */
  static final int NONEMPTY_ARRAY = 2;

  /**
   * An array with a value followed by an element separator (e.g. comma) requires a
   * subsequent element before it is closed.
   * <p>Note: Only used by {@link JsonReader}.
   */
  static final int EXPECTING_ARRAY_ELEMENT = 3;

  /**
   * An object with no name/value pairs requires no separator (e.g. comma) before
   * it is closed.
   */
  static final int EMPTY_OBJECT = 4;

  /**
   * An object with at least one name/value pair followed by a separator (e.g. comma)
   * requires a subsequent name/value pair before it is closed.
   * <p>Note: Only used by {@link JsonReader}.
   */
  static final int EXPECTING_NAME = 5;

  /**
   * An object whose most recent element is a property name requires a name/value
   * separator (e.g. colon) and afterwards a {@link #EXPECTING_PROPERTY_VALUE value}.
   */
  static final int DANGLING_NAME = 6;

  /**
   * An object property name followed by a name/value separator (e.g. colon) requires
   * a property value.
   * <p>Note: Only used by {@link JsonReader}.
   */
  static final int EXPECTING_PROPERTY_VALUE = 7;

  /**
   * An object with at least one name/value pair requires a separator (e.g. comma)
   * before the next name/value pair.
   */
  static final int NONEMPTY_OBJECT = 8;

  /**
   * A block comment <code>/* ... *&#x2F;</code> which has been started and
   * requires the closing <code>*&#x2F;</code>.
   * <p>Note: Only used by {@link JsonReader}.
   */
  static final int EXPECTING_BLOCK_COMMENT_END = 9;

  /**
   * No object or array has been started.
   */
  static final int EMPTY_DOCUMENT = 10;

  /**
   * A document with an array or object.
   */
  static final int NONEMPTY_DOCUMENT = 11;

  /**
   * A document that's been closed and cannot be accessed.
   * <p>Note: Only used by {@link JsonReader}.
   */
  static final int CLOSED = 12;
}
