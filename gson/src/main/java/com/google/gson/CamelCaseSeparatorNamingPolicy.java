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
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Converts the field name that uses camel-case define word separation into separate words that
 * are separated by the provided {@code separatorString}.
 *
 * <p>The following is an example:</p>
 * <pre>
 * class IntWrapper {
 *   public int integerField = 0;
 * }
 *
 * CamelCaseSeparatorNamingPolicy policy = new CamelCaseSeparatorNamingPolicy("_");
 * String translatedFieldName =
 *     policy.translateName(IntWrapper.class.getField("integerField"));
 *
 * assert("integer_Field".equals(translatedFieldName));
 * </pre>
 *
 * @author Joel Leitch
 */
final class CamelCaseSeparatorNamingPolicy extends RecursiveFieldNamingPolicy {
  private final String separatorString;

  /**
   * Constructs a new CamelCaseSeparatorNamingPolicy object that will add the
   * {@code separatorString} between each of the words separated by camel case.
   *
   * @param separatorString the string value to place between words
   * @throws IllegalArgumentException thrown if the {@code separatorString} parameter
   *         is null or empty.
   */
  public CamelCaseSeparatorNamingPolicy(String separatorString) {
    Preconditions.checkNotNull(separatorString);
    Preconditions.checkArgument(!"".equals(separatorString));
    this.separatorString = separatorString;
  }

  @Override
  protected String translateName(String target, Type fieldType,
      Collection<Annotation> annnotations) {
    StringBuilder translation = new StringBuilder();
    for (int i = 0; i < target.length(); i++) {
      char character = target.charAt(i);
      if (Character.isUpperCase(character) && translation.length() != 0) {
        translation.append(separatorString);
      }
      translation.append(character);
    }

    return translation.toString();
  }
}
