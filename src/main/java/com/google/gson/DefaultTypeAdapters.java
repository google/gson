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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * List of all the default type adapters ({@link JsonSerializer}s, {@link JsonDeserializer}s,
 * and {@link InstanceCreator}s.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultTypeAdapters {

  @SuppressWarnings("unchecked")
  static ParameterizedTypeHandlerMap<JsonSerializer<?>> getDefaultSerializers() {
    ParameterizedTypeHandlerMap<JsonSerializer<?>> map =
      new ParameterizedTypeHandlerMap<JsonSerializer<?>>();
    map.register(Enum.class, new EnumTypeAdapter());
    map.register(URL.class, new UrlTypeAdapter());
    map.register(URI.class, new UriTypeAdapter());
    map.register(Locale.class, new LocaleTypeAdapter());
    map.register(Map.class, new MapTypeAdapter());
    map.register(Date.class, DefaultDateTypeAdapter.DEFAULT_TYPE_ADAPTER);
    map.register(BigDecimal.class, new BigDecimalTypeAdapter());
    map.register(BigInteger.class, new BigIntegerTypeAdapter());
    return map;
  }

  @SuppressWarnings("unchecked")
  static ParameterizedTypeHandlerMap<JsonDeserializer<?>> getDefaultDeserializers() {
    ParameterizedTypeHandlerMap<JsonDeserializer<?>> map =
      new ParameterizedTypeHandlerMap<JsonDeserializer<?>>();
    map.register(Enum.class, new EnumTypeAdapter());
    map.register(URL.class, new UrlTypeAdapter());
    map.register(URI.class, new UriTypeAdapter());
    map.register(Locale.class, new LocaleTypeAdapter());
    map.register(Map.class, new MapTypeAdapter());
    map.register(Date.class, DefaultDateTypeAdapter.DEFAULT_TYPE_ADAPTER);
    map.register(BigDecimal.class, new BigDecimalTypeAdapter());
    map.register(BigInteger.class, new BigIntegerTypeAdapter());
    return map;
  }

  @SuppressWarnings("unchecked")
  static ParameterizedTypeHandlerMap<InstanceCreator<?>> getDefaultInstanceCreators() {
    ParameterizedTypeHandlerMap<InstanceCreator<?>> map =
      new ParameterizedTypeHandlerMap<InstanceCreator<?>>();
    map.register(Enum.class, new EnumTypeAdapter());
    map.register(URL.class, new UrlTypeAdapter());
    map.register(Locale.class, new LocaleTypeAdapter());
    map.register(Map.class, new MapTypeAdapter());
    map.register(BigDecimal.class, new BigDecimalTypeAdapter());
    map.register(BigInteger.class, new BigIntegerTypeAdapter());

    // Add primitive instance creators
    map.register(Boolean.class, new BooleanCreator());
    map.register(boolean.class, new BooleanCreator());
    map.register(Byte.class, new ByteCreator());
    map.register(byte.class, new ByteCreator());
    map.register(Character.class, new CharacterCreator());
    map.register(char.class, new CharacterCreator());
    map.register(Double.class, new DoubleCreator());
    map.register(double.class, new DoubleCreator());
    map.register(Float.class, new FloatCreator());
    map.register(float.class, new FloatCreator());
    map.register(Integer.class, new IntegerCreator());
    map.register(int.class, new IntegerCreator());
    map.register(Long.class, new LongCreator());
    map.register(long.class, new LongCreator());
    map.register(Short.class, new ShortCreator());
    map.register(short.class, new ShortCreator());

    // Add Collection instance creators
    InstanceCreator<LinkedList<?>> linkedListCreator = new LinkedListCreator();
    map.register(Collection.class, linkedListCreator);
    map.register(List.class, linkedListCreator);
    map.register(Queue.class, linkedListCreator);

    // Add Set instance creators
    InstanceCreator<TreeSet<?>> treeSetCreator = new TreeSetCreator();
    map.register(Set.class, treeSetCreator);
    map.register(SortedSet.class, treeSetCreator);
    return map;
  }

  @SuppressWarnings("unchecked")
  private static class EnumTypeAdapter<T extends Enum> implements JsonSerializer<T>,
      JsonDeserializer<T>, InstanceCreator<Enum<?>> {
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.name());
    }
    @SuppressWarnings("cast")
    public T deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return (T) Enum.valueOf((Class<T>)classOfT, json.getAsString());
    }
    public Enum<?> createInstance(Type type) {
      Class<Enum<?>> enumClass = (Class<Enum<?>>) type;
      try {
        Method valuesMethod = enumClass.getMethod("values");
        Enum<?>[] enums = (Enum<?>[]) valuesMethod.invoke(null);
        return enums[0];
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class UrlTypeAdapter implements JsonSerializer<URL>, JsonDeserializer<URL>,
      InstanceCreator<URL> {
    public JsonElement serialize(URL src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toExternalForm());
    }
    public URL deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      try {
        return new URL(json.getAsString());
      } catch (MalformedURLException e) {
        throw new JsonParseException(e);
      }
    }
    public URL createInstance(Type type) {
      try {
        return new URL("http://google.com/");
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class UriTypeAdapter implements JsonSerializer<URI>, JsonDeserializer<URI> {
    public JsonElement serialize(URI src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toASCIIString());
    }
    public URI deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException {
      try {
        return new URI(json.getAsString());
      } catch (URISyntaxException e) {
        throw new JsonParseException(e);
      }
    }
  }

  private static class LocaleTypeAdapter implements JsonSerializer<Locale>,
      JsonDeserializer<Locale>, InstanceCreator<Locale> {
    public JsonElement serialize(Locale src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }
    public Locale deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      String locale = json.getAsString();
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
    public Locale createInstance(Type type) {
      return new Locale("en_US");
    }
  }

  @SuppressWarnings("unchecked")
  private static class MapTypeAdapter implements JsonSerializer<Map>, JsonDeserializer<Map>,
      InstanceCreator<Map> {
    public JsonElement serialize(Map src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject map = new JsonObject();
      Type childType = new TypeInfoMap(typeOfSrc).getValueType();
      for (Iterator iterator = src.entrySet().iterator(); iterator.hasNext(); ) {
        Map.Entry entry = (Map.Entry) iterator.next();
        JsonElement valueElement = context.serialize(entry.getValue(), childType);
        map.add(entry.getKey().toString(), valueElement);
      }
      return map;
    }
    public Map deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      // Using linked hash map to preserve order in which elements are entered
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      Type childType = new TypeInfoMap(typeOfT).getValueType();
      for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
        Object value = context.deserialize(entry.getValue(), childType);
        map.put(entry.getKey(), value);
      }
      return map;
    }
    public Map createInstance(Type type) {
      return new LinkedHashMap();
    }
  }

  private static class BigDecimalTypeAdapter implements JsonSerializer<BigDecimal>,
      JsonDeserializer<BigDecimal>, InstanceCreator<BigDecimal> {

    public JsonElement serialize(BigDecimal src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public BigDecimal deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return json.getAsBigDecimal();
    }

    public BigDecimal createInstance(Type type) {
      return new BigDecimal(0);
    }
  }

  private static class BigIntegerTypeAdapter implements JsonSerializer<BigInteger>,
      JsonDeserializer<BigInteger>, InstanceCreator<BigInteger> {

    public JsonElement serialize(BigInteger src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public BigInteger deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return json.getAsBigInteger();
    }

    public BigInteger createInstance(Type type) {
      return new BigInteger("0");
    }
  }

  private static class LongCreator implements InstanceCreator<Long> {
    public Long createInstance(Type type) {
      return new Long(0L);
    }
  }

  private static class IntegerCreator implements InstanceCreator<Integer> {
    public Integer createInstance(Type type) {
      return new Integer(0);
    }
  }

  private static class ShortCreator implements InstanceCreator<Short> {
    public Short createInstance(Type type) {
      return new Short((short) 0);
    }
  }

  private static class ByteCreator implements InstanceCreator<Byte> {
    public Byte createInstance(Type type) {
      return new Byte((byte) 0);
    }
  }

  private static class FloatCreator implements InstanceCreator<Float> {
    public Float createInstance(Type type) {
      return new Float(0F);
    }
  }

  private static class DoubleCreator implements InstanceCreator<Double> {
    public Double createInstance(Type type) {
      return new Double(0D);
    }
  }

  private static class CharacterCreator implements InstanceCreator<Character> {
    public Character createInstance(Type type) {
      return new Character((char) 0);
    }
  }

  private static class BooleanCreator implements InstanceCreator<Boolean> {
    public Boolean createInstance(Type type) {
      return new Boolean(false);
    }
  }

  private static class LinkedListCreator implements InstanceCreator<LinkedList<?>> {
    public LinkedList<?> createInstance(Type type) {
      return new LinkedList<Object>();
    }
  }

  private static class TreeSetCreator implements InstanceCreator<TreeSet<?>> {
    public TreeSet<?> createInstance(Type type) {
      return new TreeSet<Object>();
    }
  }
}
