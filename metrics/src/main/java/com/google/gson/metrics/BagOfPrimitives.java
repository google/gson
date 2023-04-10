/*
 * Copyright (C) 2011 Google Inc.
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
package com.google.gson.metrics;

/**
 * Class with a bunch of primitive fields
 *
 * @author Inderjeet Singh
 */
public class BagOfPrimitives {
  public static final long DEFAULT_VALUE = 0;
  private static final int HASH_PRIME = 31;
  private static final int TRUE_HASH = 1231;
  private static final int FALSE_HASH = 1237;

  public long longValue;
  public int intValue;
  public boolean booleanValue;
  public String stringValue;


  /**
   * Constructs a BagOfPrimitives object with the default values.
   */
  public BagOfPrimitives() {
    this(DEFAULT_VALUE, 0, false, "");
  }
  /**
   * Constructs a BagOfPrimitives object with the specified values.
   * 
   * @param longValue the value of the long field.
   * @param intValue the value of the int field.
   * @param booleanValue the value of the boolean field.
   * @param stringValue the value of the String field.
   */
  public BagOfPrimitives(long longValue, int intValue, boolean booleanValue, String stringValue) {
    this.longValue = longValue;
    this.intValue = intValue;
    this.booleanValue = booleanValue;
    this.stringValue = stringValue;
  }

  /**
   * Returns the value of the int field.
   * 
   * @return the value of the int field.
   */
  public int getIntValue() {
    return intValue;
  }

  /**
   * Returns the expected JSON representation of this object.
   * 
   * @return the expected JSON representation of this object.
   */
  public String getExpectedJson() {
    return "{"
        + "\"longValue\":" + longValue + ","
        + "\"intValue\":" + intValue + ","
        + "\"booleanValue\":" + booleanValue + ","
        + "\"stringValue\":\"" + stringValue + "\""
        + "}";
  }

  /**
   * Returns the hash code for this object.
   * 
   * @return the hash code for this object.
   */
  @Override
  public int hashCode() {
    final int prime = HASH_PRIME;
    int result = 1;
    result = prime * result + (booleanValue ? TRUE_HASH : FALSE_HASH);
    result = prime * result + intValue;
    result = prime * result + (int) (longValue ^ (longValue >>> 32));
    result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
    return result;
  }
  /**
   * Compares this object to the specified object for equality.
   * 
   * @param obj the object to compare to.
   * @return true if the objects are equal, false otherwise.
   */

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    BagOfPrimitives other = (BagOfPrimitives) obj;
    if (booleanValue != other.booleanValue) return false;
    if (intValue != other.intValue) return false;
    if (longValue != other.longValue) return false;
    if (stringValue == null) {
      return other.stringValue == null;
    } else {
      return stringValue.equals(other.stringValue);
    }
  }

  @Override
  public String toString() {
    return String.format("(longValue=%d,intValue=%d,booleanValue=%b,stringValue=%s)",
        longValue, intValue, booleanValue, stringValue);
  }
}
