/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.wsclient.internal.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ConnectionPreconditions {

  public static void checkArgument(boolean condition, HttpURLConnection conn) {
    if (!condition) {
      StringBuilder sb = new StringBuilder();
      try {
        sb.append("HttpURLConnection Details\n");
        sb.append("ResponseCode:" + conn.getResponseCode());
        sb.append(", ContentType: " + conn.getContentType() + "\n");
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        for (Entry<String, List<String>> header : headerFields.entrySet()) {
          sb.append(header.getKey()).append(":");
          boolean first = true;
          for (String value : header.getValue()) {
            if (first) {
              first = false;
            } else {
              sb.append(","); 
            }
            sb.append(value);
          }
          sb.append("\n");
        }
        byte[] data = readInByteArray(conn.getInputStream());
        sb.append(new String(data));
      } catch (IOException e) {
        // ignore
      }
      throw new IllegalArgumentException(sb.toString());
    }
  }

  private static byte[] readInByteArray(InputStream src) {
    ByteArrayOutputStream dst = new ByteArrayOutputStream();
    try {
      Streams.copy(src, dst, true, true);
    } catch (IOException e) {
      // ignore
    }
    return dst.toByteArray();
  }

  private ConnectionPreconditions() {
    // prevent instantiation
  }
}
