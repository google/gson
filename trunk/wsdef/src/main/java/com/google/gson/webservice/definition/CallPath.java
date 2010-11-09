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

import com.google.gson.rest.definition.ID;
import com.google.gson.webservice.definition.internal.utils.Pair;

/**
 * Encapsulation of a Web service path that is sent by the client.
 * 
 * @author inder
 */
public final class CallPath {

  private static final double IGNORE_VERSION = -1D;
  private final String path;
  private final double version;
  private final long resourceId;

  public CallPath(String path) {
    if (path == null) {
      this.path = null;
      version = IGNORE_VERSION;
      resourceId = ID.INVALID_ID;
    } else {
      Pair<Double, String> path2 = extractVersion(path);
      this.version = path2.first;
      Pair<Long, String> path3 = extractId(path2.second);
      this.resourceId = path3.first;
      this.path = path3.second;
    }
  }

  /**
   * Returns path after consuming version number from the begining
   */
  private static Pair<Double, String> extractVersion(String path) {
    int index1 = path.indexOf('/');
    int index2 = path.substring(index1+1).indexOf('/');
    String versionStr = path.substring(index1+1, index2+1);
    double extractedVersion = -1.0D;
    String revisedPath = path;
    try {
      // Skip over the version number from the URL
      extractedVersion = Double.parseDouble(versionStr);
      revisedPath = path.substring(index2+1);
    } catch (NumberFormatException e) {
      // Assume that version number wasn't specified
    }
    return Pair.create(extractedVersion, revisedPath);
  }

  private static Pair<Long, String> extractId(String path) {
    Pair<Long, String> originalPath = Pair.create(ID.INVALID_ID, path);
    int end = path.endsWith("/") ? path.length() - 1 : path.length();
    int begin = path.substring(0, end-1).lastIndexOf('/') + 1;
    if (begin < 0 || end < 0 || begin >= end) {
      return originalPath;
    }
    try {
      String id = path.substring(begin, end);
      String pathWithoutId = path.substring(0, begin-1);
      return Pair.create(Long.parseLong(id), pathWithoutId);
    } catch (NumberFormatException e) {
      return originalPath;
    }
  }

  public String get() {
    return path;
  }

  public double getVersion() {
    return version;
  }

  public long getResourceId() {
    return resourceId;
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  public boolean matches(CallPath callPath) {
    return path.startsWith(callPath.get());
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
    return String.format("path:%s, version:%2.f, resourceId: %d", path, version, resourceId);
  }
}
