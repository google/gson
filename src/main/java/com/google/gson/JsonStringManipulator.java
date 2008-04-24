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

/**
 * A class to manipulate JSON elements in string form
 * 
 * This class is useful if you need to work with the JSON elements exactly as 
 * they appear in the original JSON string. This is required, for example, 
 * when you are signing (or verifying signature) a JSON fragment.  
 * 
 * @author Inderjeet Singh
 */
public class JsonStringManipulator {
  
  private final String json; 
  
  public JsonStringManipulator(String json) {
    this.json = json;
  }
  
  /**
   * @return The value part of the specified element from the json. Care is taken 
   * to ensure that exact content from the original JSON string is returned 
   * including any white-spaces.   
   */
  public String extractElementValueString(String element) {
    String current = json;
    int begin = 0; 
    
    begin = indexOfValueOfElement(json, begin, element);
    current = current.substring(begin);
    
    char matchingStart = current.charAt(0);
    char matchingEnd = matchingStart; 
    if (matchingStart == '{') {
      matchingEnd = '}';
    } else if (matchingStart == '[') {
      matchingEnd = ']';
    } else if (matchingStart != '\'' && matchingStart != '\"') {
      throw new RuntimeException("Invalid delimiter for a JSON value: " + matchingStart);
    }
    int end = findIndexOfMatchingChar(json, begin+1, matchingStart, matchingEnd);
        
    return json.substring(begin, end+1);
  }

  static int findIndexOfMatchingChar(String str, int index, char matchingStart, char matchingEnd) {
    boolean primitiveValue = matchingStart == matchingEnd; 
    // Primitive values are easier to handle since they will not be nested arbitrarily. 
    // We still need to worry about escaped characters though. 
    
    int count = 1; 
    char[] data = str.toCharArray();
    for (; index < data.length; ++index) {
      if (data[index] == matchingStart) {
        if (!isEscapedCharacter(data, index)) {
          if (primitiveValue) {
            count--;
          } else {
            count++;
          }
        }
      } else if (data[index] == matchingEnd) {
        if (!isEscapedCharacter(data, index)) {
          count--;
        }
      }
      if (count == 0) {
        return index;
      }
    }
    return index;
  }

  static boolean isEscapedCharacter(char[] data, int index) {
    return index == 0 ? false : data[index-1] == '\\';
  }

  /**
   * @param json the json string
   * @param index the search for element begins after the index i in data
   * @return the index where the value of the element
   */
  static int indexOfValueOfElement(String json, int index, String element) {
    while (true) {
      String current = json.substring(index);
      int subIndex = current.indexOf(element);      
      if (subIndex < 0) {
        return -1;
      }
      index += subIndex + element.length();
      
      index = findIndexOfNextNonWhiteSpaceChar(json, index);
      char c = json.charAt(index);
      if (c != ':') {
        // this is a case where element occurred as a value, so we should ignore it
        continue; 
      }
      index = findIndexOfNextNonWhiteSpaceChar(json, index+1);
      return index; 
    }
  }
  
  static int findIndexOfNextNonWhiteSpaceChar(String json, int index) {
    while (Character.isWhitespace(json.charAt(index)) && index < json.length()) {
      ++index;
    }
    return index;
  }
}