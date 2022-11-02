/*
 * Copyright (C) 2022 Google Inc.
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
 * An enumeration that defines the kind of newline to use for serialization.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Newline">Wikipedia Newline article</a>
 *
 * @since $next-version$
 */
public enum NewlineStyle {
  /**
   * Using this style will result in the same kind of newline that the current environment uses.
   *
   * <p>So it will produce {@code "\r\n"} when running on Windows, and {@code "\n"} when running
   * on macOS &amp; Linux.</p>
   *
   * @see System#lineSeparator()
   */
  CURRENT_OS(""),

  /**
   * Using this style will result in the same newline convention that Windows uses
   * and MS-DOS used. This is {@code "\r\n"} ({@code U+000D U+000A}).
   */
  CRLF("\r\n"),

  /**
   * Using this style will result in the same newline convention that macOS, Linux, and UNIX-like
   * systems use. This is {@code "\n"} ({@code U+000A}).
   */
  LF("\n"),

  /**
   * Using this style will result in the same newline convention that classic Mac OS used.
   * Rarely needed. This is {@code "\r"} ({@code U+000D}).
   */
  CR("\r");

  private final String value;

  private NewlineStyle(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
