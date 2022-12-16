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
    String javaVersion = System.getProperty("java.version");
    return getMajorJavaVersion(javaVersion);
  }

  // Visible for testing only
  static int getMajorJavaVersion(String javaVersion) {
    int version = parseDotted(javaVersion);
    if (version == -1) {
      version = extractBeginningInt(javaVersion);
    }
    if (version == -1) {
      return 6;  // Choose minimum supported JDK version as default
    }
    return version;
  }

  // Parses both legacy 1.8 style and newer 9.0.4 style 
  private static int parseDotted(String javaVersion) {
    try {
      String[] parts = javaVersion.split("[._]");
      int firstVer = Integer.parseInt(parts[0]);
      if (firstVer == 1 && parts.length > 1) {
        return Integer.parseInt(parts[1]);
      } else {
        return firstVer;
      }
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private static int extractBeginningInt(String javaVersion) {
    try {
      StringBuilder num = new StringBuilder();
      for (int i = 0; i < javaVersion.length(); ++i) {
        char c = javaVersion.charAt(i);
        if (Character.isDigit(c)) {
          num.append(c);
        } else {
          break;
        }
      }
      return Integer.parseInt(num.toString());
    } catch (NumberFormatException e) {
      return -1;
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

  private JavaVersion() { }
}
