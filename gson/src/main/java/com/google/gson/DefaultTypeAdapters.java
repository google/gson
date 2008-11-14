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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

  private static final DefaultDateTypeAdapter DATE_TYPE_ADAPTER =
    new DefaultDateTypeAdapter(DateFormat.getDateTimeInstance());
  @SuppressWarnings("unchecked")
  private static final EnumTypeAdapter ENUM_TYPE_ADAPTER = new EnumTypeAdapter();
  private static final UrlTypeAdapter URL_TYPE_ADAPTER = new UrlTypeAdapter();
  private static final UriTypeAdapter URI_TYPE_ADAPTER = new UriTypeAdapter();
  private static final LocaleTypeAdapter LOCALE_TYPE_ADAPTER = new LocaleTypeAdapter();
  private static final CollectionTypeAdapter COLLECTION_TYPE_ADAPTER = new CollectionTypeAdapter();
  private static final MapTypeAdapter MAP_TYPE_ADAPTER = new MapTypeAdapter();
  private static final BigDecimalTypeAdapter BIG_DECIMAL_TYPE_ADAPTER = new BigDecimalTypeAdapter();
  private static final BigIntegerTypeAdapter BIG_INTEGER_TYPE_ADAPTER = new BigIntegerTypeAdapter();

  private static final BooleanCreator BOOLEAN_CREATOR = new BooleanCreator();
  private static final ByteCreator BYTE_CREATOR = new ByteCreator();
  private static final CharacterCreator CHARACTER_CREATOR = new CharacterCreator();
  private static final DoubleCreator DOUBLE_CREATOR = new DoubleCreator();
  private static final FloatCreator FLOAT_CREATOR = new FloatCreator();
  private static final IntegerCreator INTEGER_CREATOR = new IntegerCreator();
  private static final LongCreator LONG_CREATOR = new LongCreator();
  private static final ShortCreator SHORT_CREATOR = new ShortCreator();
  private static final LinkedListCreator LINKED_LIST_CREATOR = new LinkedListCreator();
  private static final TreeSetCreator TREE_SET_CREATOR = new TreeSetCreator();

  // The constants DEFAULT_SERIALIZERS, DEFAULT_DESERIALIZERS, and DEFAULT_INSTANCE_CREATORS
  // must be defined after the constants for the type adapters. Otherwise, the type adapter
  // constants will appear as nulls.
  static final ParameterizedTypeHandlerMap<JsonSerializer<?>> DEFAULT_SERIALIZERS =
    getDefaultSerializers();
  static final ParameterizedTypeHandlerMap<JsonDeserializer<?>> DEFAULT_DESERIALIZERS =
    getDefaultDeserializers();
  static final ParameterizedTypeHandlerMap<InstanceCreator<?>> DEFAULT_INSTANCE_CREATORS =
    getDefaultInstanceCreators();

  private static ParameterizedTypeHandlerMap<JsonSerializer<?>> getDefaultSerializers() {
    ParameterizedTypeHandlerMap<JsonSerializer<?>> map =
      new ParameterizedTypeHandlerMap<JsonSerializer<?>>();

    map.register(Enum.class, wrapSerializer(ENUM_TYPE_ADAPTER));
    map.register(URL.class, wrapSerializer(URL_TYPE_ADAPTER));
    map.register(URI.class, wrapSerializer(URI_TYPE_ADAPTER));
    map.register(Locale.class, wrapSerializer(LOCALE_TYPE_ADAPTER));
    map.register(Collection.class, COLLECTION_TYPE_ADAPTER);
    map.register(Map.class, wrapSerializer(MAP_TYPE_ADAPTER));
    map.register(Date.class, wrapSerializer(DATE_TYPE_ADAPTER));
    map.register(BigDecimal.class, wrapSerializer(BIG_DECIMAL_TYPE_ADAPTER));
    map.register(BigInteger.class, wrapSerializer(BIG_INTEGER_TYPE_ADAPTER));
    map.makeUnmodifiable();
    return map;
  }

  private static ParameterizedTypeHandlerMap<JsonDeserializer<?>> getDefaultDeserializers() {
    ParameterizedTypeHandlerMap<JsonDeserializer<?>> map =
      new ParameterizedTypeHandlerMap<JsonDeserializer<?>>();
    map.register(Enum.class, wrapDeserializer(ENUM_TYPE_ADAPTER));
    map.register(URL.class, wrapDeserializer(URL_TYPE_ADAPTER));
    map.register(URI.class, wrapDeserializer(URI_TYPE_ADAPTER));
    map.register(Locale.class, wrapDeserializer(LOCALE_TYPE_ADAPTER));
    map.register(Collection.class, wrapDeserializer(COLLECTION_TYPE_ADAPTER));
    map.register(Map.class, wrapDeserializer(MAP_TYPE_ADAPTER));
    map.register(Date.class, wrapDeserializer(DATE_TYPE_ADAPTER));
    map.register(BigDecimal.class, wrapDeserializer(BIG_DECIMAL_TYPE_ADAPTER));
    map.register(BigInteger.class, wrapDeserializer(BIG_INTEGER_TYPE_ADAPTER));
    map.makeUnmodifiable();
    return map;
  }

  private static ParameterizedTypeHandlerMap<InstanceCreator<?>> getDefaultInstanceCreators() {
    ParameterizedTypeHandlerMap<InstanceCreator<?>> map =
      new ParameterizedTypeHandlerMap<InstanceCreator<?>>();
    map.register(Enum.class, ENUM_TYPE_ADAPTER);
    map.register(URL.class, URL_TYPE_ADAPTER);
    map.register(Locale.class, LOCALE_TYPE_ADAPTER);
    map.register(Collection.class, COLLECTION_TYPE_ADAPTER);
    map.register(Map.class, MAP_TYPE_ADAPTER);
    map.register(BigDecimal.class, BIG_DECIMAL_TYPE_ADAPTER);
    map.register(BigInteger.class, BIG_INTEGER_TYPE_ADAPTER);

    // Add primitive instance creators
    map.register(Boolean.class, BOOLEAN_CREATOR);
    map.register(boolean.class, BOOLEAN_CREATOR);
    map.register(Byte.class, BYTE_CREATOR);
    map.register(byte.class, BYTE_CREATOR);
    map.register(Character.class, CHARACTER_CREATOR);
    map.register(char.class, CHARACTER_CREATOR);
    map.register(Double.class, DOUBLE_CREATOR);
    map.register(double.class, DOUBLE_CREATOR);
    map.register(Float.class, FLOAT_CREATOR);
    map.register(float.class, FLOAT_CREATOR);
    map.register(Integer.class, INTEGER_CREATOR);
    map.register(int.class, INTEGER_CREATOR);
    map.register(Long.class, LONG_CREATOR);
    map.register(long.class, LONG_CREATOR);
    map.register(Short.class, SHORT_CREATOR);
    map.register(short.class, SHORT_CREATOR);

    map.register(Collection.class, LINKED_LIST_CREATOR);
    map.register(List.class, LINKED_LIST_CREATOR);
    map.register(Queue.class, LINKED_LIST_CREATOR);

    map.register(Set.class, TREE_SET_CREATOR);
    map.register(SortedSet.class, TREE_SET_CREATOR);
    map.makeUnmodifiable();
    return map;
  }

  @SuppressWarnings("unchecked")
  private static JsonSerializer<?> wrapSerializer(JsonSerializer<?> serializer) {
    return new JsonSerializerExceptionWrapper(serializer);
  }

  @SuppressWarnings("unchecked")
  private static JsonDeserializer<?> wrapDeserializer(JsonDeserializer<?> deserializer) {
    return new JsonDeserializerExceptionWrapper(deserializer);
  }

  static class DefaultDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    private final DateFormat format;

    public DefaultDateTypeAdapter(String datePattern) {
      this.format = new SimpleDateFormat(datePattern);
    }
    
    DefaultDateTypeAdapter(DateFormat format) {
      this.format = format;
    }

    public DefaultDateTypeAdapter(int style) {
      this.format = DateFormat.getDateInstance(style);
    }

    public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
      this.format = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
    }

    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
      String dateFormatAsString = format.format(src);
      return new JsonPrimitive(dateFormatAsString);
    }

    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      if (!(json instanceof JsonPrimitive)) {
        throw new JsonParseException("The date should be a string value");
      }

      try {
        return format.parse(json.getAsString());
      } catch (ParseException e) {
        throw new JsonParseException(e);
      }
    }
    
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(DefaultDateTypeAdapter.class.getSimpleName());
      sb.append('(').append(format.getClass().getSimpleName()).append(')');
      return sb.toString();
    }
  }

  @SuppressWarnings("unchecked")
  private static class EnumTypeAdapter<T extends Enum<T>> implements JsonSerializer<T>,
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
    @Override
    public String toString() {
      return EnumTypeAdapter.class.getSimpleName();
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
    @Override
    public String toString() {
      return UrlTypeAdapter.class.getSimpleName();
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
    @Override
    public String toString() {
      return UriTypeAdapter.class.getSimpleName();
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
    @Override
    public String toString() {
      return LocaleTypeAdapter.class.getSimpleName();
    }
  }

  @SuppressWarnings({ "unchecked" })
  private static class CollectionTypeAdapter implements JsonSerializer<Collection>, 
  JsonDeserializer<Collection>, InstanceCreator<Collection> {

    public JsonElement serialize(Collection src, Type typeOfSrc, JsonSerializationContext context) {
      if (src == null) {
        return JsonNull.INSTANCE;
      }
      JsonArray array = new JsonArray();
      Type childGenericType = null;
      if (typeOfSrc instanceof ParameterizedType) {
        childGenericType = new TypeInfoCollection(typeOfSrc).getElementType();        
      }
      for (Object child : src) {
        Type childType = (childGenericType == null) ? 
            childType = child.getClass() : childGenericType;
        JsonElement element = context.serialize(child, childType);
        array.add(element);
      }
      return array;
    }

    public Collection deserialize(JsonElement json, Type typeOfT, 
        JsonDeserializationContext context) throws JsonParseException {
      if (json.isJsonNull()) {
        return null;
      }
      // Use ObjectConstructor to create instance instead of hard-coding a specific type. 
      // This handles cases where users are using their own subclass of Collection.
      Collection collection = constructCollectionType(typeOfT, context);
      Type childType = new TypeInfoCollection(typeOfT).getElementType();
      for (JsonElement childElement : json.getAsJsonArray()) {
        if (childElement == null || childElement.isJsonNull()) {
          collection.add(null);
        } else {
          Object value = context.deserialize(childElement, childType);
          collection.add(value);
        }
      }
      return collection;
    }

    private Collection constructCollectionType(Type collectionType, 
        JsonDeserializationContext context) {      
      JsonDeserializationContextDefault contextImpl = (JsonDeserializationContextDefault) context;
      ObjectConstructor objectConstructor = contextImpl.getObjectConstructor();
      return (Collection) objectConstructor.construct(collectionType);
    }

    public Collection createInstance(Type type) {
      return new LinkedList();
    }    
  }
  
  @SuppressWarnings("unchecked")
  static class MapTypeAdapter implements JsonSerializer<Map>, JsonDeserializer<Map>,
      InstanceCreator<Map> {
    
    public JsonElement serialize(Map src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject map = new JsonObject();
      Type childGenericType = null;
      if (typeOfSrc instanceof ParameterizedType) {
        childGenericType = new TypeInfoMap(typeOfSrc).getValueType();        
      }
      for (Iterator iterator = src.entrySet().iterator(); iterator.hasNext(); ) {
        Map.Entry entry = (Map.Entry) iterator.next();
        Object value = entry.getValue();
        Type childType = (childGenericType == null) ? 
            childType = value.getClass() : childGenericType;
        JsonElement valueElement = context.serialize(value, childType);
        map.add(entry.getKey().toString(), valueElement);
      }
      return map;
    }

    public Map deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      // Use ObjectConstructor to create instance instead of hard-coding a specific type. 
      // This handles cases where users are using their own subclass of Map.
      Map<String, Object> map = constructMapType(typeOfT, context);
      Type childType = new TypeInfoMap(typeOfT).getValueType();
      for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
        Object value = context.deserialize(entry.getValue(), childType);
        map.put(entry.getKey(), value);
      }
      return map;
    }

    private Map constructMapType(Type mapType, JsonDeserializationContext context) {      
      JsonDeserializationContextDefault contextImpl = (JsonDeserializationContextDefault) context;
      ObjectConstructor objectConstructor = contextImpl.getObjectConstructor();
      return (Map) objectConstructor.construct(mapType);
    }
    
    public Map createInstance(Type type) {
      return new LinkedHashMap();
    }
    
    @Override
    public String toString() {
      return MapTypeAdapter.class.getSimpleName();
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
    @Override
    public String toString() {
      return BigDecimalTypeAdapter.class.getSimpleName();
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
    @Override
    public String toString() {
      return BigIntegerTypeAdapter.class.getSimpleName();
    }
  }

  private static class LongCreator implements InstanceCreator<Long> {
    public Long createInstance(Type type) {
      return new Long(0L);
    }
    @Override
    public String toString() {
      return LongCreator.class.getSimpleName();
    }
  }

  private static class IntegerCreator implements InstanceCreator<Integer> {
    public Integer createInstance(Type type) {
      return new Integer(0);
    }
    @Override
    public String toString() {
      return IntegerCreator.class.getSimpleName();
    }
  }

  private static class ShortCreator implements InstanceCreator<Short> {
    public Short createInstance(Type type) {
      return new Short((short) 0);
    }
    @Override
    public String toString() {
      return ShortCreator.class.getSimpleName();
    }
  }

  private static class ByteCreator implements InstanceCreator<Byte> {
    public Byte createInstance(Type type) {
      return new Byte((byte) 0);
    }
    @Override
    public String toString() {
      return ByteCreator.class.getSimpleName();
    }
  }

  private static class FloatCreator implements InstanceCreator<Float> {
    public Float createInstance(Type type) {
      return new Float(0F);
    }
    @Override
    public String toString() {
      return FloatCreator.class.getSimpleName();
    }
  }

  private static class DoubleCreator implements InstanceCreator<Double> {
    public Double createInstance(Type type) {
      return new Double(0D);
    }
    @Override
    public String toString() {
      return DoubleCreator.class.getSimpleName();
    }
  }

  private static class CharacterCreator implements InstanceCreator<Character> {
    public Character createInstance(Type type) {
      return new Character((char) 0);
    }
    @Override
    public String toString() {
      return CharacterCreator.class.getSimpleName();
    }
  }

  private static class BooleanCreator implements InstanceCreator<Boolean> {
    public Boolean createInstance(Type type) {
      return new Boolean(false);
    }
    @Override
    public String toString() {
      return BooleanCreator.class.getSimpleName();
    }
  }

  private static class LinkedListCreator implements InstanceCreator<LinkedList<?>> {
    public LinkedList<?> createInstance(Type type) {
      return new LinkedList<Object>();
    }
    @Override
    public String toString() {
      return LinkedListCreator.class.getSimpleName();
    }
  }

  private static class TreeSetCreator implements InstanceCreator<TreeSet<?>> {
    public TreeSet<?> createInstance(Type type) {
      return new TreeSet<Object>();
    }
    @Override
    public String toString() {
      return TreeSetCreator.class.getSimpleName();
    }
  }
}
