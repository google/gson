package com.google.gson;

final class StringUnmarshaller {
  static String unmarshall(String str) {
    str = str.substring(1, str.length()-1);
    
    int len = str.length();    
    StringBuilder sb = new StringBuilder(len);    
    int i = 0;    
    while (i < len) {
      char c = str.charAt(i);      
      ++i;
      if (c == '\\') {
        char c1 = str.charAt(i);
        ++i;
        if (c1 == 'u') { // This is a unicode escape
          // TODO(inder): Handle the case where code points are of size bigger than 4 
          int codePoint = getCodePoint(str, i);
          sb.appendCodePoint(codePoint);
          i += 4;           
        } else {
          char escapedChar = getEscapedChar(str, c1);
          sb.append(escapedChar);
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static int getCodePoint(String str, int i) {
//    int codePoint = Character.codePointAt(str, i);
    String s = str.substring(i, i+4);
    int codePoint = Integer.parseInt(s, 16);
    return codePoint;
  }

  private static char getEscapedChar(String str, char c) {
    char ch;
    switch (c) {
      case 'n':
        ch = '\n';
        break;
      case 'b':
        ch = '\b';
        break;
      case 'f':
        ch = '\f';
        break;
      case 't':
        ch = '\t';
        break;
      case 'r':
        ch = '\r';
        break;
      case '\"':
        ch = '\"';
        break;
      case '\'':
        ch = '\'';
        break;
      case '\\':
        ch = '\\';
        break;
      case '/':
        ch = '/';
        break;
      default:
        throw new IllegalStateException("Unexpected character: " + c + " in " + str);
    }
    return ch;
  }
}
