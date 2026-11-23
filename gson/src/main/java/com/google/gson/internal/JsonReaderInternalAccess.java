/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;

/** Internal-only APIs of JsonReader available only to other classes in Gson. */
public abstract class JsonReaderInternalAccess {
  // Suppress warnings because field is initialized by `JsonReader` class during class loading
  // (and therefore should be thread-safe), and any usage appears after `JsonReader` was loaded
  @SuppressWarnings({"ConstantField", "NonFinalStaticField"})
  public static volatile JsonReaderInternalAccess INSTANCE;

  /** Changes the type of the current property name token to a string value. */
  public abstract void promoteNameToValue(JsonReader reader) throws IOException;

  /**
   * Whether it is guaranteed that the peeked value is a number which will fit into a {@code long},
   * and {@link JsonReader#nextLong()} is guaranteed to not fail. Must only be called after {@link
   * JsonReader#peek()} returned {@link JsonToken#NUMBER}.
   *
   * <p>A return value of {@code false} does not mean the number is not a valid {@code long}; the
   * number might be valid but the implementation cannot guarantee it.
   */
  public abstract boolean isPeekedLong(JsonReader reader);
}
