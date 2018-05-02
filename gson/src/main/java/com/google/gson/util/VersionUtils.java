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

package com.google.gson.util;

/**
 * Utility to check the major Java version of the current JVM.
 */
public class VersionUtils {

  private static final int majorJavaVersion = determineMajorJavaVersion();

  private static int determineMajorJavaVersion() {
    String[] parts = System.getProperty("java.version").split("[._]");
    int firstVer = Integer.parseInt(parts[0]);
    if (firstVer == 1 && parts.length > 1) {
      return Integer.parseInt(parts[1]);
    } else {
      return firstVer;
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
