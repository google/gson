/*
 * Copyright (C) 2008 Google Inc.
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

import junit.framework.TestCase;

/**
 * Performs some unit testing for the {@link Escaper} class.
 *
 * @author Joel Leitch
 */
public class EscaperTest extends TestCase {

  public void testNoSpecialCharacters() {
    String value = "Testing123";
    String escapedString = Escaper.escapeJsonString(value);
    assertEquals(value, escapedString);
  }

  public void testNewlineEscaping() throws Exception {
    String containsNewline = "123\n456";
    String escapedString = Escaper.escapeJsonString(containsNewline);
    assertEquals("123\\n456", escapedString);
  }

  public void testCarrageReturnEscaping() throws Exception {
    String containsCarrageReturn = "123\r456";
    String escapedString = Escaper.escapeJsonString(containsCarrageReturn);
    assertEquals("123\\r456", escapedString);
  }

  public void testTabEscaping() throws Exception {
    String containsTab = "123\t456";
    String escapedString = Escaper.escapeJsonString(containsTab);
    assertEquals("123\\t456", escapedString);
  }

  public void testQuoteEscaping() throws Exception {
    String containsQuote = "123\"456";
    String escapedString = Escaper.escapeJsonString(containsQuote);
    assertEquals("123\\\"456", escapedString);
  }

  public void testEqualsEscaping() throws Exception {
    String containsEquals = "123=456";
    int index = containsEquals.indexOf('=');
    String unicodeValue = convertToUnicodeString(Character.codePointAt(containsEquals, index));
    String escapedString = Escaper.escapeJsonString(containsEquals);
    assertEquals("123" + unicodeValue + "456", escapedString);
  }

  public void testGreaterThanAndLessThanEscaping() throws Exception {
    String containsLtGt = "123>456<";
    int gtIndex = containsLtGt.indexOf('>');
    int ltIndex = containsLtGt.indexOf('<');
    String gtAsUnicode = convertToUnicodeString(Character.codePointAt(containsLtGt, gtIndex));
    String ltAsUnicode = convertToUnicodeString(Character.codePointAt(containsLtGt, ltIndex));

    String escapedString = Escaper.escapeJsonString(containsLtGt);
    assertEquals("123" + gtAsUnicode + "456" + ltAsUnicode, escapedString);
  }

  public void testAmpersandEscaping() throws Exception {
    String containsAmp = "123&456";
    int ampIndex = containsAmp.indexOf('&');
    String ampAsUnicode = convertToUnicodeString(Character.codePointAt(containsAmp, ampIndex));

    String escapedString = Escaper.escapeJsonString(containsAmp);
    assertEquals("123" + ampAsUnicode + "456", escapedString);
  }

  public void testSlashEscaping() throws Exception {
    String containsSlash = "123\\456";
    String escapedString = Escaper.escapeJsonString(containsSlash);
    assertEquals("123\\\\456", escapedString);
  }

  public void testSingleQuoteNotEscaped() throws Exception {
    String containsSingleQuote = "123'456";
    String escapedString = Escaper.escapeJsonString(containsSingleQuote);
    assertEquals(containsSingleQuote, escapedString);
  }

  private String convertToUnicodeString(int codepoint) {
    String hexValue = Integer.toHexString(codepoint);
    StringBuilder sb = new StringBuilder("\\u");
    for (int i = 0; i < 4 - hexValue.length(); i++) {
      sb.append(0);
    }
    sb.append(hexValue);

    return sb.toString().toLowerCase();
  }
}
