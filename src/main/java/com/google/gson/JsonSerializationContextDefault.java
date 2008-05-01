package com.google.gson;

import java.lang.reflect.Type;

/**
 * An implementation of serialization context for Gson
 * 
 * @author Inderjeet Singh
 */
final class JsonSerializationContextDefault implements JsonSerializationContext {
  
  private final ObjectNavigatorFactory factory;
  private final ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers;

  JsonSerializationContextDefault(ObjectNavigatorFactory factory, 
      ParameterizedTypeHandlerMap<JsonSerializer<?>> serializers) {
    this.factory = factory;
    this.serializers = serializers;
  }
  
  public JsonElement serialize(Object src) {
    return serialize(src, src.getClass());
  }
  
  public JsonElement serialize(Object src, Type typeOfSrc) {
    ObjectNavigator on = factory.create(src, typeOfSrc);
    JsonSerializationVisitor visitor = new JsonSerializationVisitor(factory, serializers, this);
    on.accept(visitor);
    return visitor.getJsonElement();
  }
}
