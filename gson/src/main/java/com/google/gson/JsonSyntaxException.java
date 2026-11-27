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
package com.google.gson;

/**
 * This exception is raised when Gson attempts to read malformed JSON data, or JSON data which is
 * well-formed but does not have the expected structure or format. For example, it is thrown when a
 * JSON string is found where a JSON array was expected, or when a JSON number cannot be represented
 * by the requested Java type, such as {@code 10000000000} for an {@code int}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonSyntaxException extends JsonParseException {

  private static final long serialVersionUID = 1L;

  public JsonSyntaxException(String msg) {
    super(msg);
  }

  public JsonSyntaxException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Creates exception with the specified cause. Consider using {@link #JsonSyntaxException(String,
   * Throwable)} instead if you can describe what actually happened.
   *
   * @param cause root exception that caused this exception to be thrown.
   */
  public JsonSyntaxException(Throwable cause) {
    super(cause);
  }
}
