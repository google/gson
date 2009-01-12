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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * A mechanism for providing custom field naming in Gson.  This allows the client code to translate
 * field names into a particular convention that is not supported as a normal Java field
 * declaration rules.  For example, Java does not support "-" characters in a field name.
 *
 * @author Joel Leitch
 */
abstract class RecursiveFieldNamingPolicy implements FieldNamingStrategy {

  public final String translateName(Field f) {
    Preconditions.checkNotNull(f);
    return translateName(f.getName(), f.getGenericType(), f.getAnnotations());
  }

  /**
   * Performs the specific string translation.
   *
   * @param target the string object that will be manipulation/translated
   * @param fieldType the actual type value of the field
   * @param annotations the annotations set on the field
   * @return the translated field name
   */
  protected abstract String translateName(String target, Type fieldType, Annotation[] annotations);
}
