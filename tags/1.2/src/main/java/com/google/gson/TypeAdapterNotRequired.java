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
 * This class implements the {@link TypeAdapter} interface; however, if the
 * from instance type is the same as the to type then this object will
 * terminate the chain early and return the "from" object to the calling
 * class.
 *
 * If the incoming object does need some kind of conversion then this object
 * will delegate to the {@link TypeAdapter} that it is wrapping.
 *
 * @author Joel Leitch
 */
final class TypeAdapterNotRequired implements TypeAdapter {

  private final TypeAdapter delegate;

  /**
   * Constructs a TypeAdapterNotRequired that will wrap the delegate instance
   * that is passed in.
   *
   * @param delegate the TypeConverter to delegate to if this instance can
   *        not handle the type adapting (can not be null)
   */
  TypeAdapterNotRequired(TypeAdapter delegate) {
    Preconditions.checkNotNull(delegate);
    this.delegate = delegate;
  }

  @SuppressWarnings("unchecked")
  public <T> T adaptType(Object from, Class<T> to) {
    if (to.isAssignableFrom(from.getClass())) {
      return (T) from;
    }
    return delegate.adaptType(from, to);
  }
}
