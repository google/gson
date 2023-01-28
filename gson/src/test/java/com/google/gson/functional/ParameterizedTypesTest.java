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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.ParameterizedTypeFixtures.MyParameterizedType;
import com.google.gson.ParameterizedTypeFixtures.MyParameterizedTypeAdapter;
import com.google.gson.ParameterizedTypeFixtures.MyParameterizedTypeInstanceCreator;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for the serialization and deserialization of parameterized types in Gson.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ParameterizedTypesTest {
  private Gson gson;

  @Before
  public void setUp() {
    gson = new Gson();
  }

  @Test
  public void testParameterizedTypesSerialization() {
    MyParameterizedType<Integer> src = new MyParameterizedType<>(10);
    Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    String json = gson.toJson(src, typeOfSrc);
    assertThat(json).isEqualTo(src.getExpectedJson());
  }

  @Test
  public void testParameterizedTypeDeserialization() {
    BagOfPrimitives bag = new BagOfPrimitives();
    MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<>(bag);
    Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        expectedType, new MyParameterizedTypeInstanceCreator<>(bagDefaultInstance))
        .create();

    String json = expected.getExpectedJson();
    MyParameterizedType<BagOfPrimitives> actual = gson.fromJson(json, expectedType);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testTypesWithMultipleParametersSerialization() {
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> src =
        new MultiParameters<>(10, 1.0F, 2.1D, "abc", new BagOfPrimitives());
    Type typeOfSrc = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
    String json = gson.toJson(src, typeOfSrc);
    String expected = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
        + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    assertThat(json).isEqualTo(expected);
  }

  @Test
  public void testTypesWithMultipleParametersDeserialization() {
    Type typeOfTarget = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
    String json = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
        + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> target =
        gson.fromJson(json, typeOfTarget);
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> expected =
        new MultiParameters<>(10, 1.0F, 2.1D, "abc", new BagOfPrimitives());
    assertThat(target).isEqualTo(expected);
  }

  @Test
  public void testParameterizedTypeWithCustomSerializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeAdapter<String>())
        .create();
    MyParameterizedType<Integer> intTarget = new MyParameterizedType<>(10);
    String json = gson.toJson(intTarget, ptIntegerType);
    assertThat(json).isEqualTo(MyParameterizedTypeAdapter.<Integer>getExpectedJson(intTarget));

    MyParameterizedType<String> stringTarget = new MyParameterizedType<>("abc");
    json = gson.toJson(stringTarget, ptStringType);
    assertThat(json).isEqualTo(MyParameterizedTypeAdapter.<String>getExpectedJson(stringTarget));
  }

  @Test
  public void testParameterizedTypesWithCustomDeserializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeAdapter<String>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeInstanceCreator<>(""))
        .registerTypeAdapter(ptIntegerType, new MyParameterizedTypeInstanceCreator<>(0))
        .create();

    MyParameterizedType<Integer> src = new MyParameterizedType<>(10);
    String json = MyParameterizedTypeAdapter.<Integer>getExpectedJson(src);
    MyParameterizedType<Integer> intTarget = gson.fromJson(json, ptIntegerType);
    assertThat(intTarget.value).isEqualTo(10);

    MyParameterizedType<String> srcStr = new MyParameterizedType<>("abc");
    json = MyParameterizedTypeAdapter.<String>getExpectedJson(srcStr);
    MyParameterizedType<String> stringTarget = gson.fromJson(json, ptStringType);
    assertThat(stringTarget.value).isEqualTo("abc");
  }

  @Test
  public void testParameterizedTypesWithWriterSerialization() {
    Writer writer = new StringWriter();
    MyParameterizedType<Integer> src = new MyParameterizedType<>(10);
    Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    gson.toJson(src, typeOfSrc, writer);
    assertThat(writer.toString()).isEqualTo(src.getExpectedJson());
  }

  @Test
  public void testParameterizedTypeWithReaderDeserialization() {
    BagOfPrimitives bag = new BagOfPrimitives();
    MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<>(bag);
    Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        expectedType, new MyParameterizedTypeInstanceCreator<>(bagDefaultInstance))
        .create();

    Reader json = new StringReader(expected.getExpectedJson());
    MyParameterizedType<Integer> actual = gson.fromJson(json, expectedType);
    assertThat(actual).isEqualTo(expected);
  }

  @SuppressWarnings("varargs")
  @SafeVarargs
  private static <T> T[] arrayOf(T... args) {
    return args;
  }

  @Test
  public void testVariableTypeFieldsAndGenericArraysSerialization() {
    Integer obj = 0;
    Integer[] array = { 1, 2, 3 };
    List<Integer> list = new ArrayList<>();
    list.add(4);
    list.add(5);
    List<Integer>[] arrayOfLists = arrayOf(list, list);

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<>(obj, array, list, arrayOfLists, list, arrayOfLists);
    String json = gson.toJson(objToSerialize, typeOfSrc);

    assertThat(json).isEqualTo(objToSerialize.getExpectedJson());
  }

  @Test
  public void testVariableTypeFieldsAndGenericArraysDeserialization() {
    Integer obj = 0;
    Integer[] array = { 1, 2, 3 };
    List<Integer> list = new ArrayList<>();
    list.add(4);
    list.add(5);
    List<Integer>[] arrayOfLists = arrayOf(list, list);

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<>(obj, array, list, arrayOfLists, list, arrayOfLists);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertThat(json).isEqualTo(objAfterDeserialization.getExpectedJson());
  }

  @Test
  public void testVariableTypeDeserialization() {
    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<>(0, null, null, null, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertThat(json).isEqualTo(objAfterDeserialization.getExpectedJson());
  }

  @Test
  public void testVariableTypeArrayDeserialization() {
    Integer[] array = { 1, 2, 3 };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<>(null, array, null, null, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertThat(json).isEqualTo(objAfterDeserialization.getExpectedJson());
  }

  @Test
  public void testParameterizedTypeWithVariableTypeDeserialization() {
    List<Integer> list = new ArrayList<>();
    list.add(4);
    list.add(5);

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<>(null, null, list, null, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertThat(json).isEqualTo(objAfterDeserialization.getExpectedJson());
  }

  @Test
  public void testParameterizedTypeGenericArraysSerialization() {
    List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    List<Integer>[] arrayOfLists = arrayOf(list, list);

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<>(null, null, null, arrayOfLists, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    assertThat(json).isEqualTo("{\"arrayOfListOfTypeParameters\":[[1,2],[1,2]]}");
  }

  @Test
  public void testParameterizedTypeGenericArraysDeserialization() {
    List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    List<Integer>[] arrayOfLists = arrayOf(list, list);

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<>(null, null, null, arrayOfLists, null, null);
    String json = gson.toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = gson.fromJson(json, typeOfSrc);

    assertThat(json).isEqualTo(objAfterDeserialization.getExpectedJson());
  }

  /**
   * An test object that has fields that are type variables.
   *
   * @param <T> Enforce T to be a string to make writing the "toExpectedJson" method easier.
   */
  private static class ObjectWithTypeVariables<T extends Number> {
    private final T typeParameterObj;
    private final T[] typeParameterArray;
    private final List<T> listOfTypeParameters;
    private final List<T>[] arrayOfListOfTypeParameters;
    private final List<? extends T> listOfWildcardTypeParameters;
    private final List<? extends T>[] arrayOfListOfWildcardTypeParameters;

    // For use by Gson
    @SuppressWarnings("unused")
    private ObjectWithTypeVariables() {
      this(null, null, null, null, null, null);
    }

    public ObjectWithTypeVariables(T obj, T[] array, List<T> list, List<T>[] arrayOfList,
        List<? extends T> wildcardList, List<? extends T>[] arrayOfWildcardList) {
      this.typeParameterObj = obj;
      this.typeParameterArray = array;
      this.listOfTypeParameters = list;
      this.arrayOfListOfTypeParameters = arrayOfList;
      this.listOfWildcardTypeParameters = wildcardList;
      this.arrayOfListOfWildcardTypeParameters = arrayOfWildcardList;
    }

    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder().append("{");

      boolean needsComma = false;
      if (typeParameterObj != null) {
        sb.append("\"typeParameterObj\":").append(toString(typeParameterObj));
        needsComma = true;
      }

      if (typeParameterArray != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"typeParameterArray\":[");
        appendObjectsToBuilder(sb, Arrays.asList(typeParameterArray));
        sb.append(']');
        needsComma = true;
      }

      if (listOfTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"listOfTypeParameters\":[");
        appendObjectsToBuilder(sb, listOfTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (arrayOfListOfTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"arrayOfListOfTypeParameters\":[");
        appendObjectsToBuilder(sb, arrayOfListOfTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (listOfWildcardTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"listOfWildcardTypeParameters\":[");
        appendObjectsToBuilder(sb, listOfWildcardTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (arrayOfListOfWildcardTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"arrayOfListOfWildcardTypeParameters\":[");
        appendObjectsToBuilder(sb, arrayOfListOfWildcardTypeParameters);
        sb.append(']');
        needsComma = true;
      }
      sb.append('}');
      return sb.toString();
    }

    private void appendObjectsToBuilder(StringBuilder sb, Iterable<? extends T> iterable) {
      boolean isFirst = true;
      for (T obj : iterable) {
        if (!isFirst) {
          sb.append(',');
        }
        isFirst = false;
        sb.append(toString(obj));
      }
    }

    private void appendObjectsToBuilder(StringBuilder sb, List<? extends T>[] arrayOfList) {
      boolean isFirst = true;
      for (List<? extends T> list : arrayOfList) {
        if (!isFirst) {
          sb.append(',');
        }
        isFirst = false;
        if (list != null) {
          sb.append('[');
          appendObjectsToBuilder(sb, list);
          sb.append(']');
        } else {
          sb.append("null");
        }
      }
    }

    public String toString(T obj) {
      return obj.toString();
    }
  }

  private static class MultiParameters<A, B, C, D, E> {
    A a;
    B b;
    C c;
    D d;
    E e;
    // For use by Gson
    @SuppressWarnings("unused")
    private MultiParameters() {
    }
    MultiParameters(A a, B b, C c, D d, E e) {
      super();
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
      this.e = e;
    }
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((a == null) ? 0 : a.hashCode());
      result = prime * result + ((b == null) ? 0 : b.hashCode());
      result = prime * result + ((c == null) ? 0 : c.hashCode());
      result = prime * result + ((d == null) ? 0 : d.hashCode());
      result = prime * result + ((e == null) ? 0 : e.hashCode());
      return result;
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      MultiParameters<A, B, C, D, E> other = (MultiParameters<A, B, C, D, E>) obj;
      if (a == null) {
        if (other.a != null) {
          return false;
        }
      } else if (!a.equals(other.a)) {
        return false;
      }
      if (b == null) {
        if (other.b != null) {
          return false;
        }
      } else if (!b.equals(other.b)) {
        return false;
      }
      if (c == null) {
        if (other.c != null) {
          return false;
        }
      } else if (!c.equals(other.c)) {
        return false;
      }
      if (d == null) {
        if (other.d != null) {
          return false;
        }
      } else if (!d.equals(other.d)) {
        return false;
      }
      if (e == null) {
        if (other.e != null) {
          return false;
        }
      } else if (!e.equals(other.e)) {
        return false;
      }
      return true;
    }
  }

  // Begin: tests to reproduce issue 103
  private static class Quantity {
    @SuppressWarnings("unused")
    int q = 10;
  }
  private static class MyQuantity extends Quantity {
    @SuppressWarnings("unused")
    int q2 = 20;
  }
  private interface Measurable<T> {
  }
  private interface Field<T> {
  }
  private interface Immutable {
  }

  public static final class Amount<Q extends Quantity>
      implements Measurable<Q>, Field<Amount<?>>, Serializable, Immutable {
    private static final long serialVersionUID = -7560491093120970437L;

    int value = 30;
  }

  @Test
  public void testDeepParameterizedTypeSerialization() {
    Amount<MyQuantity> amount = new Amount<>();
    String json = gson.toJson(amount);
    assertThat(json).contains("value");
    assertThat(json).contains("30");
  }

  @Test
  public void testDeepParameterizedTypeDeserialization() {
    String json = "{value:30}";
    Type type = new TypeToken<Amount<MyQuantity>>() {}.getType();
    Amount<MyQuantity> amount = gson.fromJson(json, type);
    assertThat(amount.value).isEqualTo(30);
  }
  // End: tests to reproduce issue 103

  private static void assertCorrectlyDeserialized(Object object) {
    @SuppressWarnings("unchecked")
    List<Quantity> list = (List<Quantity>) object;
    assertThat(list.size()).isEqualTo(1);
    assertThat(list.get(0).q).isEqualTo(4);
  }

  @Test
  public void testGsonFromJsonTypeToken() {
    TypeToken<List<Quantity>> typeToken = new TypeToken<List<Quantity>>() {};
    Type type = typeToken.getType();

    {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("q", 4);
      JsonArray jsonArray = new JsonArray();
      jsonArray.add(jsonObject);

      assertCorrectlyDeserialized(gson.fromJson(jsonArray, typeToken));
      assertCorrectlyDeserialized(gson.fromJson(jsonArray, type));
    }

    String json = "[{\"q\":4}]";

    {
      assertCorrectlyDeserialized(gson.fromJson(json, typeToken));
      assertCorrectlyDeserialized(gson.fromJson(json, type));
    }

    {
      assertCorrectlyDeserialized(gson.fromJson(new StringReader(json), typeToken));
      assertCorrectlyDeserialized(gson.fromJson(new StringReader(json), type));
    }

    {
      JsonReader reader = new JsonReader(new StringReader(json));
      assertCorrectlyDeserialized(gson.fromJson(reader, typeToken));

      reader = new JsonReader(new StringReader(json));
      assertCorrectlyDeserialized(gson.fromJson(reader, type));
    }
  }
}
