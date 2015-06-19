/*
 * Copyright (C) 2015 Google Inc.
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

package com.google.gson;

import java.io.IOException;

import com.google.gson.stream.JsonReader;

/**
 * A mechanism for providing custom unknown property handling in Gson during deserialization.
 * This allows to provide custom behavior to handle fields present in the incoming Json which
 * are not present in the Java classes, such as ignore them or throw an exception.
 *
 * @author Matteo Cerina
 * @since 2.3.2
 */
public interface UnknownFieldHandlingStrategy {

  /**
   * Handles a field present in the incoming Json but not in the Java class
   *
   * @param in the {@link JsonReader} reading the incoming Json
   * @param instance the instance of the Java class we are trying to deserialize to
   * @param name the name of the field in the incoming Json
   */
  public void handleUnknownField(JsonReader in, Object instance, String name) throws IOException;
}
