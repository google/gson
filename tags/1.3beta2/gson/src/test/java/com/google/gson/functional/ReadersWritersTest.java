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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.common.TestTypes.BagOfPrimitives;

import junit.framework.TestCase;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Functional tests for the support of {@link Reader}s and {@link Writer}s.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ReadersWritersTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testWriterForSerialization() throws Exception {
    Writer writer = new StringWriter();
    BagOfPrimitives src = new BagOfPrimitives();
    gson.toJson(src, writer);
    assertEquals(src.getExpectedJson(), writer.toString());
  }

  public void testReaderForDeserialization() throws Exception {
    BagOfPrimitives expected = new BagOfPrimitives();
    Reader json = new StringReader(expected.getExpectedJson());
    BagOfPrimitives actual = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(expected, actual);
  }

  public void testTopLevelNullObjectSerializationWithWriter() {
    StringWriter writer = new StringWriter();
    gson.toJson(null, writer);
    assertEquals("", writer.toString());
  }

  public void testTopLevelNullObjectDeserializationWithReader() {
    StringReader reader = new StringReader("null");
    Integer nullIntObject = gson.fromJson(reader, Integer.class);
    assertNull(nullIntObject);
  }

  public void testTopLevelNullObjectSerializationWithWriterAndSerializeNulls() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    StringWriter writer = new StringWriter();
    gson.toJson(null, writer);
    assertEquals("null", writer.toString());
  }

  public void testTopLevelNullObjectDeserializationWithReaderAndSerializeNulls() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    StringReader reader = new StringReader("null");
    Integer nullIntObject = gson.fromJson(reader, Integer.class);
    assertNull(nullIntObject);
  }
}
