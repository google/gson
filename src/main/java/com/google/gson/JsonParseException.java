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

package com.google.gson;

/**
 * This exception is raised if there is a serious issue that occurs
 * during parsing.  One of the main usages for this class is for the
 * GSON infrastructure.  If the incoming JSON is bad/malicious, an instance
 * of this exception is raised.
 *
 * This is a runtime exception because this is exposed to the client.  Using
 * a RuntimeException avoids bad coding practices on the client side where
 * they catch the exception and do nothing.  It is often the case that you
 * want to blow up if there is a Parse Exception (i.e. most likely client
 * code will never know how to recover from a {@link JsonParseException}.
 *
 * @author Joel Leitch
 */
public final class JsonParseException extends RuntimeException {
  static final long serialVersionUID = -4086729973971783390L;

  public JsonParseException(String msg) {
    super(msg);
  }

  public JsonParseException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public JsonParseException(Throwable cause) {
    super(cause);
  }
}
