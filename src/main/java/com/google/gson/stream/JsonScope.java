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
enum JsonScope {

    /**
     * An array with no elements requires no separators or newlines before
     * it is closed.
     */
    EMPTY_ARRAY,

    /**
     * A array with at least one value requires a comma and newline before
     * the next element.
     */
    NONEMPTY_ARRAY,

    /**
     * An object with no name/value pairs requires no separators or newlines
     * before it is closed.
     */
    EMPTY_OBJECT,

    /**
     * An object whose most recent element is a key. The next element must
     * be a value.
     */
    DANGLING_NAME,

    /**
     * An object with at least one name/value pair requires a comma and
     * newline before the next element.
     */
    NONEMPTY_OBJECT,

    /**
     * No object or array has been started.
     */
    EMPTY_DOCUMENT,

    /**
     * A document with at an array or object.
     */
    NONEMPTY_DOCUMENT,

    /**
     * A document that's been closed and cannot be accessed.
     */
    CLOSED,
}
