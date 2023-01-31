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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassOverridingEquals;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests that do not fall neatly into any of the existing classification.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class UncategorizedTest {

  private Gson gson = null;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testInvalidJsonDeserializationFails() throws Exception {
    try {
      gson.fromJson("adfasdf1112,,,\":", BagOfPrimitives.class);
      fail("Bad JSON should throw a ParseException");
    } catch (JsonParseException expected) { }

    try {
      gson.fromJson("{adfasdf1112,,,\":}", BagOfPrimitives.class);
      fail("Bad JSON should throw a ParseException");
    } catch (JsonParseException expected) { }
  }

  @Test
  public void testObjectEqualButNotSameSerialization() {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    ClassOverridingEquals objB = new ClassOverridingEquals();
    objB.ref = objA;
    String json = gson.toJson(objB);
    assertThat(json).isEqualTo(objB.getExpectedJson());
  }

  @Test
  public void testStaticFieldsAreNotSerialized() {
    BagOfPrimitives target = new BagOfPrimitives();
    assertThat(gson.toJson(target)).doesNotContain("DEFAULT_VALUE");
  }

  @Test
  public void testGsonInstanceReusableForSerializationAndDeserialization() {
    BagOfPrimitives bag = new BagOfPrimitives();
    String json = gson.toJson(bag);
    BagOfPrimitives deserialized = gson.fromJson(json, BagOfPrimitives.class);
    assertThat(deserialized).isEqualTo(bag);
  }

  /**
   * This test ensures that a custom deserializer is able to return a derived class instance for a
   * base class object. For a motivation for this test, see Issue 37 and
   * http://groups.google.com/group/google-gson/browse_thread/thread/677d56e9976d7761
   */
  @Test
  public void testReturningDerivedClassesDuringDeserialization() {
    Gson gson = new GsonBuilder().registerTypeAdapter(Base.class, new BaseTypeAdapter()).create();
    String json = "{\"opType\":\"OP1\"}";
    Base base = gson.fromJson(json, Base.class);
    assertThat(base).isInstanceOf(Derived1.class);
    assertThat(base.opType).isEqualTo(OperationType.OP1);

    json = "{\"opType\":\"OP2\"}";
    base = gson.fromJson(json, Base.class);
    assertThat(base).isInstanceOf(Derived2.class);
    assertThat(base.opType).isEqualTo(OperationType.OP2);
  }

  /**
   * Test that trailing whitespace is ignored.
   * http://code.google.com/p/google-gson/issues/detail?id=302
   */
  @Test
  public void testTrailingWhitespace() throws Exception {
    List<Integer> integers = gson.fromJson("[1,2,3]  \n\n  ",
        new TypeToken<List<Integer>>() {}.getType());
    assertThat(integers).containsExactly(1, 2, 3).inOrder();
  }

  private enum OperationType { OP1, OP2 }
  private static class Base {
    OperationType opType;
  }
  private static class Derived1 extends Base {
    Derived1() { opType = OperationType.OP1; }
  }
  private static class Derived2 extends Base {
    Derived2() { opType = OperationType.OP2; }
  }
  private static class BaseTypeAdapter implements JsonDeserializer<Base> {
    @Override public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      String opTypeStr = json.getAsJsonObject().get("opType").getAsString();
      OperationType opType = OperationType.valueOf(opTypeStr);
      switch (opType) {
      case OP1:
        return new Derived1();
      case OP2:
        return new Derived2();
      }
      throw new JsonParseException("unknown type: " + json);
    }
  }
}
