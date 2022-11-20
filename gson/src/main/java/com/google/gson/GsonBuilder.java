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

import static com.google.gson.Gson.DEFAULT_COMPLEX_MAP_KEYS;
import static com.google.gson.Gson.DEFAULT_DATE_PATTERN;
import static com.google.gson.Gson.DEFAULT_ESCAPE_HTML;
import static com.google.gson.Gson.DEFAULT_JSON_NON_EXECUTABLE;
import static com.google.gson.Gson.DEFAULT_LENIENT;
import static com.google.gson.Gson.DEFAULT_NUMBER_TO_NUMBER_STRATEGY;
import static com.google.gson.Gson.DEFAULT_OBJECT_TO_NUMBER_STRATEGY;
import static com.google.gson.Gson.DEFAULT_FORMATTING_STYLE;
import static com.google.gson.Gson.DEFAULT_SERIALIZE_NULLS;
import static com.google.gson.Gson.DEFAULT_SPECIALIZE_FLOAT_VALUES;
import static com.google.gson.Gson.DEFAULT_USE_JDK_UNSAFE;

import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.bind.DefaultDateTypeAdapter;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.internal.sql.SqlTypesSupport;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
 *     .enableComplexMapKeySerialization()
 *     .serializeNulls()
 *     .setDateFormat(DateFormat.LONG)
 *     .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
 *     .setPrettyPrinting()
 *     .setVersion(1.0)
 *     .create();
 * </pre>
 *
 * <p>NOTES:
 * <ul>
 * <li> the order of invocation of configuration methods does not matter.</li>
 * <li> The default serialization of {@link Date} and its subclasses in Gson does
 *  not contain time-zone information. So, if you are using date/time instances,
 *  use {@code GsonBuilder} and its {@code setDateFormat} methods.</li>
 * </ul>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 * @author Jesse Wilson
 */
public final class GsonBuilder {
  private Excluder excluder = Excluder.DEFAULT;
  private LongSerializationPolicy longSerializationPolicy = LongSerializationPolicy.DEFAULT;
  private FieldNamingStrategy fieldNamingPolicy = FieldNamingPolicy.IDENTITY;
  private final Map<Type, InstanceCreator<?>> instanceCreators = new HashMap<>();
  private final List<TypeAdapterFactory> factories = new ArrayList<>();
  /** tree-style hierarchy factories. These come after factories for backwards compatibility. */
  private final List<TypeAdapterFactory> hierarchyFactories = new ArrayList<>();
  private boolean serializeNulls = DEFAULT_SERIALIZE_NULLS;
  private String datePattern = DEFAULT_DATE_PATTERN;
  private int dateStyle = DateFormat.DEFAULT;
  private int timeStyle = DateFormat.DEFAULT;
  private boolean complexMapKeySerialization = DEFAULT_COMPLEX_MAP_KEYS;
  private boolean serializeSpecialFloatingPointValues = DEFAULT_SPECIALIZE_FLOAT_VALUES;
  private boolean escapeHtmlChars = DEFAULT_ESCAPE_HTML;
  private FormattingStyle formattingStyle = DEFAULT_FORMATTING_STYLE;
  private boolean generateNonExecutableJson = DEFAULT_JSON_NON_EXECUTABLE;
  private boolean lenient = DEFAULT_LENIENT;
  private boolean useJdkUnsafe = DEFAULT_USE_JDK_UNSAFE;
  private ToNumberStrategy objectToNumberStrategy = DEFAULT_OBJECT_TO_NUMBER_STRATEGY;
  private ToNumberStrategy numberToNumberStrategy = DEFAULT_NUMBER_TO_NUMBER_STRATEGY;
  private final LinkedList<ReflectionAccessFilter> reflectionFilters = new LinkedList<>();

  /**
   * Creates a GsonBuilder instance that can be used to build Gson with various configuration
   * settings. GsonBuilder follows the builder pattern, and it is typically used by first
   * invoking various configuration methods to set desired options, and finally calling
   * {@link #create()}.
   */
  public GsonBuilder() {
  }

  /**
   * Constructs a GsonBuilder instance from a Gson instance. The newly constructed GsonBuilder
   * has the same configuration as the previously built Gson instance.
   *
   * @param gson the gson instance whose configuration should by applied to a new GsonBuilder.
   */
  GsonBuilder(Gson gson) {
    this.excluder = gson.excluder;
    this.fieldNamingPolicy = gson.fieldNamingStrategy;
    this.instanceCreators.putAll(gson.instanceCreators);
    this.serializeNulls = gson.serializeNulls;
    this.complexMapKeySerialization = gson.complexMapKeySerialization;
    this.generateNonExecutableJson = gson.generateNonExecutableJson;
    this.escapeHtmlChars = gson.htmlSafe;
    this.formattingStyle = gson.formattingStyle;
    this.lenient = gson.lenient;
    this.serializeSpecialFloatingPointValues = gson.serializeSpecialFloatingPointValues;
    this.longSerializationPolicy = gson.longSerializationPolicy;
    this.datePattern = gson.datePattern;
    this.dateStyle = gson.dateStyle;
    this.timeStyle = gson.timeStyle;
    this.factories.addAll(gson.builderFactories);
    this.hierarchyFactories.addAll(gson.builderHierarchyFactories);
    this.useJdkUnsafe = gson.useJdkUnsafe;
    this.objectToNumberStrategy = gson.objectToNumberStrategy;
    this.numberToNumberStrategy = gson.numberToNumberStrategy;
    this.reflectionFilters.addAll(gson.reflectionFilters);
  }

  /**
   * Configures Gson to enable versioning support. Versioning support works based on the
   * annotation types {@link Since} and {@link Until}. It allows including or excluding fields
   * and classes based on the specified version. See the documentation of these annotation
   * types for more information.
   *
   * <p>By default versioning support is disabled and usage of {@code @Since} and {@code @Until}
   * has no effect.
   *
   * @param version the version number to use.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @throws IllegalArgumentException if the version number is NaN or negative
   * @see Since
   * @see Until
   */
  public GsonBuilder setVersion(double version) {
    if (Double.isNaN(version) || version < 0.0) {
      throw new IllegalArgumentException("Invalid version: " + version);
    }
    excluder = excluder.withVersion(version);
    return this;
  }

  /**
   * Configures Gson to excludes all class fields that have the specified modifiers. By default,
   * Gson will exclude all fields marked {@code transient} or {@code static}. This method will
   * override that behavior.
   *
   * <p>This is a convenience method which behaves as if an {@link ExclusionStrategy} which
   * excludes these fields was {@linkplain #setExclusionStrategies(ExclusionStrategy...) registered with this builder}.
   *
   * @param modifiers the field modifiers. You must use the modifiers specified in the
   * {@link java.lang.reflect.Modifier} class. For example,
   * {@link java.lang.reflect.Modifier#TRANSIENT},
   * {@link java.lang.reflect.Modifier#STATIC}.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder excludeFieldsWithModifiers(int... modifiers) {
    Objects.requireNonNull(modifiers);
    excluder = excluder.withModifiers(modifiers);
    return this;
  }

  /**
   * Makes the output JSON non-executable in Javascript by prefixing the generated JSON with some
   * special text. This prevents attacks from third-party sites through script sourcing. See
   * <a href="http://code.google.com/p/google-gson/issues/detail?id=42">Gson Issue 42</a>
   * for details.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder generateNonExecutableJson() {
    this.generateNonExecutableJson = true;
    return this;
  }

  /**
   * Configures Gson to exclude all fields from consideration for serialization and deserialization
   * that do not have the {@link com.google.gson.annotations.Expose} annotation.
   *
   * <p>This is a convenience method which behaves as if an {@link ExclusionStrategy} which excludes
   * these fields was {@linkplain #setExclusionStrategies(ExclusionStrategy...) registered with this builder}.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder excludeFieldsWithoutExposeAnnotation() {
    excluder = excluder.excludeFieldsWithoutExposeAnnotation();
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
   * Enabling this feature will only change the serialized form if the map key is
   * a complex type (i.e. non-primitive) in its <strong>serialized</strong> JSON
   * form. The default implementation of map serialization uses {@code toString()}
   * on the key; however, when this is called then one of the following cases
   * apply:
   *
   * <p><b>Maps as JSON objects</b>
   *
   * <p>For this case, assume that a type adapter is registered to serialize and
   * deserialize some {@code Point} class, which contains an x and y coordinate,
   * to/from the JSON Primitive string value {@code "(x,y)"}. The Java map would
   * then be serialized as a {@link JsonObject}.
   *
   * <p>Below is an example:
   * <pre>  {@code
   *   Gson gson = new GsonBuilder()
   *       .register(Point.class, new MyPointTypeAdapter())
   *       .enableComplexMapKeySerialization()
   *       .create();
   *
   *   Map<Point, String> original = new LinkedHashMap<>();
   *   original.put(new Point(5, 6), "a");
   *   original.put(new Point(8, 8), "b");
   *   System.out.println(gson.toJson(original, type));
   * }</pre>
   * The above code prints this JSON object:<pre>  {@code
   *   {
   *     "(5,6)": "a",
   *     "(8,8)": "b"
   *   }
   * }</pre>
   *
   * <p><b>Maps as JSON arrays</b>
   *
   * <p>For this case, assume that a type adapter was NOT registered for some
   * {@code Point} class, but rather the default Gson serialization is applied.
   * In this case, some {@code new Point(2,3)} would serialize as {@code
   * {"x":2,"y":3}}.
   *
   * <p>Given the assumption above, a {@code Map<Point, String>} will be
   * serialize as an array of arrays (can be viewed as an entry set of pairs).
   *
   * <p>Below is an example of serializing complex types as JSON arrays:
   * <pre> {@code
   *   Gson gson = new GsonBuilder()
   *       .enableComplexMapKeySerialization()
   *       .create();
   *
   *   Map<Point, String> original = new LinkedHashMap<>();
   *   original.put(new Point(5, 6), "a");
   *   original.put(new Point(8, 8), "b");
   *   System.out.println(gson.toJson(original, type));
   * }
   * </pre>
   *
   * The JSON output would look as follows:
   * <pre>   {@code
   *   [
   *     [
   *       {
   *         "x": 5,
   *         "y": 6
   *       },
   *       "a"
   *     ],
   *     [
   *       {
   *         "x": 8,
   *         "y": 8
   *       },
   *       "b"
   *     ]
   *   ]
   * }</pre>
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.7
   */
  public GsonBuilder enableComplexMapKeySerialization() {
    complexMapKeySerialization = true;
    return this;
  }

  /**
   * Configures Gson to exclude inner classes (= non-{@code static} nested classes) during serialization
   * and deserialization. This is a convenience method which behaves as if an {@link ExclusionStrategy}
   * which excludes inner classes was {@linkplain #setExclusionStrategies(ExclusionStrategy...) registered with this builder}.
   * This means inner classes will be serialized as JSON {@code null}, and will be deserialized as
   * Java {@code null} with their JSON data being ignored. And fields with an inner class as type will
   * be ignored during serialization and deserialization.
   *
   * <p>By default Gson serializes and deserializes inner classes, but ignores references to the
   * enclosing instance. Deserialization might not be possible at all when {@link #disableJdkUnsafe()}
   * is used (and no custom {@link InstanceCreator} is registered), or it can lead to unexpected
   * {@code NullPointerException}s when the deserialized instance is used afterwards.
   *
   * <p>In general using inner classes with Gson should be avoided; they should be converted to {@code static}
   * nested classes if possible.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder disableInnerClassSerialization() {
    excluder = excluder.disableInnerClassSerialization();
    return this;
  }

  /**
   * Configures Gson to apply a specific serialization policy for {@code Long} and {@code long}
   * objects.
   *
   * @param serializationPolicy the particular policy to use for serializing longs.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder setLongSerializationPolicy(LongSerializationPolicy serializationPolicy) {
    this.longSerializationPolicy = Objects.requireNonNull(serializationPolicy);
    return this;
  }

  /**
   * Configures Gson to apply a specific naming policy to an object's fields during serialization
   * and deserialization.
   *
   * <p>This method just delegates to {@link #setFieldNamingStrategy(FieldNamingStrategy)}.
   */
  public GsonBuilder setFieldNamingPolicy(FieldNamingPolicy namingConvention) {
    return setFieldNamingStrategy(namingConvention);
  }

  /**
   * Configures Gson to apply a specific naming strategy to an object's fields during
   * serialization and deserialization.
   *
   * <p>The created Gson instance might only use the field naming strategy once for a
   * field and cache the result. It is not guaranteed that the strategy will be used
   * again every time the value of a field is serialized or deserialized.
   *
   * @param fieldNamingStrategy the naming strategy to apply to the fields
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
    this.fieldNamingPolicy = Objects.requireNonNull(fieldNamingStrategy);
    return this;
  }

  /**
   * Configures Gson to apply a specific number strategy during deserialization of {@link Object}.
   *
   * @param objectToNumberStrategy the actual object-to-number strategy
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @see ToNumberPolicy#DOUBLE The default object-to-number strategy
   * @since 2.8.9
   */
  public GsonBuilder setObjectToNumberStrategy(ToNumberStrategy objectToNumberStrategy) {
    this.objectToNumberStrategy = Objects.requireNonNull(objectToNumberStrategy);
    return this;
  }

  /**
   * Configures Gson to apply a specific number strategy during deserialization of {@link Number}.
   *
   * @param numberToNumberStrategy the actual number-to-number strategy
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @see ToNumberPolicy#LAZILY_PARSED_NUMBER The default number-to-number strategy
   * @since 2.8.9
   */
  public GsonBuilder setNumberToNumberStrategy(ToNumberStrategy numberToNumberStrategy) {
    this.numberToNumberStrategy = Objects.requireNonNull(numberToNumberStrategy);
    return this;
  }

  /**
   * Configures Gson to apply a set of exclusion strategies during both serialization and
   * deserialization. Each of the {@code strategies} will be applied as a disjunction rule.
   * This means that if one of the {@code strategies} suggests that a field (or class) should be
   * skipped then that field (or object) is skipped during serialization/deserialization.
   * The strategies are added to the existing strategies (if any); the existing strategies
   * are not replaced.
   *
   * <p>Fields are excluded for serialization and deserialization when
   * {@link ExclusionStrategy#shouldSkipField(FieldAttributes) shouldSkipField} returns {@code true},
   * or when {@link ExclusionStrategy#shouldSkipClass(Class) shouldSkipClass} returns {@code true}
   * for the field type. Gson behaves as if the field did not exist; its value is not serialized
   * and on deserialization if a JSON member with this name exists it is skipped by default.<br>
   * When objects of an excluded type (as determined by
   * {@link ExclusionStrategy#shouldSkipClass(Class) shouldSkipClass}) are serialized a
   * JSON null is written to output, and when deserialized the JSON value is skipped and
   * {@code null} is returned.
   *
   * <p>The created Gson instance might only use an exclusion strategy once for a field or
   * class and cache the result. It is not guaranteed that the strategy will be used again
   * every time the value of a field or a class is serialized or deserialized.
   *
   * @param strategies the set of strategy object to apply during object (de)serialization.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.4
   */
  public GsonBuilder setExclusionStrategies(ExclusionStrategy... strategies) {
    Objects.requireNonNull(strategies);
    for (ExclusionStrategy strategy : strategies) {
      excluder = excluder.withExclusionStrategy(strategy, true, true);
    }
    return this;
  }

  /**
   * Configures Gson to apply the passed in exclusion strategy during serialization.
   * If this method is invoked numerous times with different exclusion strategy objects
   * then the exclusion strategies that were added will be applied as a disjunction rule.
   * This means that if one of the added exclusion strategies suggests that a field (or
   * class) should be skipped then that field (or object) is skipped during its
   * serialization.
   *
   * <p>See the documentation of {@link #setExclusionStrategies(ExclusionStrategy...)}
   * for a detailed description of the effect of exclusion strategies.
   *
   * @param strategy an exclusion strategy to apply during serialization.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.7
   */
  public GsonBuilder addSerializationExclusionStrategy(ExclusionStrategy strategy) {
    Objects.requireNonNull(strategy);
    excluder = excluder.withExclusionStrategy(strategy, true, false);
    return this;
  }

  /**
   * Configures Gson to apply the passed in exclusion strategy during deserialization.
   * If this method is invoked numerous times with different exclusion strategy objects
   * then the exclusion strategies that were added will be applied as a disjunction rule.
   * This means that if one of the added exclusion strategies suggests that a field (or
   * class) should be skipped then that field (or object) is skipped during its
   * deserialization.
   *
   * <p>See the documentation of {@link #setExclusionStrategies(ExclusionStrategy...)}
   * for a detailed description of the effect of exclusion strategies.
   *
   * @param strategy an exclusion strategy to apply during deserialization.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.7
   */
  public GsonBuilder addDeserializationExclusionStrategy(ExclusionStrategy strategy) {
    Objects.requireNonNull(strategy);
    excluder = excluder.withExclusionStrategy(strategy, false, true);
    return this;
  }

  /**
   * Configures Gson to output JSON that fits in a page for pretty printing. This option only
   * affects JSON serialization.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder setPrettyPrinting() {
    return setPrettyPrinting(FormattingStyle.DEFAULT);
  }

  /**
   * Configures Gson to output JSON that uses a certain kind of formatting stile (for example newline and indent).
   * This option only affects JSON serialization.
   *
   * <p>Has no effect if the serialized format is a single line.</p>
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since $next-version$
   */
  public GsonBuilder setPrettyPrinting(FormattingStyle formattingStyle) {
    this.formattingStyle = formattingStyle;
    return this;
  }

  /**
   * Configures Gson to allow JSON data which does not strictly comply with the JSON specification.
   *
   * <p>Note: Due to legacy reasons most methods of Gson are always lenient, regardless of
   * whether this builder method is used.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @see JsonReader#setLenient(boolean)
   * @see JsonWriter#setLenient(boolean)
   */
  public GsonBuilder setLenient() {
    lenient = true;
    return this;
  }

  /**
   * By default, Gson escapes HTML characters such as &lt; &gt; etc. Use this option to configure
   * Gson to pass-through HTML characters as is.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.3
   */
  public GsonBuilder disableHtmlEscaping() {
    this.escapeHtmlChars = false;
    return this;
  }

  /**
   * Configures Gson to serialize {@code Date} objects according to the pattern provided. You can
   * call this method or {@link #setDateFormat(int)} multiple times, but only the last invocation
   * will be used to decide the serialization format.
   *
   * <p>The date format will be used to serialize and deserialize {@link java.util.Date} and in case
   * the {@code java.sql} module is present, also {@link java.sql.Timestamp} and {@link java.sql.Date}.
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
   * registration of an {@link TypeAdapter}, {@link InstanceCreator}, {@link JsonSerializer}, and a
   * {@link JsonDeserializer}. It is best used when a single object {@code typeAdapter} implements
   * all the required interfaces for custom serialization with Gson. If a type adapter was
   * previously registered for the specified {@code type}, it is overwritten.
   *
   * <p>This registers the type specified and no other types: you must manually register related
   * types! For example, applications registering {@code boolean.class} should also register {@code
   * Boolean.class}.
   *
   * <p>{@link JsonSerializer} and {@link JsonDeserializer} are made "{@code null}-safe". This
   * means when trying to serialize {@code null}, Gson will write a JSON {@code null} and the
   * serializer is not called. Similarly when deserializing a JSON {@code null}, Gson will emit
   * {@code null} without calling the deserializer. If it is desired to handle {@code null} values,
   * a {@link TypeAdapter} should be used instead.
   *
   * @param type the type definition for the type adapter being registered
   * @param typeAdapter This object must implement at least one of the {@link TypeAdapter},
   * {@link InstanceCreator}, {@link JsonSerializer}, and a {@link JsonDeserializer} interfaces.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   */
  public GsonBuilder registerTypeAdapter(Type type, Object typeAdapter) {
    Objects.requireNonNull(type);
    $Gson$Preconditions.checkArgument(typeAdapter instanceof JsonSerializer<?>
        || typeAdapter instanceof JsonDeserializer<?>
        || typeAdapter instanceof InstanceCreator<?>
        || typeAdapter instanceof TypeAdapter<?>);
    if (typeAdapter instanceof InstanceCreator<?>) {
      instanceCreators.put(type, (InstanceCreator<?>) typeAdapter);
    }
    if (typeAdapter instanceof JsonSerializer<?> || typeAdapter instanceof JsonDeserializer<?>) {
      TypeToken<?> typeToken = TypeToken.get(type);
      factories.add(TreeTypeAdapter.newFactoryWithMatchRawType(typeToken, typeAdapter));
    }
    if (typeAdapter instanceof TypeAdapter<?>) {
      @SuppressWarnings({"unchecked", "rawtypes"})
      TypeAdapterFactory factory = TypeAdapters.newFactory(TypeToken.get(type), (TypeAdapter)typeAdapter);
      factories.add(factory);
    }
    return this;
  }

  /**
   * Register a factory for type adapters. Registering a factory is useful when the type
   * adapter needs to be configured based on the type of the field being processed. Gson
   * is designed to handle a large number of factories, so you should consider registering
   * them to be at par with registering an individual type adapter.
   *
   * <p>The created Gson instance might only use the factory once to create an adapter for
   * a specific type and cache the result. It is not guaranteed that the factory will be used
   * again every time the type is serialized or deserialized.
   *
   * @since 2.1
   */
  public GsonBuilder registerTypeAdapterFactory(TypeAdapterFactory factory) {
    Objects.requireNonNull(factory);
    factories.add(factory);
    return this;
  }

  /**
   * Configures Gson for custom serialization or deserialization for an inheritance type hierarchy.
   * This method combines the registration of a {@link TypeAdapter}, {@link JsonSerializer} and
   * a {@link JsonDeserializer}. If a type adapter was previously registered for the specified
   * type hierarchy, it is overridden. If a type adapter is registered for a specific type in
   * the type hierarchy, it will be invoked instead of the one registered for the type hierarchy.
   *
   * @param baseType the class definition for the type adapter being registered for the base class
   *        or interface
   * @param typeAdapter This object must implement at least one of {@link TypeAdapter},
   *        {@link JsonSerializer} or {@link JsonDeserializer} interfaces.
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 1.7
   */
  public GsonBuilder registerTypeHierarchyAdapter(Class<?> baseType, Object typeAdapter) {
    Objects.requireNonNull(baseType);
    $Gson$Preconditions.checkArgument(typeAdapter instanceof JsonSerializer<?>
        || typeAdapter instanceof JsonDeserializer<?>
        || typeAdapter instanceof TypeAdapter<?>);
    if (typeAdapter instanceof JsonDeserializer || typeAdapter instanceof JsonSerializer) {
      hierarchyFactories.add(TreeTypeAdapter.newTypeHierarchyFactory(baseType, typeAdapter));
    }
    if (typeAdapter instanceof TypeAdapter<?>) {
      @SuppressWarnings({"unchecked", "rawtypes"})
      TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(baseType, (TypeAdapter)typeAdapter);
      factories.add(factory);
    }
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
   * Disables usage of JDK's {@code sun.misc.Unsafe}.
   *
   * <p>By default Gson uses {@code Unsafe} to create instances of classes which don't have
   * a no-args constructor. However, {@code Unsafe} might not be available for all Java
   * runtimes. For example Android does not provide {@code Unsafe}, or only with limited
   * functionality. Additionally {@code Unsafe} creates instances without executing any
   * constructor or initializer block, or performing initialization of field values. This can
   * lead to surprising and difficult to debug errors.
   * Therefore, to get reliable behavior regardless of which runtime is used, and to detect
   * classes which cannot be deserialized in an early stage of development, this method allows
   * disabling usage of {@code Unsafe}.
   *
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 2.9.0
   */
  public GsonBuilder disableJdkUnsafe() {
    this.useJdkUnsafe = false;
    return this;
  }

  /**
   * Adds a reflection access filter. A reflection access filter prevents Gson from using
   * reflection for the serialization and deserialization of certain classes. The logic in
   * the filter specifies which classes those are.
   *
   * <p>Filters will be invoked in reverse registration order, that is, the most recently
   * added filter will be invoked first.
   *
   * <p>By default Gson has no filters configured and will try to use reflection for
   * all classes for which no {@link TypeAdapter} has been registered, and for which no
   * built-in Gson {@code TypeAdapter} exists.
   *
   * <p>The created Gson instance might only use an access filter once for a class or its
   * members and cache the result. It is not guaranteed that the filter will be used again
   * every time a class or its members are accessed during serialization or deserialization.
   *
   * @param filter filter to add
   * @return a reference to this {@code GsonBuilder} object to fulfill the "Builder" pattern
   * @since 2.9.1
   */
  public GsonBuilder addReflectionAccessFilter(ReflectionAccessFilter filter) {
    Objects.requireNonNull(filter);
    reflectionFilters.addFirst(filter);
    return this;
  }

  /**
   * Creates a {@link Gson} instance based on the current configuration. This method is free of
   * side-effects to this {@code GsonBuilder} instance and hence can be called multiple times.
   *
   * @return an instance of Gson configured with the options currently set in this builder
   */
  public Gson create() {
    List<TypeAdapterFactory> factories = new ArrayList<>(this.factories.size() + this.hierarchyFactories.size() + 3);
    factories.addAll(this.factories);
    Collections.reverse(factories);

    List<TypeAdapterFactory> hierarchyFactories = new ArrayList<>(this.hierarchyFactories);
    Collections.reverse(hierarchyFactories);
    factories.addAll(hierarchyFactories);

    addTypeAdaptersForDate(datePattern, dateStyle, timeStyle, factories);

    return new Gson(excluder, fieldNamingPolicy, new HashMap<>(instanceCreators),
        serializeNulls, complexMapKeySerialization,
        generateNonExecutableJson, escapeHtmlChars, formattingStyle, lenient,
        serializeSpecialFloatingPointValues, useJdkUnsafe, longSerializationPolicy,
        datePattern, dateStyle, timeStyle, new ArrayList<>(this.factories),
        new ArrayList<>(this.hierarchyFactories), factories,
        objectToNumberStrategy, numberToNumberStrategy, new ArrayList<>(reflectionFilters));
  }

  private void addTypeAdaptersForDate(String datePattern, int dateStyle, int timeStyle,
      List<TypeAdapterFactory> factories) {
    TypeAdapterFactory dateAdapterFactory;
    boolean sqlTypesSupported = SqlTypesSupport.SUPPORTS_SQL_TYPES;
    TypeAdapterFactory sqlTimestampAdapterFactory = null;
    TypeAdapterFactory sqlDateAdapterFactory = null;

    if (datePattern != null && !datePattern.trim().isEmpty()) {
      dateAdapterFactory = DefaultDateTypeAdapter.DateType.DATE.createAdapterFactory(datePattern);

      if (sqlTypesSupported) {
        sqlTimestampAdapterFactory = SqlTypesSupport.TIMESTAMP_DATE_TYPE.createAdapterFactory(datePattern);
        sqlDateAdapterFactory = SqlTypesSupport.DATE_DATE_TYPE.createAdapterFactory(datePattern);
      }
    } else if (dateStyle != DateFormat.DEFAULT && timeStyle != DateFormat.DEFAULT) {
      dateAdapterFactory = DefaultDateTypeAdapter.DateType.DATE.createAdapterFactory(dateStyle, timeStyle);

      if (sqlTypesSupported) {
        sqlTimestampAdapterFactory = SqlTypesSupport.TIMESTAMP_DATE_TYPE.createAdapterFactory(dateStyle, timeStyle);
        sqlDateAdapterFactory = SqlTypesSupport.DATE_DATE_TYPE.createAdapterFactory(dateStyle, timeStyle);
      }
    } else {
      return;
    }

    factories.add(dateAdapterFactory);
    if (sqlTypesSupported) {
      factories.add(sqlTimestampAdapterFactory);
      factories.add(sqlDateAdapterFactory);
    }
  }
}
