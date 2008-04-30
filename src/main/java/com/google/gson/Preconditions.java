package com.google.gson;

final class Preconditions {
  public static void checkNotNull(Object obj) {
    checkArgument(obj != null);
  }
  
  public static void checkArgument(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("condition failed: " + condition);
    }
  }
}
