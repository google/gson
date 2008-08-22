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

import com.google.gson.TestTypes.BagOfPrimitives;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Small test for the serialization/deserialization support of parameterized types in Gson.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ParameterizedTypesTest extends TestCase {

  private Gson gson;

  private static class MyParameterizedType<T> {
    final T value;
    public MyParameterizedType(T value) {
      this.value = value;
    }
    public T getValue() {
      return value;
    }

    String getExpectedJson() {
      String valueAsJson = getExpectedJson(value);
      return String.format("{\"value\":%s}", valueAsJson);
    }

    private String getExpectedJson(Object obj) {
      Class<?> clazz = obj.getClass();
      if (Primitives.isWrapperType(Primitives.wrap(clazz))) {
        return obj.toString();
      } else if (obj.getClass().equals(String.class)) {
        return "\"" + obj.toString() + "\"";
      } else {
        // Try invoking a getExpectedJson() method if it exists
        try {
          Method method = clazz.getMethod("getExpectedJson");
          Object results = method.invoke(obj);
          return (String) results;
        } catch (SecurityException e) {
          throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
          throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
    }

    @Override
    public int hashCode() {
      return value == null ? 0 : value.hashCode();
    }
    @SuppressWarnings("unchecked")
    @Override
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
      MyParameterizedType other = (MyParameterizedType) obj;
      if (value == null) {
        if (other.value != null) {
          return false;
        }
      } else if (!value.equals(other.value)) {
        return false;
      }
      return true;
    }
  }

  private static class MyParameterizedTypeAdapter<T>
      implements JsonSerializer<MyParameterizedType<T>>, JsonDeserializer<MyParameterizedType<T>> {
    @SuppressWarnings("unchecked")
    public static<T> String getExpectedJson(MyParameterizedType<T> obj) {
      Class<T> clazz = (Class<T>) obj.value.getClass();
      boolean addQuotes = !clazz.isArray() && !Primitives.unwrap(clazz).isPrimitive();
      StringBuilder sb = new StringBuilder("{\"");
      sb.append(obj.value.getClass().getSimpleName()).append("\":");
      if (addQuotes) {
        sb.append("\"");
      }
      sb.append(obj.value.toString());
      if (addQuotes) {
        sb.append("\"");
      }
      sb.append("}");
      return sb.toString();
    }

    public JsonElement serialize(MyParameterizedType<T> src, Type classOfSrc,
            JsonSerializationContext context) {
      JsonObject json = new JsonObject();
      T value = src.getValue();
      json.add(value.getClass().getSimpleName(), context.serialize(value));
      return json;
    }

    @SuppressWarnings("unchecked")
    public MyParameterizedType<T> deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
      Type genericClass = TypeUtils.getActualTypeForFirstTypeVariable(typeOfT);
      TypeInfo typeInfo = new TypeInfo(genericClass);
      String className = typeInfo.getRawClass().getSimpleName();
      T value = (T) json.getAsJsonObject().get(className).getAsObject();
      if (typeInfo.isPrimitive()) {
        PrimitiveTypeAdapter typeAdapter = new PrimitiveTypeAdapter();
        value = (T) typeAdapter.adaptType(value, typeInfo.getRawClass());
      }
      return new MyParameterizedType<T>(value);
    }
  }

  private static class MyParameterizedTypeInstanceCreator<T>
      implements InstanceCreator<MyParameterizedType<T>>{
    private final T instanceOfT;
    /**
     * Caution the specified instance is reused by the instance creator for each call.
     * This means that the fields of the same objects will be overwritten by Gson.
     * This is usually fine in tests since there we deserialize just once, but quite
     * dangerous in practice.
     *
     * @param instanceOfT
     */
    public MyParameterizedTypeInstanceCreator(T instanceOfT) {
      this.instanceOfT = instanceOfT;
    }
    public MyParameterizedType<T> createInstance(Type type) {
      return new MyParameterizedType<T>(instanceOfT);
    }
  }

  private static class MultiParameters<A, B, C, D, E> {
    A a;
    B b;
    C c;
    D d;
    E e;
    MultiParameters() {
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

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testDeserializeParameterizedType() throws Exception {
    BagOfPrimitives bag = new BagOfPrimitives();
    MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<BagOfPrimitives>(bag);
    Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
    Gson gson = new GsonBuilder().registerInstanceCreator(
        expectedType, new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(bagDefaultInstance))
        .create();

    String json = expected.getExpectedJson();
    MyParameterizedType<Integer> actual = gson.fromJson(json, expectedType);
    assertEquals(expected, actual);
  }

  public void testSerializeParameterizedTypes() throws Exception {
    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    String json = gson.toJson(src, typeOfSrc);
    assertEquals(src.getExpectedJson(), json);
  }

  public void testSerializeTypesWithMultipleParameters() throws Exception {
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> src =
      new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(10, 1.0F, 2.1D,
          "abc", new BagOfPrimitives());
    Type typeOfSrc = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
    String json = gson.toJson(src, typeOfSrc);
    String expected = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
      + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    assertEquals(expected, json);
  }

  public void testDeserializeTypesWithMultipleParameters() throws Exception {
    Type typeOfTarget = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
    String json = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
      + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> target =
      gson.fromJson(json, typeOfTarget);
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> expected =
      new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(10, 1.0F, 2.1D,
          "abc", new BagOfPrimitives());
    assertEquals(expected, target);
  }

  public void testSerializeParameterizedTypeWithCustomSerializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .registerSerializer(ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerSerializer(ptStringType, new MyParameterizedTypeAdapter<String>())
        .create();
    MyParameterizedType<Integer> intTarget = new MyParameterizedType<Integer>(10);
    String json = gson.toJson(intTarget, ptIntegerType);
    assertEquals(MyParameterizedTypeAdapter.<Integer>getExpectedJson(intTarget), json);

    MyParameterizedType<String> stringTarget = new MyParameterizedType<String>("abc");
    json = gson.toJson(stringTarget, ptStringType);
    assertEquals(MyParameterizedTypeAdapter.<String>getExpectedJson(stringTarget), json);
  }

  public void testDeserializeParameterizedTypesWithCustomDeserializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    Gson gson = new GsonBuilder().registerDeserializer(
        ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerDeserializer(ptStringType, new MyParameterizedTypeAdapter<String>())
        .registerInstanceCreator(ptStringType, new MyParameterizedTypeInstanceCreator<String>(""))
        .registerInstanceCreator(ptIntegerType,
            new MyParameterizedTypeInstanceCreator<Integer>(new Integer(0)))
        .create();

    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    String json = MyParameterizedTypeAdapter.<Integer>getExpectedJson(src);
    MyParameterizedType<Integer> intTarget = gson.fromJson(json, ptIntegerType);
    assertEquals(10, (int) intTarget.value);

    MyParameterizedType<String> srcStr = new MyParameterizedType<String>("abc");
    json = MyParameterizedTypeAdapter.<String>getExpectedJson(srcStr);
    MyParameterizedType<String> stringTarget = gson.fromJson(json, ptStringType);
    assertEquals("abc", stringTarget.value);
  }

  public void testDeserializeParameterizedTypeWithReader() throws Exception {
    BagOfPrimitives bag = new BagOfPrimitives();
    MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<BagOfPrimitives>(bag);
    Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
    Gson gson = new GsonBuilder().registerInstanceCreator(
        expectedType, new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(bagDefaultInstance))
        .create();

    Reader json = new StringReader(expected.getExpectedJson());
    MyParameterizedType<Integer> actual = gson.fromJson(json, expectedType);
    assertEquals(expected, actual);
  }

  public void testSerializeParameterizedTypesWithWriter() throws Exception {
    Writer writer = new StringWriter();
    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    gson.toJson(src, typeOfSrc, writer);
    assertEquals(src.getExpectedJson(), writer.toString());
  }
}
