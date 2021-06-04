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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.ReflectionAccessFilter.FilterResult;
import com.google.gson.internal.reflect.ReflectionAccessor;
import com.google.gson.reflect.TypeToken;

/**
 * Returns a function that can construct an instance of a requested type.
 */
public final class ConstructorConstructor {
  private final Map<Type, InstanceCreator<?>> instanceCreators;
  private final List<ReflectionAccessFilter> reflectionFilters;
  private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();

  public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators,
      List<ReflectionAccessFilter> reflectionFilters) {
    this.instanceCreators = instanceCreators;
    this.reflectionFilters = reflectionFilters;
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

    FilterResult filterResult = ReflectionAccessFilterHelper.getFilterResult(reflectionFilters, rawType);
    boolean blockInaccessible = filterResult == FilterResult.BLOCK_INACCESSIBLE;
    if (blockInaccessible || filterResult == FilterResult.ALLOW) {
      ObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType, blockInaccessible);
      if (defaultConstructor != null) {
        return defaultConstructor;
      }
    }

    ObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(type, rawType);
    if (defaultImplementation != null) {
      return defaultImplementation;
    }

    // Check whether type is instantiable; otherwise ReflectionAccessFilter recommendation
    // of adjusting filter suggested below is irrelevant since it would not solve the problem
    final String exceptionMessage = UnsafeAllocator.checkInstantiable(rawType);
    if (exceptionMessage != null) {
      return new ObjectConstructor<T>() {
        @Override public T construct() {
          throw new JsonIOException(exceptionMessage);
        }
      };
    }

    // Consider usage of Unsafe as reflection, so don't use if BLOCK_ALL
    // Additionally, since it is not calling any constructor at all, don't use if BLOCK_INACCESSIBLE
    if (filterResult == FilterResult.ALLOW) {
      // finally try unsafe
      return newUnsafeAllocator(type, rawType);
    } else {
      final String message = "Unable to create instance of " + rawType + "; ReflectionAccessFilter "
          + "does not permit using reflection or Unsafe. Register an InstanceCreator or a TypeAdapter "
          + "for this type or adjust the access filter to allow using reflection.";
      return new ObjectConstructor<T>() {
        @Override public T construct() {
          throw new JsonIOException(message);
        }
      };
    }
  }

  private <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType, boolean blockInaccessible) {
    final Constructor<? super T> constructor;
    try {
      constructor = rawType.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      return null;
    }

    if (blockInaccessible && !ReflectionAccessFilterHelper.canAccess(constructor, null)) {
      final String message = "Unable to invoke no-args constructor of " + rawType + "; "
          + "constructor is not accessible and ReflectionAccessFilter does not permit making "
          + "it accessible. Register an InstanceCreator or a TypeAdapter for this type, change "
          + "the visibility of the constructor or adjust the access filter.";
      return new ObjectConstructor<T>() {
        @Override public T construct() {
          throw new JsonIOException(message);
        }
      };
    }
    accessor.makeAccessible(constructor);

    return new ObjectConstructor<T>() {
      @SuppressWarnings("unchecked") // T is the same raw type as is requested
      @Override public T construct() {
        try {
          Object[] args = null;
          return (T) constructor.newInstance(args);
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
  @SuppressWarnings("unchecked") // use runtime checks to guarantee that 'T' is what it is
  private <T> ObjectConstructor<T> newDefaultImplementationConstructor(
      final Type type, Class<? super T> rawType) {
    if (Collection.class.isAssignableFrom(rawType)) {
      if (SortedSet.class.isAssignableFrom(rawType)) {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new TreeSet<Object>();
          }
        };
      } else if (EnumSet.class.isAssignableFrom(rawType)) {
        return new ObjectConstructor<T>() {
          @SuppressWarnings("rawtypes")
          @Override public T construct() {
            if (type instanceof ParameterizedType) {
              Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
              if (elementType instanceof Class) {
                return (T) EnumSet.noneOf((Class)elementType);
              } else {
                throw new JsonIOException("Invalid EnumSet type: " + type.toString());
              }
            } else {
              throw new JsonIOException("Invalid EnumSet type: " + type.toString());
            }
          }
        };
      } else if (Set.class.isAssignableFrom(rawType)) {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new LinkedHashSet<Object>();
          }
        };
      } else if (Queue.class.isAssignableFrom(rawType)) {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new ArrayDeque<Object>();
          }
        };
      } else {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new ArrayList<Object>();
          }
        };
      }
    }

    if (Map.class.isAssignableFrom(rawType)) {
      if (ConcurrentNavigableMap.class.isAssignableFrom(rawType)) {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new ConcurrentSkipListMap<Object, Object>();
          }
        };
      } else if (ConcurrentMap.class.isAssignableFrom(rawType)) {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new ConcurrentHashMap<Object, Object>();
          }
        };
      } else if (SortedMap.class.isAssignableFrom(rawType)) {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new TreeMap<Object, Object>();
          }
        };
      } else if (type instanceof ParameterizedType && !(String.class.isAssignableFrom(
          TypeToken.get(((ParameterizedType) type).getActualTypeArguments()[0]).getRawType()))) {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new LinkedHashMap<Object, Object>();
          }
        };
      } else {
        return new ObjectConstructor<T>() {
          @Override public T construct() {
            return (T) new LinkedTreeMap<String, Object>();
          }
        };
      }
    }

    return null;
  }

  private <T> ObjectConstructor<T> newUnsafeAllocator(
      final Type type, final Class<? super T> rawType) {
    return new ObjectConstructor<T>() {
      private final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
      @SuppressWarnings("unchecked")
      @Override public T construct() {
        try {
          Object newInstance = unsafeAllocator.newInstance(rawType);
          return (T) newInstance;
        } catch (Exception e) {
          throw new RuntimeException(("Unable to invoke no-args constructor for " + type + ". "
              + "Registering an InstanceCreator with Gson for this type may fix this problem."), e);
        }
      }
    };
  }

  @Override public String toString() {
    return instanceCreators.toString();
  }
}
