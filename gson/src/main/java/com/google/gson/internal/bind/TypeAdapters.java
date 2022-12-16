/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Type adapters for basic types.
 */
public final class TypeAdapters {
  private TypeAdapters() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("rawtypes")
  public static final TypeAdapter<Class> CLASS = new TypeAdapter<Class>() {
    @Override
    public void write(JsonWriter out, Class value) throws IOException {
      throw new UnsupportedOperationException("Attempted to serialize java.lang.Class: "
              + value.getName() + ". Forgot to register a type adapter?");
    }
    @Override
    public Class read(JsonReader in) throws IOException {
      throw new UnsupportedOperationException(
              "Attempted to deserialize a java.lang.Class. Forgot to register a type adapter?");
    }
  }.nullSafe();

  public static final TypeAdapterFactory CLASS_FACTORY = newFactory(Class.class, CLASS);

  public static final TypeAdapter<BitSet> BIT_SET = new TypeAdapter<BitSet>() {
    @Override public BitSet read(JsonReader in) throws IOException {
      BitSet bitset = new BitSet();
      in.beginArray();
      int i = 0;
      JsonToken tokenType = in.peek();
      while (tokenType != JsonToken.END_ARRAY) {
        boolean set;
        switch (tokenType) {
        case NUMBER:
        case STRING:
          int intValue = in.nextInt();
          if (intValue == 0) {
            set = false;
          } else if (intValue == 1) {
            set = true;
          } else {
            throw new JsonSyntaxException("Invalid bitset value " + intValue + ", expected 0 or 1; at path " + in.getPreviousPath());
          }
          break;
        case BOOLEAN:
          set = in.nextBoolean();
          break;
        default:
          throw new JsonSyntaxException("Invalid bitset value type: " + tokenType + "; at path " + in.getPath());
        }
        if (set) {
          bitset.set(i);
        }
        ++i;
        tokenType = in.peek();
      }
      in.endArray();
      return bitset;
    }

    @Override public void write(JsonWriter out, BitSet src) throws IOException {
      out.beginArray();
      for (int i = 0, length = src.length(); i < length; i++) {
        int value = (src.get(i)) ? 1 : 0;
        out.value(value);
      }
      out.endArray();
    }
  }.nullSafe();

  public static final TypeAdapterFactory BIT_SET_FACTORY = newFactory(BitSet.class, BIT_SET);

  public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>() {
    @Override
    public Boolean read(JsonReader in) throws IOException {
      JsonToken peek = in.peek();
      if (peek == JsonToken.NULL) {
        in.nextNull();
        return null;
      } else if (peek == JsonToken.STRING) {
        // support strings for compatibility with GSON 1.7
        return Boolean.parseBoolean(in.nextString());
      }
      return in.nextBoolean();
    }
    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
      out.value(value);
    }
  };

  /**
   * Writes a boolean as a string. Useful for map keys, where booleans aren't
   * otherwise permitted.
   */
  public static final TypeAdapter<Boolean> BOOLEAN_AS_STRING = new TypeAdapter<Boolean>() {
    @Override public Boolean read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return Boolean.valueOf(in.nextString());
    }

    @Override public void write(JsonWriter out, Boolean value) throws IOException {
      out.value(value == null ? "null" : value.toString());
    }
  };

  public static final TypeAdapterFactory BOOLEAN_FACTORY
      = newFactory(boolean.class, Boolean.class, BOOLEAN);

  public static final TypeAdapter<Number> BYTE = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      int intValue;
      try {
        intValue = in.nextInt();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
      // Allow up to 255 to support unsigned values
      if (intValue > 255 || intValue < Byte.MIN_VALUE) {
        throw new JsonSyntaxException("Lossy conversion from " + intValue + " to byte; at path " + in.getPreviousPath());
      }
      return (byte) intValue;
    }
    @Override
    public void write(JsonWriter out, Number value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.byteValue());
      }
    }
  };

  public static final TypeAdapterFactory BYTE_FACTORY
      = newFactory(byte.class, Byte.class, BYTE);

  public static final TypeAdapter<Number> SHORT = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      int intValue;
      try {
        intValue = in.nextInt();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
      // Allow up to 65535 to support unsigned values
      if (intValue > 65535 || intValue < Short.MIN_VALUE) {
        throw new JsonSyntaxException("Lossy conversion from " + intValue + " to short; at path " + in.getPreviousPath());
      }
      return (short) intValue;
    }
    @Override
    public void write(JsonWriter out, Number value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.shortValue());
      }
    }
  };

  public static final TypeAdapterFactory SHORT_FACTORY
      = newFactory(short.class, Short.class, SHORT);

  public static final TypeAdapter<Number> INTEGER = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return in.nextInt();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    @Override
    public void write(JsonWriter out, Number value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.intValue());
      }
    }
  };
  public static final TypeAdapterFactory INTEGER_FACTORY
      = newFactory(int.class, Integer.class, INTEGER);

  public static final TypeAdapter<AtomicInteger> ATOMIC_INTEGER = new TypeAdapter<AtomicInteger>() {
    @Override public AtomicInteger read(JsonReader in) throws IOException {
      try {
        return new AtomicInteger(in.nextInt());
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    @Override public void write(JsonWriter out, AtomicInteger value) throws IOException {
      out.value(value.get());
    }
  }.nullSafe();
  public static final TypeAdapterFactory ATOMIC_INTEGER_FACTORY =
      newFactory(AtomicInteger.class, TypeAdapters.ATOMIC_INTEGER);

  public static final TypeAdapter<AtomicBoolean> ATOMIC_BOOLEAN = new TypeAdapter<AtomicBoolean>() {
    @Override public AtomicBoolean read(JsonReader in) throws IOException {
      return new AtomicBoolean(in.nextBoolean());
    }
    @Override public void write(JsonWriter out, AtomicBoolean value) throws IOException {
      out.value(value.get());
    }
  }.nullSafe();
  public static final TypeAdapterFactory ATOMIC_BOOLEAN_FACTORY =
      newFactory(AtomicBoolean.class, TypeAdapters.ATOMIC_BOOLEAN);

  public static final TypeAdapter<AtomicIntegerArray> ATOMIC_INTEGER_ARRAY = new TypeAdapter<AtomicIntegerArray>() {
    @Override public AtomicIntegerArray read(JsonReader in) throws IOException {
        List<Integer> list = new ArrayList<>();
        in.beginArray();
        while (in.hasNext()) {
          try {
            int integer = in.nextInt();
            list.add(integer);
          } catch (NumberFormatException e) {
            throw new JsonSyntaxException(e);
          }
        }
        in.endArray();
        int length = list.size();
        AtomicIntegerArray array = new AtomicIntegerArray(length);
        for (int i = 0; i < length; ++i) {
          array.set(i, list.get(i));
        }
        return array;
    }
    @Override public void write(JsonWriter out, AtomicIntegerArray value) throws IOException {
      out.beginArray();
      for (int i = 0, length = value.length(); i < length; i++) {
        out.value(value.get(i));
      }
      out.endArray();
    }
  }.nullSafe();
  public static final TypeAdapterFactory ATOMIC_INTEGER_ARRAY_FACTORY =
      newFactory(AtomicIntegerArray.class, TypeAdapters.ATOMIC_INTEGER_ARRAY);

  public static final TypeAdapter<Number> LONG = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        return in.nextLong();
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException(e);
      }
    }
    @Override
    public void write(JsonWriter out, Number value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.longValue());
      }
    }
  };

  public static final TypeAdapter<Number> FLOAT = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return (float) in.nextDouble();
    }
    @Override
    public void write(JsonWriter out, Number value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        // For backward compatibility don't call `JsonWriter.value(float)` because that method has
        // been newly added and not all custom JsonWriter implementations might override it yet
        Number floatNumber = value instanceof Float ? value : value.floatValue();
        out.value(floatNumber);
      }
    }
  };

  public static final TypeAdapter<Number> DOUBLE = new TypeAdapter<Number>() {
    @Override
    public Number read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return in.nextDouble();
    }
    @Override
    public void write(JsonWriter out, Number value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.doubleValue());
      }
    }
  };

  public static final TypeAdapter<Character> CHARACTER = new TypeAdapter<Character>() {
    @Override
    public Character read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String str = in.nextString();
      if (str.length() != 1) {
        throw new JsonSyntaxException("Expecting character, got: " + str + "; at " + in.getPreviousPath());
      }
      return str.charAt(0);
    }
    @Override
    public void write(JsonWriter out, Character value) throws IOException {
      out.value(value == null ? null : String.valueOf(value));
    }
  };

  public static final TypeAdapterFactory CHARACTER_FACTORY
      = newFactory(char.class, Character.class, CHARACTER);

  public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
    @Override
    public String read(JsonReader in) throws IOException {
      JsonToken peek = in.peek();
      if (peek == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      /* coerce booleans to strings for backwards compatibility */
      if (peek == JsonToken.BOOLEAN) {
        return Boolean.toString(in.nextBoolean());
      }
      return in.nextString();
    }
    @Override
    public void write(JsonWriter out, String value) throws IOException {
      out.value(value);
    }
  };

  public static final TypeAdapter<BigDecimal> BIG_DECIMAL = new TypeAdapter<BigDecimal>() {
    @Override public BigDecimal read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String s = in.nextString();
      try {
        return new BigDecimal(s);
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException("Failed parsing '" + s + "' as BigDecimal; at path " + in.getPreviousPath(), e);
      }
    }

    @Override public void write(JsonWriter out, BigDecimal value) throws IOException {
      out.value(value);
    }
  };

  public static final TypeAdapter<BigInteger> BIG_INTEGER = new TypeAdapter<BigInteger>() {
    @Override public BigInteger read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String s = in.nextString();
      try {
        return new BigInteger(s);
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException("Failed parsing '" + s + "' as BigInteger; at path " + in.getPreviousPath(), e);
      }
    }

    @Override public void write(JsonWriter out, BigInteger value) throws IOException {
      out.value(value);
    }
  };

  public static final TypeAdapter<LazilyParsedNumber> LAZILY_PARSED_NUMBER = new TypeAdapter<LazilyParsedNumber>() {
    // Normally users should not be able to access and deserialize LazilyParsedNumber because
    // it is an internal type, but implement this nonetheless in case there are legit corner
    // cases where this is possible
    @Override public LazilyParsedNumber read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new LazilyParsedNumber(in.nextString());
    }

    @Override public void write(JsonWriter out, LazilyParsedNumber value) throws IOException {
      out.value(value);
    }
  };

  public static final TypeAdapterFactory STRING_FACTORY = newFactory(String.class, STRING);

  public static final TypeAdapter<StringBuilder> STRING_BUILDER = new TypeAdapter<StringBuilder>() {
    @Override
    public StringBuilder read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new StringBuilder(in.nextString());
    }
    @Override
    public void write(JsonWriter out, StringBuilder value) throws IOException {
      out.value(value == null ? null : value.toString());
    }
  };

  public static final TypeAdapterFactory STRING_BUILDER_FACTORY =
    newFactory(StringBuilder.class, STRING_BUILDER);

  public static final TypeAdapter<StringBuffer> STRING_BUFFER = new TypeAdapter<StringBuffer>() {
    @Override
    public StringBuffer read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return new StringBuffer(in.nextString());
    }
    @Override
    public void write(JsonWriter out, StringBuffer value) throws IOException {
      out.value(value == null ? null : value.toString());
    }
  };

  public static final TypeAdapterFactory STRING_BUFFER_FACTORY =
    newFactory(StringBuffer.class, STRING_BUFFER);

  public static final TypeAdapter<URL> URL = new TypeAdapter<URL>() {
    @Override
    public URL read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String nextString = in.nextString();
      return "null".equals(nextString) ? null : new URL(nextString);
    }
    @Override
    public void write(JsonWriter out, URL value) throws IOException {
      out.value(value == null ? null : value.toExternalForm());
    }
  };

  public static final TypeAdapterFactory URL_FACTORY = newFactory(URL.class, URL);

  public static final TypeAdapter<URI> URI = new TypeAdapter<URI>() {
    @Override
    public URI read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      try {
        String nextString = in.nextString();
        return "null".equals(nextString) ? null : new URI(nextString);
      } catch (URISyntaxException e) {
        throw new JsonIOException(e);
      }
    }
    @Override
    public void write(JsonWriter out, URI value) throws IOException {
      out.value(value == null ? null : value.toASCIIString());
    }
  };

  public static final TypeAdapterFactory URI_FACTORY = newFactory(URI.class, URI);

  public static final TypeAdapter<InetAddress> INET_ADDRESS = new TypeAdapter<InetAddress>() {
    @Override
    public InetAddress read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      // regrettably, this should have included both the host name and the host address
      return InetAddress.getByName(in.nextString());
    }
    @Override
    public void write(JsonWriter out, InetAddress value) throws IOException {
      out.value(value == null ? null : value.getHostAddress());
    }
  };

  public static final TypeAdapterFactory INET_ADDRESS_FACTORY =
    newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);

  public static final TypeAdapter<UUID> UUID = new TypeAdapter<UUID>() {
    @Override
    public UUID read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String s = in.nextString();
      try {
        return java.util.UUID.fromString(s);
      } catch (IllegalArgumentException e) {
        throw new JsonSyntaxException("Failed parsing '" + s + "' as UUID; at path " + in.getPreviousPath(), e);
      }
    }
    @Override
    public void write(JsonWriter out, UUID value) throws IOException {
      out.value(value == null ? null : value.toString());
    }
  };

  public static final TypeAdapterFactory UUID_FACTORY = newFactory(UUID.class, UUID);

  public static final TypeAdapter<Currency> CURRENCY = new TypeAdapter<Currency>() {
    @Override
    public Currency read(JsonReader in) throws IOException {
      String s = in.nextString();
      try {
        return Currency.getInstance(s);
      } catch (IllegalArgumentException e) {
        throw new JsonSyntaxException("Failed parsing '" + s + "' as Currency; at path " + in.getPreviousPath(), e);
      }
    }
    @Override
    public void write(JsonWriter out, Currency value) throws IOException {
      out.value(value.getCurrencyCode());
    }
  }.nullSafe();
  public static final TypeAdapterFactory CURRENCY_FACTORY = newFactory(Currency.class, CURRENCY);

  public static final TypeAdapter<Calendar> CALENDAR = new TypeAdapter<Calendar>() {
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY_OF_MONTH = "dayOfMonth";
    private static final String HOUR_OF_DAY = "hourOfDay";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    @Override
    public Calendar read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return  null;
      }
      in.beginObject();
      int year = 0;
      int month = 0;
      int dayOfMonth = 0;
      int hourOfDay = 0;
      int minute = 0;
      int second = 0;
      while (in.peek() != JsonToken.END_OBJECT) {
        String name = in.nextName();
        int value = in.nextInt();
        if (YEAR.equals(name)) {
          year = value;
        } else if (MONTH.equals(name)) {
          month = value;
        } else if (DAY_OF_MONTH.equals(name)) {
          dayOfMonth = value;
        } else if (HOUR_OF_DAY.equals(name)) {
          hourOfDay = value;
        } else if (MINUTE.equals(name)) {
          minute = value;
        } else if (SECOND.equals(name)) {
          second = value;
        }
      }
      in.endObject();
      return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
    }

    @Override
    public void write(JsonWriter out, Calendar value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }
      out.beginObject();
      out.name(YEAR);
      out.value(value.get(Calendar.YEAR));
      out.name(MONTH);
      out.value(value.get(Calendar.MONTH));
      out.name(DAY_OF_MONTH);
      out.value(value.get(Calendar.DAY_OF_MONTH));
      out.name(HOUR_OF_DAY);
      out.value(value.get(Calendar.HOUR_OF_DAY));
      out.name(MINUTE);
      out.value(value.get(Calendar.MINUTE));
      out.name(SECOND);
      out.value(value.get(Calendar.SECOND));
      out.endObject();
    }
  };

  public static final TypeAdapterFactory CALENDAR_FACTORY =
    newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, CALENDAR);

  public static final TypeAdapter<Locale> LOCALE = new TypeAdapter<Locale>() {
    @Override
    public Locale read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String locale = in.nextString();
      StringTokenizer tokenizer = new StringTokenizer(locale, "_");
      String language = null;
      String country = null;
      String variant = null;
      if (tokenizer.hasMoreElements()) {
        language = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        country = tokenizer.nextToken();
      }
      if (tokenizer.hasMoreElements()) {
        variant = tokenizer.nextToken();
      }
      if (country == null && variant == null) {
        return new Locale(language);
      } else if (variant == null) {
        return new Locale(language, country);
      } else {
        return new Locale(language, country, variant);
      }
    }
    @Override
    public void write(JsonWriter out, Locale value) throws IOException {
      out.value(value == null ? null : value.toString());
    }
  };

  public static final TypeAdapterFactory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);

  public static final TypeAdapter<JsonElement> JSON_ELEMENT = new TypeAdapter<JsonElement>() {
    /**
     * Tries to begin reading a JSON array or JSON object, returning {@code null} if
     * the next element is neither of those.
     */
    private JsonElement tryBeginNesting(JsonReader in, JsonToken peeked) throws IOException {
      switch (peeked) {
        case BEGIN_ARRAY:
          in.beginArray();
          return new JsonArray();
        case BEGIN_OBJECT:
          in.beginObject();
          return new JsonObject();
        default:
          return null;
      }
    }

    /** Reads a {@link JsonElement} which cannot have any nested elements */
    private JsonElement readTerminal(JsonReader in, JsonToken peeked) throws IOException {
      switch (peeked) {
        case STRING:
          return new JsonPrimitive(in.nextString());
        case NUMBER:
          String number = in.nextString();
          return new JsonPrimitive(new LazilyParsedNumber(number));
        case BOOLEAN:
          return new JsonPrimitive(in.nextBoolean());
        case NULL:
          in.nextNull();
          return JsonNull.INSTANCE;
        default:
          // When read(JsonReader) is called with JsonReader in invalid state
          throw new IllegalStateException("Unexpected token: " + peeked);
      }
    }

    @Override public JsonElement read(JsonReader in) throws IOException {
      if (in instanceof JsonTreeReader) {
        return ((JsonTreeReader) in).nextJsonElement();
      }

      // Either JsonArray or JsonObject
      JsonElement current;
      JsonToken peeked = in.peek();

      current = tryBeginNesting(in, peeked);
      if (current == null) {
        return readTerminal(in, peeked);
      }

      Deque<JsonElement> stack = new ArrayDeque<>();

      while (true) {
        while (in.hasNext()) {
          String name = null;
          // Name is only used for JSON object members
          if (current instanceof JsonObject) {
            name = in.nextName();
          }

          peeked = in.peek();
          JsonElement value = tryBeginNesting(in, peeked);
          boolean isNesting = value != null;

          if (value == null) {
            value = readTerminal(in, peeked);
          }

          if (current instanceof JsonArray) {
            ((JsonArray) current).add(value);
          } else {
            ((JsonObject) current).add(name, value);
          }

          if (isNesting) {
            stack.addLast(current);
            current = value;
          }
        }

        // End current element
        if (current instanceof JsonArray) {
          in.endArray();
        } else {
          in.endObject();
        }

        if (stack.isEmpty()) {
          return current;
        } else {
          // Continue with enclosing element
          current = stack.removeLast();
        }
      }
    }

    @Override public void write(JsonWriter out, JsonElement value) throws IOException {
      if (value == null || value.isJsonNull()) {
        out.nullValue();
      } else if (value.isJsonPrimitive()) {
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isNumber()) {
          out.value(primitive.getAsNumber());
        } else if (primitive.isBoolean()) {
          out.value(primitive.getAsBoolean());
        } else {
          out.value(primitive.getAsString());
        }

      } else if (value.isJsonArray()) {
        out.beginArray();
        for (JsonElement e : value.getAsJsonArray()) {
          write(out, e);
        }
        out.endArray();

      } else if (value.isJsonObject()) {
        out.beginObject();
        for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
          out.name(e.getKey());
          write(out, e.getValue());
        }
        out.endObject();

      } else {
        throw new IllegalArgumentException("Couldn't write " + value.getClass());
      }
    }
  };

  public static final TypeAdapterFactory JSON_ELEMENT_FACTORY
      = newTypeHierarchyFactory(JsonElement.class, JSON_ELEMENT);

  private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
    private final Map<String, T> nameToConstant = new HashMap<>();
    private final Map<String, T> stringToConstant = new HashMap<>();
    private final Map<T, String> constantToName = new HashMap<>();

    public EnumTypeAdapter(final Class<T> classOfT) {
      try {
        // Uses reflection to find enum constants to work around name mismatches for obfuscated classes
        // Reflection access might throw SecurityException, therefore run this in privileged context;
        // should be acceptable because this only retrieves enum constants, but does not expose anything else
        Field[] constantFields = AccessController.doPrivileged(new PrivilegedAction<Field[]>() {
          @Override public Field[] run() {
            Field[] fields = classOfT.getDeclaredFields();
            ArrayList<Field> constantFieldsList = new ArrayList<>(fields.length);
            for (Field f : fields) {
              if (f.isEnumConstant()) {
                constantFieldsList.add(f);
              }
            }

            Field[] constantFields = constantFieldsList.toArray(new Field[0]);
            AccessibleObject.setAccessible(constantFields, true);
            return constantFields;
          }
        });
        for (Field constantField : constantFields) {
          @SuppressWarnings("unchecked")
          T constant = (T)(constantField.get(null));
          String name = constant.name();
          String toStringVal = constant.toString();

          SerializedName annotation = constantField.getAnnotation(SerializedName.class);
          if (annotation != null) {
            name = annotation.value();
            for (String alternate : annotation.alternate()) {
              nameToConstant.put(alternate, constant);
            }
          }
          nameToConstant.put(name, constant);
          stringToConstant.put(toStringVal, constant);
          constantToName.put(constant, name);
        }
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
    @Override public T read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String key = in.nextString();
      T constant = nameToConstant.get(key);
      return (constant == null) ? stringToConstant.get(key) : constant;
    }

    @Override public void write(JsonWriter out, T value) throws IOException {
      out.value(value == null ? null : constantToName.get(value));
    }
  }

  public static final TypeAdapterFactory ENUM_FACTORY = new TypeAdapterFactory() {
    @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      Class<? super T> rawType = typeToken.getRawType();
      if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
        return null;
      }
      if (!rawType.isEnum()) {
        rawType = rawType.getSuperclass(); // handle anonymous subclasses
      }
      @SuppressWarnings({"rawtypes", "unchecked"})
      TypeAdapter<T> adapter = (TypeAdapter<T>) new EnumTypeAdapter(rawType);
      return adapter;
    }
  };

  public static <TT> TypeAdapterFactory newFactory(
      final TypeToken<TT> type, final TypeAdapter<TT> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        return typeToken.equals(type) ? (TypeAdapter<T>) typeAdapter : null;
      }
    };
  }

  public static <TT> TypeAdapterFactory newFactory(
      final Class<TT> type, final TypeAdapter<TT> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        return typeToken.getRawType() == type ? (TypeAdapter<T>) typeAdapter : null;
      }
      @Override public String toString() {
        return "Factory[type=" + type.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }

  public static <TT> TypeAdapterFactory newFactory(
      final Class<TT> unboxed, final Class<TT> boxed, final TypeAdapter<? super TT> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == unboxed || rawType == boxed) ? (TypeAdapter<T>) typeAdapter : null;
      }
      @Override public String toString() {
        return "Factory[type=" + boxed.getName()
            + "+" + unboxed.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }

  public static <TT> TypeAdapterFactory newFactoryForMultipleTypes(final Class<TT> base,
      final Class<? extends TT> sub, final TypeAdapter<? super TT> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
      @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        return (rawType == base || rawType == sub) ? (TypeAdapter<T>) typeAdapter : null;
      }
      @Override public String toString() {
        return "Factory[type=" + base.getName()
            + "+" + sub.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }

  /**
   * Returns a factory for all subtypes of {@code typeAdapter}. We do a runtime check to confirm
   * that the deserialized type matches the type requested.
   */
  public static <T1> TypeAdapterFactory newTypeHierarchyFactory(
      final Class<T1> clazz, final TypeAdapter<T1> typeAdapter) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      @Override public <T2> TypeAdapter<T2> create(Gson gson, TypeToken<T2> typeToken) {
        final Class<? super T2> requestedType = typeToken.getRawType();
        if (!clazz.isAssignableFrom(requestedType)) {
          return null;
        }
        return (TypeAdapter<T2>) new TypeAdapter<T1>() {
          @Override public void write(JsonWriter out, T1 value) throws IOException {
            typeAdapter.write(out, value);
          }

          @Override public T1 read(JsonReader in) throws IOException {
            T1 result = typeAdapter.read(in);
            if (result != null && !requestedType.isInstance(result)) {
              throw new JsonSyntaxException("Expected a " + requestedType.getName()
                  + " but was " + result.getClass().getName() + "; at path " + in.getPreviousPath());
            }
            return result;
          }
        };
      }
      @Override public String toString() {
        return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
      }
    };
  }
}
