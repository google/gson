package com.google.gson.internal.bind;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/** Functional tests are in {@link com.google.gson.functional.JavaTimeTest}. */
public class JavaTimeTypeAdaptersTest {
  @Test
  public void testJavaTimePackage() {
    assertThat(JavaTimeTypeAdapters.javaTimePackage()).isEqualTo("java.time.");
  }

  @Test
  public void testGetFactory() {
    assertThat(new JavaTimeTypeAdapters().get()).isNotNull();
  }
}
