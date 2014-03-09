/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal;

import com.google.gson.common.MoreAsserts;
import com.google.gson.internal.LinkedHashTreeMap.AvlBuilder;
import com.google.gson.internal.LinkedHashTreeMap.AvlIterator;
import com.google.gson.internal.LinkedHashTreeMap.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import junit.framework.TestCase;

public final class LinkedHashTreeMapTest extends TestCase {
  public void testIterationOrder() {
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    assertIterationOrder(map.keySet(), "a", "c", "b");
    assertIterationOrder(map.values(), "android", "cola", "bbq");
  }

  public void testRemoveRootDoesNotDoubleUnlink() {
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
    it.next();
    it.next();
    it.next();
    it.remove();
    assertIterationOrder(map.keySet(), "a", "c");
  }

  public void testPutNullKeyFails() {
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    try {
      map.put(null, "android");
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testPutNonComparableKeyFails() {
    LinkedHashTreeMap<Object, String> map = new LinkedHashTreeMap<Object, String>();
    try {
      map.put(new Object(), "android");
      fail();
    } catch (ClassCastException expected) {}
  }

  public void testContainsNonComparableKeyReturnsFalse() {
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    map.put("a", "android");
    assertFalse(map.containsKey(new Object()));
  }

  public void testContainsNullKeyIsAlwaysFalse() {
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    map.put("a", "android");
    assertFalse(map.containsKey(null));
  }

  public void testPutOverrides() throws Exception {
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    assertNull(map.put("d", "donut"));
    assertNull(map.put("e", "eclair"));
    assertNull(map.put("f", "froyo"));
    assertEquals(3, map.size());

    assertEquals("donut", map.get("d"));
    assertEquals("donut", map.put("d", "done"));
    assertEquals(3, map.size());
  }

  public void testEmptyStringValues() {
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    map.put("a", "");
    assertTrue(map.containsKey("a"));
    assertEquals("", map.get("a"));
  }

  // NOTE that this does not happen every time, but given the below predictable random,
  // this test will consistently fail (assuming the initial size is 16 and rehashing
  // size remains at 3/4)
  public void testForceDoublingAndRehash() throws Exception {
    Random random = new Random(1367593214724L);
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    String[] keys = new String[1000];
    for (int i = 0; i < keys.length; i++) {
      keys[i] = Integer.toString(Math.abs(random.nextInt()), 36) + "-" + i;
      map.put(keys[i], "" + i);
    }

    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      assertTrue(map.containsKey(key));
      assertEquals("" + i, map.get(key));
    }
  }

  public void testClear() {
    LinkedHashTreeMap<String, String> map = new LinkedHashTreeMap<String, String>();
    map.put("a", "android");
    map.put("c", "cola");
    map.put("b", "bbq");
    map.clear();
    assertIterationOrder(map.keySet());
    assertEquals(0, map.size());
  }

  public void testEqualsAndHashCode() throws Exception {
    LinkedHashTreeMap<String, Integer> map1 = new LinkedHashTreeMap<String, Integer>();
    map1.put("A", 1);
    map1.put("B", 2);
    map1.put("C", 3);
    map1.put("D", 4);

    LinkedHashTreeMap<String, Integer> map2 = new LinkedHashTreeMap<String, Integer>();
    map2.put("C", 3);
    map2.put("B", 2);
    map2.put("D", 4);
    map2.put("A", 1);

    MoreAsserts.assertEqualsAndHashCode(map1, map2);
  }

  public void testAvlWalker() {
    assertAvlWalker(node(node("a"), "b", node("c")),
        "a", "b", "c");
    assertAvlWalker(node(node(node("a"), "b", node("c")), "d", node(node("e"), "f", node("g"))),
        "a", "b", "c", "d", "e", "f", "g");
    assertAvlWalker(node(node(null, "a", node("b")), "c", node(node("d"), "e", null)),
        "a", "b", "c", "d", "e");
    assertAvlWalker(node(null, "a", node(null, "b", node(null, "c", node("d")))),
        "a", "b", "c", "d");
    assertAvlWalker(node(node(node(node("a"), "b", null), "c", null), "d", null),
        "a", "b", "c", "d");
  }

  private void assertAvlWalker(Node<String, String> root, String... values) {
    AvlIterator<String, String> iterator = new AvlIterator<String, String>();
    iterator.reset(root);
    for (String value : values) {
      assertEquals(value, iterator.next().getKey());
    }
    assertNull(iterator.next());
  }

  public void testAvlBuilder() {
    assertAvlBuilder(1, "a");
    assertAvlBuilder(2, "(. a b)");
    assertAvlBuilder(3, "(a b c)");
    assertAvlBuilder(4, "(a b (. c d))");
    assertAvlBuilder(5, "(a b (c d e))");
    assertAvlBuilder(6, "((. a b) c (d e f))");
    assertAvlBuilder(7, "((a b c) d (e f g))");
    assertAvlBuilder(8, "((a b c) d (e f (. g h)))");
    assertAvlBuilder(9, "((a b c) d (e f (g h i)))");
    assertAvlBuilder(10, "((a b c) d ((. e f) g (h i j)))");
    assertAvlBuilder(11, "((a b c) d ((e f g) h (i j k)))");
    assertAvlBuilder(12, "((a b (. c d)) e ((f g h) i (j k l)))");
    assertAvlBuilder(13, "((a b (c d e)) f ((g h i) j (k l m)))");
    assertAvlBuilder(14, "(((. a b) c (d e f)) g ((h i j) k (l m n)))");
    assertAvlBuilder(15, "(((a b c) d (e f g)) h ((i j k) l (m n o)))");
    assertAvlBuilder(16, "(((a b c) d (e f g)) h ((i j k) l (m n (. o p))))");
    assertAvlBuilder(30, "((((. a b) c (d e f)) g ((h i j) k (l m n))) o "
        + "(((p q r) s (t u v)) w ((x y z) A (B C D))))");
    assertAvlBuilder(31, "((((a b c) d (e f g)) h ((i j k) l (m n o))) p "
        + "(((q r s) t (u v w)) x ((y z A) B (C D E))))");
  }

  private void assertAvlBuilder(int size, String expected) {
    char[] values = "abcdefghijklmnopqrstuvwxyzABCDE".toCharArray();
    AvlBuilder<String, String> avlBuilder = new AvlBuilder<String, String>();
    avlBuilder.reset(size);
    for (int i = 0; i < size; i++) {
      avlBuilder.add(node(Character.toString(values[i])));
    }
    assertTree(expected, avlBuilder.root());
  }

  public void testDoubleCapacity() {
    @SuppressWarnings("unchecked") // Arrays and generics don't get along.
    Node<String, String>[] oldTable = new Node[1];
    oldTable[0] = node(node(node("a"), "b", node("c")), "d", node(node("e"), "f", node("g")));

    Node<String, String>[] newTable = LinkedHashTreeMap.doubleCapacity(oldTable);
    assertTree("(b d f)", newTable[0]); // Even hash codes!
    assertTree("(a c (. e g))", newTable[1]); // Odd hash codes!
  }

  public void testDoubleCapacityAllNodesOnLeft() {
    @SuppressWarnings("unchecked") // Arrays and generics don't get along.
    Node<String, String>[] oldTable = new Node[1];
    oldTable[0] = node(node("b"), "d", node("f"));

    Node<String, String>[] newTable = LinkedHashTreeMap.doubleCapacity(oldTable);
    assertTree("(b d f)", newTable[0]); // Even hash codes!
    assertNull(newTable[1]); // Odd hash codes!

    for (Node<?, ?> node : newTable) {
      if (node != null) {
        assertConsistent(node);
      }
    }
  }

  private static final Node<String, String> head = new Node<String, String>();

  private Node<String, String> node(String value) {
    return new Node<String, String>(null, value, value.hashCode(), head, head);
  }

  private Node<String, String> node(Node<String, String> left, String value,
      Node<String, String> right) {
    Node<String, String> result = node(value);
    if (left != null) {
      result.left = left;
      left.parent = result;
    }
    if (right != null) {
      result.right = right;
      right.parent = result;
    }
    return result;
  }

  private void assertTree(String expected, Node<?, ?> root) {
    assertEquals(expected, toString(root));
    assertConsistent(root);
  }

  private void assertConsistent(Node<?, ?> node) {
    int leftHeight = 0;
    if (node.left != null) {
      assertConsistent(node.left);
      assertSame(node, node.left.parent);
      leftHeight = node.left.height;
    }
    int rightHeight = 0;
    if (node.right != null) {
      assertConsistent(node.right);
      assertSame(node, node.right.parent);
      rightHeight = node.right.height;
    }
    if (node.parent != null) {
      assertTrue(node.parent.left == node || node.parent.right == node);
    }
    if (Math.max(leftHeight, rightHeight) + 1 != node.height) {
      fail();
    }
  }

  private String toString(Node<?, ?> root) {
    if (root == null) {
      return ".";
    } else if (root.left == null && root.right == null) {
      return String.valueOf(root.key);
    } else {
      return String.format("(%s %s %s)", toString(root.left), root.key, toString(root.right));
    }
  }

  private <T> void assertIterationOrder(Iterable<T> actual, T... expected) {
    ArrayList<T> actualList = new ArrayList<T>();
    for (T t : actual) {
      actualList.add(t);
    }
    assertEquals(Arrays.asList(expected), actualList);
  }
}
