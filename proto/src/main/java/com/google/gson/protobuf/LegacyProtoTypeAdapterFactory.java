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
package com.google.gson.protobuf;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.Message;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;

/**
 * A {@link TypeAdapterFactory} that supports the broken JSON mapping that Gson users get for
 * protobuf messages if they forget to register a proper handler such as {@link
 * ProtoTypeAdapter}. <b>This class is a migration aid.</b> If your project currently
 * uses it, you should consider migrating to {@link ProtoTypeAdapter} or similar. That does
 * <i>change</i> the JSON encoding, though, so there can be compatibility concerns.
 *
 * <p>The default JSON mapping for protobuf messages is derived by examining the private fields of
 * the generated protobuf classes. That's obviously very fragile, and leads to ugly JSON that is not
 * what people would reasonably expect. For example, here is what a serialized {@code
 * .google.protobuf.Duration} might look like:
 *
 * <pre>
 * {
 *   "seconds_": 10,
 *   "nanos_": 20,
 *   "bitField0_": 3
 * }
 * </pre>
 *
 * <p>Notice the underscore at the end of each field name and the extra field {@code bitField0_}
 * whose meaning is unlikely to be obvious to typical observers.
 *
 * <p>This class does not support Java Proto Lite.
 */
public enum LegacyProtoTypeAdapterFactory implements TypeAdapterFactory {
  INSTANCE;

  private LegacyProtoTypeAdapterFactory() {}

  @Override
  public <T> @Nullable TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    Class<?> rawType = type.getRawType();
    if (!Message.class.isAssignableFrom(rawType)) {
      return null;
    }
    if (rawType == Message.class) {
      @SuppressWarnings("unchecked")
      TypeAdapter<T> dynamicAdapter = (TypeAdapter<T>) new DynamicAdapter(gson).nullSafe();
      return dynamicAdapter;
    }
    @SuppressWarnings("unchecked")
    TypeAdapter<T> adapter = (TypeAdapter<T>) new Adapter<>(gson, type).nullSafe();
    return adapter;
  }

  // In what follows, RTAF means ReflectiveTypeAdapterFactory, which is the fallback that Gson
  // uses for classes that don't have an explicit TypeAdapter. When serializing or deserializing
  // a Java object, RTAF reflects on the instance fields of the object's class to determine
  // its JSON representation. To output JSON, it outputs a JSON object with one key-value
  // pair for each instance field. The keys are the field names and the values are the JSON
  // representations of the field contents. To read JSON, it creates a new instance of the class
  // using its private no-arg constructor then reflectively sets each of the instance fields based
  // on the key-value pairs in the JSON object. Our Adapter here mimics both of these things
  // without depending on the private implementation details of generated proto message classes.
  // Specifically, it attempts to behave the same as RTAF would when reflecting on the version of
  // proto generated code that was current in early 2026.

  private static final class Adapter<T extends Message> extends TypeAdapter<T> {
    private final Gson gson;
    private final FieldNamingPolicy fieldNamingPolicy;
    private final Class<T> messageClass;

    private static final ClassValue<Message> DEFAULT_INSTANCE_CACHE =
        new ClassValue<Message>() {
          @Override
          protected Message computeValue(Class<?> type) {
            try {
              return (Message) type.getMethod("getDefaultInstance").invoke(null);
            } catch (Exception e) {
              throw new IllegalArgumentException(e);
            }
          }
        };

    private final ImmutableMap<String, FieldDescriptor> javaNameToFieldDescriptor;
    private final ImmutableMap<String, OneofDescriptor> oneofNameToOneofDescriptor;
    private final ImmutableMap<FieldDescriptor, Integer> fieldWithPresenceToBitIndex;

    // We use this to write or read string-valued fields. We don't bother using a TypeAdapter for
    // the other kinds of fields, because no google3 code needs that.
    private final TypeAdapter<String> stringAdapter;

    // We could support the other cases, but they don't have immediate CaseFormat equivalents and
    // are not actually used in conjunction with RTAF, so it's not worth it.
    private static final ImmutableSet<FieldNamingPolicy> SUPPORTED_FIELD_NAMING_POLICIES =
        ImmutableSet.of(
            FieldNamingPolicy.IDENTITY,
            FieldNamingPolicy.UPPER_CAMEL_CASE,
            FieldNamingPolicy.UPPER_CASE_WITH_UNDERSCORES,
            FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
            FieldNamingPolicy.LOWER_CASE_WITH_DASHES);

    Adapter(Gson gson, TypeToken<?> type) {
      this.gson = gson;
      if (gson.fieldNamingStrategy() instanceof FieldNamingPolicy) {
        FieldNamingPolicy fieldNamingPolicy = (FieldNamingPolicy) gson.fieldNamingStrategy();
        if (!SUPPORTED_FIELD_NAMING_POLICIES.contains(fieldNamingPolicy)) {
          throw new IllegalArgumentException(
              "Gson instance has FieldNamingPolicy that is not supported by"
                  + " LegacyProtoTypeAdapterFactory: "
                  + fieldNamingPolicy);
        }
        this.fieldNamingPolicy = fieldNamingPolicy;
      } else {
        throw new IllegalArgumentException(
            "Gson instance has a custom FieldNamingStrategy, which is not supported by"
                + " LegacyProtoTypeAdapterFactory: "
                + gson.fieldNamingStrategy());
      }
      @SuppressWarnings("unchecked") // we're inventing T here to track types
      Class<T> messageClass = (Class<T>) type.getRawType().asSubclass(Message.class);
      this.messageClass = messageClass;
      Descriptor descriptor = DEFAULT_INSTANCE_CACHE.get(messageClass).getDescriptorForType();
      this.javaNameToFieldDescriptor = makeJavaNameToFieldDescriptor(descriptor);
      this.oneofNameToOneofDescriptor = makeOneofNameToOneofDescriptor(descriptor);
      this.fieldWithPresenceToBitIndex = makeFieldWithPresenceToBitIndex(descriptor);
      this.stringAdapter = gson.getAdapter(String.class);
    }

    private static ImmutableMap<String, FieldDescriptor> makeJavaNameToFieldDescriptor(
        Descriptor descriptor) {
      ImmutableMap.Builder<String, FieldDescriptor> builder = ImmutableMap.builder();
      for (FieldDescriptor fieldDescriptor : descriptor.getFields()) {
        builder.put(javaName(fieldDescriptor.getName()), fieldDescriptor);
      }
      return builder.buildOrThrow();
    }

    private static ImmutableMap<String, OneofDescriptor> makeOneofNameToOneofDescriptor(
        Descriptor descriptor) {
      ImmutableMap.Builder<String, OneofDescriptor> builder = ImmutableMap.builder();
      for (OneofDescriptor oneofDescriptor : descriptor.getOneofs()) {
        builder.put(javaName(oneofDescriptor.getName()), oneofDescriptor);
      }
      return builder.buildOrThrow();
    }

    private static ImmutableMap<FieldDescriptor, Integer> makeFieldWithPresenceToBitIndex(
        Descriptor descriptor) {
      ImmutableMap.Builder<FieldDescriptor, Integer> builder = ImmutableMap.builder();
      int bitIndex = 0;
      for (FieldDescriptor fieldDescriptor : descriptor.getFields()) {
        if (fieldDescriptor.hasPresence() && fieldDescriptor.getRealContainingOneof() == null) {
          builder.put(fieldDescriptor, bitIndex++);
        }
      }
      return builder.buildOrThrow();
    }

    private static String javaName(String name) {
      return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
    }

    // The field `private int bitField0_` tracks presence for the first 32 fields that have
    // presence, with bit 0 corresponding to the first field, and so on. The field `private int
    // bitField1_` tracks presence for the next 32 fields with presence, and so on.
    private static final Pattern BIT_FIELD_PATTERN = Pattern.compile("bitField(\\d+)_");

    @Override
    public T read(JsonReader in) throws IOException {
      Message defaultInstance = DEFAULT_INSTANCE_CACHE.get(messageClass);
      Message.Builder builder = defaultInstance.newBuilderForType();
      in.beginObject();
      BigInteger presenceBitmask = BigInteger.ZERO;
      Map<String, Integer> oneofNameToCaseNumber = new LinkedHashMap<>();
      Map<String, String> oneofNameToValue = new LinkedHashMap<>();
      while (in.hasNext()) {
        String fieldName = readFieldName(in);
        Matcher matcher = BIT_FIELD_PATTERN.matcher(fieldName);
        if (matcher.matches()) {
          int shift = Integer.parseInt(matcher.group(1)) * 32;
          int mask = in.nextInt();
          presenceBitmask = presenceBitmask.or(BigInteger.valueOf(mask).shiftLeft(shift));
        } else {
          if (fieldName.endsWith("_")) {
            fieldName = fieldName.substring(0, fieldName.length() - 1);
          }
          FieldDescriptor fieldDescriptor = javaNameToFieldDescriptor.get(fieldName);
          if (fieldDescriptor != null) {
            builder.setField(fieldDescriptor, readField(in, builder, fieldDescriptor));
          } else if (!readOneofField(fieldName, oneofNameToCaseNumber, oneofNameToValue, in)) {
            in.skipValue();
          }
        }
      }
      in.endObject();
      // Now use the presence bitmask to determine whether to keep each value read from the JSON. If
      // a field with presence does not have a 1 bit in the bitmask, then we clear it, but only if
      // its value is the same as the default value. Its Java field in the source message will have
      // been copied into the JSON by RTAF regardless of "presence", so if it really is absent then
      // that value should be the same as the default. Doing this avoids a problem where the set of
      // fields with presence might have changed between the time the proto was converted to JSON
      // and now. If that happens, the bits in the bitmask may be completely bogus, but at worst we
      // will mark a field as absent when it should be present but with the default value. That is
      // actually a much better failure mode than RTAF. If the meaning of the bits in the bitmask
      // has changed then RTAF can mark a field as absent even though a non-default value has been
      // read into the corresponding Java field.
      // We do have another failure mode that RTAF doesn't have: if the default value of a field has
      // changed, then we might set that field to the old default value when it should have been
      // absent.
      for (Map.Entry<FieldDescriptor, Integer> entry : fieldWithPresenceToBitIndex.entrySet()) {
        FieldDescriptor fieldDescriptor = entry.getKey();
        Integer presenceBitIndex = entry.getValue();
        if (!presenceBitmask.testBit(presenceBitIndex)
            && builder
                .getField(fieldDescriptor)
                .equals(defaultInstance.getField(fieldDescriptor))) {
          builder.clearField(fieldDescriptor);
        }
      }
      // Finally, set any string-valued oneof fields.
      oneofNameToValue.forEach(
          (oneofName, value) -> {
            Integer caseNumber = oneofNameToCaseNumber.get(oneofName);
            if (caseNumber != null && caseNumber > 0) {
              OneofDescriptor oneofDescriptor = oneofNameToOneofDescriptor.get(oneofName);
              FieldDescriptor fieldDescriptor =
                  oneofDescriptor.getContainingType().findFieldByNumber(caseNumber);
              if (fieldDescriptor != null
                  && fieldDescriptor.getRealContainingOneof() == oneofDescriptor) {
                builder.setField(fieldDescriptor, value);
              }
            }
          });
      @SuppressWarnings("unchecked")
      T result = (T) builder.build();
      return result;
    }

    // A oneof `foo` is represented as two fields in the generated proto class: `int fooCase` and
    // `Object foo`. RTAF can deserialize this, but except in special cases it will produce a
    // corrupt message where getting the set oneof field will throw a `ClassCastException`.
    // The most important of those special cases is when the oneof field is of type `string`. We
    // handle that here: if the JSON has `"fooCase":2,"foo":"hello"` then the oneof case is a
    // string and we will set it. Because of the way the JSON is read from a stream, we have to
    // record the "fooCase" and "foo" fields separately, and join the read values from the two maps
    // here at the end.
    private boolean readOneofField(
        String fieldName,
        Map<String, Integer> oneofNameToCaseNumber,
        Map<String, String> oneofNameToValue,
        JsonReader in)
        throws IOException {
      for (String oneofName : oneofNameToOneofDescriptor.keySet()) {
        if (fieldName.equals(oneofName) && in.peek() == JsonToken.STRING) {
          oneofNameToValue.put(oneofName, stringAdapter.read(in));
          return true;
        } else if (fieldName.equals(oneofName + "Case") && in.peek() == JsonToken.NUMBER) {
          oneofNameToCaseNumber.put(oneofName, in.nextInt());
          return true;
        }
      }
      return false;
    }

    private Object readField(
        JsonReader in, Message.Builder builder, FieldDescriptor fieldDescriptor)
        throws IOException {
      if (fieldDescriptor.getType() == FieldDescriptor.Type.MESSAGE) {
        Class<? extends Message> nestedMessageClass =
            builder
                .newBuilderForField(fieldDescriptor)
                .getClass()
                .getEnclosingClass()
                .asSubclass(Message.class);
        return readMessageField(in, nestedMessageClass, fieldDescriptor);
      } else if (fieldDescriptor.isRepeated()) {
        return readRepeatedField(in, fieldDescriptor);
      } else {
        return readSingleFieldValue(in, fieldDescriptor);
      }
    }

    private Object readSingleFieldValue(JsonReader in, FieldDescriptor fieldDescriptor)
        throws IOException {
      switch (fieldDescriptor.getType()) {
        case INT32:
        case SINT32:
        case UINT32:
        case FIXED32:
        case SFIXED32:
          return in.nextInt();
        case INT64:
        case SINT64:
        case UINT64:
        case FIXED64:
        case SFIXED64:
          return in.nextLong();
        case BOOL:
          return in.nextBoolean();
        case FLOAT:
          return (float) in.nextDouble();
        case DOUBLE:
          return in.nextDouble();
        case STRING:
          return stringAdapter.read(in);
        case BYTES:
          return readByteString(in);
        case ENUM:
          return fieldDescriptor.getEnumType().findValueByNumber(in.nextInt());
        case MESSAGE:
          throw new AssertionError("Should not happen");
        case GROUP:
          throw new JsonSyntaxException("Groups are not supported");
      }
      throw new AssertionError("Unexpected type: " + fieldDescriptor.getType());
    }

    private List<?> readRepeatedField(JsonReader in, FieldDescriptor fieldDescriptor)
        throws IOException {
      List<Object> result = new ArrayList<>();
      in.beginArray();
      while (in.hasNext()) {
        result.add(readSingleFieldValue(in, fieldDescriptor));
      }
      in.endArray();
      return result;
    }

    private Object readMessageField(
        JsonReader in, Class<? extends Message> messageClass, FieldDescriptor fieldDescriptor)
        throws IOException {
      TypeAdapter<?> nestedAdapter = gson.getAdapter(messageClass);
      if (fieldDescriptor.isRepeated()) {
        List<Message> result = new ArrayList<>();
        in.beginArray();
        while (in.hasNext()) {
          result.add(messageClass.cast(nestedAdapter.read(in)));
        }
        in.endArray();
        return result;
      } else {
        return nestedAdapter.read(in);
      }
    }

    private ByteString readByteString(JsonReader in) throws IOException {
      // Since RTAF doesn't know any better, it will serialize a ByteString as a JSON object with
      // two fields:
      // - "bytes": the actual byte contents of the ByteString, a JSON array of integers;
      // - "hash": an integer hash code of the ByteString.
      ByteString byteString = null;
      in.beginObject();
      while (in.hasNext()) {
        String fieldName = readFieldName(in);
        switch (fieldName) {
          case "bytes":
            byteString = ByteString.copyFrom((byte[]) gson.fromJson(in, byte[].class));
            break;
          case "hash":
            in.skipValue();
            break;
          default:
            throw new IllegalArgumentException("Unrecognized ByteString field: " + fieldName);
        }
      }
      in.endObject();
      if (byteString == null) {
        throw new IllegalArgumentException("Missing bytes field");
      }
      return byteString;
    }

    private String readFieldName(JsonReader in) throws IOException {
      String fieldName = in.nextName();
      String translatedName;
      switch (fieldNamingPolicy) {
        case IDENTITY:
          translatedName = fieldName;
          break;
        case UPPER_CAMEL_CASE:
          translatedName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, fieldName);
          break;
        case UPPER_CASE_WITH_UNDERSCORES:
          translatedName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName);
          break;
        case LOWER_CASE_WITH_UNDERSCORES:
          translatedName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName);
          break;
        case LOWER_CASE_WITH_DASHES:
          translatedName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, fieldName);
          break;
        default:
          throw new AssertionError(fieldNamingPolicy);
      }
      if (fieldName.endsWith("_") && !translatedName.endsWith("_")) {
        translatedName += "_";
      }
      return translatedName;
    }

    @Override
    public void write(JsonWriter out, T message) throws IOException {
      int bitFieldCount = (fieldWithPresenceToBitIndex.size() + 31) / 32;
      int[] bitFields = new int[bitFieldCount];
      fieldWithPresenceToBitIndex.forEach(
          (fieldDescriptor, bitIndex) -> {
            if (message.hasField(fieldDescriptor)) {
              bitFields[bitIndex / 32] |= 1 << bitIndex; // `<<` automatically mods with 32
            }
          });
      out.beginObject();
      for (int i = 0; i < bitFieldCount; i++) {
        writeFieldName(out, "bitField" + i + "_").value(bitFields[i]);
      }
      // Consistently with RTAF, we write all fields, regardless of presence.
      List<FieldDescriptor> fields = message.getDescriptorForType().getFields();
      for (int i = 0; i < fields.size(); i++) {
        FieldDescriptor fieldDescriptor = fields.get(i);
        OneofDescriptor containingOneof = fieldDescriptor.getRealContainingOneof();
        if (containingOneof != null) {
          i = writeOneofFields(out, message, fields, i, containingOneof);
          continue;
        }
        writeFieldName(out, javaName(fieldDescriptor.getName()) + "_");
        if (fieldDescriptor.getType() == FieldDescriptor.Type.MESSAGE
            && fieldDescriptor.hasPresence()
            && !message.hasField(fieldDescriptor)) {
          // Writing null is consistent with what RTAF does, and avoids the infinite recursion we
          // could otherwise get for a recursive message.
          out.nullValue();
        } else {
          writeField(out, message.getField(fieldDescriptor), fieldDescriptor);
        }
      }
      out.endObject();
    }

    private void writeField(JsonWriter out, Object value, FieldDescriptor fieldDescriptor)
        throws IOException {
      if (fieldDescriptor.isMapField()) {
        writeMapField(out, value, fieldDescriptor);
      } else if (fieldDescriptor.isRepeated()) {
        writeRepeatedField(out, value, fieldDescriptor);
      } else {
        writeSingleFieldValue(out, value, fieldDescriptor);
      }
    }

    private void writeRepeatedField(JsonWriter out, Object value, FieldDescriptor fieldDescriptor)
        throws IOException {
      out.beginArray();
      for (Object element : (List<?>) value) {
        writeSingleFieldValue(out, element, fieldDescriptor);
      }
      out.endArray();
    }

    private void writeSingleFieldValue(
        JsonWriter out, Object value, FieldDescriptor fieldDescriptor) throws IOException {
      switch (fieldDescriptor.getType()) {
        case INT32:
        case SINT32:
        case UINT32:
        case FIXED32:
        case SFIXED32:
          out.value((int) value);
          break;
        case INT64:
        case SINT64:
        case UINT64:
        case FIXED64:
        case SFIXED64:
          out.value((long) value);
          break;
        case BOOL:
          out.value((boolean) value);
          break;
        case FLOAT:
          out.value((float) value);
          break;
        case DOUBLE:
          out.value((double) value);
          break;
        case STRING:
          stringAdapter.write(out, (String) value);
          break;
        case BYTES:
          writeByteString(out, (ByteString) value);
          break;
        case ENUM:
          out.value(((EnumValueDescriptor) value).getNumber());
          break;
        case MESSAGE:
          writeMessageField(out, (Message) value);
          break;
        case GROUP:
          throw new JsonSyntaxException("Groups are not supported");
      }
    }

    private void writeMessageField(JsonWriter out, Message value) throws IOException {
      @SuppressWarnings("unchecked") // remove `? extends` to allow the write call
      TypeAdapter<Message> nestedAdapter = (TypeAdapter<Message>) gson.getAdapter(value.getClass());
      nestedAdapter.write(out, value);
    }

    private void writeMapField(JsonWriter out, Object value, FieldDescriptor fieldDescriptor)
        throws IOException {
      // At the proto level, a map field is a repeated field of MapEntry instances, each with fields
      // "key" and "value". That's what the FieldDescriptor will tell us, and in particular it will
      // tell us what the type of the value field is. (We always serialize the key as a string
      // because that's how Gson serializes Java Map objects.)
      // In the generated code (in the version we are emulating), a map field my_map is a Java field
      // `com.google.protobuf.MapField<...> myMap_`. The field is null if the map is empty.
      @SuppressWarnings("unchecked") // we know it's a list of MapEntry, which is a Message.
      List<? extends Message> mapEntries = (List<? extends Message>) value;
      if (mapEntries.isEmpty()) {
        out.nullValue();
        return;
      }
      Descriptor mapEntryDescriptor = fieldDescriptor.getMessageType();
      FieldDescriptor keyFieldDescriptor = mapEntryDescriptor.findFieldByName("key");
      FieldDescriptor valueFieldDescriptor = mapEntryDescriptor.findFieldByName("value");
      // MapField can represent maps in two ways, as a Java Map or as a List of MapEntry objects.
      // We'll emulate just the Map version here. So we'll emulate the fields `boolean isMutable`,
      // `StorageMode mode`, `MutabilityAwareMapData mapData` and `Converter<K, V> converter`, and
      // leave the field `List<Message> listData` null (which by default means Gson will output
      // nothing for it).
      out.beginObject();
      writeFieldName(out, "isMutable").value(false);
      writeFieldName(out, "mode").value("MAP");
      // MutabilityAwareMapData inherits from Map, so Gson serializes it as a Map, not using RTAF.
      writeFieldName(out, "mapData");
      out.beginObject();
      for (Message entry : mapEntries) {
        writeFieldName(out, entry.getField(keyFieldDescriptor).toString());
        Object entryValue = entry.getField(valueFieldDescriptor);
        gson.toJson(entryValue, entryValue.getClass(), out);
      }
      out.endObject();
      writeFieldName(out, "listData").nullValue();
      writeFieldName(out, "converter");
      out.beginObject();
      out.endObject();
      out.endObject();
    }

    private int writeOneofFields(
        JsonWriter out,
        Message message,
        List<FieldDescriptor> messageFields,
        int firstOneofIndex,
        OneofDescriptor containingOneof)
        throws IOException {
      // gather all fields that are in the same oneof
      int i;
      for (i = firstOneofIndex;
          i < messageFields.size()
              && messageFields.get(i).getRealContainingOneof() == containingOneof;
          i++) {}
      int lastOneofIndex = i - 1;
      // Determine which of the oneof fields is set, if any.
      FieldDescriptor presentField =
          IntStream.rangeClosed(firstOneofIndex, lastOneofIndex)
              .mapToObj(messageFields::get)
              .filter(field -> message.hasField(field))
              .findFirst()
              .orElse(null);
      // For a oneof `foo_bar`, a legacy proto class has two fields: `int fooBarCase_` and `Object
      // fooBar_`. Write these based on `presentField`.
      String javaName = javaName(containingOneof.getName());
      writeFieldName(out, javaName + "Case_")
          .value(presentField == null ? 0 : presentField.getNumber());
      writeFieldName(out, javaName + "_");
      if (presentField == null) {
        out.nullValue();
      } else {
        writeField(out, message.getField(presentField), presentField);
      }
      return lastOneofIndex;
    }

    private void writeByteString(JsonWriter out, ByteString byteString) throws IOException {
      out.beginObject();
      writeFieldName(out, "bytes");
      gson.toJson(byteString.toByteArray(), byte[].class, out);
      // A value for the `hash` field of 0 means that the hashCode has not been computed because
      // hashCode() has never been called on this ByteString. We could also just not output `hash`
      // at all and still be compatible with RTAF, but we would be more likely to fail tests that
      // textually compare expected JSON output.
      // For ByteString.EMPTY, chances are that something has already called hashCode() on it. So to
      // make tests pass that compare JSON output, we do call its hashCode().
      writeFieldName(out, "hash").value(byteString.isEmpty() ? byteString.hashCode() : 0);
      out.endObject();
      // If you are later deserializing with RTAF, and you don't have a specific TypeAdapter for
      // ByteString, the default will be to use RTAF for that too. It won't be able to make a
      // ByteString because that is an abstract class. But LegacyProtoTypeAdapterFactory does know
      // how to deserialize, because it is using the type in FieldDescriptor.getType(), not the Java
      // declared type of the field.
    }

    @CanIgnoreReturnValue
    private JsonWriter writeFieldName(JsonWriter out, String name) throws IOException {
      String translatedName;
      switch (fieldNamingPolicy) {
        case IDENTITY:
          translatedName = name;
          break;
        case UPPER_CAMEL_CASE:
          translatedName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name);
          break;
        case UPPER_CASE_WITH_UNDERSCORES:
          translatedName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
          break;
        case LOWER_CASE_WITH_UNDERSCORES:
          translatedName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
          break;
        case LOWER_CASE_WITH_DASHES:
          translatedName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, name);
          break;
        default:
          throw new AssertionError(fieldNamingPolicy);
      }
      return out.name(translatedName);
    }
  }

  private static class DynamicAdapter extends TypeAdapter<Message> {
    private final Gson gson;

    DynamicAdapter(Gson gson) {
      this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, Message value) throws IOException {
      writeSubclass(out, value, value.getClass());
    }

    private <T extends Message> void writeSubclass(
        JsonWriter out, Message value, Class<T> valueClass) throws IOException {
      TypeAdapter<T> adapter = gson.getAdapter(valueClass);
      adapter.write(out, valueClass.cast(value));
    }

    @Override
    public Message read(JsonReader in) throws IOException {
      throw new JsonParseException("Cannot deserialize a generic Message");
    }
  }
}
