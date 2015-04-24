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
import java.lang.reflect.Method;

import com.google.gson.annotations.GsonGetter;
import com.google.gson.annotations.GsonSetter;

public enum MethodAndFieldNamingPolicy implements MethodAndFieldNamingStrategy {

	
	DEFAULT() {
    public String translateName(Field f) {
      return f.getName();
    }

	@Override
	public String translateName(Method m) {
		return cleanMethodName(m, /*lowerCaseFirstLetter*/true);
	}
  },
	
  IDENTITY() {
    public String translateName(Field f) {
      return f.getName();
    }

	@Override
	public String translateName(Method m) {
		return cleanMethodName(m, /*lowerCaseFirstLetter*/false);
	}
  },

  UPPER_CAMEL_CASE() {
    public String translateName(Field f) {
      return upperCaseFirstLetter(f.getName());
    }

	@Override
	public String translateName(Method m) {
		String cleanedMethodName = cleanMethodName(m, /*lowerCaseFirstLetter*/false);
		return upperCaseFirstLetter(cleanedMethodName);
	}
  },

  UPPER_CAMEL_CASE_WITH_SPACES() {
    public String translateName(Field f) {
      return upperCaseFirstLetter(separateCamelCase(f.getName(), " "));
    }

	@Override
	public String translateName(Method m) {
		String cleanedMethodName = cleanMethodName(m, /*lowerCaseFirstLetter*/false);
		return upperCaseFirstLetter(separateCamelCase(cleanedMethodName, " "));
	}
  },

  LOWER_CASE_WITH_UNDERSCORES() {
    public String translateName(Field f) {
      return separateCamelCase(f.getName(), "_").toLowerCase();
    }

	@Override
	public String translateName(Method m) {
		String cleanedMethodName = cleanMethodName(m, /*lowerCaseFirstLetter*/true);
      return separateCamelCase(cleanedMethodName, "_").toLowerCase();
	}
  },

  LOWER_CASE_WITH_DASHES() {
    public String translateName(Field f) {
      return separateCamelCase(f.getName(), "-").toLowerCase();
    }

	@Override
	public String translateName(Method m) {
		String cleanedMethodName = cleanMethodName(m, /*lowerCaseFirstLetter*/true);
      return separateCamelCase(cleanedMethodName, "-").toLowerCase();
	}
  };

  private static String separateCamelCase(String name, String separator) {
    StringBuilder translation = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char character = name.charAt(i);
      if (Character.isUpperCase(character) && translation.length() != 0) {
        translation.append(separator);
      }
      translation.append(character);
    }
    return translation.toString();
  }

  private static String upperCaseFirstLetter(String name) {
    StringBuilder fieldNameBuilder = new StringBuilder();
    int index = 0;
    char firstCharacter = name.charAt(index);

    while (index < name.length() - 1) {
      if (Character.isLetter(firstCharacter)) {
        break;
      }

      fieldNameBuilder.append(firstCharacter);
      firstCharacter = name.charAt(++index);
    }

    if (index == name.length()) {
      return fieldNameBuilder.toString();
    }

    if (!Character.isUpperCase(firstCharacter)) {
      String modifiedTarget = modifyString(Character.toUpperCase(firstCharacter), name, ++index);
      return fieldNameBuilder.append(modifiedTarget).toString();
    } else {
      return name;
    }
  }
  
  private static String lowerCaseFirstLetter(String name) {
    StringBuilder fieldNameBuilder = new StringBuilder();
    int index = 0;
    char firstCharacter = name.charAt(index);

    while (index < name.length() - 1) {
      if (Character.isLetter(firstCharacter)) {
        break;
      }

      fieldNameBuilder.append(firstCharacter);
      firstCharacter = name.charAt(++index);
    }

    if (index == name.length()) {
      return fieldNameBuilder.toString();
    }

    if (!Character.isLowerCase(firstCharacter)) {
      String modifiedTarget = modifyString(Character.toLowerCase(firstCharacter), name, ++index);
      return fieldNameBuilder.append(modifiedTarget).toString();
    } else {
      return name;
    }
  }

  private static String modifyString(char firstCharacter, String srcString, int indexOfSubstring) {
    return (indexOfSubstring < srcString.length())
        ? firstCharacter + srcString.substring(indexOfSubstring)
        : String.valueOf(firstCharacter);
  }
  
  private static String cleanMethodName(Method method, boolean lowercaseFirstLetter) {
	 GsonGetter g = method.getAnnotation(GsonGetter.class); 
	 GsonSetter s = method.getAnnotation(GsonSetter.class);
	 String methodName = method.getName();
	 String methodPrefix = methodName.substring(0, 3);
	 String methodWithoutPrefix = method.getName().substring(3);
	 if (g != null && !methodPrefix.equals("get")) {
		 throw new RuntimeException("Gson did not know how to translate your getter");
	 } else if (s != null && !methodPrefix.equals("set")) {
		 throw new RuntimeException("Gson did not know how to translate your setter");
	 } else if (methodWithoutPrefix.length() == 0) {
		 throw new RuntimeException("Gson did not know how to translate your getter/setter");
	 } else if (g == null && s == null){
		 throw new RuntimeException("Should never get here");
	 }
	 if (lowercaseFirstLetter) {
		 return lowerCaseFirstLetter(methodWithoutPrefix);
	 }
	 return methodWithoutPrefix;
	 
  }
}