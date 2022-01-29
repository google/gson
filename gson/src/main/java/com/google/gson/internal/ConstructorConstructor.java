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

package com.google.gson.internal;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.internal.reflect.ReflectionHelper;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Returns a function that can construct an instance of a requested type.
 */
public final class ConstructorConstructor {
  private final Map<Type, InstanceCreator<?>> instanceCreators;
  private final boolean useJdkUnsafe;

  public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators, boolean useJdkUnsafe) {
    this.instanceCreators = instanceCreators;
    this.useJdkUnsafe = useJdkUnsafe;
  }

  public <T> ObjectConstructor<T> get(TypeToken<T> typeToken) {
    final Type type = typeToken.getType();
    final Class<? super T> rawType = typeToken.getRawType();

    // first try an instance creator

    @SuppressWarnings("unchecked") // types must agree
    final InstanceCreator<T> typeCreator = (InstanceCreator<T>) instanceCreators.get(type);
    if (typeCreator != null) {
      return new ObjectConstructor<T>() {
        @Override public T construct() {
          return typeCreator.createInstance(type);
        }
      };
    }

    // Next try raw type match for instance creators
    @SuppressWarnings("unchecked") // types must agree
    final InstanceCreator<T> rawTypeCreator =
        (InstanceCreator<T>) instanceCreators.get(rawType);
    if (rawTypeCreator != null) {
      return new ObjectConstructor<T>() {
        @Override public T construct() {
          return rawTypeCreator.createInstance(type);
        }
      };
    }

    ObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType);
    if (defaultConstructor != null) {
      return defaultConstructor;
    }

    ObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(type, rawType);
    if (defaultImplementation != null) {
      return defaultImplementation;
    }

    // finally try unsafe
    return newUnsafeAllocator(rawType);
  }

  private <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType) {
    final Constructor<? super T> constructor;
    try {
      constructor = rawType.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      return null;
    }

    final String exceptionMessage = ReflectionHelper.tryMakeAccessible(constructor);
    if (exceptionMessage != null) {
      /*
       * Create ObjectConstructor which throws exception.
       * This keeps backward compatibility (compared to returning `null` which
       * would then choose another way of creating object).
       * And it supports types which are only serialized but not deserialized
       * (compared to directly throwing exception here), e.g. when runtime type
       * of object is inaccessible, but compile-time type is accessible.
       */
      return new ObjectConstructor<T>() {
        @Override
        public T construct() {
          // New exception is created every time to avoid keeping reference
          // to exception with potentially long stack trace, causing a
          // memory leak
          throw new JsonIOException(exceptionMessage);
        }
      };
    }

    return new ObjectConstructor<T>() {
      @Override public T construct() {
        try {
          @SuppressWarnings("unchecked") // T is the same raw type as is requested
          T newInstance = (T) constructor.newInstance();
          return newInstance;
        } catch (InstantiationException e) {
          // TODO: JsonParseException ?
          throw new RuntimeException("Failed to invoke " + constructor + " with no args", e);
        } catch (InvocationTargetException e) {
          // TODO: don't wrap if cause is unchecked!
          // TODO: JsonParseException ?
          throw new RuntimeException("Failed to invoke " + constructor + " with no args",
              e.getTargetException());
        } catch (IllegalAccessException e) {
          throw new AssertionError(e);
        }
      }
    };
  }

  /**
   * Constructors for common interface types like Map and List and their
   * subtypes.
   */
  private static <T> ObjectConstructor<T> newDefaultImplementationConstructor(
      final Type type, Class<? super T> rawType) {

    if (Collection.class.isAssignableFrom(rawType)) {
      @SuppressWarnings("unchecked")
      ObjectConstructor<T> constructor = (ObjectConstructor<T>) newCollectionConstructor(type, rawType);
      return constructor;
    }

    if (Map.class.isAssignableFrom(rawType)) {
      @SuppressWarnings("unchecked")
      ObjectConstructor<T> constructor = (ObjectConstructor<T>) newMapConstructor(type, rawType);
      return constructor;
    }

    // Unsupported type; try other means of creating constructor
    return null;
  }

  private static ObjectConstructor<? extends Collection<? extends Object>> newCollectionConstructor(
      final Type type, Class<?> rawType) {

    if (EnumSet.class.isAssignableFrom(rawType)) {
      return new ObjectConstructor<EnumSet<?>>() {
        @Override public EnumSet<?> construct() {
          if (type instanceof ParameterizedType) {
            Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (elementType instanceof Class) {
              @SuppressWarnings({"unchecked", "rawtypes"})
              EnumSet<?> set = EnumSet.noneOf((Class) elementType);
              return set;
            } else {
              throw new JsonIOException("Invalid EnumSet type: " + type.toString());
            }
          } else {
            throw new JsonIOException("Invalid EnumSet type: " + type.toString());
          }
        }
      };
    }
    // First try List implementation
    else if (rawType.isAssignableFrom(ArrayList.class)) {
      return new ObjectConstructor<ArrayList<Object>>() {
        @Override public ArrayList<Object> construct() {
          return new ArrayList<Object>();
        }
      };
    }
    // Then try Set implementation
    else if (rawType.isAssignableFrom(LinkedHashSet.class)) {
      return new ObjectConstructor<LinkedHashSet<Object>>() {
        @Override public LinkedHashSet<Object> construct() {
          return new LinkedHashSet<Object>();
        }
      };
    }
    // Then try SortedSet / NavigableSet implementation
    else if (rawType.isAssignableFrom(TreeSet.class)) {
      return new ObjectConstructor<TreeSet<Object>>() {
        @Override public TreeSet<Object> construct() {
          return new TreeSet<Object>();
        }
      };
    }
    // Then try Queue implementation
    else if (rawType.isAssignableFrom(ArrayDeque.class)) {
      return new ObjectConstructor<ArrayDeque<Object>>() {
        @Override public ArrayDeque<Object> construct() {
          return new ArrayDeque<Object>();
        }
      };
    }

    // Was unable to create matching Collection constructor
    return null;
  }

  private static boolean hasStringKeyType(Type mapType) {
    // If mapType is not parameterized, assume it might have String as key type
    if (!(mapType instanceof ParameterizedType)) {
      return true;
    }

    Type[] typeArguments = ((ParameterizedType) mapType).getActualTypeArguments();
    if (typeArguments.length == 0) {
      return false;
    }
    return TypeToken.get(typeArguments[0]).getRawType() == String.class;
  }

  private static ObjectConstructor<? extends Map<? extends Object, Object>> newMapConstructor(Type type, Class<?> rawType) {
    // First try Map implementation
    /*
     * Legacy special casing for Map<String, ...> to avoid DoS from colliding String hashCode
     * values for older JDKs; use own LinkedTreeMap<String, Object> instead
     */
    if (rawType.isAssignableFrom(LinkedHashMap.class) && !hasStringKeyType(type)) {
      return new ObjectConstructor<LinkedHashMap<Object, Object>>() {
        @Override public LinkedHashMap<Object, Object> construct() {
          return new LinkedHashMap<Object, Object>();
        }
      };
    } else if (rawType.isAssignableFrom(LinkedTreeMap.class)) {
      return new ObjectConstructor<LinkedTreeMap<String, Object>>() {
        @Override public LinkedTreeMap<String, Object> construct() {
          return new LinkedTreeMap<String, Object>();
        }
      };
    }
    // Then try SortedMap / NavigableMap implementation
    else if (rawType.isAssignableFrom(TreeMap.class)) {
      return new ObjectConstructor<TreeMap<Object, Object>>() {
        @Override public TreeMap<Object, Object> construct() {
          return new TreeMap<Object, Object>();
        }
      };
    }
    // Then try ConcurrentMap implementation
    else if (rawType.isAssignableFrom(ConcurrentHashMap.class)) {
      return new ObjectConstructor<ConcurrentHashMap<Object, Object>>() {
        @Override public ConcurrentHashMap<Object, Object> construct() {
          return new ConcurrentHashMap<Object, Object>();
        }
      };
    }
    // Then try ConcurrentNavigableMap implementation
    else if (rawType.isAssignableFrom(ConcurrentSkipListMap.class)) {
      return new ObjectConstructor<ConcurrentSkipListMap<Object, Object>>() {
        @Override public ConcurrentSkipListMap<Object, Object> construct() {
          return new ConcurrentSkipListMap<Object, Object>();
        }
      };
    }

    // Was unable to create matching Map constructor
    return null;
  }

  private <T> ObjectConstructor<T> newUnsafeAllocator(final Class<? super T> rawType) {
    if (useJdkUnsafe) {
      return new ObjectConstructor<T>() {
        private final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
        @Override public T construct() {
          try {
            @SuppressWarnings("unchecked")
            T newInstance = (T) unsafeAllocator.newInstance(rawType);
            return newInstance;
          } catch (Exception e) {
            throw new RuntimeException(("Unable to create instance of " + rawType + ". "
                + "Registering an InstanceCreator or a TypeAdapter for this type, or adding a no-args "
                + "constructor may fix this problem."), e);
          }
        }
      };
    } else {
      final String exceptionMessage = "Unable to create instance of " + rawType + "; usage of JDK Unsafe "
          + "is disabled. Registering an InstanceCreator or a TypeAdapter for this type, adding a no-args "
          + "constructor, or enabling usage of JDK Unsafe may fix this problem.";
      return new ObjectConstructor<T>() {
        @Override public T construct() {
          throw new JsonIOException(exceptionMessage);
        }
      };
    }
  }

  @Override public String toString() {
    return instanceCreators.toString();
  }
}
