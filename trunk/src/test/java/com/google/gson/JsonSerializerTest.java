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

package com.google.gson;

import com.google.gson.TestTypes.BagOfPrimitives;
import com.google.gson.TestTypes.ClassOverridingEquals;
import com.google.gson.TestTypes.ClassWithCustomTypeConverter;
import com.google.gson.TestTypes.ClassWithSerializedNameFields;
import com.google.gson.TestTypes.StringWrapper;
import com.google.gson.annotations.Since;

import junit.framework.TestCase;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * Small test for Json Serialization
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonSerializerTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testObjectEqualButNotSame() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    ClassOverridingEquals objB = new ClassOverridingEquals();
    objB.ref = objA;

    assertEquals(objB.getExpectedJson(), gson.toJson(objB));
  }

  public void testWriter() throws Exception {
    Writer writer = new StringWriter();
    BagOfPrimitives src = new BagOfPrimitives();
    gson.toJson(src, writer);
    assertEquals(src.getExpectedJson(), writer.toString());
  }

  public void testCustomSerializers() {
    Gson gson = new GsonBuilder().registerSerializer(
        ClassWithCustomTypeConverter.class, new JsonSerializer<ClassWithCustomTypeConverter>() {
      public JsonElement serialize(ClassWithCustomTypeConverter src, Type typeOfSrc,
          JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("bag", 5);
        json.addProperty("value", 25);
        return json;
      }
    }).create();
    ClassWithCustomTypeConverter target = new ClassWithCustomTypeConverter();
    assertEquals("{\"bag\":5,\"value\":25}", gson.toJson(target));
  }

  public void testNestedCustomSerializers() {
    Gson gson = new GsonBuilder().registerSerializer(
        BagOfPrimitives.class, new JsonSerializer<BagOfPrimitives>() {
      public JsonElement serialize(BagOfPrimitives src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive(6);
      }
    }).create();
    ClassWithCustomTypeConverter target = new ClassWithCustomTypeConverter();
    assertEquals("{\"bag\":6,\"value\":10}", gson.toJson(target));
  }

  public void testStaticFieldsAreNotSerialized() {
    BagOfPrimitives target = new BagOfPrimitives();
    assertFalse(gson.toJson(target).contains("DEFAULT_VALUE"));
  }

  static class Version1 {
    int a = 0;
    @Since(1.0) int b = 1;
  }

  static class Version1_1 extends Version1 {
    @Since(1.1) int c = 2;
  }

  @Since(1.2)
  static class Version1_2 {
    int d = 3;
  }

  public void testVersionedClasses() {
    Gson gson = new GsonBuilder().setVersion(1.0).create();
    String json1 = gson.toJson(new Version1());
    String json2 = gson.toJson(new Version1_1());
    assertEquals(json1, json2);
  }

  public void testIgnoreLaterVersionClass() {
    Gson gson = new GsonBuilder().setVersion(1.0).create();
    assertEquals("", gson.toJson(new Version1_2()));
  }

  public void testVersionedGsonWithUnversionedClasses() {
    Gson gson = new GsonBuilder().setVersion(1.0).create();
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testEscapingQuotesInStringArray() throws Exception {
    String[] valueWithQuotes = { "beforeQuote\"afterQuote" };
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    String[] target = gson.fromJson(jsonRepresentation, String[].class);
    assertEquals(1, target.length);
    assertEquals(valueWithQuotes[0], target[0]);
  }

  public void testEscapingObjectFields() throws Exception {
    BagOfPrimitives objWithPrimitives = new BagOfPrimitives(1L, 1, true, "test with\" <script>");
    String jsonRepresentation = gson.toJson(objWithPrimitives);
    assertFalse(jsonRepresentation.contains("<"));
    assertFalse(jsonRepresentation.contains(">"));
    assertTrue(jsonRepresentation.contains("\\\""));

    BagOfPrimitives expectedObject = gson.fromJson(jsonRepresentation, BagOfPrimitives.class);
    assertEquals(objWithPrimitives.getExpectedJson(), expectedObject.getExpectedJson());
  }

  public void testGsonWithNonDefaultFieldNamingPolicy() {
    Gson gson = new GsonBuilder().setFieldNamingPolicy(
        FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"SomeConstantStringInstanceField\":\""
        + target.someConstantStringInstanceField + "\"}", gson.toJson(target));
  }

  public void testGsonWithSerializedNameFieldNamingPolicy() {
    ClassWithSerializedNameFields expected = new ClassWithSerializedNameFields(5);
    String actual = gson.toJson(expected);
    assertEquals(expected.getExpectedJson(), actual);
  }
}
