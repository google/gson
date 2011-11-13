/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson;

import com.google.gson.internal.bind.MiniGson;
import com.google.gson.internal.bind.TypeAdapter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import junit.framework.TestCase;

public final class ObjectTypeAdapterTest extends TestCase {
  private final MiniGson gson = new MiniGson.Builder().build();
  private final TypeAdapter<Object> adapter = gson.getAdapter(Object.class);

  public void testDeserialize() throws Exception {
    Map<?, ?> map = (Map<?, ?>) adapter.fromJson("{a: 5, b: [1, 2, null], c: {x: y}}");
    assertEquals(5.0, map.get("a"));
    assertEquals(Arrays.asList(1.0, 2.0, null), map.get("b"));
    assertEquals(Collections.singletonMap("x", "y"), map.get("c"));
    assertEquals(3, map.size());
  }

  @SuppressWarnings("unused")
  public void testSerialize() throws Exception {
    Object object = new Object() {
      Object a = 5;
      Object b = Arrays.asList(1, 2, null);
    };
    assertEquals("{'a':5,'b':[1,2,null]}", adapter.toJson(object).replace("\"", "'"));
  }

  public void testSerializeObject() throws Exception {
    assertEquals("{}", adapter.toJson(new Object()));
  }
}
