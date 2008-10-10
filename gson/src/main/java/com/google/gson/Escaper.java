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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class that is used to perform JSON escaping so that ", <, >, etc. characters are
 * properly encoded in the JSON string representation before returning to the client code.
 *
 * <p>This class contains a single method to escape a passed in string value:
 * <pre>
 *   String jsonStringValue = "beforeQuote\"afterQuote";
 *   String escapedValue = Escaper.escapeJsonString(jsonStringValue);
 * </pre></p>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
class Escaper {

  static final char[] HEX_CHARS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  private static final Set<Character> JS_ESCAPE_CHARS;

  static {
    Set<Character> tmpSet = new HashSet<Character>();
    tmpSet.add('\u0000');
    tmpSet.add('\r');
    tmpSet.add('\n');
    tmpSet.add('\u2028');
    tmpSet.add('\u2029');
    tmpSet.add('\u0085');
    tmpSet.add('\'');
    tmpSet.add('"');
    tmpSet.add('<');
    tmpSet.add('>');
    tmpSet.add('&');
    tmpSet.add('=');
    tmpSet.add('\\');
    JS_ESCAPE_CHARS = Collections.unmodifiableSet(tmpSet);
  }

  public static String escapeJsonString(CharSequence plainText) {
    StringBuffer escapedString = new StringBuffer(plainText.length() + 20);
    try {
      escapeJsonString(plainText, escapedString);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return escapedString.toString();
  }

  private static void escapeJsonString(CharSequence plainText, StringBuffer out) throws IOException {
    int pos = 0;  // Index just past the last char in plainText written to out.
    int len = plainText.length();
     for (int charCount, i = 0; i < len; i += charCount) {
       int codePoint = Character.codePointAt(plainText, i);
       charCount = Character.charCount(codePoint);

         if (!((codePoint < 0x20 || codePoint >= 0x7f)
               || mustEscapeCharInJsString(codePoint))) {
            continue;
         }

         out.append(plainText, pos, i);
         pos = i + charCount;
         switch (codePoint) {
           case '\b':
             out.append("\\b");
             break;
           case '\t':
             out.append("\\t");
             break;
           case '\n':
             out.append("\\n");
             break;
           case '\f':
             out.append("\\f");
             break;
           case '\r':
             out.append("\\r");
             break;
           case '\\':
             out.append("\\\\");
             break;
           case '"':
             out.append('\\').append((char) codePoint);
             break;
           case '\'':
             out.append((char) codePoint);
             break;
           default:
             appendHexJavaScriptRepresentation(codePoint, out);
             break;
         }
     }
     out.append(plainText, pos, len);
  }

  private static void appendHexJavaScriptRepresentation(int codePoint, Appendable out)
      throws IOException {
    if (Character.isSupplementaryCodePoint(codePoint)) {
      // Handle supplementary unicode values which are not representable in
      // javascript.  We deal with these by escaping them as two 4B sequences
      // so that they will round-trip properly when sent from java to javascript
      // and back.
      char[] surrogates = Character.toChars(codePoint);
      appendHexJavaScriptRepresentation(surrogates[0], out);
      appendHexJavaScriptRepresentation(surrogates[1], out);
      return;
    }
    out.append("\\u")
        .append(HEX_CHARS[(codePoint >>> 12) & 0xf])
        .append(HEX_CHARS[(codePoint >>> 8) & 0xf])
        .append(HEX_CHARS[(codePoint >>> 4) & 0xf])
        .append(HEX_CHARS[codePoint & 0xf]);
  }

  private static boolean mustEscapeCharInJsString(int codepoint) {
    if (!Character.isSupplementaryCodePoint(codepoint)) {
      return JS_ESCAPE_CHARS.contains((char)codepoint);
    } else {
      return false;
    }
  }
}
