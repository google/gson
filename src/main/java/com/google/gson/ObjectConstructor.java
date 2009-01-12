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
 * Defines a generic object construction factory.  The purpose of this class
 * is to construct a default instance of a class that can be used for object
 * navigation while deserialization from its JSON representation.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
interface ObjectConstructor {

  /**
   * Creates a new instance of the given type.
   *
   * @param typeOfT the class type that should be instantiated
   * @return a default instance of the provided class.
   */
  public <T> T construct(Type typeOfT);

  /**
   * Constructs an array type of the provided length.
   *
   * @param typeOfArrayElements type of objects in the array
   * @param length size of the array
   * @return new array of size length
   */
  public Object constructArray(Type typeOfArrayElements, int length);
}