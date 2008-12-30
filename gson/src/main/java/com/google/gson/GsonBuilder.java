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

import com.google.gson.DefaultTypeAdapters.DefaultDateTypeAdapter;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Use this builder to construct a {@link Gson} instance when you need to set configuration
 * options other than the default. For {@link Gson} with default configuration, it is simpler to
 * use {@code new Gson()}. {@code GsonBuilder} is best used by creating it, and then invoking its
 * various configuration methods, and finally calling create.</p>
 *
 * <p>The following is an example shows how to use the {@code GsonBuilder} to construct a Gson
 * instance:
 *
 * <pre>
 * Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(Id.class, new IdTypeAdapter())
 *     .serializeNulls()
 *     .setDateFormat(DateFormat.LONG)
 *     .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
 *     .setPrettyPrinting()
 *     .setVersion(1.0)
 *     .create();
 * </pre></p>
 *
 * <p>NOTE: the order of invocation of configuration methods does not matter.</p>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class GsonBuilder {

  private double ignoreVersionsAfter;
  private boolean serializeLongAsString;
  private ModifierBasedExclusionStrategy modifierBasedExclusionStrategy;
  private boolean serializeInnerClasses;
  private final AnonymousAndLocalClassExclusionStrategy anonAndLocalClassExclusionStrategy;
  private final InnerClassExclusionStrategy innerClassExclusionStrategy;
  private boolean excludeFieldsWithoutExposeAnnotation;
  private JsonFormatter formatter;
  private FieldNamingStrategy fieldNamingPolicy;
  private final ParameterizedTypeHandlerMap<InstanceCreator<?>> instanceCreators;
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;
  private final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers;
  private boolean serializeNulls;
  private String datePattern;
  private int dateStyle;
  private int timeStyle;
  private boolean serializeSpecialFloatingPointValues;

  /**
   * Creates a GsonBuilder instance that can be used to build Gson with various configuration
   * settings. GsonBuilder follows the builder pattern, and it is typically used by first
   * invoking various configuration methods to set desired options, and finally calling
   * {@link #create()}.
   */
  public GsonBuilder() {
    // setup default values
    ignoreVersionsAfter = VersionConstants.IGNORE_VERSIONS;
    serializeLongAsString = false;
    serializeInnerClasses = true;
    anonAndLocalClassExclusionStrategy = new AnonymousAndLocalClassExclusionStrategy();
    innerClassExclusionStrategy = new InnerClassExclusionStrategy();
    modifierBasedExclusionStrategy = Gson.DEFAULT_MODIFIER_BASED_EXCLUSION_STRATEGY;
    excludeFieldsWithoutExposeAnnotation = false;
    formatter = Gson.DEFAULT_JSON_FORMATTER;
    fieldNamingPolicy = Gson.DEFAULT_NAMING_POLICY;
    instanceCreators = new ParameterizedTypeHandlerMap<InstanceCreator<?>>();
    serializers = new ParameterizedTypeHandlerMap<JsonSerializer<?>>();
    deserializers = new ParameterizedTypeHandlerMap<JsonDeserializer<?>>();
    serializeNulls = false;
    dateStyle = DateFormat.DEFAULT;
    timeStyle = DateFormat.DEFAULT;
    serializeSpecialFloatingPointValues = false;
  }

  /**
   * Configures Gson to enable versioning support.
   *
   * @param ignoreVersionsAfter any field or type marked with a version higher than this value
   * are ignored during serialization or deserialization.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder setVersion(double ignoreVersionsAfter) {
    this.ignoreVersionsAfter = ignoreVersionsAfter;
    return this;
  }

  /**
   * Configures Gson to excludes all class fields that have the specified modifiers. By default,
   * Gson will exclude all fields marked transient or static. This method will override that
   * behavior.
   *
   * @param modifiers the field modifiers. You must use the modifiers specified in the
   * {@link java.lang.reflect.Modifier} class. For example,
   * {@link java.lang.reflect.Modifier#TRANSIENT},
   * {@link java.lang.reflect.Modifier#STATIC}.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder excludeFieldsWithModifiers(int... modifiers) {
    boolean skipSynthetics = true;
    modifierBasedExclusionStrategy = new ModifierBasedExclusionStrategy(skipSynthetics, modifiers);
    return this;
  }

  /**
   * Configures Gson to exclude all fields from consideration for serialization or deserialization
   * that do not have the {@link com.google.gson.annotations.Expose} annotation.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder excludeFieldsWithoutExposeAnnotation() {
    excludeFieldsWithoutExposeAnnotation = true;
    return this;
  }

  /**
   * Configure Gson to serialize null fields. By default, Gson omits all fields that are null
   * during serialization.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.2
   */
  public GsonBuilder serializeNulls() {
    this.serializeNulls = true;
    return this;
  }
  
  /**
   * Configures Gson to output fields of type {@code long} as {@code String}s instead of a number.
   *
   * @param value the boolean value on whether or not {@code Gson} should serialize a {@code long}
   * field as a {@code String}
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder serializeLongFieldsAsString(boolean value) {
    serializeLongAsString = value;
    return this;
  }

  /**
   * Configures Gson to exclude inner classes during serialization.
   * 
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder disableInnerClassSerialization() {
    serializeInnerClasses = false;
    return this;
  }
  
  /**
   * Configures Gson to apply a specific naming policy to an object's field during serialization
   * and deserialization.
   *
   * @param namingConvention the JSON field naming convention to use for serialization and
   * deserialization.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder setFieldNamingPolicy(FieldNamingPolicy namingConvention) {
    return setFieldNamingStrategy(namingConvention.getFieldNamingPolicy());
  }

  /**
   * Configures Gson to apply a specific naming policy strategy to an object's field during
   * serialization and deserialization.
   *
   * @param fieldNamingPolicy the actual naming strategy to apply to the fields
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  private GsonBuilder setFieldNamingStrategy(FieldNamingStrategy fieldNamingPolicy) {
    this.fieldNamingPolicy = new SerializedNameAnnotationInterceptingNamingPolicy(fieldNamingPolicy);
    return this;
  }

  /**
   * Configures Gson to output Json that fits in a page for pretty printing. This option only
   * affects Json serialization.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder setPrettyPrinting() {
    setFormatter(new JsonPrintFormatter());
    return this;
  }
  
  /**
   * Configures Gson to output Json that fits in a page for pretty printing. This option only
   * affects Json serialization.
   *
   * @param escapeHtmlChars true if specific HTML characters should be escaped
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder setPrettyPrinting(boolean escapeHtmlChars) {
    setFormatter(new JsonPrintFormatter(escapeHtmlChars));
    return this;
  }
  
  /**
   * Configures Gson to output Json in a compact format.
   *
   * @param escapeHtmlChars true if specific HTML characters should be escaped
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder setCompactPrinting(boolean escapeHtmlChars) {
    setFormatter(new JsonCompactFormatter(escapeHtmlChars));
    return this;
  }

  /**
   * Configures Gson with a new formatting strategy other than the default strategy. The default
   * strategy is to provide a compact representation that eliminates all unneeded white-space.
   *
   * @param formatter the new formatter to use.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @see JsonPrintFormatter
   */
  GsonBuilder setFormatter(JsonFormatter formatter) {
    this.formatter = formatter;
    return this;
  }

  /**
   * Configures Gson to serialize {@code Date} objects according to the pattern provided. You can
   * call this method or {@link #setDateFormat(int)} multiple times, but only the last invocation
   * will be used to decide the serialization format.
   *
   * <p>Note that this pattern must abide by the convention provided by {@code SimpleDateFormat}
   * class. See the documentation in {@link java.text.SimpleDateFormat} for more information on
   * valid date and time patterns.</p>
   *
   * @param pattern the pattern that dates will be serialized/deserialized to/from
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.2
   */
  public GsonBuilder setDateFormat(String pattern) {
    // TODO(Joel): Make this fail fast if it is an invalid date format
    this.datePattern = pattern;
    return this;
  }

  /**
   * Configures Gson to to serialize {@code Date} objects according to the style value provided.
   * You can call this method or {@link #setDateFormat(String)} multiple times, but only the last
   * invocation will be used to decide the serialization format.
   *
   * <p>Note that this style value should be one of the predefined constants in the
   * {@code DateFormat} class. See the documentation in {@link java.text.DateFormat} for more
   * information on the valid style constants.</p>
   *
   * @param style the predefined date style that date objects will be serialized/deserialized
   * to/from
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.2
   */
  public GsonBuilder setDateFormat(int style) {
    this.dateStyle = style;
    this.datePattern = null;
    return this;
  }

  /**
   * Configures Gson to to serialize {@code Date} objects according to the style value provided.
   * You can call this method or {@link #setDateFormat(String)} multiple times, but only the last
   * invocation will be used to decide the serialization format.
   *
   * <p>Note that this style value should be one of the predefined constants in the
   * {@code DateFormat} class. See the documentation in {@link java.text.DateFormat} for more
   * information on the valid style constants.</p>
   *
   * @param dateStyle the predefined date style that date objects will be serialized/deserialized
   * to/from
   * @param timeStyle the predefined style for the time portion of the date objects
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.2
   */
  public GsonBuilder setDateFormat(int dateStyle, int timeStyle) {
    this.dateStyle = dateStyle;
    this.timeStyle = timeStyle;
    this.datePattern = null;
    return this;
  }
  
  /**
   * Configures Gson for custom serialization or deserialization. This method combines the
   * registration of an {@link InstanceCreator}, {@link JsonSerializer}, and a
   * {@link JsonDeserializer}. It is best used when a single object {@code typeAdapter} implements
   * all the required interfaces for custom serialization with Gson. If an instance creator,
   * serializer or deserializer was previously registered for the specified {@code type}, it is
   * overwritten.
   *
   * @param type the type definition for the type adapter being registered
   * @param typeAdapter This object must implement at least one of the {@link InstanceCreator},
   * {@link JsonSerializer}, and a {@link JsonDeserializer} interfaces.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder registerTypeAdapter(Type type, Object typeAdapter) {
    Preconditions.checkArgument(typeAdapter instanceof JsonSerializer
        || typeAdapter instanceof JsonDeserializer || typeAdapter instanceof InstanceCreator);
    if (typeAdapter instanceof InstanceCreator) {
      registerInstanceCreator(type, (InstanceCreator<?>) typeAdapter);
    }
    if (typeAdapter instanceof JsonSerializer) {
      registerSerializer(type, (JsonSerializer<?>) typeAdapter);
    }
    if (typeAdapter instanceof JsonDeserializer) {
      registerDeserializer(type, (JsonDeserializer<?>) typeAdapter);
    }
    return this;
  }

  /**
   * Configures Gson to use a custom {@link InstanceCreator} for the specified type. If an instance
   * creator was previously registered for the specified class, it is overwritten. Since this method
   * takes a type instead of a Class object, it can be used to register a specific handler for a
   * generic type corresponding to a raw type.
   *
   * @param <T> the type for which instance creator is being registered
   * @param typeOfT The Type definition for T
   * @param instanceCreator the instance creator for T
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  private <T> GsonBuilder registerInstanceCreator(Type typeOfT,
      InstanceCreator<? extends T> instanceCreator) {
    instanceCreators.register(typeOfT, instanceCreator);
    return this;
  }

  /**
   * Configures Gson to use a custom JSON serializer for the specified type. You should use this
   * method if you want to register different serializers for different generic types corresponding
   * to a raw type.
   *
   * @param <T> the type for which the serializer is being registered
   * @param typeOfT The type definition for T
   * @param serializer the custom serializer
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  private <T> GsonBuilder registerSerializer(Type typeOfT, final JsonSerializer<T> serializer) {
    serializers.register(typeOfT, serializer);
    return this;
  }

  /**
   * Configures Gson to use a custom JSON deserializer for the specified type. You should use this
   * method if you want to register different deserializers for different generic types
   * corresponding to a raw type.
   *
   * @param <T> the type for which the deserializer is being registered
   * @param typeOfT The type definition for T
   * @param deserializer the custom deserializer
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  private <T> GsonBuilder registerDeserializer(Type typeOfT, JsonDeserializer<T> deserializer) {
    deserializers.register(typeOfT, new JsonDeserializerExceptionWrapper<T>(deserializer));
    return this;
  }

  /**
   * Section 2.4 of <a href="http://www.ietf.org/rfc/rfc4627.txt">JSON specification</a> disallows
   * special double values (NaN, Infinity, -Infinity). However, 
   * <a href="http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-262.pdf">Javascript 
   * specification</a> (see section 4.3.20, 4.3.22, 4.3.23) allows these values as valid Javascript
   * values. Moreover, most JavaScript engines will accept these special values in JSON without 
   * problem. So, at a practical level, it makes sense to accept these values as valid JSON even
   * though JSON specification disallows them. 
   * 
   * <p>Gson always accepts these special values during deserialization. However, it outputs 
   * strictly compliant JSON. Hence, if it encounters a float value {@link Float#NaN}, 
   * {@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}, or a double value 
   * {@link Double#NaN}, {@link Double#POSITIVE_INFINITY}, {@link Double#NEGATIVE_INFINITY}, it 
   * will throw an {@link IllegalArgumentException}. This method provides a way to override the
   * default behavior when you know that the JSON receiver will be able to handle these special
   * values.   
   * 
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder serializeSpecialFloatingPointValues() {
    this.serializeSpecialFloatingPointValues = true;
    return this;
  }

  /**
   * Creates a {@link Gson} instance based on the current configuration. This method is free of
   * side-effects to this {@code GsonBuilder} instance and hence can be called multiple times.
   *
   * @return an instance of Gson configured with the options currently set in this builder
   */
  public Gson create() {
    List<ExclusionStrategy> strategies = new LinkedList<ExclusionStrategy>();
    strategies.add(modifierBasedExclusionStrategy);
    strategies.add(anonAndLocalClassExclusionStrategy);

    if (!serializeInnerClasses) {
      strategies.add(innerClassExclusionStrategy);
    }
    if (ignoreVersionsAfter != VersionConstants.IGNORE_VERSIONS) {
      strategies.add(new VersionExclusionStrategy(ignoreVersionsAfter));
    }
    if (excludeFieldsWithoutExposeAnnotation) {
      strategies.add(new ExposeAnnotationBasedExclusionStrategy());
    }
    ExclusionStrategy exclusionStrategy = new DisjunctionExclusionStrategy(strategies);

    ParameterizedTypeHandlerMap<JsonSerializer<?>> customSerializers = serializers.copyOf();
    ParameterizedTypeHandlerMap<JsonDeserializer<?>> customDeserializers = deserializers.copyOf();
    addTypeAdaptersForDate(datePattern, dateStyle, timeStyle, customSerializers,
        customDeserializers);

    customSerializers.registerIfAbsent(DefaultTypeAdapters.getDefaultSerializers(
        serializeSpecialFloatingPointValues, serializeLongAsString));
    
    customDeserializers.registerIfAbsent(DefaultTypeAdapters.getDefaultDeserializers());
    
    ParameterizedTypeHandlerMap<InstanceCreator<?>> customInstanceCreators =
      instanceCreators.copyOf();
    customInstanceCreators.registerIfAbsent(DefaultTypeAdapters.getDefaultInstanceCreators());
    MappedObjectConstructor objConstructor = Gson.createObjectConstructor(customInstanceCreators);

    Gson gson = new Gson(exclusionStrategy, fieldNamingPolicy, objConstructor, 
        formatter, serializeNulls, customSerializers, customDeserializers);
    return gson;
  }

  private static void addTypeAdaptersForDate(String datePattern, int dateStyle, int timeStyle,
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers,
      ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers) {
    // NOTE: if a date pattern exists, then that style takes priority
    DefaultDateTypeAdapter dateTypeAdapter = null;
    if (datePattern != null && !"".equals(datePattern.trim())) {
      dateTypeAdapter = new DefaultDateTypeAdapter(datePattern);
    } else if (dateStyle != DateFormat.DEFAULT && timeStyle != DateFormat.DEFAULT) {
      dateTypeAdapter = new DefaultDateTypeAdapter(dateStyle, timeStyle);
    }
    if (dateTypeAdapter != null
        && !serializers.hasAnyHandlerFor(Date.class)
        && !deserializers.hasAnyHandlerFor(Date.class)) {
      serializers.register(Date.class, dateTypeAdapter);
      deserializers.register(Date.class, dateTypeAdapter);
    }
  }
}
