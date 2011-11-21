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


import junit.framework.TestCase;

/**
 * Unit test for the {@link LruCache} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class LruCacheTest extends TestCase {

  public void testCacheHitAndMiss() throws Exception {
    LruCache<String, Integer> cache = new LruCache<String, Integer>(3);

    String key = "key1";
    assertNull(cache.get(key));
    cache.put(key, 1);
    assertEquals(1, cache.get(key).intValue());

    String key2 = "key2";
    cache.put(key2, 2);
    assertEquals(1, cache.get(key).intValue());
    assertEquals(2, cache.get(key2).intValue());
  }

  public void testCacheKeyOverwrite() throws Exception {
    LruCache<String, Integer> cache = new LruCache<String, Integer>(3);

    String key = "key1";
    assertNull(cache.get(key));
    cache.put(key, 1);
    assertEquals(1, cache.get(key).intValue());

    cache.put(key, 5);
    assertEquals(5, cache.get(key).intValue());
  }

  public void testCacheEviction() throws Exception {
    LruCache<String, Integer> cache = new LruCache<String, Integer>(5);

    cache.put("key1", 1);
    cache.put("key2", 2);
    cache.put("key3", 3);
    cache.put("key4", 4);
    cache.put("key5", 5);
    assertEquals(1, cache.get("key1").intValue());
    assertEquals(2, cache.get("key2").intValue());
    assertEquals(3, cache.get("key3").intValue());
    assertEquals(4, cache.get("key4").intValue());
    assertEquals(5, cache.get("key5").intValue());

    // Access key1 to show key2 will be evicted (shows not a FIFO cache)
    cache.get("key1");
    cache.get("key3");
    cache.put("key6", 6);
    cache.put("key7", 7);
    assertEquals(1, cache.get("key1").intValue());
    assertNull(cache.get("key2"));
    assertEquals(3, cache.get("key3").intValue());
    assertNull(cache.get("key4"));
    assertEquals(5, cache.get("key5").intValue());
    assertEquals(6, cache.get("key6").intValue());
    assertEquals(7, cache.get("key7").intValue());
  }
}
