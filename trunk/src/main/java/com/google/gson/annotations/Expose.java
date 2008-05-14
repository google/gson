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

package com.google.gson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates this member should be exposed for Json
 * serialization or deserialization.
 * 
 * <p>This annotation has no effect unless you build {@link com.google.gson.Gson} 
 * with a {@link com.google.gson.GsonBuilder} and invoke 
 * {@link com.google.gson.GsonBuilder#excludeFieldsWithoutExposeAnnotation()}
 * method.</p>
 * 
 * <p>Here is an example of how this annotation is meant to be used: 
 * <p><code>
 * public class User {<br>
 * &nbsp; @Expose private String firstName;<br>
 * &nbsp; @Expose private String lastName;<br>
 * &nbsp; @Expose private String emailAddress;<br>
 * &nbsp; private String password;<br>
 * }<br>
 * </code></p>
 * If you created Gson with <code>new Gson()</code>, the <code>toJson()</code> and 
 * <code>fromJson()</code> methods will use the <code>password</code> field 
 * along-with <code>firstName</code>, <code>lastName</code>, and <code>emailAddress</code>  
 * for serialization and deserialization. However, if you created Gson with 
 * <code>Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()</code>
 * then the <code>toJson()</code> and <code>fromJson()</code> methods of Gson will exclude the 
 * <code>password</code> field. This is because the <code>password</code> field is not marked with 
 * the <code>@Expose</code> annotation. 
 * 
 * <p>Note that another way to acheive the same effect would have been to just mark the 
 * <code>password</code> field as <code>transient</code>, and Gson would have excluded it even with 
 * default settings. The <code>@Expose</code> annotation is useful in a style of programming where 
 * you want to explicitly specify all fields that should get considered for serialization or 
 * deserialization.    
 *
 * @author Inderjeet Singh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Expose {
  // This is a marker annotation with no additional properties 
}
