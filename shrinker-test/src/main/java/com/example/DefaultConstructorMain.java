package com.example;

import static com.example.TestExecutor.same;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class DefaultConstructorMain {
  static class TestClass {
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
}
