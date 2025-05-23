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
import java.util.NoSuchElementException;
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
}
