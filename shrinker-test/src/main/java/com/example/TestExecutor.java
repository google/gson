package com.example;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TestExecutor {
  /**
   * Helper method for running individual tests. In case of an exception wraps it and
   * includes the {@code name} of the test to make debugging issues with the obfuscated
   * JARs a bit easier.
   */
  public static void run(BiConsumer<String, String> outputConsumer, String name, Supplier<String> resultSupplier) {
    String result;
    try {
      result = resultSupplier.get();
    } catch (Throwable t) {
      throw new RuntimeException("Test failed: " + name, t);
    }
    outputConsumer.accept(name, result);
  }

  /**
   * Returns {@code t}, but in a way which (hopefully) prevents code shrinkers from
   * simplifying this.
   */
  public static <T> T same(T t) {
    // This is essentially `return t`, but contains some redundant code to try
    // prevent the code shrinkers from simplifying this
    return Optional.of(t)
        .map(v -> Optional.of(v).get())
        .orElseThrow(() -> new AssertionError("unreachable"));
  }
}
