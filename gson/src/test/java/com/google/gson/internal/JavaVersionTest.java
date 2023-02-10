/*
 * Copyright (C) 2017 The Gson authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/**
 * Unit and functional tests for {@link JavaVersion}
 *
 * @author Inderjeet Singh
 */
public class JavaVersionTest {
  // Borrowed some of test strings from https://github.com/prestodb/presto/blob/master/presto-main/src/test/java/com/facebook/presto/server/TestJavaVersion.java

  @Test
  public void testGetMajorJavaVersion() {
    assertThat(JavaVersion.getMajorJavaVersion() >= 7).isTrue(); // Gson currently requires at least Java 7
  }

  @Test
  public void testJava6() {
    assertThat(JavaVersion.getMajorJavaVersion("1.6.0")).isEqualTo(6); // http://www.oracle.com/technetwork/java/javase/version-6-141920.html
  }

  @Test
  public void testJava7() {
    assertThat(JavaVersion.getMajorJavaVersion("1.7.0")).isEqualTo(7); // http://www.oracle.com/technetwork/java/javase/jdk7-naming-418744.html
  }

  @Test
  public void testJava8() {
    assertThat(JavaVersion.getMajorJavaVersion("1.8")).isEqualTo(8);
    assertThat(JavaVersion.getMajorJavaVersion("1.8.0")).isEqualTo(8);
    assertThat(JavaVersion.getMajorJavaVersion("1.8.0_131")).isEqualTo(8);
    assertThat(JavaVersion.getMajorJavaVersion("1.8.0_60-ea")).isEqualTo(8);
    assertThat(JavaVersion.getMajorJavaVersion("1.8.0_111-internal")).isEqualTo(8);

    // openjdk8 per https://github.com/AdoptOpenJDK/openjdk-build/issues/93
    assertThat(JavaVersion.getMajorJavaVersion("1.8.0-internal")).isEqualTo(8);
    assertThat(JavaVersion.getMajorJavaVersion("1.8.0_131-adoptopenjdk")).isEqualTo(8);
  }

  @Test
  public void testJava9() {
    // Legacy style
    assertThat(JavaVersion.getMajorJavaVersion("9.0.4")).isEqualTo(9); // Oracle JDK 9
    assertThat(JavaVersion.getMajorJavaVersion("9-Debian")).isEqualTo(9); // Debian as reported in https://github.com/google/gson/issues/1310
    // New style
    assertThat(JavaVersion.getMajorJavaVersion("9-ea+19")).isEqualTo(9);
    assertThat(JavaVersion.getMajorJavaVersion("9+100")).isEqualTo(9);
    assertThat(JavaVersion.getMajorJavaVersion("9.0.1+20")).isEqualTo(9);
    assertThat(JavaVersion.getMajorJavaVersion("9.1.1+20")).isEqualTo(9);
  }

  @Test
  public void testJava10() {
    assertThat(JavaVersion.getMajorJavaVersion("10.0.1")).isEqualTo(10); // Oracle JDK 10.0.1
  }

  @Test
  public void testUnknownVersionFormat() {
    assertThat(JavaVersion.getMajorJavaVersion("Java9")).isEqualTo(6); // unknown format
  }
}
