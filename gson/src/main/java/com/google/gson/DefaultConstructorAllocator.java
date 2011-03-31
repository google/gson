/*
 * Copyright (C) 2011 Google Inc.
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


import java.lang.reflect.Constructor;

/**
 * Use the default constructor on the class to instantiate an object.
 *
 * @author Joel Leitch
 */
final class DefaultConstructorAllocator {
  private static final Constructor<Null> NULL_CONSTRUCTOR = createNullConstructor();

  private final Cache<Class<?>, Constructor<?>> constructorCache;

  public DefaultConstructorAllocator() {
    this(200);
  }

  public DefaultConstructorAllocator(int cacheSize) {
    constructorCache = new LruCache<Class<?>, Constructor<?>>(cacheSize);
  }

  // for testing purpose
  final Cache<Class<?>, Constructor<?>> getConstructorCache() {
    return constructorCache;
  }

  private static final Constructor<Null> createNullConstructor() {
    try {
      return getNoArgsConstructor(Null.class);
    } catch (Exception e) {
      return null;
    }
  }

  public <T> T newInstance(Class<T> c) throws Exception {
    Constructor<T> constructor = findConstructor(c);
    return (constructor != null) ? constructor.newInstance() : null;
  }

  @SuppressWarnings("unchecked")
  private <T> Constructor<T> findConstructor(Class<T> c) {
    Constructor<T> cachedElement = (Constructor<T>) constructorCache.getElement(c);
    if (cachedElement != null) {
      if (cachedElement == NULL_CONSTRUCTOR) {
        return null;
      } else {
        return cachedElement;
      }
    }

    Constructor<T> noArgsConstructor = getNoArgsConstructor(c);
    if (noArgsConstructor != null) {
      constructorCache.addElement(c, noArgsConstructor);
    } else {
      constructorCache.addElement(c, NULL_CONSTRUCTOR);
    }
    return noArgsConstructor;
  }

  private static <T> Constructor<T> getNoArgsConstructor(Class<T> c) {
    try {
      Constructor<T> declaredConstructor = c.getDeclaredConstructor();
      declaredConstructor.setAccessible(true);
      return declaredConstructor;
    } catch (Exception e) {
      return null;
    }
  }

  // placeholder class for Null constructor
  private static final class Null {
  }
}
