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

import java.util.Objects;

/**
 * A class used to control what the serialization looks like.
 *
 * <p>It currently defines the kind of newline to use, and the indent, but
 * might add more in the future.</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Newline">Wikipedia Newline article</a>
 *
 * @since $next-version$
 */
public class FormattingStyle {
  private final String newline;
  private final String indent;

  static public final FormattingStyle DEFAULT =
      new FormattingStyle("\n", "  ");

  private FormattingStyle(String newline, String indent) {
    Objects.requireNonNull(newline, "newline == null");
    Objects.requireNonNull(indent, "indent == null");
    if (!newline.matches("[\r\n]*")) {
      throw new IllegalArgumentException(
          "Only combinations of \\n and \\r are allowed in newline.");
    }
    if (!indent.matches("[ \t]*")) {
      throw new IllegalArgumentException(
          "Only combinations of spaces and tabs allowed in indent.");
    }
    this.newline = newline;
    this.indent = indent;
  }

  /**
   * Creates a {@link FormattingStyle} with the specified newline setting.
   *
   * <p>It can be used to accommodate certain OS convention, for example
   * hardcode {@code "\r"} for Linux and macos, {@code "\r\n"} for Windows, or
   * call {@link java.lang.System#lineSeparator()} to match the current OS.</p>
   *
   * <p>Only combinations of {@code \n} and {@code \r} are allowed.</p>
   *
   * @param newline the string value that will be used as newline.
   * @return a newly created {@link FormattingStyle}
   */
  public FormattingStyle withNewline(String newline) {
    return new FormattingStyle(newline, this.indent);
  }

  /**
   * Creates a {@link FormattingStyle} with the specified indent string.
   *
   * <p>Only combinations of spaces and tabs allowed in indent.</p>
   *
   * @param indent the string value that will be used as indent.
   * @return a newly created {@link FormattingStyle}
   */
  public FormattingStyle withIndent(String indent) {
    return new FormattingStyle(this.newline, indent);
  }

  /**
   * The string value that will be used as a newline.
   *
   * @return the newline value.
   */
  public String getNewline() {
    return this.newline;
  }

  /**
   * The string value that will be used as indent.
   *
   * @return the indent value.
   */
  public String getIndent() {
    return this.indent;
  }
}
