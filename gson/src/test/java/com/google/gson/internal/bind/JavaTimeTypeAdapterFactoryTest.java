package com.google.gson.internal.bind;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/** Functional tests are in {@link com.google.gson.functional.JavaTimeTest}. */
public class JavaTimeTypeAdapterFactoryTest {
  @Test
  public void testJavaTimePackage() {
    assertThat(JavaTimeTypeAdapterFactory.javaTimePackage()).isEqualTo("java.time.");
  }

  @Test
  public void testGetFactory() {
    assertThat(new JavaTimeTypeAdapterFactory().get()).isNotNull();
  }
}
