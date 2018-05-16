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

/**
 * Utility to check the major Java version of the current JVM.
 */
public final class JavaVersion {
  // Oracle defines naming conventions at http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html
  // However, many alternate implementations differ. For example, Debian used 9-debian as the version string

  private static final int majorJavaVersion = determineMajorJavaVersion();

  private static int determineMajorJavaVersion() {
    int version = (int) Double.parseDouble(System.getProperty("java.class.version"));
    switch (version) {
      case 50: return 6;
      case 51: return 7;
      case 52: return 8;
      case 53: return 9;
      case 54: return 10;
      case 55: return 11;
      default: return 6; // <- return minimum supported version
    }
  }

  /**
   * @return the major Java version, i.e. '8' for Java 1.8, '9' for Java 9 etc.
   */
  public static int getMajorJavaVersion() {
    return majorJavaVersion;
  }

  /**
   * @return {@code true} if the application is running on Java 9 or later; and {@code false} otherwise.
   */
  public static boolean isJava9OrLater() {
    return majorJavaVersion >= 9;
  }
}
