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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can be used to check the validity of a JSON field name.
 *
 * <p>The primary use of this object is to ensure that any Java fields that use the
 * {@link com.google.gson.annotations.SerializedName} annotation is providing valid JSON
 * field names.  This will make the code fail-fast rather than letting the invalid
 * field name propagate to the client and it fails to parse.</p>
 *
 * @author Joel Leitch
 */
class JsonFieldNameValidator {
  private static final Pattern JSON_FIELD_NAME_PATTERN =
      Pattern.compile("(^[a-zA-Z][a-zA-Z0-9\\$_\\-]*$)|(^[\\$_][a-zA-Z][a-zA-Z0-9\\$_\\-]*$)");

  
  /**
   * Performs validation on the JSON field name to ensure it is a valid field name.
   *
   * @param fieldName the name of the field to validate
   * @return {@code fieldName} if it is a valid JSON field name
   * @throws IllegalArgumentException if the field name is an invalid JSON field name
   */
  public String validate(String fieldName) {
    Preconditions.checkNotNull(fieldName);
    Preconditions.checkArgument(!"".equals(fieldName.trim()));

    Matcher matcher = JSON_FIELD_NAME_PATTERN.matcher(fieldName);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(fieldName + " is not a valid JSON field name.");
    } else {
      return fieldName;
    }
  }
}
