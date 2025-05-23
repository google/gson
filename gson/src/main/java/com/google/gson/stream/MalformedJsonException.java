/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.gson.stream;

import com.google.gson.Strictness;
import java.io.IOException;

/**
 * Thrown when a reader encounters malformed JSON. Some syntax errors can be ignored by using {@link
 * Strictness#LENIENT} for {@link JsonReader#setStrictness(Strictness)}.
 */
public final class MalformedJsonException extends IOException {
  private static final long serialVersionUID = 1L;

  public MalformedJsonException(String msg) {
    super(msg);
  }

  public MalformedJsonException(String msg, Throwable throwable) {
    super(msg, throwable);
  }

  public MalformedJsonException(Throwable throwable) {
    super(throwable);
  }
}
