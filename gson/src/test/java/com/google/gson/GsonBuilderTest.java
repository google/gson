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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import org.junit.Test;

/**
 * Unit tests for {@link GsonBuilder}.
 *
 * @author Inderjeet Singh
 */
public class GsonBuilderTest {
  private static final TypeAdapter<Object> NULL_TYPE_ADAPTER = new TypeAdapter<Object>() {
    @Override public void write(JsonWriter out, Object value) {
      throw new AssertionError();
    }
    @Override public Object read(JsonReader in) {
      throw new AssertionError();
    }
  };

  @Test
  public void testCreatingMoreThanOnce() {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    assertNotNull(gson);
    assertNotNull(builder.create());

    builder.setFieldNamingStrategy(new FieldNamingStrategy() {
      @Override public String translateName(Field f) {
        return "test";
      }
    });

    Gson otherGson = builder.create();
    assertNotNull(otherGson);
    // Should be different instances because builder has been modified in the meantime
    assertNotSame(gson, otherGson);
  }

  /**
   * Gson instances should not be affected by subsequent modification of GsonBuilder
   * which created them.
   */
  @Test
  public void testModificationAfterCreate() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();

    // Modifications of `gsonBuilder` should not affect `gson` object
    gsonBuilder.registerTypeAdapter(CustomClass1.class, new TypeAdapter<CustomClass1>() {
      @Override public CustomClass1 read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException();
      }

      @Override public void write(JsonWriter out, CustomClass1 value) throws IOException {
        out.value("custom-adapter");
      }
    });
    gsonBuilder.registerTypeHierarchyAdapter(CustomClass2.class, new JsonSerializer<CustomClass2>() {
      @Override public JsonElement serialize(CustomClass2 src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive("custom-hierarchy-adapter");
      }
    });
    gsonBuilder.registerTypeAdapter(CustomClass3.class, new InstanceCreator<CustomClass3>() {
      @Override public CustomClass3 createInstance(Type type) {
        return new CustomClass3("custom-instance");
      }
    });

    assertDefaultGson(gson);
    // New GsonBuilder created from `gson` should not have been affected by changes
    // to `gsonBuilder` either
    assertDefaultGson(gson.newBuilder().create());

    // New Gson instance from modified GsonBuilder should be affected by changes
    assertCustomGson(gsonBuilder.create());
  }

  private static void assertDefaultGson(Gson gson) {
    // Should use default reflective adapter
    String json1 = gson.toJson(new CustomClass1());
    assertEquals("{}", json1);

    // Should use default reflective adapter
    String json2 = gson.toJson(new CustomClass2());
    assertEquals("{}", json2);

    // Should use default instance creator
    CustomClass3 customClass3 = gson.fromJson("{}", CustomClass3.class);
    assertEquals(CustomClass3.NO_ARG_CONSTRUCTOR_VALUE, customClass3.s);
  }

  private static void assertCustomGson(Gson gson) {
    String json1 = gson.toJson(new CustomClass1());
    assertEquals("\"custom-adapter\"", json1);

    String json2 = gson.toJson(new CustomClass2());
    assertEquals("\"custom-hierarchy-adapter\"", json2);

    CustomClass3 customClass3 = gson.fromJson("{}", CustomClass3.class);
    assertEquals("custom-instance", customClass3.s);
  }

  static class CustomClass1 { }
  static class CustomClass2 { }
  static class CustomClass3 {
    static final String NO_ARG_CONSTRUCTOR_VALUE = "default instance";

    final String s;

    public CustomClass3(String s) {
      this.s = s;
    }

    public CustomClass3() {
      this(NO_ARG_CONSTRUCTOR_VALUE);
    }
  }

  @Test
  public void testExcludeFieldsWithModifiers() {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.VOLATILE, Modifier.PRIVATE)
        .create();
    assertEquals("{\"d\":\"d\"}", gson.toJson(new HasModifiers()));
  }

  @SuppressWarnings("unused")
  static class HasModifiers {
    private String a = "a";
    volatile String b = "b";
    private volatile String c = "c";
    String d = "d";
  }

  @Test
  public void testTransientFieldExclusion() {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers()
        .create();
    assertEquals("{\"a\":\"a\"}", gson.toJson(new HasTransients()));
  }

  static class HasTransients {
    transient String a = "a";
  }

  @Test
  public void testRegisterTypeAdapterForCoreType() {
    Type[] types = {
        byte.class,
        int.class,
        double.class,
        Short.class,
        Long.class,
        String.class,
    };
    for (Type type : types) {
      new GsonBuilder().registerTypeAdapter(type, NULL_TYPE_ADAPTER);
    }
  }

  @Test
  public void testDisableJdkUnsafe() {
    Gson gson = new GsonBuilder()
        .disableJdkUnsafe()
        .create();
    try {
      gson.fromJson("{}", ClassWithoutNoArgsConstructor.class);
      fail("Expected exception");
    } catch (JsonIOException expected) {
      assertEquals(
        "Unable to create instance of class com.google.gson.GsonBuilderTest$ClassWithoutNoArgsConstructor; "
        + "usage of JDK Unsafe is disabled. Registering an InstanceCreator or a TypeAdapter for this type, "
        + "adding a no-args constructor, or enabling usage of JDK Unsafe may fix this problem.",
        expected.getMessage()
      );
    }
  }

  private static class ClassWithoutNoArgsConstructor {
    @SuppressWarnings("unused")
    public ClassWithoutNoArgsConstructor(String s) {
    }
  }

  @Test
  public void testSetVersionInvalid() {
    GsonBuilder builder = new GsonBuilder();
    try {
      builder.setVersion(Double.NaN);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid version: NaN", e.getMessage());
    }

    try {
      builder.setVersion(-0.1);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid version: -0.1", e.getMessage());
    }
  }
}
