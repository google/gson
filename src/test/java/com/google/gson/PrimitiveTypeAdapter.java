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

import com.google.gson.internal.Primitives;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Handles type conversion from some object to some primitive (or primitive
 * wrapper instance).
 *
 * @author Joel Leitch
 */
final class PrimitiveTypeAdapter {

  @SuppressWarnings("unchecked")
  public <T> T adaptType(Object from, Class<T> to) {
    Class<?> aClass = Primitives.wrap(to);
    if (Primitives.isWrapperType(aClass)) {
      if (aClass == Character.class) {
        String value = from.toString();
        if (value.length() == 1) {
          return (T) (Character) from.toString().charAt(0);
        }
        throw new JsonParseException("The value: " + value + " contains more than a character.");
      }

      try {
        Constructor<?> constructor = aClass.getConstructor(String.class);
        return (T) constructor.newInstance(from.toString());
      } catch (NoSuchMethodException e) {
        throw new JsonParseException(e);
      } catch (IllegalAccessException e) {
        throw new JsonParseException(e);
      } catch (InvocationTargetException e) {
        throw new JsonParseException(e);
      } catch (InstantiationException e) {
        throw new JsonParseException(e);
      }
    } else if (Enum.class.isAssignableFrom(to)) {
      // Case where the type being adapted to is an Enum
      // We will try to convert from.toString() to the enum
      try {
        Method valuesMethod = to.getMethod("valueOf", String.class);
        return (T) valuesMethod.invoke(null, from.toString());
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new JsonParseException("Can not adapt type " + from.getClass() + " to " + to);
    }
  }
}
