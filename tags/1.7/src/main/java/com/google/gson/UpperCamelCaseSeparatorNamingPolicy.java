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
 * A {@link FieldNamingStrategy2} that ensures the JSON field names consist of mixed
 * case letters starting with a capital and are separated by a particular
 * {@code separatorString}.
 *
 *<p>The following is an example:</p>
 * <pre>
 * class StringWrapper {
 *   public String AStringField = "abcd";
 * }
 *
 * UpperCamelCaseSeparatorNamingPolicy policy = new UpperCamelCaseSeparatorNamingPolicy("_");
 * String translatedFieldName =
 *     policy.translateName(StringWrapper.class.getField("AStringField"));
 *
 * assert("A_String_Field".equals(translatedFieldName));
 * </pre>
 *
 * @author Joel Leitch
 */
final class UpperCamelCaseSeparatorNamingPolicy extends CompositionFieldNamingPolicy {

  public UpperCamelCaseSeparatorNamingPolicy(String separatorString) {
    super(new CamelCaseSeparatorNamingPolicy(separatorString),
        new ModifyFirstLetterNamingPolicy(ModifyFirstLetterNamingPolicy.LetterModifier.UPPER));
  }
}
