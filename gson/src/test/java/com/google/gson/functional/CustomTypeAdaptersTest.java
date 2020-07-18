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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.ClassWithCustomTypeConverter;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Functional tests for the support of custom serializer and deserializers.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class CustomTypeAdaptersTest extends TestCase {
  private GsonBuilder builder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    builder = new GsonBuilder();
  }

  public void testCustomSerializers() {
    Gson gson = builder.registerTypeAdapter(
        ClassWithCustomTypeConverter.class, new JsonSerializer<ClassWithCustomTypeConverter>() {
          @Override public JsonElement serialize(ClassWithCustomTypeConverter src, Type typeOfSrc,
              JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("bag", 5);
        json.addProperty("value", 25);
        return json;
      }
    }).create();
    ClassWithCustomTypeConverter target = new ClassWithCustomTypeConverter();
    assertEquals("{\"bag\":5,\"value\":25}", gson.toJson(target));
  }

  public void testCustomDeserializers() {
    Gson gson = new GsonBuilder().registerTypeAdapter(
        ClassWithCustomTypeConverter.class, new JsonDeserializer<ClassWithCustomTypeConverter>() {
          @Override public ClassWithCustomTypeConverter deserialize(JsonElement json, Type typeOfT,
              JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        int value = jsonObject.get("bag").getAsInt();
        return new ClassWithCustomTypeConverter(new BagOfPrimitives(value,
            value, false, ""), value);
      }
    }).create();
    String json = "{\"bag\":5,\"value\":25}";
    ClassWithCustomTypeConverter target = gson.fromJson(json, ClassWithCustomTypeConverter.class);
    assertEquals(5, target.getBag().getIntValue());
  }

  public void disable_testCustomSerializersOfSelf() {
    Gson gson = createGsonObjectWithFooTypeAdapter();
    Gson basicGson = new Gson();
    Foo newFooObject = new Foo(1, 2L);
    String jsonFromCustomSerializer = gson.toJson(newFooObject);
    String jsonFromGson = basicGson.toJson(newFooObject);

    assertEquals(jsonFromGson, jsonFromCustomSerializer);
  }

  public void disable_testCustomDeserializersOfSelf() {
    Gson gson = createGsonObjectWithFooTypeAdapter();
    Gson basicGson = new Gson();
    Foo expectedFoo = new Foo(1, 2L);
    String json = basicGson.toJson(expectedFoo);
    Foo newFooObject = gson.fromJson(json, Foo.class);

    assertEquals(expectedFoo.key, newFooObject.key);
    assertEquals(expectedFoo.value, newFooObject.value);
  }

  public void testCustomNestedSerializers() {
    Gson gson = new GsonBuilder().registerTypeAdapter(
        BagOfPrimitives.class, new JsonSerializer<BagOfPrimitives>() {
          @Override public JsonElement serialize(BagOfPrimitives src, Type typeOfSrc,
          JsonSerializationContext context) {
        return new JsonPrimitive(6);
      }
    }).create();
    ClassWithCustomTypeConverter target = new ClassWithCustomTypeConverter();
    assertEquals("{\"bag\":6,\"value\":10}", gson.toJson(target));
  }

  public void testCustomNestedDeserializers() {
    Gson gson = new GsonBuilder().registerTypeAdapter(
        BagOfPrimitives.class, new JsonDeserializer<BagOfPrimitives>() {
          @Override public BagOfPrimitives deserialize(JsonElement json, Type typeOfT,
          JsonDeserializationContext context) throws JsonParseException {
        int value = json.getAsInt();
        return new BagOfPrimitives(value, value, false, "");
      }
    }).create();
    String json = "{\"bag\":7,\"value\":25}";
    ClassWithCustomTypeConverter target = gson.fromJson(json, ClassWithCustomTypeConverter.class);
    assertEquals(7, target.getBag().getIntValue());
  }

  public void testCustomTypeAdapterDoesNotAppliesToSubClasses() {
    Gson gson = new GsonBuilder().registerTypeAdapter(Base.class, new JsonSerializer<Base> () {
      @Override
      public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("value", src.baseValue);
        return json;
      }
    }).create();
    Base b = new Base();
    String json = gson.toJson(b);
    assertTrue(json.contains("value"));
    b = new Derived();
    json = gson.toJson(b);
    assertTrue(json.contains("derivedValue"));
  }

  public void testCustomTypeAdapterAppliesToSubClassesSerializedAsBaseClass() {
    Gson gson = new GsonBuilder().registerTypeAdapter(Base.class, new JsonSerializer<Base> () {
      @Override
      public JsonElement serialize(Base src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("value", src.baseValue);
        return json;
      }
    }).create();
    Base b = new Base();
    String json = gson.toJson(b);
    assertTrue(json.contains("value"));
    b = new Derived();
    json = gson.toJson(b, Base.class);
    assertTrue(json.contains("value"));
    assertFalse(json.contains("derivedValue"));
  }

  private static class Base {
    int baseValue = 2;
  }

  private static class Derived extends Base {
    @SuppressWarnings("unused")
    int derivedValue = 3;
  }


  private Gson createGsonObjectWithFooTypeAdapter() {
    return new GsonBuilder().registerTypeAdapter(Foo.class, new FooTypeAdapter()).create();
  }

  public static class Foo {
    private final int key;
    private final long value;

    public Foo() {
      this(0, 0L);
    }

    public Foo(int key, long value) {
      this.key = key;
      this.value = value;
    }
  }

  public static final class FooTypeAdapter implements JsonSerializer<Foo>, JsonDeserializer<Foo> {
    @Override
    public Foo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return context.deserialize(json, typeOfT);
    }

    @Override
    public JsonElement serialize(Foo src, Type typeOfSrc, JsonSerializationContext context) {
      return context.serialize(src, typeOfSrc);
    }
  }

  public void testCustomSerializerInvokedForPrimitives() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(boolean.class, new JsonSerializer<Boolean>() {
          @Override public JsonElement serialize(Boolean s, Type t, JsonSerializationContext c) {
            return new JsonPrimitive(s ? 1 : 0);
          }
        })
        .create();
    assertEquals("1", gson.toJson(true, boolean.class));
    assertEquals("true", gson.toJson(true, Boolean.class));
  }

  @SuppressWarnings("rawtypes")
  public void testCustomDeserializerInvokedForPrimitives() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(boolean.class, new JsonDeserializer() {
          @Override
          public Object deserialize(JsonElement json, Type t, JsonDeserializationContext context) {
            return json.getAsInt() != 0;
          }
        })
        .create();
    assertEquals(Boolean.TRUE, gson.fromJson("1", boolean.class));
    assertEquals(Boolean.TRUE, gson.fromJson("true", Boolean.class));
  }

  public void testCustomByteArraySerializer() {
    Gson gson = new GsonBuilder().registerTypeAdapter(byte[].class, new JsonSerializer<byte[]>() {
      @Override
      public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        StringBuilder sb = new StringBuilder(src.length);
        for (byte b : src) {
          sb.append(b);
        }
        return new JsonPrimitive(sb.toString());
      }
    }).create();
    byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    String json = gson.toJson(data);
    assertEquals("\"0123456789\"", json);
  }

  public void testCustomByteArrayDeserializerAndInstanceCreator() {
    GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(byte[].class,
        new JsonDeserializer<byte[]>() {
          @Override public byte[] deserialize(JsonElement json,
              Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String str = json.getAsString();
        byte[] data = new byte[str.length()];
        for (int i = 0; i < data.length; ++i) {
          data[i] = Byte.parseByte(""+str.charAt(i));
        }
        return data;
      }
    });
    Gson gson = gsonBuilder.create();
    String json = "'0123456789'";
    byte[] actual = gson.fromJson(json, byte[].class);
    byte[] expected = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    for (int i = 0; i < actual.length; ++i) {
      assertEquals(expected[i], actual[i]);
    }
  }

  private static final class StringHolder {
    String part1;
    String part2;

    public StringHolder(String string) {
      String[] parts = string.split(":");
      part1 = parts[0];
      part2 = parts[1];
    }
    public StringHolder(String part1, String part2) {
      this.part1 = part1;
      this.part2 = part2;
    }
  }

  private static class StringHolderTypeAdapter implements JsonSerializer<StringHolder>,
      JsonDeserializer<StringHolder>, InstanceCreator<StringHolder> {

    @Override public StringHolder createInstance(Type type) {
      //Fill up with objects that will be thrown away
      return new StringHolder("unknown:thing");
    }

    @Override public StringHolder deserialize(JsonElement src, Type type,
        JsonDeserializationContext context) {
      return new StringHolder(src.getAsString());
    }

    @Override public JsonElement serialize(StringHolder src, Type typeOfSrc,
        JsonSerializationContext context) {
      String contents = src.part1 + ':' + src.part2;
      return new JsonPrimitive(contents);
    }
  }

  // Test created from Issue 70
  public void testCustomAdapterInvokedForCollectionElementSerializationWithType() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    Type setType = new TypeToken<Set<StringHolder>>() {}.getType();
    StringHolder holder = new StringHolder("Jacob", "Tomaw");
    Set<StringHolder> setOfHolders = new HashSet<StringHolder>();
    setOfHolders.add(holder);
    String json = gson.toJson(setOfHolders, setType);
    assertTrue(json.contains("Jacob:Tomaw"));
  }

  // Test created from Issue 70
  public void testCustomAdapterInvokedForCollectionElementSerialization() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    StringHolder holder = new StringHolder("Jacob", "Tomaw");
    Set<StringHolder> setOfHolders = new HashSet<StringHolder>();
    setOfHolders.add(holder);
    String json = gson.toJson(setOfHolders);
    assertTrue(json.contains("Jacob:Tomaw"));
  }

  // Test created from Issue 70
  public void testCustomAdapterInvokedForCollectionElementDeserialization() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    Type setType = new TypeToken<Set<StringHolder>>() {}.getType();
    Set<StringHolder> setOfHolders = gson.fromJson("['Jacob:Tomaw']", setType);
    assertEquals(1, setOfHolders.size());
    StringHolder foo = setOfHolders.iterator().next();
    assertEquals("Jacob", foo.part1);
    assertEquals("Tomaw", foo.part2);
  }

  // Test created from Issue 70
  public void testCustomAdapterInvokedForMapElementSerializationWithType() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    Type mapType = new TypeToken<Map<String,StringHolder>>() {}.getType();
    StringHolder holder = new StringHolder("Jacob", "Tomaw");
    Map<String, StringHolder> mapOfHolders = new HashMap<String, StringHolder>();
    mapOfHolders.put("foo", holder);
    String json = gson.toJson(mapOfHolders, mapType);
    assertTrue(json.contains("\"foo\":\"Jacob:Tomaw\""));
  }

  // Test created from Issue 70
  public void testCustomAdapterInvokedForMapElementSerialization() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    StringHolder holder = new StringHolder("Jacob", "Tomaw");
    Map<String, StringHolder> mapOfHolders = new HashMap<String, StringHolder>();
    mapOfHolders.put("foo", holder);
    String json = gson.toJson(mapOfHolders);
    assertTrue(json.contains("\"foo\":\"Jacob:Tomaw\""));
  }

  // Test created from Issue 70
  public void testCustomAdapterInvokedForMapElementDeserialization() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(StringHolder.class, new StringHolderTypeAdapter())
      .create();
    Type mapType = new TypeToken<Map<String, StringHolder>>() {}.getType();
    Map<String, StringHolder> mapOfFoo = gson.fromJson("{'foo':'Jacob:Tomaw'}", mapType);
    assertEquals(1, mapOfFoo.size());
    StringHolder foo = mapOfFoo.get("foo");
    assertEquals("Jacob", foo.part1);
    assertEquals("Tomaw", foo.part2);
  }

  public void testEnsureCustomSerializerNotInvokedForNullValues() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DataHolder.class, new DataHolderSerializer())
        .create();
    DataHolderWrapper target = new DataHolderWrapper(new DataHolder("abc"));
    String json = gson.toJson(target);
    assertEquals("{\"wrappedData\":{\"myData\":\"abc\"}}", json);
  }

  public void testEnsureCustomDeserializerNotInvokedForNullValues() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DataHolder.class, new DataHolderDeserializer())
        .create();
    String json = "{wrappedData:null}";
    DataHolderWrapper actual = gson.fromJson(json, DataHolderWrapper.class);
    assertNull(actual.wrappedData);
  }

  // Test created from Issue 352
  public void testRegisterHierarchyAdapterForDate() {
    Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Date.class, new DateTypeAdapter())
        .create();
    assertEquals("0", gson.toJson(new Date(0)));
    assertEquals("0", gson.toJson(new java.sql.Date(0)));
    assertEquals(new Date(0), gson.fromJson("0", Date.class));
    assertEquals(new java.sql.Date(0), gson.fromJson("0", java.sql.Date.class));
  }

  private static class DataHolder {
    final String data;

    public DataHolder(String data) {
      this.data = data;
    }
  }

  private static class DataHolderWrapper {
    final DataHolder wrappedData;

    public DataHolderWrapper(DataHolder data) {
      this.wrappedData = data;
    }
  }

  private static class DataHolderSerializer implements JsonSerializer<DataHolder> {
    @Override
    public JsonElement serialize(DataHolder src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.addProperty("myData", src.data);
      return obj;
    }
  }

  private static class DataHolderDeserializer implements JsonDeserializer<DataHolder> {
    @Override
    public DataHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonObject jsonObj = json.getAsJsonObject();
      JsonElement jsonElement = jsonObj.get("data");
      if (jsonElement == null || jsonElement.isJsonNull()) {
        return new DataHolder(null);
      }
      return new DataHolder(jsonElement.getAsString());
    }
  }

  private static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
      return typeOfT == Date.class
          ? new Date(json.getAsLong())
          : new java.sql.Date(json.getAsLong());
    }
    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getTime());
    }
  }
}
