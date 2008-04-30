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

import com.google.gson.parser.JsonParser;
import com.google.gson.parser.TokenMgrError;
import com.google.gson.version.VersionConstants;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the main class that a user of Gson will use. Gson is typically 
 * used by first constructing a {@link #Gson()} object and then invoking 
 * {@link #toJson(Object)} or {@link #fromJson(Class, String)} methods on it.
 * You can also use {@link GsonBuilder} to build a Gson instance with options 
 * such as versioning support. 
 * 
 * This is a facade class which abstracts the details of the underlying JSON
 * serialization and deserialization subsystems. 
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class Gson {
  private static Logger logger = Logger.getLogger(Gson.class.getName());

  private final ObjectNavigatorFactory navigatorFactory;
  private final MappedObjectConstructor objectConstructor;
  private final TypeAdapter typeAdapter;

  /** Map containing Type or Class objects as keys */
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers =
      new ParameterizedTypeHandlerMap<JsonSerializer<?>>();

  /** Map containing Type or Class objects as keys */
  private final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers =
      new ParameterizedTypeHandlerMap<JsonDeserializer<?>>();

  private final JsonFormatter formatter;

  static final TypeAdapter DEFAULT_TYPE_ADAPTER = 
      new TypeAdapterNotRequired(new PrimitiveTypeAdapter());
  
  static final ModifierBasedExclusionStrategy DEFAULT_MODIFIER_BASED_EXCLUSION_STRATEGY =
      new ModifierBasedExclusionStrategy(true, new int[] { Modifier.TRANSIENT, Modifier.STATIC });

  static final JsonFormatter DEFAULT_JSON_FORMATTER = new JsonCompactFormatter();
  
  /**
   * Constructs a Gson object that skips inner class references and ignores
   * all object versioning information
   */
  public Gson() {
    this(new ObjectNavigatorFactory(createExclusionStrategy(VersionConstants.IGNORE_VERSIONS)),
        new MappedObjectConstructor(), DEFAULT_TYPE_ADAPTER, DEFAULT_JSON_FORMATTER);
  }
  
  /**
   * Constructs a Gson object with the specified version and the
   * mode of operation while encountering inner class references.
   *
   * @param factory the object navigator factory to use when creating a
    *       new {@link ObjectNavigator} instance.
   */
  Gson(ObjectNavigatorFactory factory) {
    this(factory, new MappedObjectConstructor(), DEFAULT_TYPE_ADAPTER, DEFAULT_JSON_FORMATTER);
  }

  Gson(ObjectNavigatorFactory factory, MappedObjectConstructor objectConstructor,
      TypeAdapter typeAdapter, JsonFormatter formatter) {
    this.navigatorFactory = factory;
    this.objectConstructor = objectConstructor;
    this.typeAdapter = typeAdapter;
    this.formatter = formatter;

    Map<Type, JsonSerializer<?>> defaultSerializers =
      DefaultJsonSerializers.getDefaultSerializers();
    for (Map.Entry<Type, JsonSerializer<?>> entry : defaultSerializers.entrySet()) {
      registerSerializer(entry.getKey(), entry.getValue());
    }

    Map<Type, JsonDeserializer<?>> defaultDeserializers =
        DefaultJsonDeserializers.getDefaultDeserializers();
    for (Map.Entry<Type, JsonDeserializer<?>> entry : defaultDeserializers.entrySet()) {
      registerDeserializer(entry.getKey(), entry.getValue());
    }

    Map<Type, InstanceCreator<?>> defaultInstanceCreators =
        DefaultInstanceCreators.getDefaultInstanceCreators();
    for (Map.Entry<Type, InstanceCreator<?>> entry : defaultInstanceCreators.entrySet()) {
      objectConstructor.register(entry.getKey(), entry.getValue());
    }
  }

  private static ExclusionStrategy createExclusionStrategy(double version) {
    List<ExclusionStrategy> strategies = new LinkedList<ExclusionStrategy>();
    strategies.add(new InnerClassExclusionStrategy());
    strategies.add(DEFAULT_MODIFIER_BASED_EXCLUSION_STRATEGY);
    if (version != VersionConstants.IGNORE_VERSIONS) {
      strategies.add(new VersionExclusionStrategy(version));
    }
    return new DisjunctionExclusionStrategy(strategies);
  }

  /**
   * Registers an instance creator for the specified class. If an
   * instance creator was previously registered for the specified
   * class, it is overwritten. You should use this method if you
   * want to register a single instance creator for all generic
   * types mapping to a single raw type. If you want different
   * handling for different generic types of a single raw type,
   * use {@link #registerInstanceCreator(Type, InstanceCreator)}
   * instead.
   *
   * @param <T> the type for which instance creator is being registered
   * @param classOfT The class definition for the type T
   * @param instanceCreator the instance creator for T
   */
  public <T> void registerInstanceCreator(Class<T> classOfT,
      InstanceCreator<? extends T> instanceCreator) {
    registerInstanceCreator((Type) classOfT, instanceCreator);
  }

  /**
   * Registers an instance creator for the specified type. If an
   * instance creator was previously registered for the specified
   * class, it is overwritten. Since this method takes a type instead
   * of a Class object, it can be used to register a specific handler
   * for a generic type corresponding to a raw type. If you want to
   * have common handling for all generic types corresponding to a
   * raw type, use
   * {@link #registerInstanceCreator(Class, InstanceCreator)} instead.
   *
   * @param <T> the type for which instance creator is being registered
   * @param typeOfT The Type definition for T
   * @param instanceCreator the instance creator for T
   */
  public <T> void registerInstanceCreator(Type typeOfT,
      InstanceCreator<? extends T> instanceCreator) {
    objectConstructor.register(typeOfT, instanceCreator);
  }

  /**
   * Register a custom JSON serializer for the specified class. You
   * should use this method if you want to register a common serializer
   * for all generic types corresponding to a raw type. If you want
   * different handling for different generic types corresponding
   * to a raw type, use {@link #registerSerializer(Type, JsonSerializer)}
   * instead.
   *
   * @param <T> the type for which the serializer is being registered
   * @param classOfT The class definition for the type T
   * @param serializer the custom serializer
   */
  public <T> void registerSerializer(Class<T> classOfT, JsonSerializer<T> serializer) {
    registerSerializer((Type) classOfT, serializer);
  }

  /**
   * Register a custom JSON serializer for the specified type. You
   * should use this method if you want to register different
   * serializers for different generic types corresponding to a raw
   * type. If you want common handling for all generic types corresponding
   * to a raw type, use {@link #registerSerializer(Class, JsonSerializer)}
   * instead.
   *
   * @param <T> the type for which the serializer is being registered
   * @param typeOfT The type definition for T
   * @param serializer the custom serializer
   */
  public <T> void registerSerializer(Type typeOfT, final JsonSerializer<T> serializer) {
    if (serializers.hasSpecificHandlerFor(typeOfT)) {
      logger.log(Level.WARNING, "Overriding the existing Serializer for " + typeOfT);
    }
    serializers.register(typeOfT, serializer);
  }

  /**
   * Register a custom JSON deserializer for the specified class. You
   * should use this method if you want to register a common deserializer
   * for all generic types corresponding to a raw type. If you want
   * different handling for different generic types corresponding
   * to a raw type, use {@link #registerDeserializer(Type, JsonDeserializer)}
   * instead.
   *
   * @param <T> the type for which the deserializer is being registered
   * @param classOfT The class definition for the type T
   * @param deserializer the custom deserializer
   */
  public <T> void registerDeserializer(Class<T> classOfT, JsonDeserializer<T> deserializer) {
    registerDeserializer((Type) classOfT, deserializer);
  }

  /**
   * Register a custom JSON deserializer for the specified type. You
   * should use this method if you want to register different
   * deserializers for different generic types corresponding to a raw
   * type. If you want common handling for all generic types corresponding
   * to a raw type, use {@link #registerDeserializer(Class, JsonDeserializer)}
   * instead.
   *
   * @param <T> the type for which the deserializer is being registered
   * @param typeOfT The type definition for T
   * @param deserializer the custom deserializer
   */
  public <T> void registerDeserializer(Type typeOfT, final JsonDeserializer<T> deserializer) {
    if (deserializers.hasSpecificHandlerFor(typeOfT)) {
      logger.log(Level.WARNING, "Overriding the existing Deserializer for " + typeOfT);
    }
    deserializers.register(typeOfT, deserializer);
  }

  /**
   * @param src the object for which JSON representation is to be created
   *        setting for Gson
   * @return JSON representation of src
   */
  public String toJson(Object src) {
    if (src == null) {
      return "";
    }
    return toJson(src, src.getClass());
  }

  /**
   * This method is useful if you want custom handling of src
   * based on its generic type and also want to override versioning
   * information. Typically, this is done when you have registered a
   * custom serializer for src, or if src is a Collection of a specific
   * generic type. For most purposes, you should use
   * {@link #toJson(Object)} instead
   *
   * @param src the object for which JSON representation is to be created
   * @param typeOfSrc The specific genericized type of src
   * @return JSON representation of src
   */
  public String toJson(Object src, Type typeOfSrc) {
    if (src == null) {
      return "";
    }
    JsonSerializer.Context context = new JsonSerializationContext(navigatorFactory, serializers);
    JsonElement jsonElement = context.serialize(src, typeOfSrc);
    StringWriter writer = new StringWriter();
    formatter.format(jsonElement, new PrintWriter(writer));
    return jsonElement == null ? "" : writer.toString();
  }

  /**
   * @param <T> the type of the desired object
   * @param classOfT the class of T
   * @param json the string from which the object is to be deserialized
   * @return an object of type T from the string
   * @throws ParseException if json is not a valid representation for an object
   *         of type classOfT.
   */
  @SuppressWarnings("unchecked")
  public <T> T fromJson(Class<T> classOfT, String json) throws ParseException {
    return (T) fromJson((Type) classOfT, json);
  }

  /**
   * This method is useful if you want custom deserialization
   * corresponding to the specific generic type. For most other cases,
   * use {@link #fromJson(Class, String)} instead
   *
   * @param <T> the type of the desired object
   * @param typeOfT the specific generic type of T
   * @param json the string from which the object is to be deserialized
   * @return an object of type T from the string
   * @throws ParseException if json is not a valid representation for an object
   *         of type typeOfT.
   */
  @SuppressWarnings("unchecked")
  public <T> T fromJson(Type typeOfT, String json) throws ParseException {
    try {
      StringReader reader = new StringReader(json);
      JsonParser parser = new JsonParser(reader);
      JsonElement root = parser.parse();
      JsonDeserializer.Context context = new JsonDeserializationContext(navigatorFactory, 
          deserializers, objectConstructor, typeAdapter);
      return (T) context.deserialize(typeOfT, root);
    } catch (TokenMgrError e) {
      throw new ParseException("Failed parsing JSON source: " + json + " to Json", e);
    } catch (com.google.gson.parser.ParseException e) {
      throw new ParseException("Failed parsing JSON source: " + json + " to Json", e);
    }
  }
}
