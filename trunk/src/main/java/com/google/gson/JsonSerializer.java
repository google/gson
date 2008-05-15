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

package com.google.gson;

import java.lang.reflect.Type;

/**
 * Interface representing a custom serializer for Json. You should write a custom serializer, if
 * you are not happy with the default serialization done by Gson. You will also need to register
 * this serializer through {@link GsonBuilder#registerTypeAdapter(Type, Object)}.
 *
 * <p>Let us look at example where defining a serializer will be useful. The <code>Id</code> class
 * defined below has two fields: <code>clazz</code> and <code>value</code>.
 *
 * <p><code>
 * public class Id&lt;T&gt; {<br>
 *  &nbsp; private final Class&lt;T&gt; clazz;<br>
 *  &nbsp; private final long value;<br>
 *  &nbsp; public Id(Class&lt;T&gt; clazz, long value) {<br>
 *  &nbsp; &nbsp; this.clazz = clazz;<br>
 *  &nbsp; &nbsp; this.value = value;<br>
 *  &nbsp; }<br>
 *  &nbsp; public long getValue() {<br>
 *  &nbsp; &nbsp; return value;<br>
 *  &nbsp; }<br>
 * }
 * </code></p>
 *
 * <p>The default serialization of <code>Id(com.foo.MyObject.class, 20L)</code> will be
 * <code>{"clazz":com.foo.MyObject,"value":20}</code>. Suppose, you just want the output to be the
 * value instead, which is 20 in this case. You can achieve that by writing a custom serializer:</p>
 *
 * <p><code>
 * class IdSerializer implements JsonSerializer&lt;Id&gt;() {<br>
 *  &nbsp;  public JsonElement toJson(Id id, Type typeOfId, JsonSerializationContext context) {<br>
 *  &nbsp; &nbsp; return new JsonPrimitive(id.getValue());<br>
 *  }
 * </code></p>
 * You will also need to register <code>IdSerializer</code> with Gson as follows:
 * <p><code>
 * Gson gson = new GsonBuilder().registerTypeAdapter(new IdSerializer()).create();<br>
 * </code></p>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 *
 * @param <T> type for which the serializer is being registered. It is possible that a serializer
 * may be asked to serialize a specific generic type of the T.
 */
public interface JsonSerializer<T> {

  /**
   * Gson invokes this call-back method during serialization when it encounters a field of the
   * specified type.
   *
   * @param src the object that needs to be converted to Json
   * @param typeOfSrc the actual type (fully genericized version) of
   *        the source object
   * @return a JsonElement corresponding to the specified object
   */
  public JsonElement toJson(T src, Type typeOfSrc, JsonSerializationContext context);
}
