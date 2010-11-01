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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;

/**
 * List of all the default type adapters ({@link JsonSerializer}s, {@link JsonDeserializer}s,
 * and {@link InstanceCreator}s.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultTypeAdapters {

  private static final DefaultDateTypeAdapter DATE_TYPE_ADAPTER = new DefaultDateTypeAdapter();
  private static final DefaultJavaSqlDateTypeAdapter JAVA_SQL_DATE_TYPE_ADAPTER =
    new DefaultJavaSqlDateTypeAdapter();
  private static final DefaultTimeTypeAdapter TIME_TYPE_ADAPTER =
    new DefaultTimeTypeAdapter();
  private static final DefaultTimestampDeserializer TIMESTAMP_DESERIALIZER =
    new DefaultTimestampDeserializer();

  @SuppressWarnings({ "rawtypes" })
  private static final EnumTypeAdapter ENUM_TYPE_ADAPTER = new EnumTypeAdapter();
  private static final UrlTypeAdapter URL_TYPE_ADAPTER = new UrlTypeAdapter();
  private static final UriTypeAdapter URI_TYPE_ADAPTER = new UriTypeAdapter();
  private static final UuidTypeAdapter UUUID_TYPE_ADAPTER = new UuidTypeAdapter();
  private static final LocaleTypeAdapter LOCALE_TYPE_ADAPTER = new LocaleTypeAdapter();
  private static final CollectionTypeAdapter COLLECTION_TYPE_ADAPTER = new CollectionTypeAdapter();
  private static final MapTypeAdapter MAP_TYPE_ADAPTER = new MapTypeAdapter();
  private static final BigDecimalTypeAdapter BIG_DECIMAL_TYPE_ADAPTER = new BigDecimalTypeAdapter();
  private static final BigIntegerTypeAdapter BIG_INTEGER_TYPE_ADAPTER = new BigIntegerTypeAdapter();

  private static final BooleanTypeAdapter BOOLEAN_TYPE_ADAPTER = new BooleanTypeAdapter();
  private static final ByteTypeAdapter BYTE_TYPE_ADAPTER = new ByteTypeAdapter();
  private static final CharacterTypeAdapter CHARACTER_TYPE_ADAPTER = new CharacterTypeAdapter();
  private static final DoubleDeserializer DOUBLE_TYPE_ADAPTER = new DoubleDeserializer();
  private static final FloatDeserializer FLOAT_TYPE_ADAPTER = new FloatDeserializer();
  private static final IntegerTypeAdapter INTEGER_TYPE_ADAPTER = new IntegerTypeAdapter();
  private static final LongDeserializer LONG_DESERIALIZER = new LongDeserializer();
  private static final NumberTypeAdapter NUMBER_TYPE_ADAPTER = new NumberTypeAdapter();
  private static final ShortTypeAdapter SHORT_TYPE_ADAPTER = new ShortTypeAdapter();
  private static final StringTypeAdapter STRING_TYPE_ADAPTER = new StringTypeAdapter();

  private static final PropertiesCreator PROPERTIES_CREATOR = new PropertiesCreator();
  private static final TreeSetCreator TREE_SET_CREATOR = new TreeSetCreator();
  private static final HashSetCreator HASH_SET_CREATOR = new HashSetCreator();
  private static final GregorianCalendarTypeAdapter GREGORIAN_CALENDAR_TYPE_ADAPTER = 
    new GregorianCalendarTypeAdapter();

  // The constants DEFAULT_SERIALIZERS, DEFAULT_DESERIALIZERS, and DEFAULT_INSTANCE_CREATORS
  // must be defined after the constants for the type adapters. Otherwise, the type adapter
  // constants will appear as nulls.
  private static final ParameterizedTypeHandlerMap<JsonSerializer<?>> DEFAULT_SERIALIZERS =
      createDefaultSerializers();
  private static final ParameterizedTypeHandlerMap<JsonDeserializer<?>> DEFAULT_DESERIALIZERS =
      createDefaultDeserializers();
  private static final ParameterizedTypeHandlerMap<InstanceCreator<?>> DEFAULT_INSTANCE_CREATORS =
      createDefaultInstanceCreators();

  private static ParameterizedTypeHandlerMap<JsonSerializer<?>> createDefaultSerializers() {
    ParameterizedTypeHandlerMap<JsonSerializer<?>> map =
        new ParameterizedTypeHandlerMap<JsonSerializer<?>>();

    map.registerForTypeHierarchy(Enum.class, ENUM_TYPE_ADAPTER);
    map.register(URL.class, URL_TYPE_ADAPTER);
    map.register(URI.class, URI_TYPE_ADAPTER);
    map.register(UUID.class, UUUID_TYPE_ADAPTER);
    map.register(Locale.class, LOCALE_TYPE_ADAPTER);
    map.registerForTypeHierarchy(Collection.class, COLLECTION_TYPE_ADAPTER);
    map.registerForTypeHierarchy(Map.class, MAP_TYPE_ADAPTER);
    map.register(Date.class, DATE_TYPE_ADAPTER);
    map.register(java.sql.Date.class, JAVA_SQL_DATE_TYPE_ADAPTER);
    map.register(Timestamp.class, DATE_TYPE_ADAPTER);
    map.register(Time.class, TIME_TYPE_ADAPTER);
    map.register(Calendar.class, GREGORIAN_CALENDAR_TYPE_ADAPTER);
    map.register(GregorianCalendar.class, GREGORIAN_CALENDAR_TYPE_ADAPTER);
    map.register(BigDecimal.class, BIG_DECIMAL_TYPE_ADAPTER);
    map.register(BigInteger.class, BIG_INTEGER_TYPE_ADAPTER);
    
    // Add primitive serializers
    map.register(Boolean.class, BOOLEAN_TYPE_ADAPTER);
    map.register(boolean.class, BOOLEAN_TYPE_ADAPTER);
    map.register(Byte.class, BYTE_TYPE_ADAPTER);
    map.register(byte.class, BYTE_TYPE_ADAPTER);
    map.register(Character.class, CHARACTER_TYPE_ADAPTER);
    map.register(char.class, CHARACTER_TYPE_ADAPTER);
    map.register(Integer.class, INTEGER_TYPE_ADAPTER);
    map.register(int.class, INTEGER_TYPE_ADAPTER);
    map.register(Number.class, NUMBER_TYPE_ADAPTER);
    map.register(Short.class, SHORT_TYPE_ADAPTER);
    map.register(short.class, SHORT_TYPE_ADAPTER);
    map.register(String.class, STRING_TYPE_ADAPTER);

    map.makeUnmodifiable();
    return map;
  }

  private static ParameterizedTypeHandlerMap<JsonDeserializer<?>> createDefaultDeserializers() {
    ParameterizedTypeHandlerMap<JsonDeserializer<?>> map =
        new ParameterizedTypeHandlerMap<JsonDeserializer<?>>();
    map.registerForTypeHierarchy(Enum.class, wrapDeserializer(ENUM_TYPE_ADAPTER));
    map.register(URL.class, wrapDeserializer(URL_TYPE_ADAPTER));
    map.register(URI.class, wrapDeserializer(URI_TYPE_ADAPTER));
    map.register(UUID.class, wrapDeserializer(UUUID_TYPE_ADAPTER));
    map.register(Locale.class, wrapDeserializer(LOCALE_TYPE_ADAPTER));
    map.registerForTypeHierarchy(Collection.class, wrapDeserializer(COLLECTION_TYPE_ADAPTER));
    map.registerForTypeHierarchy(Map.class, wrapDeserializer(MAP_TYPE_ADAPTER));
    map.register(Date.class, wrapDeserializer(DATE_TYPE_ADAPTER));
    map.register(java.sql.Date.class, wrapDeserializer(JAVA_SQL_DATE_TYPE_ADAPTER));
    map.register(Timestamp.class, wrapDeserializer(TIMESTAMP_DESERIALIZER));
    map.register(Time.class, wrapDeserializer(TIME_TYPE_ADAPTER));
    map.register(Calendar.class, GREGORIAN_CALENDAR_TYPE_ADAPTER);
    map.register(GregorianCalendar.class, GREGORIAN_CALENDAR_TYPE_ADAPTER);
    map.register(BigDecimal.class, wrapDeserializer(BIG_DECIMAL_TYPE_ADAPTER));
    map.register(BigInteger.class, wrapDeserializer(BIG_INTEGER_TYPE_ADAPTER));
    
    // Add primitive deserializers
    map.register(Boolean.class, wrapDeserializer(BOOLEAN_TYPE_ADAPTER));
    map.register(boolean.class, wrapDeserializer(BOOLEAN_TYPE_ADAPTER));
    map.register(Byte.class, wrapDeserializer(BYTE_TYPE_ADAPTER));
    map.register(byte.class, wrapDeserializer(BYTE_TYPE_ADAPTER));
    map.register(Character.class, wrapDeserializer(CHARACTER_TYPE_ADAPTER));
    map.register(char.class, wrapDeserializer(CHARACTER_TYPE_ADAPTER));
    map.register(Double.class, wrapDeserializer(DOUBLE_TYPE_ADAPTER));
    map.register(double.class, wrapDeserializer(DOUBLE_TYPE_ADAPTER));
    map.register(Float.class, wrapDeserializer(FLOAT_TYPE_ADAPTER));
    map.register(float.class, wrapDeserializer(FLOAT_TYPE_ADAPTER));
    map.register(Integer.class, wrapDeserializer(INTEGER_TYPE_ADAPTER));
    map.register(int.class, wrapDeserializer(INTEGER_TYPE_ADAPTER));
    map.register(Long.class, wrapDeserializer(LONG_DESERIALIZER));
    map.register(long.class, wrapDeserializer(LONG_DESERIALIZER));
    map.register(Number.class, wrapDeserializer(NUMBER_TYPE_ADAPTER));
    map.register(Short.class, wrapDeserializer(SHORT_TYPE_ADAPTER));
    map.register(short.class, wrapDeserializer(SHORT_TYPE_ADAPTER));
    map.register(String.class, wrapDeserializer(STRING_TYPE_ADAPTER));

    map.makeUnmodifiable();
    return map;
  }

  private static ParameterizedTypeHandlerMap<InstanceCreator<?>> createDefaultInstanceCreators() {
    ParameterizedTypeHandlerMap<InstanceCreator<?>> map =
        new ParameterizedTypeHandlerMap<InstanceCreator<?>>();
    map.registerForTypeHierarchy(Map.class, MAP_TYPE_ADAPTER);

    // Add Collection type instance creators
    map.registerForTypeHierarchy(Collection.class, COLLECTION_TYPE_ADAPTER);

    map.registerForTypeHierarchy(Set.class, HASH_SET_CREATOR);
    map.registerForTypeHierarchy(SortedSet.class, TREE_SET_CREATOR);
    map.register(Properties.class, PROPERTIES_CREATOR);
    map.makeUnmodifiable();
    return map;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static JsonDeserializer<?> wrapDeserializer(JsonDeserializer<?> deserializer) {
    return new JsonDeserializerExceptionWrapper(deserializer);
  }

  static ParameterizedTypeHandlerMap<JsonSerializer<?>> getDefaultSerializers() {
    return getDefaultSerializers(false, LongSerializationPolicy.DEFAULT);
  }
      
  static ParameterizedTypeHandlerMap<JsonSerializer<?>> getDefaultSerializers(
      boolean serializeSpecialFloatingPointValues, LongSerializationPolicy longSerializationPolicy) {
    ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers =
        new ParameterizedTypeHandlerMap<JsonSerializer<?>>();
    
    // Double primitive
    DefaultTypeAdapters.DoubleSerializer doubleSerializer = 
        new DefaultTypeAdapters.DoubleSerializer(serializeSpecialFloatingPointValues);
    serializers.registerIfAbsent(Double.class, doubleSerializer);
    serializers.registerIfAbsent(double.class, doubleSerializer);

    // Float primitive
    DefaultTypeAdapters.FloatSerializer floatSerializer = 
        new DefaultTypeAdapters.FloatSerializer(serializeSpecialFloatingPointValues);
    serializers.registerIfAbsent(Float.class, floatSerializer);
    serializers.registerIfAbsent(float.class, floatSerializer);

    // Long primitive
    DefaultTypeAdapters.LongSerializer longSerializer = 
        new DefaultTypeAdapters.LongSerializer(longSerializationPolicy);
    serializers.registerIfAbsent(Long.class, longSerializer);
    serializers.registerIfAbsent(long.class, longSerializer);

    serializers.registerIfAbsent(DEFAULT_SERIALIZERS);
    return serializers;
  }
  
  static ParameterizedTypeHandlerMap<JsonDeserializer<?>> getDefaultDeserializers() {
    return DEFAULT_DESERIALIZERS;
  }
  
  static ParameterizedTypeHandlerMap<InstanceCreator<?>> getDefaultInstanceCreators() {
    return DEFAULT_INSTANCE_CREATORS;
  }

  static class DefaultDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    private final DateFormat format;

    DefaultDateTypeAdapter() {
      this.format = DateFormat.getDateTimeInstance();
    }

    DefaultDateTypeAdapter(final String datePattern) {
      this.format = new SimpleDateFormat(datePattern);
    }
    
    DefaultDateTypeAdapter(final int style) {
      this.format = DateFormat.getDateInstance(style);
    }

    public DefaultDateTypeAdapter(final int dateStyle, final int timeStyle) {
      this.format = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
    }

    // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
    // See issue 162
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
      synchronized (format) {
        String dateFormatAsString = format.format(src);
        return new JsonPrimitive(dateFormatAsString);
      }
    }

    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      if (!(json instanceof JsonPrimitive)) {
        throw new JsonParseException("The date should be a string value");
      }
      try {
        synchronized (format) {
          return format.parse(json.getAsString());
        }
      } catch (ParseException e) {
        throw new JsonSyntaxException(e);
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

  static class DefaultJavaSqlDateTypeAdapter implements JsonSerializer<java.sql.Date>,
      JsonDeserializer<java.sql.Date> {
    private final DateFormat format;
    DefaultJavaSqlDateTypeAdapter() {
      this.format = new SimpleDateFormat("MMM d, yyyy");
    }

    public JsonElement serialize(java.sql.Date src, Type typeOfSrc,
        JsonSerializationContext context) {
      synchronized (format) {
        String dateFormatAsString = format.format(src);
        return new JsonPrimitive(dateFormatAsString);
      }
    }    
    public java.sql.Date deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      if (!(json instanceof JsonPrimitive)) {
        throw new JsonParseException("The date should be a string value");
      }
      try {
        synchronized (format) {
          Date date = format.parse(json.getAsString());
          return new java.sql.Date(date.getTime());
        }
      } catch (ParseException e) {
        throw new JsonSyntaxException(e);
      }
    }
  }

  static class DefaultTimestampDeserializer implements JsonDeserializer<Timestamp> {
    public Timestamp deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      Date date = context.deserialize(json, Date.class);
      return new Timestamp(date.getTime());
    }
  }

  static class DefaultTimeTypeAdapter implements JsonSerializer<Time>, JsonDeserializer<Time> {
    private final DateFormat format;
    DefaultTimeTypeAdapter() {
      this.format = new SimpleDateFormat("hh:mm:ss a");
    }
    public JsonElement serialize(Time src, Type typeOfSrc, JsonSerializationContext context) {
      synchronized (format) {
        String dateFormatAsString = format.format(src);
        return new JsonPrimitive(dateFormatAsString);
      }
    }
    public Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      if (!(json instanceof JsonPrimitive)) {
        throw new JsonParseException("The date should be a string value");
      }
      try {
        synchronized (format) {
          Date date = format.parse(json.getAsString());
          return new Time(date.getTime());
        }
      } catch (ParseException e) {
        throw new JsonSyntaxException(e);
      }
    }
  }

  private static class GregorianCalendarTypeAdapter 
      implements JsonSerializer<GregorianCalendar>, JsonDeserializer<GregorianCalendar> {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY_OF_MONTH = "dayOfMonth";
    private static final String HOUR_OF_DAY = "hourOfDay";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    public JsonElement serialize(GregorianCalendar src, Type typeOfSrc,
        JsonSerializationContext context) {
      JsonObject obj = new JsonObject();
      obj.addProperty(YEAR, src.get(Calendar.YEAR));
      obj.addProperty(MONTH, src.get(Calendar.MONTH));
      obj.addProperty(DAY_OF_MONTH, src.get(Calendar.DAY_OF_MONTH));
      obj.addProperty(HOUR_OF_DAY, src.get(Calendar.HOUR_OF_DAY));
      obj.addProperty(MINUTE, src.get(Calendar.MINUTE));      
      obj.addProperty(SECOND, src.get(Calendar.SECOND));      
      return obj;
    }
    
    public GregorianCalendar deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      JsonObject obj = json.getAsJsonObject();
      int year = obj.get(YEAR).getAsInt();
      int month = obj.get(MONTH).getAsInt();
      int dayOfMonth = obj.get(DAY_OF_MONTH).getAsInt();
      int hourOfDay = obj.get(HOUR_OF_DAY).getAsInt();
      int minute = obj.get(MINUTE).getAsInt();      
      int second = obj.get(SECOND).getAsInt();      
      return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
    }
    
    @Override
    public String toString() {
      return GregorianCalendarTypeAdapter.class.getSimpleName();
    }
  }
  
  @SuppressWarnings("unchecked")
  private static class EnumTypeAdapter<T extends Enum<T>>
      implements JsonSerializer<T>, JsonDeserializer<T> {
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.name());
    }

    @SuppressWarnings("cast")
    public T deserialize(JsonElement json, Type classOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return (T) Enum.valueOf((Class<T>) classOfT, json.getAsString());
    }

    @Override
    public String toString() {
      return EnumTypeAdapter.class.getSimpleName();
    }
  }

  private static class UrlTypeAdapter implements JsonSerializer<URL>, JsonDeserializer<URL> {
    public JsonElement serialize(URL src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toExternalForm());
    }

    public URL deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      try {
        return new URL(json.getAsString());
      } catch (MalformedURLException e) {
        throw new JsonSyntaxException(e);
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
        throw new JsonSyntaxException(e);
      }
    }
    @Override
    public String toString() {
      return UriTypeAdapter.class.getSimpleName();
    }
  }
  
  private static class UuidTypeAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
    public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString());
    }

    public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
        throws JsonParseException {     
      return UUID.fromString(json.getAsString());
    }

    @Override
    public String toString() {
        return UuidTypeAdapter.class.getSimpleName();
    }
  }

  private static class LocaleTypeAdapter 
      implements JsonSerializer<Locale>, JsonDeserializer<Locale> {
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

    @Override
    public String toString() {
      return LocaleTypeAdapter.class.getSimpleName();
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static class CollectionTypeAdapter implements JsonSerializer<Collection>, 
      JsonDeserializer<Collection>, InstanceCreator<Collection> {
    public JsonElement serialize(Collection src, Type typeOfSrc, JsonSerializationContext context) {
      if (src == null) {
        return JsonNull.createJsonNull();
      }
      JsonArray array = new JsonArray();
      Type childGenericType = null;
      if (typeOfSrc instanceof ParameterizedType) {
        childGenericType = new TypeInfoCollection(typeOfSrc).getElementType();        
      }
      for (Object child : src) {
        if (child == null) {
          array.add(JsonNull.createJsonNull());
        } else {
          Type childType = (childGenericType == null || childGenericType == Object.class)
              ? child.getClass() : childGenericType;
          JsonElement element = context.serialize(child, childType);
          array.add(element);
        }
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

  private static class PropertiesCreator implements InstanceCreator<Properties> {
    public Properties createInstance(Type type) {
      return new Properties();
    }    
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  static class MapTypeAdapter implements JsonSerializer<Map>, JsonDeserializer<Map>,
      InstanceCreator<Map> {
    
    public JsonElement serialize(Map src, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject map = new JsonObject();
      Type childGenericType = null;
      if (typeOfSrc instanceof ParameterizedType) {
        childGenericType = new TypeInfoMap(typeOfSrc).getValueType();        
      }

      for (Map.Entry entry : (Set<Map.Entry>) src.entrySet()) {
        Object value = entry.getValue();

        JsonElement valueElement;
        if (value == null) {
          valueElement = JsonNull.createJsonNull();
        } else {
          Type childType = (childGenericType == null)
              ? value.getClass() : childGenericType;
          valueElement = context.serialize(value, childType);
        }
        map.add(String.valueOf(entry.getKey()), valueElement);
      }
      return map;
    }

    public Map deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      // Use ObjectConstructor to create instance instead of hard-coding a specific type. 
      // This handles cases where users are using their own subclass of Map.
      Map<Object, Object> map = constructMapType(typeOfT, context);
      TypeInfoMap mapTypeInfo = new TypeInfoMap(typeOfT);
      for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
        Object key = context.deserialize(new JsonPrimitive(entry.getKey()), mapTypeInfo.getKeyType());
        Object value = context.deserialize(entry.getValue(), mapTypeInfo.getValueType());
        map.put(key, value);
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

  private static class BigDecimalTypeAdapter
      implements JsonSerializer<BigDecimal>, JsonDeserializer<BigDecimal> {
    public JsonElement serialize(BigDecimal src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public BigDecimal deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return json.getAsBigDecimal();
    }

    @Override
    public String toString() {
      return BigDecimalTypeAdapter.class.getSimpleName();
    }
  }

  private static class BigIntegerTypeAdapter 
      implements JsonSerializer<BigInteger>, JsonDeserializer<BigInteger> {

    public JsonElement serialize(BigInteger src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public BigInteger deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      return json.getAsBigInteger();
    }

    @Override
    public String toString() {
      return BigIntegerTypeAdapter.class.getSimpleName();
    }
  }
  
  private static class NumberTypeAdapter
      implements JsonSerializer<Number>, JsonDeserializer<Number> {
    public JsonElement serialize(Number src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }
    
    public Number deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsNumber();
    }
    
    @Override
    public String toString() {
      return NumberTypeAdapter.class.getSimpleName();
    }
  }
  
  private static class LongSerializer implements JsonSerializer<Long> {
    private final LongSerializationPolicy longSerializationPolicy;
    
    private LongSerializer(LongSerializationPolicy longSerializationPolicy) {
      this.longSerializationPolicy = longSerializationPolicy;
    }

    public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
      return longSerializationPolicy.serialize(src);
    }

    @Override
    public String toString() {
      return LongSerializer.class.getSimpleName();
    }
  }

  private static class LongDeserializer implements JsonDeserializer<Long> {
    public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsLong();
    }

    @Override
    public String toString() {
      return LongDeserializer.class.getSimpleName();
    }
  }

  private static class IntegerTypeAdapter 
      implements JsonSerializer<Integer>, JsonDeserializer<Integer> {
    public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsInt();
    }

    @Override
    public String toString() {
      return IntegerTypeAdapter.class.getSimpleName();
    }
  }

  private static class ShortTypeAdapter
      implements JsonSerializer<Short>, JsonDeserializer<Short> {
    public JsonElement serialize(Short src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public Short deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsShort();
    }
    
    @Override
    public String toString() {
      return ShortTypeAdapter.class.getSimpleName();
    }
  }

  private static class ByteTypeAdapter implements JsonSerializer<Byte>, JsonDeserializer<Byte> {
    public JsonElement serialize(Byte src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public Byte deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsByte();
    }

    @Override
    public String toString() {
      return ByteTypeAdapter.class.getSimpleName();
    }
  }

  static class FloatSerializer implements JsonSerializer<Float> {
    private final boolean serializeSpecialFloatingPointValues;

    FloatSerializer(boolean serializeSpecialDoubleValues) {
      this.serializeSpecialFloatingPointValues = serializeSpecialDoubleValues;
    }

    public JsonElement serialize(Float src, Type typeOfSrc, JsonSerializationContext context) {
      if (!serializeSpecialFloatingPointValues) {
        if (Float.isNaN(src) || Float.isInfinite(src)) {
          throw new IllegalArgumentException(src 
              + " is not a valid float value as per JSON specification. To override this"
              + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
        }
      }
      return new JsonPrimitive(src);
    }
  }
  
  private static class FloatDeserializer implements JsonDeserializer<Float> {
    public Float deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsFloat();
    }

    @Override
    public String toString() {
      return FloatDeserializer.class.getSimpleName();
    }
  }

  static class DoubleSerializer implements JsonSerializer<Double> {
    private final boolean serializeSpecialFloatingPointValues;

    DoubleSerializer(boolean serializeSpecialDoubleValues) {
      this.serializeSpecialFloatingPointValues = serializeSpecialDoubleValues;
    }

    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
      if (!serializeSpecialFloatingPointValues) {
        if (Double.isNaN(src) || Double.isInfinite(src)) {
          throw new IllegalArgumentException(src 
              + " is not a valid double value as per JSON specification. To override this"
              + " behavior, use GsonBuilder.serializeSpecialDoubleValues() method.");
        }
      }
      return new JsonPrimitive(src);
    }
  }

  private static class DoubleDeserializer implements JsonDeserializer<Double> {
    public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsDouble();
    }

    @Override
    public String toString() {
      return DoubleDeserializer.class.getSimpleName();
    }
  }

  private static class CharacterTypeAdapter 
      implements JsonSerializer<Character>, JsonDeserializer<Character> {
    public JsonElement serialize(Character src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public Character deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsCharacter();
    }

    @Override
    public String toString() {
      return CharacterTypeAdapter.class.getSimpleName();
    }
  }
  
  private static class StringTypeAdapter
      implements JsonSerializer<String>, JsonDeserializer<String> {
    public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }
    
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsString();
    }
    
    @Override
    public String toString() {
      return StringTypeAdapter.class.getSimpleName();
    }
  }

  private static class BooleanTypeAdapter 
      implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {
    public JsonElement serialize(Boolean src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src);
    }

    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return json.getAsBoolean();
    }

    @Override
    public String toString() {
      return BooleanTypeAdapter.class.getSimpleName();
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

  private static class HashSetCreator implements InstanceCreator<HashSet<?>> {
    public HashSet<?> createInstance(Type type) {
      return new HashSet<Object>();
    }
    @Override
    public String toString() {
      return HashSetCreator.class.getSimpleName();
    }
  }
}
