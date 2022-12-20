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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.JavaVersion;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional test for Json serialization and deserialization for common classes for which default
 * support is provided in Gson. The tests for Map types are available in {@link MapTest}.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class DefaultTypeAdaptersTest {

  private Gson gson;
  private TimeZone oldTimeZone;
  private Locale oldLocale;

  @Before
  public void setUp() throws Exception {
    this.oldTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    this.oldLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    gson = new Gson();
  }

  @After
  public void tearDown() throws Exception {
    TimeZone.setDefault(oldTimeZone);
    Locale.setDefault(oldLocale);
  }

  @Test
  public void testClassSerialization() {
    try {
      gson.toJson(String.class);
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    // Override with a custom type adapter for class.
    gson = new GsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertEquals("\"java.lang.String\"", gson.toJson(String.class));
  }

  @Test
  public void testClassDeserialization() {
    try {
      gson.fromJson("String.class", String.class.getClass());
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    // Override with a custom type adapter for class.
    gson = new GsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertEquals(String.class, gson.fromJson("java.lang.String", Class.class));
  }

  @Test
  public void testUrlSerialization() throws Exception {
    String urlValue = "http://google.com/";
    URL url = new URL(urlValue);
    assertEquals("\"http://google.com/\"", gson.toJson(url));
  }

  @Test
  public void testUrlDeserialization() {
    String urlValue = "http://google.com/";
    String json = "'http:\\/\\/google.com\\/'";
    URL target = gson.fromJson(json, URL.class);
    assertEquals(urlValue, target.toExternalForm());

    gson.fromJson('"' + urlValue + '"', URL.class);
    assertEquals(urlValue, target.toExternalForm());
  }

  @Test
  public void testUrlNullSerialization() throws Exception {
    ClassWithUrlField target = new ClassWithUrlField();
    assertEquals("{}", gson.toJson(target));
  }

  @Test
  public void testUrlNullDeserialization() {
    String json = "{}";
    ClassWithUrlField target = gson.fromJson(json, ClassWithUrlField.class);
    assertNull(target.url);
  }

  @Test
  public void testUriSerialization() throws Exception {
    String uriValue = "http://google.com/";
    URI uri = new URI(uriValue);
    assertEquals("\"http://google.com/\"", gson.toJson(uri));
  }

  @Test
  public void testUriDeserialization() {
    String uriValue = "http://google.com/";
    String json = '"' + uriValue + '"';
    URI target = gson.fromJson(json, URI.class);
    assertEquals(uriValue, target.toASCIIString());
  }

  @Test
  public void testNullSerialization() throws Exception {
    testNullSerializationAndDeserialization(Boolean.class);
    testNullSerializationAndDeserialization(Byte.class);
    testNullSerializationAndDeserialization(Short.class);
    testNullSerializationAndDeserialization(Integer.class);
    testNullSerializationAndDeserialization(Long.class);
    testNullSerializationAndDeserialization(Double.class);
    testNullSerializationAndDeserialization(Float.class);
    testNullSerializationAndDeserialization(Number.class);
    testNullSerializationAndDeserialization(Character.class);
    testNullSerializationAndDeserialization(String.class);
    testNullSerializationAndDeserialization(StringBuilder.class);
    testNullSerializationAndDeserialization(StringBuffer.class);
    testNullSerializationAndDeserialization(BigDecimal.class);
    testNullSerializationAndDeserialization(BigInteger.class);
    testNullSerializationAndDeserialization(TreeSet.class);
    testNullSerializationAndDeserialization(ArrayList.class);
    testNullSerializationAndDeserialization(HashSet.class);
    testNullSerializationAndDeserialization(Properties.class);
    testNullSerializationAndDeserialization(URL.class);
    testNullSerializationAndDeserialization(URI.class);
    testNullSerializationAndDeserialization(UUID.class);
    testNullSerializationAndDeserialization(Locale.class);
    testNullSerializationAndDeserialization(InetAddress.class);
    testNullSerializationAndDeserialization(BitSet.class);
    testNullSerializationAndDeserialization(Date.class);
    testNullSerializationAndDeserialization(GregorianCalendar.class);
    testNullSerializationAndDeserialization(Calendar.class);
    testNullSerializationAndDeserialization(Class.class);
  }

  @Test
  public void testUuidSerialization() throws Exception {
    String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    UUID uuid = UUID.fromString(uuidValue);
    assertEquals('"' + uuidValue + '"', gson.toJson(uuid));
  }

  private void testNullSerializationAndDeserialization(Class<?> c) {
    testNullSerializationAndDeserialization(gson, c);
  }

  public static void testNullSerializationAndDeserialization(Gson gson, Class<?> c) {
    assertEquals("null", gson.toJson(null, c));
    assertNull(gson.fromJson("null", c));
  }

  @Test
  public void testUuidDeserialization() {
    String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    String json = '"' + uuidValue + '"';
    UUID target = gson.fromJson(json, UUID.class);
    assertEquals(uuidValue, target.toString());
  }

  @Test
  public void testLocaleSerializationWithLanguage() {
    Locale target = new Locale("en");
    assertEquals("\"en\"", gson.toJson(target));
  }

  @Test
  public void testLocaleDeserializationWithLanguage() {
    String json = "\"en\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("en", locale.getLanguage());
  }

  @Test
  public void testLocaleSerializationWithLanguageCountry() {
    Locale target = Locale.CANADA_FRENCH;
    assertEquals("\"fr_CA\"", gson.toJson(target));
  }

  @Test
  public void testLocaleDeserializationWithLanguageCountry() {
    String json = "\"fr_CA\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals(Locale.CANADA_FRENCH, locale);
  }

  @Test
  public void testLocaleSerializationWithLanguageCountryVariant() {
    Locale target = new Locale("de", "DE", "EURO");
    String json = gson.toJson(target);
    assertEquals("\"de_DE_EURO\"", json);
  }

  @Test
  public void testLocaleDeserializationWithLanguageCountryVariant() {
    String json = "\"de_DE_EURO\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("de", locale.getLanguage());
    assertEquals("DE", locale.getCountry());
    assertEquals("EURO", locale.getVariant());
  }

  @Test
  public void testBigDecimalFieldSerialization() {
    ClassWithBigDecimal target = new ClassWithBigDecimal("-122.01e-21");
    String json = gson.toJson(target);
    String actual = json.substring(json.indexOf(':') + 1, json.indexOf('}'));
    assertEquals(target.value, new BigDecimal(actual));
  }

  @Test
  public void testBigDecimalFieldDeserialization() {
    ClassWithBigDecimal expected = new ClassWithBigDecimal("-122.01e-21");
    String json = expected.getExpectedJson();
    ClassWithBigDecimal actual = gson.fromJson(json, ClassWithBigDecimal.class);
    assertEquals(expected.value, actual.value);
  }

  @Test
  public void testBadValueForBigDecimalDeserialization() {
    try {
      gson.fromJson("{\"value\"=1.5e-1.0031}", ClassWithBigDecimal.class);
      fail("Exponent of a BigDecimal must be an integer value.");
    } catch (JsonParseException expected) {
    }
  }

  @Test
  public void testBigIntegerFieldSerialization() {
    ClassWithBigInteger target = new ClassWithBigInteger("23232323215323234234324324324324324324");
    String json = gson.toJson(target);
    assertEquals(target.getExpectedJson(), json);
  }

  @Test
  public void testBigIntegerFieldDeserialization() {
    ClassWithBigInteger expected = new ClassWithBigInteger("879697697697697697697697697697697697");
    String json = expected.getExpectedJson();
    ClassWithBigInteger actual = gson.fromJson(json, ClassWithBigInteger.class);
    assertEquals(expected.value, actual.value);
  }

  @Test
  public void testOverrideBigIntegerTypeAdapter() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(BigInteger.class, new NumberAsStringAdapter(BigInteger.class))
        .create();
    assertEquals("\"123\"", gson.toJson(new BigInteger("123"), BigInteger.class));
    assertEquals(new BigInteger("123"), gson.fromJson("\"123\"", BigInteger.class));
  }

  @Test
  public void testOverrideBigDecimalTypeAdapter() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(BigDecimal.class, new NumberAsStringAdapter(BigDecimal.class))
        .create();
    assertEquals("\"1.1\"", gson.toJson(new BigDecimal("1.1"), BigDecimal.class));
    assertEquals(new BigDecimal("1.1"), gson.fromJson("\"1.1\"", BigDecimal.class));
  }

  @Test
  public void testSetSerialization() throws Exception {
    Gson gson = new Gson();
    HashSet<String> s = new HashSet<>();
    s.add("blah");
    String json = gson.toJson(s);
    assertEquals("[\"blah\"]", json);

    json = gson.toJson(s, Set.class);
    assertEquals("[\"blah\"]", json);
  }

  @Test
  public void testBitSetSerialization() throws Exception {
    Gson gson = new Gson();
    BitSet bits = new BitSet();
    bits.set(1);
    bits.set(3, 6);
    bits.set(9);
    String json = gson.toJson(bits);
    assertEquals("[0,1,0,1,1,1,0,0,0,1]", json);
  }

  @Test
  public void testBitSetDeserialization() throws Exception {
    BitSet expected = new BitSet();
    expected.set(0);
    expected.set(2, 6);
    expected.set(8);

    Gson gson = new Gson();
    String json = gson.toJson(expected);
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[1,0,1,1,1,1,0,0,1,0,0,0]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[\"1\",\"0\",\"1\",\"1\",\"1\",\"1\",\"0\",\"0\",\"1\"]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    json = "[true,false,true,true,true,true,false,false,true,false,false]";
    assertEquals(expected, gson.fromJson(json, BitSet.class));

    try {
      gson.fromJson("[1, []]", BitSet.class);
      fail();
    } catch (JsonSyntaxException e) {
      assertEquals("Invalid bitset value type: BEGIN_ARRAY; at path $[1]", e.getMessage());
    }

    try {
      gson.fromJson("[1, 2]", BitSet.class);
      fail();
    } catch (JsonSyntaxException e) {
      assertEquals("Invalid bitset value 2, expected 0 or 1; at path $[1]", e.getMessage());
    }
  }

  @Test
  public void testDefaultDateSerialization() {
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    if (JavaVersion.isJava9OrLater()) {
      assertEquals("\"Sep 11, 2011, 10:55:03 PM\"", json);
    } else {
      assertEquals("\"Sep 11, 2011 10:55:03 PM\"", json);
    }
  }

  @Test
  public void testDefaultDateDeserialization() {
    String json = "'Dec 13, 2009 07:18:02 AM'";
    Date extracted = gson.fromJson(json, Date.class);
    assertEqualsDate(extracted, 2009, 11, 13);
    assertEqualsTime(extracted, 7, 18, 2);
  }

  @Test
  public void testDefaultDateSerializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    if (JavaVersion.isJava9OrLater()) {
      assertEquals("\"Sep 11, 2011, 10:55:03 PM\"", json);
    } else {
      assertEquals("\"Sep 11, 2011 10:55:03 PM\"", json);
    }
  }

  // Date can not directly be compared with another instance since the deserialization loses the
  // millisecond portion.
  @SuppressWarnings("deprecation")
  public static void assertEqualsDate(Date date, int year, int month, int day) {
    assertEquals(year-1900, date.getYear());
    assertEquals(month, date.getMonth());
    assertEquals(day, date.getDate());
  }

  @SuppressWarnings("deprecation")
  public static void assertEqualsTime(Date date, int hours, int minutes, int seconds) {
    assertEquals(hours, date.getHours());
    assertEquals(minutes, date.getMinutes());
    assertEquals(seconds, date.getSeconds());
  }

  @Test
  public void testDefaultDateDeserializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.toString(), extracted.toString());
  }

  @Test
  public void testDefaultCalendarSerialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(Calendar.getInstance());
    assertTrue(json.contains("year"));
    assertTrue(json.contains("month"));
    assertTrue(json.contains("dayOfMonth"));
    assertTrue(json.contains("hourOfDay"));
    assertTrue(json.contains("minute"));
    assertTrue(json.contains("second"));
  }

  @Test
  public void testDefaultCalendarDeserialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    Calendar cal = gson.fromJson(json, Calendar.class);
    assertEquals(2009, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(29, cal.get(Calendar.MINUTE));
    assertEquals(23, cal.get(Calendar.SECOND));
  }

  @Test
  public void testDefaultGregorianCalendarSerialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    GregorianCalendar cal = new GregorianCalendar();
    String json = gson.toJson(cal);
    assertTrue(json.contains("year"));
    assertTrue(json.contains("month"));
    assertTrue(json.contains("dayOfMonth"));
    assertTrue(json.contains("hourOfDay"));
    assertTrue(json.contains("minute"));
    assertTrue(json.contains("second"));
  }

  @Test
  public void testDefaultGregorianCalendarDeserialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    GregorianCalendar cal = gson.fromJson(json, GregorianCalendar.class);
    assertEquals(2009, cal.get(Calendar.YEAR));
    assertEquals(2, cal.get(Calendar.MONTH));
    assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(29, cal.get(Calendar.MINUTE));
    assertEquals(23, cal.get(Calendar.SECOND));
  }

  @Test
  public void testDateSerializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    assertEquals("\"2011-09-11\"", json);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testDateDeserializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.getYear(), extracted.getYear());
    assertEquals(now.getMonth(), extracted.getMonth());
    assertEquals(now.getDay(), extracted.getDay());
  }

  @Test
  public void testDateSerializationWithPatternNotOverridenByTypeAdapter() throws Exception {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder()
        .setDateFormat(pattern)
        .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
          @Override
          public Date deserialize(JsonElement json, Type typeOfT,
              JsonDeserializationContext context)
              throws JsonParseException {
            return new Date(1315806903103L);
          }
        })
        .create();

    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    assertEquals("\"2011-09-11\"", json);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  @Test
  public void testDateSerializationInCollection() throws Exception {
    Type listOfDates = new TypeToken<List<Date>>() {
    }.getType();
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      List<Date> dates = Arrays.asList(new Date(0));
      String json = gson.toJson(dates, listOfDates);
      assertEquals("[\"1970-01-01\"]", json);
      assertEquals(0L, gson.<List<Date>>fromJson("[\"1970-01-01\"]", listOfDates).get(0).getTime());
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testJsonPrimitiveSerialization() {
    assertEquals("5", gson.toJson(new JsonPrimitive(5), JsonElement.class));
    assertEquals("true", gson.toJson(new JsonPrimitive(true), JsonElement.class));
    assertEquals("\"foo\"", gson.toJson(new JsonPrimitive("foo"), JsonElement.class));
    assertEquals("\"a\"", gson.toJson(new JsonPrimitive('a'), JsonElement.class));
  }

  @Test
  public void testJsonPrimitiveDeserialization() {
    assertEquals(new JsonPrimitive(5), gson.fromJson("5", JsonElement.class));
    assertEquals(new JsonPrimitive(5), gson.fromJson("5", JsonPrimitive.class));
    assertEquals(new JsonPrimitive(true), gson.fromJson("true", JsonElement.class));
    assertEquals(new JsonPrimitive(true), gson.fromJson("true", JsonPrimitive.class));
    assertEquals(new JsonPrimitive("foo"), gson.fromJson("\"foo\"", JsonElement.class));
    assertEquals(new JsonPrimitive("foo"), gson.fromJson("\"foo\"", JsonPrimitive.class));
    assertEquals(new JsonPrimitive('a'), gson.fromJson("\"a\"", JsonElement.class));
    assertEquals(new JsonPrimitive('a'), gson.fromJson("\"a\"", JsonPrimitive.class));
  }

  @Test
  public void testJsonNullSerialization() {
    assertEquals("null", gson.toJson(JsonNull.INSTANCE, JsonElement.class));
    assertEquals("null", gson.toJson(JsonNull.INSTANCE, JsonNull.class));
  }

  @Test
  public void testNullJsonElementSerialization() {
    assertEquals("null", gson.toJson(null, JsonElement.class));
    assertEquals("null", gson.toJson(null, JsonNull.class));
  }

  @Test
  public void testJsonArraySerialization() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));
    assertEquals("[1,2,3]", gson.toJson(array, JsonElement.class));
  }

  @Test
  public void testJsonArrayDeserialization() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));

    String json = "[1,2,3]";
    assertEquals(array, gson.fromJson(json, JsonElement.class));
    assertEquals(array, gson.fromJson(json, JsonArray.class));
  }

  @Test
  public void testJsonObjectSerialization() {
    JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));
    assertEquals("{\"foo\":1,\"bar\":2}", gson.toJson(object, JsonElement.class));
  }

  @Test
  public void testJsonObjectDeserialization() {
    JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));

    String json = "{\"foo\":1,\"bar\":2}";
    JsonElement actual = gson.fromJson(json, JsonElement.class);
    assertEquals(object, actual);

    JsonObject actualObj = gson.fromJson(json, JsonObject.class);
    assertEquals(object, actualObj);
  }

  @Test
  public void testJsonNullDeserialization() {
    assertEquals(JsonNull.INSTANCE, gson.fromJson("null", JsonElement.class));
    assertEquals(JsonNull.INSTANCE, gson.fromJson("null", JsonNull.class));
  }

  @Test
  public void testJsonElementTypeMismatch() {
    try {
      gson.fromJson("\"abc\"", JsonObject.class);
      fail();
    } catch (JsonSyntaxException expected) {
      assertEquals(
          "Expected a com.google.gson.JsonObject but was com.google.gson.JsonPrimitive; at path $",
          expected.getMessage());
    }
  }

  @Test
  public void testPropertiesSerialization() {
    Properties props = new Properties();
    props.setProperty("foo", "bar");
    String json = gson.toJson(props);
    String expected = "{\"foo\":\"bar\"}";
    assertEquals(expected, json);
  }

  private static class ClassWithBigDecimal {
    BigDecimal value;
    ClassWithBigDecimal(String value) {
      this.value = new BigDecimal(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value.toEngineeringString() + "}";
    }
  }

  @Test
  public void testPropertiesDeserialization() {
    String json = "{foo:'bar'}";
    Properties props = gson.fromJson(json, Properties.class);
    assertEquals("bar", props.getProperty("foo"));
  }

  @Test
  public void testTreeSetSerialization() {
    TreeSet<String> treeSet = new TreeSet<>();
    treeSet.add("Value1");
    String json = gson.toJson(treeSet);
    assertEquals("[\"Value1\"]", json);
  }

  @Test
  public void testTreeSetDeserialization() {
    String json = "['Value1']";
    Type type = new TypeToken<TreeSet<String>>() {
    }.getType();
    TreeSet<String> treeSet = gson.fromJson(json, type);
    assertTrue(treeSet.contains("Value1"));
  }

  @Test
  public void testStringBuilderSerialization() {
    StringBuilder sb = new StringBuilder("abc");
    String json = gson.toJson(sb);
    assertEquals("\"abc\"", json);
  }

  @Test
  public void testStringBuilderDeserialization() {
    StringBuilder sb = gson.fromJson("'abc'", StringBuilder.class);
    assertEquals("abc", sb.toString());
  }

  @Test
  public void testStringBufferSerialization() {
    StringBuffer sb = new StringBuffer("abc");
    String json = gson.toJson(sb);
    assertEquals("\"abc\"", json);
  }

  @Test
  public void testStringBufferDeserialization() {
    StringBuffer sb = gson.fromJson("'abc'", StringBuffer.class);
    assertEquals("abc", sb.toString());
  }

  private static class ClassWithUrlField {

    URL url;
  }

  private static class ClassWithBigInteger {

    BigInteger value;

    ClassWithBigInteger(String value) {
      this.value = new BigInteger(value);
    }

    String getExpectedJson() {
      return "{\"value\":" + value + "}";
    }
  }

  private static class MyClassTypeAdapter extends TypeAdapter<Class<?>> {

    @Override
    public void write(JsonWriter out, Class<?> value) throws IOException {
      out.value(value.getName());
    }

    @Override
    public Class<?> read(JsonReader in) throws IOException {
      String className = in.nextString();
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new IOException(e);
      }
    }
  }

  static class NumberAsStringAdapter extends TypeAdapter<Number> {
    private final Constructor<? extends Number> constructor;
    NumberAsStringAdapter(Class<? extends Number> type) throws Exception {
      this.constructor = type.getConstructor(String.class);
    }
    @Override public void write(JsonWriter out, Number value) throws IOException {
      out.value(value.toString());
    }
    @Override public Number read(JsonReader in) throws IOException {
      try {
        return constructor.newInstance(in.nextString());
      } catch (Exception e) {
        throw new AssertionError(e);
      }
    }
  }
}
