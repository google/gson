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
import com.google.gson.TestTypes.ClassOverridingEquals;
import com.google.gson.TestTypes.ClassWithCustomTypeConverter;
import com.google.gson.TestTypes.ClassWithEnumFields;
import com.google.gson.TestTypes.ClassWithExposedFields;
import com.google.gson.TestTypes.ClassWithNoFields;
import com.google.gson.TestTypes.ClassWithSubInterfacesOfCollection;
import com.google.gson.TestTypes.ClassWithTransientFields;
import com.google.gson.TestTypes.ContainsReferenceToSelfType;
import com.google.gson.TestTypes.MyEnum;
import com.google.gson.TestTypes.MyParameterizedType;
import com.google.gson.TestTypes.Nested;
import com.google.gson.TestTypes.PrimitiveArray;
import com.google.gson.TestTypes.SubTypeOfNested;
import com.google.gson.annotations.Since;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

  public void testCircular() {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    a.children.add(b);
    b.children.add(a);
    try {
      gson.toJson(a);
      fail("Circular types should not get printed!");
    } catch (IllegalStateException expected) { }
  }

  public void testSelfReference() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    objA.ref = objA;

    try {
      gson.toJson(objA);
      fail("Circular reference to self can not be serialized!");
    } catch (IllegalStateException expected) { }
  }

  public void testObjectEqualButNotSame() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    ClassOverridingEquals objB = new ClassOverridingEquals();
    objB.ref = objA;

    assertEquals(objB.getExpectedJson(), gson.toJson(objB));
  }

  public void testDirectedAcyclicGraph() {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType c = new ContainsReferenceToSelfType();
    a.children.add(b);
    a.children.add(c);
    b.children.add(c);
    assertNotNull(gson.toJson(a));
  }

  public void testClassWithTransientFields() throws Exception {
    ClassWithTransientFields target = new ClassWithTransientFields(1L);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testClassWithNoFields() {
    assertEquals("{}", gson.toJson(new ClassWithNoFields()));
  }

  public void testAnonymousLocalClasses() {
    assertEquals("", gson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    }));

    gson = new Gson(new ObjectNavigatorFactory(new ModifierBasedExclusionStrategy(
        true, Modifier.TRANSIENT, Modifier.STATIC)));
    assertEquals("{}", gson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  public void testStringValue() throws Exception {
    String value = "someRandomStringValue";
    assertEquals('"' + value + '"', gson.toJson(value));
  }

  public void testPrimitiveIntegerAutoboxed() {
    assertEquals("1", gson.toJson(1));
  }

  public void testPrimitiveBooleanAutoboxed() {
    assertEquals("true", gson.toJson(true));
    assertEquals("false", gson.toJson(false));
  }

  public void testPrimitiveDoubleAutoboxed() {
    assertEquals("-122.08234335", gson.toJson(-122.08234335));
    assertEquals("122.08112002", gson.toJson(new Double(122.08112002)));
  }

  public void testArrayOfOneValue() {
    int target[] = {1};
    assertEquals("[1]", gson.toJson(target));
  }

  public void testBagOfPrimitives() {
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testBagOfPrimitiveWrappers() {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testPrimitiveArrayField() throws Exception {
    PrimitiveArray target = new PrimitiveArray(new long[] { 1L, 2L, 3L });
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testCollection() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    Type targetType = new TypeToken<Collection<Integer>>() {}.getType();
    String json = gson.toJson(target, targetType);
    assertEquals("[1,2,3,4,5,6,7,8,9]", json);
  }

  public void testCollectionWithoutSpecifyingType() {
    Collection<Integer> target = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals("[1,2,3,4,5,6,7,8,9]", gson.toJson(target));
  }

  public void testEmptyArray() {
    int[] target = {};
    assertEquals("[]", gson.toJson(target));
  }

  public void testEmptyCollectionInAnObject() {
    ContainsReferenceToSelfType target = new ContainsReferenceToSelfType();
    assertEquals("{\"children\":[]}", gson.toJson(target));
  }

  public void testArrayOfInts() {
    int[] target = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    assertEquals("[1,2,3,4,5,6,7,8,9]", gson.toJson(target));
  }

  public void testArrayOfObjects() {
    ArrayOfObjects target = new ArrayOfObjects();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testArrayOfStrings() {
    String[] target = {"Hello", "World"};
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
  }

  public void testArrayOfArrays() {
    ArrayOfArrays target = new ArrayOfArrays();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testCollectionOfStrings() {
    List<String> target = new ArrayList<String>();
    target.add("Hello");
    target.add("World");
    assertEquals("[\"Hello\",\"World\"]", gson.toJson(target));
  }

  public void testCollectionOfObjects() {
    List<BagOfPrimitives> target = new ArrayList<BagOfPrimitives>();
    BagOfPrimitives objA = new BagOfPrimitives(3L, 1, true, "blah");
    BagOfPrimitives objB = new BagOfPrimitives(2L, 6, false, "blahB");
    target.add(objA);
    target.add(objB);

    String result = gson.toJson(target);
    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
    for (BagOfPrimitives obj : target) {
      assertTrue(result.contains(obj.getExpectedJson()));
    }
  }

  public void testNested() {
    Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"),
       new BagOfPrimitives(30, 40, true, "stringValue"));
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testInheritence() {
    SubTypeOfNested target = new SubTypeOfNested(new BagOfPrimitives(10, 20, false, "stringValue"),
        new BagOfPrimitives(30, 40, true, "stringValue"));
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testNull() {
    assertEquals("", gson.toJson(null));
  }

  public void testNullFields() {
    Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"), null);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }
  
  public void testArrayWithNulls() {
    String[] array = {"foo", null, "bar"};
	String expected = "[\"foo\",null,\"bar\"]";
	String json = gson.toJson(array);
	assertEquals(expected, json);
  }

  public void testListsWithNulls() {
    List<String> list = new ArrayList<String>();
    list.add("foo");
    list.add(null);
    list.add("bar");
    String expected = "[\"foo\",null,\"bar\"]";
    Type typeOfList = new TypeToken<List<String>>() {}.getType();
    String json = gson.toJson(list, typeOfList);
    assertEquals(expected, json);
  }

  public void testSubInterfacesOfCollection() {
    List<Integer> list = new LinkedList<Integer>();
    list.add(0);
    list.add(1);
    list.add(2);
    list.add(3);
    Queue<Long> queue = new LinkedList<Long>();
    queue.add(0L);
    queue.add(1L);
    queue.add(2L);
    queue.add(3L);
    Set<Float> set = new TreeSet<Float>();
    set.add(0.1F);
    set.add(0.2F);
    set.add(0.3F);
    set.add(0.4F);
    SortedSet<Character> sortedSet = new TreeSet<Character>();
    sortedSet.add('a');
    sortedSet.add('b');
    sortedSet.add('c');
    sortedSet.add('d');
    ClassWithSubInterfacesOfCollection target =
        new ClassWithSubInterfacesOfCollection(list, queue, set, sortedSet);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testCustomSerializers() {
    gson.registerSerializer(ClassWithCustomTypeConverter.class,
        new JsonSerializer<ClassWithCustomTypeConverter>() {
      public JsonElement serialize(ClassWithCustomTypeConverter src, Type typeOfSrc,
          JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("bag", 5);
        json.addProperty("value", 25);
        return json;
      }
    });
    ClassWithCustomTypeConverter target = new ClassWithCustomTypeConverter();
    assertEquals("{\"bag\":5,\"value\":25}", gson.toJson(target));
  }

  public void testNestedCustomSerializers() {
    gson.registerSerializer(BagOfPrimitives.class, new JsonSerializer<BagOfPrimitives>() {
      public JsonElement serialize(BagOfPrimitives src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive(6);
      }
    });
    ClassWithCustomTypeConverter target = new ClassWithCustomTypeConverter();
    assertEquals("{\"bag\":6,\"value\":10}", gson.toJson(target));
  }

  public void testStaticFieldsAreNotSerialized() {
    BagOfPrimitives target = new BagOfPrimitives();
    assertFalse(gson.toJson(target).contains("DEFAULT_VALUE"));
  }

  private static class MyParameterizedSerializer<T>
      implements JsonSerializer<MyParameterizedType<T>> {
    public JsonElement serialize(MyParameterizedType<T> src, Type classOfSrc,
        JsonSerializationContext context) {
      JsonObject json = new JsonObject();
      T value = src.getValue();
      json.add(value.getClass().getSimpleName(), context.serialize(value));
      return json;
    }
  }

  public void testParameterizedTypeWithCustomSerializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    gson.registerSerializer(ptIntegerType, new MyParameterizedSerializer<Integer>());
    gson.registerSerializer(ptStringType, new MyParameterizedSerializer<String>());
    MyParameterizedType<Integer> intTarget = new MyParameterizedType<Integer>(10);
    assertEquals(intTarget.getExpectedJson(), gson.toJson(intTarget, ptIntegerType));

    MyParameterizedType<String> stringTarget = new MyParameterizedType<String>("abc");
    assertEquals(stringTarget.getExpectedJson(), gson.toJson(stringTarget, ptStringType));
  }

  public void testTopLevelEnum() {
    MyEnum target = MyEnum.VALUE1;
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testClassWithEnumField() {
    ClassWithEnumFields target = new ClassWithEnumFields();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
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

  public void testDefaultSupportForUrl() throws Exception {
    String urlValue = "http://google.com/";
    URL url = new URL(urlValue);
    assertEquals('"' + urlValue + '"', gson.toJson(url));
  }

  public void testDefaultSupportForUri() throws Exception {
    String uriValue = "http://google.com/";
    URI uri = new URI(uriValue);
    assertEquals('"' + uriValue + '"', gson.toJson(uri));
  }

  public void testDefaultSupportForLocaleWithLanguage() throws Exception {
    Locale target = new Locale("en");
    assertEquals("\"en\"", gson.toJson(target));
  }

  public void testDefaultSupportForLocaleWithLanguageCountry() throws Exception {
    Locale target = Locale.CANADA_FRENCH;
    assertEquals("\"fr_CA\"", gson.toJson(target));
  }

  public void testDefaultSupportForLocaleWithLanguageCountryVariant() throws Exception {
    Locale target = new Locale("de", "DE", "EURO");
    String json = gson.toJson(target);
    assertEquals("\"de_DE_EURO\"", json);
  }

  public void testMap() throws Exception {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("a", 1);
    map.put("b", 2);
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);
    assertTrue(json.contains("\"a\":1"));
    assertTrue(json.contains("\"b\":2"));
  }

  public void testExposeAnnotation() throws Exception {
    // First test that Gson works without the expose annotation as well
    ClassWithExposedFields target = new ClassWithExposedFields();
    assertEquals(target.getExpectedJsonWithoutAnnotations(), gson.toJson(target));

    // Now recreate gson with the proper setting
    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testSingleQuoteInStrings() throws Exception {
    String valueWithQuotes = "beforeQuote'afterQuote";
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    assertEquals(valueWithQuotes, gson.fromJson(jsonRepresentation, String.class));
  }

  public void testEscapingQuotesInStrings() throws Exception {
    String valueWithQuotes = "beforeQuote\"afterQuote";
    String jsonRepresentation = gson.toJson(valueWithQuotes);
    String target = gson.fromJson(jsonRepresentation, String.class);
    assertEquals(valueWithQuotes, target);
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
}
