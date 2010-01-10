/*
 * Copyright (C) 2010 Google Inc.
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
 * Defines generic cache interface.
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
interface Cache<K, V> {

  /**
   * Adds the new value object into the cache for the given key.  If the key already
   * exists, then this method will override the value for the key.
   *
   * @param key the key identifier for the {@code value} object
   * @param value the value object to store in the cache
   */
  void addElement(K key, V value);

  /**
   * Retrieve the cached value for the given {@code key}.
   *
   * @param key the key identifying the value
   * @return the cached value for the given {@code key}
   */
  V getElement(K key);
  
  /**
   * Removes the value from the cache for the given key.
   * 
   * @param key the key identifying the value to remove
   * @return the value for the given {@code key}
   */
  V removeElement(K key);

  /**
   * Removes everything from this cache.
   */
  void clear();
  
  /**
   * @return the number of objects in this cache
   */
  int size();
}
