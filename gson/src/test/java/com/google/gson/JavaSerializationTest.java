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

package com.google.gson;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Check that Gson doesn't return non-serializable data types.
 *
 * @author Jesse Wilson
 */
public final class JavaSerializationTest {
  private final Gson gson = new Gson();

  @Test
  public void testMapIsSerializable() throws Exception {
    Type type = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson("{\"b\":1,\"c\":2,\"a\":3}", type);
    Map<String, Integer> serialized = serializedCopy(map);
    assertThat(serialized).isEqualTo(map);
    // Also check that the iteration order is retained.
    assertThat(serialized.keySet()).containsExactly("b", "c", "a").inOrder();
  }

  @Test
  public void testListIsSerializable() throws Exception {
    Type type = new TypeToken<List<String>>() {}.getType();
    List<String> list = gson.fromJson("[\"a\",\"b\",\"c\"]", type);
    List<String> serialized = serializedCopy(list);
    assertThat(serialized).isEqualTo(list);
  }

  @Test
  public void testNumberIsSerializable() throws Exception {
    Type type = new TypeToken<List<Number>>() {}.getType();
    List<Number> list = gson.fromJson("[1,3.14,6.673e-11]", type);
    List<Number> serialized = serializedCopy(list);
    assertThat(serialized.get(0).doubleValue()).isEqualTo(1.0);
    assertThat(serialized.get(1).doubleValue()).isEqualTo(3.14);
    assertThat(serialized.get(2).doubleValue()).isEqualTo(6.673e-11);
  }

  @SuppressWarnings("unchecked") // Serialization promises to return the same type.
  private static <T> T serializedCopy(T object) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bytesOut);
    out.writeObject(object);
    out.close();
    ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytesOut.toByteArray());
    ObjectInputStream in = new ObjectInputStream(bytesIn);
    return (T) in.readObject();
  }
}
