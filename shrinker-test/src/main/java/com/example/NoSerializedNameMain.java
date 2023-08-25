package com.example;

import static com.example.TestExecutor.same;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Covers cases of classes which don't use {@code @SerializedName} on their fields, and are
 * therefore not matched by the default {@code gson.pro} rules.
 */
public class NoSerializedNameMain {
  static class TestClass {
    public String s;
  }

  // R8 test rule in r8.pro for this class still removes no-args constructor, but doesn't make class abstract
  static class TestClassNotAbstract {
    public String s;
  }

  static class TestClassConstructorHasArgs {
    public String s;

    // Specify explicit constructor with args to remove implicit no-args default constructor
    public TestClassConstructorHasArgs(String s) {
      this.s = s;
    }
  }

  /**
   * Main entrypoint, called by {@code ShrinkingIT.testNoSerializedName_ConstructorHasArgs()}.
   */
  public static String runTest() {
    TestClass deserialized = new Gson().fromJson("{\"s\":\"value\"}", same(TestClass.class));
    return deserialized.s;
  }

  /**
   * Main entrypoint, called by {@code ShrinkingIT.testNoSerializedName_NoArgsConstructorNoJdkUnsafe()}.
   */
  public static String runTestNoJdkUnsafe() {
    Gson gson = new GsonBuilder().disableJdkUnsafe().create();
    TestClassNotAbstract deserialized = gson.fromJson("{\"s\": \"value\"}", same(TestClassNotAbstract.class));
    return deserialized.s;
  }

  /**
   * Main entrypoint, called by {@code ShrinkingIT.testNoSerializedName_ConstructorHasArgs()}.
   */
  public static String runTestConstructorHasArgs() {
    TestClassConstructorHasArgs deserialized = new Gson().fromJson("{\"s\":\"value\"}", same(TestClassConstructorHasArgs.class));
    return deserialized.s;
  }
}
