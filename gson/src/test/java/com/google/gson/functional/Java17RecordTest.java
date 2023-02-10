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

import static com.google.common.truth.Truth.assertThat;
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
    assertThat(gson.toJson(target)).isEqualTo("{\"name\":\"v1\",\"name1\":\"v2\"}");
  }

  @Test
  public void testMultipleNamesDeserializedCorrectly() {
    assertThat(gson.fromJson("{'name':'v1'}", RecordWithCustomNames.class).a).isEqualTo("v1");

    // Both name1 and name2 gets deserialized to b
    assertThat(gson.fromJson("{'name': 'v1', 'name1':'v11'}", RecordWithCustomNames.class).b).isEqualTo("v11");
    assertThat(gson.fromJson("{'name': 'v1', 'name2':'v2'}", RecordWithCustomNames.class).b).isEqualTo("v2");
    assertThat(gson.fromJson("{'name': 'v1', 'name3':'v3'}", RecordWithCustomNames.class).b).isEqualTo("v3");
  }

  @Test
  public void testMultipleNamesInTheSameString() {
    // The last value takes precedence
    assertThat(gson.fromJson("{'name': 'foo', 'name1':'v1','name2':'v2','name3':'v3'}", RecordWithCustomNames.class).b)
        .isEqualTo("v3");
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
    assertThat(exception).hasMessageThat()
            .isEqualTo("@SerializedName on method '" + LocalRecord.class.getName() + "#i()' is not supported");
  }

  @Test
  public void testFieldNamingStrategy() {
    record LocalRecord(int i) {}

    Gson gson = new GsonBuilder()
        .setFieldNamingStrategy(f -> f.getName() + "-custom")
        .create();

    assertThat(gson.toJson(new LocalRecord(1))).isEqualTo("{\"i-custom\":1}");
    assertThat(gson.fromJson("{\"i-custom\":2}", LocalRecord.class)).isEqualTo(new LocalRecord(2));
  }

  @Test
  public void testUnknownJsonProperty() {
    record LocalRecord(int i) {}

    // Unknown property 'x' should be ignored
    assertThat(gson.fromJson("{\"i\":1,\"x\":2}", LocalRecord.class)).isEqualTo(new LocalRecord(1));
  }

  @Test
  public void testDuplicateJsonProperties() {
    record LocalRecord(Integer a, Integer b) {}

    String json = "{\"a\":null,\"a\":2,\"b\":1,\"b\":null}";
    // Should use value of last occurrence
    assertThat(gson.fromJson(json, LocalRecord.class)).isEqualTo(new LocalRecord(2, null));
  }

  @Test
  public void testConstructorRuns() {
    record LocalRecord(String s) {
      LocalRecord {
        s = "custom-" + s;
      }
    }

    LocalRecord deserialized = gson.fromJson("{\"s\": null}", LocalRecord.class);
    assertThat(deserialized).isEqualTo(new LocalRecord(null));
    assertThat(deserialized.s()).isEqualTo("custom-null");
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
      assertThat(e).hasMessageThat()
              .isEqualTo("Failed to invoke constructor '" + LocalRecord.class.getName() + "(String)' with args [value]");
      assertThat(e).hasCauseThat().isSameInstanceAs(LocalRecord.thrownException);
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

    assertThat(gson.toJson(new LocalRecord(null))).isEqualTo("{\"s\":\"accessor-value\"}");
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
      assertThat(e).hasMessageThat()
          .isEqualTo("Accessor method '" + LocalRecord.class.getName() + "#s()' threw exception");
      assertThat(e).hasCauseThat().isSameInstanceAs(LocalRecord.thrownException);
    }
  }

  /** Tests behavior for a record without components */
  @Test
  public void testEmptyRecord() {
    record EmptyRecord() {}

    assertThat(gson.toJson(new EmptyRecord())).isEqualTo("{}");
    assertThat(gson.fromJson("{}", EmptyRecord.class)).isEqualTo(new EmptyRecord());
  }

  /**
   * Tests behavior when {@code null} is serialized / deserialized as record value;
   * basically makes sure the adapter is 'null-safe'
   */
  @Test
  public void testRecordNull() throws IOException {
    record LocalRecord(int i) {}

    TypeAdapter<LocalRecord> adapter = gson.getAdapter(LocalRecord.class);
    assertThat(adapter.toJson(null)).isEqualTo("null");
    assertThat(adapter.fromJson("null")).isNull();
  }

  @Test
  public void testPrimitiveDefaultValues() {
    RecordWithPrimitives expected = new RecordWithPrimitives("s", (byte) 0, (short) 0, 0, 0, 0, 0, '\0', false);
    assertThat(gson.fromJson("{'aString': 's'}", RecordWithPrimitives.class)).isEqualTo(expected);
  }

  @Test
  public void testPrimitiveJsonNullValue() {
    String s = "{'aString': 's', 'aByte': null, 'aShort': 0}";
    var e = assertThrows(JsonParseException.class, () -> gson.fromJson(s, RecordWithPrimitives.class));
    assertThat(e).hasMessageThat()
            .isEqualTo("null is not allowed as value for record component 'aByte' of primitive type; at path $.aByte");
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
    assertThat(exception).hasMessageThat()
        .isEqualTo("null is not allowed as value for record component 'aByte' of primitive type; at path $.aByte");
  }

  private record RecordWithPrimitives(
      String aString, byte aByte, short aShort, int anInt, long aLong, float aFloat, double aDouble, char aChar, boolean aBoolean) {}

  /** Tests behavior when value of Object component is missing; should default to null */
  @Test
  public void testObjectDefaultValue() {
    record LocalRecord(String s, int i) {}

    assertThat(gson.fromJson("{\"i\":1}", LocalRecord.class)).isEqualTo(new LocalRecord(null, 1));
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
    assertThat(gson.toJson(new RecordWithStaticField())).isEqualTo("{}");

    Gson gson = new GsonBuilder()
        // Include static fields
        .excludeFieldsWithModifiers(0)
        .create();

    String json = gson.toJson(new RecordWithStaticField());
    assertThat(json).isEqualTo("{\"s\":\"initial\"}");
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
    assertThat(RecordWithStaticField.s).isEqualTo("initial");

    Gson gson = new GsonBuilder()
        // Include static fields
        .excludeFieldsWithModifiers(0)
        .create();

    String oldValue = RecordWithStaticField.s;
    try {
      RecordWithStaticField obj = gson.fromJson("{\"s\":\"custom\"}", RecordWithStaticField.class);
      assertThat(obj).isNotNull();
      // Currently record deserialization always ignores static fields
      assertThat(RecordWithStaticField.s).isEqualTo("initial");
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
    assertThat(json).isEqualTo("{\"a\":1}");
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

    assertThat(gson.toJson(new LocalRecord(1, 2, 3.0))).isEqualTo("{\"b\":2}");
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

    assertThat(gson.toJson(new LocalRecord("a"))).isEqualTo("{\"s\":\"serializer-a\"}");
    assertThat(gson.fromJson("{\"s\":\"a\"}", LocalRecord.class)).isEqualTo(new LocalRecord("deserializer-a"));
  }

  @Test
  public void testClassReflectionFilter() {
    record Allowed(int a) {}
    record Blocked(int b) {}

    Gson gson = new GsonBuilder()
        .addReflectionAccessFilter(c -> c == Allowed.class ? FilterResult.ALLOW : FilterResult.BLOCK_ALL)
        .create();

    String json = gson.toJson(new Allowed(1));
    assertThat(json).isEqualTo("{\"a\":1}");

    var exception = assertThrows(JsonIOException.class, () -> gson.toJson(new Blocked(1)));
    assertThat(exception).hasMessageThat()
            .isEqualTo("ReflectionAccessFilter does not permit using reflection for class " + Blocked.class.getName() +
                ". Register a TypeAdapter for this type or adjust the access filter.");
  }

  @Test
  public void testReflectionFilterBlockInaccessible() {
    Gson gson = new GsonBuilder()
        .addReflectionAccessFilter(c -> FilterResult.BLOCK_INACCESSIBLE)
        .create();

    var exception = assertThrows(JsonIOException.class, () -> gson.toJson(new PrivateRecord(1)));
    assertThat(exception).hasMessageThat()
            .isEqualTo("Constructor 'com.google.gson.functional.Java17RecordTest$PrivateRecord(int)' is not accessible and"
                + " ReflectionAccessFilter does not permit making it accessible. Register a TypeAdapter for the declaring"
                + " type, adjust the access filter or increase the visibility of the element and its declaring type.");

    exception = assertThrows(JsonIOException.class, () -> gson.fromJson("{}", PrivateRecord.class));
    assertThat(exception).hasMessageThat()
            .isEqualTo("Constructor 'com.google.gson.functional.Java17RecordTest$PrivateRecord(int)' is not accessible and"
                + " ReflectionAccessFilter does not permit making it accessible. Register a TypeAdapter for the declaring"
                + " type, adjust the access filter or increase the visibility of the element and its declaring type.");

    assertThat(gson.toJson(new PublicRecord(1))).isEqualTo("{\"i\":1}");
    assertThat(gson.fromJson("{\"i\":2}", PublicRecord.class)).isEqualTo(new PublicRecord(2));
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

    assertThat(gson.toJson(new LocalRecord(1), Record.class)).isEqualTo("{}");

    var exception = assertThrows(JsonIOException.class, () -> gson.fromJson("{}", Record.class));
    assertThat(exception).hasMessageThat()
            .isEqualTo("Abstract classes can't be instantiated! Register an InstanceCreator or a TypeAdapter for"
                + " this type. Class name: java.lang.Record");
  }
}
