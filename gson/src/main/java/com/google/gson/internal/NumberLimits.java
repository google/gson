package com.google.gson.internal;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This class enforces limits on numbers parsed from JSON to avoid potential performance problems
 * when extremely large numbers are used.
 */
public class NumberLimits {
  private NumberLimits() {}

  private static final int MAX_NUMBER_STRING_LENGTH = 10_000;

  private static void checkNumberStringLength(String s) {
    if (s.length() > MAX_NUMBER_STRING_LENGTH) {
      throw new NumberFormatException("Number string too large: " + s.substring(0, 30) + "...");
    }
  }

  public static BigDecimal parseBigDecimal(String s) throws NumberFormatException {
    checkNumberStringLength(s);
    BigDecimal decimal = new BigDecimal(s);

    // Cast to long to avoid issues with abs when value is Integer.MIN_VALUE
    if (Math.abs((long) decimal.scale()) >= 10_000) {
      throw new NumberFormatException("Number has unsupported scale: " + s);
    }
    return decimal;
  }

  public static BigInteger parseBigInteger(String s) throws NumberFormatException {
    checkNumberStringLength(s);
    return new BigInteger(s);
  }
}
