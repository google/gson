/*
 * Copyright (C) 2018 Google Inc.
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

/**
 * Defines the Gson serialization/deserialization API.
 * @since 2.8.6
 */
module com.google.gson {
  exports com.google.gson;
  exports com.google.gson.annotations;
  exports com.google.gson.reflect;
  exports com.google.gson.stream;

  // Optional dependency on java.sql
  requires static java.sql;

  // Optional dependency on jdk.unsupported for JDK's sun.misc.Unsafe
  requires static jdk.unsupported;
}
