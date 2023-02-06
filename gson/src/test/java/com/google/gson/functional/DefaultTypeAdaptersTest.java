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
  public void tearDown() {
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
    assertThat(gson.toJson(String.class)).isEqualTo("\"java.lang.String\"");
  }

  @Test
  public void testClassDeserialization() {
    try {
      gson.fromJson("String.class", Class.class);
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    // Override with a custom type adapter for class.
    gson = new GsonBuilder().registerTypeAdapter(Class.class, new MyClassTypeAdapter()).create();
    assertThat(gson.fromJson("java.lang.String", Class.class)).isAssignableTo(String.class);
  }

  @Test
  public void testUrlSerialization() throws Exception {
    String urlValue = "http://google.com/";
    URL url = new URL(urlValue);
    assertThat(gson.toJson(url)).isEqualTo("\"http://google.com/\"");
  }

  @Test
  public void testUrlDeserialization() {
    String urlValue = "http://google.com/";
    String json = "'http:\\/\\/google.com\\/'";
    URL target = gson.fromJson(json, URL.class);
    assertThat(target.toExternalForm()).isEqualTo(urlValue);

    gson.fromJson('"' + urlValue + '"', URL.class);
    assertThat(target.toExternalForm()).isEqualTo(urlValue);
  }

  @Test
  public void testUrlNullSerialization() {
    ClassWithUrlField target = new ClassWithUrlField();
    assertThat(gson.toJson(target)).isEqualTo("{}");
  }

  @Test
  public void testUrlNullDeserialization() {
    String json = "{}";
    ClassWithUrlField target = gson.fromJson(json, ClassWithUrlField.class);
    assertThat(target.url).isNull();
  }

  private static class ClassWithUrlField {
    URL url;
  }

  @Test
  public void testUriSerialization() throws Exception {
    String uriValue = "http://google.com/";
    URI uri = new URI(uriValue);
    assertThat(gson.toJson(uri)).isEqualTo("\"http://google.com/\"");
  }

  @Test
  public void testUriDeserialization() {
    String uriValue = "http://google.com/";
    String json = '"' + uriValue + '"';
    URI target = gson.fromJson(json, URI.class);
    assertThat(target.toASCIIString()).isEqualTo(uriValue);
  }

  @Test
  public void testNullSerialization() {
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

  private void testNullSerializationAndDeserialization(Class<?> c) {
    testNullSerializationAndDeserialization(gson, c);
  }

  public static void testNullSerializationAndDeserialization(Gson gson, Class<?> c) {
    assertThat(gson.toJson(null, c)).isEqualTo("null");
    assertThat(gson.fromJson("null", c)).isEqualTo(null);
  }

  @Test
  public void testUuidSerialization() {
    String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    UUID uuid = UUID.fromString(uuidValue);
    assertThat(gson.toJson(uuid)).isEqualTo('"' + uuidValue + '"');
  }

  @Test
  public void testUuidDeserialization() {
    String uuidValue = "c237bec1-19ef-4858-a98e-521cf0aad4c0";
    String json = '"' + uuidValue + '"';
    UUID target = gson.fromJson(json, UUID.class);
    assertThat(target.toString()).isEqualTo(uuidValue);
  }

  @Test
  public void testLocaleSerializationWithLanguage() {
    Locale target = new Locale("en");
    assertThat(gson.toJson(target)).isEqualTo("\"en\"");
  }

  @Test
  public void testLocaleDeserializationWithLanguage() {
    String json = "\"en\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertThat(locale.getLanguage()).isEqualTo("en");
  }

  @Test
  public void testLocaleSerializationWithLanguageCountry() {
    Locale target = Locale.CANADA_FRENCH;
    assertThat(gson.toJson(target)).isEqualTo("\"fr_CA\"");
  }

  @Test
  public void testLocaleDeserializationWithLanguageCountry() {
    String json = "\"fr_CA\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertThat(locale).isEqualTo(Locale.CANADA_FRENCH);
  }

  @Test
  public void testLocaleSerializationWithLanguageCountryVariant() {
    Locale target = new Locale("de", "DE", "EURO");
    String json = gson.toJson(target);
    assertThat(json).isEqualTo("\"de_DE_EURO\"");
  }

  @Test
  public void testLocaleDeserializationWithLanguageCountryVariant() {
    String json = "\"de_DE_EURO\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertThat(locale.getLanguage()).isEqualTo("de");
    assertThat(locale.getCountry()).isEqualTo("DE");
    assertThat(locale.getVariant()).isEqualTo("EURO");
  }

  @Test
  public void testBigDecimalFieldSerialization() {
    ClassWithBigDecimal target = new ClassWithBigDecimal("-122.01e-21");
    String json = gson.toJson(target);
    String actual = json.substring(json.indexOf(':') + 1, json.indexOf('}'));
    assertThat(new BigDecimal(actual)).isEqualTo(target.value);
  }

  @Test
  public void testBigDecimalFieldDeserialization() {
    ClassWithBigDecimal expected = new ClassWithBigDecimal("-122.01e-21");
    String json = expected.getExpectedJson();
    ClassWithBigDecimal actual = gson.fromJson(json, ClassWithBigDecimal.class);
    assertThat(actual.value).isEqualTo(expected.value);
  }

  @Test
  public void testBadValueForBigDecimalDeserialization() {
    try {
      gson.fromJson("{\"value\"=1.5e-1.0031}", ClassWithBigDecimal.class);
      fail("Exponent of a BigDecimal must be an integer value.");
    } catch (JsonParseException expected) { }
  }

  @Test
  public void testBigIntegerFieldSerialization() {
    ClassWithBigInteger target = new ClassWithBigInteger("23232323215323234234324324324324324324");
    String json = gson.toJson(target);
    assertThat(json).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testBigIntegerFieldDeserialization() {
    ClassWithBigInteger expected = new ClassWithBigInteger("879697697697697697697697697697697697");
    String json = expected.getExpectedJson();
    ClassWithBigInteger actual = gson.fromJson(json, ClassWithBigInteger.class);
    assertThat(actual.value).isEqualTo(expected.value);
  }

  @Test
  public void testOverrideBigIntegerTypeAdapter() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(BigInteger.class, new NumberAsStringAdapter(BigInteger.class))
        .create();
    assertThat(gson.toJson(new BigInteger("123"), BigInteger.class)).isEqualTo("\"123\"");
    assertThat(gson.fromJson("\"123\"", BigInteger.class)).isEqualTo(new BigInteger("123"));
  }

  @Test
  public void testOverrideBigDecimalTypeAdapter() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(BigDecimal.class, new NumberAsStringAdapter(BigDecimal.class))
        .create();
    assertThat(gson.toJson(new BigDecimal("1.1"), BigDecimal.class)).isEqualTo("\"1.1\"");
    assertThat(gson.fromJson("\"1.1\"", BigDecimal.class)).isEqualTo(new BigDecimal("1.1"));
  }

  @Test
  public void testSetSerialization() {
    Gson gson = new Gson();
    HashSet<String> s = new HashSet<>();
    s.add("blah");
    String json = gson.toJson(s);
    assertThat(json).isEqualTo("[\"blah\"]");

    json = gson.toJson(s, Set.class);
    assertThat(json).isEqualTo("[\"blah\"]");
  }

  @Test
  public void testBitSetSerialization() {
    Gson gson = new Gson();
    BitSet bits = new BitSet();
    bits.set(1);
    bits.set(3, 6);
    bits.set(9);
    String json = gson.toJson(bits);
    assertThat(json).isEqualTo("[0,1,0,1,1,1,0,0,0,1]");
  }

  @Test
  public void testBitSetDeserialization() {
    BitSet expected = new BitSet();
    expected.set(0);
    expected.set(2, 6);
    expected.set(8);

    Gson gson = new Gson();
    String json = gson.toJson(expected);
    assertThat(gson.fromJson(json, BitSet.class)).isEqualTo(expected);

    json = "[1,0,1,1,1,1,0,0,1,0,0,0]";
    assertThat(gson.fromJson(json, BitSet.class)).isEqualTo(expected);

    json = "[\"1\",\"0\",\"1\",\"1\",\"1\",\"1\",\"0\",\"0\",\"1\"]";
    assertThat(gson.fromJson(json, BitSet.class)).isEqualTo(expected);

    json = "[true,false,true,true,true,true,false,false,true,false,false]";
    assertThat(gson.fromJson(json, BitSet.class)).isEqualTo(expected);

    try {
      gson.fromJson("[1, []]", BitSet.class);
      fail();
    } catch (JsonSyntaxException e) {
      assertThat(e.getMessage()).isEqualTo("Invalid bitset value type: BEGIN_ARRAY; at path $[1]");
    }

    try {
      gson.fromJson("[1, 2]", BitSet.class);
      fail();
    } catch (JsonSyntaxException e) {
      assertThat(e).hasMessageThat().isEqualTo("Invalid bitset value 2, expected 0 or 1; at path $[1]");
    }
  }

  @Test
  public void testDefaultDateSerialization() {
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    if (JavaVersion.isJava9OrLater()) {
      assertThat(json).isEqualTo("\"Sep 11, 2011, 10:55:03 PM\"");
    } else {
      assertThat(json).isEqualTo("\"Sep 11, 2011 10:55:03 PM\"");
    }
  }

  @Test
  public void testDefaultDateDeserialization() {
    String json = "'Dec 13, 2009 07:18:02 AM'";
    Date extracted = gson.fromJson(json, Date.class);
    assertEqualsDate(extracted, 2009, 11, 13);
    assertEqualsTime(extracted, 7, 18, 2);
  }

  // Date can not directly be compared with another instance since the deserialization loses the
  // millisecond portion.
  @SuppressWarnings("deprecation")
  public static void assertEqualsDate(Date date, int year, int month, int day) {
    assertThat(date.getYear()).isEqualTo(year-1900);
    assertThat(date.getMonth()).isEqualTo(month);
    assertThat(date.getDate()).isEqualTo(day);
  }

  @SuppressWarnings("deprecation")
  public static void assertEqualsTime(Date date, int hours, int minutes, int seconds) {
    assertThat(date.getHours()).isEqualTo(hours);
    assertThat(date.getMinutes()).isEqualTo(minutes);
    assertThat(date.getSeconds()).isEqualTo(seconds);
  }

  @Test
  public void testDefaultDateSerializationUsingBuilder() {
    Gson gson = new GsonBuilder().create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    if (JavaVersion.isJava9OrLater()) {
      assertThat(json).isEqualTo("\"Sep 11, 2011, 10:55:03 PM\"");
    } else {
      assertThat(json).isEqualTo("\"Sep 11, 2011 10:55:03 PM\"");
    }
  }

  @Test
  public void testDefaultDateDeserializationUsingBuilder() {
    Gson gson = new GsonBuilder().create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertThat(extracted.toString()).isEqualTo(now.toString());
  }

  @Test
  public void testDefaultCalendarSerialization() {
    Gson gson = new GsonBuilder().create();
    String json = gson.toJson(Calendar.getInstance());
    assertThat(json).contains("year");
    assertThat(json).contains("month");
    assertThat(json).contains("dayOfMonth");
    assertThat(json).contains("hourOfDay");
    assertThat(json).contains("minute");
    assertThat(json).contains("second");
  }

  @Test
  public void testDefaultCalendarDeserialization() {
    Gson gson = new GsonBuilder().create();
    String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    Calendar cal = gson.fromJson(json, Calendar.class);
    assertThat(cal.get(Calendar.YEAR)).isEqualTo(2009);
    assertThat(cal.get(Calendar.MONTH)).isEqualTo(2);
    assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(11);
    assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(14);
    assertThat(cal.get(Calendar.MINUTE)).isEqualTo(29);
    assertThat(cal.get(Calendar.SECOND)).isEqualTo(23);
  }

  @Test
  public void testDefaultGregorianCalendarSerialization() {
    Gson gson = new GsonBuilder().create();
    GregorianCalendar cal = new GregorianCalendar();
    String json = gson.toJson(cal);
    assertThat(json).contains("year");
    assertThat(json).contains("month");
    assertThat(json).contains("dayOfMonth");
    assertThat(json).contains("hourOfDay");
    assertThat(json).contains("minute");
    assertThat(json).contains("second");
  }

  @Test
  public void testDefaultGregorianCalendarDeserialization() {
    Gson gson = new GsonBuilder().create();
    String json = "{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}";
    GregorianCalendar cal = gson.fromJson(json, GregorianCalendar.class);
    assertThat(cal.get(Calendar.YEAR)).isEqualTo(2009);
    assertThat(cal.get(Calendar.MONTH)).isEqualTo(2);
    assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(11);
    assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(14);
    assertThat(cal.get(Calendar.MINUTE)).isEqualTo(29);
    assertThat(cal.get(Calendar.SECOND)).isEqualTo(23);
  }

  @Test
  public void testDateSerializationWithPattern() {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    assertThat(json).isEqualTo("\"2011-09-11\"");
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testDateDeserializationWithPattern() {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertThat(extracted.getYear()).isEqualTo(now.getYear());
    assertThat(extracted.getMonth()).isEqualTo(now.getMonth());
    assertThat(extracted.getDay()).isEqualTo(now.getDay());
  }

  @Test
  public void testDateSerializationWithPatternNotOverridenByTypeAdapter() {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder()
        .setDateFormat(pattern)
        .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
          @Override public Date deserialize(JsonElement json, Type typeOfT,
              JsonDeserializationContext context)
              throws JsonParseException {
            return new Date(1315806903103L);
          }
        })
        .create();

    Date now = new Date(1315806903103L);
    String json = gson.toJson(now);
    assertThat(json).isEqualTo("\"2011-09-11\"");
  }

  // http://code.google.com/p/google-gson/issues/detail?id=230
  @Test
  public void testDateSerializationInCollection() {
    Type listOfDates = new TypeToken<List<Date>>() {}.getType();
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    try {
      Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
      List<Date> dates = Arrays.asList(new Date(0));
      String json = gson.toJson(dates, listOfDates);
      assertThat(json).isEqualTo("[\"1970-01-01\"]");
      assertThat(gson.<List<Date>>fromJson("[\"1970-01-01\"]", listOfDates).get(0).getTime()).isEqualTo(0L);
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testJsonPrimitiveSerialization() {
    assertThat(gson.toJson(new JsonPrimitive(5), JsonElement.class)).isEqualTo("5");
    assertThat(gson.toJson(new JsonPrimitive(true), JsonElement.class)).isEqualTo("true");
    assertThat(gson.toJson(new JsonPrimitive("foo"), JsonElement.class)).isEqualTo("\"foo\"");
    assertThat(gson.toJson(new JsonPrimitive('a'), JsonElement.class)).isEqualTo("\"a\"");
  }

  @Test
  public void testJsonPrimitiveDeserialization() {
    assertThat(gson.fromJson("5", JsonElement.class)).isEqualTo(new JsonPrimitive(5));
    assertThat(gson.fromJson("5", JsonPrimitive.class)).isEqualTo(new JsonPrimitive(5));
    assertThat(gson.fromJson("true", JsonElement.class)).isEqualTo(new JsonPrimitive(true));
    assertThat(gson.fromJson("true", JsonPrimitive.class)).isEqualTo(new JsonPrimitive(true));
    assertThat(gson.fromJson("\"foo\"", JsonElement.class)).isEqualTo(new JsonPrimitive("foo"));
    assertThat(gson.fromJson("\"foo\"", JsonPrimitive.class)).isEqualTo(new JsonPrimitive("foo"));
    assertThat(gson.fromJson("\"a\"", JsonElement.class)).isEqualTo(new JsonPrimitive('a'));
    assertThat(gson.fromJson("\"a\"", JsonPrimitive.class)).isEqualTo(new JsonPrimitive('a'));
  }

  @Test
  public void testJsonNullSerialization() {
    assertThat(gson.toJson(JsonNull.INSTANCE, JsonElement.class)).isEqualTo("null");
    assertThat(gson.toJson(JsonNull.INSTANCE, JsonNull.class)).isEqualTo("null");
  }

  @Test
  public void testNullJsonElementSerialization() {
    assertThat(gson.toJson(null, JsonElement.class)).isEqualTo("null");
    assertThat(gson.toJson(null, JsonNull.class)).isEqualTo("null");
  }

  @Test
  public void testJsonArraySerialization() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));
    assertThat(gson.toJson(array, JsonElement.class)).isEqualTo("[1,2,3]");
  }

  @Test
  public void testJsonArrayDeserialization() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));
    array.add(new JsonPrimitive(3));

    String json = "[1,2,3]";
    assertThat(gson.fromJson(json, JsonElement.class)).isEqualTo(array);
    assertThat(gson.fromJson(json, JsonArray.class)).isEqualTo(array);
  }

  @Test
  public void testJsonObjectSerialization() {
    JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));
    assertThat(gson.toJson(object, JsonElement.class)).isEqualTo("{\"foo\":1,\"bar\":2}");
  }

  @Test
  public void testJsonObjectDeserialization() {
    JsonObject object = new JsonObject();
    object.add("foo", new JsonPrimitive(1));
    object.add("bar", new JsonPrimitive(2));

    String json = "{\"foo\":1,\"bar\":2}";
    JsonElement actual = gson.fromJson(json, JsonElement.class);
    assertThat(actual).isEqualTo(object);

    JsonObject actualObj = gson.fromJson(json, JsonObject.class);
    assertThat(actualObj).isEqualTo(object);
  }

  @Test
  public void testJsonNullDeserialization() {
    assertThat(gson.fromJson("null", JsonElement.class)).isEqualTo(JsonNull.INSTANCE);
    assertThat(gson.fromJson("null", JsonNull.class)).isEqualTo(JsonNull.INSTANCE);
  }

  @Test
  public void testJsonElementTypeMismatch() {
    try {
      gson.fromJson("\"abc\"", JsonObject.class);
      fail();
    } catch (JsonSyntaxException expected) {
      assertThat(expected.getMessage()).isEqualTo("Expected a com.google.gson.JsonObject but was com.google.gson.JsonPrimitive; at path $");
    }
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

  private static class ClassWithBigInteger {
    BigInteger value;
    ClassWithBigInteger(String value) {
      this.value = new BigInteger(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value + "}";
    }
  }

  @Test
  public void testPropertiesSerialization() {
    Properties props = new Properties();
    props.setProperty("foo", "bar");
    String json = gson.toJson(props);
    String expected = "{\"foo\":\"bar\"}";
    assertThat(json).isEqualTo(expected);
  }

  @Test
  public void testPropertiesDeserialization() {
    String json = "{foo:'bar'}";
    Properties props = gson.fromJson(json, Properties.class);
    assertThat(props.getProperty("foo")).isEqualTo("bar");
  }

  @Test
  public void testTreeSetSerialization() {
    TreeSet<String> treeSet = new TreeSet<>();
    treeSet.add("Value1");
    String json = gson.toJson(treeSet);
    assertThat(json).isEqualTo("[\"Value1\"]");
  }

  @Test
  public void testTreeSetDeserialization() {
    String json = "['Value1']";
    Type type = new TypeToken<TreeSet<String>>() {}.getType();
    TreeSet<String> treeSet = gson.fromJson(json, type);
    assertThat(treeSet).contains("Value1");
  }

  @Test
  public void testStringBuilderSerialization() {
    StringBuilder sb = new StringBuilder("abc");
    String json = gson.toJson(sb);
    assertThat(json).isEqualTo("\"abc\"");
  }

  @Test
  public void testStringBuilderDeserialization() {
    StringBuilder sb = gson.fromJson("'abc'", StringBuilder.class);
    assertThat(sb.toString()).isEqualTo("abc");
  }

  @Test
  public void testStringBufferSerialization() {
    StringBuffer sb = new StringBuffer("abc");
    String json = gson.toJson(sb);
    assertThat(json).isEqualTo("\"abc\"");
  }

  @Test
  public void testStringBufferDeserialization() {
    StringBuffer sb = gson.fromJson("'abc'", StringBuffer.class);
    assertThat(sb.toString()).isEqualTo("abc");
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
