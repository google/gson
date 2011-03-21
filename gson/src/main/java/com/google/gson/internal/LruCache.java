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

package com.google.gson.internal;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An implementation of the {@link Cache} interface that evict objects from the cache using an
 * LRU (least recently used) algorithm.  Object start getting evicted from the cache once the
 * {@code maxCapacity} is reached.
 * 
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class LruCache<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {
  private static final long serialVersionUID = 1L;

  private final int maxCapacity;

  public LruCache(int maxCapacity) {
    super(maxCapacity, 0.7F, true);
    this.maxCapacity = maxCapacity;
  }

  public synchronized void addElement(K key, V value) {
    put(key, value);
  }

  public synchronized V getElement(K key) {
    return get(key);
  }

  public synchronized V removeElement(K key) {
    return remove(key);
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
    return size() > maxCapacity;
  }
}
