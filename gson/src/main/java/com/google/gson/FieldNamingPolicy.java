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

/**
 * An enumeration that defines a few standard naming conventions for JSON field names.
 * This enumeration should be used in conjunction with {@link com.google.gson.GsonBuilder}
 * to configure a {@link com.google.gson.Gson} instance to properly translate Java field
 * names into the desired JSON field names.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public enum FieldNamingPolicy {
  /**
   * Using this naming policy with Gson will ensure that the first "letter" of the Java
   * field name is capitalized when serialized to its JSON form.
   *
   * <p>Here's a few examples of the form "Java Field Name" ---> "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---> SomeFieldName</li>
   *   <li>_someFieldName ---> _SomeFieldName</li>
   * </ul>
   */
  UPPER_CAMEL_CASE(new ModifyFirstLetterNamingPolicy(
      ModifyFirstLetterNamingPolicy.LetterModifier.UPPER)),

  /**
   * Using this naming policy with Gson will modify the Java Field name from its camel cased
   * form to a lower case field name where each word is separated by an underscore (_).
   *
   * <p>Here's a few examples of the form "Java Field Name" ---> "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---> some_field_name</li>
   *   <li>_someFieldName ---> _some_field_name</li>
   *   <li>aStringField ---> a_string_field</li>
   *   <li>aURL ---> a_u_r_l</li>
   * </ul>
   */
  LOWER_CASE_WITH_UNDERSCORES(new LowerCamelCaseSeparatorNamingPolicy("_")),
  
  /**
   * Using this naming policy with Gson will modify the Java Field name from its camel cased
   * form to a lower case field name where each word is separated by a dash (-).
   *
   * <p>Here's a few examples of the form "Java Field Name" ---> "JSON Field Name":</p>
   * <ul>
   *   <li>someFieldName ---> some-field-name</li>
   *   <li>_someFieldName ---> _some-field-name</li>
   *   <li>aStringField ---> a-string-field</li>
   *   <li>aURL ---> a-u-r-l</li>
   * </ul>
   * Using dashes in JavaScript is not recommended since dash is also used for a minus sign in
   * expressions. This requires that a field named with dashes is always accessed as a quoted
   * property like {@code myobject['my-field']}. Accessing it as an object field
   * {@code myobject.my-field} will result in an unintended javascript expression.
   * @since 1.4
   */
  LOWER_CASE_WITH_DASHES(new LowerCamelCaseSeparatorNamingPolicy("-"));

  private final FieldNamingStrategy namingPolicy;

  private FieldNamingPolicy(FieldNamingStrategy namingPolicy) {
    this.namingPolicy = namingPolicy;
  }

  FieldNamingStrategy getFieldNamingPolicy() {
    return namingPolicy;
  }
}
