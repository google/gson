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

import com.google.gson.internal.LruCache;
import com.google.gson.internal.Types;
import com.google.gson.internal.UnsafeAllocator;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * This class contains a mapping of all the application specific
 * {@link InstanceCreator} instances.  Registering an {@link InstanceCreator}
 * with this class will override the default object creation that is defined
 * by the ObjectConstructor that this class is wrapping.  Using this class
 * with the JSON framework provides the application with "pluggable" modules
 * to customize framework to suit the application's needs.
 *
 * @author Joel Leitch
 */
final class MappedObjectConstructor implements ObjectConstructor {
  private static final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();

  private static final LruCache<Class<?>, Constructor<?>> noArgsConstructorsCache =
      new LruCache<Class<?>, Constructor<?>>(500);
  private final ParameterizedTypeHandlerMap<InstanceCreator<?>> instanceCreatorMap;
  /**
   * We need a special null value to indicate that the class does not have a no-args constructor.
   * This helps avoid using reflection over and over again for such classes. For convenience, we
   * use the no-args constructor of this class itself since this class would never be
   * deserialized using Gson.
   */
  private static final Constructor<MappedObjectConstructor> NULL_VALUE =
    getNoArgsConstructorUsingReflection(MappedObjectConstructor.class);
  
  @SuppressWarnings("unused")
  private MappedObjectConstructor() {
    this(null);
  }

  public MappedObjectConstructor(
      ParameterizedTypeHandlerMap<InstanceCreator<?>> instanceCreators) {
    instanceCreatorMap = instanceCreators;
  }

  @SuppressWarnings("unchecked")
  public <T> T construct(Type typeOfT) {
    InstanceCreator<T> creator = (InstanceCreator<T>) instanceCreatorMap.getHandlerFor(typeOfT);
    if (creator != null) {
      return creator.createInstance(typeOfT);
    }
    return (T) constructWithNoArgConstructor(typeOfT);
  }

  public Object constructArray(Type type, int length) {
    return Array.newInstance(Types.getRawType(type), length);
  }

  @SuppressWarnings({"unchecked", "cast"})
  private <T> T constructWithNoArgConstructor(Type typeOfT) {
    try {
      Class<T> clazz = (Class<T>) Types.getRawType(typeOfT);
      Constructor<T> constructor = getNoArgsConstructor(clazz);
      return constructor == null
          ? unsafeAllocator.newInstance(clazz)
          : constructor.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(("Unable to invoke no-args constructor for " + typeOfT + ". "
          + "Register an InstanceCreator with Gson for this type may fix this problem."), e);
    }
  }

  private <T> Constructor<T> getNoArgsConstructor(Class<T> clazz) {
    @SuppressWarnings("unchecked")
    Constructor<T> constructor = (Constructor<T>)noArgsConstructorsCache.getElement(clazz);
    if (constructor == NULL_VALUE) {
      return null;
    }
    if (constructor == null) {
      constructor = getNoArgsConstructorUsingReflection(clazz);
      noArgsConstructorsCache.addElement(clazz, constructor);
    }
    return constructor == NULL_VALUE ? null : constructor;
  }

  @SuppressWarnings("unchecked")
  private static <T> Constructor<T> getNoArgsConstructorUsingReflection(Class<T> clazz) {
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor;
    } catch (Exception e) {
      return (Constructor<T>) NULL_VALUE;
    }
  }

  @Override
  public String toString() {
    return instanceCreatorMap.toString();
  }
}
