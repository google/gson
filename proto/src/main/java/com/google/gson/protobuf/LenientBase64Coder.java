// Copyright 2003-2010 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
// www.source-code.biz, www.inventec.ch/chdh
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
//  EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
//  LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
//  GPL, GNU General Public License, V2 or later, http://www.gnu.org/licenses/gpl.html
//  AL, Apache License, V2.0 or later, http://www.apache.org/licenses
//  BSD, BSD License, http://www.opensource.org/licenses/bsd-license.php
//  MIT, MIT License, http://www.opensource.org/licenses/MIT
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.

package com.google.gson.protobuf;

/**
 * A Base64 encoder/decoder.
 *
 * <p> This class is used to encode and decode data in Base64 format as described in RFC 1521.
 *
 * <p> Project home page: <a href="http://www.source-code.biz/base64coder/java/">www.source-code.biz/base64coder/java</a><br>
 * Author: Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland<br> Multi-licensed: EPL
 * / LGPL / GPL / AL / BSD / MIT.
 *
 * Modified to accept URL encoding, some apis removed from the original.
 */
public class LenientBase64Coder {

  // Mapping table from 6-bit nibbles to Base64 characters.
  private static char[] map1 = new char[64];

  static {
    int i = 0;
    for (char c = 'A'; c <= 'Z'; c++)
      map1[i++] = c;
    for (char c = 'a'; c <= 'z'; c++)
      map1[i++] = c;
    for (char c = '0'; c <= '9'; c++)
      map1[i++] = c;
    map1[i++] = '+';
    map1[i++] = '/';
  }

  // Mapping table from Base64 characters to 6-bit nibbles.
  private static byte[] map2 = new byte[128];

  static {
    for (int i = 0; i < map2.length; i++)
      map2[i] = -1;
    for (int i = 0; i < 64; i++)
      map2[map1[i]] = (byte) i;
    // support URL safe variants
    map2['-'] = map2['+'];
    map2['_'] = map2['/'];
  }

  /**
   * Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
   *
   * @param in An array containing the data bytes to be encoded.
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode(byte[] in) {
    return encode(in, 0, in.length);
  }

  /**
   * Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
   *
   * @param in An array containing the data bytes to be encoded.
   * @param iLen Number of bytes to process in <code>in</code>.
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode(byte[] in, int iLen) {
    return encode(in, 0, iLen);
  }

  /**
   * Encodes a byte array into Base64 format. No blanks or line breaks are inserted in the output.
   *
   * @param in An array containing the data bytes to be encoded.
   * @param iOff Offset of the first byte in <code>in</code> to be processed.
   * @param iLen Number of bytes to process in <code>in</code>, starting at <code>iOff</code>.
   * @return A character array containing the Base64 encoded data.
   */
  public static char[] encode(byte[] in, int iOff, int iLen) {
    int oDataLen = (iLen * 4 + 2) / 3; // output length without padding
    int oLen = ((iLen + 2) / 3) * 4; // output length including padding
    char[] out = new char[oLen];
    int ip = iOff;
    int iEnd = iOff + iLen;
    int op = 0;
    while (ip < iEnd) {
      int i0 = in[ip++] & 0xff;
      int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
      int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
      int o0 = i0 >>> 2;
      int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
      int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
      int o3 = i2 & 0x3F;
      out[op++] = map1[o0];
      out[op++] = map1[o1];
      out[op] = op < oDataLen ? map1[o2] : '=';
      op++;
      out[op] = op < oDataLen ? map1[o3] : '=';
      op++;
    }
    return out;
  }

  /**
   * Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64
   * encoded input data.
   *
   * @param s A Base64 String to be decoded.
   * @return An array containing the decoded data bytes.
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decode(String s) {
    return decode(s.toCharArray());
  }

  /**
   * Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64
   * encoded input data.
   *
   * @param in A character array containing the Base64 encoded data.
   * @return An array containing the decoded data bytes.
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decode(char[] in) {
    return decode(in, 0, in.length);
  }

  /**
   * Decodes a byte array from Base64 format. No blanks or line breaks are allowed within the Base64
   * encoded input data.
   *
   * @param in A character array containing the Base64 encoded data.
   * @param iOff Offset of the first character in <code>in</code> to be processed.
   * @param iLen Number of characters to process in <code>in</code>, starting at <code>iOff</code>.
   * @return An array containing the decoded data bytes.
   * @throws IllegalArgumentException If the input is not valid Base64 encoded data.
   */
  public static byte[] decode(char[] in, int iOff, int iLen) {
    while (iLen > 0 && in[iOff + iLen - 1] == '=') {
      iLen--;
    }
    if (iLen % 4 == 1) {
      throw new IllegalArgumentException("Invalid base64 encoded length");
    }
    int oLen = (iLen * 3) / 4;
    byte[] out = new byte[oLen];
    int ip = iOff;
    int iEnd = iOff + iLen;
    int op = 0;
    while (ip < iEnd) {
      int i0 = in[ip++];
      int i1 = in[ip++];
      int i2 = ip < iEnd ? in[ip++] : 'A';
      int i3 = ip < iEnd ? in[ip++] : 'A';
      if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
        throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
      }
      int b0 = map2[i0];
      int b1 = map2[i1];
      int b2 = map2[i2];
      int b3 = map2[i3];
      if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
        throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
      }
      int o0 = (b0 << 2) | (b1 >>> 4);
      int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
      int o2 = ((b2 & 3) << 6) | b3;
      out[op++] = (byte) o0;
      if (op < oLen) {
        out[op++] = (byte) o1;
      }
      if (op < oLen) {
        out[op++] = (byte) o2;
      }
    }
    return out;
  }

  // Dummy constructor.
  private LenientBase64Coder() {
  }
} // end class LenientBase64Coder
