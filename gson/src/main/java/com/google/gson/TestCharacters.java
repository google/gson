package com.google.gson;

public class TestCharacters {

  public static void main(String[] args) {
    System.out.println("\\b: " + Character.codePointAt("\b", 0));
    System.out.println("\\r: " + Character.codePointAt("\r", 0));
    System.out.println("\\n: " + Character.codePointAt("\n", 0));
    System.out.println("\\t: " + Character.codePointAt("\t", 0));
    System.out.println("': " + Character.codePointAt("'", 0));
  }
}
