package com.google.gson;

import java.lang.reflect.Type;

/**
 * Context for serialization that is passed to a custom 
 * serializer during invocation of its 
 * {@link JsonSerializer#toJson(Object, Type, JsonSerializationContext)} method
 * 
 * @author Inderjeet Singh
 */
public interface JsonSerializationContext {
  
  /**
   * Invokes default serialization on the specified object. 
   * @param src the object that needs to be serialized
   * @return a tree of {@link JsonElement}s corresponding to the serialized form of {@code src}
   */
  public JsonElement serialize(Object src);
  
  /**
   * Invokes default serialization on the specified object passing the 
   * specific type information. This method should be used if src is 
   * of a generic type
   * @param src the object that needs to be serialized
   * @param typeOfSrc the actual genericized type of src object
   * @return a tree of {@link JsonElement}s corresponding to the serialized form of {@code src}
   */
  public JsonElement serialize(Object src, Type typeOfSrc);
}