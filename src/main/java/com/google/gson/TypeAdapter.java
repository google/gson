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
 * This class is responsible for adapting/converting an particular "from"
 * instance to an instance of type "to".
 *
 * @author Joel Leitch
 */
interface TypeAdapter {

  /**
   * Adapts an object instance "from" to and instance of type "to".
   *
   * @param from the object to adapt
   * @param to the Type/Class which this will convert to
   * @return the converted "from" instance to type "to"
   */
  public <T> T adaptType(Object from, Class<T> to);
}
