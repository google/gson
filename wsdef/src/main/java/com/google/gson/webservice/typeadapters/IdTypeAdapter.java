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
package com.google.gson.webservice.typeadapters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.webservice.definition.rest.Id;

/**
 * Type adapter for converting an Id to its serialized form
 *
 * @author inder
 *
 */
public final class IdTypeAdapter implements JsonSerializer<Id<?>>, JsonDeserializer<Id<?>> {

  @Override
  public JsonElement serialize(Id<?> src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.getValue());
  }

  @Override
  public Id<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (!(typeOfT instanceof ParameterizedType)) {
      throw new JsonParseException("Id of unknown type: " + typeOfT);
    }
    ParameterizedType parameterizedType = (ParameterizedType) typeOfT;
    // Since Id takes only one TypeVariable, the actual type corresponding to the first
    // TypeVariable is the Type we are looking for
    Type typeOfId = parameterizedType.getActualTypeArguments()[0];
    return Id.get(json.getAsLong(), typeOfId);
  }
}
