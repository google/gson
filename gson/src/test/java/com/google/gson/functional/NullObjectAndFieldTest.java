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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassWithObjects;
import java.lang.reflect.Type;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for the different cases for serializing (or ignoring) null fields and object.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class NullObjectAndFieldTest {
  private GsonBuilder gsonBuilder;

  @Before
  public void setUp() throws Exception {
    gsonBuilder = new GsonBuilder().serializeNulls();
  }

  @Test
  public void testTopLevelNullObjectSerialization() {
    Gson gson = gsonBuilder.create();
    String actual = gson.toJson(null);
    assertThat(actual).isEqualTo("null");

    actual = gson.toJson(null, String.class);
    assertThat(actual).isEqualTo("null");
  }

  @Test
  public void testTopLevelNullObjectDeserialization() {
    Gson gson = gsonBuilder.create();
    String actual = gson.fromJson("null", String.class);
    assertThat(actual).isNull();
  }

  @Test
  public void testExplicitSerializationOfNulls() {
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(null);
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testExplicitDeserializationOfNulls() {
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = gson.fromJson("{\"bag\":null}", ClassWithObjects.class);
    assertThat(target.bag).isNull();
  }

  @Test
  public void testExplicitSerializationOfNullArrayMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertThat(json).contains("\"array\":null");
  }

  /** Added to verify http://code.google.com/p/google-gson/issues/detail?id=68 */
  @Test
  public void testNullWrappedPrimitiveMemberSerialization() {
    Gson gson = gsonBuilder.serializeNulls().create();
    ClassWithNullWrappedPrimitive target = new ClassWithNullWrappedPrimitive();
    String json = gson.toJson(target);
    assertThat(json).contains("\"value\":null");
  }

  /** Added to verify http://code.google.com/p/google-gson/issues/detail?id=68 */
  @Test
  public void testNullWrappedPrimitiveMemberDeserialization() {
    Gson gson = gsonBuilder.create();
    String json = "{'value':null}";
    ClassWithNullWrappedPrimitive target = gson.fromJson(json, ClassWithNullWrappedPrimitive.class);
    assertThat(target.value).isNull();
  }

  @Test
  public void testExplicitSerializationOfNullCollectionMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertThat(json).contains("\"col\":null");
  }

  @Test
  public void testExplicitSerializationOfNullStringMembers() {
    Gson gson = gsonBuilder.create();
    ClassWithMembers target = new ClassWithMembers();
    String json = gson.toJson(target);
    assertThat(json).contains("\"str\":null");
  }

  @Test
  public void testCustomSerializationOfNulls() {
    gsonBuilder.registerTypeAdapter(ClassWithObjects.class, new ClassWithObjectsSerializer());
    Gson gson = gsonBuilder.create();
    ClassWithObjects target = new ClassWithObjects(new BagOfPrimitives());
    String actual = gson.toJson(target);
    String expected = "{\"bag\":null}";
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testPrintPrintingObjectWithNulls() {
    gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    String result = gson.toJson(new ClassWithMembers());
    assertThat(result).isEqualTo("{}");

    gson = gsonBuilder.serializeNulls().create();
    result = gson.toJson(new ClassWithMembers());
    assertThat(result).contains("\"str\":null");
  }

  @Test
  public void testPrintPrintingArraysWithNulls() {
    gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    String result = gson.toJson(new String[] {"1", null, "3"});
    assertThat(result).isEqualTo("[\"1\",null,\"3\"]");

    gson = gsonBuilder.serializeNulls().create();
    result = gson.toJson(new String[] {"1", null, "3"});
    assertThat(result).isEqualTo("[\"1\",null,\"3\"]");
  }

  // test for issue 389
  @Test
  public void testAbsentJsonElementsAreSetToNull() {
    Gson gson = new Gson();
    ClassWithInitializedMembers target =
        gson.fromJson("{array:[1,2,3]}", ClassWithInitializedMembers.class);
    assertThat(target.array).hasLength(3);
    assertThat(target.array[1]).isEqualTo(2);
    assertThat(target.str1).isEqualTo(ClassWithInitializedMembers.MY_STRING_DEFAULT);
    assertThat(target.str2).isNull();
    assertThat(target.int1).isEqualTo(ClassWithInitializedMembers.MY_INT_DEFAULT);
    // test the default value of a primitive int field per JVM spec
    assertThat(target.int2).isEqualTo(0);
    assertThat(target.bool1).isEqualTo(ClassWithInitializedMembers.MY_BOOLEAN_DEFAULT);
    // test the default value of a primitive boolean field per JVM spec
    assertThat(target.bool2).isFalse();
  }

  public static class ClassWithInitializedMembers {
    // Using a mix of no-args constructor and field initializers
    // Also, some fields are initialized and some are not (so initialized per JVM spec)
    public static final String MY_STRING_DEFAULT = "string";
    private static final int MY_INT_DEFAULT = 2;
    private static final boolean MY_BOOLEAN_DEFAULT = true;
    int[] array;
    String str1;
    String str2;
    int int1 = MY_INT_DEFAULT;
    int int2;
    boolean bool1 = MY_BOOLEAN_DEFAULT;
    boolean bool2;

    public ClassWithInitializedMembers() {
      str1 = MY_STRING_DEFAULT;
    }
  }

  private static class ClassWithNullWrappedPrimitive {
    private Long value;
  }

  @SuppressWarnings("unused")
  private static class ClassWithMembers {
    String str;
    int[] array;
    Collection<String> col;
  }

  private static class ClassWithObjectsSerializer implements JsonSerializer<ClassWithObjects> {
    @Override
    public JsonElement serialize(
        ClassWithObjects src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.add("bag", JsonNull.INSTANCE);
      return obj;
    }
  }

  @Test
  public void testExplicitNullSetsFieldToNullDuringDeserialization() {
    Gson gson = new Gson();
    String json = "{value:null}";
    ObjectWithField obj = gson.fromJson(json, ObjectWithField.class);
    assertThat(obj.value).isNull();
  }

  @Test
  public void testCustomTypeAdapterPassesNullSerialization() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                ObjectWithField.class,
                (JsonSerializer<ObjectWithField>)
                    (src, typeOfSrc, context) -> context.serialize(null))
            .create();
    ObjectWithField target = new ObjectWithField();
    target.value = "value1";
    String json = gson.toJson(target);
    assertThat(json).doesNotContain("value1");
  }

  @Test
  public void testCustomTypeAdapterPassesNullDeserialization() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                ObjectWithField.class,
                (JsonDeserializer<ObjectWithField>)
                    (json, type, context) -> context.deserialize(null, type))
            .create();
    String json = "{value:'value1'}";
    ObjectWithField target = gson.fromJson(json, ObjectWithField.class);
    assertThat(target).isNull();
  }

  private static class ObjectWithField {
    String value = "";
  }
}
