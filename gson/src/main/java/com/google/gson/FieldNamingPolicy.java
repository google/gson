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

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * An enumeration that defines a few standard naming conventions for JSON field names.
 * This enumeration should be used in conjunction with {@link com.google.gson.GsonBuilder}
 * to configure a {@link com.google.gson.Gson} instance to properly translate Java field
 * names into the desired JSON field names.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public enum FieldNamingPolicy implements FieldNamingStrategy {

  /**
   * Using this naming policy with Gson will ensure that the field name is
   * unchanged.
   */
  IDENTITY() {
    @Override public String translateName(Field f) {
      return f.getName();
    }
  },

  /**
   * Using this naming policy with Gson will ensure that the first "letter" of the Java
   * field name is capitalized when serialized to its JSON form.
   *
   * <p>Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---&gt; SomeFieldName</li>
   *   <li>_someFieldName ---&gt; _SomeFieldName</li>
   * </ul>
   */
  UPPER_CAMEL_CASE() {
    @Override public String translateName(Field f) {
      return upperCaseFirstLetter(f.getName());
    }
  },

  /**
   * Using this naming policy with Gson will ensure that the first "letter" of the Java
   * field name is capitalized when serialized to its JSON form and the words will be
   * separated by a space.
   *
   * <p>Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---&gt; Some Field Name</li>
   *   <li>_someFieldName ---&gt; _Some Field Name</li>
   * </ul>
   *
   * @since 1.4
   */
  UPPER_CAMEL_CASE_WITH_SPACES() {
    @Override public String translateName(Field f) {
      return upperCaseFirstLetter(separateCamelCase(f.getName(), ' '));
    }
  },

  /**
   * Using this naming policy with Gson will modify the Java Field name from its camel cased
   * form to an upper case field name where each word is separated by an underscore (_).
   *
   * <p>Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---&gt; SOME_FIELD_NAME</li>
   *   <li>_someFieldName ---&gt; _SOME_FIELD_NAME</li>
   *   <li>aStringField ---&gt; A_STRING_FIELD</li>
   *   <li>aURL ---&gt; A_U_R_L</li>
   * </ul>
   *
   * @since 2.9.0
   */
  UPPER_CASE_WITH_UNDERSCORES() {
    @Override public String translateName(Field f) {
      return separateCamelCase(f.getName(), '_').toUpperCase(Locale.ENGLISH);
    }
  },

  /**
   * Using this naming policy with Gson will modify the Java Field name from its camel cased
   * form to a lower case field name where each word is separated by an underscore (_).
   *
   * <p>Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---&gt; some_field_name</li>
   *   <li>_someFieldName ---&gt; _some_field_name</li>
   *   <li>aStringField ---&gt; a_string_field</li>
   *   <li>aURL ---&gt; a_u_r_l</li>
   * </ul>
   */
  LOWER_CASE_WITH_UNDERSCORES() {
    @Override public String translateName(Field f) {
      return separateCamelCase(f.getName(), '_').toLowerCase(Locale.ENGLISH);
    }
  },

  /**
   * Using this naming policy with Gson will modify the Java Field name from its camel cased
   * form to a lower case field name where each word is separated by a dash (-).
   *
   * <p>Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---&gt; some-field-name</li>
   *   <li>_someFieldName ---&gt; _some-field-name</li>
   *   <li>aStringField ---&gt; a-string-field</li>
   *   <li>aURL ---&gt; a-u-r-l</li>
   * </ul>
   * Using dashes in JavaScript is not recommended since dash is also used for a minus sign in
   * expressions. This requires that a field named with dashes is always accessed as a quoted
   * property like {@code myobject['my-field']}. Accessing it as an object field
   * {@code myobject.my-field} will result in an unintended JavaScript expression.
   *
   * @since 1.4
   */
  LOWER_CASE_WITH_DASHES() {
    @Override public String translateName(Field f) {
      return separateCamelCase(f.getName(), '-').toLowerCase(Locale.ENGLISH);
    }
  },

  /**
   * Using this naming policy with Gson will modify the Java Field name from its camel cased
   * form to a lower case field name where each word is separated by a dot (.).
   *
   * <p>Here are a few examples of the form "Java Field Name" ---&gt; "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---&gt; some.field.name</li>
   *   <li>_someFieldName ---&gt; _some.field.name</li>
   *   <li>aStringField ---&gt; a.string.field</li>
   *   <li>aURL ---&gt; a.u.r.l</li>
   * </ul>
   * Using dots in JavaScript is not recommended since dot is also used for a member sign in
   * expressions. This requires that a field named with dots is always accessed as a quoted
   * property like {@code myobject['my.field']}. Accessing it as an object field
   * {@code myobject.my.field} will result in an unintended JavaScript expression.
   *
   * @since 2.8.4
   */
  LOWER_CASE_WITH_DOTS() {
    @Override public String translateName(Field f) {
      return separateCamelCase(f.getName(), '.').toLowerCase(Locale.ENGLISH);
    }
  };

  /**
   * Converts the field name that uses camel-case define word separation into
   * separate words that are separated by the provided {@code separator}.
   */
  static String separateCamelCase(String name, char separator) {
    StringBuilder translation = new StringBuilder();
    for (int i = 0, length = name.length(); i < length; i++) {
      char character = name.charAt(i);
      if (Character.isUpperCase(character) && translation.length() != 0) {
        translation.append(separator);
      }
      translation.append(character);
    }
    return translation.toString();
  }

  /**
   * Ensures the JSON field names begins with an upper case letter.
   */
  static String upperCaseFirstLetter(String s) {
    int length = s.length();
    for (int i = 0; i < length; i++) {
      char c = s.charAt(i);
      if (Character.isLetter(c)) {
        if (Character.isUpperCase(c)) {
          return s;
        }

        char uppercased = Character.toUpperCase(c);
        // For leading letter only need one substring
        if (i == 0) {
          return uppercased + s.substring(1);
        } else {
          return s.substring(0, i) + uppercased + s.substring(i + 1);
        }
      }
    }

    return s;
  }
}
