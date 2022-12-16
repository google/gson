/*
 * Copyright (C) 2022 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.ReflectionAccessFilter.FilterResult;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class Java17RecordTest {
  private final Gson gson = new Gson();

  @Test
  public void testFirstNameIsChosenForSerialization() {
    RecordWithCustomNames target = new RecordWithCustomNames("v1", "v2");
    // Ensure name1 occurs exactly once, and name2 and name3 don't appear
    assertEquals("{\"name\":\"v1\",\"name1\":\"v2\"}", gson.toJson(target));
  }

  @Test
  public void testMultipleNamesDeserializedCorrectly() {
    assertEquals("v1", gson.fromJson("{'name':'v1'}", RecordWithCustomNames.class).a);

    // Both name1 and name2 gets deserialized to b
    assertEquals("v11", gson.fromJson("{'name': 'v1', 'name1':'v11'}", RecordWithCustomNames.class).b);
    assertEquals("v2", gson.fromJson("{'name': 'v1', 'name2':'v2'}", RecordWithCustomNames.class).b);
    assertEquals("v3", gson.fromJson("{'name': 'v1', 'name3':'v3'}", RecordWithCustomNames.class).b);
  }

  @Test
  public void testMultipleNamesInTheSameString() {
    // The last value takes precedence
    assertEquals("v3",
        gson.fromJson("{'name': 'foo', 'name1':'v1','name2':'v2','name3':'v3'}", RecordWithCustomNames.class).b);
  }

  private record RecordWithCustomNames(
      @SerializedName("name") String a,
      @SerializedName(value = "name1", alternate = {"name2", "name3"}) String b) {}

  @Test
  public void testSerializedNameOnAccessor() {
    record LocalRecord(int i) {
      @SerializedName("a")
      @Override
      public int i() {
        return i;
      }
    }

    var exception = assertThrows(JsonIOException.class, () -> gson.getAdapter(LocalRecord.class));
    assertEquals("@SerializedName on method '" + LocalRecord.class.getName() + "#i()' is not supported",
        exception.getMessage());
  }

  @Test
  public void testFieldNamingStrategy() {
    record LocalRecord(int i) {}

    Gson gson = new GsonBuilder()
        .setFieldNamingStrategy(f -> f.getName() + "-custom")
        .create();

    assertEquals("{\"i-custom\":1}", gson.toJson(new LocalRecord(1)));
    assertEquals(new LocalRecord(2), gson.fromJson("{\"i-custom\":2}", LocalRecord.class));
  }

  @Test
  public void testUnknownJsonProperty() {
    record LocalRecord(int i) {}

    // Unknown property 'x' should be ignored
    assertEquals(new LocalRecord(1), gson.fromJson("{\"i\":1,\"x\":2}", LocalRecord.class));
  }

  @Test
  public void testDuplicateJsonProperties() {
    record LocalRecord(Integer a, Integer b) {}

    String json = "{\"a\":null,\"a\":2,\"b\":1,\"b\":null}";
    // Should use value of last occurrence
    assertEquals(new LocalRecord(2, null), gson.fromJson(json, LocalRecord.class));
  }

  @Test
  public void testConstructorRuns() {
    record LocalRecord(String s) {
      LocalRecord {
        s = "custom-" + s;
      }
    }

    LocalRecord deserialized = gson.fromJson("{\"s\": null}", LocalRecord.class);
    assertEquals(new LocalRecord(null), deserialized);
    assertEquals("custom-null", deserialized.s());
  }

  /** Tests behavior when the canonical constructor throws an exception */
  @Test
  public void testThrowingConstructor() {
    record LocalRecord(String s) {
      static final RuntimeException thrownException = new RuntimeException("Custom exception");

      @SuppressWarnings("unused")
      LocalRecord {
        throw thrownException;
      }
    }

    try {
      gson.fromJson("{\"s\":\"value\"}", LocalRecord.class);
      fail();
    }
    // TODO: Adjust this once Gson throws more specific exception type
    catch (RuntimeException e) {
      assertEquals("Failed to invoke constructor '" + LocalRecord.class.getName() + "(String)' with args [value]",
          e.getMessage());
      assertSame(LocalRecord.thrownException, e.getCause());
    }
  }

  @Test
  public void testAccessorIsCalled() {
    record LocalRecord(String s) {
      @Override
      public String s() {
        return "accessor-value";
      }
    }

    assertEquals("{\"s\":\"accessor-value\"}", gson.toJson(new LocalRecord(null)));
  }

  /** Tests behavior when a record accessor method throws an exception */
  @Test
  public void testThrowingAccessor() {
    record LocalRecord(String s) {
      static final RuntimeException thrownException = new RuntimeException("Custom exception");

      @Override
      public String s() {
        throw thrownException;
      }
    }

    try {
      gson.toJson(new LocalRecord("a"));
      fail();
    } catch (JsonIOException e) {
      assertEquals("Accessor method '" + LocalRecord.class.getName() + "#s()' threw exception",
          e.getMessage());
      assertSame(LocalRecord.thrownException, e.getCause());
    }
  }

  /** Tests behavior for a record without components */
  @Test
  public void testEmptyRecord() {
    record EmptyRecord() {}

    assertEquals("{}", gson.toJson(new EmptyRecord()));
    assertEquals(new EmptyRecord(), gson.fromJson("{}", EmptyRecord.class));
  }

  /**
   * Tests behavior when {@code null} is serialized / deserialized as record value;
   * basically makes sure the adapter is 'null-safe'
   */
  @Test
  public void testRecordNull() throws IOException {
    record LocalRecord(int i) {}

    TypeAdapter<LocalRecord> adapter = gson.getAdapter(LocalRecord.class);
    assertEquals("null", adapter.toJson(null));
    assertNull(adapter.fromJson("null"));
  }

  @Test
  public void testPrimitiveDefaultValues() {
    RecordWithPrimitives expected = new RecordWithPrimitives("s", (byte) 0, (short) 0, 0, 0, 0, 0, '\0', false);
    assertEquals(expected, gson.fromJson("{'aString': 's'}", RecordWithPrimitives.class));
  }

  @Test
  public void testPrimitiveJsonNullValue() {
    String s = "{'aString': 's', 'aByte': null, 'aShort': 0}";
    var e = assertThrows(JsonParseException.class, () -> gson.fromJson(s, RecordWithPrimitives.class));
    assertEquals("null is not allowed as value for record component 'aByte' of primitive type; at path $.aByte",
        e.getMessage());
  }

  /**
   * Tests behavior when JSON contains non-null value, but custom adapter returns null
   * for primitive component
   */
  @Test
  public void testPrimitiveAdapterNullValue() {
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(byte.class, new TypeAdapter<Byte>() {
          @Override public Byte read(JsonReader in) throws IOException {
            in.skipValue();
            // Always return null
            return null;
          }

          @Override public void write(JsonWriter out, Byte value) {
            throw new AssertionError("not needed for test");
          }
        })
        .create();

    String s = "{'aString': 's', 'aByte': 0}";
    var exception = assertThrows(JsonParseException.class, () -> gson.fromJson(s, RecordWithPrimitives.class));
    assertEquals("null is not allowed as value for record component 'aByte' of primitive type; at path $.aByte",
        exception.getMessage());
  }

  private record RecordWithPrimitives(
      String aString, byte aByte, short aShort, int anInt, long aLong, float aFloat, double aDouble, char aChar, boolean aBoolean) {}

  /** Tests behavior when value of Object component is missing; should default to null */
  @Test
  public void testObjectDefaultValue() {
    record LocalRecord(String s, int i) {}

    assertEquals(new LocalRecord(null, 1), gson.fromJson("{\"i\":1}", LocalRecord.class));
  }

  /**
   * Tests serialization of a record with {@code static} field.
   *
   * <p>Important: It is not documented that this is officially supported; this
   * test just checks the current behavior.
   */
  @Test
  public void testStaticFieldSerialization() {
    // By default Gson should ignore static fields
    assertEquals("{}", gson.toJson(new RecordWithStaticField()));

    Gson gson = new GsonBuilder()
        // Include static fields
        .excludeFieldsWithModifiers(0)
        .create();

    String json = gson.toJson(new RecordWithStaticField());
    assertEquals("{\"s\":\"initial\"}", json);
  }

  /**
   * Tests deserialization of a record with {@code static} field.
   *
   * <p>Important: It is not documented that this is officially supported; this
   * test just checks the current behavior.
   */
  @Test
  public void testStaticFieldDeserialization() {
    // By default Gson should ignore static fields
    gson.fromJson("{\"s\":\"custom\"}", RecordWithStaticField.class);
    assertEquals("initial", RecordWithStaticField.s);

    Gson gson = new GsonBuilder()
        // Include static fields
        .excludeFieldsWithModifiers(0)
        .create();

    String oldValue = RecordWithStaticField.s;
    try {
      RecordWithStaticField obj = gson.fromJson("{\"s\":\"custom\"}", RecordWithStaticField.class);
      assertNotNull(obj);
      // Currently record deserialization always ignores static fields
      assertEquals("initial", RecordWithStaticField.s);
    } finally {
      RecordWithStaticField.s = oldValue;
    }
  }

  private record RecordWithStaticField() {
    static String s = "initial";
  }

  @Test
  public void testExposeAnnotation() {
    record RecordWithExpose(
        @Expose int a,
        int b
    ) {}

    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    String json = gson.toJson(new RecordWithExpose(1, 2));
    assertEquals("{\"a\":1}", json);
  }

  @Test
  public void testFieldExclusionStrategy() {
    record LocalRecord(int a, int b, double c) {}

    Gson gson = new GsonBuilder()
        .setExclusionStrategies(new ExclusionStrategy() {
          @Override public boolean shouldSkipField(FieldAttributes f) {
            return f.getName().equals("a");
          }

          @Override public boolean shouldSkipClass(Class<?> clazz) {
            return clazz == double.class;
          }
        })
        .create();

    assertEquals("{\"b\":2}", gson.toJson(new LocalRecord(1, 2, 3.0)));
  }

  @Test
  public void testJsonAdapterAnnotation() {
    record Adapter() implements JsonSerializer<String>, JsonDeserializer<String> {
      @Override public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return "deserializer-" + json.getAsString();
      }

      @Override public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive("serializer-" + src);
      }
    }
    record LocalRecord(
        @JsonAdapter(Adapter.class) String s
    ) {}

    assertEquals("{\"s\":\"serializer-a\"}", gson.toJson(new LocalRecord("a")));
    assertEquals(new LocalRecord("deserializer-a"), gson.fromJson("{\"s\":\"a\"}", LocalRecord.class));
  }

  @Test
  public void testClassReflectionFilter() {
    record Allowed(int a) {}
    record Blocked(int b) {}

    Gson gson = new GsonBuilder()
        .addReflectionAccessFilter(c -> c == Allowed.class ? FilterResult.ALLOW : FilterResult.BLOCK_ALL)
        .create();

    String json = gson.toJson(new Allowed(1));
    assertEquals("{\"a\":1}", json);

    var exception = assertThrows(JsonIOException.class, () -> gson.toJson(new Blocked(1)));
    assertEquals("ReflectionAccessFilter does not permit using reflection for class " + Blocked.class.getName() +
        ". Register a TypeAdapter for this type or adjust the access filter.",
        exception.getMessage());
  }

  @Test
  public void testReflectionFilterBlockInaccessible() {
    Gson gson = new GsonBuilder()
        .addReflectionAccessFilter(c -> FilterResult.BLOCK_INACCESSIBLE)
        .create();

    var exception = assertThrows(JsonIOException.class, () -> gson.toJson(new PrivateRecord(1)));
    assertEquals("Constructor 'com.google.gson.functional.Java17RecordTest$PrivateRecord(int)' is not accessible and"
        + " ReflectionAccessFilter does not permit making it accessible. Register a TypeAdapter for the declaring"
        + " type, adjust the access filter or increase the visibility of the element and its declaring type.",
        exception.getMessage());

    exception = assertThrows(JsonIOException.class, () -> gson.fromJson("{}", PrivateRecord.class));
    assertEquals("Constructor 'com.google.gson.functional.Java17RecordTest$PrivateRecord(int)' is not accessible and"
        + " ReflectionAccessFilter does not permit making it accessible. Register a TypeAdapter for the declaring"
        + " type, adjust the access filter or increase the visibility of the element and its declaring type.",
        exception.getMessage());

    assertEquals("{\"i\":1}", gson.toJson(new PublicRecord(1)));
    assertEquals(new PublicRecord(2), gson.fromJson("{\"i\":2}", PublicRecord.class));
  }

  private record PrivateRecord(int i) {}
  public record PublicRecord(int i) {}

  /**
   * Tests behavior when {@code java.lang.Record} is used as type for serialization
   * and deserialization.
   */
  @Test
  public void testRecordBaseClass() {
    record LocalRecord(int i) {}

    assertEquals("{}", gson.toJson(new LocalRecord(1), Record.class));

    var exception = assertThrows(JsonIOException.class, () -> gson.fromJson("{}", Record.class));
    assertEquals("Abstract classes can't be instantiated! Register an InstanceCreator or a TypeAdapter for"
        + " this type. Class name: java.lang.Record",
        exception.getMessage());
  }
}
