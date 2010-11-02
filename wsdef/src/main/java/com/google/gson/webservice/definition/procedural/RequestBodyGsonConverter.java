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
package com.google.gson.webservice.definition.procedural;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson type adapter for {@link RequestBody}. 
 * 
 * @author inder
 */
public class RequestBodyGsonConverter implements JsonSerializer<RequestBody>, 
  JsonDeserializer<RequestBody>, InstanceCreator<RequestBody> {

  private final RequestBodySpec spec;

  public RequestBodyGsonConverter(RequestBodySpec spec) {
    this.spec = spec;
  }
  
  @Override
  public JsonElement serialize(RequestBody src, Type typeOfSrc, 
      JsonSerializationContext context) {
    JsonObject root = new JsonObject();
    for(Map.Entry<String, Object> entry : src.entrySet()) {
      String key = entry.getKey();
      Type entryType = src.getSpec().getTypeFor(key);
      JsonElement value = context.serialize(entry.getValue(), entryType);
      root.add(key, value);        
    }
    return root;
  }

  @Override
  public RequestBody deserialize(JsonElement json, Type typeOfT, 
      JsonDeserializationContext context) throws JsonParseException {
    RequestBody.Builder builder = new RequestBody.Builder(spec);
    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
      String key = entry.getKey();
      Type entryType = spec.getTypeFor(key);
      Object value = context.deserialize(entry.getValue(), entryType);
      builder.put(key, value);
    }
    return builder.build();
  }

  @Override
  public RequestBody createInstance(Type type) {
    return new RequestBody.Builder(spec).build();
  }
}
