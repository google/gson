/*
 * Copyright (C) 2009 Google Inc.
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
import static org.junit.Assert.assertThrows;

import java.io.EOFException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link JsonStreamParser}
 *
 * @author Inderjeet Singh
 */
public class JsonStreamParserTest {
  private JsonStreamParser parser;

  @Before
  public void setUp() throws Exception {
    parser = new JsonStreamParser("'one' 'two'");
  }

  @Test
  public void testParseTwoStrings() {
    String actualOne = parser.next().getAsString();
    assertThat(actualOne).isEqualTo("one");
    String actualTwo = parser.next().getAsString();
    assertThat(actualTwo).isEqualTo("two");
  }

  @Test
  public void testIterator() {
    assertThat(parser.hasNext()).isTrue();
    assertThat(parser.next().getAsString()).isEqualTo("one");
    assertThat(parser.hasNext()).isTrue();
    assertThat(parser.next().getAsString()).isEqualTo("two");
    assertThat(parser.hasNext()).isFalse();
  }

  @Test
  public void testNoSideEffectForHasNext() {
    assertThat(parser.hasNext()).isTrue();
    assertThat(parser.hasNext()).isTrue();
    assertThat(parser.hasNext()).isTrue();
    assertThat(parser.next().getAsString()).isEqualTo("one");

    assertThat(parser.hasNext()).isTrue();
    assertThat(parser.hasNext()).isTrue();
    assertThat(parser.next().getAsString()).isEqualTo("two");

    assertThat(parser.hasNext()).isFalse();
    assertThat(parser.hasNext()).isFalse();
  }

  @Test
  public void testCallingNextBeyondAvailableInput() {
    JsonElement unused1 = parser.next();
    JsonElement unused2 = parser.next();
    // Parser should not go beyond available input
    assertThrows(NoSuchElementException.class, parser::next);
  }

  @Test
  public void testEmptyInput() {
    JsonStreamParser parser = new JsonStreamParser("");
    JsonIOException e = assertThrows(JsonIOException.class, parser::next);
    assertThat(e).hasCauseThat().isInstanceOf(EOFException.class);

    parser = new JsonStreamParser("");
    e = assertThrows(JsonIOException.class, parser::hasNext);
    assertThat(e).hasCauseThat().isInstanceOf(EOFException.class);
  }

  @Test
  public void testIncompleteInput() {
    JsonStreamParser parser = new JsonStreamParser("[");
    assertThat(parser.hasNext()).isTrue();
    assertThrows(JsonSyntaxException.class, parser::next);
  }

  @Test
  public void testMalformedInput() {
    JsonStreamParser parser = new JsonStreamParser(":");
    assertThrows(JsonSyntaxException.class, parser::hasNext);

    parser = new JsonStreamParser(":");
    assertThrows(JsonSyntaxException.class, parser::next);
  }

  @Test
  public void testConcurrentNext() throws InterruptedException {
    int elementCount = 1000;
    Set<String> expected = new HashSet<>();
    StringBuilder json = new StringBuilder();
    for (int i = 0; i < elementCount; i++) {
      json.append("'element").append(i).append("' ");
      expected.add("element" + i);
    }
    JsonStreamParser parser = new JsonStreamParser(json.toString());

    // Consume the elements from two threads; every element must be returned to exactly one
    // thread, without corrupting the state of the parser
    Set<String> consumed = ConcurrentHashMap.newKeySet();
    Queue<Throwable> failures = new ConcurrentLinkedQueue<>();
    CountDownLatch startSignal = new CountDownLatch(1);
    Thread[] threads = new Thread[2];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(() -> consumeElements(parser, consumed, failures, startSignal));
      threads[i].start();
    }

    startSignal.countDown();
    for (Thread thread : threads) {
      thread.join(10000);
    }

    assertThat(failures).isEmpty();
    assertThat(consumed).containsExactlyElementsIn(expected);
  }

  private static void consumeElements(
      JsonStreamParser parser,
      Set<String> consumed,
      Queue<Throwable> failures,
      CountDownLatch startSignal) {
    try {
      startSignal.await();
      while (true) {
        consumed.add(parser.next().getAsString());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (NoSuchElementException expected) {
      // All elements have been consumed
    } catch (RuntimeException | Error e) {
      failures.add(e);
    }
  }
}
