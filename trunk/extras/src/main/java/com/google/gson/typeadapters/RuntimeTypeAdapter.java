/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.typeadapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapts values whose runtime type may differ from their declaration type. This
 * is necessary when a field's type is not the same type that GSON should create
 * when deserializing that field. For example, consider these types:
 * <pre>   {@code
 *   abstract class Shape {
 *     int x;
 *     int y;
 *   }
 *   class Circle extends Shape {
 *     int radius;
 *   }
 *   class Rectangle extends Shape {
 *     int width;
 *     int height;
 *   }
 *   class Diamond extends Shape {
 *     int width;
 *     int height;
 *   }
 *   class Drawing {
 *     Shape bottomShape;
 *     Shape topShape;
 *   }
 * }</pre>
 * <p>Without additional type information, the serialized JSON is ambiguous. Is
 * the bottom shape in this drawing a rectangle or a diamond? <pre>   {@code
 *   {
 *     "bottomShape": {
 *       "width": 10,
 *       "height": 5,
 *       "x": 0,
 *       "y": 0
 *     },
 *     "topShape": {
 *       "radius": 2,
 *       "x": 4,
 *       "y": 1
 *     }
 *   }}</pre>
 * This class addresses this problem by adding type information to the
 * serialized JSON and honoring that type information when the JSON is
 * deserialized: <pre>   {@code
 *   {
 *     "bottomShape": {
 *       "type": "Diamond",
 *       "width": 10,
 *       "height": 5,
 *       "x": 0,
 *       "y": 0
 *     },
 *     "topShape": {
 *       "type": "Circle",
 *       "radius": 2,
 *       "x": 4,
 *       "y": 1
 *     }
 *   }}</pre>
 * Both the type field name ({@code "type"}) and the type labels ({@code
 * "Rectangle"}) are configurable.
 *
 * <h3>Registering Types</h3>
 * Create a {@code RuntimeTypeAdapter} by passing the base type and type field
 * name to the {@link #create} factory method. If you don't supply an explicit
 * type field name, {@code "type"} will be used. <pre>   {@code
 *   RuntimeTypeAdapter<Shape> shapeAdapter
 *       = RuntimeTypeAdapter.create(Shape.class, "type");
 * }</pre>
 * Next register all of your subtypes. Every subtype must be explicitly
 * registered. This protects your application from injection attacks. If you
 * don't supply an explicit type label, the type's simple name will be used.
 * <pre>   {@code
 *   shapeAdapter.registerSubtype(Rectangle.class, "Rectangle");
 *   shapeAdapter.registerSubtype(Circle.class, "Circle");
 *   shapeAdapter.registerSubtype(Diamond.class, "Diamond");
 * }</pre>
 * Finally, register the type adapter in your application's GSON builder:
 * <pre>   {@code
 *   Gson gson = new GsonBuilder()
 *       .registerTypeAdapter(Shape.class, shapeAdapter)
 *       .create();
 * }</pre>
 * Like {@code GsonBuilder}, this API supports chaining: <pre>   {@code
 *   RuntimeTypeAdapter<Shape> shapeAdapter = RuntimeTypeAdapter.create(Shape.class)
 *       .registerSubtype(Rectangle.class)
 *       .registerSubtype(Circle.class)
 *       .registerSubtype(Diamond.class);
 * }</pre>
 */
public final class RuntimeTypeAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
  private final Class<?> baseType;
  private final String typeFieldName;
  private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<String, Class<?>>();
  private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<Class<?>, String>();

  private RuntimeTypeAdapter(Class<?> baseType, String typeFieldName) {
    if (typeFieldName == null || baseType == null) {
      throw new NullPointerException();
    }
    this.baseType = baseType;
    this.typeFieldName = typeFieldName;
  }

  /**
   * Creates a new runtime type adapter for {@code c} using {@code "type"} as
   * the type field name.
   */
  public static <T> RuntimeTypeAdapter<T> create(Class<T> c) {
    return create(c, "type");
  }

  /**
   * Creates a new runtime type adapter using for {@code c} using {@code
   * typeFieldName} as the type field name. Type field names are case sensitive.
   */
  public static <T> RuntimeTypeAdapter<T> create(Class<T> c, String typeFieldName) {
    return new RuntimeTypeAdapter<T>(c, typeFieldName);
  }

  /**
   * Registers {@code type} identified by {@code label}. Labels are case
   * sensitive.
   *
   * @throws IllegalArgumentException if either {@code type} or {@code label}
   *     have already been registered on this type adapter.
   */
  public RuntimeTypeAdapter<T> registerSubtype(Class<? extends T> type, String label) {
    if (type == null || label == null) {
      throw new NullPointerException();
    }
    if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
      throw new IllegalArgumentException("types and labels must be unique");
    }
    labelToSubtype.put(label, type);
    subtypeToLabel.put(type, label);
    return this;
  }

  /**
   * Registers {@code type} identified by its {@link Class#getSimpleName simple
   * name}. Labels are case sensitive.
   *
   * @throws IllegalArgumentException if either {@code type} or its simple name
   *     have already been registered on this type adapter.
   */
  public RuntimeTypeAdapter<T> registerSubtype(Class<? extends T> type) {
    return registerSubtype(type, type.getSimpleName());
  }

  public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
    Class<?> srcType = src.getClass();
    String label = subtypeToLabel.get(srcType);
    if (label == null) {
      throw new JsonParseException("cannot serialize " + srcType.getName()
          + "; did you forget to register a subtype?");
    }
    JsonElement serialized = context.serialize(src, srcType);
    final JsonObject jsonObject = serialized.getAsJsonObject();
    if (jsonObject.has(typeFieldName)) {
      throw new JsonParseException("cannot serialize " + srcType.getName()
          + " because it already defines a field named " + typeFieldName);
    }
    JsonObject clone = new JsonObject();
    clone.add(typeFieldName, new JsonPrimitive(label));
    for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
      clone.add(e.getKey(), e.getValue());
    }
    return clone;
  }

  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonElement labelJsonElement = json.getAsJsonObject().remove(typeFieldName);
    if (labelJsonElement == null) {
      throw new JsonParseException("cannot deserialize " + typeOfT
          + " because it does not define a field named " + typeFieldName);
    }
    String label = labelJsonElement.getAsString();
    Class<?> subtype = labelToSubtype.get(label);
    if (subtype == null) {
      throw new JsonParseException("cannot deserialize " + baseType + " subtype named "
          + label + "; did you forget to register a subtype?");
    }
    @SuppressWarnings("unchecked") // registration requires that subtype extends T
    T result = (T) context.deserialize(json, subtype);
    return result;
  }
}
