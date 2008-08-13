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

import java.lang.reflect.Type;

/**
 * Class to extract information about types used to define a generic array.
 *
 * @author Joel Leitch
 */
final class ArrayTypeInfo {
  private final Class<?> rawTypeOfArray;
  private final Class<?> componentRawType;
  private final Class<?> secondLevel;

  public ArrayTypeInfo(Type type) {
    Preconditions.checkArgument(TypeUtils.isArray(type));
    this.rawTypeOfArray = TypeUtils.toRawClass(type);
    Class<?> rootComponentType = rawTypeOfArray;
    while (rootComponentType.isArray()) {
      rootComponentType = rootComponentType.getComponentType();
    }
    componentRawType = rootComponentType;
    this.secondLevel = rawTypeOfArray.getComponentType();
  }

  /**
   * @return the raw type associated with this array
   */
  public Class<?> getRawType() {
    return rawTypeOfArray;
  }

  /**
   * @return the raw type unwrapped of the second level of array.
   * If the object is (single-dimensional or multi-dimensional) array, it is the class of the
   * elements of the array. For example, this method returns Foo.class for Foo[].
   * It will return Foo[].class for Foo[][]
   */
  public Class<?> getSecondLevelClass() {
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
