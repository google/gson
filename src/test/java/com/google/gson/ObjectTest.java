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

import com.google.gson.TestTypes.BagOfPrimitiveWrappers;
import com.google.gson.TestTypes.BagOfPrimitives;
import com.google.gson.TestTypes.ContainsReferenceToSelfType;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Unit tests for Json serialization and deserialization of regular classes.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ObjectTest extends TestCase {
  private Gson gson;

  private static class ClassWithBigDecimal {
    BigDecimal value;
    ClassWithBigDecimal() { }
    ClassWithBigDecimal(String value) {
      this.value = new BigDecimal(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value.toEngineeringString() + "}";
    }
  }

  private static class ClassWithBigInteger {
    BigInteger value;
    ClassWithBigInteger() { }
    ClassWithBigInteger(String value) {
      this.value = new BigInteger(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value + "}";
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testBagOfPrimitivesSerialization() {
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testBagOfPrimitivesDeserialization() {
    BagOfPrimitives src = new BagOfPrimitives(10, 20, false, "stringValue");
    String json = src.getExpectedJson();
    BagOfPrimitives target = gson.fromJson(json, BagOfPrimitives.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testBagOfPrimitiveWrappersSerialization() {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testBagOfPrimitiveWrappersDeserialization() {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    String jsonString = target.getExpectedJson();
    target = gson.fromJson(jsonString, BagOfPrimitiveWrappers.class);
    assertEquals(jsonString, target.getExpectedJson());
  }

  public void testBigDecimalFieldSerialization() {
    ClassWithBigDecimal target = new ClassWithBigDecimal("-122.01e-21");
    String json = gson.toJson(target);
    String actual = json.substring(json.indexOf(':') + 1, json.indexOf('}'));
    assertEquals(target.value, new BigDecimal(actual));
  }

  public void testBigDecimalFieldDeserialization() {
    ClassWithBigDecimal expected = new ClassWithBigDecimal("-122.01e-21");
    String json = expected.getExpectedJson();
    ClassWithBigDecimal actual = gson.fromJson(json, ClassWithBigDecimal.class);
    assertEquals(expected.value, actual.value);
  }

  public void testBadValueForBigDecimalDeserialization() {
    try {
      gson.fromJson("{\"value\"=1.5e-1.0031}", ClassWithBigDecimal.class);
      fail("Exponent of a BigDecimal must be an integer value.");
    } catch (JsonParseException expected) { }
  }

  public void testBigIntegerFieldSerialization() {
    ClassWithBigInteger target = new ClassWithBigInteger("23232323215323234234324324324324324324");
    String json = gson.toJson(target);
    assertEquals(target.getExpectedJson(), json);
  }

  public void testBigIntegerFieldDeserialization() {
    ClassWithBigInteger expected = new ClassWithBigInteger("879697697697697697697697697697697697");
    String json = expected.getExpectedJson();
    ClassWithBigInteger actual = gson.fromJson(json, ClassWithBigInteger.class);
    assertEquals(expected.value, actual.value);
  }

  public void testDirectedAcyclicGraphSerialization() {
    ContainsReferenceToSelfType a = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType b = new ContainsReferenceToSelfType();
    ContainsReferenceToSelfType c = new ContainsReferenceToSelfType();
    a.children.add(b);
    a.children.add(c);
    b.children.add(c);
    assertNotNull(gson.toJson(a));
  }

  public void testDirectedAcyclicGraphDeserialization() {
    String json = "{\"children\":[{\"children\":[{\"children\":[]}]},{\"children\":[]}]}";
    ContainsReferenceToSelfType target = gson.fromJson(json, ContainsReferenceToSelfType.class);
    assertNotNull(target);
    assertEquals(2, target.children.size());
  }


}
