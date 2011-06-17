/*
 * Copyright (C) 2011 Google Inc.
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

import com.google.gson.common.MoreAsserts;
import junit.framework.TestCase;

/**
 * @author Jesse Wilson
 */
public final class JsonArrayTest extends TestCase {

  public void testEqualsOnEmptyArray() {
    MoreAsserts.assertEqualsAndHashCode(new JsonArray(), new JsonArray());
  }

  public void testEqualsNonEmptyArray() {
    JsonArray a = new JsonArray();
    JsonArray b = new JsonArray();

    assertEquals(a, a);

    a.add(new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add(new JsonObject());
    MoreAsserts.assertEqualsAndHashCode(a, b);

    a.add(new JsonObject());
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));

    b.add(JsonNull.INSTANCE);
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
  }

  public void testDeepCopy() {
    JsonObject v1 = new JsonObject();
    v1.add("k", new JsonPrimitive("v"));
    JsonNull v2 = JsonNull.INSTANCE;
    JsonPrimitive v3 = new JsonPrimitive("abc");
    JsonArray v4 = new JsonArray();
    v4.add(new JsonPrimitive("def"));

    JsonArray array = new JsonArray();
    array.add(v1);
    array.add(v2);
    array.add(v3);
    array.add(v4);

    // the deep copy must be equal
    JsonArray deepCopy = array.deepCopy();
    assertEquals(array, deepCopy);

    // collections must be copied by value
    JsonObject d1 = deepCopy.get(0).getAsJsonObject();
    assertEquals(v1, d1);
    assertTrue(v1 != d1);
    JsonArray d4 = deepCopy.get(3).getAsJsonArray();
    assertEquals(v4, d4);
    assertTrue(v4 != d4);

    // collections should themselves be deeply immutable
    v1.add("k2", new JsonPrimitive("v2"));
    assertEquals(1, d1.entrySet().size());
    v4.add(new JsonPrimitive("ghi"));
    assertEquals(1, d4.size());
  }
}
