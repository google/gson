package com.example;

import static com.example.TestExecutor.same;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class DefaultConstructorMain {
  static class TestClass {
    @SerializedName("s")
    public String s;
  }

  // R8 rule for this class still removes no-args constructor, but doesn't make class abstract
  static class TestClassNotAbstract {
    @SerializedName("s")
    public String s;
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
}
