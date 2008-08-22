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

import com.google.gson.TestTypes.ArrayOfArrays;
import com.google.gson.TestTypes.ArrayOfObjects;
import com.google.gson.TestTypes.BagOfPrimitiveWrappers;
import com.google.gson.TestTypes.BagOfPrimitives;
import com.google.gson.TestTypes.ClassWithArray;
import com.google.gson.TestTypes.ClassWithCustomTypeConverter;
import com.google.gson.TestTypes.ClassWithEnumFields;
import com.google.gson.TestTypes.ClassWithNoFields;
import com.google.gson.TestTypes.ClassWithObjects;
import com.google.gson.TestTypes.ClassWithPrivateNoArgsConstructor;
import com.google.gson.TestTypes.ClassWithSerializedNameFields;
import com.google.gson.TestTypes.ClassWithSubInterfacesOfCollection;
import com.google.gson.TestTypes.ClassWithTransientFields;
import com.google.gson.TestTypes.ContainsReferenceToSelfType;
import com.google.gson.TestTypes.ExceptionHolder;
import com.google.gson.TestTypes.MyEnum;
import com.google.gson.TestTypes.MyEnumCreator;
import com.google.gson.TestTypes.Nested;
import com.google.gson.TestTypes.PrimitiveArray;
import com.google.gson.TestTypes.StringWrapper;
import com.google.gson.TestTypes.SubTypeOfNested;
import com.google.gson.common.MoreAsserts;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Small test for Json Deserialization.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonDeserializerTest extends TestCase {

  private Gson gson = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testInvalidJson() throws Exception {
    try {
      gson.fromJson("adfasdf1112,,,\":", BagOfPrimitives.class);
      fail("Bad JSON should throw a ParseException");
    } catch (JsonParseException expected) { }

    try {
      gson.fromJson("{adfasdf1112,,,\":}", BagOfPrimitives.class);
      fail("Bad JSON should throw a ParseException");
    } catch (JsonParseException expected) { }
  }

  public void testBagOfPrimitives() {
    BagOfPrimitives src = new BagOfPrimitives(10, 20, false, "stringValue");
    String json = src.getExpectedJson();
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testReader() throws Exception {
    BagOfPrimitives expected = new BagOfPrimitives();
    Reader json = new StringReader(expected.getExpectedJson());
    BagOfPrimitives actual = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(expected, actual);
  }

  @SuppressWarnings("unchecked")
  public void testRawCollectionOfBagOfPrimitives() {
    try {
      BagOfPrimitives bag = new BagOfPrimitives(10, 20, false, "stringValue");
      String json = '[' + bag.getExpectedJson() + ',' + bag.getExpectedJson() + ']';
      Collection target = gson.fromJson(json, Collection.class);
      assertEquals(2, target.size());
      for (BagOfPrimitives bag1 : (Collection<BagOfPrimitives>) target) {
        assertEquals(bag.getExpectedJson(), bag1.getExpectedJson());
      }
      fail("Raw collection of objects should not work");
    } catch (JsonParseException expected) {
    }
  }

  public void testBagOfPrimitiveWrappers() {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    String jsonString = target.getExpectedJson();
    target = gson.fromJson(jsonString, BagOfPrimitiveWrappers.class);
    assertEquals(jsonString, target.getExpectedJson());
  }

  public void testDirectedAcyclicGraph() {
    String json = "{\"children\":[{\"children\":[{\"children\":[]}]},{\"children\":[]}]}";
    ContainsReferenceToSelfType target = gson.fromJson(json, ContainsReferenceToSelfType.class);
    assertNotNull(target);
    assertEquals(2, target.children.size());
  }

  public void testEmptyCollectionInAnObject() {
    String json = "{\"children\":[]}";
    ContainsReferenceToSelfType target = gson.fromJson(json, ContainsReferenceToSelfType.class);
    assertNotNull(target);
    assertTrue(target.children.isEmpty());
  }

  public void testPrimitiveArrayInAnObject() {
    String json = "{\"longArray\":[0,1,2,3,4,5,6,7,8,9]}";
    PrimitiveArray target = gson.fromJson(json, PrimitiveArray.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfPrimitives() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    int[] target = gson.fromJson(json, int[].class);
    int[] expected = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    MoreAsserts.assertEquals(expected, target);
  }

  public void testArrayOfStrings() {
    String json = "[\"Hello\",\"World\"]";
    String[] target = gson.fromJson(json, String[].class);
    assertEquals("Hello", target[0]);
    assertEquals("World", target[1]);
  }

  public void testCollectionOfStrings() {
    String json = "[\"Hello\",\"World\"]";
    Type collectionType = new TypeToken<Collection<String>>() { }.getType();
    Collection<String> target = gson.fromJson(json, collectionType);

    assertTrue(target.contains("Hello"));
    assertTrue(target.contains("World"));
  }

  public void testCollectionOfIntegers() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    Type collectionType = new TypeToken<Collection<Integer>>() { }.getType();
    Collection<Integer> target = gson.fromJson(json, collectionType);
    int[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    MoreAsserts.assertEquals(expected, toIntArray(target));
  }

  public void testRawCollectionNotAllowed() {
    String json = "[0,1,2,3,4,5,6,7,8,9]";
    try {
	    gson.fromJson(json, Collection.class);
	    fail("Can not deserialize a non-genericized collection.");
    } catch (JsonParseException expected) { }

    json = "[\"Hello\", \"World\"]";
    try {
      gson.fromJson(json, Collection.class);
      fail("Can not deserialize a non-genericized collection.");
    } catch (JsonParseException expected) { }
  }

  public void testListOfIntegerCollections() throws Exception {
    String json = "[[1,2,3],[4,5,6],[7,8,9]]";
    Type collectionType = new TypeToken<Collection<Collection<Integer>>>() {}.getType();
    List<Collection<Integer>> target = gson.fromJson(json, collectionType);
    int[][] expected = new int[3][3];
    for (int i = 0; i < 3; ++i) {
      int start = (3 * i) + 1;
      for (int j = 0; j < 3; ++j) {
        expected[i][j] = start + j;
      }
    }

    for (int i = 0; i < 3; i++) {
      MoreAsserts.assertEquals(expected[i], toIntArray(target.get(i)));
    }
  }

  @SuppressWarnings("unchecked")
  private static int[] toIntArray(Collection collection) {
    int[] ints = new int[collection.size()];
    int i = 0;
    for (Iterator iterator = collection.iterator(); iterator.hasNext(); ++i) {
      Object obj = iterator.next();
      if (obj instanceof Integer) {
        ints[i] = ((Integer)obj).intValue();
      } else if (obj instanceof Long) {
        ints[i] = ((Long)obj).intValue();
      }
    }
    return ints;
  }

  public void testClassWithTransientFields() throws Exception {
    String json = "{\"longValue\":[1]}";
    ClassWithTransientFields target = gson.fromJson(
        json, ClassWithTransientFields.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testTransientFieldsPassedInJsonAreIgnored() throws Exception {
    String json = "{\"transientLongValue\":1,\"longValue\":[1]}";
    ClassWithTransientFields target = gson.fromJson(
        json, ClassWithTransientFields.class);
    assertFalse(target.transientLongValue != 1);
  }

  public void testClassWithNoFields() {
    String json = "{}";
    ClassWithNoFields target = gson.fromJson(json, ClassWithNoFields.class);
    assertNotNull(target);
  }

  public void testTopLevelCollections() {
    Type type = new TypeToken<Collection<Integer>>() {
    }.getType();
    Collection<Integer> collection = gson.fromJson("[1,2,3,4,5,6,7,8,9]", type);
    assertEquals(9, collection.size());
  }

  public void testTopLevelArray() {
    int[] expected = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    int[] actual = gson.fromJson("[1,2,3,4,5,6,7,8,9]", int[].class);
    MoreAsserts.assertEquals(expected, actual);
  }

  public void testEmptyArray() {
    int[] actualObject = gson.fromJson("[]", int[].class);
    assertTrue(actualObject.length == 0);

    Integer[] actualObject2 = gson.fromJson("[]", Integer[].class);
    assertTrue(actualObject2.length == 0);
  }

  public void testNested() {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false,"
        + "\"stringValue\":\"stringValue\"},\"primitive2\":{\"longValue\":30,\"intValue\":40,"
        + "\"booleanValue\":true,\"stringValue\":\"stringValue\"}}";
    Nested target = gson.fromJson(json, Nested.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testInheritence() {
    String json = "{\"value\":5,\"primitive1\":{\"longValue\":10,\"intValue\":20,"
        + "\"booleanValue\":false,\"stringValue\":\"stringValue\"},\"primitive2\":"
        + "{\"longValue\":30,\"intValue\":40,\"booleanValue\":true,"
        + "\"stringValue\":\"stringValue\"}}";
    SubTypeOfNested target = gson.fromJson(json, SubTypeOfNested.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testNull() {
    try {
      gson.fromJson("", Object.class);
      fail("Null strings should not be allowed");
    } catch (JsonParseException expected) {
    }
  }

  public void testNullFields() {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false"
        + ",\"stringValue\":\"stringValue\"}}";
    Nested target = gson.fromJson(json, Nested.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testSubInterfacesOfCollection() {
    String json = "{\"list\":[0,1,2,3],\"queue\":[0,1,2,3],\"set\":[0.1,0.2,0.3,0.4],"
        + "\"sortedSet\":[\"a\",\"b\",\"c\",\"d\"]"
//        + ",\"navigableSet\":[\"abc\",\"def\",\"ghi\",\"jkl\"]"
        + "}";
    ClassWithSubInterfacesOfCollection target = gson.fromJson(
        json, ClassWithSubInterfacesOfCollection.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testCustomDeserializer() {
    Gson gson = new GsonBuilder().registerDeserializer(
        ClassWithCustomTypeConverter.class, new JsonDeserializer<ClassWithCustomTypeConverter>() {
      public ClassWithCustomTypeConverter deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        int value = jsonObject.get("bag").getAsInt();
        return new ClassWithCustomTypeConverter(new BagOfPrimitives(value,
            value, false, ""), value);
      }
    }).create();
    String json = "{\"bag\":5,\"value\":25}";
    ClassWithCustomTypeConverter target = gson.fromJson(json, ClassWithCustomTypeConverter.class);
    assertEquals(5, target.getBag().getIntValue());
  }

  public void testNestedCustomTypeConverters() {
    Gson gson = new GsonBuilder().registerDeserializer(
        BagOfPrimitives.class, new JsonDeserializer<BagOfPrimitives>() {
      public BagOfPrimitives deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        int value = json.getAsInt();
        return new BagOfPrimitives(value, value, false, "");
      }
    }).create();
    String json = "{\"bag\":7,\"value\":25}";
    ClassWithCustomTypeConverter target = gson.fromJson(json, ClassWithCustomTypeConverter.class);
    assertEquals(7, target.getBag().getIntValue());
  }

  public void testArrayOfObjects() {
    String json = new ArrayOfObjects().getExpectedJson();
    ArrayOfObjects target = gson.fromJson(json, ArrayOfObjects.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfArrays() {
    String json = new ArrayOfArrays().getExpectedJson();
    ArrayOfArrays target = gson.fromJson(json, ArrayOfArrays.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testTopLevelEnum() {
    Gson gson = new GsonBuilder()
        .registerInstanceCreator(MyEnum.class, new MyEnumCreator())
        .create();
    String json = MyEnum.VALUE1.getExpectedJson();
    MyEnum target = gson.fromJson(json, MyEnum.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testTopLevelEnumInASingleElementArray() {
    Gson gson = new GsonBuilder()
        .registerInstanceCreator(MyEnum.class, new MyEnumCreator())
        .create();
    String json = "[" + MyEnum.VALUE1.getExpectedJson() + "]";
    MyEnum target = gson.fromJson(json, MyEnum.class);
    assertEquals(json, "[" + target.getExpectedJson() + "]");
  }

  public void testClassWithEnumField() {
    Gson gson = new GsonBuilder()
        .registerInstanceCreator(MyEnum.class, new MyEnumCreator())
        .create();
    String json = new ClassWithEnumFields().getExpectedJson();
    ClassWithEnumFields target = gson.fromJson(json, ClassWithEnumFields.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testCollectionOfEnums() {
    Gson gson = new GsonBuilder()
        .registerInstanceCreator(MyEnum.class, new MyEnumCreator())
        .create();
    Type type = new TypeToken<Collection<MyEnum>>() {
    }.getType();
    String json = "[\"VALUE1\",\"VALUE2\"]";
    Collection<MyEnum> target = gson.fromJson(json, type);
    MoreAsserts.assertContains(target, MyEnum.VALUE1);
    MoreAsserts.assertContains(target, MyEnum.VALUE2);
  }

  public void testPrivateNoArgConstructor() {
    ClassWithPrivateNoArgsConstructor target =
      gson.fromJson("{\"a\":20}", ClassWithPrivateNoArgsConstructor.class);
    assertEquals(20, target.a);
  }

  public void testDefaultSupportForUrl() throws Exception {
    String urlValue = "http://google.com/";
    String json = '"' + urlValue + '"';
    URL target = gson.fromJson(json, URL.class);
    assertEquals(urlValue, target.toExternalForm());
  }

  public void testDefaultSupportForUri() throws Exception {
    String uriValue = "http://google.com/";
    String json = '"' + uriValue + '"';
    URI target = gson.fromJson(json, URI.class);
    assertEquals(uriValue, target.toASCIIString());
  }

  public void testDefaultSupportForLocaleWithLanguage() throws Exception {
    String json = "\"en\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("en", locale.getLanguage());
  }

  public void testDefaultSupportForLocaleWithLanguageCountry() throws Exception {
    String json = "\"fr_CA\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals(Locale.CANADA_FRENCH, locale);
  }

  public void testDefaultSupportForLocaleWithLanguageCountryVariant() throws Exception {
    String json = "\"de_DE_EURO\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("de", locale.getLanguage());
    assertEquals("DE", locale.getCountry());
    assertEquals("EURO", locale.getVariant());
  }

  public void testMap() throws Exception {
    String json = "{\"a\":1,\"b\":2}";
    Type typeOfMap = new TypeToken<Map<String,Integer>>(){}.getType();
    Map<String, Integer> target = gson.fromJson(json, typeOfMap);
    assertEquals(1, target.get("a").intValue());
    assertEquals(2, target.get("b").intValue());
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullArrays() {
    String json = "{\"array\": null}";
    ClassWithArray target = gson.fromJson(json, ClassWithArray.class);
    assertNull(target.array);
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullObjectFields() {
    String json = "{\"bag\": null}";
    ClassWithObjects target = gson.fromJson(json, ClassWithObjects.class);
    assertNull(target.bag);
  }

  public void testArrayWithNulls() {
    String json = "[\"foo\",null,\"bar\"]";
    String[] expected = {"foo", null, "bar"};
    String[] target = gson.fromJson(json, expected.getClass());
    for (int i = 0; i < expected.length; ++i) {
      assertEquals(expected[i], target[i]);
    }
  }

  public void testListsWithNulls() {
    List<String> expected = new ArrayList<String>();
    expected.add("foo");
    expected.add(null);
    expected.add("bar");
    String json = "[\"foo\",null,\"bar\"]";
    Type expectedType = new TypeToken<List<String>>() {}.getType();
    List<String> target = gson.fromJson(json, expectedType);
    for (int i = 0; i < expected.size(); ++i) {
      assertEquals(expected.get(i), target.get(i));
    }
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullPrimitiveFields() {
    String json = "{\"longValue\":null}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(BagOfPrimitives.DEFAULT_VALUE, target.longValue);
  }

  public void testGsonWithNonDefaultFieldNamingPolicy() {
    Gson gson = new GsonBuilder().setFieldNamingPolicy(
        FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    StringWrapper target = new StringWrapper("SomeValue");
    String jsonRepresentation = gson.toJson(target);
    StringWrapper deserializedObject = gson.fromJson(jsonRepresentation, StringWrapper.class);
    assertEquals(target.someConstantStringInstanceField,
        deserializedObject.someConstantStringInstanceField);
  }

  public void testGsonWithSerializedNameFieldNamingPolicy() {
    ClassWithSerializedNameFields expected = new ClassWithSerializedNameFields(5);
    ClassWithSerializedNameFields actual =
        gson.fromJson(expected.getExpectedJson(), ClassWithSerializedNameFields.class);
    assertEquals(expected.f, actual.f);
  }

  public void testReallyLongStrings() {
    StringBuilder sb = new StringBuilder(8096);
    sb.append("Once upon a time there was a really long string that caused a StackOverFlowError\n");
    sb.append("and now it is fixed and instead throws a JsonParserException.....Yippie!!!\n");
    sb.append("Wow....that is a really long string that is mean to be an exception, but is not :( \n.");
    sb.append("C'est la vie!!! \n\n\n\n\n");

    for (int i = 0; i < 10; i++) {
      sb.append(sb.toString());
    }

    while (true) {
      try {
        String stackTrace = sb.toString();
        sb.append(stackTrace);
        String json = "{\"message\":\"Error message.\","
          + "\"stackTrace\":\"" + stackTrace + "\"}";
        parseLongJson(json);
      } catch (JsonParseException expected) {
        break;
      }
    }
  }

  private void parseLongJson(String json) throws JsonParseException {
    ExceptionHolder target = gson.fromJson(json, ExceptionHolder.class);
    assertTrue(target.message.contains("Error"));
    assertTrue(target.stackTrace.contains("Yippie"));
  }
}
