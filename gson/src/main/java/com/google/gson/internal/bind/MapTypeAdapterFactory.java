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

package com.google.gson.internal.bind;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapts maps to either JSON objects or JSON arrays.
 *
 * <h3>Maps as JSON objects</h3>
 * For primitive keys or when complex map key serialization is not enabled, this
 * converts Java {@link Map Maps} to JSON Objects. This requires that map keys
 * can be serialized as strings; this is insufficient for some key types. For
 * example, consider a map whose keys are points on a grid. The default JSON
 * form encodes reasonably: <pre>   {@code
 *   Map<Point, String> original = new LinkedHashMap<Point, String>();
 *   original.put(new Point(5, 6), "a");
 *   original.put(new Point(8, 8), "b");
 *   System.out.println(gson.toJson(original, type));
 * }</pre>
 * The above code prints this JSON object:<pre>   {@code
 *   {
 *     "(5,6)": "a",
 *     "(8,8)": "b"
 *   }
 * }</pre>
 * But GSON is unable to deserialize this value because the JSON string name is
 * just the {@link Object#toString() toString()} of the map key. Attempting to
 * convert the above JSON to an object fails with a parse exception:
 * <pre>com.google.gson.JsonParseException: Expecting object found: "(5,6)"
 *   at com.google.gson.JsonObjectDeserializationVisitor.visitFieldUsingCustomHandler
 *   at com.google.gson.ObjectNavigator.navigateClassFields
 *   ...</pre>
 *
 * <h3>Maps as JSON arrays</h3>
 * An alternative approach taken by this type adapter when it is required and
 * complex map key serialization is enabled is to encode maps as arrays of map
 * entries. Each map entry is a two element array containing a key and a value.
 * This approach is more flexible because any type can be used as the map's key;
 * not just strings. But it's also less portable because the receiver of such
 * JSON must be aware of the map entry convention.
 *
 * <p>Register this adapter when you are creating your GSON instance.
 * <pre>   {@code
 *   Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(Map.class, new MapAsArrayTypeAdapter())
 *     .create();
 * }</pre>
 * This will change the structure of the JSON emitted by the code above. Now we
 * get an array. In this case the arrays elements are map entries:
 * <pre>   {@code
 *   [
 *     [
 *       {
 *         "x": 5,
 *         "y": 6
 *       },
 *       "a",
 *     ],
 *     [
 *       {
 *         "x": 8,
 *         "y": 8
 *       },
 *       "b"
 *     ]
 *   ]
 * }</pre>
 * This format will serialize and deserialize just fine as long as this adapter
 * is registered.
 */
public final class MapTypeAdapterFactory implements TypeAdapter.Factory {
  private final ConstructorConstructor constructorConstructor;
  private final boolean complexMapKeySerialization;

  public MapTypeAdapterFactory(ConstructorConstructor constructorConstructor,
      boolean complexMapKeySerialization) {
    this.constructorConstructor = constructorConstructor;
    this.complexMapKeySerialization = complexMapKeySerialization;
  }

  public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Map.class.isAssignableFrom(rawType)) {
      return null;
    }

    Class<?> rawTypeOfSrc = $Gson$Types.getRawType(type);
    Type[] keyAndValueTypes = $Gson$Types.getMapKeyAndValueTypes(type, rawTypeOfSrc);
    TypeAdapter<?> keyAdapter = getKeyAdapter(context, keyAndValueTypes[0]);
    TypeAdapter<?> valueAdapter = context.getAdapter(TypeToken.get(keyAndValueTypes[1]));
    ObjectConstructor<T> constructor = constructorConstructor.getConstructor(typeToken);

    @SuppressWarnings("unchecked") // we don't define a type parameter for the key or value types
    TypeAdapter<T> result = new Adapter(context, keyAndValueTypes[0], keyAdapter,
        keyAndValueTypes[1], valueAdapter, constructor);
    return result;
  }

  /**
   * Returns a type adapter that writes the value as a string.
   */
  private TypeAdapter<?> getKeyAdapter(MiniGson context, Type keyType) {
    return (keyType == boolean.class || keyType == Boolean.class)
        ? TypeAdapters.BOOLEAN_AS_STRING
        : context.getAdapter(TypeToken.get(keyType));
  }

  private final class Adapter<K, V> extends TypeAdapter<Map<K, V>> {
    private final TypeAdapter<K> keyTypeAdapter;
    private final TypeAdapter<V> valueTypeAdapter;
    private final ObjectConstructor<? extends Map<K, V>> constructor;

    public Adapter(MiniGson context, Type keyType, TypeAdapter<K> keyTypeAdapter,
        Type valueType, TypeAdapter<V> valueTypeAdapter,
        ObjectConstructor<? extends Map<K, V>> constructor) {
      this.keyTypeAdapter =
        new TypeAdapterRuntimeTypeWrapper<K>(context, keyTypeAdapter, keyType);
      this.valueTypeAdapter =
        new TypeAdapterRuntimeTypeWrapper<V>(context, valueTypeAdapter, valueType);
      this.constructor = constructor;
    }

    public Map<K, V> read(JsonReader reader) throws IOException {
      JsonToken peek = reader.peek();
      if (peek == JsonToken.NULL) {
        reader.nextNull();
        return null;
      }

      Map<K, V> map = constructor.construct();

      if (peek == JsonToken.BEGIN_ARRAY) {
        reader.beginArray();
        while (reader.hasNext()) {
          reader.beginArray(); // entry array
          K key = keyTypeAdapter.read(reader);
          V value = valueTypeAdapter.read(reader);
          V replaced = map.put(key, value);
          if (replaced != null) {
            throw new JsonSyntaxException("duplicate key: " + key);
          }
          reader.endArray();
        }
        reader.endArray();
      } else {
        reader.beginObject();
        while (reader.hasNext()) {
          String keyString = reader.nextName();
          K key = keyTypeAdapter.fromJsonElement(new JsonPrimitive(keyString));
          V value = valueTypeAdapter.read(reader);
          V replaced = map.put(key, value);
          if (replaced != null) {
            throw new JsonSyntaxException("duplicate key: " + key);
          }
        }
        reader.endObject();
      }
      return map;
    }

    public void write(JsonWriter writer, Map<K, V> map) throws IOException {
      if (map == null) {
        writer.nullValue(); // TODO: better policy here?
        return;
      }

      if (!complexMapKeySerialization) {
        writer.beginObject();
        for (Map.Entry<K, V> entry : map.entrySet()) {
          writer.name(String.valueOf(entry.getKey()));
          valueTypeAdapter.write(writer, entry.getValue());
        }
        writer.endObject();
        return;
      }

      boolean hasComplexKeys = false;
      List<JsonElement> keys = new ArrayList<JsonElement>(map.size());

      List<V> values = new ArrayList<V>(map.size());
      for (Map.Entry<K, V> entry : map.entrySet()) {
        JsonElement keyElement = keyTypeAdapter.toJsonElement(entry.getKey());
        keys.add(keyElement);
        values.add(entry.getValue());
        hasComplexKeys |= keyElement.isJsonArray() || keyElement.isJsonObject();
      }

      if (hasComplexKeys) {
        writer.beginArray();
        for (int i = 0; i < keys.size(); i++) {
          writer.beginArray(); // entry array
          Streams.write(keys.get(i), writer);
          valueTypeAdapter.write(writer, values.get(i));
          writer.endArray();
        }
        writer.endArray();
      } else {
        writer.beginObject();
        for (int i = 0; i < keys.size(); i++) {
          JsonElement keyElement = keys.get(i);
          writer.name(keyToString(keyElement));
          valueTypeAdapter.write(writer, values.get(i));
        }
        writer.endObject();
      }
    }

    private String keyToString(JsonElement keyElement) {
      if (keyElement.isJsonPrimitive()) {
        JsonPrimitive primitive = keyElement.getAsJsonPrimitive();
        if (primitive.isNumber()) {
          return String.valueOf(primitive.getAsNumber());
        } else if (primitive.isBoolean()) {
          return Boolean.toString(primitive.getAsBoolean());
        } else if (primitive.isString()) {
          return primitive.getAsString();
        } else {
          throw new AssertionError();
        }
      } else if (keyElement.isJsonNull()) {
        return "null";
      } else {
        throw new AssertionError();
      }
    }
  }
}
