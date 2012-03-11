/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.gson.internal;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A map of strings to values. Like LinkedHashMap, this map's iteration order is
 * well defined: it is the order that elements were inserted into the map. This
 * map does not support null keys.
 * 
 * <p>This implementation was derived from Android 4.0's LinkedHashMap.
 */
public final class StringMap<K, V> extends AbstractMap<K, V> {
  // TODO: defend against predictable hash collisions
  
  /**
   * Min capacity (other than zero) for a HashMap. Must be a power of two
   * greater than 1 (and less than 1 << 30).
   */
  private static final int MINIMUM_CAPACITY = 4;

  /**
   * Max capacity for a HashMap. Must be a power of two >= MINIMUM_CAPACITY.
   */
  private static final int MAXIMUM_CAPACITY = 1 << 30;

  /**
   * A dummy entry in the circular linked list of entries in the map.
   * The first real entry is header.nxt, and the last is header.prv.
   * If the map is empty, header.nxt == header && header.prv == header.
   */
  private LinkedEntry<K, V> header;

  /**
   * An empty table shared by all zero-capacity maps (typically from default
   * constructor). It is never written to, and replaced on first put. Its size
   * is set to half the minimum, so that the first resize will create a
   * minimum-sized table.
   */
  private static final Entry[] EMPTY_TABLE = new LinkedEntry[MINIMUM_CAPACITY >>> 1];

  /**
   * The hash table. If this hash map contains a mapping for null, it is
   * not represented this hash table.
   */
  private LinkedEntry<K, V>[] table;

  /**
   * The number of mappings in this hash map.
   */
  private int size;

  /**
   * The table is rehashed when its size exceeds this threshold.
   * The value of this field is generally .75 * capacity, except when
   * the capacity is zero, as described in the EMPTY_TABLE declaration
   * above.
   */
  private int threshold;

  // Views - lazily initialized
  private Set<K> keySet;
  private Set<Entry<K, V>> entrySet;
  private Collection<V> values;

  @SuppressWarnings("unchecked")
  public StringMap() {
    table = (LinkedEntry<K, V>[]) EMPTY_TABLE;
    threshold = -1; // Forces first put invocation to replace EMPTY_TABLE
    header = new LinkedEntry<K, V>();
  }

  @Override public int size() {
    return size;
  }

  @Override public boolean containsKey(Object key) {
    return getEntry(key) != null;
  }

  @Override public V get(Object key) {
    LinkedEntry<K, V> entry = getEntry(key);
    return entry != null ? entry.value : null;
  }

  private LinkedEntry<K, V> getEntry(Object key) {
    if (key == null) {
      return null;
    }

    int hash = secondaryHash(key.hashCode());
    LinkedEntry<K, V>[] tab = table;
    for (LinkedEntry<K, V> e = tab[hash & (tab.length - 1)]; e != null; e = e.next) {
      K eKey = e.key;
      if (eKey == key || (e.hash == hash && key.equals(eKey))) {
        return e;
      }
    }
    return null;
  }

  @Override public V put(K key, V value) {
    if (key == null) {
      throw new NullPointerException("key == null");
    }

    int hash = secondaryHash(key.hashCode());
    LinkedEntry<K, V>[] tab = table;
    int index = hash & (tab.length - 1);
    for (LinkedEntry<K, V> e = tab[index]; e != null; e = e.next) {
      if (e.hash == hash && key.equals(e.key)) {
        V oldValue = e.value;
        e.value = value;
        return oldValue;
      }
    }

    // No entry for (non-null) key is present; create one
    if (size++ > threshold) {
      tab = doubleCapacity();
      index = hash & (tab.length - 1);
    }
    addNewEntry(key, value, hash, index);
    return null;
  }

  private void addNewEntry(K key, V value, int hash, int index) {
    LinkedEntry<K, V> header = this.header;

    // Create new entry, link it on to list, and put it into table
    LinkedEntry<K, V> oldTail = header.prv;
    LinkedEntry<K, V> newTail = new LinkedEntry<K, V>(
        key, value, hash, table[index], header, oldTail);
    table[index] = oldTail.nxt = header.prv = newTail;
  }

  /**
   * Allocate a table of the given capacity and set the threshold accordingly.
   * @param newCapacity must be a power of two
   */
  private LinkedEntry<K, V>[] makeTable(int newCapacity) {
    @SuppressWarnings("unchecked")
    LinkedEntry<K, V>[] newTable = (LinkedEntry<K, V>[]) new LinkedEntry[newCapacity];
    table = newTable;
    threshold = (newCapacity >> 1) + (newCapacity >> 2); // 3/4 capacity
    return newTable;
  }

  /**
   * Doubles the capacity of the hash table. Existing entries are placed in
   * the correct bucket on the enlarged table. If the current capacity is,
   * MAXIMUM_CAPACITY, this method is a no-op. Returns the table, which
   * will be new unless we were already at MAXIMUM_CAPACITY.
   */
  private LinkedEntry<K, V>[] doubleCapacity() {
    LinkedEntry<K, V>[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
      return oldTable;
    }
    int newCapacity = oldCapacity * 2;
    LinkedEntry<K, V>[] newTable = makeTable(newCapacity);
    if (size == 0) {
      return newTable;
    }

    for (int j = 0; j < oldCapacity; j++) {
      /*
       * Rehash the bucket using the minimum number of field writes.
       * This is the most subtle and delicate code in the class.
       */
      LinkedEntry<K, V> e = oldTable[j];
      if (e == null) {
        continue;
      }
      int highBit = e.hash & oldCapacity;
      LinkedEntry<K, V> broken = null;
      newTable[j | highBit] = e;
      for (LinkedEntry<K, V> n = e.next; n != null; e = n, n = n.next) {
        int nextHighBit = n.hash & oldCapacity;
        if (nextHighBit != highBit) {
          if (broken == null) {
            newTable[j | nextHighBit] = n;
          } else {
            broken.next = n;
          }
          broken = e;
          highBit = nextHighBit;
        }
      }
      if (broken != null) {
        broken.next = null;
      }
    }
    return newTable;
  }

  @Override public V remove(Object key) {
    if (key == null) {
      return null;
    }
    int hash = secondaryHash(key.hashCode());
    LinkedEntry<K, V>[] tab = table;
    int index = hash & (tab.length - 1);
    for (LinkedEntry<K, V> e = tab[index], prev = null;
        e != null; prev = e, e = e.next) {
      if (e.hash == hash && key.equals(e.key)) {
        if (prev == null) {
          tab[index] = e.next;
        } else {
          prev.next = e.next;
        }
        size--;
        unlink(e);
        return e.value;
      }
    }
    return null;
  }

  private void unlink(LinkedEntry<K, V> e) {
    e.prv.nxt = e.nxt;
    e.nxt.prv = e.prv;
    e.nxt = e.prv = null; // Help the GC (for performance)
  }

  @Override public void clear() {
    if (size != 0) {
      Arrays.fill(table, null);
      size = 0;
    }

    // Clear all links to help GC
    LinkedEntry<K, V> header = this.header;
    for (LinkedEntry<K, V> e = header.nxt; e != header; ) {
      LinkedEntry<K, V> nxt = e.nxt;
      e.nxt = e.prv = null;
      e = nxt;
    }

    header.nxt = header.prv = header;
  }

  @Override public Set<K> keySet() {
    Set<K> ks = keySet;
    return (ks != null) ? ks : (keySet = new KeySet());
  }

  @Override public Collection<V> values() {
    Collection<V> vs = values;
    return (vs != null) ? vs : (values = new Values());
  }

  public Set<Entry<K, V>> entrySet() {
    Set<Entry<K, V>> es = entrySet;
    return (es != null) ? es : (entrySet = new EntrySet());
  }

  static class LinkedEntry<K, V> implements Entry<K, V> {
    final K key;
    V value;
    final int hash;
    LinkedEntry<K, V> next;
    LinkedEntry<K, V> nxt;
    LinkedEntry<K, V> prv;

    /** Create the header entry */
    LinkedEntry() {
      this(null, null, 0, null, null, null);
      nxt = prv = this;
    }

    LinkedEntry(K key, V value, int hash, LinkedEntry<K, V> next,
        LinkedEntry<K, V> nxt, LinkedEntry<K, V> prv) {
      this.key = key;
      this.value = value;
      this.hash = hash;
      this.next = next;
      this.nxt = nxt;
      this.prv = prv;
    }

    public final K getKey() {
      return key;
    }

    public final V getValue() {
      return value;
    }

    public final V setValue(V value) {
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    @Override public final boolean equals(Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      Entry<?, ?> e = (Entry<?, ?>) o;
      Object eValue = e.getValue();
      return key.equals(e.getKey())
          && (value == null ? eValue == null : value.equals(eValue));
    }

    @Override public final int hashCode() {
      return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }

    @Override public final String toString() {
      return key + "=" + value;
    }
  }

  /**
   * Removes the mapping from key to value and returns true if this mapping
   * exists; otherwise, returns does nothing and returns false.
   */
  private boolean removeMapping(Object key, Object value) {
    if (key == null) {
      return false;
    }

    int hash = secondaryHash(key.hashCode());
    LinkedEntry<K, V>[] tab = table;
    int index = hash & (tab.length - 1);
    for (LinkedEntry<K, V> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
      if (e.hash == hash && key.equals(e.key)) {
        if (value == null ? e.value != null : !value.equals(e.value)) {
          return false;  // Map has wrong value for key
        }
        if (prev == null) {
          tab[index] = e.next;
        } else {
          prev.next = e.next;
        }
        size--;
        unlink(e);
        return true;
      }
    }
    return false; // No entry for key
  }

  private abstract class LinkedHashIterator<T> implements Iterator<T> {
    LinkedEntry<K, V> next = header.nxt;
    LinkedEntry<K, V> lastReturned = null;

    public final boolean hasNext() {
      return next != header;
    }

    final LinkedEntry<K, V> nextEntry() {
      LinkedEntry<K, V> e = next;
      if (e == header) {
        throw new NoSuchElementException();
      }
      next = e.nxt;
      return lastReturned = e;
    }

    public final void remove() {
      if (lastReturned == null) {
        throw new IllegalStateException();
      }
      StringMap.this.remove(lastReturned.key);
      lastReturned = null;
    }
  }

  private final class KeySet extends AbstractSet<K> {
    public Iterator<K> iterator() {
      return new LinkedHashIterator<K>() {
        public final K next() {
          return nextEntry().key;
        }
      };
    }

    public int size() {
      return size;
    }

    public boolean contains(Object o) {
      return containsKey(o);
    }

    public boolean remove(Object o) {
      int oldSize = size;
      StringMap.this.remove(o);
      return size != oldSize;
    }

    public void clear() {
      StringMap.this.clear();
    }
  }

  private final class Values extends AbstractCollection<V> {
    public Iterator<V> iterator() {
      return new LinkedHashIterator<V>() {
        public final V next() {
          return nextEntry().value;
        }
      };
    }

    public int size() {
      return size;
    }

    public boolean contains(Object o) {
      return containsValue(o);
    }

    public void clear() {
      StringMap.this.clear();
    }
  }

  private final class EntrySet extends AbstractSet<Entry<K, V>> {
    public Iterator<Entry<K, V>> iterator() {
      return new LinkedHashIterator<Map.Entry<K, V>>() {
        public final Map.Entry<K, V> next() {
          return nextEntry();
        }
      };
    }

    public boolean contains(Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      Entry<?, ?> e = (Entry<?, ?>) o;
      V mappedValue = get(e.getKey());
      return mappedValue != null && mappedValue.equals(e.getValue());
    }

    public boolean remove(Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      Entry<?, ?> e = (Entry<?, ?>) o;
      return removeMapping(e.getKey(), e.getValue());
    }

    public int size() {
      return size;
    }

    public void clear() {
      StringMap.this.clear();
    }
  }

  /**
   * Applies a supplemental hash function to a given hashCode, which defends
   * against poor quality hash functions. This is critical because HashMap
   * uses power-of-two length hash tables, that otherwise encounter collisions
   * for hashCodes that do not differ in lower or upper bits.
   */
  private static int secondaryHash(int h) {
    // Doug Lea's supplemental hash function
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
  }
}