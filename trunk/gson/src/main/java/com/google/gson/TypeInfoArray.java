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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * Class to extract information about types used to define a generic array.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class TypeInfoArray extends TypeInfo {
  private final Class<?> componentRawType;
  private final Type secondLevel;

  TypeInfoArray(Type type) {
    super(type);
    Class<?> rootComponentType = rawClass;
    while (rootComponentType.isArray()) {
      rootComponentType = rootComponentType.getComponentType();
    }
    this.componentRawType = rootComponentType;
    this.secondLevel = extractSecondLevelType(actualType, rawClass);
  }

  private static Type extractSecondLevelType(Type actualType, Class<?> rawClass) {
    return actualType instanceof GenericArrayType ?
        ((GenericArrayType) actualType).getGenericComponentType() : rawClass.getComponentType();
  }

  /**
   * @return the raw type unwrapped of the second level of array.
   * If the object is (single-dimensional or multi-dimensional) array, it is the class of the
   * elements of the array. For example, this method returns Foo.class for Foo[].
   * It will return Foo[].class for Foo[][].  For Foo&lt;String&gt;[][] types, it will return the 
   * type representing Foo&lt;String&gt;[] 
   * (i.e. <code>new TypeToken<Foo<String>[]>() {}.getType()</code>).
   */
  public Type getSecondLevelType() {
    return secondLevel;
  }

  /**
   * @return the raw type of the root component.
   * If the object is a single-dimensional array then the component type is the class of an
   * element of the array.
   * If the object is a multi-dimensional array then the component type is the class of the
   * inner-most array element. For example, the This method will return Foo.class for Foo[][][].
   */
  public Class<?> getComponentRawType() {
    return componentRawType;
  }
}
