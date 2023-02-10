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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.common.TestTypes.Base;
import com.google.gson.common.TestTypes.ClassWithBaseField;
import java.lang.reflect.Type;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional Test exercising custom deserialization only. When test applies to both
 * serialization and deserialization then add it to CustomTypeAdapterTest.
 *
 * @author Joel Leitch
 */
public class CustomDeserializerTest {
  private static final String DEFAULT_VALUE = "test123";
  private static final String SUFFIX = "blah";

  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new GsonBuilder().registerTypeAdapter(DataHolder.class, new DataHolderDeserializer()).create();
  }

  @Test
  public void testDefaultConstructorNotCalledOnObject() {
    DataHolder data = new DataHolder(DEFAULT_VALUE);
    String json = gson.toJson(data);

    DataHolder actual = gson.fromJson(json, DataHolder.class);
    assertThat(actual.getData()).isEqualTo(DEFAULT_VALUE + SUFFIX);
  }

  @Test
  public void testDefaultConstructorNotCalledOnField() {
    DataHolderWrapper dataWrapper = new DataHolderWrapper(new DataHolder(DEFAULT_VALUE));
    String json = gson.toJson(dataWrapper);

    DataHolderWrapper actual = gson.fromJson(json, DataHolderWrapper.class);
    assertThat(actual.getWrappedData().getData()).isEqualTo(DEFAULT_VALUE + SUFFIX);
  }

  private static class DataHolder {
    private final String data;

    // For use by Gson
    @SuppressWarnings("unused")
    private DataHolder() {
      throw new IllegalStateException();
    }

    public DataHolder(String data) {
      this.data = data;
    }

    public String getData() {
      return data;
    }
  }

  private static class DataHolderWrapper {
    private final DataHolder wrappedData;

    // For use by Gson
    @SuppressWarnings("unused")
    private DataHolderWrapper() {
      this(new DataHolder(DEFAULT_VALUE));
    }

    public DataHolderWrapper(DataHolder data) {
      this.wrappedData = data;
    }

    public DataHolder getWrappedData() {
      return wrappedData;
    }
  }

  private static class DataHolderDeserializer implements JsonDeserializer<DataHolder> {
    @Override
    public DataHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonObject jsonObj = json.getAsJsonObject();
      String dataString = jsonObj.get("data").getAsString();
      return new DataHolder(dataString + SUFFIX);
    }
  }

  @Test
  public void testJsonTypeFieldBasedDeserialization() {
    String json = "{field1:'abc',field2:'def',__type__:'SUB_TYPE1'}";
    Gson gson = new GsonBuilder().registerTypeAdapter(MyBase.class, new JsonDeserializer<MyBase>() {
      @Override public MyBase deserialize(JsonElement json, Type pojoType,
          JsonDeserializationContext context) throws JsonParseException {
        String type = json.getAsJsonObject().get(MyBase.TYPE_ACCESS).getAsString();
        return context.deserialize(json, SubTypes.valueOf(type).getSubclass());
      }
    }).create();
    SubType1 target = (SubType1) gson.fromJson(json, MyBase.class);
    assertThat(target.field1).isEqualTo("abc");
  }

  private static class MyBase {
    static final String TYPE_ACCESS = "__type__";
  }

  private enum SubTypes {
    SUB_TYPE1(SubType1.class),
    SUB_TYPE2(SubType2.class);
    private final Type subClass;
    private SubTypes(Type subClass) {
      this.subClass = subClass;
    }
    public Type getSubclass() {
      return subClass;
    }
  }

  private static class SubType1 extends MyBase {
    String field1;
  }

  private static class SubType2 extends MyBase {
    @SuppressWarnings("unused")
    String field2;
  }

  @Test
  public void testCustomDeserializerReturnsNullForTopLevelObject() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        @Override
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "{baseName:'Base',subName:'SubRevised'}";
    Base target = gson.fromJson(json, Base.class);
    assertThat(target).isNull();
  }

  @Test
  public void testCustomDeserializerReturnsNull() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        @Override
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "{base:{baseName:'Base',subName:'SubRevised'}}";
    ClassWithBaseField target = gson.fromJson(json, ClassWithBaseField.class);
    assertThat(target.base).isNull();
  }

  @Test
  public void testCustomDeserializerReturnsNullForArrayElements() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        @Override
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "[{baseName:'Base'},{baseName:'Base'}]";
    Base[] target = gson.fromJson(json, Base[].class);
    assertThat(target[0]).isNull();
    assertThat(target[1]).isNull();
  }

  @Test
  public void testCustomDeserializerReturnsNullForArrayElementsForArrayField() {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Base.class, new JsonDeserializer<Base>() {
        @Override
        public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
          return null;
        }
      }).create();
    String json = "{bases:[{baseName:'Base'},{baseName:'Base'}]}";
    ClassWithBaseArray target = gson.fromJson(json, ClassWithBaseArray.class);
    assertThat(target.bases[0]).isNull();
    assertThat(target.bases[1]).isNull();
  }

  private static final class ClassWithBaseArray {
    Base[] bases;
  }
}
