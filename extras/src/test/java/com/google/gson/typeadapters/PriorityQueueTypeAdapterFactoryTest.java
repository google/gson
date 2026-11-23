/*
 * Copyright (C) 2026 Gson Authors
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

package com.google.gson.typeadapters;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.junit.Test;

public class PriorityQueueTypeAdapterFactoryTest {
  private static final TypeToken<PriorityQueue<Integer>> QUEUE_TYPE =
      new TypeToken<PriorityQueue<Integer>>() {};

  private final Gson gson =
      new GsonBuilder().registerTypeAdapterFactory(new PriorityQueueTypeAdapterFactory()).create();

  @Test
  public void testSerializeInPriorityOrderWithoutModifyingQueue() {
    PriorityQueue<Integer> queue = new PriorityQueue<>();
    queue.addAll(List.of(4, 2, 9, 1, 3, 5, 2));
    List<Integer> originalOrder = new ArrayList<>(queue);

    assertThat(gson.toJson(queue, QUEUE_TYPE.getType())).isEqualTo("[1,2,2,3,4,5,9]");
    assertThat(queue).containsExactlyElementsIn(originalOrder).inOrder();
    assertThat(queue.peek()).isEqualTo(1);
  }

  @Test
  public void testDeserializeUnsortedElements() {
    PriorityQueue<Integer> queue = gson.fromJson("[22,10,20]", QUEUE_TYPE);

    assertThat(queue.poll()).isEqualTo(10);
    assertThat(queue.poll()).isEqualTo(20);
    assertThat(queue.poll()).isEqualTo(22);
    assertThat(queue).isEmpty();
  }

  @Test
  public void testQueueWithWildcardUpperBound() {
    TypeToken<List<? extends PriorityQueue<Integer>>> type =
        new TypeToken<List<? extends PriorityQueue<Integer>>>() {};
    List<? extends PriorityQueue<Integer>> queues = gson.fromJson("[[3,1,2]]", type);

    PriorityQueue<Integer> queue = queues.get(0);
    Integer first = queue.poll();
    assertThat(first).isEqualTo(1);
    assertThat(queue.poll()).isEqualTo(2);
    assertThat(queue.poll()).isEqualTo(3);
    assertThat(queue).isEmpty();
  }

  @Test
  public void testEmptyAndNullQueue() {
    assertThat(gson.toJson(new PriorityQueue<>(), QUEUE_TYPE.getType())).isEqualTo("[]");
    assertThat(gson.fromJson("[]", QUEUE_TYPE)).isEmpty();
    assertThat(gson.toJson(null, QUEUE_TYPE.getType())).isEqualTo("null");
    assertThat(gson.fromJson("null", QUEUE_TYPE)).isNull();
  }

  @Test
  public void testRawQueue() {
    PriorityQueue<Integer> queue = new PriorityQueue<>(List.of(3, 1, 2));

    assertThat(gson.toJson(queue)).isEqualTo("[1,2,3]");
    PriorityQueue<?> deserialized = gson.fromJson("[3,1,2]", PriorityQueue.class);
    assertThat(deserialized.poll()).isEqualTo(1.0);
    assertThat(deserialized.poll()).isEqualTo(2.0);
    assertThat(deserialized.poll()).isEqualTo(3.0);
  }

  @Test
  public void testCustomComparatorRejected() {
    PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.reverseOrder());
    queue.addAll(List.of(1, 2));

    var e = assertThrows(IllegalArgumentException.class, () -> gson.toJson(queue));
    assertThat(e)
        .hasMessageThat()
        .isEqualTo("PriorityQueue with a custom comparator is not supported");
  }

  @Test
  public void testParameterizedElementAdapter() {
    TypeToken<Box<String>> boxType = new TypeToken<Box<String>>() {};
    TypeAdapter<Box<String>> boxAdapter =
        new TypeAdapter<Box<String>>() {
          @Override
          public Box<String> read(JsonReader in) throws IOException {
            return new Box<>(in.nextString().substring("box:".length()));
          }

          @Override
          public void write(JsonWriter out, Box<String> box) throws IOException {
            out.value("box:" + box.value);
          }
        };
    Gson customGson =
        new GsonBuilder()
            .registerTypeAdapter(boxType.getType(), boxAdapter)
            .registerTypeAdapterFactory(new PriorityQueueTypeAdapterFactory())
            .create();
    TypeToken<PriorityQueue<Box<String>>> queueType =
        new TypeToken<PriorityQueue<Box<String>>>() {};
    PriorityQueue<Box<String>> queue = new PriorityQueue<>();
    queue.add(new Box<>("b"));
    queue.add(new Box<>("a"));

    String json = customGson.toJson(queue, queueType.getType());
    assertThat(json).isEqualTo("[\"box:a\",\"box:b\"]");
    PriorityQueue<Box<String>> deserialized = customGson.fromJson(json, queueType);
    assertThat(deserialized.poll().value).isEqualTo("a");
    assertThat(deserialized.poll().value).isEqualTo("b");
  }

  @Test
  public void testListAdapterUsedInBothDirections() {
    TypeToken<List<Integer>> listType = new TypeToken<List<Integer>>() {};
    TypeAdapter<List<Integer>> delegate = new Gson().getAdapter(listType);
    TypeAdapter<List<Integer>> listAdapter =
        new TypeAdapter<List<Integer>>() {
          @Override
          public List<Integer> read(JsonReader in) throws IOException {
            in.beginObject();
            assertThat(in.nextName()).isEqualTo("elements");
            List<Integer> elements = delegate.read(in);
            in.endObject();
            return elements;
          }

          @Override
          public void write(JsonWriter out, List<Integer> elements) throws IOException {
            out.beginObject();
            out.name("elements");
            delegate.write(out, elements);
            out.endObject();
          }
        };
    Gson customGson =
        new GsonBuilder()
            .registerTypeAdapter(listType.getType(), listAdapter)
            .registerTypeAdapterFactory(new PriorityQueueTypeAdapterFactory())
            .create();
    PriorityQueue<Integer> queue = new PriorityQueue<>(List.of(3, 1, 2));

    String json = customGson.toJson(queue, QUEUE_TYPE.getType());
    assertThat(json).isEqualTo("{\"elements\":[1,2,3]}");
    PriorityQueue<Integer> deserialized = customGson.fromJson(json, QUEUE_TYPE);
    assertThat(deserialized.poll()).isEqualTo(1);
    assertThat(deserialized.poll()).isEqualTo(2);
    assertThat(deserialized.poll()).isEqualTo(3);
  }

  @Test
  public void testNullElementsRejected() {
    var e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson("[null]", QUEUE_TYPE));
    assertThat(e).hasCauseThat().isInstanceOf(NullPointerException.class);
  }

  @Test
  public void testNonComparableElementsRejected() {
    TypeToken<PriorityQueue<Object>> queueType = new TypeToken<PriorityQueue<Object>>() {};
    for (String json : List.of("[{}]", "[{},{}]")) {
      var e = assertThrows(JsonSyntaxException.class, () -> gson.fromJson(json, queueType));
      assertThat(e).hasCauseThat().isInstanceOf(ClassCastException.class);
    }
  }

  @Test
  public void testOtherCollectionsUnaffected() {
    assertThat(gson.toJson(List.of(3, 1, 2))).isEqualTo("[3,1,2]");
  }

  private static final class Box<T extends Comparable<T>> implements Comparable<Box<T>> {
    final T value;

    Box(T value) {
      this.value = value;
    }

    @Override
    public int compareTo(Box<T> other) {
      return value.compareTo(other.value);
    }
  }
}
