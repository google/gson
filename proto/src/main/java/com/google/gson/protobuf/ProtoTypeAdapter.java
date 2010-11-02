// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.gson.protobuf;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gson type adapter for protocol buffers
 *
 * @author Inderjeet Singh
 */
public class ProtoTypeAdapter implements JsonSerializer<GeneratedMessage>,
    JsonDeserializer<GeneratedMessage> {

  @Override
  public JsonElement serialize(GeneratedMessage src, Type typeOfSrc,
      JsonSerializationContext context) {
    JsonObject ret = new JsonObject();
    final Map<FieldDescriptor, Object> fields = src.getAllFields();

    for (Map.Entry<FieldDescriptor, Object> fieldPair : fields.entrySet()) {
      final FieldDescriptor desc = fieldPair.getKey();
      if (desc.isRepeated()) {
        List<?> fieldList = (List<?>) fieldPair.getValue();
        if (fieldList.size() != 0) {
          JsonArray array = new JsonArray();
          for (Object o : fieldList) {
            array.add(context.serialize(o));
          }
          ret.add(desc.getName(), array);
        }
      } else {
        ret.add(desc.getName(), context.serialize(fieldPair.getValue()));
      }
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  @Override
  public GeneratedMessage deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    try {
      JsonObject jsonObject = json.getAsJsonObject();
      Class<? extends GeneratedMessage> protoClass =
        (Class<? extends GeneratedMessage>) typeOfT; 
      try {
        // Invoke the ProtoClass.newBuilder() method
        Object protoBuilder = getCachedMethod(protoClass, "newBuilder")
          .invoke(null);
        Class<?> builderClass = protoBuilder.getClass();

        Descriptor protoDescriptor = (Descriptor) getCachedMethod(
            protoClass, "getDescriptor").invoke(null);
        // Call setters on all of the available fields
        for (FieldDescriptor fieldDescriptor : protoDescriptor.getFields()) {
          String name = fieldDescriptor.getName();
          if (jsonObject.has(name)) {
            JsonElement jsonElement = jsonObject.get(name);
            String fieldName = name + "_";
            Field field = protoClass.getDeclaredField(fieldName);
            Type fieldType = field.getGenericType();
            Object fieldValue = context.deserialize(jsonElement, fieldType);
            Method method = getCachedMethod(
              builderClass, "setField", FieldDescriptor.class, Object.class);
            method.invoke(protoBuilder, fieldDescriptor, fieldValue);
          }
        }
        
        // Invoke the build method to return the final proto
        return (GeneratedMessage) getCachedMethod(builderClass, "build")
            .invoke(protoBuilder);
      } catch (SecurityException e) {
        throw new JsonParseException(e);
      } catch (NoSuchMethodException e) {
        throw new JsonParseException(e);
      } catch (IllegalArgumentException e) {
        throw new JsonParseException(e);
      } catch (IllegalAccessException e) {
        throw new JsonParseException(e);
      } catch (InvocationTargetException e) {
        throw new JsonParseException(e);
      }
    } catch (Exception e) {
      throw new JsonParseException("Error while parsing proto: ", e);
    }
  }

  private static Method getCachedMethod(Class<?> clazz, String methodName,
      Class<?>... methodParamTypes) throws NoSuchMethodException {
    Map<Class<?>, Method> mapOfMethods = mapOfMapOfMethods.get(methodName);
    if (mapOfMethods == null) {
      mapOfMethods = new HashMap<Class<?>, Method>();
      mapOfMapOfMethods.put(methodName, mapOfMethods);
    }
    Method method = mapOfMethods.get(clazz);
    if (method == null) {
      method = clazz.getMethod(methodName, methodParamTypes);
      mapOfMethods.put(clazz, method);
    }
    return method;
  }

  private static Map<String, Map<Class<?>, Method>> mapOfMapOfMethods =
    new HashMap<String, Map<Class<?>, Method>>();
}