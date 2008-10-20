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
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Functional test for Json serialization and deserialization for common classes for which default
 * support is provided in Gson.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class DefaultTypeAdaptersTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testUrlSerialization() throws Exception {
    String urlValue = "http://google.com/";
    URL url = new URL(urlValue);
    assertEquals('"' + urlValue + '"', gson.toJson(url));
  }

  public void testUrlDeserialization() {
    String urlValue = "http://google.com/";
    String json = '"' + urlValue + '"';
    URL target = gson.fromJson(json, URL.class);
    assertEquals(urlValue, target.toExternalForm());
  }

  public void testUriSerialization() throws Exception {
    String uriValue = "http://google.com/";
    URI uri = new URI(uriValue);
    assertEquals('"' + uriValue + '"', gson.toJson(uri));
  }

  public void testUriDeserialization() {
    String uriValue = "http://google.com/";
    String json = '"' + uriValue + '"';
    URI target = gson.fromJson(json, URI.class);
    assertEquals(uriValue, target.toASCIIString());
  }

  public void testLocaleSerializationWithLanguage() {
    Locale target = new Locale("en");
    assertEquals("\"en\"", gson.toJson(target));
  }

  public void testLocaleDeserializationWithLanguage() {
    String json = "\"en\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("en", locale.getLanguage());
  }

  public void testLocaleSerializationWithLanguageCountry() {
    Locale target = Locale.CANADA_FRENCH;
    assertEquals("\"fr_CA\"", gson.toJson(target));
  }

  public void testLocaleDeserializationWithLanguageCountry() {
    String json = "\"fr_CA\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals(Locale.CANADA_FRENCH, locale);
  }

  public void testLocaleSerializationWithLanguageCountryVariant() {
    Locale target = new Locale("de", "DE", "EURO");
    String json = gson.toJson(target);
    assertEquals("\"de_DE_EURO\"", json);
  }

  public void testLocaleDeserializationWithLanguageCountryVariant() {
    String json = "\"de_DE_EURO\"";
    Locale locale = gson.fromJson(json, Locale.class);
    assertEquals("de", locale.getLanguage());
    assertEquals("DE", locale.getCountry());
    assertEquals("EURO", locale.getVariant());
  }

  public void testMapSerialization() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    map.put("a", 1);
    map.put("b", 2);
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);
    assertTrue(json.contains("\"a\":1"));
    assertTrue(json.contains("\"b\":2"));
  }

  public void testMapDeserialization() {
    String json = "{\"a\":1,\"b\":2}";
    Type typeOfMap = new TypeToken<Map<String,Integer>>(){}.getType();
    Map<String, Integer> target = gson.fromJson(json, typeOfMap);
    assertEquals(1, target.get("a").intValue());
    assertEquals(2, target.get("b").intValue());
  }

  public void testMapSerializationEmpty() {
    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
    Type typeOfMap = new TypeToken<Map<String, Integer>>() {}.getType();
    String json = gson.toJson(map, typeOfMap);
    assertEquals("{}", json);
  }

  public void testBigDecimalFieldSerialization() {
    ClassWithBigDecimal target = new ClassWithBigDecimal("-122.01e-21");
    String json = gson.toJson(target);
    String actual = json.substring(json.indexOf(':') + 1, json.indexOf('}'));
    assertEquals(target.value, new BigDecimal(actual));
  }

  public void testBigDecimalFieldDeserialization() {
    ClassWithBigDecimal expected = new ClassWithBigDecimal("-122.01e-21");
    String json = expected.getExpectedJson();
    ClassWithBigDecimal actual = gson.fromJson(json, ClassWithBigDecimal.class);
    assertEquals(expected.value, actual.value);
  }

  public void testBadValueForBigDecimalDeserialization() {
    try {
      gson.fromJson("{\"value\"=1.5e-1.0031}", ClassWithBigDecimal.class);
      fail("Exponent of a BigDecimal must be an integer value.");
    } catch (JsonParseException expected) { }
  }

  public void testBigIntegerFieldSerialization() {
    ClassWithBigInteger target = new ClassWithBigInteger("23232323215323234234324324324324324324");
    String json = gson.toJson(target);
    assertEquals(target.getExpectedJson(), json);
  }

  public void testBigIntegerFieldDeserialization() {
    ClassWithBigInteger expected = new ClassWithBigInteger("879697697697697697697697697697697697");
    String json = expected.getExpectedJson();
    ClassWithBigInteger actual = gson.fromJson(json, ClassWithBigInteger.class);
    assertEquals(expected.value, actual.value);
  }

  public void testSetSerialization() throws Exception {
    Gson gson = new Gson();
    HashSet<String> s = new HashSet<String>();
    s.add("blah");
    String json = gson.toJson(s);
    assertEquals("[\"blah\"]", json);

    json = gson.toJson(s, Set.class);
    assertEquals("[\"blah\"]", json);
  }

  public void testDefaultDateSerialization() {
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + DateFormat.getDateTimeInstance().format(now) + "\"", json);
  }

  public void testDefaultDateDeserialization() {
    Date date = new Date();
    String json = gson.toJson(date);
    Date extracted = gson.fromJson(json, Date.class);
    // Using comparison of string forms since the extracted date has lost the millisecond portion.
    assertEquals(date.toString(), extracted.toString());    
  }
  
  public void testDefaultDateSerializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + DateFormat.getDateTimeInstance().format(now) + "\"", json);
  }

  public void testDefaultDateDeserializationUsingBuilder() throws Exception {
    Gson gson = new GsonBuilder().create();
    Date now = new Date();
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.toString(), extracted.toString());    
  }

  public void testDateSerializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    DateFormat formatter = new SimpleDateFormat(pattern);
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date();
    String json = gson.toJson(now);
    assertEquals("\"" + formatter.format(now) + "\"", json);
  }
  
  @SuppressWarnings("deprecation")
  public void testDateDeserializationWithPattern() throws Exception {
    String pattern = "yyyy-MM-dd";
    Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL).setDateFormat(pattern).create();
    Date now = new Date();
    String json = gson.toJson(now);
    Date extracted = gson.fromJson(json, Date.class);
    assertEquals(now.getYear(), extracted.getYear());    
    assertEquals(now.getMonth(), extracted.getMonth());    
    assertEquals(now.getDay(), extracted.getDay());    
  }
  
  private static class ClassWithBigDecimal {
    BigDecimal value;
    ClassWithBigDecimal() { }
    ClassWithBigDecimal(String value) {
      this.value = new BigDecimal(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value.toEngineeringString() + "}";
    }
  }

  private static class ClassWithBigInteger {
    BigInteger value;
    ClassWithBigInteger() { }
    ClassWithBigInteger(String value) {
      this.value = new BigInteger(value);
    }
    String getExpectedJson() {
      return "{\"value\":" + value + "}";
    }
  }
}
