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
 * Defines an interface which the sole purpose is for object creation for
 * a particular type <T>.  Implementations of this interface should be
 * registered with a {@link com.google.gson.MappedObjectConstructor} instance.
 *
 * This interface is exposed to allow applications to plug in its own
 * {@link InstanceCreator} implementations to be used by this general JSON
 * converter framework.
 *
 * @param <T> the type of object that will be created by this implementation.
 *
 * @see com.google.gson.MappedObjectConstructor
 *
 * @author Joel Leitch
 */
public interface InstanceCreator<T> {

  /**
   * @param type the parameterized T represented as a {@link Type}
   * @return a default object instance of type T
   */
  public T createInstance(Type type);
}
