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

/**
 * A {@link FieldNamingStrategy} that ensures the JSON field names begins with
 * an upper case letter.
 *
 *<p>The following is an example:</p>
 * <pre>
 * class StringWrapper {
 *   public String stringField = "abcd";
 *   public String _stringField = "efg";
 * }
 *
 * ModifyFirstLetterNamingPolicy policy =
 *     new ModifyFirstLetterNamingPolicy(LetterModifier.UPPER);
 * String translatedFieldName =
 *     policy.translateName(StringWrapper.class.getField("stringField"));
 *
 * assert("StringField".equals(translatedFieldName));
 *
 * String translatedFieldName =
 *     policy.translateName(StringWrapper.class.getField("_stringField"));
 *
 * assert("_StringField".equals(translatedFieldName));
 * </pre>
 *
 * @author Joel Leitch
 */
class ModifyFirstLetterNamingPolicy extends RecursiveFieldNamingPolicy {

  public enum LetterModifier {
    UPPER,
    LOWER;
  }

  private final LetterModifier letterModifier;

  /**
   * Creates a new ModifyFirstLetterNamingPolicy that will either modify the first letter of the
   * target name to either UPPER case or LOWER case depending on the {@code modifier} parameter.
   *
   * @param modifier the type of modification that should be performed
   * @throws IllegalArgumentException if {@code modifier} is null
   */
  public ModifyFirstLetterNamingPolicy(LetterModifier modifier) {
    Preconditions.checkNotNull(modifier);
    this.letterModifier = modifier;
  }

  @Override
  protected String translateName(String target, Type fieldType, Annotation[] annotations) {
    StringBuilder fieldNameBuilder = new StringBuilder();
    int index = 0;
    char firstCharacter = target.charAt(index);

    while (index < target.length() - 1) {
      if (Character.isLetter(firstCharacter)) {
        break;
      }

      fieldNameBuilder.append(firstCharacter);
      firstCharacter = target.charAt(++index);
    }

    if (index == target.length()) {
      return fieldNameBuilder.toString();
    }

    boolean capitalizeFirstLetter = (letterModifier == LetterModifier.UPPER);
    if (capitalizeFirstLetter && !Character.isUpperCase(firstCharacter)) {
      String modifiedTarget = modifyString(Character.toUpperCase(firstCharacter), target, ++index);
      return fieldNameBuilder.append(modifiedTarget).toString();
    } else if (!capitalizeFirstLetter && Character.isUpperCase(firstCharacter)) {
      String modifiedTarget = modifyString(Character.toLowerCase(firstCharacter), target, ++index);
      return fieldNameBuilder.append(modifiedTarget).toString();
    } else {
      return target;
    }
  }

  private String modifyString(char firstCharacter, String srcString, int indexOfSubstring) {
    if (indexOfSubstring < srcString.length()) {
      return firstCharacter + srcString.substring(indexOfSubstring);
    } else {
      return String.valueOf(firstCharacter);
    }
  }
}
