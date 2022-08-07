/*
 * Copyright (C) 2012 Google Inc.
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

package com.google.gson.interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Use this annotation to indicate various interceptors for class instances after
 * they have been processed by Gson. For example, you can use it to validate an instance
 * after it has been deserialized from Json.
 * Here is an example of how this annotation is used:
 * <p>Here is an example of how this annotation is used:
 * <pre>
 * &#64;Intercept(postDeserialize=UserValidator.class)
 * public class User {
 *   String name;
 *   String password;
 *   String emailAddress;
 * }
 *
 * public class UserValidator implements JsonPostDeserializer&lt;User&gt; {
 *   public void postDeserialize(User user) {
 *     // Do some checks on user
 *     if (user.name == null || user.password == null) {
 *       throw new JsonParseException("name and password are required fields.");
 *     }
 *     if (user.emailAddress == null) {
 *       emailAddress = "unknown"; // assign a default value.
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Inderjeet Singh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercept {

  /**
   * Specify the class that provides the methods that should be invoked after an instance
   * has been deserialized.
   */
  @SuppressWarnings("rawtypes")
  public Class<? extends JsonPostDeserializer> postDeserialize();
}
