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
 * This interface is implemented to create instances of a class that does not define a no-args
 * constructor. If you can modify the class, you should instead add a private, or public 
 * no-args constructor. However, that is not possible for library classes, such as JDK classes, or
 * a third-party library that you do not have source-code of. In such cases, you should define an 
 * instance creator for the class. Implementations of this interface should be registered with 
 * {@link GsonBuilder#registerTypeAdapter(Type, Object)} method before Gson will be able to use 
 * them.   
 * <p>Let us look at an example where defining an InstanceCreator might be useful. The 
 * <code>Id</code> class defined below does not have a default no-args constructor.   
 * <p><code>
 * public class Id&lt;T&gt; {<br>
 *  &nbsp; private final Class&lt;T&gt; clazz;<br>
 *  &nbsp; private final long value;<br>
 *  &nbsp; public Id(Class&lt;T&gt; clazz, long value) {<br>
 *  &nbsp; &nbsp; this.clazz = clazz;<br>
 *  &nbsp; &nbsp; this.value = value;<br>
 *  &nbsp; }<br>
 * }
 * </code></p>
 * 
 * <p>If Gson encounters an object of type <code>Id</code> during deserialization, it will throw an 
 * exception. The easiest way to solve this problem will be to add a (public or private) no-args 
 * constructor as follows: 
 * 
 * <p><code>
 * private Id() {<br>
 *  &nbsp; this(Object.class, 0L);<br>
 * }
 * </code></p> 
 * 
 * However, let us assume that the developer does not have access to the source-code of the 
 * <code>Id</code> class, or does not want to define a no-args constructor for it. The developer 
 * can solve this problem by defining an <code>InstanceCreator</code> for <code>Id</code>: 
 *  
 * <p><code>
 * class IdInstanceCreator implements InstanceCreator&lt;Id&gt;() {<br>
 *  &nbsp; public Id createInstance(Type type) {<br>
 *  &nbsp; &nbsp; return new Id(Object.class, 0L); <br>
 *  &nbsp; }
 * </code></p> 
 * 
 * Note that it does not matter what the fields of the created instance contain since Gson will 
 * overwrite them with the deserialized values specified in Json. You should also ensure that a
 * <i>new</i> object is returned, not a common object since its fields will be overwritten. 
 * The developer will need to register <code>IdInstanceCreator</code> with Gson as follows: 
 * <p><code>
 * Gson gson = new GsonBuilder().registerTypeAdapter(new IdInstanceCreator()).create();<br>
 * </code></p>
 *  
 * @param <T> the type of object that will be created by this implementation.
 *
 * @author Joel Leitch
 */
public interface InstanceCreator<T> {

  /**
   * Gson invokes this call-back method during deserialization to create an instance of the 
   * specified type. The fields of the returned instance are overwritten with the data present
   * in the Json. Since the prior contents of the object are destroyed and overwritten, do not 
   * return an instance that is useful elsewhere. In particular, do not return a common instance, 
   * always use <code>new</code> to create a new instance.   
   * 
   * @param type the parameterized T represented as a {@link Type}
   * @return a default object instance of type T
   */
  public T createInstance(Type type);
}
