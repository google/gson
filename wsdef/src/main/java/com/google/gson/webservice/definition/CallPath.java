/*
 * Copyright (C) 2008 Google Inc.
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
package com.google.gson.webservice.definition;

/**
 * Encapsulation of a Web service path that is sent by the client.
 * 
 * @author inder
 */
public final class CallPath {

  private final String path;
  private final double version;

  public CallPath(String path) {
    if (path == null) {
      this.path = null;
      version = -1D;
    } else {
      int index1 = path.indexOf('/');
      int index2 = path.substring(index1+1).indexOf('/');
      String versionStr = path.substring(index1+1, index2+1);
      String callPathStr = path;
      double givenVersion = -1D;
      try {
        // Skip over the version number from the URL
        givenVersion = Double.parseDouble(versionStr);
        callPathStr = path.substring(index2+1);
      } catch (NumberFormatException e) {
        // Assume that version number wasn't specified
      }
      this.path = callPathStr;
      this.version = givenVersion;
    }
  }

  public String get() {
    return path;
  }

  public double getVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    return getClass() == obj.getClass() && equal(path, ((CallPath)obj).path);
  }

  private static boolean equal(String s1, String s2) {
    return s1 == s2 || (s1 != null && s2 != null && s1.equals(s2));
  }

  @Override
  public String toString() {
    return path;
  }
}
