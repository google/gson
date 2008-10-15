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

import java.lang.reflect.Type;

/**
 * Decorators a {@code JsonSerializer} instance with exception handling.  This wrapper class
 * ensures that a {@code JsonSerializer} will not propagate any exception other than a
 * {@link JsonParseException}.
 *
 * @param <T> type of the serializer being wrapped.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
class JsonSerializerExceptionWrapper<T> implements JsonSerializer<T> {
  private final JsonSerializer<T> delegate;

  /**
   * Returns a wrapped {@link JsonSerializer} object that has been decorated with
   * {@link JsonParseException} handling.
   *
   * @param delegate the {@code JsonSerializer} instance to be wrapped
   * @throws IllegalArgumentException if {@code delegate} is {@code null}.
   */
  JsonSerializerExceptionWrapper(JsonSerializer<T> delegate) {
    Preconditions.checkNotNull(delegate);
    this.delegate = delegate;
  }

  public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
    try {
      return delegate.serialize(src, typeOfSrc, context);
    } catch (JsonParseException e) {
      // just rethrow the exception
      throw e;
    } catch (Exception e) {
      // throw as a JsonParseException
      StringBuilder errorMsg = new StringBuilder()
          .append("The JsonSerializer ")
          .append(delegate)
          .append(" failed to serialized object ")
          .append(src)
          .append(" given the type ")
          .append(typeOfSrc);
      throw new JsonParseException(errorMsg.toString(), e);
    }
  }
  
  @Override
  public String toString() {
    return delegate.toString();
  }
}
