package com.google.gson;

import java.lang.reflect.Type;

import com.google.gson.reflect.ObjectNavigator;
import com.google.gson.reflect.ObjectNavigatorFactory;

/**
 * implementation of a deserialization context for Gson
 * 
 * @author Inderjeet Singh
 */
final class JsonDeserializationContext implements JsonDeserializer.Context {
  
  private final ObjectNavigatorFactory navigatorFactory;
  private final ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers;
  private final MappedObjectConstructor objectConstructor;
  private final TypeAdapter typeAdapter;

  JsonDeserializationContext(ObjectNavigatorFactory navigatorFactory, 
      ParameterizedTypeHandlerMap<JsonDeserializer<?>> deserializers, 
      MappedObjectConstructor objectConstructor, TypeAdapter typeAdapter) {
    this.navigatorFactory = navigatorFactory;
    this.deserializers = deserializers;
    this.objectConstructor = objectConstructor;
    this.typeAdapter = typeAdapter;
  }

  @SuppressWarnings("unchecked")
  public <T> T deserialize(Type typeOfT, JsonElement json) throws ParseException {
    if (json.isArray()) {
      return (T) fromJsonArray(typeOfT, json.getAsJsonArray(), this);
    } else if (json.isObject()) {
      return (T) fromJsonObject(typeOfT, json.getAsJsonObject(), this);
    } else if (json.isPrimitive()) {
      return (T) fromJsonPrimitive(typeOfT, json.getAsJsonPrimitive(), this);
    } else {
      throw new ParseException("Failed parsing JSON source: " + json + " to Json");
    }
  }  
  
  @SuppressWarnings("unchecked")
  private <T> T fromJsonArray(Type arrayType, JsonArray jsonArray, 
      JsonDeserializer.Context context) throws ParseException {
    JsonArrayDeserializationVisitor<T> visitor = new JsonArrayDeserializationVisitor<T>(
        jsonArray, arrayType, navigatorFactory, objectConstructor, typeAdapter, deserializers, 
        context);
    Object target = visitor.getTarget();
    ObjectNavigator on = navigatorFactory.create(target, arrayType);
    on.accept(visitor);
    return visitor.getTarget();
  }

  @SuppressWarnings("unchecked")
  private <T> T fromJsonObject(Type typeOfT, JsonObject jsonObject, 
      JsonDeserializer.Context context) throws ParseException {
    JsonObjectDeserializationVisitor<T> visitor = new JsonObjectDeserializationVisitor<T>(
        jsonObject, typeOfT, navigatorFactory, objectConstructor, typeAdapter, deserializers, 
        context);
    Object target = visitor.getTarget();
    ObjectNavigator on = navigatorFactory.create(target, typeOfT);
    on.accept(visitor);
    return visitor.getTarget();
  }
  
  @SuppressWarnings("unchecked")
  private <T> T fromJsonPrimitive(Type typeOfT, JsonPrimitive json, 
      JsonDeserializer.Context context) throws ParseException {
    JsonPrimitiveDeserializationVisitor<T> visitor = new JsonPrimitiveDeserializationVisitor<T>(
        json, typeOfT, navigatorFactory, objectConstructor, typeAdapter, deserializers, context);
    Object target = visitor.getTarget();
    ObjectNavigator on = navigatorFactory.create(target, typeOfT);
    on.accept(visitor);
    target = visitor.getTarget();
    if (typeOfT instanceof Class) {
      target = typeAdapter.adaptType(target, (Class) typeOfT);
    }
    return (T) target;    
  }
}
