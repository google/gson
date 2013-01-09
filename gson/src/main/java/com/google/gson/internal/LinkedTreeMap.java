/*
 * Copyright (C) 2012 Google Inc.
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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map with a {@code Comparable} key that is implemented as a red-black tree.
 *
 * <p>A red-black tree offers quicker insert operations than AVL trees; however, slower "find"
 * operations.
 *
 * <p>This implementation was derived from the JDK's TreeMap class.
 */
public class LinkedTreeMap<K extends Comparable<K>, V>
    extends AbstractMap<K, V> implements Serializable {
  private static final boolean BLACK = false;
  private static final boolean RED = true;

  // Size stored as a field for optimization instead of recursing tree.
  private int size = 0;

  private TreeNode<K, V> root;

  // Store the head and tail to preserve the ordering of nodes inserted into tree
  private TreeNode<K, V> head;
  private TreeNode<K, V> tail;

  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  public boolean containsKey(K key) {
    return (find(key) != null);
  }

  public V get(K key) {
    TreeNode<K, V> entry = find(key);
    return (entry == null) ? null : entry.getValue();
  }

  public V put(K key, V value) {
    $Gson$Preconditions.checkNotNull(key);
    if (root == null) {
      root = new TreeNode<K, V>(null, null, key, value);
      head = root;
      tail = root;
      size++;
      return null;
    } else {
      return findAndUpdateOrCreateNode(key, value);
    }
  }

  private V findAndUpdateOrCreateNode(K key, V value) {
    TreeNode<K, V> parent;
    int lastCompare;

    TreeNode<K, V> entry = root;
    do {
      parent = entry;
      lastCompare = key.compareTo(entry.key);
      if (lastCompare < 0) {
        entry = entry.left;
      } else if (lastCompare > 0) {
        entry = entry.right;
      } else {
        V rval = entry.getValue();
        entry.setValue(value);
        return rval;
      }
    } while (entry != null);

    size++;

    // Create a new node and set up the tree edges
    TreeNode<K, V> newEntry = new TreeNode<K, V>(parent, tail, key, value);
    if (lastCompare < 0) {
      parent.left = newEntry;
    } else if (lastCompare > 0) {
      parent.right = newEntry;
    }

    tail.next = newEntry;
    tail = newEntry;
    rebalanceAfterInsert(newEntry);
    return null;
  }

  private void rebalanceAfterInsert(TreeNode<K, V> x) {
    x.color = RED;

    while (x != null && x != root && x.parent.color == RED) {
      if (x.parent == leftOf(parentOf(parentOf(x)))) {
        TreeNode<K, V> y = rightOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {
          if (x == rightOf(parentOf(x))) {
            x= parentOf(x);
            rotateLeft(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          rotateRight(parentOf(parentOf(x)));
        }
      } else {
        TreeNode<K, V> y = leftOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {
          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {
          if (x == leftOf(parentOf(x))) {
            x = parentOf(x);
            rotateRight(x);
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          rotateLeft(parentOf(parentOf(x)));
        }
      }
    }
    root.color = BLACK;
  }

  private static <K extends Comparable<K>, V> TreeNode<K, V> parentOf(TreeNode<K, V> e) {
    return (e != null ? e.parent : null);
  }

  private static <K extends Comparable<K>, V> boolean colorOf(TreeNode<K, V> e) {
    return (e != null ? e.color : BLACK);
  }

  private static <K extends Comparable<K>, V> TreeNode<K, V> leftOf(TreeNode<K, V> e) {
    return (e != null ? e.left : null);
  }

  private static <K extends Comparable<K>, V> TreeNode<K, V> rightOf(TreeNode<K, V> e) {
    return (e != null ? e.right : null);
  }

  private static <K extends Comparable<K>, V> void setColor(TreeNode<K, V> e, boolean c) {
    if (e != null){
      e.color = c;
    }
  }

  private static <K extends Comparable<K>, V> TreeNode<K, V> successor(TreeNode<K, V> t) {
    if (t == null) {
      return null;
    } else if (t.right != null) {
      TreeNode<K, V> p = t.right;
      while (p.left != null) {
        p = p.left;
      }
      return p;
    } else {
      TreeNode<K, V> p = t.parent;
      TreeNode<K, V> ch = t;
      while (p != null && ch == p.right) {
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  private void rotateLeft(TreeNode<K, V> p) {
    if (p != null) {
      TreeNode<K, V> r = p.right;
      p.right = r.left;
      if (r.left != null) {
        r.left.parent = p;
      }
      r.parent = p.parent;
      if (p.parent == null) {
        root = r;
      } else if (p.parent.left == p) {
        p.parent.left = r;
      } else {
        p.parent.right = r;
      }
      r.left = p;
      p.parent = r;
    }
  }

  private void rotateRight(TreeNode<K, V> p) {
    if (p != null) {
      TreeNode<K, V> l = p.left;
      p.left = l.right;
      if (l.right != null) {
        l.right.parent = p;
      }
      l.parent = p.parent;
      if (p.parent == null) {
        root = l;
      } else if (p.parent.right == p) {
        p.parent.right = l;
      } else {
        p.parent.left = l;
      }
      l.right = p;
      p.parent = l;
    }
  }

  public V remove(K key) {
    TreeNode<K, V> entry = find(key);
    if (entry == null) {
      return null;
    } else {
      size--;
      V rval = entry.getValue();
      preserveOrderForRemoval(entry);
      removeNode(entry);
      return rval;
    }
  }

  private void removeNode(TreeNode<K, V> p) {
    if (p.left != null && p.right != null) {
      TreeNode<K, V> s = successor(p);
      p.key = s.key;
      p.value = s.value;
      p = s;
    }

    TreeNode<K, V> replacement = (p.left != null ? p.left : p.right);
    if (replacement != null) {
      // Link replacement to parent
      replacement.parent = p.parent;
      if (p.parent == null) {
        root = replacement;
      } else if (p == p.parent.left) {
        p.parent.left  = replacement;
      } else {
        p.parent.right = replacement;
      }

      // Null out links so they are OK to use by fixAfterDeletion.
      p.left = null;
      p.right = null;
      p.parent = null;

      // Fix replacement
      if (p.color == BLACK) {
        fixAfterDeletion(replacement);
      }
    } else if (p.parent == null) { // return if we are the only node.
      root = null;
    } else { //  No children. Use self as phantom replacement and unlink.
      if (p.color == BLACK) {
        fixAfterDeletion(p);
      }

      if (p.parent != null) {
        if (p == p.parent.left) {
          p.parent.left = null;
        } else if (p == p.parent.right) {
          p.parent.right = null;
        }
        p.parent = null;
      }
    }
  }

  private void preserveOrderForRemoval(TreeNode<K, V> p) {
    // Preserve insertion order for entry set iteration
    if (p == head) {
      head = p.next;
    }
    if (p == tail) {
      tail = p.previous;
    }

    TreeNode<K, V> previousNode = p.previous;
    TreeNode<K, V> nextNode = p.next;
    if (previousNode != null) {
      previousNode.next = nextNode;
    }
    if (nextNode != null) {
      nextNode.previous = previousNode;
    }
  }

  private void fixAfterDeletion(TreeNode<K, V> x) {
    while (x != root && colorOf(x) == BLACK) {
      if (x == leftOf(parentOf(x))) {
        TreeNode<K, V> sib = rightOf(parentOf(x));

        if (colorOf(sib) == RED) {
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateLeft(parentOf(x));
          sib = rightOf(parentOf(x));
        }

        if (colorOf(leftOf(sib))  == BLACK &&
            colorOf(rightOf(sib)) == BLACK) {
          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if (colorOf(rightOf(sib)) == BLACK) {
            setColor(leftOf(sib), BLACK);
            setColor(sib, RED);
            rotateRight(sib);
            sib = rightOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(rightOf(sib), BLACK);
          rotateLeft(parentOf(x));
          x = root;
        }
      } else { // symmetric
        TreeNode<K, V> sib = leftOf(parentOf(x));

        if (colorOf(sib) == RED) {
          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          rotateRight(parentOf(x));
          sib = leftOf(parentOf(x));
        }

        if (colorOf(rightOf(sib)) == BLACK &&
            colorOf(leftOf(sib)) == BLACK) {
          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if (colorOf(leftOf(sib)) == BLACK) {
            setColor(rightOf(sib), BLACK);
            setColor(sib, RED);
            rotateLeft(sib);
            sib = leftOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(leftOf(sib), BLACK);
          rotateRight(parentOf(x));
          x = root;
        }
      }
    }

    setColor(x, BLACK);
  }

  public int size() {
    return size;
  }

  /**
   * If somebody is unlucky enough to have to serialize one of these, serialize
   * it as a LinkedHashMap so that they won't need Gson on the other side to
   * deserialize it. Using serialization defeats our DoS defence, so most apps
   * shouldn't use it.
   */
  private Object writeReplace() throws ObjectStreamException {
    return new LinkedHashMap<K, V>(this);
  }

  private TreeNode<K, V> find(K key) {
    if (key != null) {
      for (TreeNode<K, V> entry = root; entry != null; ) {
        int compareVal = key.compareTo(entry.key);
        if (compareVal < 0) {
          entry = entry.left;
        } else if (compareVal > 0) {
          entry = entry.right;
        } else {
          return entry;
        }
      }
    }
    return null;
  }

  private static class TreeNode<K extends Comparable<K>, V> implements Map.Entry<K, V> {
    private K key;
    private V value;
    private TreeNode<K, V> parent;
    private TreeNode<K, V> left;
    private TreeNode<K, V> right;

    // Used for rebalance tree
    private boolean color = BLACK;

    // This is used for preserving the insertion order
    private TreeNode<K, V> next;
    private TreeNode<K, V> previous;

    TreeNode(TreeNode<K, V> parent, TreeNode<K, V> previous, K key, V value) {
      this.parent = parent;
      this.previous = previous;
      this.key = key;
      this.value = value;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    // I'd like to make this throw an UnsupportedOperationException; however,
    public V setValue(V value) {
      V rval = this.value;
      this.value = value;
      return rval;
    }

    @Override
    public final boolean equals(Object o) {
      if (!(o instanceof Entry)) {
        return false;
      }
      Entry<?, ?> e = (Entry<?, ?>) o;
      Object eValue = e.getValue();
      return key.equals(e.getKey())
          && (value == null ? eValue == null : value.equals(eValue));
    }

    @Override
    public final int hashCode() {
      return key.hashCode() ^ (value == null ? 0 : value.hashCode());
    }

    @Override
    public final String toString() {
      return key + "=" + value;
    }
  }

  class EntrySet extends AbstractSet<Entry<K, V>> {
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new LinkedTreeIterator(head);
    }

    @Override
    public int size() {
      return LinkedTreeMap.this.size();
    }
  }

  private class LinkedTreeIterator implements Iterator<Map.Entry<K, V>> {
    private TreeNode<K, V> current;

    private LinkedTreeIterator(TreeNode<K, V> first) {
      this.current = first;
    }

    public boolean hasNext() {
      return current != null;
    }

    public Map.Entry<K, V> next() {
      TreeNode<K, V> rval = current;
      current = current.next;
      return rval;
    }

    public final void remove() {
      LinkedTreeMap.this.remove(current.getKey());
    }
  }
}
