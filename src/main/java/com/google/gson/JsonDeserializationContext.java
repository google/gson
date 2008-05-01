package com.google.gson;

import java.lang.reflect.Type;

/**
 * Context for deserialization that is passed to a custom deserializer
 * during invocation of its 
 * {@link JsonDeserializer#fromJson(Type, JsonElement, JsonDeserializationContext)}
 * method
 * 
 * @author Inderjeet Singh
 */
public interface JsonDeserializationContext {
  
  /**
   * Invokes default deserialization on the specified object.
   * @param <T> The type of the deserialized object
   * @param typeOfT type of the expected return value
   * @param json the parse tree
   * @return An object of type typeOfT
   * @throws JsonParseException if the parse tree does not contain expected data
   */
  public <T> T deserialize(Type typeOfT, JsonElement json) throws JsonParseException;
}