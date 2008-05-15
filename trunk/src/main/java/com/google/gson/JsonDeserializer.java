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
 * Interface representing a custom deserializer for Json. You should write a custom deserializer, if 
 * you are not happy with the default deserialization done by Gson. You will also need to register
 * this deserializer through {@link GsonBuilder#registerTypeAdapter(Type, Object)}. 
 * 
 * <p>Let us look at example where defining a deserializer will be useful. The <code>Id</code> class 
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
 * <p>The default deserialization of <code>Id(com.foo.MyObject.class, 20L)</code> will require the 
 * Json string to be <code>{"clazz":com.foo.MyObject,"value":20}</code>. Suppose, you already know
 * the type of the field that the <code>Id</code> will be deserialized into, and hence just want to 
 * deserialize it from a Json string <code>20</code>. You can achieve that by writing a custom 
 * deserializer:</p>
 * 
 * <p><code>
 * class IdDeserializer implements JsonDeserializer&lt;Id&gt;() {<br>
 *  &nbsp; public Id fromJson(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
 *  throws JsonParseException {<br>
 *  &nbsp; &nbsp; return (Id) new Id((Class)typeOfT, id.getValue());<br>
 *  }
 * </code></p>   
 * You will also need to register <code>IdDeserializer</code> with Gson as follows: 
 * <p><code>
 * Gson gson = new GsonBuilder().registerTypeAdapter(new IdDeserializer()).create();<br>
 * </code></p>
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 *
 * @param <T> type for which the deserializer is being registered. It is possible that a 
 * deserializer may be asked to deserialize a specific generic type of the T.
 */
public interface JsonDeserializer<T> {
  
  /**
   * Gson invokes this call-back method during deserialization when it encounters a field of the 
   * specified type. 
   * 
   * @param json The Json data being deserialized
   * @param typeOfT The type of the Object to deserialize to
   * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
   * @throws JsonParseException if json is not in the expected format of {@code typeofT}
   */
  public T fromJson(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
      throws JsonParseException;
}
