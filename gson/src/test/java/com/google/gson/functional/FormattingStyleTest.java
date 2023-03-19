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
import static org.junit.Assert.fail;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

  private static final String[] INPUT = {"v1", "v2"};
  private static final String EXPECTED = "[<EOL><INDENT>\"v1\",<EOL><INDENT>\"v2\"<EOL>]";
  private static final String EXPECTED_OS = buildExpected(System.lineSeparator(), "  ");
  private static final String EXPECTED_CR = buildExpected("\r", "  ");
  private static final String EXPECTED_LF = buildExpected("\n", "  ");
  private static final String EXPECTED_CRLF = buildExpected("\r\n", "  ");

  // Various valid strings that can be used for newline and indent
  private static final String[] TEST_NEWLINES = {
    "", "\r", "\n", "\r\n", "\n\r\r\n", System.lineSeparator()
  };
  private static final String[] TEST_INDENTS = {
    "", "  ", "    ", "        ", "\t", " \t \t"
  };

  @Test
  public void testDefault() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(INPUT);
    // Make sure the default uses LF, like before.
    assertThat(json).isEqualTo(EXPECTED_LF);
  }

  @Test
  public void testNewlineCrLf() {
    FormattingStyle style = FormattingStyle.DEFAULT.withNewline("\r\n");
    Gson gson = new GsonBuilder().setPrettyPrinting(style).create();
    String json = gson.toJson(INPUT);
    assertThat(json).isEqualTo(EXPECTED_CRLF);
  }

  @Test
  public void testNewlineLf() {
    FormattingStyle style = FormattingStyle.DEFAULT.withNewline("\n");
    Gson gson = new GsonBuilder().setPrettyPrinting(style).create();
    String json = gson.toJson(INPUT);
    assertThat(json).isEqualTo(EXPECTED_LF);
  }

  @Test
  public void testNewlineCr() {
    FormattingStyle style = FormattingStyle.DEFAULT.withNewline("\r");
    Gson gson = new GsonBuilder().setPrettyPrinting(style).create();
    String json = gson.toJson(INPUT);
    assertThat(json).isEqualTo(EXPECTED_CR);
  }

  @Test
  public void testNewlineOs() {
    FormattingStyle style = FormattingStyle.DEFAULT.withNewline(System.lineSeparator());
    Gson gson = new GsonBuilder().setPrettyPrinting(style).create();
    String json = gson.toJson(INPUT);
    assertThat(json).isEqualTo(EXPECTED_OS);
  }

  @Test
  public void testVariousCombinationsToString() {
    for (String indent : TEST_INDENTS) {
      for (String newline : TEST_NEWLINES) {
        FormattingStyle style = FormattingStyle.DEFAULT.withNewline(newline).withIndent(indent);
        Gson gson = new GsonBuilder().setPrettyPrinting(style).create();
        String json = gson.toJson(INPUT);
        assertThat(json).isEqualTo(buildExpected(newline, indent));
      }
    }
  }

  @Test
  public void testVariousCombinationsParse() {
    // Mixing various indent and newline styles in the same string, to be parsed.
    String jsonStringMix = "[\r\t'v1',\r\n        'v2'\n]";

    String[] actualParsed;
    // Test all that all combinations of newline can be parsed and generate the same INPUT.
    for (String indent : TEST_INDENTS) {
      for (String newline : TEST_NEWLINES) {
        FormattingStyle style = FormattingStyle.DEFAULT.withNewline(newline).withIndent(indent);
        Gson gson = new GsonBuilder().setPrettyPrinting(style).create();

        String toParse = buildExpected(newline, indent);
        actualParsed = gson.fromJson(toParse, INPUT.getClass());
        assertThat(actualParsed).isEqualTo(INPUT);

        // Parse the mixed string with the gson parsers configured with various newline / indents.
        actualParsed = gson.fromJson(jsonStringMix, INPUT.getClass());
        assertThat(actualParsed).isEqualTo(INPUT);
      }
    }
  }

  @Test
  public void testStyleValidations() {
    try {
      // TBD if we want to accept \u2028 and \u2029. For now we don't because JSON specification
      // does not consider them to be newlines
      FormattingStyle.DEFAULT.withNewline("\u2028");
      fail("Gson should not accept anything but \\r and \\n for newline");
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat()
          .isEqualTo("Only combinations of \\n and \\r are allowed in newline.");
    }

    try {
      FormattingStyle.DEFAULT.withNewline("NL");
      fail("Gson should not accept anything but \\r and \\n for newline");
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat()
          .isEqualTo("Only combinations of \\n and \\r are allowed in newline.");
    }

    try {
      FormattingStyle.DEFAULT.withIndent("\f");
      fail("Gson should not accept anything but space and tab for indent");
    } catch (IllegalArgumentException expected) {
      assertThat(expected).hasMessageThat()
          .isEqualTo("Only combinations of spaces and tabs are allowed in indent.");
    }
  }

  private static String buildExpected(String newline, String indent) {
    return EXPECTED.replace("<EOL>", newline).replace("<INDENT>", indent);
  }
}
