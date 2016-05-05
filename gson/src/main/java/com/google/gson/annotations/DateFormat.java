/*
 * Copyright (C) 2014 Google Inc.
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

package com.google.gson.annotations;

import com.google.gson.DefaultDateTypeAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that accept a date format that GSON need to parse and format
 * dates while deserialization and serialization.
 *
 * <p>Here is an example of how this annotation is used:</p>
 * <pre>
 * public class Trip {
 *   &#64DateFormat("yyyy-MM-dd")
 *   private Date date;
 *
 *   private User(Date date) {
 *     this.date = date;
 *   }
 * }
 * </pre>
 *
 * Since Trip class specified date format in &#64DateFormat annotation, this format
 * will automatically be used to serialize/deserialize date field of Trip class. <br>
 *
 * Note: This annotation uses {@link DefaultDateTypeAdapter} internally, any other adapter
 * provided with {@link JsonAdapter} annotation will be ignored.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DateFormat {
  String value();
}
