/*
 * Copyright (C) 2008 Google Inc.
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
package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonStreamParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for the support of {@link Reader}s and {@link Writer}s.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ReadersWritersTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testWriterForSerialization() {
    Writer writer = new StringWriter();
    BagOfPrimitives src = new BagOfPrimitives();
    gson.toJson(src, writer);
    assertThat(writer.toString()).isEqualTo(src.getExpectedJson());
  }

  @Test
  public void testReaderForDeserialization() {
    BagOfPrimitives expected = new BagOfPrimitives();
    Reader json = new StringReader(expected.getExpectedJson());
    BagOfPrimitives actual = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testTopLevelNullObjectSerializationWithWriter() {
    StringWriter writer = new StringWriter();
    gson.toJson(null, writer);
    assertThat(writer.toString()).isEqualTo("null");
  }

  @Test
  public void testTopLevelNullObjectDeserializationWithReader() {
    StringReader reader = new StringReader("null");
    Integer nullIntObject = gson.fromJson(reader, Integer.class);
    assertThat(nullIntObject).isNull();
  }

  @Test
  public void testTopLevelNullObjectSerializationWithWriterAndSerializeNulls() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    StringWriter writer = new StringWriter();
    gson.toJson(null, writer);
    assertThat(writer.toString()).isEqualTo("null");
  }

  @Test
  public void testTopLevelNullObjectDeserializationWithReaderAndSerializeNulls() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    StringReader reader = new StringReader("null");
    Integer nullIntObject = gson.fromJson(reader, Integer.class);
    assertThat(nullIntObject).isNull();
  }

  @Test
  public void testReadWriteTwoStrings() throws IOException {
    Gson gson = new Gson();
    CharArrayWriter writer = new CharArrayWriter();
    writer.write(gson.toJson("one").toCharArray());
    writer.write(gson.toJson("two").toCharArray());
    CharArrayReader reader = new CharArrayReader(writer.toCharArray());
    JsonStreamParser parser = new JsonStreamParser(reader);
    String actualOne = gson.fromJson(parser.next(), String.class);
    assertThat(actualOne).isEqualTo("one");
    String actualTwo = gson.fromJson(parser.next(), String.class);
    assertThat(actualTwo).isEqualTo("two");
  }

  @Test
  public void testReadWriteTwoObjects() throws IOException {
    Gson gson = new Gson();
    CharArrayWriter writer = new CharArrayWriter();
    BagOfPrimitives expectedOne = new BagOfPrimitives(1, 1, true, "one");
    writer.write(gson.toJson(expectedOne).toCharArray());
    BagOfPrimitives expectedTwo = new BagOfPrimitives(2, 2, false, "two");
    writer.write(gson.toJson(expectedTwo).toCharArray());
    CharArrayReader reader = new CharArrayReader(writer.toCharArray());
    JsonStreamParser parser = new JsonStreamParser(reader);
    BagOfPrimitives actualOne = gson.fromJson(parser.next(), BagOfPrimitives.class);
    assertThat(actualOne.stringValue).isEqualTo("one");
    BagOfPrimitives actualTwo = gson.fromJson(parser.next(), BagOfPrimitives.class);
    assertThat(actualTwo.stringValue).isEqualTo("two");
    assertThat(parser.hasNext()).isFalse();
  }

  @Test
  public void testTypeMismatchThrowsJsonSyntaxExceptionForStrings() {
    try {
      gson.fromJson("true", new TypeToken<Map<String, String>>() {}.getType());
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test
  public void testTypeMismatchThrowsJsonSyntaxExceptionForReaders() {
    try {
      gson.fromJson(new StringReader("true"), new TypeToken<Map<String, String>>() {}.getType());
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  /**
   * Verifies that passing an {@link Appendable} which is not an instance of {@link Writer}
   * to {@code Gson.toJson} works correctly.
   */
  @Test
  public void testToJsonAppendable() {
    class CustomAppendable implements Appendable {
      final StringBuilder stringBuilder = new StringBuilder();
      int toStringCallCount = 0;

      @Override
      public Appendable append(char c) throws IOException {
        stringBuilder.append(c);
        return this;
      }

      @Override
      public Appendable append(CharSequence csq) throws IOException {
        if (csq == null) {
          csq = "null"; // Requirement by Writer.append
        }
        append(csq, 0, csq.length());
        return this;
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        if (csq == null) {
          csq = "null"; // Requirement by Writer.append
        }

        // According to doc, toString() must return string representation
        String s = csq.toString();
        toStringCallCount++;
        stringBuilder.append(s, start, end);
        return this;
      }
    }

    CustomAppendable appendable = new CustomAppendable();
    gson.toJson(Arrays.asList("test", 123, true), appendable);
    // Make sure CharSequence.toString() was called at least two times to verify that
    // CurrentWrite.cachedString is properly overwritten when char array changes
    assertThat(appendable.toStringCallCount >= 2).isTrue();
    assertThat(appendable.stringBuilder.toString()).isEqualTo("[\"test\",123,true]");
  }
}
