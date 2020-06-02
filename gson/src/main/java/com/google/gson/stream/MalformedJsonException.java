/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.gson.stream;

import java.io.IOException;

/**
 * Thrown when a reader encounters malformed JSON. Some syntax errors can be ignored by calling
 * {@link JsonReader#setLenient(boolean)}.
 */
public final class MalformedJsonException extends IOException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates exception with the specified message. If you are wrapping another exception, consider
   * using {@link #MalformedJsonException(String, Throwable)} instead.
   *
   * @param msg error message describing a possible cause of this exception.
   */
  public MalformedJsonException(String msg) {
    super(msg);
  }

  /**
   * Creates exception with the specified message and cause.
   *
   * @param msg   error message describing what happened.
   * @param cause root exception that caused this exception to be thrown.
   */
  public MalformedJsonException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Creates exception with the specified cause. Consider using {@link
   * #MalformedJsonException(String, Throwable)} instead if you can describe what happened.
   *
   * @param cause root exception that caused this exception to be thrown.
   */
  public MalformedJsonException(Throwable cause) {
    super(cause);
  }
}
