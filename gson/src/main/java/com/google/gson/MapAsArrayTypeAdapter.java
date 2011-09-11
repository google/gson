/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.gson;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Adapts maps containing complex keys as arrays of map entries.
 *
 * <h3>Maps as JSON objects</h3>
 * The standard GSON map type adapter converts Java {@link Map Maps} to JSON
 * Objects. This requires that map keys can be serialized as strings; this is
 * insufficient for some key types. For example, consider a map whose keys are
 * points on a grid. The default JSON form encodes reasonably: <pre>   {@code
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
 * An alternative approach taken by this type adapter is to encode maps as
 * arrays of map entries. Each map entry is a two element array containing a key
 * and a value. This approach is more flexible because any type can be used as
 * the map's key; not just strings. But it's also less portable because the
 * receiver of such JSON must be aware of the map entry convention.
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
 *
 * <p>This adapter returns regular JSON objects for maps whose keys are not
 * complex. A key is complex if its JSON-serialized form is an array or an
 * object.
 */
final class MapAsArrayTypeAdapter
    implements JsonSerializer<Map<?, ?>>, JsonDeserializer<Map<?, ?>> {

  public Map<?, ?> deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    // TODO
    throw new UnsupportedOperationException();
  }

  public JsonElement serialize(Map<?, ?> src, Type typeOfSrc, JsonSerializationContext context) {
    // TODO
    throw new UnsupportedOperationException();
  }
}
