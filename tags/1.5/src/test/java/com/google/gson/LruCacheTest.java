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
    Cache<String, Integer> cache = new LruCache<String, Integer>(3);
    
    String key = "key1";
    assertNull(cache.getElement(key));
    cache.addElement(key, 1);
    assertEquals(1, cache.getElement(key).intValue());
    
    String key2 = "key2";
    cache.addElement(key2, 2);
    assertEquals(1, cache.getElement(key).intValue());
    assertEquals(2, cache.getElement(key2).intValue());
  }
  
  public void testCacheKeyOverwrite() throws Exception {
    Cache<String, Integer> cache = new LruCache<String, Integer>(3);
    
    String key = "key1";
    assertNull(cache.getElement(key));
    cache.addElement(key, 1);
    assertEquals(1, cache.getElement(key).intValue());
    
    cache.addElement(key, 5);
    assertEquals(5, cache.getElement(key).intValue());
  }
  
  public void testCacheEviction() throws Exception {
    Cache<String, Integer> cache = new LruCache<String, Integer>(5);

    cache.addElement("key1", 1);
    cache.addElement("key2", 2);
    cache.addElement("key3", 3);
    cache.addElement("key4", 4);
    cache.addElement("key5", 5);
    assertEquals(1, cache.getElement("key1").intValue());
    assertEquals(2, cache.getElement("key2").intValue());
    assertEquals(3, cache.getElement("key3").intValue());
    assertEquals(4, cache.getElement("key4").intValue());
    assertEquals(5, cache.getElement("key5").intValue());

    // Access key1 to show key2 will be evicted (shows not a FIFO cache)
    cache.getElement("key1");
    cache.getElement("key3");
    cache.addElement("key6", 6);
    cache.addElement("key7", 7);
    assertEquals(1, cache.getElement("key1").intValue());
    assertNull(cache.getElement("key2"));
    assertEquals(3, cache.getElement("key3").intValue());
    assertNull(cache.getElement("key4"));
    assertEquals(5, cache.getElement("key5").intValue());
    assertEquals(6, cache.getElement("key6").intValue());
    assertEquals(7, cache.getElement("key7").intValue());
  }
}
