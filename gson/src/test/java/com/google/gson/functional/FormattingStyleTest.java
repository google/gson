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
package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Functional tests for formatting styles.
 *
 * @author Mihai Nita
 */
@RunWith(JUnit4.class)
public class FormattingStyleTest {

  // Create new input object every time to protect against tests accidentally modifying input
  private static Map<String, List<Integer>> createInput() {
    Map<String, List<Integer>> map = new LinkedHashMap<>();
    map.put("a", Arrays.asList(1, 2));
    return map;
  }

  private static String buildExpected(String newline, String indent, boolean spaceAfterSeparators) {
    String expected =
        "{<EOL><INDENT>\"a\":<COLON_SPACE>[<EOL><INDENT><INDENT>1,<COMMA_SPACE><EOL><INDENT><INDENT>2<EOL><INDENT>]<EOL>}";
    String commaSpace = spaceAfterSeparators && newline.isEmpty() ? " " : "";
    return expected
        .replace("<EOL>", newline)
        .replace("<INDENT>", indent)
        .replace("<COLON_SPACE>", spaceAfterSeparators ? " " : "")
        .replace("<COMMA_SPACE>", commaSpace);
  }

  // Various valid strings that can be used for newline and indent
  private static final String[] TEST_NEWLINES = {
    "", "\r", "\n", "\r\n", "\n\r\r\n", System.lineSeparator()
  };
  private static final String[] TEST_INDENTS = {"", "  ", "    ", "\t", " \t \t"};

  @Test
  public void testDefault() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(createInput());
    assertThat(json).isEqualTo(buildExpected("\n", "  ", true));
  }

  @Test
  public void testVariousCombinationsParse() {
    // Mixing various indent and newline styles in the same string, to be parsed.
    String jsonStringMix = "{\r\t'a':\r\n[        1,2\t]\n}";
    TypeToken<Map<String, List<Integer>>> inputType = new TypeToken<>() {};

    Map<String, List<Integer>> actualParsed;
    // Test all that all combinations of newline can be parsed and generate the same INPUT.
    for (String indent : TEST_INDENTS) {
      for (String newline : TEST_NEWLINES) {
        FormattingStyle style = FormattingStyle.PRETTY.withNewline(newline).withIndent(indent);
        Gson gson = new GsonBuilder().setFormattingStyle(style).create();

        String toParse = buildExpected(newline, indent, true);
        actualParsed = gson.fromJson(toParse, inputType);
        assertThat(actualParsed).isEqualTo(createInput());

        // Parse the mixed string with the gson parsers configured with various newline / indents.
        actualParsed = gson.fromJson(jsonStringMix, inputType);
        assertThat(actualParsed).isEqualTo(createInput());
      }
    }
  }

  private static String toJson(Object obj, FormattingStyle style) {
    return new GsonBuilder().setFormattingStyle(style).create().toJson(obj);
  }

  @Test
  public void testFormatCompact() {
    String json = toJson(createInput(), FormattingStyle.COMPACT);
    String expectedJson = buildExpected("", "", false);
    assertThat(json).isEqualTo(expectedJson);
    // Sanity check to verify that `buildExpected` works correctly
    assertThat(json).isEqualTo("{\"a\":[1,2]}");
  }

  @Test
  public void testFormatPretty() {
    String json = toJson(createInput(), FormattingStyle.PRETTY);
    String expectedJson = buildExpected("\n", "  ", true);
    assertThat(json).isEqualTo(expectedJson);
    // Sanity check to verify that `buildExpected` works correctly
    assertThat(json)
        .isEqualTo(
            "{\n" //
                + "  \"a\": [\n" //
                + "    1,\n" //
                + "    2\n" //
                + "  ]\n" //
                + "}");
  }

  @Test
  public void testFormatPrettySingleLine() {
    FormattingStyle style = FormattingStyle.COMPACT.withSpaceAfterSeparators(true);
    String json = toJson(createInput(), style);
    String expectedJson = buildExpected("", "", true);
    assertThat(json).isEqualTo(expectedJson);
    // Sanity check to verify that `buildExpected` works correctly
    assertThat(json).isEqualTo("{\"a\": [1, 2]}");
  }

  @Test
  public void testFormat() {
    for (String newline : TEST_NEWLINES) {
      for (String indent : TEST_INDENTS) {
        for (boolean spaceAfterSeparators : new boolean[] {true, false}) {
          FormattingStyle style =
              FormattingStyle.COMPACT
                  .withNewline(newline)
                  .withIndent(indent)
                  .withSpaceAfterSeparators(spaceAfterSeparators);

          String json = toJson(createInput(), style);
          String expectedJson = buildExpected(newline, indent, spaceAfterSeparators);
          assertThat(json).isEqualTo(expectedJson);
        }
      }
    }
  }

  /**
   * Should be able to convert {@link FormattingStyle#COMPACT} to {@link FormattingStyle#PRETTY}
   * using the {@code withX} methods.
   */
  @Test
  public void testCompactToPretty() {
    FormattingStyle style =
        FormattingStyle.COMPACT.withNewline("\n").withIndent("  ").withSpaceAfterSeparators(true);

    String json = toJson(createInput(), style);
    String expectedJson = toJson(createInput(), FormattingStyle.PRETTY);
    assertThat(json).isEqualTo(expectedJson);
  }

  /**
   * Should be able to convert {@link FormattingStyle#PRETTY} to {@link FormattingStyle#COMPACT}
   * using the {@code withX} methods.
   */
  @Test
  public void testPrettyToCompact() {
    FormattingStyle style =
        FormattingStyle.PRETTY.withNewline("").withIndent("").withSpaceAfterSeparators(false);

    String json = toJson(createInput(), style);
    String expectedJson = toJson(createInput(), FormattingStyle.COMPACT);
    assertThat(json).isEqualTo(expectedJson);
  }

  @Test
  public void testStyleValidations() {
    // TBD if we want to accept \u2028 and \u2029. For now we don't because JSON specification
    // does not consider them to be newlines
    var e =
        assertThrows(
            IllegalArgumentException.class, () -> FormattingStyle.PRETTY.withNewline("\u2028"));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo("Only combinations of \\n and \\r are allowed in newline.");

    e =
        assertThrows(
            IllegalArgumentException.class, () -> FormattingStyle.PRETTY.withNewline("NL"));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo("Only combinations of \\n and \\r are allowed in newline.");

    e = assertThrows(IllegalArgumentException.class, () -> FormattingStyle.PRETTY.withIndent("\f"));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo("Only combinations of spaces and tabs are allowed in indent.");
  }
}
