/*
 * Copyright (C) 2015 Google Inc.
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

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import javax.swing.text.html.Option;

/**
 * Functional test for Json serialization and deserialization for java.util.Optional
 */
public class JavaUtilOptionalTest extends TestCase {
  private GsonBuilder gsonBuilder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gsonBuilder = new GsonBuilder().serializeNulls();
  }

  public void testTopLevelNullObjectSerialization() {
    Optional<String> emptyValue = Optional.empty();
    Gson gson = gsonBuilder.create();
    String actual = gson.toJson(emptyValue, new TypeToken<Optional<String>>() {}.getType());
    assertEquals("null", actual);

    actual = gson.toJson(Optional.empty());
    assertEquals("null", actual);
  }

  public void testTopLevelNullObjectDeserialization() throws Exception {
    Gson gson = gsonBuilder.create();
    Optional<String> actual = gson.fromJson("null", new TypeToken<Optional<String>>() {}.getType());
    assertFalse(actual.isPresent());

    Optional<?> actualNoToken = gson.fromJson("null", Optional.class);
    assertFalse(actualNoToken.isPresent());
  }

  public void testExplicitSerializationOfNulls() {
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(null);
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }

  public void testExplicitDeserializationOfNulls() throws Exception {
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = gson.fromJson("{\"bag\":null}", ClassWithObjects.class);
    assertFalse(target.bag.isPresent());
  }

  public void testExplicitSerializationOfNullArrayMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"array\":null"));
  }

  /**
   * Added to verify http://code.google.com/p/google-gson/issues/detail?id=68
   */
  public void testNullWrappedPrimitiveMemberSerialization() {
    Gson gson = gsonBuilder.serializeNulls().create();
    ClassWithNullWrappedPrimitive target = new ClassWithNullWrappedPrimitive();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"value\":null"));
  }

  /**
   * Added to verify http://code.google.com/p/google-gson/issues/detail?id=68
   */
  public void testNullWrappedPrimitiveMemberDeserialization() {
    Gson gson = gsonBuilder.create();
    String json = "{'value':null}";
    ClassWithNullWrappedPrimitive target = gson.fromJson(json, ClassWithNullWrappedPrimitive.class);
    assertFalse(target.value.isPresent());
  }

  public void testExplicitSerializationOfNullCollectionMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"col\":null"));
  }

  public void testExplicitSerializationOfNullStringMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertTrue(json.contains("\"str\":null"));
  }

  public void testCustomSerializationOfNulls() {
    gsonBuilder.registerTypeAdapter(ClassWithObjects.class, new ClassWithObjectsSerializer());
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(new BagOfPrimitives());
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertEquals(expected, actual);
  }

  public void testPrintPrintingObjectWithNulls() throws Exception {
    gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    String result = gson.toJson(new ClassWithMembers());
    assertEquals("{}", result);

    gson = gsonBuilder.serializeNulls().create();
    result = gson.toJson(new ClassWithMembers());
    assertTrue(result.contains("\"str\":null"));
  }

  public void testPrintPrintingArraysWithNulls() throws Exception {
    gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    String result = gson.toJson(new String[] { "1", null, "3" });
    assertEquals("[\"1\",null,\"3\"]", result);

    gson = gsonBuilder.serializeNulls().create();
    result = gson.toJson(new String[] { "1", null, "3" });
    assertEquals("[\"1\",null,\"3\"]", result);
  }

  // test for issue 389
  public void testAbsentJsonElementsAreSetToNull() {
    Gson gson = new Gson();
    ClassWithInitializedMembers target =
            gson.fromJson("{array:[1,2,3]}", ClassWithInitializedMembers.class);
    assertTrue(target.array.isPresent() && target.array.get().length == 3 && target.array.get()[1] == 2);
    assertEquals(ClassWithInitializedMembers.MY_STRING_DEFAULT, target.str1);
    assertFalse(target.str2.isPresent());
    assertEquals(ClassWithInitializedMembers.MY_INT_DEFAULT, target.int1);
    assertEquals(ClassWithInitializedMembers.MY_BOOLEAN_DEFAULT, target.bool1);
  }

  public void testExplicitNullSetsFieldToNullDuringDeserialization() {
    Gson gson = new Gson();
    String json = "{value:null}";
    ObjectWithField obj = gson.fromJson(json, ObjectWithField.class);
    assertFalse(obj.value.isPresent());
  }

  public void testCustomTypeAdapterPassesNullSerialization() {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(ObjectWithField.class, new JsonSerializer<ObjectWithField>() {
              @Override public JsonElement serialize(ObjectWithField src, Type typeOfSrc,
                                                     JsonSerializationContext context) {
                return context.serialize(null);
              }
            }).create();
    ObjectWithField target = new ObjectWithField();
    target.value = Optional.of("value1");
    String json = gson.toJson(target);
    assertFalse(json.contains("value1"));
  }

  public void testCustomTypeAdapterPassesNullDeserialization() {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(ObjectWithField.class, new JsonDeserializer<ObjectWithField>() {
              @Override public ObjectWithField deserialize(JsonElement json, Type type,
                                                           JsonDeserializationContext context) {
                return context.deserialize(null, type);
              }
            }).create();
    String json = "{value:'value1'}";
    ObjectWithField target = gson.fromJson(json, ObjectWithField.class);
    assertNull(target);
  }

  private static class ClassWithInitializedMembers {
    // Using a mix of no-args constructor and field initializers
    public static final Optional<String> MY_STRING_DEFAULT = Optional.of("string");
    private static final OptionalInt MY_INT_DEFAULT = OptionalInt.of(2);
    private static final Optional<Boolean> MY_BOOLEAN_DEFAULT = Optional.of(true);
    Optional<int[]> array = Optional.empty();
    Optional<String> str1; // Initialized in constructor
    Optional<String> str2 = Optional.empty();
    OptionalInt int1 = MY_INT_DEFAULT;
    Optional<Boolean> bool1 = MY_BOOLEAN_DEFAULT;
    public ClassWithInitializedMembers() {
      str1 = MY_STRING_DEFAULT;
    }
  }

  private static class ClassWithNullWrappedPrimitive {
    private OptionalLong value;
  }

  @SuppressWarnings("unused")
  private static class ClassWithMembers {
    Optional<String> str = Optional.empty();
    Optional<int[]> array = Optional.empty();
    Optional<Collection<String>> col = Optional.empty();
  }

  private static class ClassWithObjectsSerializer implements JsonSerializer<ClassWithObjects> {
    @Override public JsonElement serialize(ClassWithObjects src, Type typeOfSrc,
                                           JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.add("bag", JsonNull.INSTANCE);
      return obj;
    }
  }

  private static class ObjectWithField {
    Optional<String> value = Optional.of("");
  }

  private static class BagOfPrimitives {
    public static final OptionalLong DEFAULT_VALUE = OptionalLong.of(0);
    public OptionalLong longValue;
    public OptionalInt intValue;
    public Optional<Boolean> booleanValue;
    public Optional<String> stringValue;

    public BagOfPrimitives() {
      this(DEFAULT_VALUE, OptionalInt.of(0), Optional.of(false), Optional.of(""));
    }

    public BagOfPrimitives(OptionalLong longValue, OptionalInt intValue, Optional<Boolean> booleanValue, Optional<String> stringValue) {
      this.longValue = longValue;
      this.intValue = intValue;
      this.booleanValue = booleanValue;
      this.stringValue = stringValue;
    }
  }

  private static class ClassWithObjects {
    public final Optional<BagOfPrimitives> bag;
    public ClassWithObjects() {
      this(new BagOfPrimitives());
    }
    public ClassWithObjects(BagOfPrimitives bag) {
      this.bag = Optional.ofNullable(bag);
    }
  }
}
