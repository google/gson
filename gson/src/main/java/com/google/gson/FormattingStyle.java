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
 * An enumeration that defines the kind of newline to use for serialization.
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
          "Only \\n and \\r are allowed in newline.");
    }
    if (!indent.matches("[ \t]*")) {
      throw new IllegalArgumentException("Only spaces and tabs allowed in indent.");
    }
    this.newline = newline;
    this.indent = indent;
  }

  public FormattingStyle withNewline(String newline) {
    return new FormattingStyle(newline, this.indent);
  }

  public FormattingStyle withIndent(String indent) {
    return new FormattingStyle(this.newline, indent);
  }

  /**
   * The string value that will be used as newline.
   *
   * @return the newline value.
  */
  public String getNewline() {
    return this.newline;
  }

  public String getIndent() {
    return this.indent;
  }
}
