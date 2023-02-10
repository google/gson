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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.google.gson.common.MoreAsserts;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for Java 5.0 enums.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class EnumTest {

  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testTopLevelEnumSerialization() {
    String result = gson.toJson(MyEnum.VALUE1);
    assertThat(result).isEqualTo('"' + MyEnum.VALUE1.toString() + '"');
  }

  @Test
  public void testTopLevelEnumDeserialization() {
    MyEnum result = gson.fromJson('"' + MyEnum.VALUE1.toString() + '"', MyEnum.class);
    assertThat(result).isEqualTo(MyEnum.VALUE1);
  }

  @Test
  public void testCollectionOfEnumsSerialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    Collection<MyEnum> target = new ArrayList<>();
    target.add(MyEnum.VALUE1);
    target.add(MyEnum.VALUE2);
    String expectedJson = "[\"VALUE1\",\"VALUE2\"]";
    String actualJson = gson.toJson(target);
    assertThat(actualJson).isEqualTo(expectedJson);
    actualJson = gson.toJson(target, type);
    assertThat(actualJson).isEqualTo(expectedJson);
  }

  @Test
  public void testCollectionOfEnumsDeserialization() {
    Type type = new TypeToken<Collection<MyEnum>>() {}.getType();
    String json = "[\"VALUE1\",\"VALUE2\"]";
    Collection<MyEnum> target = gson.fromJson(json, type);
    MoreAsserts.assertContains(target, MyEnum.VALUE1);
    MoreAsserts.assertContains(target, MyEnum.VALUE2);
  }

  @Test
  public void testClassWithEnumFieldSerialization() {
    ClassWithEnumFields target = new ClassWithEnumFields();
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testClassWithEnumFieldDeserialization() {
    String json = "{value1:'VALUE1',value2:'VALUE2'}";
    ClassWithEnumFields target = gson.fromJson(json, ClassWithEnumFields.class);
    assertThat(target.value1).isEqualTo(MyEnum.VALUE1);
    assertThat(target.value2).isEqualTo(MyEnum.VALUE2);
  }

  private static enum MyEnum {
    VALUE1, VALUE2
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
  @Test
  public void testEnumSubclass() {
    assertThat(Roshambo.ROCK.getClass()).isAssignableTo(Roshambo.class);
    assertThat(gson.toJson(Roshambo.ROCK)).isEqualTo("\"ROCK\"");
    assertThat(gson.toJson(EnumSet.allOf(Roshambo.class))).isEqualTo("[\"ROCK\",\"PAPER\",\"SCISSORS\"]");
    assertThat(gson.fromJson("\"ROCK\"", Roshambo.class)).isEqualTo(Roshambo.ROCK);
    assertThat(EnumSet.allOf(Roshambo.class)).isEqualTo(
        gson.fromJson("[\"ROCK\",\"PAPER\",\"SCISSORS\"]", new TypeToken<Set<Roshambo>>() {}.getType())
    );
  }

  @Test
  public void testEnumSubclassWithRegisteredTypeAdapter() {
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Roshambo.class, new MyEnumTypeAdapter())
        .create();
    assertThat(Roshambo.ROCK.getClass()).isAssignableTo(Roshambo.class);
    assertThat(gson.toJson(Roshambo.ROCK)).isEqualTo("\"123ROCK\"");
    assertThat(gson.toJson(EnumSet.allOf(Roshambo.class))).isEqualTo("[\"123ROCK\",\"123PAPER\",\"123SCISSORS\"]");
    assertThat(gson.fromJson("\"123ROCK\"", Roshambo.class)).isEqualTo(Roshambo.ROCK);
    assertThat(EnumSet.allOf(Roshambo.class)).isEqualTo(
        gson.fromJson("[\"123ROCK\",\"123PAPER\",\"123SCISSORS\"]", new TypeToken<Set<Roshambo>>() {}.getType())
    );
  }

  @Test
  public void testEnumSubclassAsParameterizedType() {
    Collection<Roshambo> list = new ArrayList<>();
    list.add(Roshambo.ROCK);
    list.add(Roshambo.PAPER);

    String json = gson.toJson(list);
    assertThat(json).isEqualTo("[\"ROCK\",\"PAPER\"]");

    Type collectionType = new TypeToken<Collection<Roshambo>>() {}.getType();
    Collection<Roshambo> actualJsonList = gson.fromJson(json, collectionType);
    MoreAsserts.assertContains(actualJsonList, Roshambo.ROCK);
    MoreAsserts.assertContains(actualJsonList, Roshambo.PAPER);
  }

  @Test
  public void testEnumCaseMapping() {
    assertThat(gson.fromJson("\"boy\"", Gender.class)).isEqualTo(Gender.MALE);
    assertThat(gson.toJson(Gender.MALE, Gender.class)).isEqualTo("\"boy\"");
  }

  @Test
  public void testEnumSet() {
    EnumSet<Roshambo> foo = EnumSet.of(Roshambo.ROCK, Roshambo.PAPER);
    String json = gson.toJson(foo);
    assertThat(json).isEqualTo("[\"ROCK\",\"PAPER\"]");

    Type type = new TypeToken<EnumSet<Roshambo>>() {}.getType();
    EnumSet<Roshambo> bar = gson.fromJson(json, type);
    assertThat(bar).containsExactly(Roshambo.ROCK, Roshambo.PAPER).inOrder();
    assertThat(bar).doesNotContain(Roshambo.SCISSORS);;
  }

  @Test
  public void testEnumMap() {
    EnumMap<MyEnum, String> map = new EnumMap<>(MyEnum.class);
    map.put(MyEnum.VALUE1, "test");
    String json = gson.toJson(map);
    assertThat(json).isEqualTo("{\"VALUE1\":\"test\"}");

    Type type = new TypeToken<EnumMap<MyEnum, String>>() {}.getType();
    EnumMap<?, ?> actualMap = gson.fromJson("{\"VALUE1\":\"test\"}", type);
    Map<?, ?> expectedMap = Collections.singletonMap(MyEnum.VALUE1, "test");
    assertThat(actualMap).isEqualTo(expectedMap);
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

  private static class MyEnumTypeAdapter
      implements JsonSerializer<Roshambo>, JsonDeserializer<Roshambo> {
    @Override public JsonElement serialize(Roshambo src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive("123" + src.name());
    }

    @Override public Roshambo deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return Roshambo.valueOf(json.getAsString().substring(3));
    }
  }

  private enum Gender {
    @SerializedName("boy")
    MALE,

    @SerializedName("girl")
    FEMALE
  }

  @Test
  public void testEnumClassWithFields() {
    assertThat(gson.toJson(Color.RED)).isEqualTo("\"RED\"");
    assertThat(gson.fromJson("RED", Color.class).value).isEqualTo("red");
    assertThat(gson.fromJson("BLUE", Color.class).index).isEqualTo(2);
  }

  private enum Color {
    RED("red", 1), BLUE("blue", 2), GREEN("green", 3);
    String value;
    int index;
    private Color(String value, int index) {
      this.value = value;
      this.index = index;
    }
  }

  @Test
  public void testEnumToStringRead() {
    // Should still be able to read constant name
    assertThat(gson.fromJson("\"A\"", CustomToString.class)).isEqualTo(CustomToString.A);
    // Should be able to read toString() value
    assertThat(gson.fromJson("\"test\"", CustomToString.class)).isEqualTo(CustomToString.A);

    assertThat(gson.fromJson("\"other\"", CustomToString.class)).isNull();
  }

  private enum CustomToString {
    A;

    @Override
    public String toString() {
      return "test";
    }
  }

  /**
   * Test that enum constant names have higher precedence than {@code toString()}
   * result.
   */
  @Test
  public void testEnumToStringReadInterchanged() {
    assertThat(gson.fromJson("\"A\"", InterchangedToString.class)).isEqualTo(InterchangedToString.A);
    assertThat(gson.fromJson("\"B\"", InterchangedToString.class)).isEqualTo(InterchangedToString.B);
  }

  private enum InterchangedToString {
    A("B"),
    B("A");

    private final String toString;

    InterchangedToString(String toString) {
      this.toString = toString;
    }

    @Override
    public String toString() {
      return toString;
    }
  }
}
