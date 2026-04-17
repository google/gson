/*
 * Copyright (C) 2026 Google Inc.
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
package com.google.gson.protobuf.functional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.protobuf.LegacyProtoTypeAdapterFactory;
import com.google.gson.protobuf.TestAllTypes;
import com.google.gson.protobuf.TestAllTypes.NestedMessage;
import com.google.gson.protobuf.TestDuration;
import com.google.gson.protobuf.TestMap;
import com.google.gson.protobuf.TestOneof;
import com.google.gson.protobuf2.TestAllTypesProto2;
import com.google.gson.protobuf2.TestManyOptionals;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.Message;
import com.google.protobuf.NullValue;
import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import com.google.testing.junit.testparameterinjector.TestParameterValue;
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestParameterInjector.class)
public final class LegacyProtoTypeAdapterFactoryTest {
  // A reminder that RTAF means "ReflectiveTypeAdapterFactory", which is the fallback that Gson
  // uses for classes that don't have an explicit TypeAdapter. See the comments in
  // LegacyProtoTypeAdapterFactory.java for more details.

  private static final Gson RTAF_GSON = new Gson();
  private static final Gson GSON_WITH_LEGACY_ADAPTER =
      new GsonBuilder().registerTypeAdapterFactory(LegacyProtoTypeAdapterFactory.INSTANCE).create();

  private static final ImmutableMap<String, Message> SHARED_TEST_MESSAGES =
      ImmutableMap.<String, Message>builder()
          .put("TestDuration with no Duration", TestDuration.newBuilder().build())
          .put(
              "TestDuration with empty Duration",
              TestDuration.newBuilder()
                  .setDurationValue(com.google.protobuf.Duration.getDefaultInstance())
                  .build())
          .put(
              "TestDuration with large Duration",
              TestDuration.newBuilder()
                  .setDurationValue(
                      com.google.protobuf.Duration.newBuilder()
                          .setSeconds(300_000_000_000L)
                          .setNanos(67890))
                  .build())
          .put("ManyOptionals with nothing set", TestManyOptionals.getDefaultInstance())
          .put(
              "ManyOptionals with some fields set to their defaults",
              TestManyOptionals.newBuilder()
                  .setOptionalX1(0)
                  .setOptionalX8(0)
                  .setOptionalX40(0)
                  .setOptionalX41(41) // not default
                  .build())
          .put("Oneof with string", TestOneof.newBuilder().setOneofString("hello").build())
          .buildOrThrow();

  private static final ImmutableMap<String, Message> SERIALIZED_WITH_RTAF =
      ImmutableMap.<String, Message>builder()
          .putAll(SHARED_TEST_MESSAGES)
          .put("TestAllTypes with nothing set", TestAllTypes.getDefaultInstance())
          .put("TestAllTypes with everything set", TestMessages.testAllTypes())
          .put(
              "TestAllTypesProto2",
              TestAllTypesProto2.newBuilder()
                  .setOptionalInt32(1234)
                  .setOptionalString("hello")
                  .setOptionalNestedEnum(TestAllTypesProto2.NestedEnum.BAR)
                  .addRepeatedString("hello")
                  .addRepeatedString("world")
                  .addRepeatedInt32(1234)
                  .build())
          .buildOrThrow();

  private static final ImmutableMap<String, Message> DESERIALIZED_WITH_RTAF =
      ImmutableMap.<String, Message>builder().putAll(SHARED_TEST_MESSAGES).buildOrThrow();

  private static final ImmutableMap<String, Message> ONEOF_MESSAGES =
      ImmutableMap.<String, Message>builder()
          .put("Oneof with nothing set", TestOneof.getDefaultInstance())
          .put("Oneof with int32", TestOneof.newBuilder().setOneofInt32(123).build())
          .put(
              "Oneof with nested message",
              TestOneof.newBuilder()
                  .setOneofNestedMessage(NestedMessage.newBuilder().setValue(123))
                  .build())
          .put(
              "Oneof with NullValue",
              TestOneof.newBuilder().setOneofNullValue(NullValue.NULL_VALUE).build())
          .put("Oneof with string", TestOneof.newBuilder().setOneofString("hello").build())
          .buildOrThrow();

  /**
   * Tests that messages can be serialized by the RTAF logic and then deserialized by a Gson
   * instance with {@link LegacyProtoTypeAdapterFactory} registered.
   */
  @Test
  public void rtafToLegacyCompat(
      @TestParameter(valuesProvider = RtafToLegacyCompatProvider.class) Message message,
      @TestParameter({
            "IDENTITY",
            "UPPER_CAMEL_CASE",
            "UPPER_CASE_WITH_UNDERSCORES",
            "LOWER_CASE_WITH_UNDERSCORES",
            "LOWER_CASE_WITH_DASHES"
          })
          FieldNamingPolicy fieldNamingPolicy) {
    String json = applyFieldNamingPolicy(RTAF_GSON, fieldNamingPolicy).toJson(message);
    Message compatMessage =
        applyFieldNamingPolicy(GSON_WITH_LEGACY_ADAPTER, fieldNamingPolicy)
            .fromJson(json, message.getClass());
    assertThat(compatMessage).isEqualTo(message);
    assertThat(compatMessage.getSerializedSize()).isEqualTo(message.getSerializedSize());
  }

  /**
   * Tests that {@link LegacyProtoTypeAdapterFactory} produces JSON that is equivalent to what RTAF
   * produces.
   */
  @Test
  public void legacyCompatOutputsSameJsonAsRtaf(
      @TestParameter(valuesProvider = RtafToLegacyCompatProvider.class) Message message,
      @TestParameter({
            "IDENTITY",
            "UPPER_CAMEL_CASE",
            "UPPER_CASE_WITH_UNDERSCORES",
            "LOWER_CASE_WITH_UNDERSCORES",
            "LOWER_CASE_WITH_DASHES"
          })
          FieldNamingPolicy fieldNamingPolicy) {
    JsonObject rtafJson =
        applyFieldNamingPolicy(RTAF_GSON, fieldNamingPolicy).toJsonTree(message).getAsJsonObject();
    JsonObject legacyJson =
        applyFieldNamingPolicy(GSON_WITH_LEGACY_ADAPTER, fieldNamingPolicy)
            .toJsonTree(message)
            .getAsJsonObject();
    // Rewrite both JSONs to remove fields that may harmlessly differ between the two.
    rewriteJsonObject(rtafJson);
    rewriteJsonObject(legacyJson);
    assertThat(legacyJson).isEqualTo(rtafJson);

    // If the following assertion failed, it would mostly be harmless, but it would mean that some
    // golden-file tests in google3 might have to be updated after migrating to
    // LegacyProtoTypeAdapterFactory. (It's actually a bit surprising that the order of JSON object
    // members is the same in both cases, for all our tests. They're not sorted or anything.)
    assertThat(legacyJson.toString()).isEqualTo(rtafJson.toString());
  }

  private static void rewriteJsonElement(JsonElement json) {
    if (json.isJsonObject()) {
      rewriteJsonObject(json.getAsJsonObject());
    } else if (json.isJsonArray()) {
      rewriteJsonArray(json.getAsJsonArray());
    }
  }

  private static void rewriteJsonObject(JsonObject json) {
    // The RTAF JSON includes a field `fooMemoizedSerializedSize` for every `foo` that is a a
    // repeated non-message field. Not including it means that it gets its default value of -1,
    // which implies that it will be computed on demand. We verify `getSerializedSize()` in
    // `legacyCompatToRtaf`, which would fail if the absence of `fooMemoizedSerializedSize` were a
    // problem.
    // There are a number of fields that are transient in Google's internal version but not yet in
    // the open-source version (for compatibility reasons). So we rewrite those ones too.
    Pattern pattern =
        Pattern.compile(
            "^(memoized.?(Hash.?Code|Is.?Initialized|Size))|unknown.?Fields"
                + "|.*Memoized.?Serialized.?Size$",
            Pattern.CASE_INSENSITIVE);
    var keysToRemove =
        json.keySet().stream()
            .filter(key -> pattern.matcher(key).find())
            .collect(toImmutableList());
    json.keySet().removeAll(keysToRemove);
    json.keySet().removeAll(TRANSIENT_FIELDS);

    // Similarly, the RTAF JSON for a ByteString can include a `hash` field that is either 0
    // (meaning the hash has not been computed) or a non-zero hash value. We rewrite it to 0 always.
    var lowerCaseToOriginalCase =
        json.keySet().stream().collect(toImmutableMap(s -> Ascii.toLowerCase(s), s -> s));
    if (lowerCaseToOriginalCase.keySet().equals(BYTE_STRING_FIELDS)) {
      json.addProperty(lowerCaseToOriginalCase.get("hash"), 0);
    }

    for (var element : json.asMap().values()) {
      rewriteJsonElement(element);
    }
  }

  private static void rewriteJsonArray(JsonArray json) {
    for (var element : json) {
      rewriteJsonElement(element);
    }
  }

  /**
   * Fields that are transient in Google's internal version but, for compatibility reasons, not yet
   * in open-source protos. We remove them before comparing expected and actual.
   */
  private static final ImmutableSet<String> TRANSIENT_FIELDS =
      ImmutableSet.of("memoizedHashCode", "memoizedIsInitialized", "memoizedSize", "unknownFields");

  private static final ImmutableSet<String> BYTE_STRING_FIELDS = ImmutableSet.of("bytes", "hash");

  // "memoizedIsInitialized":1,"memoizedSize":-1,"memoizedHashCode":0}

  /**
   * Tests that messages with oneof fields can be serialized by a Gson instance with {@link
   * LegacyProtoTypeAdapterFactory} registered, and the result will be the same as with RTAF. Doing
   * this is really only useful for logging and golden-file tests, because neither RTAF nor {@link
   * LegacyProtoTypeAdapterFactory} can deserialize these messages correctly.
   */
  @Test
  public void legacyCompat_serializeOneof(
      @TestParameter(valuesProvider = OneofProvider.class) TestOneof message) {
    JsonElement rtafJson = RTAF_GSON.toJsonTree(message);
    rewriteJsonElement(rtafJson);
    JsonElement legacyJson = GSON_WITH_LEGACY_ADAPTER.toJsonTree(message);
    rewriteJsonElement(legacyJson);
    assertThat(legacyJson).isEqualTo(rtafJson);
  }

  /**
   * Verifies that RTAF cannot in general deserialize a message with a oneof field correctly.
   *
   * <p>For most messages with a oneof field that is set, serializing with RTAF and then
   * deserializing leads to an object where fetching the oneof field value causes a {@code
   * ClassCastException}. That's because the oneof value is stored in a field of type {@code
   * Object}. Gson will usually guess the wrong type, since all it has to go on is the shape of the
   * JSON: it will deserialize a {@code double} if the JSON value is a number (with or without
   * decimal point), and it will deserialize a {@code Map} if the JSON value is a JSON object.
   */
  @Test
  public void rtaf_cannotDeserializeOneof(
      @TestParameter(valuesProvider = OneofProvider.class) TestOneof message) {
    String json = RTAF_GSON.toJson(message);
    TestOneof possiblyCorruptMessage = RTAF_GSON.fromJson(json, TestOneof.class);

    switch (message.getOneofFieldCase()) {
      case ONEOFFIELD_NOT_SET:
        assertThat(possiblyCorruptMessage.getOneofFieldCase())
            .isEqualTo(TestOneof.OneofFieldCase.ONEOFFIELD_NOT_SET);
        break;
      case ONEOF_INT32:
        assertThrows(ClassCastException.class, possiblyCorruptMessage::getOneofInt32);
        break;
      case ONEOF_NESTED_MESSAGE:
        assertThrows(ClassCastException.class, possiblyCorruptMessage::getOneofNestedMessage);
        break;
      case ONEOF_NULL_VALUE:
        assertThrows(ClassCastException.class, possiblyCorruptMessage::getOneofNullValue);
        break;
      case ONEOF_STRING:
        assertThat(possiblyCorruptMessage.getOneofString()).isEqualTo(message.getOneofString());
        break;
    }
  }

  /**
   * Verifies that {@link LegacyProtoTypeAdapterFactory} drops oneof fields on deserialization.
   * That's probably better than producing a message that throws a {@code ClassCastException} when
   * the oneof field is accessed. We do retain oneof fields with string values, because that one
   * situation works.
   */
  @Test
  public void legacyCompat_canOnlyDeserializeStringOneof(
      @TestParameter(valuesProvider = OneofProvider.class) TestOneof message) {
    String json = GSON_WITH_LEGACY_ADAPTER.toJson(message);
    TestOneof deserialized = GSON_WITH_LEGACY_ADAPTER.fromJson(json, TestOneof.class);

    switch (message.getOneofFieldCase()) {
      case ONEOF_STRING:
        assertThat(deserialized.getOneofString()).isEqualTo(message.getOneofString());
        break;
      default:
        assertThat(deserialized.getOneofFieldCase())
            .isEqualTo(TestOneof.OneofFieldCase.ONEOFFIELD_NOT_SET);
    }
  }

  /**
   * Tests that messages can be serialized by a Gson instance with {@link
   * LegacyProtoTypeAdapterFactory} registered and then deserialized by the RTAF logic.
   *
   * <p>There are fewer test messages for this case than for {@link #rtafToLegacyCompat} because
   * RTAF can handle fewer kinds of proto fields when deserializing than when serializing. When
   * serializing an instance field in the message implementation class, it can use the actual
   * runtime type of the field contents. But when deserializing a field, it has to construct an
   * instance of some type, and the only information it has is the declared type of the field. If
   * that type is an abstract class or an interface, it will generally not be able to construct an
   * instance. That applies for example to fields of type {@code Object} or {@code ByteString} or
   * {@code Internal.IntList}.
   */
  @Test
  public void legacyCompatToRtaf(
      @TestParameter(valuesProvider = LegacyCompatToRtafProvider.class) Message message) {
    String rtafJson = RTAF_GSON.toJson(message);
    assertThat(RTAF_GSON.fromJson(rtafJson, message.getClass())).isEqualTo(message);
    String json = GSON_WITH_LEGACY_ADAPTER.toJson(message);
    Message reflectionMessage = RTAF_GSON.fromJson(json, message.getClass());
    assertThat(reflectionMessage).isEqualTo(message);
    assertThat(reflectionMessage.getSerializedSize()).isEqualTo(message.getSerializedSize());
  }

  /**
   * Tests that messages can be serialized by a Gson instance with {@link
   * LegacyProtoTypeAdapterFactory} registered, even if given only {@code Message.class} as the
   * target type. For serialization, it's possible to use the actual runtime type to get the right
   * results. (This is better than RTAF, which just produces an empty JSON object.)
   */
  @Test
  public void legacyCompatToRtafWithTargetTypeMessage(
      @TestParameter(valuesProvider = LegacyCompatToRtafProvider.class) Message message) {
    String json = GSON_WITH_LEGACY_ADAPTER.toJson(message, Message.class);
    Message reflectionMessage = RTAF_GSON.fromJson(json, message.getClass());
    assertThat(reflectionMessage).isEqualTo(message);
    assertThat(reflectionMessage.getSerializedSize()).isEqualTo(message.getSerializedSize());
  }

  /**
   * Tests that messages cannot be deserialized by a Gson instance with {@link
   * LegacyProtoTypeAdapterFactory} registered, if given only {@code Message.class} as the target
   * type.
   */
  @Test
  public void rtafToLegacyCompatWithTargetTypeMessage(
      @TestParameter(valuesProvider = LegacyCompatToRtafProvider.class) Message message) {
    String json = RTAF_GSON.toJson(message);
    var exception =
        assertThrows(
            JsonParseException.class, () -> GSON_WITH_LEGACY_ADAPTER.fromJson(json, Message.class));
    assertThat(exception).hasMessageThat().contains("Cannot deserialize a generic Message");
  }

  /**
   * Neither RTAF nor {@link LegacyProtoTypeAdapterFactory} can deserialize maps. We test only that
   * they both serialize maps in the same way.
   */
  @Test
  public void serializeMap() {
    TestMap map =
        TestMap.newBuilder()
            .putInt32ToInt32Map(1, 2)
            .putInt32ToInt32Map(3, 4)
            .putStringToInt32Map("a", 1)
            .putInt32ToMessageMap(1, NestedMessage.newBuilder().setValue(100).build())
            .build();
    JsonElement rtafJson = RTAF_GSON.toJsonTree(map);
    rewriteJsonElement(rtafJson);
    JsonElement json = GSON_WITH_LEGACY_ADAPTER.toJsonTree(map);
    rewriteJsonElement(json);
    assertThat(json).isEqualTo(rtafJson);

    assertThrows(JsonParseException.class, () -> RTAF_GSON.fromJson(json, TestMap.class));
  }

  private static final class StringAdapter extends TypeAdapter<String> {
    @Override
    public void write(JsonWriter out, String value) throws IOException {
      out.value("«" + value + "»");
    }

    @Override
    public String read(JsonReader in) throws IOException {
      String s = in.nextString();
      if (!s.startsWith("«") || !s.endsWith("»")) {
        throw new JsonParseException("Unexpected string: " + s);
      }
      return s.substring(1, s.length() - 1);
    }
  }

  @Test
  public void customStringAdapter() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapterFactory(LegacyProtoTypeAdapterFactory.INSTANCE)
            .registerTypeAdapter(String.class, new StringAdapter())
            .create();
    TestAllTypes message = TestMessages.testAllTypes();
    String json = gson.toJson(message);
    assertThat(json).contains("\"optionalString_\":\"«" + message.getOptionalString() + "»");
    TestAllTypes message2 = gson.fromJson(json, TestAllTypes.class);
    assertThat(message2).isEqualTo(message);
  }

  @Test
  public void serializeNull() {
    assertThat(RTAF_GSON.toJson(null, TestAllTypes.class)).isEqualTo("null");
    assertThat(GSON_WITH_LEGACY_ADAPTER.toJson(null, TestAllTypes.class)).isEqualTo("null");
  }

  @Test
  public void deserializeNull() {
    assertThat(RTAF_GSON.fromJson("null", TestAllTypes.class)).isNull();
    assertThat(GSON_WITH_LEGACY_ADAPTER.fromJson("null", TestAllTypes.class)).isNull();
  }

  private static Gson applyFieldNamingPolicy(Gson gson, FieldNamingPolicy fieldNamingPolicy) {
    return gson.newBuilder().setFieldNamingPolicy(fieldNamingPolicy).create();
  }

  private abstract static class TestMessagesProvider extends TestParameterValuesProvider {
    private final ImmutableMap<String, Message> testMessages;

    TestMessagesProvider(ImmutableMap<String, Message> testMessages) {
      this.testMessages = testMessages;
    }

    @Override
    public ImmutableList<TestParameterValue> provideValues(Context context) {
      return testMessages.entrySet().stream()
          .map(entry -> value(entry.getValue()).withName(entry.getKey()))
          .collect(toImmutableList());
    }
  }

  private static final class RtafToLegacyCompatProvider extends TestMessagesProvider {
    RtafToLegacyCompatProvider() {
      super(SERIALIZED_WITH_RTAF);
    }
  }

  private static final class LegacyCompatToRtafProvider extends TestMessagesProvider {
    LegacyCompatToRtafProvider() {
      super(DESERIALIZED_WITH_RTAF);
    }
  }

  private static final class OneofProvider extends TestMessagesProvider {
    OneofProvider() {
      super(ONEOF_MESSAGES);
    }
  }
}
