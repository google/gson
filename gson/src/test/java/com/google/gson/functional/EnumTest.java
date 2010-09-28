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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.common.MoreAsserts;
import com.google.gson.reflect.TypeToken;

/**
 * Functional tests for Java 5.0 enums.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class EnumTest extends TestCase {
  
  private Gson gson;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }
  
  public void testTopLevelEnumSerialization() throws Exception {
    String result = gson.toJson(MyEnum.VALUE1);
    assertEquals('"' + MyEnum.VALUE1.toString() + '"', result);
  }
  
  public void testTopLevelEnumDeserialization() throws Exception {
    MyEnum result = gson.fromJson('"' + MyEnum.VALUE1.toString() + '"', MyEnum.class);
    assertEquals(MyEnum.VALUE1, result);
  }
  
  public void testTopLevelEnumInASingleElementArrayDeserialization() {
    String json = "[" + MyEnum.VALUE1.getExpectedJson() + "]";
    MyEnum target = gson.fromJson(json, MyEnum.class);
    assertEquals(json, "[" + target.getExpectedJson() + "]");
  }

  public void testCollectionOfEnumsSerialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    Collection<MyEnum> target = new ArrayList<MyEnum>();
    target.add(MyEnum.VALUE1);
    target.add(MyEnum.VALUE2);
    String expectedJson = "[\"VALUE1\",\"VALUE2\"]";
    String actualJson = gson.toJson(target);
    assertEquals(expectedJson, actualJson);
    actualJson = gson.toJson(target, type);
    assertEquals(expectedJson, actualJson);
  }

  public void testCollectionOfEnumsDeserialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    String json = "[\"VALUE1\",\"VALUE2\"]";
    Collection<MyEnum> target = gson.fromJson(json, type);
    MoreAsserts.assertContains(target, MyEnum.VALUE1);
    MoreAsserts.assertContains(target, MyEnum.VALUE2);
  }

  public void testClassWithEnumFieldSerialization() throws Exception {
    ClassWithEnumFields target = new ClassWithEnumFields();
    assertEquals(target.getExpectedJson(), gson.toJson(target));
  }

  public void testClassWithEnumFieldDeserialization() throws Exception {
    String json = "{value1:'VALUE1',value2:'VALUE2'}";
    ClassWithEnumFields target = gson.fromJson(json, ClassWithEnumFields.class);
    assertEquals(MyEnum.VALUE1,target.value1);
    assertEquals(MyEnum.VALUE2,target.value2);
  }
  
  private static enum MyEnum {
    VALUE1, VALUE2;

    public String getExpectedJson() {
      return "\"" + toString() + "\"";
    }
  }

  private static class ClassWithEnumFields {
    private final MyEnum value1 = MyEnum.VALUE1;
    private final MyEnum value2 = MyEnum.VALUE2;
    public String getExpectedJson() {
      return "{\"value1\":\"" + value1 + "\",\"value2\":\"" + value2 + "\"}";
    }
  }

  /**
   * Test for issue 226.
   */
  public void testEnumSubclass() {
    assertFalse(Roshambo.class == Roshambo.ROCK.getClass());
    assertEquals("\"ROCK\"", gson.toJson(Roshambo.ROCK));
    assertEquals("[\"ROCK\",\"PAPER\",\"SCISSORS\"]", gson.toJson(EnumSet.allOf(Roshambo.class)));
    assertEquals(Roshambo.ROCK, gson.fromJson("\"ROCK\"", Roshambo.class));
    assertEquals(EnumSet.allOf(Roshambo.class),
        gson.fromJson("[\"ROCK\",\"PAPER\",\"SCISSORS\"]", new TypeToken<Set<Roshambo>>() {}.getType()));
  }

  private enum Roshambo {
    ROCK {
      @Override Roshambo defeats() {
        return SCISSORS;
      }
    },
    PAPER {
      @Override Roshambo defeats() {
        return ROCK;
      }
    },
    SCISSORS {
      @Override Roshambo defeats() {
        return PAPER;
      }
    };

    abstract Roshambo defeats();
  }
}
