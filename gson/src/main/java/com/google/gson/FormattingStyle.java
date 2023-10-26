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

import com.google.gson.stream.JsonWriter;
import java.util.Objects;

/**
 * A class used to control what the serialization output looks like.
 *
 * <p>It currently has the following configuration methods, but more methods
 * might be added in the future:
 * <ul>
 *   <li>{@link #withNewline(String)}
 *   <li>{@link #withIndent(String)}
 *   <li>{@link #withSpaceAfterSeparators(boolean)}
 * </ul>
 *
 * @see GsonBuilder#setFormattingStyle(FormattingStyle)
 * @see JsonWriter#setFormattingStyle(FormattingStyle)
 * @see <a href="https://en.wikipedia.org/wiki/Newline">Wikipedia Newline article</a>
 *
 * @since $next-version$
 */
public class FormattingStyle {
  private final String newline;
  private final String indent;
  private final boolean spaceAfterSeparators;

  /**
   * The default compact formatting style:
   * <ul>
   *   <li>no newline
   *   <li>no indent
   *   <li>no space after {@code ','} and {@code ':'}
   * </ul>
   */
  public static final FormattingStyle COMPACT = new FormattingStyle("", "", false);

  /**
   * The default pretty printing formatting style:
   * <ul>
   *   <li>{@code "\n"} as newline
   *   <li>two spaces as indent
   *   <li>a space between {@code ':'} and the subsequent value
   * </ul>
   */
  public static final FormattingStyle PRETTY =
      new FormattingStyle("\n", "  ", true);

  private FormattingStyle(String newline, String indent, boolean spaceAfterSeparators) {
    Objects.requireNonNull(newline, "newline == null");
    Objects.requireNonNull(indent, "indent == null");
    if (!newline.matches("[\r\n]*")) {
      throw new IllegalArgumentException(
          "Only combinations of \\n and \\r are allowed in newline.");
    }
    if (!indent.matches("[ \t]*")) {
      throw new IllegalArgumentException(
          "Only combinations of spaces and tabs are allowed in indent.");
    }
    this.newline = newline;
    this.indent = indent;
    this.spaceAfterSeparators = spaceAfterSeparators;
  }

  /**
   * Creates a {@link FormattingStyle} with the specified newline setting.
   *
   * <p>It can be used to accommodate certain OS convention, for example
   * hardcode {@code "\n"} for Linux and macOS, {@code "\r\n"} for Windows, or
   * call {@link java.lang.System#lineSeparator()} to match the current OS.</p>
   *
   * <p>Only combinations of {@code \n} and {@code \r} are allowed.</p>
   *
   * @param newline the string value that will be used as newline.
   * @return a newly created {@link FormattingStyle}
   */
  public FormattingStyle withNewline(String newline) {
    return new FormattingStyle(newline, this.indent, this.spaceAfterSeparators);
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
    return new FormattingStyle(this.newline, indent, this.spaceAfterSeparators);
  }

  /**
   * Creates a {@link FormattingStyle} which either uses a space after
   * the separators {@code ','} and {@code ':'} in the JSON output, or not.
   *
   * <p>This setting has no effect on the {@linkplain #withNewline(String) configured newline}.
   * If a non-empty newline is configured, it will always be added after
   * {@code ','} and no space is added after the {@code ','} in that case.</p>
   *
   * @param spaceAfterSeparators whether to output a space after {@code ','} and {@code ':'}.
   * @return a newly created {@link FormattingStyle}
   */
  public FormattingStyle withSpaceAfterSeparators(boolean spaceAfterSeparators) {
    return new FormattingStyle(this.newline, this.indent, spaceAfterSeparators);
  }

  /**
   * Returns the string value that will be used as a newline.
   *
   * @return the newline value.
   */
  public String getNewline() {
    return this.newline;
  }

  /**
   * Returns the string value that will be used as indent.
   *
   * @return the indent value.
   */
  public String getIndent() {
    return this.indent;
  }

  /**
   * Returns whether a space will be used after {@code ','} and {@code ':'}.
   */
  public boolean usesSpaceAfterSeparators() {
    return this.spaceAfterSeparators;
  }
}
