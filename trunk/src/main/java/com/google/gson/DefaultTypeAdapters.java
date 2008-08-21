package com.google.gson;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * List of all the default type adapters ({@link JsonSerializer}s, {@link JsonDeserializer}s,
 * and {@link InstanceCreator}s.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultTypeAdapters {

  static ParameterizedTypeHandlerMap<JsonSerializer<?>> getDefaultSerializers() {
    ParameterizedTypeHandlerMap<JsonSerializer<?>> map =
      new ParameterizedTypeHandlerMap<JsonSerializer<?>>();
    map.register(BigDecimal.class, new BigDecimalTypeAdapter());
    map.register(BigInteger.class, new BigIntegerTypeAdapter());

    map.addIfAbsent(DefaultJsonSerializers.getDefaultSerializers());
    return map;
  }

  static ParameterizedTypeHandlerMap<JsonDeserializer<?>> getDefaultDeserializers() {
    ParameterizedTypeHandlerMap<JsonDeserializer<?>> map =
      new ParameterizedTypeHandlerMap<JsonDeserializer<?>>();
    map.register(BigDecimal.class, new BigDecimalTypeAdapter());
    map.register(BigInteger.class, new BigIntegerTypeAdapter());

    map.addIfAbsent(DefaultJsonDeserializers.getDefaultDeserializers());
    return map;
  }

  static ParameterizedTypeHandlerMap<InstanceCreator<?>> getDefaultInstanceCreators() {
    ParameterizedTypeHandlerMap<InstanceCreator<?>> map =
      new ParameterizedTypeHandlerMap<InstanceCreator<?>>();
    map.register(BigDecimal.class, new BigDecimalTypeAdapter());
    map.register(BigInteger.class, new BigIntegerTypeAdapter());

    map.addIfAbsent(DefaultInstanceCreators.getDefaultInstanceCreators());
    return map;
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
}
