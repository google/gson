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
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.common.TestTypes.ArrayOfObjects;
import com.google.gson.common.TestTypes.BagOfPrimitiveWrappers;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassWithArray;
import com.google.gson.common.TestTypes.ClassWithNoFields;
import com.google.gson.common.TestTypes.ClassWithObjects;
import com.google.gson.common.TestTypes.ClassWithTransientFields;
import com.google.gson.common.TestTypes.Nested;
import com.google.gson.common.TestTypes.PrimitiveArray;
import com.google.gson.internal.JavaVersion;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for Json serialization and deserialization of regular classes.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ObjectTest {
  private Gson gson;
  private TimeZone oldTimeZone;
  private Locale oldLocale;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();

    oldTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    oldLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
  }

  @After
  public void tearDown() {
    TimeZone.setDefault(oldTimeZone);
    Locale.setDefault(oldLocale);
  }

  @Test
  public void testJsonInSingleQuotesDeserialization() {
    String json = "{'stringValue':'no message','intValue':10,'longValue':20}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(target.stringValue).isEqualTo("no message");
    assertThat(target.intValue).isEqualTo(10);
    assertThat(target.longValue).isEqualTo(20);
  }

  @Test
  public void testJsonInMixedQuotesDeserialization() {
    String json = "{\"stringValue\":'no message','intValue':10,'longValue':20}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(target.stringValue).isEqualTo("no message");
    assertThat(target.intValue).isEqualTo(10);
    assertThat(target.longValue).isEqualTo(20);
  }

  @Test
  public void testBagOfPrimitivesSerialization() {
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testBagOfPrimitivesDeserialization() {
    BagOfPrimitives src = new BagOfPrimitives(10, 20, false, "stringValue");
    String json = src.getExpectedJson();
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(target.getExpectedJson()).isEqualTo(json);
  }

  @Test
  public void testBagOfPrimitiveWrappersSerialization() {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testBagOfPrimitiveWrappersDeserialization() {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    String jsonString = target.getExpectedJson();
    target = gson.fromJson(jsonString, BagOfPrimitiveWrappers.class);
    assertThat(target.getExpectedJson()).isEqualTo(jsonString);
  }

  @Test
  public void testClassWithTransientFieldsSerialization() {
    ClassWithTransientFields<Long> target = new ClassWithTransientFields<>(1L);
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testClassWithTransientFieldsDeserialization() {
    String json = "{\"longValue\":[1]}";
    ClassWithTransientFields<?> target = gson.fromJson(json, ClassWithTransientFields.class);
    assertThat(target.getExpectedJson()).isEqualTo(json);
  }

  @Test
  public void testClassWithTransientFieldsDeserializationTransientFieldsPassedInJsonAreIgnored() {
    String json = "{\"transientLongValue\":1,\"longValue\":[1]}";
    ClassWithTransientFields<?> target = gson.fromJson(json, ClassWithTransientFields.class);
    assertThat(target.transientLongValue != 1).isFalse();
  }

  @Test
  public void testClassWithNoFieldsSerialization() {
    assertThat(gson.toJson(new ClassWithNoFields())).isEqualTo("{}");
  }

  @Test
  public void testClassWithNoFieldsDeserialization() {
    String json = "{}";
    ClassWithNoFields target = gson.fromJson(json, ClassWithNoFields.class);
    ClassWithNoFields expected = new ClassWithNoFields();
    assertThat(target).isEqualTo(expected);
  }

  private static class Subclass extends Superclass1 {
  }
  private static class Superclass1 extends Superclass2 {
    @SuppressWarnings("unused")
    String s;
  }
  private static class Superclass2 {
    @SuppressWarnings("unused")
    String s;
  }

  @Test
  public void testClassWithDuplicateFields() {
    try {
      gson.getAdapter(Subclass.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().isEqualTo("Class com.google.gson.functional.ObjectTest$Subclass declares multiple JSON fields named 's';"
          + " conflict is caused by fields com.google.gson.functional.ObjectTest$Superclass1#s and"
          + " com.google.gson.functional.ObjectTest$Superclass2#s");
    }
  }

  @Test
  public void testNestedSerialization() {
    Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"),
       new BagOfPrimitives(30, 40, true, "stringValue"));
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testNestedDeserialization() {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false,"
        + "\"stringValue\":\"stringValue\"},\"primitive2\":{\"longValue\":30,\"intValue\":40,"
        + "\"booleanValue\":true,\"stringValue\":\"stringValue\"}}";
    Nested target = gson.fromJson(json, Nested.class);
    assertThat(target.getExpectedJson()).isEqualTo(json);
  }
  @Test
  public void testNullSerialization() {
    assertThat(gson.toJson(null)).isEqualTo("null");
  }

  @Test
  public void testEmptyStringDeserialization() {
    Object object = gson.fromJson("", Object.class);
    assertThat(object).isNull();
  }

  @Test
  public void testTruncatedDeserialization() {
    try {
      gson.fromJson("[\"a\", \"b\",", new TypeToken<List<String>>() {}.getType());
      fail();
    } catch (JsonParseException expected) {
    }
  }

  @Test
  public void testNullDeserialization() {
    String myNullObject = null;
    Object object = gson.fromJson(myNullObject, Object.class);
    assertThat(object).isNull();
  }

  @Test
  public void testNullFieldsSerialization() {
    Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"), null);
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testNullFieldsDeserialization() {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false"
        + ",\"stringValue\":\"stringValue\"}}";
    Nested target = gson.fromJson(json, Nested.class);
    assertThat(target.getExpectedJson()).isEqualTo(json);
  }

  @Test
  public void testArrayOfObjectsSerialization() {
    ArrayOfObjects target = new ArrayOfObjects();
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testArrayOfObjectsDeserialization() {
    String json = new ArrayOfObjects().getExpectedJson();
    ArrayOfObjects target = gson.fromJson(json, ArrayOfObjects.class);
    assertThat(target.getExpectedJson()).isEqualTo(json);
  }

  @Test
  public void testArrayOfArraysSerialization() {
    ArrayOfArrays target = new ArrayOfArrays();
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testArrayOfArraysDeserialization() {
    String json = new ArrayOfArrays().getExpectedJson();
    ArrayOfArrays target = gson.fromJson(json, ArrayOfArrays.class);
    assertThat(target.getExpectedJson()).isEqualTo(json);
  }

  @Test
  public void testArrayOfObjectsAsFields() {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String stringValue = "someStringValueInArray";
    String classWithObjectsJson = gson.toJson(classWithObjects);
    String bagOfPrimitivesJson = gson.toJson(bagOfPrimitives);

    ClassWithArray classWithArray = new ClassWithArray(
        new Object[] { stringValue, classWithObjects, bagOfPrimitives });
    String json = gson.toJson(classWithArray);

    assertThat(json).contains(classWithObjectsJson);
    assertThat(json).contains(bagOfPrimitivesJson);
    assertThat(json).contains("\"" + stringValue + "\"");
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  @Test
  public void testNullArraysDeserialization() {
    String json = "{\"array\": null}";
    ClassWithArray target = gson.fromJson(json, ClassWithArray.class);
    assertThat(target.array).isNull();
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  @Test
  public void testNullObjectFieldsDeserialization() {
    String json = "{\"bag\": null}";
    ClassWithObjects target = gson.fromJson(json, ClassWithObjects.class);
    assertThat(target.bag).isNull();
  }

  @Test
  public void testEmptyCollectionInAnObjectDeserialization() {
    String json = "{\"children\":[]}";
    ClassWithCollectionField target = gson.fromJson(json, ClassWithCollectionField.class);
    assertThat(target).isNotNull();
    assertThat(target.children).isEmpty();
  }

  private static class ClassWithCollectionField {
    Collection<String> children = new ArrayList<>();
  }

  @Test
  public void testPrimitiveArrayInAnObjectDeserialization() {
    String json = "{\"longArray\":[0,1,2,3,4,5,6,7,8,9]}";
    PrimitiveArray target = gson.fromJson(json, PrimitiveArray.class);
    assertThat(target.getExpectedJson()).isEqualTo(json);
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  @Test
  public void testNullPrimitiveFieldsDeserialization() {
    String json = "{\"longValue\":null}";
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(target.longValue).isEqualTo(BagOfPrimitives.DEFAULT_VALUE);
  }

  @Test
  public void testEmptyCollectionInAnObjectSerialization() {
    ClassWithCollectionField target = new ClassWithCollectionField();
    assertThat(gson.toJson(target)).isEqualTo("{\"children\":[]}");
  }

  @Test
  public void testPrivateNoArgConstructorDeserialization() {
    ClassWithPrivateNoArgsConstructor target =
      gson.fromJson("{\"a\":20}", ClassWithPrivateNoArgsConstructor.class);
    assertThat(target.a).isEqualTo(20);
  }

  @Test
  public void testAnonymousLocalClassesSerialization() {
    assertThat(gson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    })).isEqualTo("null");
  }

  @Test
  public void testAnonymousLocalClassesCustomSerialization() {
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(ClassWithNoFields.class,
            new JsonSerializer<ClassWithNoFields>() {
              @Override public JsonElement serialize(
                  ClassWithNoFields src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonObject();
              }
            }).create();

    assertThat(gson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    })).isEqualTo("null");
  }

  @Test
  public void testPrimitiveArrayFieldSerialization() {
    PrimitiveArray target = new PrimitiveArray(new long[] { 1L, 2L, 3L });
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  /**
   * Tests that a class field with type Object can be serialized properly.
   * See issue 54
   */
  @Test
  public void testClassWithObjectFieldSerialization() {
    ClassWithObjectField obj = new ClassWithObjectField();
    obj.member = "abc";
    String json = gson.toJson(obj);
    assertThat(json).contains("abc");
  }

  private static class ClassWithObjectField {
    @SuppressWarnings("unused")
    Object member;
  }

  @Test
  public void testInnerClassSerialization() {
    Parent p = new Parent();
    Parent.Child c = p.new Child();
    String json = gson.toJson(c);
    assertThat(json).contains("value2");
    assertThat(json).doesNotContain("value1");
  }

  @Test
  public void testInnerClassDeserialization() {
    final Parent p = new Parent();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        Parent.Child.class, new InstanceCreator<Parent.Child>() {
      @Override public Parent.Child createInstance(Type type) {
        return p.new Child();
      }
    }).create();
    String json = "{'value2':3}";
    Parent.Child c = gson.fromJson(json, Parent.Child.class);
    assertThat(c.value2).isEqualTo(3);
  }

  private static class Parent {
    @SuppressWarnings("unused")
    int value1 = 1;
    private class Child {
      int value2 = 2;
    }
  }

  private static class ArrayOfArrays {
    private final BagOfPrimitives[][] elements;
    public ArrayOfArrays() {
      elements = new BagOfPrimitives[3][2];
      for (int i = 0; i < elements.length; ++i) {
        BagOfPrimitives[] row = elements[i];
        for (int j = 0; j < row.length; ++j) {
          row[j] = new BagOfPrimitives(i+j, i*j, false, i+"_"+j);
        }
      }
    }
    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder("{\"elements\":[");
      boolean first = true;
      for (BagOfPrimitives[] row : elements) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        boolean firstOfRow = true;
        sb.append("[");
        for (BagOfPrimitives element : row) {
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
  @Test
  public void testObjectFieldNamesWithoutQuotesDeserialization() {
    String json = "{longValue:1,'booleanValue':true,\"stringValue\":'bar'}";
    BagOfPrimitives bag = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(bag.longValue).isEqualTo(1);
    assertThat(bag.booleanValue).isTrue();
    assertThat(bag.stringValue).isEqualTo("bar");
  }

  @Test
  public void testStringFieldWithNumberValueDeserialization() {
    String json = "{\"stringValue\":1}";
    BagOfPrimitives bag = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(bag.stringValue).isEqualTo("1");

    json = "{\"stringValue\":1.5E+6}";
    bag = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(bag.stringValue).isEqualTo("1.5E+6");

    json = "{\"stringValue\":true}";
    bag = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(bag.stringValue).isEqualTo("true");
  }

  /**
   * Created to reproduce issue 140
   */
  @Test
  public void testStringFieldWithEmptyValueSerialization() {
    ClassWithEmptyStringFields target = new ClassWithEmptyStringFields();
    target.a = "5794749";
    String json = gson.toJson(target);
    assertThat(json).contains("\"a\":\"5794749\"");
    assertThat(json).contains("\"b\":\"\"");
    assertThat(json).contains("\"c\":\"\"");
  }

  /**
   * Created to reproduce issue 140
   */
  @Test
  public void testStringFieldWithEmptyValueDeserialization() {
    String json = "{a:\"5794749\",b:\"\",c:\"\"}";
    ClassWithEmptyStringFields target = gson.fromJson(json, ClassWithEmptyStringFields.class);
    assertThat(target.a).isEqualTo("5794749");
    assertThat(target.b).isEqualTo("");
    assertThat(target.c).isEqualTo("");
  }

  private static class ClassWithEmptyStringFields {
    String a = "";
    String b = "";
    String c = "";
  }

  @Test
  public void testJsonObjectSerialization() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    JsonObject obj = new JsonObject();
    String json = gson.toJson(obj);
    assertThat(json).isEqualTo("{}");
  }

  /**
   * Test for issue 215.
   */
  @Test
  public void testSingletonLists() {
    Gson gson = new Gson();
    Product product = new Product();
    assertThat(gson.toJson(product)).isEqualTo("{\"attributes\":[],\"departments\":[]}");
    gson.fromJson(gson.toJson(product), Product.class);

    product.departments.add(new Department());
    assertThat(gson.toJson(product))
        .isEqualTo("{\"attributes\":[],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}");
    gson.fromJson(gson.toJson(product), Product.class);

    product.attributes.add("456");
    assertThat(gson.toJson(product))
        .isEqualTo("{\"attributes\":[\"456\"],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}");
    gson.fromJson(gson.toJson(product), Product.class);
  }

  static final class Department {
    public String name = "abc";
    public String code = "123";
  }

  static final class Product {
    private List<String> attributes = new ArrayList<>();
    private List<Department> departments = new ArrayList<>();
  }

  // http://code.google.com/p/google-gson/issues/detail?id=270
  @Test
  public void testDateAsMapObjectField() {
    HasObjectMap a = new HasObjectMap();
    a.map.put("date", new Date(0));
    if (JavaVersion.isJava9OrLater()) {
      assertThat(gson.toJson(a)).isEqualTo("{\"map\":{\"date\":\"Dec 31, 1969, 4:00:00 PM\"}}");
    } else {
      assertThat(gson.toJson(a)).isEqualTo("{\"map\":{\"date\":\"Dec 31, 1969 4:00:00 PM\"}}");
    }
  }

  static class HasObjectMap {
    Map<String, Object> map = new HashMap<>();
  }

  /**
   * Tests serialization of a class with {@code static} field.
   *
   * <p>Important: It is not documented that this is officially supported; this
   * test just checks the current behavior.
   */
  @Test
  public void testStaticFieldSerialization() {
    // By default Gson should ignore static fields
    assertThat(gson.toJson(new ClassWithStaticField())).isEqualTo("{}");

    Gson gson = new GsonBuilder()
        // Include static fields
        .excludeFieldsWithModifiers(0)
        .create();

    String json = gson.toJson(new ClassWithStaticField());
    assertThat(json).isEqualTo("{\"s\":\"initial\"}");

    json = gson.toJson(new ClassWithStaticFinalField());
    assertThat(json).isEqualTo("{\"s\":\"initial\"}");
  }

  /**
   * Tests deserialization of a class with {@code static} field.
   *
   * <p>Important: It is not documented that this is officially supported; this
   * test just checks the current behavior.
   */
  @Test
  public void testStaticFieldDeserialization() {
    // By default Gson should ignore static fields
    gson.fromJson("{\"s\":\"custom\"}", ClassWithStaticField.class);
    assertThat(ClassWithStaticField.s).isEqualTo("initial");

    Gson gson = new GsonBuilder()
        // Include static fields
        .excludeFieldsWithModifiers(0)
        .create();

    String oldValue = ClassWithStaticField.s;
    try {
      ClassWithStaticField obj = gson.fromJson("{\"s\":\"custom\"}", ClassWithStaticField.class);
      assertThat(obj).isNotNull();
      assertThat(ClassWithStaticField.s).isEqualTo("custom");
    } finally {
      ClassWithStaticField.s = oldValue;
    }

    try {
      gson.fromJson("{\"s\":\"custom\"}", ClassWithStaticFinalField.class);
      fail();
    } catch (JsonIOException e) {
      assertThat(          e.getMessage()).isEqualTo("Cannot set value of 'static final' field 'com.google.gson.functional.ObjectTest$ClassWithStaticFinalField#s'");
    }
  }

  static class ClassWithStaticField {
    static String s = "initial";
  }

  static class ClassWithStaticFinalField {
    static final String s = "initial";
  }

  @Test
  public void testThrowingDefaultConstructor() {
    try {
      gson.fromJson("{}", ClassWithThrowingConstructor.class);
      fail();
    }
    // TODO: Adjust this once Gson throws more specific exception type
    catch (RuntimeException e) {
      assertThat(          e.getMessage()).isEqualTo("Failed to invoke constructor 'com.google.gson.functional.ObjectTest$ClassWithThrowingConstructor()' with no args");
      assertThat(e).hasCauseThat().isSameInstanceAs(ClassWithThrowingConstructor.thrownException);
    }
  }

  static class ClassWithThrowingConstructor {
    static final RuntimeException thrownException = new RuntimeException("Custom exception");

    public ClassWithThrowingConstructor() {
      throw thrownException;
    }
  }
}
