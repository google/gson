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
 * Decorators a {@code JsonDeserializer} instance with exception handling.  This wrapper class
 * ensures that a {@code JsonDeserializer} will not propagate any exception other than a
 * {@link JsonParseException}.
 *
 * @param <T> type of the deserializer being wrapped.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
class JsonDeserializerExceptionWrapper<T> implements JsonDeserializer<T> {

  private final JsonDeserializer<T> delegate;

  /**
   * Returns a wrapped {@link JsonDeserializer} object that has been decorated with
   * {@link JsonParseException} handling.
   *
   * @param delegate the {@code JsonDeserializer} instance to be wrapped.
   * @throws IllegalArgumentException if {@code delegate} is {@code null}.
   */
  JsonDeserializerExceptionWrapper(JsonDeserializer<T> delegate) {
    Preconditions.checkNotNull(delegate);
    this.delegate = delegate;
  }

  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    try {
      return delegate.deserialize(json, typeOfT, context);
    } catch (JsonParseException e) {
      // just rethrow the exception
      throw e;
    } catch (Exception e) {
      // rethrow as a JsonParseException
      StringBuilder errorMsg = new StringBuilder()
          .append("The JsonDeserializer ")
          .append(delegate)
          .append(" failed to deserialized json object ")
          .append(json)
          .append(" given the type ")
          .append(typeOfT);
      throw new JsonParseException(errorMsg.toString(), e);
    }
  }
  
  @Override
  public String toString() {
    return delegate.toString();
  }
}