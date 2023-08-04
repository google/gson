package com.example;

import static com.example.TestExecutor.same;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class DefaultConstructorMain {
  static class TestClass {
    public String s;
  }

  // R8 rule for this class still removes no-args constructor, but doesn't make class abstract
  static class TestClassNotAbstract {
    public String s;
  }

  // Current Gson ProGuard rules only keep default constructor (and only then prevent R8 from
  // making class abstract); other constructors are ignored to suggest to user adding default
  // constructor instead of implicitly relying on JDK Unsafe
  static class TestClassWithoutDefaultConstructor {
    @SerializedName("s")
    public String s;

    public TestClassWithoutDefaultConstructor(String s) {
      this.s = s;
    }
  }

  /**
   * Main entrypoint, called by {@code ShrinkingIT.testDefaultConstructor()}.
   */
  public static String runTest() {
    TestClass deserialized = new Gson().fromJson("{\"s\":\"value\"}", same(TestClass.class));
    return deserialized.s;
  }

  /**
   * Main entrypoint, called by {@code ShrinkingIT.testDefaultConstructorNoJdkUnsafe()}.
   */
  public static String runTestNoJdkUnsafe() {
    Gson gson = new GsonBuilder().disableJdkUnsafe().create();
    TestClassNotAbstract deserialized = gson.fromJson("{\"s\": \"value\"}", same(TestClassNotAbstract.class));
    return deserialized.s;
  }

  /**
   * Main entrypoint, called by {@code ShrinkingIT.testNoDefaultConstructor()}.
   */
  public static String runTestNoDefaultConstructor() {
    TestClassWithoutDefaultConstructor deserialized = new Gson().fromJson("{\"s\":\"value\"}", same(TestClassWithoutDefaultConstructor.class));
    return deserialized.s;
  }
}
