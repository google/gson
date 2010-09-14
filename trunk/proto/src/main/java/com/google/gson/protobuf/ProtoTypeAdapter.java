/*
 * Copyright (C) 2010 Google Inc.
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
package com.google.gson.protobuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;

import java.lang.reflect.Type;
import java.util.Map;

public class ProtoTypeAdapter implements JsonSerializer<Message> {

  @Override
  public JsonElement serialize(Message msg, Type typeOfMsg, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    Map<FieldDescriptor, Object> allFields = msg.getAllFields();
    for (Map.Entry<FieldDescriptor, Object> entry : allFields.entrySet()) {
      FieldDescriptor key = entry.getKey();
      Object value = entry.getValue();
      JavaType javaType = key.getJavaType();
      Class<?> type = toJavaType(javaType);
      JsonElement element = context.serialize(value, type);
      obj.add(key.getName(), element);
    }
    return obj;
  }

  private Class<?> toJavaType(JavaType javaType) {
    switch (javaType) {
    case BOOLEAN:
      return Boolean.class;
    case BYTE_STRING:
      return String.class;
    case DOUBLE:
      return double.class;
    case ENUM:
      return Enum.class;
    case FLOAT:
      return float.class;
    case INT:
      return int.class;
    case LONG:
      return long.class;
    case MESSAGE:
      return Message.class;
    case STRING:
      return String.class;
    }
    return Object.class;
  }
}
