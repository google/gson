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

  private Escaper escapeHtmlChar;
  private Escaper noEscapeHtmlChar;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    escapeHtmlChar = new Escaper(true);
    noEscapeHtmlChar = new Escaper(false);
  }

  public void testNoSpecialCharacters() {
    String value = "Testing123";
    String escapedString = escapeHtmlChar.escapeJsonString(value);
    assertEquals(value, escapedString);
  }

  public void testNewlineEscaping() throws Exception {
    String containsNewline = "123\n456";
    String escapedString = escapeHtmlChar.escapeJsonString(containsNewline);
    assertEquals("123\\n456", escapedString);
  }

  public void testCarrageReturnEscaping() throws Exception {
    String containsCarrageReturn = "123\r456";
    String escapedString = escapeHtmlChar.escapeJsonString(containsCarrageReturn);
    assertEquals("123\\r456", escapedString);
  }

  public void testTabEscaping() throws Exception {
    String containsTab = "123\t456";
    String escapedString = escapeHtmlChar.escapeJsonString(containsTab);
    assertEquals("123\\t456", escapedString);
  }

  public void testDoubleQuoteEscaping() throws Exception {
    String containsQuote = "123\"456";
    String escapedString = escapeHtmlChar.escapeJsonString(containsQuote);
    assertEquals("123\\\"456", escapedString);
  }
  
  public void testSingleQuoteEscaping() throws Exception {
    String containsQuote = "123'456";
    String escapedString = escapeHtmlChar.escapeJsonString(containsQuote);
    assertEquals("123\\u0027456", escapedString);
  }

  public void testLineSeparatorEscaping() throws Exception {
    String src = "123\u2028 456";
    String escapedString = escapeHtmlChar.escapeJsonString(src);
    assertEquals("123\\u2028 456", escapedString);
  }

  public void testParagraphSeparatorEscaping() throws Exception {
    String src = "123\u2029 456";
    String escapedString = escapeHtmlChar.escapeJsonString(src);
    assertEquals("123\\u2029 456", escapedString);
  }

  public void testControlCharBlockEscaping() throws Exception {
    for (char c = '\u007f'; c <= '\u009f'; ++c) {
      String src = "123 " + c + " 456";
      String escapedString = escapeHtmlChar.escapeJsonString(src);
      assertFalse(src.equals(escapedString));
    }
  }

  public void testEqualsEscaping() throws Exception {
    String containsEquals = "123=456";
    int index = containsEquals.indexOf('=');
    String unicodeValue = convertToUnicodeString(Character.codePointAt(containsEquals, index));
    String escapedString = escapeHtmlChar.escapeJsonString(containsEquals);
    assertEquals("123" + unicodeValue + "456", escapedString);
    
    escapedString = noEscapeHtmlChar.escapeJsonString(containsEquals);
    assertEquals(containsEquals, escapedString);
  }

  public void testGreaterThanAndLessThanEscaping() throws Exception {
    String containsLtGt = "123>456<";
    int gtIndex = containsLtGt.indexOf('>');
    int ltIndex = containsLtGt.indexOf('<');
    String gtAsUnicode = convertToUnicodeString(Character.codePointAt(containsLtGt, gtIndex));
    String ltAsUnicode = convertToUnicodeString(Character.codePointAt(containsLtGt, ltIndex));

    String escapedString = escapeHtmlChar.escapeJsonString(containsLtGt);
    assertEquals("123" + gtAsUnicode + "456" + ltAsUnicode, escapedString);
    
    escapedString = noEscapeHtmlChar.escapeJsonString(containsLtGt);
    assertEquals(containsLtGt, escapedString);
  }

  public void testAmpersandEscaping() throws Exception {
    String containsAmp = "123&456";
    int ampIndex = containsAmp.indexOf('&');
    String ampAsUnicode = convertToUnicodeString(Character.codePointAt(containsAmp, ampIndex));

    String escapedString = escapeHtmlChar.escapeJsonString(containsAmp);
    assertEquals("123" + ampAsUnicode + "456", escapedString);
    
    escapedString = noEscapeHtmlChar.escapeJsonString(containsAmp);
    assertEquals(containsAmp, escapedString);

    char ampCharAsUnicode = '\u0026';
    String containsAmpUnicode = "123" + ampCharAsUnicode + "456";
    escapedString = escapeHtmlChar.escapeJsonString(containsAmpUnicode);
    assertEquals("123" + ampAsUnicode + "456", escapedString);

    escapedString = noEscapeHtmlChar.escapeJsonString(containsAmpUnicode);
    assertEquals(containsAmp, escapedString);
  }

  public void testSlashEscaping() throws Exception {
    String containsSlash = "123\\456";
    String escapedString = escapeHtmlChar.escapeJsonString(containsSlash);
    assertEquals("123\\\\456", escapedString);
  }

  public void testSingleQuoteNotEscaped() throws Exception {
    String containsSingleQuote = "123'456";
    String escapedString = noEscapeHtmlChar.escapeJsonString(containsSingleQuote);
    assertEquals(containsSingleQuote, escapedString);
  }

  public void testRequiredEscapingUnicodeCharacter() throws Exception {
    char unicodeChar = '\u2028';
    String unicodeString = "Testing" + unicodeChar;

    String escapedString = escapeHtmlChar.escapeJsonString(unicodeString);
    assertFalse(unicodeString.equals(escapedString));
    assertEquals("Testing\\u2028", escapedString);
  }

  public void testUnicodeCharacterStringNoEscaping() throws Exception {
    String unicodeString = "\u0065\u0066";

    String escapedString = escapeHtmlChar.escapeJsonString(unicodeString);
    assertEquals(unicodeString, escapedString);
  }

  /*
  public void testChineseCharacterEscaping() throws Exception {
    String unicodeString = "\u597d\u597d\u597d";
    String chineseString = "好好好";
    assertEquals(unicodeString, chineseString);

    String expectedEscapedString = "\\u597d\\u597d\\u597d";
    String escapedString = Escaper.escapeJsonString(chineseString);
    assertEquals(expectedEscapedString, escapedString);
  }
   */

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
