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

package com.economic.persistgson.functional;

import com.economic.persistgson.Gson;
import com.economic.persistgson.JsonElement;
import com.economic.persistgson.JsonObject;
import com.economic.persistgson.GsonBuilder;
import com.economic.persistgson.InstanceCreator;
import com.economic.persistgson.JsonParseException;
import com.economic.persistgson.JsonSerializationContext;
import com.economic.persistgson.JsonSerializer;
import com.economic.persistgson.common.TestTypes;
import com.economic.persistgson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import junit.framework.TestCase;

/**
 * Functional tests for Json serialization and deserialization of regular classes.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ObjectTest extends TestCase {
  private Gson gson;
  private TimeZone oldTimeZone = TimeZone.getDefault();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();

    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale.setDefault(Locale.US);
  }

  @Override
  protected void tearDown() throws Exception {
    TimeZone.setDefault(oldTimeZone);
    super.tearDown();
  }
  public void testJsonInSingleQuotesDeserialization() {
    String json = "{'stringValue':'no message','intValue':10,'longValue':20}";
    TestTypes.BagOfPrimitives target = gson.fromJson(json, TestTypes.BagOfPrimitives.class);
    assertEquals("no message", target.stringValue);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
  }

  public void testJsonInMixedQuotesDeserialization() {
    String json = "{\"stringValue\":'no message','intValue':10,'longValue':20}";
    TestTypes.BagOfPrimitives target = gson.fromJson(json, TestTypes.BagOfPrimitives.class);
    assertEquals("no message", target.stringValue);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
  }

  public void testBagOfPrimitivesSerialization() throws Exception {
    TestTypes.BagOfPrimitives target = new TestTypes.BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testBagOfPrimitivesDeserialization() throws Exception {
    TestTypes.BagOfPrimitives src = new TestTypes.BagOfPrimitives(10, 20, false, "stringValue");
    String json = src.getExpectedJson();
    TestTypes.BagOfPrimitives target = gson.fromJson(json, TestTypes.BagOfPrimitives.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testBagOfPrimitiveWrappersSerialization() throws Exception {
    TestTypes.BagOfPrimitiveWrappers target = new TestTypes.BagOfPrimitiveWrappers(10L, 20, false);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testBagOfPrimitiveWrappersDeserialization() throws Exception {
    TestTypes.BagOfPrimitiveWrappers target = new TestTypes.BagOfPrimitiveWrappers(10L, 20, false);
    String jsonString = target.getExpectedJson();
    target = gson.fromJson(jsonString, TestTypes.BagOfPrimitiveWrappers.class);
    assertEquals(jsonString, target.getExpectedJson());
  }

  public void testClassWithTransientFieldsSerialization() throws Exception {
    TestTypes.ClassWithTransientFields<Long> target = new TestTypes.ClassWithTransientFields<Long>(1L);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  @SuppressWarnings("rawtypes")
  public void testClassWithTransientFieldsDeserialization() throws Exception {
    String json = "{\"longValue\":[1]}";
    TestTypes.ClassWithTransientFields target = gson.fromJson(json, TestTypes.ClassWithTransientFields.class);
    assertEquals(json, target.getExpectedJson());
  }

  @SuppressWarnings("rawtypes")
  public void testClassWithTransientFieldsDeserializationTransientFieldsPassedInJsonAreIgnored()
      throws Exception {
    String json = "{\"transientLongValue\":1,\"longValue\":[1]}";
    TestTypes.ClassWithTransientFields target = gson.fromJson(json, TestTypes.ClassWithTransientFields.class);
    assertFalse(target.transientLongValue != 1);
  }

  public void testClassWithNoFieldsSerialization() throws Exception {
    assertEquals("{}", gson.toJson(new TestTypes.ClassWithNoFields()));
  }

  public void testClassWithNoFieldsDeserialization() throws Exception {
    String json = "{}";
    TestTypes.ClassWithNoFields target = gson.fromJson(json, TestTypes.ClassWithNoFields.class);
    TestTypes.ClassWithNoFields expected = new TestTypes.ClassWithNoFields();
    assertEquals(expected, target);
  }

  public void testNestedSerialization() throws Exception {
    TestTypes.Nested target = new TestTypes.Nested(new TestTypes.BagOfPrimitives(10, 20, false, "stringValue"),
       new TestTypes.BagOfPrimitives(30, 40, true, "stringValue"));
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testNestedDeserialization() throws Exception {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false,"
        + "\"stringValue\":\"stringValue\"},\"primitive2\":{\"longValue\":30,\"intValue\":40,"
        + "\"booleanValue\":true,\"stringValue\":\"stringValue\"}}";
    TestTypes.Nested target = gson.fromJson(json, TestTypes.Nested.class);
    assertEquals(json, target.getExpectedJson());
  }
  public void testNullSerialization() throws Exception {
    assertEquals("null", gson.toJson(null));
  }

  public void testEmptyStringDeserialization() throws Exception {
    Object object = gson.fromJson("", Object.class);
    assertNull(object);
  }

  public void testTruncatedDeserialization() {
    try {
      gson.fromJson("[\"a\", \"b\",", new TypeToken<List<String>>() {}.getType());
      fail();
    } catch (JsonParseException expected) {
    }
  }

  public void testNullDeserialization() throws Exception {
    String myNullObject = null;
    Object object = gson.fromJson(myNullObject, Object.class);
    assertNull(object);
  }

  public void testNullFieldsSerialization() throws Exception {
    TestTypes.Nested target = new TestTypes.Nested(new TestTypes.BagOfPrimitives(10, 20, false, "stringValue"), null);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testNullFieldsDeserialization() throws Exception {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false"
        + ",\"stringValue\":\"stringValue\"}}";
    TestTypes.Nested target = gson.fromJson(json, TestTypes.Nested.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfObjectsSerialization() throws Exception {
    TestTypes.ArrayOfObjects target = new TestTypes.ArrayOfObjects();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testArrayOfObjectsDeserialization() throws Exception {
    String json = new TestTypes.ArrayOfObjects().getExpectedJson();
    TestTypes.ArrayOfObjects target = gson.fromJson(json, TestTypes.ArrayOfObjects.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfArraysSerialization() throws Exception {
    ArrayOfArrays target = new ArrayOfArrays();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testArrayOfArraysDeserialization() throws Exception {
    String json = new ArrayOfArrays().getExpectedJson();
    ArrayOfArrays target = gson.fromJson(json, ArrayOfArrays.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfObjectsAsFields() throws Exception {
    TestTypes.ClassWithObjects classWithObjects = new TestTypes.ClassWithObjects();
    TestTypes.BagOfPrimitives bagOfPrimitives = new TestTypes.BagOfPrimitives();
    String stringValue = "someStringValueInArray";
    String classWithObjectsJson = gson.toJson(classWithObjects);
    String bagOfPrimitivesJson = gson.toJson(bagOfPrimitives);

    TestTypes.ClassWithArray classWithArray = new TestTypes.ClassWithArray(
        new Object[] { stringValue, classWithObjects, bagOfPrimitives });
    String json = gson.toJson(classWithArray);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
    assertTrue(json.contains("\"" + stringValue + "\""));
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullArraysDeserialization() throws Exception {
    String json = "{\"array\": null}";
    TestTypes.ClassWithArray target = gson.fromJson(json, TestTypes.ClassWithArray.class);
    assertNull(target.array);
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullObjectFieldsDeserialization() throws Exception {
    String json = "{\"bag\": null}";
    TestTypes.ClassWithObjects target = gson.fromJson(json, TestTypes.ClassWithObjects.class);
    assertNull(target.bag);
  }

  public void testEmptyCollectionInAnObjectDeserialization() throws Exception {
    String json = "{\"children\":[]}";
    ClassWithCollectionField target = gson.fromJson(json, ClassWithCollectionField.class);
    assertNotNull(target);
    assertTrue(target.children.isEmpty());
  }

  private static class ClassWithCollectionField {
    Collection<String> children = new ArrayList<String>();
  }

  public void testPrimitiveArrayInAnObjectDeserialization() throws Exception {
    String json = "{\"longArray\":[0,1,2,3,4,5,6,7,8,9]}";
    TestTypes.PrimitiveArray target = gson.fromJson(json, TestTypes.PrimitiveArray.class);
    assertEquals(json, target.getExpectedJson());
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullPrimitiveFieldsDeserialization() throws Exception {
    String json = "{\"longValue\":null}";
    TestTypes.BagOfPrimitives target = gson.fromJson(json, TestTypes.BagOfPrimitives.class);
    assertEquals(TestTypes.BagOfPrimitives.DEFAULT_VALUE, target.longValue);
  }

  public void testEmptyCollectionInAnObjectSerialization() throws Exception {
    ClassWithCollectionField target = new ClassWithCollectionField();
    assertEquals("{\"children\":[]}", gson.toJson(target));
  }

  public void testPrivateNoArgConstructorDeserialization() throws Exception {
    ClassWithPrivateNoArgsConstructor target =
      gson.fromJson("{\"a\":20}", ClassWithPrivateNoArgsConstructor.class);
    assertEquals(20, target.a);
  }

  public void testAnonymousLocalClassesSerialization() throws Exception {
    assertEquals("null", gson.toJson(new TestTypes.ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  public void testAnonymousLocalClassesCustomSerialization() throws Exception {
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(TestTypes.ClassWithNoFields.class,
            new JsonSerializer<TestTypes.ClassWithNoFields>() {
              public JsonElement serialize(
                      TestTypes.ClassWithNoFields src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonObject();
              }
            }).create();

    assertEquals("null", gson.toJson(new TestTypes.ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  public void testPrimitiveArrayFieldSerialization() {
    TestTypes.PrimitiveArray target = new TestTypes.PrimitiveArray(new long[] { 1L, 2L, 3L });
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  /**
   * Tests that a class field with type Object can be serialized properly.
   * See issue 54
   */
  public void testClassWithObjectFieldSerialization() {
    ClassWithObjectField obj = new ClassWithObjectField();
    obj.member = "abc";
    String json = gson.toJson(obj);
    assertTrue(json.contains("abc"));
  }

  private static class ClassWithObjectField {
    @SuppressWarnings("unused")
    Object member;
  }

  public void testInnerClassSerialization() {
    Parent p = new Parent();
    Parent.Child c = p.new Child();
    String json = gson.toJson(c);
    assertTrue(json.contains("value2"));
    assertFalse(json.contains("value1"));
  }

  public void testInnerClassDeserialization() {
    final Parent p = new Parent();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        Parent.Child.class, new InstanceCreator<Parent.Child>() {
      public Parent.Child createInstance(Type type) {
        return p.new Child();
      }
    }).create();
    String json = "{'value2':3}";
    Parent.Child c = gson.fromJson(json, Parent.Child.class);
    assertEquals(3, c.value2);
  }

  private static class Parent {
    @SuppressWarnings("unused")
    int value1 = 1;
    private class Child {
      int value2 = 2;
    }
  }

  private static class ArrayOfArrays {
    private final TestTypes.BagOfPrimitives[][] elements;
    public ArrayOfArrays() {
      elements = new TestTypes.BagOfPrimitives[3][2];
      for (int i = 0; i < elements.length; ++i) {
        TestTypes.BagOfPrimitives[] row = elements[i];
        for (int j = 0; j < row.length; ++j) {
          row[j] = new TestTypes.BagOfPrimitives(i+j, i*j, false, i+"_"+j);
        }
      }
    }
    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder("{\"elements\":[");
      boolean first = true;
      for (TestTypes.BagOfPrimitives[] row : elements) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        boolean firstOfRow = true;
        sb.append("[");
        for (TestTypes.BagOfPrimitives element : row) {
          if (firstOfRow) {
            firstOfRow = false;
          } else {
            sb.append(",");
          }
          sb.append(element.getExpectedJson());
        }
        sb.append("]");
      }
      sb.append("]}");
      return sb.toString();
    }
  }

  private static class ClassWithPrivateNoArgsConstructor {
    public int a;
    private ClassWithPrivateNoArgsConstructor() {
      a = 10;
    }
  }

  /**
   * In response to Issue 41 http://code.google.com/p/google-gson/issues/detail?id=41
   */
  public void testObjectFieldNamesWithoutQuotesDeserialization() {
    String json = "{longValue:1,'booleanValue':true,\"stringValue\":'bar'}";
    TestTypes.BagOfPrimitives bag = gson.fromJson(json, TestTypes.BagOfPrimitives.class);
    assertEquals(1, bag.longValue);
    assertTrue(bag.booleanValue);
    assertEquals("bar", bag.stringValue);
  }

  public void testStringFieldWithNumberValueDeserialization() {
    String json = "{\"stringValue\":1}";
    TestTypes.BagOfPrimitives bag = gson.fromJson(json, TestTypes.BagOfPrimitives.class);
    assertEquals("1", bag.stringValue);

    json = "{\"stringValue\":1.5E+6}";
    bag = gson.fromJson(json, TestTypes.BagOfPrimitives.class);
    assertEquals("1.5E+6", bag.stringValue);

    json = "{\"stringValue\":true}";
    bag = gson.fromJson(json, TestTypes.BagOfPrimitives.class);
    assertEquals("true", bag.stringValue);
  }

  /**
   * Created to reproduce issue 140
   */
  public void testStringFieldWithEmptyValueSerialization() {
    ClassWithEmptyStringFields target = new ClassWithEmptyStringFields();
    target.a = "5794749";
    String json = gson.toJson(target);
    assertTrue(json.contains("\"a\":\"5794749\""));
    assertTrue(json.contains("\"b\":\"\""));
    assertTrue(json.contains("\"c\":\"\""));
  }

  /**
   * Created to reproduce issue 140
   */
  public void testStringFieldWithEmptyValueDeserialization() {
    String json = "{a:\"5794749\",b:\"\",c:\"\"}";
    ClassWithEmptyStringFields target = gson.fromJson(json, ClassWithEmptyStringFields.class);
    assertEquals("5794749", target.a);
    assertEquals("", target.b);
    assertEquals("", target.c);
  }

  private static class ClassWithEmptyStringFields {
    String a = "";
    String b = "";
    String c = "";
  }

  public void testJsonObjectSerialization() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    JsonObject obj = new JsonObject();
    String json = gson.toJson(obj);
    assertEquals("{}", json);
  }

  /**
   * Test for issue 215.
   */
  public void testSingletonLists() {
    Gson gson = new Gson();
    Product product = new Product();
    assertEquals("{\"attributes\":[],\"departments\":[]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);

    product.departments.add(new Department());
    assertEquals("{\"attributes\":[],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);

    product.attributes.add("456");
    assertEquals("{\"attributes\":[\"456\"],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}",
        gson.toJson(product));
    gson.fromJson(gson.toJson(product), Product.class);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=270
  public void testDateAsMapObjectField() {
    HasObjectMap a = new HasObjectMap();
    a.map.put("date", new Date(0));
    assertEquals("{\"map\":{\"date\":\"Dec 31, 1969 4:00:00 PM\"}}", gson.toJson(a));
  }

  public class HasObjectMap {
    Map<String, Object> map = new HashMap<String, Object>();
  }

  static final class Department {
    public String name = "abc";
    public String code = "123";
  }

  static final class Product {
    private List<String> attributes = new ArrayList<String>();
    private List<Department> departments = new ArrayList<Department>();
  }
}
